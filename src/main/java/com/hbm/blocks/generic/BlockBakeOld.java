package com.hbm.blocks.generic;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

// automatic transform. Works best when an update is already scheduled
public class BlockBakeOld extends BlockBakeBase {
    private final IOnUpdateTick transform;

    public BlockBakeOld(Material m, String s, IOnUpdateTick onUpdateTick) {
        super(m, s);
        transform = onUpdateTick;
        setTickRandomly(true); //fallback
    }

    public BlockBakeOld(Material m, String s, IBlockState replacement) {
        this(m, s, (w, p, _) -> w.setBlockState(p, replacement));
    }

    public interface IOnUpdateTick {
        void tick(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state);
    }

    @Override
    public int tickRate(@NotNull World worldIn) {
        return 1;
    }

    @Override
    public void observedNeighborChange(IBlockState observerState, World world, BlockPos observerPos, Block changedBlock, BlockPos changedBlockPos) {
        transform.tick(world, observerPos, observerState);//fallback
    }

    @Override
    public void updateTick(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Random rand) {
        transform.tick(world, pos, state);
    }

    @Override
    public void onBlockAdded(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        worldIn.scheduleUpdate(pos, this, 1);//fallback in case players still have old item
    }
}
