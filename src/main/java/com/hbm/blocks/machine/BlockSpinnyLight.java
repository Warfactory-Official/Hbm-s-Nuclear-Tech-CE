package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.deco.TileEntitySpinnyLight;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

public class BlockSpinnyLight extends BlockContainer {

	public static final PropertyDirection FACING = BlockDirectional.FACING;
	public static final PropertyBool POWERED = PropertyBool.create("powered");
	public static AxisAlignedBB[] boxes;
	static {
		boxes = new AxisAlignedBB[EnumFacing.VALUES.length];
		boxes[EnumFacing.UP.ordinal()] = new AxisAlignedBB(0.3, 0, 0.3, 0.7, 0.4F, 0.7);
		boxes[EnumFacing.DOWN.ordinal()] = new AxisAlignedBB(0.3, 0.6, 0.3, 0.7, 1, 0.7);
		boxes[EnumFacing.NORTH.ordinal()] = new AxisAlignedBB(0.3, 0.3, 0.6, 0.7, 0.7, 1);
		boxes[EnumFacing.SOUTH.ordinal()] = new AxisAlignedBB(0.3, 0.3, 0, 0.7, 0.7, 0.4);
		boxes[EnumFacing.EAST.ordinal()] = new AxisAlignedBB(0, 0.3, 0.3, 0.4, 0.7, 0.7);
		boxes[EnumFacing.WEST.ordinal()] = new AxisAlignedBB(0.6, 0.3, 0.3, 1, 0.7, 0.7);
	}

	public BlockSpinnyLight(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntitySpinnyLight();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!playerIn.getHeldItem(hand).isEmpty() && !worldIn.isRemote){
			int[] ores = OreDictionary.getOreIDs(playerIn.getHeldItem(hand));
			for(int ore : ores){
				String name = OreDictionary.getOreName(ore);
				//Why are these ones named differently
				if(name.equals("dyeLightBlue"))
					name = "dyeLight_Blue";
				if(name.equals("dyeLightGray"))
					name = "dyeSilver";
				if(name.length() > 3 && name.startsWith("dye")){
					try {
						EnumDyeColor color = EnumDyeColor.valueOf(name.substring(3, name.length()).toUpperCase());
						TileEntitySpinnyLight ent = (TileEntitySpinnyLight)worldIn.getTileEntity(pos);
						ent.color = color;
						ent.markDirty();
						worldIn.notifyBlockUpdate(pos, state, state, 2 | 4);
						if(!playerIn.isCreative())
							playerIn.getHeldItem(hand).shrink(1);
						return true;
					} catch(IllegalArgumentException e){}
				}
			}
		}
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(stack, player, tooltip, advanced);
		tooltip.add(I18nUtil.resolveKey("desc.spinnylight"));
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return boxes[state.getValue(FACING).ordinal()];
	}
	
	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
	
	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
		return canPlaceBlock(worldIn, pos, side);
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		for(EnumFacing enumfacing : EnumFacing.VALUES) {
			if(canPlaceBlock(worldIn, pos, enumfacing)) {
				return true;
			}
		}
		return false;
	}

	//From BlockButton
	@SuppressWarnings("deprecation")
	protected static boolean canPlaceBlock(World worldIn, BlockPos pos, EnumFacing direction) {
		BlockPos blockpos = pos.offset(direction.getOpposite());
		IBlockState iblockstate = worldIn.getBlockState(blockpos);
		boolean flag = iblockstate.getBlockFaceShape(worldIn, blockpos, direction) == BlockFaceShape.SOLID;
		Block block = iblockstate.getBlock();

		if(direction == EnumFacing.UP) {
			return iblockstate.isTopSolid() || !isExceptionBlockForAttaching(block) && flag;
		} else {
			return !isExceptBlockForAttachWithPiston(block) && flag;
		}
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return canPlaceBlock(worldIn, pos, facing) ? this.getDefaultState().withProperty(FACING, facing).withProperty(POWERED, Boolean.FALSE) : this.getDefaultState().withProperty(FACING, EnumFacing.DOWN).withProperty(POWERED, Boolean.FALSE);
	}

	private boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state) {
		if(this.canPlaceBlockAt(worldIn, pos)) {
			return true;
		} else {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
			return false;
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (this.checkForDrop(world, pos, state) && !canPlaceBlock(world, pos, (EnumFacing)state.getValue(FACING)))
        {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
            return;
        }
		if(world.isBlockPowered(pos)) {
			if(!state.getValue(POWERED)){
				TileEntity te = world.getTileEntity(pos);
				world.setBlockState(pos, state.withProperty(POWERED, true));
				if(te != null){
					te.validate();
					world.setTileEntity(pos, te);
				}
			}
		} else {
			if(state.getValue(POWERED)){
				TileEntity te = world.getTileEntity(pos);
				world.setBlockState(pos, state.withProperty(POWERED, false));
				if(te != null){
					te.validate();
					world.setTileEntity(pos, te);
				}
			}
		}
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
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
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@Override
	public int getLightValue(IBlockState state) {
		return state.getValue(POWERED) ? 5 : 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, POWERED);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int powered = state.getValue(POWERED) ? 1 : 0;
		int facing = state.getValue(FACING).ordinal();
		return facing | (powered << 3);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean powered = ((meta >>> 3) & 1) > 0;
		EnumFacing facing = EnumFacing.VALUES[meta & 7];
		return this.getDefaultState().withProperty(FACING, facing).withProperty(POWERED, powered);
	}

}
