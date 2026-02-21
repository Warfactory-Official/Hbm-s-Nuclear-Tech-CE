package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
// Th3_Sl1ze: I'm too lazy to make this shit bakeable, don't mind me for now
public class BlockSupplyCrate extends BlockContainer {

    public BlockSupplyCrate(Material materialIn, String s) {
        super(materialIn);
        this.setHarvestLevel("axe", 0);
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntitySupplyCrate();
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override public @NotNull Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) { return null; }

    @Override
    public boolean removedByPlayer(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos, EntityPlayer player, boolean willHarvest) {

        if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest) {

            ItemStack drop = new ItemStack(this);
            TileEntitySupplyCrate inv = (TileEntitySupplyCrate) world.getTileEntity(pos);
            NBTTagCompound nbt = new NBTTagCompound();

            if(inv != null) {
                for(int i = 0; i < inv.items.size(); i++) {
                    ItemStack stack = inv.items.get(i);
                    if(stack == null) continue;
                    NBTTagCompound slot = new NBTTagCompound();
                    stack.writeToNBT(slot);
                    nbt.setTag("slot" + i, slot);
                }
                nbt.setInteger("amount", inv.items.size());
            }

            if(!nbt.isEmpty()) drop.setTagCompound(nbt);
            world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
        }
        return world.setBlockToAir(pos);
    }

    @Override
    public void onBlockPlacedBy(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {

        TileEntitySupplyCrate inv = (TileEntitySupplyCrate) world.getTileEntity(pos);

        if(inv != null && stack.hasTagCompound()) {
            int amount = stack.getTagCompound().getInteger("amount");
            for(int i = 0; i < amount; i++) {
                inv.items.add(new ItemStack(stack.getTagCompound().getCompoundTag("slot" + i)));
            }
        }

        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public boolean onBlockActivated(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem().equals(ModItems.crowbar)) {
            if(!world.isRemote) {
                dropContents(world, pos);
                world.destroyBlock(pos, true);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.crateBreak, SoundCategory.BLOCKS, 0.5F, 1.0F);
            }
            return true;
        }
        return false;
    }

    public void dropContents(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntitySupplyCrate crate) {

            for (ItemStack stack : crate.items) {
                Block.spawnAsEntity(world, pos, stack);
            }
        }
    }

    @AutoRegister
    public static class TileEntitySupplyCrate extends TileEntity {

        public List<ItemStack> items = new ArrayList<>();

        @Override
        public void readFromNBT(@NotNull NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            items.clear();
            NBTTagList list = nbt.getTagList("items", 10);
            for(int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbt1 = list.getCompoundTagAt(i);
                items.add(new ItemStack(nbt1));
            }
        }

        @Override
        public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
            NBTTagList list = new NBTTagList();
            for (ItemStack item : items) {
                NBTTagCompound nbt1 = new NBTTagCompound();
                item.writeToNBT(nbt1);
                list.appendTag(nbt1);
            }
            nbt.setTag("items", list);
            return super.writeToNBT(nbt);
        }
    }
}
