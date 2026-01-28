package com.hbm.world.phased;

import com.hbm.util.ObjectPool;
import com.hbm.world.phased.DynamicStructureDispatcher.PendingDynamicStructure;
import com.hbm.world.phased.PhasedEventHandler.AbstractChunkWaitJob;
import com.hbm.world.phased.PhasedStructureGenerator.PhasedChunkTask;
import com.hbm.world.phased.PhasedStructureGenerator.PhasedStructureStart;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.world.WorldServer;

import java.util.ArrayDeque;
import java.util.ArrayList;

final class DimensionState {
    static final ObjectPool<ArrayList<PhasedChunkTask>> TASK_LIST_POOL = new ObjectPool<>(() -> new ArrayList<>(1), ArrayList::clear, 2048);
    static final ObjectPool<LongArrayList> CHUNK_LIST_POOL = new ObjectPool<>(LongArrayList::new, LongArrayList::clear, 512);
    final Long2ObjectOpenHashMap<ArrayList<PhasedChunkTask>> componentsByChunk = new Long2ObjectOpenHashMap<>(4096);
    final Long2ObjectOpenHashMap<ArrayList<PhasedStructureStart>> structureMap = new Long2ObjectOpenHashMap<>(4096);
    final ArrayDeque<ArrayList<PhasedChunkTask>> recycleQueue = new ArrayDeque<>(128);
    final ArrayDeque<PhasedStructureStart> completedStarts = new ArrayDeque<>(256);
    final Long2ObjectOpenHashMap<ReferenceLinkedOpenHashSet<AbstractChunkWaitJob>> waitingJobs = new Long2ObjectOpenHashMap<>(128);
    final Long2ObjectOpenHashMap<ArrayList<PendingDynamicStructure>> jobsByOriginChunk = new Long2ObjectOpenHashMap<>(400);
    final WorldServer world;
    final int dimension;
    final long worldSeed;
    long currentlyProcessingChunk = Long.MIN_VALUE;
    boolean processingTasks;
    int processingDepth;

    DimensionState(int dimension, WorldServer world) {
        this.dimension = dimension;
        this.world = world;
        worldSeed = world.getSeed();
    }

    static void recycleChunkList(LongArrayList list) {
        CHUNK_LIST_POOL.recycle(list);
    }

    static ArrayList<PhasedChunkTask> borrowTaskList() {
        return TASK_LIST_POOL.borrow();
    }

    static void recycleTaskList(ArrayList<PhasedChunkTask> list) {
        for (PhasedChunkTask task : list) {
            PhasedChunkTask.recycle(task);
        }
        TASK_LIST_POOL.recycle(list);
    }

    void drainCompletedStarts() {
        PhasedStructureStart start;
        while ((start = completedStarts.poll()) != null) {
            start.finalizeStart(this);
        }
    }

    void recycleAllStarts() {
        drainCompletedStarts();
        for (var bucket : structureMap.values()) {
            for (var start : bucket) {
                PhasedStructureStart.recycle(start);
            }
            bucket.clear();
        }
    }

    void drainRecycleQueue() {
        ArrayList<PhasedChunkTask> list;
        while ((list = recycleQueue.poll()) != null) {
            recycleTaskList(list);
        }
    }

    void recycleAllComponents() {
        for (var list : componentsByChunk.values()) {
            recycleTaskList(list);
        }
        drainRecycleQueue();
    }
}
