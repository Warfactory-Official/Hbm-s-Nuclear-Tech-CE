package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.ArmorUtil;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockClorine extends Block {

	public BlockClorine(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setTickRandomly(true);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
		if(!(entity instanceof EntityLivingBase))
			return;

		EntityLivingBase entityLiving = (EntityLivingBase) entity;

		if(ArmorRegistry.hasAllProtection(entityLiving, EntityEquipmentSlot.HEAD, HazardClass.GAS_LUNG)) {
			ArmorUtil.damageGasMaskFilter(entityLiving, 1);
		} else {
			entityLiving.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 5 * 20, 0));
			entityLiving.addPotionEffect(new PotionEffect(MobEffects.POISON, 20 * 20, 2));
			entityLiving.addPotionEffect(new PotionEffect(MobEffects.WITHER, 1 * 20, 1));
			entityLiving.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 30 * 20, 1));
			entityLiving.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 30 * 20, 2));
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.AIR;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
		return false;
	}

	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}

}
