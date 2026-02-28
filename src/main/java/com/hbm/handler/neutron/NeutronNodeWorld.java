package com.hbm.handler.neutron;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NeutronNodeWorld {
    public static HashMap<World, StreamWorld> streamWorlds = new HashMap<>();

    public static NeutronNode getNode(World world, BlockPos pos) {
        StreamWorld streamWorld = streamWorlds.get(world);
        return streamWorld != null ? streamWorld.nodeCache.get(pos.toLong()) : null;
    }

    public static void removeNode(World world, BlockPos pos) {
        StreamWorld streamWorld = streamWorlds.get(world);
        if(streamWorld == null) return;
        streamWorld.removeNode(pos);
    }

    public static StreamWorld getOrAddWorld(World world) {
        StreamWorld streamWorld = streamWorlds.get(world);
        if(streamWorld == null) {
            streamWorld = new StreamWorld();
            streamWorlds.put(world, streamWorld);
        }
        return streamWorld;
    }

    public static void removeEmptyWorlds() {
        streamWorlds.values().removeIf((streamWorld) -> streamWorld.streams.isEmpty());
    }

    public static class StreamWorld {

        public List<NeutronStream> streams = new ArrayList<>();
        public Long2ObjectOpenHashMap<NeutronNode> nodeCache = new Long2ObjectOpenHashMap<>();

        public StreamWorld() { }

        public void runStreamInteractions(World world) {
            for(NeutronStream stream : streams) {
                stream.runStreamInteraction(world, this);
            }
        }

        public void addStream(NeutronStream stream) {
            streams.add(stream);
        }

        public void removeAllStreams() {
            streams.clear();
        }

        public void cleanNodes() {
            List<Long> toRemove = new ArrayList<>();
            for(Long2ObjectMap.Entry<NeutronNode> entry : nodeCache.long2ObjectEntrySet()) {
                NeutronNode cachedNode = entry.getValue();
                if(cachedNode.type == NeutronStream.NeutronType.RBMK) {
                    RBMKNeutronHandler.RBMKNeutronNode node = (RBMKNeutronHandler.RBMKNeutronNode) cachedNode;
                    node.checkNode(this, toRemove);
                }
            }

            for(long pos : toRemove) {
                nodeCache.remove(pos);
            }
        }

        public NeutronNode getNode(BlockPos pos) {
            return nodeCache.get(pos.toLong());
        }

        public NeutronNode getNode(long posLong) {
            return nodeCache.get(posLong);
        }

        public void addNode(NeutronNode node) {
            nodeCache.put(node.pos.toLong(), node);
        }

        public void removeNode(BlockPos pos) {
            nodeCache.remove(pos.toLong());
        }

        public void removeAllStreamsOfType(NeutronStream.NeutronType type) {
            streams.removeIf(stream -> stream.type == type);
        }
    }
}

