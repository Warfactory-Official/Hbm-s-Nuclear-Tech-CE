package com.hbm.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.hbm.Tags;
import com.hbm.lib.maps.NonBlockingHashMapLong;
import com.hbm.main.MainRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class DelayedTick {

    private static final NonBlockingHashMapLong<MpscCollector<Ticket<?>>> SERVER_TASKS_START = new NonBlockingHashMapLong<>();
    private static final NonBlockingHashMapLong<MpscCollector<Ticket<?>>> SERVER_TASKS_END = new NonBlockingHashMapLong<>();

    private static final NonBlockingHashMapLong<NonBlockingHashMapLong<MpscCollector<Ticket<?>>>> WORLD_TASKS_START = new NonBlockingHashMapLong<>();
    private static final NonBlockingHashMapLong<NonBlockingHashMapLong<MpscCollector<Ticket<?>>>> WORLD_TASKS_END = new NonBlockingHashMapLong<>();

    private DelayedTick() {
    }

    @CanIgnoreReturnValue
    public static Ticket<MinecraftServer> scheduleServerEnd(MinecraftServer server, int delayTicks, Consumer<MinecraftServer> task) {
        return scheduleServer(server, TickEvent.Phase.END, delayTicks, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<MinecraftServer> scheduleServerEnd(World world, int delayTicks, Consumer<MinecraftServer> task) {
        if (world == null || world.isRemote) return null;
        return scheduleServer(world.getMinecraftServer(), TickEvent.Phase.END, delayTicks, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<MinecraftServer> nextServerTickEnd(MinecraftServer server, Consumer<MinecraftServer> task) {
        return scheduleServer(server, TickEvent.Phase.END, 1, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<MinecraftServer> nextServerTickEnd(World world, Consumer<MinecraftServer> task) {
        if (world == null || world.isRemote) return null;
        return scheduleServer(world.getMinecraftServer(), TickEvent.Phase.END, 1, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<MinecraftServer> scheduleServerStart(MinecraftServer server, int delayTicks, Consumer<MinecraftServer> task) {
        return scheduleServer(server, TickEvent.Phase.START, delayTicks, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<MinecraftServer> scheduleServerStart(World world, int delayTicks, Consumer<MinecraftServer> task) {
        if (world == null || world.isRemote) return null;
        return scheduleServer(world.getMinecraftServer(), TickEvent.Phase.START, delayTicks, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<MinecraftServer> nextServerTickStart(MinecraftServer server, Consumer<MinecraftServer> task) {
        return scheduleServer(server, TickEvent.Phase.START, 1, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<MinecraftServer> nextServerTickStart(World world, Consumer<MinecraftServer> task) {
        if (world == null || world.isRemote) return null;
        return scheduleServer(world.getMinecraftServer(), TickEvent.Phase.START, 1, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<MinecraftServer> scheduleServer(MinecraftServer server, TickEvent.Phase phase, int delayTicks,
                                                         Consumer<MinecraftServer> task) {
        if (server == null || task == null) return null;
        final long when = Integer.toUnsignedLong(server.getTickCounter() + Math.max(1, delayTicks));
        final Ticket<MinecraftServer> ticket = new Ticket<>(TaskType.SERVER, phase, task);

        final NonBlockingHashMapLong<MpscCollector<Ticket<?>>> byTime = switch (phase) {
            case START -> SERVER_TASKS_START;
            case END -> SERVER_TASKS_END;
        };

        byTime.computeIfAbsent(when, k -> new MpscCollector<>()).push(ticket);
        return ticket;
    }

    @CanIgnoreReturnValue
    public static Ticket<World> scheduleWorldEnd(World world, int delayTicks, Consumer<World> task) {
        return scheduleWorld(world, TickEvent.Phase.END, delayTicks, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<World> nextWorldTickEnd(World world, Consumer<World> task) {
        return scheduleWorld(world, TickEvent.Phase.END, 1, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<World> scheduleWorldStart(World world, int delayTicks, Consumer<World> task) {
        return scheduleWorld(world, TickEvent.Phase.START, delayTicks, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<World> nextWorldTickStart(World world, Consumer<World> task) {
        return scheduleWorld(world, TickEvent.Phase.START, 1, task);
    }

    @CanIgnoreReturnValue
    public static Ticket<World> scheduleWorld(World world, TickEvent.Phase phase, int delayTicks, Consumer<World> task) {
        if (world == null || world.isRemote || task == null) return null;
        final long runAt = world.getTotalWorldTime() + Math.max(1, delayTicks);
        final long dim = world.provider.getDimension();
        final Ticket<World> ticket = new Ticket<>(TaskType.WORLD, phase, task);

        final NonBlockingHashMapLong<NonBlockingHashMapLong<MpscCollector<Ticket<?>>>> byDim = switch (phase) {
            case START -> WORLD_TASKS_START;
            case END -> WORLD_TASKS_END;
        };

        byDim.computeIfAbsent(dim, k -> new NonBlockingHashMapLong<>()).computeIfAbsent(runAt, _ -> new MpscCollector<>()).push(ticket);

        return ticket;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.side != Side.SERVER) return;

        final MinecraftServer srv = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (srv == null) return;

        final long now = Integer.toUnsignedLong(srv.getTickCounter());
        final MpscCollector<Ticket<?>> col = switch (e.phase) {
            case START -> SERVER_TASKS_START.remove(now);
            case END -> SERVER_TASKS_END.remove(now);
        };
        runAll(col, TaskType.SERVER, srv);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent e) {
        if (e.side != Side.SERVER || e.world.isRemote) return;

        final long dim = e.world.provider.getDimension();
        final long now = e.world.getTotalWorldTime();
        final NonBlockingHashMapLong<NonBlockingHashMapLong<MpscCollector<Ticket<?>>>> current = switch (e.phase) {
            case START -> WORLD_TASKS_START;
            case END -> WORLD_TASKS_END;
        };
        final NonBlockingHashMapLong<MpscCollector<Ticket<?>>> byTime = current.get(dim);
        if (byTime == null) return;

        final MpscCollector<Ticket<?>> col = byTime.remove(now);
        runAll(col, TaskType.WORLD, e.world);

        if (byTime.isEmpty()) {
            current.remove(dim, byTime);
        }
    }

    private static <T> void runAll(MpscCollector<Ticket<?>> collector, TaskType expectedType, T target) {
        if (collector == null) return;
        final List<Ticket<?>> tasks = collector.drain();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            final Ticket<?> raw = tasks.get(i);
            if (raw.taskType != expectedType || raw.cancelled) {
                continue;
            }
            try {
                @SuppressWarnings("unchecked") final Ticket<T> typed = (Ticket<T>) raw;
                typed.runIfNotCancelled(target);
            } catch (ClassCastException ex) {
                MainRegistry.logger.error("Mismatched delayed task target type for {}", expectedType, ex);
            }
        }
    }

    private enum TaskType {
        SERVER, WORLD
    }

    public static final class Ticket<T> {
        private final TaskType taskType;
        private final TickEvent.Phase phase;
        private final Consumer<T> task;
        private volatile boolean cancelled;

        private Ticket(TaskType taskType, TickEvent.Phase phase, Consumer<T> task) {
            this.taskType = taskType;
            this.phase = phase;
            this.task = task;
        }

        public void cancel() {
            cancelled = true;
        }

        public TickEvent.Phase getPhase() {
            return phase;
        }

        void runIfNotCancelled(T target) {
            if (cancelled) return;
            try {
                task.accept(target);
            } catch (Throwable t) {
                MainRegistry.logger.error("Exception in delayed {} task", taskType.name().toLowerCase(), t);
            }
        }
    }
}
