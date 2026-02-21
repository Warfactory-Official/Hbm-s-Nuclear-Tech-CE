package com.hbm.handler.threading;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.main.NetworkHandler;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jctools.queues.MpscBlockingConsumerArrayQueue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import static com.hbm.lib.internal.UnsafeHolder.*;

/**
 * Methods here are safe to call off-thread
 */
public class PacketThreading {
    /**
     * Global lock guarding the FML channel state for outbound packets.
     */
    public static final ReentrantLock LOCK = new ReentrantLock();
    public static final String THREAD_PREFIX = "NTM-Packet-Thread-";
    private static final Object IN_FLIGHT_BASE = staticFieldBase(PacketThreading.class, "inFlightDispatch");
    private static final long IN_FILGHT_OFF = staticfieldOffset(PacketThreading.class, "inFlightDispatch");
    private static final Object LAST_S_FLUSH_BASE = staticFieldBase(PacketThreading.class, "lastServerFlushNs");
    private static final long LAST_S_FLUSH_OFF = staticfieldOffset(PacketThreading.class, "lastServerFlushNs");
    private static final Object LAST_C_FLUSH_BASE = staticFieldBase(PacketThreading.class, "lastClientFlushNs");
    private static final long LAST_C_FLUSH_OFF = staticfieldOffset(PacketThreading.class, "lastClientFlushNs");
    private static final int QUEUE_CAPACITY = 4096;
    private static final int BATCH_SIZE = 128;
    // Coalesce flushes in multi-threaded mode to avoid flush storms.
    private static final long MIN_FLUSH_NS = 1_000_000L; // 1ms

    private static final ThreadFactory packetThreadFactory = new ThreadFactoryBuilder().setNameFormat(THREAD_PREFIX + "%d").setDaemon(true).build();

    private static final LongAdder totalCnt = new LongAdder();
    private static final LongAdder nanosWaited = new LongAdder();
    @SuppressWarnings("FieldMayBeFinal")
    private static volatile long lastServerFlushNs = 0L, lastClientFlushNs = 0L;
    private static volatile boolean multiThreaded = false;
    private static volatile boolean running = true;
    private static volatile boolean enabled = false;
    @SuppressWarnings("unused")
    private static volatile int inFlightDispatch;
    private static volatile MpscBlockingConsumerArrayQueue<PacketTask> singleThreadQueue;
    private static volatile Thread singleWorkerThread;
    private static volatile ThreadPoolExecutor threadPool;

