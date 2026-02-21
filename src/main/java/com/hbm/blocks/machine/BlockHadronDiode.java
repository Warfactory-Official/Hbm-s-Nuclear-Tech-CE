package com.hbm.blocks.machine;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.TileEntityHadronDiode;
import com.hbm.tileentity.machine.TileEntityHadronDiode.DiodeConfig;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockHadronDiode extends BlockContainer implements IToolable {

	public static final PropertyInteger[] textures = new PropertyInteger[6];
	static {
		for(int i = 0; i < textures.length; i ++){
			textures[i] = PropertyInteger.create(EnumFacing.VALUES[i].getName2(), 0, 2);
		}
	}
	
	public BlockHadronDiode(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityHadronDiode();
	}
	
	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool){
		if(tool != ToolType.SCREWDRIVER)
			return false;
		
		if(!world.isRemote) {
			TileEntityHadronDiode diode = (TileEntityHadronDiode) world.getTileEntity(new BlockPos(x, y, z));
			int config = diode.getConfig(side.ordinal()).ordinal();
			config += 1;
			config %= DiodeConfig.VALUES.length;
			diode.setConfig(side.ordinal(), config);
			resetBlockState(world, new BlockPos(x, y, z));
		}
		
		return true;
	}
	
	public static void resetBlockState(World world, BlockPos pos){
		TileEntityHadronDiode diode = (TileEntityHadronDiode) world.getTileEntity(pos);
		IBlockState newState = ModBlocks.hadron_diode.getDefaultState();
		for(int i = 0; i < 6; i++){
			newState = newState.withProperty(BlockHadronDiode.textures[i], diode.sides[i].ordinal());
		}
		world.setBlockState(pos, newState);
		diode.validate();
		world.setTileEntity(pos, diode);
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, textures);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState();
	}
	
}
