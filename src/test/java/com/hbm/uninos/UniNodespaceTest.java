package com.hbm.uninos;

import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UniNodespaceTest {

    @Test
    void destroyBridgeNodeSplitsOnlyTheAffectedNet() {
        TestWorld world = new TestWorld();
        INetworkProvider<TestNet> provider = TestNet::new;

        TestNode left = lineNode(provider, 0, false, true);
        TestNode middle = lineNode(provider, 1, true, true);
        TestNode right = lineNode(provider, 2, true, false);

        UniNodespace.createNode(world, left);
        UniNodespace.createNode(world, middle);
        UniNodespace.createNode(world, right);

        TestNet originalNet = new TestNet();
        originalNet.joinLink(left);
        originalNet.joinLink(middle);
        originalNet.joinLink(right);

        UniNodespace.destroyNode(world, middle);

        assertTrue(middle.expired);
        assertNull(middle.net);
        assertNull(UniNodespace.getNode(world, new BlockPos(1, 0, 0), provider));

        assertTrue(left.hasValidNet());
        assertTrue(right.hasValidNet());
        assertNotSame(left.net, right.net);
        assertEquals(1, left.net.links.size());
        assertEquals(1, right.net.links.size());
    }

    @Test
    void destroyLeafNodeKeepsTheRemainingNetIntact() {
        TestWorld world = new TestWorld();
        INetworkProvider<TestNet> provider = TestNet::new;

        TestNode left = lineNode(provider, 0, false, true);
        TestNode middle = lineNode(provider, 1, true, true);
        TestNode right = lineNode(provider, 2, true, false);

        UniNodespace.createNode(world, left);
        UniNodespace.createNode(world, middle);
        UniNodespace.createNode(world, right);

        TestNet originalNet = new TestNet();
        originalNet.joinLink(left);
        originalNet.joinLink(middle);
        originalNet.joinLink(right);

        UniNodespace.destroyNode(world, left);

        assertTrue(left.expired);
        assertNull(left.net);
        assertNull(UniNodespace.getNode(world, new BlockPos(0, 0, 0), provider));

        assertSame(originalNet, middle.net);
        assertSame(originalNet, right.net);
        assertEquals(2, originalNet.links.size());
    }

    @Test
    void destroyLeafNodeClearsTransientMembershipImmediately() {
        TestWorld world = new TestWorld();
        INetworkProvider<TestNet> provider = TestNet::new;

        TestNode left = lineNode(provider, 0, false, true);
        TestNode middle = lineNode(provider, 1, true, true);
        TestNode right = lineNode(provider, 2, true, false);

        UniNodespace.createNode(world, left);
        UniNodespace.createNode(world, middle);
        UniNodespace.createNode(world, right);

        TestNet originalNet = new TestNet();
        originalNet.joinLink(left);
        originalNet.joinLink(middle);
        originalNet.joinLink(right);
        originalNet.addReceiver("receiver");
        originalNet.addProvider("provider");

        UniNodespace.destroyNode(world, left);

        assertTrue(originalNet.receiverEntries.isEmpty());
        assertTrue(originalNet.providerEntries.isEmpty());
        assertSame(originalNet, middle.net);
        assertSame(originalNet, right.net);
    }

    private static TestNode lineNode(INetworkProvider<TestNet> provider, int x, boolean connectWest, boolean connectEast) {
        TestNode node = new TestNode(provider, new BlockPos(x, 0, 0));
        if (connectWest && connectEast) {
            return node.setConnections(
                    new DirPos(x - 1, 0, 0, ForgeDirection.WEST),
                    new DirPos(x + 1, 0, 0, ForgeDirection.EAST));
        }
        if (connectWest) {
            return node.setConnections(new DirPos(x - 1, 0, 0, ForgeDirection.WEST));
        }
        if (connectEast) {
            return node.setConnections(new DirPos(x + 1, 0, 0, ForgeDirection.EAST));
        }
        return node.setConnections();
    }

    private static final class TestNode extends GenNode<TestNet> {
        private TestNode(INetworkProvider<TestNet> provider, BlockPos pos) {
            super(provider, pos);
        }

        @Override
        public TestNode setConnections(DirPos... connections) {
            super.setConnections(connections);
            return this;
        }
    }

    private static final class TestNet extends NodeNet<String, String, TestNode, TestNet> {
        @Override
        public void update() {
        }
    }

    private static final class TestWorld extends World {
        private TestWorld() {
            super(new SaveHandlerMP(), new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), false);
        }

        @Override
        protected IChunkProvider createChunkProvider() {
            return null;
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
