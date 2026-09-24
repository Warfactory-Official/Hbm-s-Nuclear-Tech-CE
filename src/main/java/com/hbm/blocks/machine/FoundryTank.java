package com.hbm.blocks.machine;

import com.hbm.api.block.ICrucibleAcceptor;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemScraps;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.TileEntityFoundryTank;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class FoundryTank extends BlockContainer implements ICrucibleAcceptor {

	public static final PropertyBool UP = PropertyBool.create("up");
	public static final PropertyBool DOWN = PropertyBool.create("down");
	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool WEST = PropertyBool.create("west");
	public static final PropertyBool OUT_NORTH = PropertyBool.create("out_north");
	public static final PropertyBool OUT_SOUTH = PropertyBool.create("out_south");
	public static final PropertyBool OUT_EAST = PropertyBool.create("out_east");
	public static final PropertyBool OUT_WEST = PropertyBool.create("out_west");

	public FoundryTank(String s) {
		super(Material.ROCK);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	protected @NotNull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, UP, DOWN, NORTH, SOUTH, EAST, WEST, OUT_NORTH, OUT_SOUTH, OUT_EAST, OUT_WEST);
	}

	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState();
	}

	@Override
	public int getMetaFromState(@NotNull IBlockState state) {
		return 0;
	}

	@Override
	public @NotNull IBlockState getActualState(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
		return state
				.withProperty(UP, isTank(world, pos.up()))
				.withProperty(DOWN, isTank(world, pos.down()))
				.withProperty(NORTH, isTank(world, pos.north()))
				.withProperty(SOUTH, isTank(world, pos.south()))
				.withProperty(EAST, isTank(world, pos.east()))
				.withProperty(WEST, isTank(world, pos.west()))
				.withProperty(OUT_NORTH, isOutlet(world, pos, EnumFacing.NORTH))
				.withProperty(OUT_SOUTH, isOutlet(world, pos, EnumFacing.SOUTH))
				.withProperty(OUT_EAST, isOutlet(world, pos, EnumFacing.EAST))
				.withProperty(OUT_WEST, isOutlet(world, pos, EnumFacing.WEST));
	}

	public static boolean isTank(IBlockAccess world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() == ModBlocks.foundry_tank;
	}

	private static boolean isOutlet(IBlockAccess world, BlockPos pos, EnumFacing dir) {
		IBlockState neighbor = world.getBlockState(pos.offset(dir));
		Block block = neighbor.getBlock();
		return block instanceof FoundryOutlet && block.getMetaFromState(neighbor) == dir.getIndex();
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityFoundryTank();
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isOpaqueCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public @NotNull BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess world, @NotNull IBlockState state, @NotNull BlockPos pos, @NotNull EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean canAcceptPartialPour(World world, BlockPos pos, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		TileEntity te = world.getTileEntity(pos);
		return te instanceof ICrucibleAcceptor && ((ICrucibleAcceptor) te).canAcceptPartialPour(world, pos, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack pour(World world, BlockPos pos, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof ICrucibleAcceptor) return ((ICrucibleAcceptor) te).pour(world, pos, dX, dY, dZ, side, stack);
		return stack;
	}

	@Override public boolean canAcceptPartialFlow(World world, BlockPos pos, ForgeDirection side, MaterialStack stack) { return false; }
	@Override public MaterialStack flow(World world, BlockPos pos, ForgeDirection side, MaterialStack stack) { return stack; }

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(world.isRemote) return true;

		TileEntity te = world.getTileEntity(pos);
		if(!(te instanceof TileEntityFoundryTank)) return false;
		TileEntityFoundryTank cast = (TileEntityFoundryTank) te;

		ItemStack held = player.getHeldItem(hand);

		if(!held.isEmpty() && held.getItem() instanceof ItemTool && ((ItemTool) held.getItem()).getToolClasses(held).contains("shovel")) {
			if(cast.amount > 0) {
				ItemStack scrap = ItemScraps.create(new MaterialStack(cast.type, cast.amount));
				if(!player.inventory.addItemStackToInventory(scrap)) {
					world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, scrap));
				}
				cast.amount = 0;
				cast.type = null;
				cast.markDirty();
				return true;
			}
		}

		return false;
	}
}
