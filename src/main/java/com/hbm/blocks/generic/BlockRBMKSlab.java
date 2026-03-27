package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.items.block.ItemBlockBase;
import com.hbm.main.MainRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockRBMKSlab extends BlockBase implements ICustomBlockItem {
	public boolean isDouble;
	public Block doubleBlock;
	public Block singleBlock;
	public BlockRBMKSlab(Material m,String s) {
		super(m,s);
		this.isDouble = true;
		setHardness(5);
		setResistance(10);
		setCreativeTab(null);
		fullBlock = false;
		useNeighborBrightness = false;
	}
	public BlockRBMKSlab(Material m,String s,BlockRBMKSlab doubleBlock) {
		super(m,s);
		this.doubleBlock = doubleBlock;
		doubleBlock.singleBlock = this;
		this.isDouble = false;
		setHardness(5);
		setResistance(10);
		setCreativeTab(MainRegistry.blockTab);
		fullBlock = false;
		useNeighborBrightness = false;
	}
	@Override
	public boolean isTopSolid(IBlockState state) {
		return false;
	}
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess source,BlockPos pos) {
		return isDouble ? new AxisAlignedBB(0,0,0,1,4/16d,1) : new AxisAlignedBB(0,0,0,1,2/16d,1);
	}
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn,IBlockState state,BlockPos pos,EnumFacing face) {
		return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
	{
		if (side == EnumFacing.UP)
		{
			return true;
		}
		else
		{
			IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
			return (iblockstate.getBlock() == this || (!isDouble && iblockstate.getBlock() == doubleBlock)) || super.shouldSideBeRendered(blockState, blockAccess, pos, side);
		}
	}
	@Override
	protected boolean canSilkHarvest()
	{
		return false;
	}
	@Override
	public Item getItemDropped(IBlockState state,Random rand,int fortune) {
		return isDouble ? Item.getItemFromBlock(singleBlock) : Item.getItemFromBlock(this);
	}
	@Override
	public int quantityDropped(Random random) {
		return isDouble ? 2 : 1;
	}
	@Override
	public ItemStack getPickBlock(IBlockState state,RayTraceResult target,World world,BlockPos pos,EntityPlayer player) {
		Item item = isDouble ? Item.getItemFromBlock(singleBlock) : Item.getItemFromBlock(this);
		return new ItemStack(item,1);
	}
	@Override
	public void registerItem() {
		if (isDouble) return;
		ItemRBMKSlab item = new ItemRBMKSlab(this,doubleBlock);
		item.setRegistryName(getRegistryName());
		ForgeRegistries.ITEMS.register(item);
	}
	public static class ItemRBMKSlab extends ItemBlockBase {
		public Block doubleSlab;
		public ItemRBMKSlab(Block block,Block doubleSlab) {
			super(block);
			this.doubleSlab = doubleSlab;
		}
		// mostly copied code
		@Override
		public EnumActionResult onItemUse(EntityPlayer player,World worldIn,BlockPos pos,EnumHand hand,EnumFacing facing,float hitX,float hitY,float hitZ) {
			ItemStack itemstack = player.getHeldItem(hand);

			if (!itemstack.isEmpty() && player.canPlayerEdit(pos, facing, itemstack))
			{
				IBlockState iblockstate = worldIn.getBlockState(pos);
				Block block = iblockstate.getBlock();
				BlockPos blockpos = pos;

				if ((facing != EnumFacing.UP || block != this.block) && !block.isReplaceable(worldIn, pos))
				{
					blockpos = pos.offset(facing);
					iblockstate = worldIn.getBlockState(blockpos);
					block = iblockstate.getBlock();
				}

				if (block == this.block)
				{
					IBlockState iblockstate1 = doubleSlab.getDefaultState();
					AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, blockpos);

					if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(blockpos)) && worldIn.setBlockState(blockpos, iblockstate1, 10))
					{
						SoundType soundtype = this.block.getSoundType(iblockstate1, worldIn, pos, player);
						worldIn.playSound(player, blockpos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

						if (player instanceof EntityPlayerMP)
						{
							CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, itemstack);
						}

						itemstack.shrink(1);
						return EnumActionResult.SUCCESS;
					}
				}

				return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
			}
			else
			{
				return EnumActionResult.FAIL;
			}
		}
		@Override
		public boolean canPlaceBlockOnSide(World world,BlockPos pos,EnumFacing side,EntityPlayer player,ItemStack stack) {
			IBlockState state = world.getBlockState(pos);
			return (state.getBlock() instanceof BlockRBMKSlab rbmkSlab && !rbmkSlab.isDouble) || super.canPlaceBlockOnSide(world,pos,side,player,stack);
		}
	}
}
