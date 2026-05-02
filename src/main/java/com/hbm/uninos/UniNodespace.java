package com.hbm.uninos;

import com.hbm.config.GeneralConfig;
import com.hbm.lib.DirPos;
import com.hbm.main.MainRegistry;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Unified Nodespace, a Nodespace for all applications.
 * "Nodespace" is an invisible "dimension" where nodes exist, a node is basically the "soul" of a tile entity with networking capabilities.
 * Instead of tile entities having to find each other which is costly and assumes the tiles are loaded, tiles simply create nodes at their
 * respective position in nodespace, the nodespace itself handles stuff like connections which can also happen in unloaded chunks.
 * A node is so to say the "soul" of a tile entity which can act independent of its "body".
 * Edit: now every NodeNet have their own "dimension"
 *
 * @author hbm
 */
public final class UniNodespace {

    private static final Reference2ObjectOpenHashMap<INetworkProvider<?>, PerTypeNodeManager<?, ?, ?, ?>> managers = new Reference2ObjectOpenHashMap<>();
    private static int reapTimer = 0;

    private UniNodespace() {
    }

    public static <N extends NodeNet<R, P, L, N>, L extends GenNode<N>, R, P> L getNode(World world, BlockPos pos, INetworkProvider<N> type) {
        return getManagerFor(type).getNode(world, pos);
    }

    public static <N extends NodeNet<R, P, L, N>, L extends GenNode<N>, R, P> void createNode(World world, L node) {
        getManagerFor(node.networkProvider).createNode(world, node);
    }

    public static <N extends NodeNet<R, P, L, N>, L extends GenNode<N>, R, P> void destroyNode(World world, BlockPos pos, INetworkProvider<N> type) {
        getManagerFor(type).destroyNode(world, pos);
    }

    @SuppressWarnings("unchecked, rawtypes")
    public static <N extends NodeNet<R, P, L, N>, L extends GenNode<N>, R, P> void destroyNode(World world, L node) {
        if (world == null || node == null) return;
        INetworkProvider<N> provider = node.networkProvider;
        if (provider != null) {
            PerTypeNodeManager<R, P, L, N> manager = (PerTypeNodeManager<R, P, L, N>) managers.get(provider);
            if (manager != null) {
                if (!manager.destroyNode(world, node)) {
                    MainRegistry.logger.warn("Failed to destroy node {}", node.getClass().getName());
                    Thread.dumpStack();
                }
                return;
            }
        }
        MainRegistry.logger.warn("[UniNodeSpace] Node {} attempts to be destroyed through mismatched type provider {}", node.getClass().getName(), provider);
        // Fallback: if provider is null/mismatched or manager isn't present yet, locate the node by identity.
        ObjectIterator<Reference2ObjectMap.Entry<INetworkProvider<?>, PerTypeNodeManager<?, ?, ?, ?>>> it = managers.reference2ObjectEntrySet().fastIterator();
        while (it.hasNext()) {
            PerTypeNodeManager manager = it.next().getValue();
            if (manager.destroyNode(world, node)) return;
        }
        if (node.net != null) node.net.destroy();
        node.expired = true;
    }

    /**
     * <code>GeneralConfig.enableThreadedNodeSpaceUpdate</code> is safe provided that:
     * <ul>
     *   <li>While this method is running, no other thread may call createNode/destroyNode or otherwise mutate UniNodespace / PerTypeNodeManager state.</li>
     *   <li>TileEntity logic that interacts with NodeNet must not run concurrently with this method.</li>
     * </ul>
     */
    public static CompletableFuture<Void> updateNodespaceAsync(Executor executor) {
        final World[] currentWorlds = DimensionManager.getWorlds();
        if (currentWorlds.length == 0) return CompletableFuture.completedFuture(null);
        if (!GeneralConfig.enableThreadedNodeSpaceUpdate) {
            return CompletableFuture.runAsync(() -> {
                for (World world : currentWorlds) {
                    ObjectIterator<Reference2ObjectMap.Entry<INetworkProvider<?>, PerTypeNodeManager<?, ?, ?, ?>>> iterator = managers.reference2ObjectEntrySet()
                                                                                                                                   .fastIterator();
                    while (iterator.hasNext()) {
                        iterator.next().getValue().updateForWorld(world);
                    }
                }
                updateNetworks();
            }, executor).exceptionally(UniNodespace::rethrowAsRuntime);
        }
        List<CompletableFuture<Void>> phase1 = new ArrayList<>(currentWorlds.length * Math.max(1, managers.size()));
        for (World world : currentWorlds) {
            ObjectIterator<Reference2ObjectMap.Entry<INetworkProvider<?>, PerTypeNodeManager<?, ?, ?, ?>>> iterator = managers.reference2ObjectEntrySet()
                                                                                                                           .fastIterator();
            while (iterator.hasNext()) {
                PerTypeNodeManager<?, ?, ?, ?> manager = iterator.next().getValue();
                phase1.add(CompletableFuture.runAsync(() -> manager.updateForWorld(world), executor));
            }
        }

        return CompletableFuture.allOf(phase1.toArray(new CompletableFuture[0]))
                                .thenRunAsync(UniNodespace::resetAllNetTrackers, executor)
                                .thenCompose(_ -> updateAllNetsAsync(executor))
                                .thenRunAsync(UniNodespace::reapNetworks, executor)
                                .exceptionally(UniNodespace::rethrowAsRuntime);
    }

