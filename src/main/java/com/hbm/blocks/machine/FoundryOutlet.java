package com.hbm.blocks.machine;

import com.hbm.api.block.ICrucibleAcceptor;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.TileEntityFoundryOutlet;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;

public class FoundryOutlet extends BlockContainer implements ICrucibleAcceptor, ILookOverlay, IToolable {

	public static AxisAlignedBB[] boxes;
	static {
		boxes = new AxisAlignedBB[EnumFacing.VALUES.length];
		boxes[EnumFacing.WEST.ordinal()] = new AxisAlignedBB(0.625, 0, 0.3125, 1, 0.5, 0.6875);
		boxes[EnumFacing.EAST.ordinal()] = new AxisAlignedBB(0, 0, 0.3125, 0.375, 0.5, 0.6875);
		boxes[EnumFacing.NORTH.ordinal()] = new AxisAlignedBB(0.3125, 0, 0.625, 0.6875, 0.5, 1);
		boxes[EnumFacing.SOUTH.ordinal()] = new AxisAlignedBB(0.3125, 0, 0, 0.6875, 0.5, 0.375);
	}

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public FoundryOutlet(String s) {
		super(Material.ROCK);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setSoundType(SoundType.METAL);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING});
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing)state.getValue(FACING)).getIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
	{
	   return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return boxes[state.getValue(FACING).ordinal()];
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundryOutlet();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
	 		return true;
	 	}

	 	if(!player.isSneaking()) {
	 		TileEntityFoundryOutlet tile = (TileEntityFoundryOutlet) world.getTileEntity(pos);

	 		if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == ModItems.scraps) {
	 			MaterialStack mat = ItemScraps.getMats(player.getHeldItem(hand));
	 			if(mat != null) {
	 				tile.filter = mat.material;
	 			}
	 		} else {
	 			tile.invertRedstone = !tile.invertRedstone;
	 		}
	 		tile.markDirty();
	 		world.markAndNotifyBlock(pos, world.getChunk(pos), state, state, 2);
	 	}

	 	return true;
	}

	@Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {

		if(tool == ToolType.SCREWDRIVER) {
			if(world.isRemote) return true;

			TileEntityFoundryOutlet tile = (TileEntityFoundryOutlet) world.getTileEntity(new BlockPos(x, y, z));
			tile.filter = null;
			tile.invertFilter = false;
			tile.markDirty();
		}

		if(tool == ToolType.HAND_DRILL) {
			if(world.isRemote) return true;

			TileEntityFoundryOutlet tile = (TileEntityFoundryOutlet) world.getTileEntity(new BlockPos(x, y, z));
			tile.invertFilter = !tile.invertFilter;
			tile.markDirty();
		}

		return false;
	}

	@Override public boolean canAcceptPartialPour(World world, BlockPos p, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return false; }
	@Override public MaterialStack pour(World world, BlockPos p, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return stack; }

	@Override
	public boolean canAcceptPartialFlow(World world, BlockPos p, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(p)).canAcceptPartialFlow(world, p, side, stack);
	}

	@Override
	public MaterialStack flow(World world, BlockPos p, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(p)).flow(world, p, side, stack);
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

	@Override
	public void printHook(Pre event, World world, BlockPos pos) {
		TileEntityFoundryOutlet outlet = (TileEntityFoundryOutlet) world.getTileEntity(pos);
		List<String> text = new ArrayList<>();

//		if(outlet.isClosed()) {
//			text.add("§c" + I18nUtil.resolveKey("foundry.status.closed"));
//		} else {
//			text.add("§a" + I18nUtil.resolveKey("foundry.status.open"));
//		}
		if(outlet.filter != null) {
			text.add("§e" + I18nUtil.resolveKey("foundry.filter", outlet.filter.names[0]));
		}
		if(outlet.invertFilter) {
			text.add("§e" + I18nUtil.resolveKey("foundry.invertFilter"));
		}
		if(outlet.invertRedstone) {
			text.add("§2" + I18nUtil.resolveKey("foundry.inverted"));
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getTranslationKey() + ".name"), 0xFF4000, 0x401000, text);
	}
}
