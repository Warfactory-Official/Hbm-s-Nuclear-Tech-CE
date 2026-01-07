package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.world.HugeMush;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockMush extends BlockBush implements IGrowable {
    protected static final AxisAlignedBB MUSHROOM_AABB = new AxisAlignedBB(0.30000001192092896D, 0.0D, 0.30000001192092896D, 0.699999988079071D, 0.4000000059604645D, 0.699999988079071D);

    public BlockMush(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.controlTab);
        this.setTickRandomly(true);
        this.setSoundType(SoundType.PLANT);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return MUSHROOM_AABB;
    }

    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            Block block = world.getBlockState(pos.down()).getBlock();
            return block == ModBlocks.waste_earth || block == ModBlocks.waste_mycelium;
        } else {
            return false;
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return this.canBlockStay(world, pos, world.getBlockState(pos));
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return this.canBlockStay(world, pos, world.getBlockState(pos));
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        this.checkAndDropBlock(worldIn, pos, state);
        if (GeneralConfig.enableMycelium && (worldIn.getBlockState(pos.down()).getBlock() == ModBlocks.waste_mycelium || worldIn.getBlockState(pos.down()).getBlock() == ModBlocks.waste_earth) && rand.nextInt(5) == 0) {
            worldIn.setBlockState(pos.down(), ModBlocks.waste_mycelium.getDefaultState());
        }

    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return (double) rand.nextFloat() < 0.4D;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        this.generateBigMushroom(worldIn, pos, state, rand);
    }

    private boolean generateBigMushroom(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        worldIn.setBlockToAir(pos);
        HugeMush worldgenbigmushroom = null;

        worldgenbigmushroom = new HugeMush();
        worldgenbigmushroom.generate(worldIn, rand, pos);

        return true;
    }

}
