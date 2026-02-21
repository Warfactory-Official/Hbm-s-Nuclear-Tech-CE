package com.hbm.blocks.fluid;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ModDamageSource;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import java.util.Random;

public class AcidBlock extends BlockFluidClassic implements IFluidFog {
    public static DamageSource damageSource;

    public AcidBlock(Fluid fluid, Material material, DamageSource damage, String name) {
        super(fluid, material);
        damageSource = damage;
        this.setTranslationKey(name);
        this.setRegistryName(name);
        this.setQuantaPerBlock(4);
        this.setCreativeTab(null);
        displacements.put(this, false);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public boolean canDisplace(IBlockAccess world, BlockPos pos) {
        if (world.getBlockState(pos).getMaterial().isLiquid()) {
            return false;
        }
        return super.canDisplace(world, pos);
    }

    @Override
    public boolean displaceIfPossible(World world, BlockPos pos) {
        if (world.getBlockState(pos).getMaterial().isLiquid()) {
            return false;
        }
        return super.displaceIfPossible(world, pos);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entity) {
        entity.setInWeb();
        entity.attackEntityFrom(ModDamageSource.acid, 10000F);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        reactToBlocks(world, pos.east());
        reactToBlocks(world, pos.west());
        reactToBlocks(world, pos.up());
        reactToBlocks(world, pos.down());
        reactToBlocks(world, pos.south());
        reactToBlocks(world, pos.north());
        super.updateTick(world, pos, state, rand);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighbourPos) {
        reactToBlocks(world, pos.east());
        reactToBlocks(world, pos.west());
        reactToBlocks(world, pos.up());
        reactToBlocks(world, pos.down());
        reactToBlocks(world, pos.south());
        reactToBlocks(world, pos.north());
        super.neighborChanged(state, world, pos, neighborBlock, neighbourPos);
    }

    public void reactToBlocks(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() != ModBlocks.acid_block) {
            world.setBlockToAir(pos);
        }
    }

    @Override
    public int tickRate(World world) {
        return 5;
    }

    @Override
    public float getFogDensity() {
        return 2.0F;
    }

    @Override
    public int getFogColor() {
        return 0x9F00B9;
    }
}
