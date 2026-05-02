package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.ArmorUtil;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BarbedWire extends Block {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BarbedWire(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity ent) {
        ent.setInWeb();

        if (this == ModBlocks.barbed_wire) {
            ent.attackEntityFrom(DamageSource.CACTUS, 2.0F);
        }

        if (this == ModBlocks.barbed_wire_fire) {
            ent.attackEntityFrom(DamageSource.CACTUS, 2.0F);
            ent.setFire(1);
        }

        if (this == ModBlocks.barbed_wire_poison) {
            ent.attackEntityFrom(DamageSource.CACTUS, 2.0F);

            if (ent instanceof EntityLivingBase)
                ((EntityLivingBase) ent).addPotionEffect(new PotionEffect(MobEffects.POISON, 5 * 20, 2));

        }

        if (this == ModBlocks.barbed_wire_acid) {
            ent.attackEntityFrom(DamageSource.CACTUS, 2.0F);

            if (ent instanceof EntityPlayer) {
                ArmorUtil.damageSuit((EntityPlayer) ent, 0, 1);
                ArmorUtil.damageSuit((EntityPlayer) ent, 1, 1);
                ArmorUtil.damageSuit((EntityPlayer) ent, 2, 1);
                ArmorUtil.damageSuit((EntityPlayer) ent, 3, 1);
            }
        }

        if (this == ModBlocks.barbed_wire_wither) {
            ent.attackEntityFrom(DamageSource.CACTUS, 3.0F);

            if (ent instanceof EntityLivingBase)
                ((EntityLivingBase) ent).addPotionEffect(new PotionEffect(MobEffects.WITHER, 5 * 20, 4));
        }

        if (this == ModBlocks.barbed_wire_ultradeath) {
            ent.attackEntityFrom(ModDamageSource.pc, 5.0F);

            if (ent instanceof EntityLivingBase)
                ((EntityLivingBase) ent).addPotionEffect(new PotionEffect(HbmPotion.radiation, 5 * 20, 9));
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return ((EnumFacing) state.getValue(FACING)).getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }


    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
    }

}
