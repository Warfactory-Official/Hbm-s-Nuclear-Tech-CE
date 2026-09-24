package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IMultiBlock;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityDummy;
import com.hbm.tileentity.machine.TileEntityMachineSatDock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class MachineSatDock extends BlockContainer implements IMultiBlock {

	public static final AxisAlignedBB SATDOCK_BOX = new AxisAlignedBB(0, 0, 0, 1, 12*0.0625, 1);
	
	public MachineSatDock(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityMachineSatDock();
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return SATDOCK_BOX;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		InventoryHelper.dropInventoryItems(worldIn, pos, worldIn.getTileEntity(pos));
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		for(int k = -1; k <= 1; k++)
			for(int l = -1; l <= 1; l++)
				if(l != 0 || k != 0)
					if(!world.getBlockState(pos.add(k, 0, l)).getBlock().isReplaceable(world, pos.add(k, 0, l))) {
						world.destroyBlock(pos, true);
						return;
					}
		
		for(int k = -1; k <= 1; k++)
			for(int l = -1; l <= 1; l++)
				if(l != 0 || k != 0)
					placeDummy(world, pos.add(k, 0, l), pos, ModBlocks.dummy_plate_cargo);
	}
	
	private void placeDummy(World world, BlockPos pos, BlockPos target, Block block) {
		
		world.setBlockState(pos, block.getDefaultState());
		
		TileEntity te = world.getTileEntity(pos);
		
		if(te instanceof TileEntityDummy) {
			TileEntityDummy dummy = (TileEntityDummy)te;
			dummy.target = target;
		}
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
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

}