    private static void resetAllNetTrackers() {
        for (PerTypeNodeManager<?, ?, ?, ?> manager : managers.values()) {
            manager.resetTrackers();
        }
    }

    private static CompletableFuture<Void> updateAllNetsAsync(Executor executor) {
        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        for (PerTypeNodeManager<?, ?, ?, ?> manager : managers.values()) {
            manager.collectNetUpdateTasks(executor, tasks);
        }
        if (tasks.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
    }

    private static Void rethrowAsRuntime(Throwable t) {
        if (t instanceof CompletionException && t.getCause() != null) t = t.getCause();
        if (t instanceof RuntimeException) throw (RuntimeException) t;
        if (t instanceof Error) throw (Error) t;
        throw new RuntimeException("Exception during UniNodespace update", t);
    }

    static void removeActiveNet(NodeNet<?, ?, ?, ?> net) {
        if (net.links.isEmpty()) {
            ObjectIterator<Reference2ObjectMap.Entry<INetworkProvider<?>, PerTypeNodeManager<?, ?, ?, ?>>> iterator = managers.reference2ObjectEntrySet()
                                                                                                                           .fastIterator();
            while (iterator.hasNext()) {
                PerTypeNodeManager<?, ?, ?, ?> manager = iterator.next().getValue();
                manager.removeActiveNet(net);
            }
            return;
        }
        GenNode<?> node = net.links.first();
        PerTypeNodeManager<?, ?, ?, ?> manager = managers.get(node.networkProvider);
        if (manager != null) manager.removeActiveNet(net);
    }

    private static void updateNetworks() {
        for (PerTypeNodeManager<?, ?, ?, ?> manager : managers.values()) manager.resetTrackers();
        for (PerTypeNodeManager<?, ?, ?, ?> manager : managers.values()) manager.updateNetworks();
        reapNetworks();
    }

    private static void updateReapTimer() {
        if (reapTimer <= 0) reapTimer = 5 * 60 * 20;
        else reapTimer--;
    }

    private static void reapNetworks() {
        updateReapTimer();
        if (reapTimer > 0) return;
        for (PerTypeNodeManager<?, ?, ?, ?> manager : managers.values()) manager.reapLinksAndNets();
    }

    @SuppressWarnings("unchecked")
    private static <R, P, L extends GenNode<N>, N extends NodeNet<R, P, L, N>> PerTypeNodeManager<R, P, L, N> getManagerFor(
            INetworkProvider<N> provider) {
        PerTypeNodeManager<?, ?, ?, ?> manager = managers.get(provider);
        if (manager == null) {
            manager = new PerTypeNodeManager<>(provider);
            managers.put(provider, manager);
        }
        return (PerTypeNodeManager<R, P, L, N>) manager;
    }

    @SuppressWarnings("Java8CollectionRemoveIf")
    private static class PerTypeNodeManager<R, P, L extends GenNode<N>, N extends NodeNet<R, P, L, N>> {

        private final Reference2ObjectOpenHashMap<World, UniNodeWorld<N, L>> worlds = new Reference2ObjectOpenHashMap<>();
        private final Set<N> activeNodeNets = GeneralConfig.enableThreadedNodeSpaceUpdate ? ConcurrentHashMap.newKeySet() : new ReferenceOpenHashSet<>();
        private final INetworkProvider<N> provider;

        PerTypeNodeManager(INetworkProvider<N> provider) {
            this.provider = provider;
        }

        private static boolean checkConnection(GenNode<?> connectsTo, DirPos connectFrom, boolean skipSideCheck) {
            for (DirPos revCon : connectsTo.connections) {// @formatter:off
                if (revCon.getPos().getX() - revCon.getDir().offsetX == connectFrom.getPos().getX() &&
                        revCon.getPos().getY() - revCon.getDir().offsetY == connectFrom.getPos().getY() &&
                        revCon.getPos().getZ() - revCon.getDir().offsetZ == connectFrom.getPos().getZ() &&
                        (revCon.getDir() == connectFrom.getDir().getOpposite() || skipSideCheck)) {
                    return true;
                }// @formatter:on
            }
            return false;
        }

        private UniNodeWorld<N, L> getWorldManager(World world) {
            return worlds.computeIfAbsent(world, _ -> new UniNodeWorld<>());
        }

        L getNode(World world, BlockPos pos) {
            UniNodeWorld<N, L> nodeWorld = worlds.get(world);
            return nodeWorld != null ? nodeWorld.getNode(pos) : null;
        }

        void createNode(World world, L node) {
            getWorldManager(world).pushNode(node);
        }

        void destroyNode(World world, BlockPos pos) {
            UniNodeWorld<N, L> nodeWorld = worlds.get(world);
            if (nodeWorld == null) return;

            L node = nodeWorld.getNode(pos);
            if (node != null) removeNode(nodeWorld, node);
        }

        boolean destroyNode(World world, L node) {
            UniNodeWorld<N, L> nodeWorld = worlds.get(world);
            if (nodeWorld == null) return false;
            if (!nodeWorld.containsNode(node)) return false;
            removeNode(nodeWorld, node);
            return true;
        }

        void addActiveNet(N net) {
            activeNodeNets.add(net);
        }

        void removeActiveNet(Object net) {
            activeNodeNets.remove(net);
        }

        void updateForWorld(World world) {
            UniNodeWorld<N, L> nodeWorld = worlds.get(world);
            if (nodeWorld == null) return;

            for (L node : nodeWorld.getAllNodes()) {
                if (node.expired) continue;
                if (!node.hasValidNet() || node.recentlyChanged) {
                    checkNodeConnection(world, node);
                    node.recentlyChanged = false;
                }
            }
        }

        void resetTrackers() {
            for (N net : activeNodeNets) {
                net.resetTrackers();
            }
        }

        void removeEmptyNets() {
            var it = activeNodeNets.iterator();
            while (it.hasNext()) {
                if (it.next().links.isEmpty()) it.remove();
            }
        }

        void reapLinksAndNets() {
            for (N net : activeNodeNets) {
                var it = net.links.iterator();
                while (it.hasNext()) {
                    if (it.next().expired) it.remove();
                }
            }
            removeEmptyNets();
        }

        void updateNetworks() {
            for (N net : activeNodeNets) {
                if (net.isValid()) net.update();
            }
        }

        void collectNetUpdateTasks(Executor executor, List<CompletableFuture<Void>> tasks) {
            for (N net : activeNodeNets) {
                tasks.add(CompletableFuture.runAsync(() -> {
                    if (net.isValid()) {
                        net.update();
                    }
                }, executor));
            }
        }

        private void checkNodeConnection(World world, L node) {
            for (DirPos con : node.connections) {
                L conNode = getNode(world, con.getPos());
                if (conNode != null) {
                    if (conNode.hasValidNet() && conNode.net == node.net) continue;
                    if (checkConnection(conNode, con, false)) {
                        connectToNode(node, conNode);
                    }
                }
            }
            if (node.net == null || !node.net.isValid()) {
                N newNet = provider.get();
                this.addActiveNet(newNet);
                newNet.joinLink(node);
            }
        }

        private void connectToNode(L origin, L connection) {
            if (origin.hasValidNet() && connection.hasValidNet()) { // both nodes have nets, but the nets are different (previous assumption), join networks
                if (origin.net.links.size() > connection.net.links.size()) {
                    origin.net.joinNetworks(connection.net);
                } else {
                    connection.net.joinNetworks(origin.net);
                }
            } else if (!origin.hasValidNet() && connection.hasValidNet()) { // origin has no net, connection does, have origin join connection's net
                connection.net.joinLink(origin);
            } else if (origin.hasValidNet() && !connection.hasValidNet()) { // ...and vice versa
                origin.net.joinLink(connection);
            }
        }

        private void removeNode(UniNodeWorld<N, L> nodeWorld, L node) {
            N oldNet = node.net;
            nodeWorld.popNode(node);
            if (oldNet != null) oldNet.links.remove(node);

            if (oldNet == null || !oldNet.isValid()) {
                node.setNet(null);
                return;
            }
            node.setNet(null);
            resetNetMembership(oldNet);

            if (oldNet.links.isEmpty()) {
                oldNet.destroy();
                return;
            }

            splitNetIfNecessary(nodeWorld, node, oldNet);
        }

        /**
         * Removing a single node can only split the graph along the removed node's incident edges.
         * Recompute connected components locally instead of invalidating the entire net.
         */
        private void splitNetIfNecessary(UniNodeWorld<N, L> nodeWorld, L removedNode, N oldNet) {
            List<L> seeds = collectAdjacentLinks(nodeWorld, removedNode, oldNet);
            if (seeds.size() <= 1) return;

            List<ReferenceOpenHashSet<L>> components = new ArrayList<>();
            ReferenceOpenHashSet<L> visited = new ReferenceOpenHashSet<>();

            for (L seed : seeds) {
                if (seed.expired || visited.contains(seed)) continue;
                components.add(collectComponent(nodeWorld, seed, oldNet, visited));
            }

            for (L link : oldNet.links) {
                if (link.expired || visited.contains(link)) continue;
                components.add(collectComponent(nodeWorld, link, oldNet, visited));
            }

            if (components.size() <= 1) return;

            int keepIndex = 0;
            int keepSize = components.get(0).size();

            for (int i = 1; i < components.size(); i++) {
                int componentSize = components.get(i).size();
                if (componentSize > keepSize) {
                    keepIndex = i;
                    keepSize = componentSize;
                }
            }

            ReferenceOpenHashSet<L> keep = components.get(keepIndex);

            var oldLinks = oldNet.links.iterator();
            while (oldLinks.hasNext()) {
                L link = oldLinks.next();
                if (!keep.contains(link)) oldLinks.remove();
            }

            for (L link : keep) {
                link.recentlyChanged = false;
            }

            for (int i = 0; i < components.size(); i++) {
                if (i == keepIndex) continue;

                N splitNet = provider.get();
                addActiveNet(splitNet);
                resetNetMembership(splitNet);

                for (L link : components.get(i)) {
                    splitNet.forceJoinLink(link);
                    link.recentlyChanged = false;
                }
            }
        }

        private List<L> collectAdjacentLinks(UniNodeWorld<N, L> nodeWorld, L node, N expectedNet) {
            List<L> seeds = new ArrayList<>();
            ReferenceOpenHashSet<L> seen = new ReferenceOpenHashSet<>();
            if (node.connections == null) return seeds;

            for (DirPos con : node.connections) {
                L conNode = nodeWorld.getNode(con.getPos());
                if (conNode == null || conNode.expired || conNode.net != expectedNet) continue;
                if (!checkConnection(conNode, con, false) || !seen.add(conNode)) continue;
                seeds.add(conNode);
            }

            return seeds;
        }

        private ReferenceOpenHashSet<L> collectComponent(UniNodeWorld<N, L> nodeWorld, L seed, N expectedNet, ReferenceOpenHashSet<L> visited) {
            ReferenceOpenHashSet<L> component = new ReferenceOpenHashSet<>();
            ArrayDeque<L> queue = new ArrayDeque<>();
            queue.add(seed);
            visited.add(seed);

            while (!queue.isEmpty()) {
                L current = queue.removeFirst();
                component.add(current);

                if (current.connections == null) continue;

                for (DirPos con : current.connections) {
                    L conNode = nodeWorld.getNode(con.getPos());
                    if (conNode == null || conNode.expired || conNode.net != expectedNet) continue;
                    if (!checkConnection(conNode, con, false) || !visited.add(conNode)) continue;
                    queue.addLast(conNode);
                }
            }

            return component;
        }

        private void resetNetMembership(N net) {
            net.receiverEntries.clear();
            net.providerEntries.clear();
            net.resetTrackers();
        }
    }

    /**
     * Holds all nodes of a single network type for a specific World.
     */
    private static class UniNodeWorld<N extends NodeNet<?, ?, L, N>, L extends GenNode<N>> {
        private final Object2ObjectLinkedOpenHashMap<BlockPos, L> nodesByPosition = new Object2ObjectLinkedOpenHashMap<>();

        L getNode(BlockPos pos) {
            return nodesByPosition.get(pos);
        }

        /** Adds a node at all its positions to the nodespace */
        void pushNode(L node) {
            for (BlockPos pos : node.positions) {
                nodesByPosition.put(pos, node);
            }
        }

        /** Removes the specified node from all positions from nodespace */
        void popNode(L node) {
            for (BlockPos pos : node.positions) {
                nodesByPosition.remove(pos, node);
            }
            node.expired = true;
        }

        boolean containsNode(GenNode<?> node) {
            if (node == null) return false;
            BlockPos[] positions = node.positions;
            if (positions == null) return false;
            for (BlockPos pos : positions) {
                if (nodesByPosition.get(pos) == node) return true;
            }
            return false;
        }

        /** @return a view of all nodes in this world, do not modify */
        ObjectCollection<L> getAllNodes() {
            return nodesByPosition.values();
        }
    }
}
