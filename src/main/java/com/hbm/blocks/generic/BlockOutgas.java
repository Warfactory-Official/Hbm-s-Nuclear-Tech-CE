package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.util.DelayedTick;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockOutgas extends BlockNTMOre {

    private final boolean randomTick;
    private final int rate;
    private final boolean onBreak;
    private final boolean onNeighbour;

    public BlockOutgas(boolean randomTick, int rate, boolean onBreak, String s) {
        super(s, 1);
        this.setTickRandomly(randomTick);
        this.randomTick = randomTick;
        this.rate = rate;
        this.onBreak = onBreak;
        this.onNeighbour = false;
    }

    public BlockOutgas(boolean randomTick, int rate, boolean onBreak, boolean onNeighbour, String s) {
        super(s, 1);
        this.setTickRandomly(randomTick);
        this.randomTick = randomTick;
        this.rate = rate;
        this.onBreak = onBreak;
        this.onNeighbour = onNeighbour;
    }

    @Override
    public int tickRate(World world) {
        return rate;
    }

    public Block getGas() {

        if (GeneralConfig.enableRadon) {
            if (this == ModBlocks.ore_uranium || this == ModBlocks.ore_uranium_scorched ||
                    this == ModBlocks.ore_gneiss_uranium || this == ModBlocks.ore_gneiss_uranium_scorched ||
                    this == ModBlocks.ore_nether_uranium || this == ModBlocks.ore_nether_uranium_scorched) {
                return ModBlocks.gas_radon;
            }

            if (this == ModBlocks.block_corium_cobble)
                return ModBlocks.gas_radon_dense;

            if (this == ModBlocks.ancient_scrap)
                return ModBlocks.gas_radon_tomb;
        }

        if (GeneralConfig.enableCarbonMonoxide) {
            if (this == ModBlocks.ore_nether_coal) {
                return ModBlocks.gas_monoxide;
            }
        }

        if (GeneralConfig.enableAsbestosDust) {
            if (this == ModBlocks.ore_asbestos || this == ModBlocks.ore_gneiss_asbestos ||
                    this == ModBlocks.block_asbestos || this == ModBlocks.deco_asbestos ||
                    this == ModBlocks.brick_asbestos || this == ModBlocks.tile_lab ||
                    this == ModBlocks.tile_lab_cracked || this == ModBlocks.tile_lab_broken
            ) {
                return ModBlocks.gas_asbestos;
            }
        }
        return Blocks.AIR;
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        BlockPos up = pos.up();
        if(this.randomTick && getGas() == ModBlocks.gas_asbestos) {
            IBlockState upState = world.getBlockState(up);
            if(upState.getBlock().isAir(upState, world, up)) {

                if(world.rand.nextInt(10) == 0)
                    world.setBlockState(up, ModBlocks.gas_asbestos.getDefaultState());

                for(int i = 0; i < 5; i++)
                    world.spawnParticle(EnumParticleTypes.TOWN_AURA, pos.getX() + world.rand.nextFloat(), pos.getY() + 1.1, pos.getZ() + world.rand.nextFloat(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        if(onBreak) worldIn.setBlockState(pos, getGas().getDefaultState());
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if(onNeighbour && !world.isRemote &&world.rand.nextInt(3) == 0) {
            for(EnumFacing dir : EnumFacing.VALUES) {
                BlockPos targetPos = pos.offset(dir);
                DelayedTick.nextWorldTickEnd(world, w -> {
                    IBlockState targetState = w.getBlockState(targetPos);
                    if (targetState.getBlock().isAir(targetState, w, targetPos)) {
                        w.setBlockState(targetPos, getGas().getDefaultState(), 3);
                    }
                });
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        BlockOutgas block = (BlockOutgas) state.getBlock();
        Block gas = block.getGas();
        if (block == ModBlocks.ancient_scrap) {
            for (int x = -2; x <= 2; x++) for (int y = -2; y <= 2; y++) for (int z = -2; z <= 2; z++) {
                int manhattan = Math.abs(x + y + z);
                if (manhattan > 0 && manhattan < 5) {
                    BlockPos targetPos = pos.add(x, y, z);
                    DelayedTick.nextWorldTickEnd(world, w -> {
                        IBlockState state1 = w.getBlockState(targetPos);
                        if (state1.getBlock().isAir(state1, w, targetPos)) {
                            w.setBlockState(targetPos, gas.getDefaultState(), 3);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) return;
        EnumFacing dir = EnumFacing.VALUES[rand.nextInt(6)];
        BlockPos randomPos = pos.offset(dir);
        IBlockState neighbourPos = world.getBlockState(randomPos);
        if(neighbourPos.getBlock().isAir(neighbourPos, world, randomPos)) {
            world.setBlockState(randomPos, getGas().getDefaultState(), 3);
        }
    }
}