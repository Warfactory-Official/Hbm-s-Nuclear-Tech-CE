package com.hbm.blocks.network.energy;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.network.energy.TileEntityCableSwitch;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CableDetector extends BlockContainer {

	public static final PropertyBool STATE = PropertyBool.create("state");
	
	public CableDetector(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityCableSwitch();
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if(world.isRemote) return;

		boolean on = world.isBlockPowered(pos);
		boolean wasOn = state.getValue(STATE);

		boolean update = false;

		if(on && !wasOn) {
			world.setBlockState(pos, state.withProperty(STATE, true), 2);
			world.playSound(null, pos, HBMSoundHandler.reactorStart, SoundCategory.BLOCKS, 1.0F, 1.0F);
			update = true;
		} else if(!on && wasOn) {
			world.setBlockState(pos, state.withProperty(STATE, false), 2);
			world.playSound(null, pos, HBMSoundHandler.reactorStart, SoundCategory.BLOCKS, 1.0F, 0.85F);
			update = true;
		}

		if(update) {
			TileEntityCableSwitch te = (TileEntityCableSwitch) world.getTileEntity(pos);
			te.updateState();
		}
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { STATE });
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(STATE).booleanValue() == true ? 1 : 0;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(STATE, meta == 1 ? true : false);
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