    /**
     * Sets up thread pool settings during mod initialization.
     */
    public static synchronized void init() {
        shutdown();

        if (!GeneralConfig.enablePacketThreading) {
            enabled = false;
            return;
        }

        int coreCount = GeneralConfig.packetThreadingCoreCount;
        int maxCount = GeneralConfig.packetThreadingMaxCount;

        if (coreCount <= 0 || maxCount <= 0) {
            MainRegistry.logger.error("packetThreadingCoreCount ({}) or packetThreadingMaxCount ({}) is <= 0. Defaulting to single-threaded mode.",
                    coreCount, maxCount);
            coreCount = 1;
        } else if (maxCount < coreCount) {
            MainRegistry.logger.warn(
                    "packetThreadingMaxCount ({}) cannot be less than packetThreadingCoreCount ({}). Setting max count to core count.", maxCount,
                    coreCount);
            maxCount = coreCount;
        }

        enabled = true;

        if (coreCount > 1) {
            multiThreaded = true;
            singleThreadQueue = null;
            MainRegistry.logger.info("Initializing PacketThreading in Multi-Threaded mode (Core: {}, Max: {}).", coreCount, maxCount);
            ThreadPoolExecutor tp = new ThreadPoolExecutor(coreCount, maxCount, 50L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                    packetThreadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
            tp.allowCoreThreadTimeOut(false);
            threadPool = tp;
        } else {
            multiThreaded = false;
            threadPool = null;
            MainRegistry.logger.info("Initializing PacketThreading in Optimized Single-Threaded mode.");
            running = true;
            MpscBlockingConsumerArrayQueue<PacketTask> q = new MpscBlockingConsumerArrayQueue<>(QUEUE_CAPACITY);
            singleThreadQueue = q;
            Thread t = new Thread(() -> processBatch(q), "NTM-Packet-Thread-0");
            t.setDaemon(true);
            singleWorkerThread = t;
            t.start();
        }
    }

    private static void shutdown() {
        enabled = false;
        running = false;
        int spin = 0;
        while (inFlightDispatch != 0) {
            spin++;
            if (spin < 1000) {
                Thread.yield();
            } else {
                LockSupport.parkNanos(500L);
            }
        }
        ThreadPoolExecutor tp = threadPool;
        threadPool = null;
        if (tp != null) {
            List<Runnable> pendingTasks = tp.shutdownNow();
            for (Runnable r : pendingTasks) {
                if (r instanceof PacketRunner runner) {
                    runner.free();
                }
            }
        }
        Thread t = singleWorkerThread;
        singleWorkerThread = null;
        if (t != null && t.isAlive()) t.interrupt();
        MpscBlockingConsumerArrayQueue<PacketTask> q = singleThreadQueue;
        singleThreadQueue = null;
        if (q != null) {
            PacketTask task;
            while ((task = q.relaxedPoll()) != null) {
                task.packet.releaseBuffer();
            }
        }
    }

    private static void maybeFlushServer() {
        long now = System.nanoTime();
        long prev = lastServerFlushNs;
        if (now - prev >= MIN_FLUSH_NS && U.compareAndSetLong(LAST_S_FLUSH_BASE, LAST_S_FLUSH_OFF, prev, now)) {
            NetworkHandler.flushServer();
        }
    }

    private static void maybeFlushClient() {
        long now = System.nanoTime();
        long prev = lastClientFlushNs;
        if (now - prev >= MIN_FLUSH_NS && U.compareAndSetLong(LAST_C_FLUSH_BASE, LAST_C_FLUSH_OFF, prev, now)) {
            NetworkHandler.flushClient();
        }
    }

    private static void processBatch(MpscBlockingConsumerArrayQueue<PacketTask> q) {
        List<PacketTask> batchBuffer = new ArrayList<>(BATCH_SIZE);

        while (running) {
            try {
                PacketTask first = q.take();
                batchBuffer.add(first);
                for (int i = 0; i < BATCH_SIZE - 1; i++) {
                    PacketTask next = q.relaxedPoll();
                    if (next == null) break;
                    batchBuffer.add(next);
                }

                for (int i = 0; i < batchBuffer.size(); i++) {
                    PacketTask task = batchBuffer.get(i);
                    try {
                        task.packet.getCompiledBuffer();
                    } catch (Throwable t) {
                        MainRegistry.logger.error("Failed to compile threaded packet", t);
                        task.packet.releaseBuffer();
                        batchBuffer.set(i, null);
                    }
                }

                LOCK.lock();
                try {
                    boolean doFlushServer = false;
                    boolean doFlushClient = false;

                    for (PacketTask task : batchBuffer) {
                        if (task == null) continue;
                        try {
                            send(task);
                            if (task.op == PacketOp.SERVER) doFlushClient = true;
                            else doFlushServer = true;
                        } catch (Throwable t) {
                            MainRegistry.logger.error("Failed to write packet to channel", t);
                        }
                    }

                    // Early flush to reduce latency (tick-end flush stays as a backstop).
                    if (doFlushServer) NetworkHandler.flushServerDirect();
                    if (doFlushClient) NetworkHandler.flushClientDirect();

                } finally {
                    LOCK.unlock();
                }

                for (PacketTask task : batchBuffer) {
                    if (task != null) task.packet.releaseBuffer();
                }
            } catch (InterruptedException e) {
                MainRegistry.logger.warn("Packet worker interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable t) {
                MainRegistry.logger.error("Crash in packet worker loop", t);
                for (PacketTask task : batchBuffer) {
                    if (task != null) task.packet.releaseBuffer();
                }
            } finally {
                batchBuffer.clear();
            }
        }

        PacketTask task;
        while ((task = q.relaxedPoll()) != null) {
            task.packet.releaseBuffer();
        }
    }

    // caller shall release() the buffer sent once after send()
    // FML fan-out retains pkt.payload() for each dispatcher, so the underlying memory
    // stays alive until all downstream retains are released.
    private static void send(PacketTask task) {
        switch (task.op) {
            case SERVER -> PacketDispatcher.wrapper.sendToServerDirect(task.packet);
            case PLAYER -> PacketDispatcher.wrapper.sendToDirect(task.packet, (EntityPlayerMP) task.target);
            case ALL -> PacketDispatcher.wrapper.sendToAllDirect(task.packet);
            case DIMENSION -> PacketDispatcher.wrapper.sendToDimensionDirect(task.packet, task.dimension);
            case ALL_AROUND -> PacketDispatcher.wrapper.sendToAllAroundDirect(task.packet, (TargetPoint) task.target);
            case TRACKING_POINT -> PacketDispatcher.wrapper.sendToAllTrackingDirect(task.packet, (TargetPoint) task.target);
            case TRACKING_ENTITY -> PacketDispatcher.wrapper.sendToAllTrackingDirect(task.packet, (Entity) task.target);
        }
    }

    private static void dispatch(ThreadedPacket packet, PacketOp op, Object target, int dimension) {
        totalCnt.increment();
        U.getAndAddInt(IN_FLIGHT_BASE, IN_FILGHT_OFF, 1);
        try {
            if (!enabled || !GeneralConfig.enablePacketThreading) {
                runSynchronously(packet, op, target, dimension);
                return;
            }

            PacketTask task = new PacketTask(packet, op, target, dimension);

            if (multiThreaded) {
                ThreadPoolExecutor tp = threadPool;
                if (tp == null || tp.isShutdown()) {
                    runSynchronously(packet, op, target, dimension);
                    return;
                }
                tp.execute(new PacketRunner(task));
            } else {
                MpscBlockingConsumerArrayQueue<PacketTask> q = singleThreadQueue;
                if (q == null || !q.offer(task)) {
                    MainRegistry.logger.warn("Packet Queue full (size > {}). Running synchronously.", QUEUE_CAPACITY);
                    runSynchronously(packet, op, target, dimension);
                }
            }
        } finally {
            U.getAndAddInt(IN_FLIGHT_BASE, IN_FILGHT_OFF, -1);
        }
    }

    private static void runSynchronously(ThreadedPacket packet, PacketOp op, Object target, int dimension) {
        long start = System.nanoTime();
        try {
            packet.getCompiledBuffer();
            LOCK.lock();
            try {
                send(new PacketTask(packet, op, target, dimension));
            } finally {
                LOCK.unlock();
            }
        } catch (Throwable t) {
            MainRegistry.logger.error("Error sending packet synchronously", t);
            throw t;
        } finally {
            packet.releaseBuffer();
            nanosWaited.add(System.nanoTime() - start);
        }
    }

    /**
     * Mirrors {@link com.hbm.main.NetworkHandler#sendToDimension(IMessage, int)}.
     */
    public static void createSendToDimensionThreadedPacket(@NotNull ThreadedPacket message, int dimensionId) {
        dispatch(message, PacketOp.DIMENSION, null, dimensionId);
    }

    /**
     * Mirrors {@link com.hbm.main.NetworkHandler#sendToAllAround(IMessage, TargetPoint)}.
     */
    public static void createAllAroundThreadedPacket(@NotNull ThreadedPacket message, @NotNull TargetPoint target) {
        dispatch(message, PacketOp.ALL_AROUND, target, Integer.MIN_VALUE);
    }

    /**
     * Mirrors {@link com.hbm.main.NetworkHandler#sendToAllTracking(IMessage, TargetPoint)}.
     */
    public static void createSendToAllTrackingThreadedPacket(@NotNull ThreadedPacket message, @NotNull TargetPoint point) {
        dispatch(message, PacketOp.TRACKING_POINT, point, Integer.MIN_VALUE);
    }

    /**
     * Mirrors {@link com.hbm.main.NetworkHandler#sendToAllTracking(IMessage, Entity)}.
     */
    public static void createSendToAllTrackingThreadedPacket(@NotNull ThreadedPacket message, @NotNull Entity entity) {
        dispatch(message, PacketOp.TRACKING_ENTITY, entity, Integer.MIN_VALUE);
    }

    /**
     * Mirrors {@link com.hbm.main.NetworkHandler#sendTo(IMessage, EntityPlayerMP)}.
     */
    public static void createSendToThreadedPacket(@NotNull ThreadedPacket message, @NotNull EntityPlayerMP player) {
        dispatch(message, PacketOp.PLAYER, player, Integer.MIN_VALUE);
    }

    /**
     * Mirrors {@link com.hbm.main.NetworkHandler#sendToAll(IMessage)}.
     */
    public static void createSendToAllThreadedPacket(@NotNull ThreadedPacket message) {
        dispatch(message, PacketOp.ALL, null, Integer.MIN_VALUE);
    }

    /**
     * Mirrors {@link com.hbm.main.NetworkHandler#sendToServer(IMessage)}.
     */
    public static void createSendToServerThreadedPacket(@NotNull ThreadedPacket message) {
        dispatch(message, PacketOp.SERVER, null, Integer.MIN_VALUE);
    }

    // debugging
    public static int getPoolSize() {
        ThreadPoolExecutor tp = threadPool;
        if (tp == null) return -1;
        return tp.getPoolSize();
    }

    public static int getCorePoolSize() {
        ThreadPoolExecutor tp = threadPool;
        if (tp == null) return -1;
        return tp.getCorePoolSize();
    }

    public static int getActiveCount() {
        ThreadPoolExecutor tp = threadPool;
        if (tp == null) return -1;
        return tp.getActiveCount();
    }

    public static int getMaximumPoolSize() {
        ThreadPoolExecutor tp = threadPool;
        if (tp == null) return -1;
        return tp.getMaximumPoolSize();
    }

    public static int getThreadPoolQueueSize() {
        ThreadPoolExecutor tp = threadPool;
        if (tp == null) return -1;
        return tp.getQueue().size();
    }

    public static int getQueueSize() {
        MpscBlockingConsumerArrayQueue<PacketTask> q = singleThreadQueue;
        if (q == null) return -1;
        return q.size();
    }

    public static boolean isMultiThreaded() {
        return multiThreaded;
    }

    public static long getTotalCount() {
        return totalCnt.sum();
    }

    public static long getNanosWaited() {
        return nanosWaited.sum();
    }

    private enum PacketOp {
        PLAYER, ALL, DIMENSION, ALL_AROUND, TRACKING_POINT, TRACKING_ENTITY, SERVER
    }

    @SuppressWarnings("ClassCanBeRecord") // dude
    static class PacketRunner implements Runnable {
        final PacketTask task;

        PacketRunner(PacketTask task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                task.packet.getCompiledBuffer();
                LOCK.lock();
                try {
                    send(task);
                } finally {
                    LOCK.unlock();
                }
                if (task.op == PacketOp.SERVER) {
                    maybeFlushClient();
                } else {
                    maybeFlushServer();
                }
            } catch (Throwable t) {
                MainRegistry.logger.error("Error processing packet in thread pool", t);
                throw t;
            } finally {
                task.packet.releaseBuffer();
            }
        }

        void free() {
            if (task != null && task.packet != null) {
                task.packet.releaseBuffer();
            }
        }
    }

    record PacketTask(ThreadedPacket packet, PacketOp op, Object target, int dimension) {
    }
}
