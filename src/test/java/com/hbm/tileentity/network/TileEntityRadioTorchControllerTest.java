package com.hbm.tileentity.network;

import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.tileentity.network.RTTYSystem.RTTYChannel;
import com.hbm.util.Tuple.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TileEntityRadioTorchControllerTest {

    private static final String CHAN = "TEST";
    private static final String CHAN2 = "TEST2";
    private static final BlockPos CONTROLLER_POS = new BlockPos(0, 10, 0);
    private static final BlockPos TARGET_POS = new BlockPos(0, 9, 0);

    private TestWorld world;
    private TestController controller;
    private Target target;

    //Compat asks Loader.isModLoaded in its static initializer, which needs a mod list
    @BeforeAll
    static void emptyModList() throws Exception {
        Loader loader = Loader.instance();
        Field namedMods = Loader.class.getDeclaredField("namedMods");
        namedMods.setAccessible(true);
        if (namedMods.get(loader) == null) namedMods.set(loader, new HashMap<String, ModContainer>());
    }

    @BeforeEach
    void setUp() {
        world = new TestWorld();
        target = new Target();
        world.target = target;
        controller = new TestController();
        controller.setWorld(world);
        controller.setPos(CONTROLLER_POS);
        controller.channel = CHAN;
        controller.polling = false;
    }

    @AfterEach
    void tearDown() {
        RTTYSystem.broadcast.remove(new Pair<>(world, CHAN));
        RTTYSystem.broadcast.remove(new Pair<>(world, CHAN2));
    }

    private void publish(String channel, String signal, long time) {
        RTTYChannel chan = new RTTYChannel();
        chan.timeStamp = time;
        chan.signal = signal;
        RTTYSystem.broadcast.put(new Pair<>(world, channel), chan);
    }

    private void tick(long time) {
        world.getWorldInfo().setWorldTotalTime(time);
        controller.update();
    }

    @Test
    void oneShotRunsOncePerBurstWithPollingOff() {
        publish(CHAN, "setmode!2", 100);
        for (long t = 100; t <= 103; t++) tick(t); //a signal is fresh for two ticks
        assertEquals(List.of("fun:setmode|2"), target.calls);
    }

    @Test
    void identicalStringAfterAGapRunsAgain() {
        publish(CHAN, "enqueue!1:2:3", 100);
        for (long t = 100; t <= 105; t++) tick(t);
        assertEquals(1, target.calls.size());

        publish(CHAN, "enqueue!1:2:3", 110); //same string again after a gap
        for (long t = 106; t <= 115; t++) tick(t);
        assertEquals(2, target.calls.size());
    }

    @Test
    void changedStringInsideABurstRunsImmediately() {
        publish(CHAN, "setmode!1", 200);
        tick(200);
        publish(CHAN, "setmode!2", 201);
        tick(201);
        assertEquals(List.of("fun:setmode|1", "fun:setmode|2"), target.calls);
    }

    @Test
    void pollingOnStillRunsOnEveryFreshTick() {
        controller.polling = true;
        publish(CHAN, "setmode!2", 100);
        for (long t = 100; t <= 103; t++) tick(t);
        assertEquals(2, target.calls.size());
    }

    @Test
    void staleSignalIsNotExecutedWhenTheControllerStarts() {
        publish(CHAN, "setmode!2", 100);
        tick(500);
        tick(501);
        assertEquals(0, target.calls.size());
    }

    @Test
    void changingTheChannelStartsANewBurst() {
        publish(CHAN, "setmode!2", 100);
        publish(CHAN2, "setmode!2", 101);
        tick(100);
        tick(101); //same burst, nothing new
        assertEquals(1, target.calls.size());

        NBTTagCompound data = new NBTTagCompound();
        data.setString("channel", CHAN2);
        controller.receiveControl(null, data);
        tick(101);
        assertEquals(2, target.calls.size());
    }

    @Test
    void emptyAndInvalidSignalsDoNotReachTheMachine() {
        publish(CHAN, "", 100);
        tick(100);
        publish(CHAN, "a!b!c", 102); //too many name separators
        tick(102);
        assertEquals(0, target.calls.size());
    }

    private static final class TestController extends TileEntityRadioTorchController {
        @Override
        public EnumFacing getTorchFacing() {
            return EnumFacing.UP; //the machine is below the torch
        }

        @Override
        public void networkPackNT(int range) {
        }

        @Override
        public void markDirty() {
        }
    }

    private static final class Target extends TileEntity implements IRORInteractive {
        final List<String> calls = new ArrayList<>();

        @Override
        public String[] getFunctionInfo() {
            return new String[0];
        }

        @Override
        public String runRORFunction(String name, String[] params) {
            calls.add(name.toLowerCase() + "|" + String.join(",", params));
            return null;
        }
    }

    private static final class TestWorld extends World {
        Target target;

        private TestWorld() {
            super(new SaveHandlerMP(), new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), false);
        }

        @Override
        protected IChunkProvider createChunkProvider() {
            return null;
        }

        @Override
        public IChunkProvider getChunkProvider() {
            return new IChunkProvider() {
                @Override
                public Chunk getLoadedChunk(int x, int z) {
                    return null;
                }

                @Override
                public Chunk provideChunk(int x, int z) {
                    return null;
                }

                @Override
                public boolean tick() {
                    return false;
                }

                @Override
                public String makeString() {
                    return "test";
                }

                @Override
                public boolean isChunkGeneratedAt(int x, int z) {
                    return true;
                }
            };
        }

        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return TARGET_POS.equals(pos) ? target : null;
        }

        @Override
        protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
            return true;
        }

        @Override
        public Entity getEntityByID(int id) {
            return null;
        }
    }
}
