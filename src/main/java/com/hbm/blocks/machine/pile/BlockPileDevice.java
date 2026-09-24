package com.hbm.blocks.machine.pile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.machine.pile.TileEntityPileControl;
import com.hbm.tileentity.machine.pile.TileEntityPileCore;
import com.hbm.tileentity.machine.pile.TileEntityPileLoader;
import com.hbm.tileentity.machine.pile.TileEntityPileVent;
import com.hbm.util.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockPileDevice extends BlockMeta implements ITileEntityProvider, IBlockMulti, ILookOverlay, IToolable {

	public static final int ITEM_META_LOADER = 0;
	public static final int ITEM_META_VENT = 1;
	public static final int ITEM_META_CONTROL = 2;

	public static final int BLOCK_META_LOADER = 0;
	public static final int BLOCK_META_VENT = 4;
	public static final int BLOCK_META_CONTROL = 8;

	public BlockPileDevice(String s) {
		super(Material.IRON, s, (short) 3, true);
	}

	@Override
	public int getSubCount() {
		return 3;
	}

	@Override
	protected boolean useSpecialRenderer() {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		meta -= meta % 4;
		if(meta == BLOCK_META_LOADER) return new TileEntityPileLoader();
		if(meta == BLOCK_META_VENT) return new TileEntityPileVent();
		if(meta == BLOCK_META_CONTROL) return new TileEntityPileControl();
		return null;
	}

	@NotNull
	@Override
	public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override public boolean isOpaqueCube(@NotNull IBlockState state) { return false; }
	@Override public boolean isFullCube(@NotNull IBlockState state) { return false; }

	@NotNull
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		int metaOffset = itemMetaToBlockMeta(meta);
		return this.getDefaultState().withProperty(META, metaOffset + MathHelper.clamp(facing.getIndex() - 2, 0, 3));
	}

	@Override
	public boolean onBlockActivated(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(player.isSneaking()) return false;

		int meta = state.getValue(META);
		meta -= meta % 4;

		if(meta == BLOCK_META_LOADER) {
			if(world.isRemote) return true;

			TileEntityPileLoader tile = (TileEntityPileLoader) world.getTileEntity(pos);
			if(tile == null) return true;

			if(tile.level <= 0 && !tile.loading) {

				ItemStack held = player.getHeldItem(hand);

				if(!held.isEmpty() && tile.stack.isEmpty() && TileEntityPileLoader.isItemLoadable(held)) {
					tile.stack = held.copy();
					tile.stack.setCount(1);
					held.shrink(1);
					world.playSound(null, pos, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1F, 1F);
					return true;
				}

				tile.loading = true;
			}

			return true;
		}

		return false;
	}

	@Override
	public void onBlockPlacedBy(@NotNull World world, @NotNull BlockPos pos, IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
		if(state.getValue(META) != BLOCK_META_CONTROL) return;

		int i = MathHelper.floor(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		if(i == 0) world.setBlockState(pos, state.withProperty(META, BLOCK_META_CONTROL), 2);
		if(i == 1) world.setBlockState(pos, state.withProperty(META, BLOCK_META_CONTROL + 3), 2);
		if(i == 2) world.setBlockState(pos, state.withProperty(META, BLOCK_META_CONTROL + 1), 2);
		if(i == 3) world.setBlockState(pos, state.withProperty(META, BLOCK_META_CONTROL + 2), 2);
	}

	public static int itemMetaToBlockMeta(int meta) {
		if(meta >= ITEM_META_CONTROL) return BLOCK_META_CONTROL;
		if(meta == ITEM_META_VENT) return BLOCK_META_VENT;
		return BLOCK_META_LOADER;
	}

	@Override
	public int damageDropped(@NotNull IBlockState state) {
		int meta = state.getValue(META);
		if(meta >= BLOCK_META_CONTROL) return ITEM_META_CONTROL;
		if(meta >= BLOCK_META_VENT) return ITEM_META_VENT;
		return ITEM_META_LOADER;
	}

	@Override
	public @NotNull List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return Arrays.asList(new ItemStack(Item.getItemFromBlock(this), 1, damageDropped(state)));
	}

	@NotNull
	@Override
	public ItemStack getPickBlock(@NotNull IBlockState state, @NotNull net.minecraft.util.math.RayTraceResult target, @NotNull World world, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1, damageDropped(state));
	}

	@Override
	public boolean isSideSolid(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing side) {
		int meta = state.getValue(META);
		if(meta >= BLOCK_META_CONTROL) return side.getIndex() == meta % 4 + 2;
		if(meta >= BLOCK_META_VENT) return false;
		return side.getIndex() == meta % 4 + 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);

		List<String> text = new ArrayList<>();
		TileEntity tile = world.getTileEntity(pos);

		if(tile instanceof TileEntityPileLoader device) {
            text.add("Temp: " + Math.round(device.channelTemp) + " / " + TileEntityPileCore.MAX_HEAT + "°C");
			if(!device.syncStack.isEmpty()) text.add("Loading: " + device.syncStack.getDisplayName());

			if(!device.channelStack.isEmpty()) {
				text.add("Last rod: " + device.channelStack.getDisplayName());
				if(device.channelDepletion > 0) text.add("Depletion: " + Math.round(device.channelDepletion) + "%");
			}
		}

		if(tile instanceof TileEntityPileControl device) {
            text.add("Extraction level: " + (int) (device.level * 100) + "%");
		}

		if(!text.isEmpty())
			ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getTranslationKey() + "_" + this.damageDropped(state) + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {

		int meta = world.getBlockState(new BlockPos(x, y, z)).getValue(META);

		if(meta >= BLOCK_META_CONTROL) {
			y -= 1;
			side = EnumFacing.UP;
		} else {
			ForgeDirection dir = ForgeDirection.getOrientation(meta % 4 + 2);
			x -= dir.offsetX;
			z -= dir.offsetZ;
			side = dir.toEnumFacing();
		}

		Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock();
		if(b == ModBlocks.pile_block) {
			return ((BlockPile) b).onScrew(world, player, x, y, z, side, fX, fY, fZ, hand, tool);
		}

		return false;
	}
}
