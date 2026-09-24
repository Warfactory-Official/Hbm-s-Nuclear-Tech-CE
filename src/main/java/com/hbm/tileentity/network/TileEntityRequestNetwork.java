package com.hbm.tileentity.network;

import com.hbm.util.Vec3NT;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.SwappedHashSet;
import com.hbm.tileentity.network.RequestNetwork.PathNode;
import com.hbm.util.ParticleUtil;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;

/**
 * i can see it clearly
 * this simple idea, this concept of 4 individually acting objects performing basic tasks
 * it is all spiraling out of control
 * in a giant mess of nested generics, magic numbers and static global variables
 * may god have mercy on my soul
 *
 * @author hbm
 *
 */
public abstract class TileEntityRequestNetwork extends TileEntityLoadedBase implements ITickable {
    public SwappedHashSet<PathNode> reachableNodes = new SwappedHashSet<>();
    public SwappedHashSet<PathNode> knownNodes = new SwappedHashSet<>();
    public static final int maxRange = 24;

    @Override
    public void update() {
        if (!world.isRemote) {

            if (world.getTotalWorldTime() % 20 == 0) {
                BlockPos coord = getCoord();

                PathNode newNode = createNode(coord);
                if (this.world.isBlockPowered(pos)) newNode.active = false;
                else newNode.active = true;
                // push new node
                push(world, newNode);

                // remove known nodes that no longer exist
                // since we can assume a sane number of nodes to exist at any given time, we can run this check in full every second
                Iterator<PathNode> it = knownNodes.iterator();
                SwappedHashSet<PathNode> localNodes = getAllLocalNodes(world, pos.getX(), pos.getZ(), 2); // this bit may spiral into multiple nested hashtable lookups but it's limited to only a few chunks so it shouldn't be an issue
                localNodes.remove(coord);

                while (it.hasNext()) {
                    PathNode node = it.next();
                    if (!localNodes.contains(node)) {
                        reachableNodes.remove(node);
                        it.remove();
                    }
                }

                // draw debug crap
                for (PathNode known : knownNodes) {
                    if (reachableNodes.contains(known)) ParticleUtil.spawnDroneLine(world,
                            coord.getX() + 0.5, coord.getY() + 0.5, coord.getZ() + 0.5,
                            (known.pos.getX()  - coord.getX()) / 2D, (known.pos.getY() - coord.getY()) / 2D, (known.pos.getZ() - coord.getZ()) / 2D,
                            reachableNodes.contains(known) ? 0x00ff00 : 0xff0000);
                }

                //both following checks run the `hasPath` function which is costly, so it only runs one op at a time

                //rescan known nodes
                for (PathNode known : knownNodes) {

                    if (!hasPath(world, coord, known.pos)) {
                        reachableNodes.remove(known);
                    } else {
                        reachableNodes.add(known);
                    }
                }

                //discover new nodes
                int newNodeLimit = 5;
                for (PathNode node : localNodes) {

                    if (!knownNodes.contains(node) && !node.pos.equals(coord)) {
                        newNodeLimit--;
                        knownNodes.add(node);
                        if (hasPath(world, coord, node.pos)) reachableNodes.add(node);
                    }

                    if (newNodeLimit <= 0) break;
                }
            }
        }
    }

    public abstract PathNode createNode(BlockPos pos);

    public BlockPos getCoord() {
        return new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
    }

    /**
     * Performs a bidirectional scan to see if the nodes have line of sight
     */
    public static boolean hasPath(World world, BlockPos pos1, BlockPos pos2) {
        Vec3NT vec1 = Vec3NT.createVectorHelper(pos1.getX() + 0.5, pos1.getY() + 0.5, pos1.getZ() + 0.5);
        Vec3NT vec2 = Vec3NT.createVectorHelper(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);
        Vec3NT vec3 = vec1.subtract(vec2);
        if (vec3.length() > maxRange) return false;
        //for some fucking reason beyond any human comprehension, this function will randomly yield incorrect results but only from one side
        //therefore we just run the stupid fucking thing twice and then compare the results
        //thanks for this marvelous piece of programming, mojang
        RayTraceResult result0 = world.rayTraceBlocks(vec1.toVec3d(), vec2.toVec3d(), false, true, false);
        RayTraceResult result1 = world.rayTraceBlocks(vec1.toVec3d(), vec2.toVec3d(), false, true, false);
        return (result0 == null || result0.typeOfHit == RayTraceResult.Type.MISS) && (result1 == null || result1.typeOfHit == RayTraceResult.Type.MISS);
    }

    /**
     * Adds the position to that chunk's node list.
     */
    public static void push(World world, PathNode node) {

        HashMap<ChunkPos, SwappedHashSet<PathNode>> coordMap = RequestNetwork.activeWaypoints.computeIfAbsent(world, k -> new HashMap<>());

        ChunkPos chunkPos = new ChunkPos(node.pos.getX() >> 4, node.pos.getZ() >> 4);
        SwappedHashSet<PathNode> posList = coordMap.get(chunkPos);

        if (posList == null) {
            posList = new SwappedHashSet<>();
            coordMap.put(chunkPos, posList);
        }

        posList.add(node);
    }

    /**
     * Gets all active nodes in a 5x5 chunk area, centered around the given position.
     * Used for finding neighbors to check connections to.
     */
    public static SwappedHashSet<PathNode> getAllLocalNodes(World world, int x, int z, int range) {

        SwappedHashSet<PathNode> nodes = new SwappedHashSet<>();

        x >>= 4;
        z >>= 4;

        HashMap<ChunkPos, SwappedHashSet<PathNode>> coordMap = RequestNetwork.activeWaypoints.get(world);

        if (coordMap == null) return nodes;

        for (int i = -range; i <= range; i++) {
            for (int j = -range; j <= range; j++) {

                SwappedHashSet<PathNode> nodeList = coordMap.get(new ChunkPos(x + i, z + j));

                if (nodeList != null) nodes.addAll(nodeList);
            }
        }

        return nodes;
    }
}
