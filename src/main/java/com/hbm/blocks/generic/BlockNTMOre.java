package com.hbm.blocks.generic;

import com.hbm.blocks.IOreType;
import com.hbm.blocks.ModBlocks;
import com.hbm.hazard.HazardSystem;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockNTMOre extends BlockOre {


    public static int xp;
    protected final IOreType oreEnum;

    public BlockNTMOre(String name, IOreType oreEnum, int harvestLvl, int xp) {
        super();
        BlockNTMOre.xp = xp;
        this.oreEnum = oreEnum;
        this.setTranslationKey(name);
        this.setRegistryName(name);
        this.setCreativeTab(MainRegistry.controlTab);
        this.setTickRandomly(false);
        this.setHarvestLevel("pickaxe", harvestLvl);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    public BlockNTMOre(String name, @Nullable IOreType oreEnum, int harvestLvl) {
        this(name, oreEnum, harvestLvl, 2);
    }

    public BlockNTMOre(String name, int harvestLvl) {
        this(name, null, harvestLvl, 2);
    }


    public BlockNTMOre(SoundType sound, String name, IOreType oreEnum, int harvestLvl) {
        this(name, oreEnum, harvestLvl);
        super.setSoundType(sound);
    }

    @Override
    public boolean canSilkHarvest(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player) {
        if (this == ModBlocks.ore_oil) return false;
        return super.canSilkHarvest(world, pos, state, player);
    }

    @Override
    public int getExpDrop(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, int fortune) {
        if (this.getItemDropped(state, RANDOM, fortune) != Item.getItemFromBlock(this))
            return xp;
        return 0;
    }

    @Override
    public void getDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull IBlockState state, int fortune) {

        Random rand =  ((World)world).rand;
        //TODO: perhaps move everyting to meta
        //For the time, just normal blocks


        int count = (oreEnum == null) ? quantityDropped(state, fortune, rand) : oreEnum.getQuantityFunction().apply(state, fortune, rand);

        for (int i = 0; i < count; i++)
        {
            ItemStack droppedItem;

            if(oreEnum  == null) {
                droppedItem = new ItemStack(this.getItemDropped(state, rand, fortune), 1, this.damageDropped(state));
            } else {
                droppedItem = oreEnum.getDropFunction().apply(state, rand);
            }

            if (!droppedItem.isEmpty())
            {
                drops.add(droppedItem);
            }
        }
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return 0;
    }


    @Override
    public void neighborChanged(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos) {
        if (this == ModBlocks.ore_oil && world.getBlockState(pos.down()).getBlock() == ModBlocks.ore_oil_empty) {
            world.setBlockState(pos, ModBlocks.ore_oil_empty.getDefaultState());
            world.setBlockState(pos.down(), ModBlocks.ore_oil.getDefaultState());
        }
    }

    @Override
    public void onEntityWalk(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (entity instanceof EntityLivingBase)
            HazardSystem.applyHazards(this, (EntityLivingBase)entity);
    }

    @Override
    public void onEntityCollision(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Entity entity) {
        if (entity instanceof EntityLivingBase)
            HazardSystem.applyHazards(this, (EntityLivingBase)entity);
    }
    // Th3_Sl1ze: I'm not sure this doesn't cause charred wood to be able to burn...
    @Override
    public @NotNull Material getMaterial(@NotNull IBlockState state)
    {
        if(this == ModBlocks.waste_planks) return Material.WOOD;
        else return super.getMaterial(state);
    }
}
