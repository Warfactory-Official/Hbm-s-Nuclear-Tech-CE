package com.hbm.blocks.machine;

import com.hbm.api.block.ICrucibleAcceptor;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemScraps;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.TileEntityFoundryChannel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class FoundryChannel extends BlockContainer implements ICrucibleAcceptor {

	public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);

	public FoundryChannel(String s) {
		super(Material.ROCK);
		this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, META);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		updateMeta(world, pos);
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(META, getConnectionMeta(worldIn, pos));
	}

	private void updateMeta(World world, BlockPos pos) {
		IBlockState oldState = world.getBlockState(pos);
		int oldMeta = oldState.getValue(META);

		int meta = getConnectionMeta(world, pos);

		if (meta == oldMeta) return;

		TileEntity tile = world.getTileEntity(pos);
		NBTTagCompound nbt = null;
		if (tile != null) {
			nbt = tile.writeToNBT(new NBTTagCompound());
		}

		world.setBlockState(pos, this.getDefaultState().withProperty(META, meta), 3);

		if (nbt != null) {
			TileEntity newTile = world.getTileEntity(pos);
			if (newTile != null) {
				newTile.readFromNBT(nbt);
				newTile.markDirty();
			}
		}
	}

	private int getConnectionMeta(World world, BlockPos pos) {
		int meta = 0;
		if (canConnectTo(world, pos, EnumFacing.EAST)) meta |= 1;
		if (canConnectTo(world, pos, EnumFacing.WEST)) meta |= 2;
		if (canConnectTo(world, pos, EnumFacing.SOUTH)) meta |= 4;
		if (canConnectTo(world, pos, EnumFacing.NORTH)) meta |= 8;
		return meta;
	}

	public boolean canConnectTo(World world, BlockPos pos, EnumFacing direction) {
		BlockPos neighborPos = pos.offset(direction);
		IBlockState neighborState = world.getBlockState(neighborPos);
		Block neighborBlock = neighborState.getBlock();

		if (neighborBlock instanceof FoundryOutlet) {
			if (neighborBlock.getMetaFromState(neighborState) == direction.getIndex()) return true;
		}

		return neighborBlock instanceof FoundryChannel || neighborBlock == ModBlocks.foundry_mold;
	}

	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundryChannel();
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean isActualState) {
		int meta = state.getValue(META);
		List<AxisAlignedBB> boxes = new ArrayList<>();

		// Center box
		boxes.add(new AxisAlignedBB(0.3125D, 0, 0.3125D, 0.6875D, 0.5D, 0.6875D));

		// Add boxes based on meta
		if ((meta & 1) != 0) boxes.add(new AxisAlignedBB(0.6875D, 0, 0.3125D, 1D, 0.5D, 0.6875D)); // +X
		if ((meta & 2) != 0) boxes.add(new AxisAlignedBB(0, 0, 0.3125D, 0.3125D, 0.5D, 0.6875D));   // -X
		if ((meta & 4) != 0) boxes.add(new AxisAlignedBB(0.3125D, 0, 0.6875D, 0.6875D, 0.5D, 1D));  // +Z
		if ((meta & 8) != 0) boxes.add(new AxisAlignedBB(0.3125D, 0, 0, 0.6875D, 0.5D, 0.3125D));   // -Z

		for (AxisAlignedBB box : boxes) {
			if (entityBox.intersects(box.offset(pos))) {
				collidingBoxes.add(box.offset(pos));
			}
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		int meta = state.getValue(META);
		double minX = (meta & 2) != 0 ? 0 : 0.3125D;
		double maxX = (meta & 1) != 0 ? 1 : 0.6875D;
		double minZ = (meta & 8) != 0 ? 0 : 0.3125D;
		double maxZ = (meta & 4) != 0 ? 1 : 0.6875D;
		return new AxisAlignedBB(minX, 0, minZ, maxX, 0.5D, maxZ);
	}

	@Override
	public boolean canAcceptPartialPour(World world, BlockPos pos, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(pos)).canAcceptPartialPour(world, pos, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack pour(World world, BlockPos pos, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(pos)).pour(world, pos, dX, dY, dZ, side, stack);
	}

	@Override
	public boolean canAcceptPartialFlow(World world, BlockPos pos, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(pos)).canAcceptPartialFlow(world, pos, side, stack);
	}
	
	@Override
	public MaterialStack flow(World world, BlockPos pos, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(pos)).flow(world, pos, side, stack);
	}

	public boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {
		if (dir == ForgeDirection.UP || dir == ForgeDirection.DOWN || dir == ForgeDirection.UNKNOWN)
			return false;

		BlockPos neighborPos = new BlockPos(x + dir.offsetX, y, z + dir.offsetZ);
		IBlockState neighborState = world.getBlockState(neighborPos);
		Block neighborBlock = neighborState.getBlock();

		if (neighborBlock instanceof FoundryChannel) {
			int neighborMeta = neighborState.getValue(META);
			return (neighborMeta & (1 << dir.getOpposite().ordinal())) != 0;
		}

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
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		}
		
		TileEntityFoundryChannel cast = (TileEntityFoundryChannel) world.getTileEntity(pos);
		
		if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof ItemTool && (player.getHeldItem(hand).getItem()).getToolClasses(player.getHeldItem(hand)).contains("shovel")) {
			if(cast.amount > 0) {
				ItemStack scrap = ItemScraps.create(new MaterialStack(cast.type, cast.amount));
				if(!player.inventory.addItemStackToInventory(scrap)) {
					EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + this.getBoundingBox(state, world, pos).maxY, pos.getZ() + 0.5, scrap);
					world.spawnEntity(item);
				} else {
					player.inventoryContainer.detectAndSendChanges();
				}
				cast.amount = 0;
				cast.type = null;
				cast.propagateMaterial(null);
				cast.markDirty();
				world.markAndNotifyBlock(pos, world.getChunk(pos), state, state, 3);
			}
			return true;
		}
		
		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		
		TileEntityFoundryChannel channel = (TileEntityFoundryChannel) world.getTileEntity(pos);
		if(channel.amount > 0) {
			ItemStack scrap = ItemScraps.create(new MaterialStack(channel.type, channel.amount));
			EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + this.getBoundingBox(state, world, pos).maxY, pos.getZ() + 0.5, scrap);
			world.spawnEntity(item);
			channel.amount = 0;
		}
		
		super.breakBlock(world, pos, state);
	}
}
