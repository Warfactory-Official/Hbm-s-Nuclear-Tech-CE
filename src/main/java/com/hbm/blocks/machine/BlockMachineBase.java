package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMachineBase extends BlockContainer implements INBTBlockTransformable {

	int guiID = -1;
	
	public BlockMachineBase(Material materialIn, int guiID, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.guiID = guiID;
		
		ModBlocks.ALL_BLOCKS.add(this);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(guiID == -1)
			return false;

		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			player.openGui(MainRegistry.instance, this.guiID, world, pos.getX(), pos.getY(), pos.getZ());
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
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		if(!rotatable())
			return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
		return this.getDefaultState().withProperty(BlockHorizontal.FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		if(rotatable()){
			return new BlockStateContainer(this, new IProperty[]{BlockHorizontal.FACING});
		}
		return super.createBlockState();
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		if(!rotatable())
			return 0;
		return ((EnumFacing)state.getValue(BlockHorizontal.FACING)).getIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		if(!rotatable())
			return this.getDefaultState();
		EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(BlockHorizontal.FACING, enumfacing);
	}
	
	protected boolean rotatable(){
		return false;
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		if(!rotatable()) return meta;
		return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
	}
	
}
