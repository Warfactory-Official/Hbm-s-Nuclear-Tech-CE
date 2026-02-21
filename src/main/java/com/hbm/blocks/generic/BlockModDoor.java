package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockModDoor extends BlockDoor implements INBTBlockTransformable {
    public BlockModDoor(Material materialIn, String key) {
        super(materialIn);
        this.setTranslationKey(key);
        this.setRegistryName(key);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    private static void playDoorSound(World world, BlockPos pos) {
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.openDoor, SoundCategory.BLOCKS, 1.0F,
                world.rand.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        BlockPos base = state.getValue(HALF) == EnumDoorHalf.LOWER ? pos : pos.down();
        IBlockState baseState = pos.equals(base) ? state : worldIn.getBlockState(base);

        if (baseState.getBlock() != this) return false;

        IBlockState toggled = baseState.cycleProperty(OPEN);
        worldIn.setBlockState(base, toggled, 10);
        worldIn.markBlockRangeForRenderUpdate(base, pos);
        playDoorSound(worldIn, pos);
        return true;
    }

    @Override
    public void toggleDoor(World worldIn, BlockPos pos, boolean open) {
        IBlockState state = worldIn.getBlockState(pos);
        if (state.getBlock() != this) return;

        BlockPos base = state.getValue(HALF) == EnumDoorHalf.LOWER ? pos : pos.down();
        IBlockState baseState = pos.equals(base) ? state : worldIn.getBlockState(base);

        if (baseState.getBlock() == this && baseState.getValue(OPEN) != open) {
            worldIn.setBlockState(base, baseState.withProperty(OPEN, open), 10);
            worldIn.markBlockRangeForRenderUpdate(base, pos);
            playDoorSound(worldIn, pos);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (state.getValue(HALF) == EnumDoorHalf.UPPER) {
            BlockPos below = pos.down();
            IBlockState belowState = worldIn.getBlockState(below);
            if (belowState.getBlock() != this) {
                worldIn.setBlockToAir(pos);
            } else if (blockIn != this) {
                belowState.neighborChanged(worldIn, below, blockIn, fromPos);
            }
            return;
        }

        boolean changed = false;
        BlockPos above = pos.up();
        IBlockState aboveState = worldIn.getBlockState(above);

        if (aboveState.getBlock() != this) {
            worldIn.setBlockToAir(pos);
            changed = true;
        }

        if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP)) {
            worldIn.setBlockToAir(pos);
            changed = true;
            if (aboveState.getBlock() == this) {
                worldIn.setBlockToAir(above);
            }
        }

        if (changed) {
            if (!worldIn.isRemote) this.dropBlockAsItem(worldIn, pos, state, 0);
            return;
        }

        boolean powered = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(above);
        if (blockIn != this && (powered || blockIn.getDefaultState().canProvidePower()) && powered != aboveState.getValue(POWERED)) {
            worldIn.setBlockState(above, aboveState.withProperty(POWERED, powered), 2);
            if (powered != state.getValue(OPEN)) {
                worldIn.setBlockState(pos, state.withProperty(OPEN, powered), 2);
                worldIn.markBlockRangeForRenderUpdate(pos, pos);
                playDoorSound(worldIn, pos);
            }
        }
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.IRON;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == EnumDoorHalf.UPPER ? Items.AIR : this.getItem();
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this.getItem());
    }

    private Item getItem() {
        if (this == ModBlocks.door_metal) return ModItems.door_metal;
        if (this == ModBlocks.door_office) return ModItems.door_office;
        if (this == ModBlocks.door_red) return ModItems.door_red;
        return ModItems.door_bunker;
    }

    @Override
    public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaDoor(meta, coordBaseMode);
    }
}
