package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBattery;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.machine.TileEntityRadiobox;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class Radiobox extends BlockContainer {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool STATE = PropertyBool.create("state");
	
	public static final float f = 0.0625F;
	public static final AxisAlignedBB EAST_BB = new AxisAlignedBB(0*f, 1*f, 4*f, 5*f, 15*f, 12*f);
	public static final AxisAlignedBB NORTH_BB = new AxisAlignedBB(4*f, 1*f, 11*f, 12*f, 15*f, 16*f);
	public static final AxisAlignedBB WEST_BB = new AxisAlignedBB(11*f, 1*f, 4*f, 16*f, 15*f, 12*f);
	public static final AxisAlignedBB SOUTH_BB = new AxisAlignedBB(4*f, 1*f, 0*f, 12*f, 15*f, 5*f);
	
	public Radiobox(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityRadiobox();
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			TileEntityRadiobox box = (TileEntityRadiobox)world.getTileEntity(pos);
			boolean wasInfinite = false;
			if(box != null)
				wasInfinite = box.infinite;

			if(player.getHeldItem(hand).getItem() == ModItems.battery_spark && !wasInfinite) {
				player.getHeldItem(hand).shrink(1);
				world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.5F, 1.0F);
				box.infinite = true;
				box.markDirty();
				return true;
			}
			boolean on = world.getBlockState(pos).getValue(STATE);
			if(!on) {
				world.setBlockState(pos, world.getBlockState(pos).withProperty(STATE, true));
				world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.reactorStart, SoundCategory.BLOCKS, 1.0F, 1.0F);
			} else {
				world.setBlockState(pos, world.getBlockState(pos).withProperty(STATE, false));
				world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.reactorStart, SoundCategory.BLOCKS, 1.0F, 0.85F);
			}
			((TileEntityRadiobox)world.getTileEntity(pos)).infinite = wasInfinite;
			
			return true;
		} else {
			//FMLNetworkHandler.openGui(player, MainRegistry.instance, ModBlocks.guiID_radiobox, world, x, y, z);
			//return true;
			return false;
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntityRadiobox box = (TileEntityRadiobox)world.getTileEntity(pos);

		if(box != null && box.infinite) {
			world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ItemBattery.getEmptyBattery(ModItems.battery_spark)));
		}
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		switch(state.getValue(FACING)){
		case EAST:
			return EAST_BB;
		case WEST:
			return WEST_BB;
		case NORTH:
			return NORTH_BB;
		case SOUTH:
			return SOUTH_BB;
		default:
			return NORTH_BB;
		}
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(STATE, false);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING, STATE });
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (((EnumFacing) state.getValue(FACING)).getIndex() << 1) + (state.getValue(STATE) == true ? 1 : 0);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean state = (meta & 1) == 1 ? true : false;
		meta = meta >> 1;
		EnumFacing enumfacing = EnumFacing.byIndex(meta);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(STATE, state);
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(stack, player, tooltip, advanced);
		tooltip.add("Right click when powered to kill all hostile mobs in 15m radius.");
		tooltip.add("Right click with Spark-battery to make it selfcharging.");
	}
}
