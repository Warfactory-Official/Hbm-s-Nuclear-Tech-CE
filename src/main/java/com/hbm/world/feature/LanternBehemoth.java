package com.hbm.world.feature;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.deco.TileEntityLanternBehemoth;
import com.hbm.util.LootGenerator;
import com.hbm.world.phased.AbstractPhasedStructure;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class LanternBehemoth extends AbstractPhasedStructure {

    public static final LanternBehemoth INSTANCE = new LanternBehemoth();

    private static final int COVERAGE_RADIUS = 2;

    private final LongArrayList chunkOffsets = collectChunkOffsetsByRadius(COVERAGE_RADIUS);

    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    protected boolean useDynamicScheduler() {
        return true;
    }

    @Override
    public LongArrayList getWatchedChunkOffsets(long origin) {
        return chunkOffsets;
    }

    @Override
    public void postGenerate(@NotNull World world, @NotNull Random rand, long finalOrigin) {
        int x = Library.getBlockPosX(finalOrigin);
        int z = Library.getBlockPosZ(finalOrigin);
        int y = world.getHeight(x, z);

        BlockPos below = new BlockPos(x, y - 1, z);
        IBlockState belowState = world.getBlockState(below);
        if(!belowState.getBlock().canPlaceTorchOnTop(belowState, world, below)) return;

        BlockPos basePos = new BlockPos(x, y, z);
        IBlockState baseState = world.getBlockState(basePos);
        if(!baseState.getBlock().isReplaceable(world, basePos)) return;

        world.setBlockState(basePos, ModBlocks.lantern_behemoth.getDefaultState().withProperty(BlockDummyable.META, 12), 2 | 16);
        MultiblockHandlerXR.fillSpace(world, x, y, z, new int[]{4, 0, 0, 0, 0, 0}, ModBlocks.lantern_behemoth, ForgeDirection.NORTH);

        TileEntityLanternBehemoth lantern = (TileEntityLanternBehemoth) world.getTileEntity(basePos);
        if(lantern != null) lantern.isBroken = true;

        if(rand.nextInt(2) == 0) {
            LootGenerator.setBlock(world, x, y, z - 2);
            LootGenerator.lootBooklet(world, x, y, z - 2);
        }

        if(GeneralConfig.enableDebugMode) {
            MainRegistry.logger.info("[Debug] Successfully spawned lantern at {} {} {}", x, y, z);
        }
    }
}
