package com.hbm.blocks.machine.pile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.blocks.machine.MachinePWRController;
import com.hbm.lib.ForgeDirection;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.machine.pile.TileEntityPileBaseMK2;
import com.hbm.tileentity.machine.pile.TileEntityPileCore;
import com.hbm.util.I18nUtil;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class BlockPile extends BlockMeta implements ITileEntityProvider, IToolable, ILookOverlay {

	public static final int META_DUMMY		= 0;
	public static final int META_CORE		= 1;
	public static final int META_CHANNEL	= 2;
	public static final int META_FUEL_IN	= 3;
	public static final int META_FUEL_OUT	= 4;
	public static final int META_AIR_IN		= 5;
	public static final int META_AIR_OUT	= 6;
	public static final int META_CONTROL	= 7;
	public static final int META_EDGE		= 8;

	public BlockPile(String s) {
		super(Material.IRON, s, (short) 9, false, frames());
		this.setCreativeTab(null);
	}

	protected static BlockBakeFrame[] frames() {
		BlockBakeFrame[] frames = new BlockBakeFrame[9];
		frames[META_DUMMY] = BlockBakeFrame.cubeBottomTop("pile_block_top", "pile_block", "pile_block_top");
		frames[META_CORE] = BlockBakeFrame.cubeBottomTop("pile_block_top", "pile_block_core", "pile_block_top");
		frames[META_CHANNEL] = BlockBakeFrame.cubeBottomTop("pile_block_top", "pile_block", "pile_block_top");
		frames[META_FUEL_IN] = BlockBakeFrame.cubeBottomTop("pile_block_top", "pile_block_input", "pile_block_top");
		frames[META_FUEL_OUT] = BlockBakeFrame.cubeBottomTop("pile_block_top", "pile_block_output", "pile_block_top");
		frames[META_AIR_IN] = BlockBakeFrame.cubeBottomTop("pile_block_top", "pile_block_input", "pile_block_top");
		frames[META_AIR_OUT] = BlockBakeFrame.cubeBottomTop("pile_block_top", "pile_block_output", "pile_block_top");
		frames[META_CONTROL] = BlockBakeFrame.cubeBottomTop("pile_block_control_top", "pile_block", "pile_block_control_top");
		frames[META_EDGE] = BlockBakeFrame.cubeBottomTop("pile_block_top", "pile_block", "pile_block_top");
		return frames;
	}

	@Override
	protected boolean useCTM() {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta == META_CORE) return new TileEntityPileCore();
		return new TileEntityPileBaseMK2();
	}

	@NotNull
	@Override
	public Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
		return Items.AIR;
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return new ArrayList<>();
	}

	@Override
	public void breakBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {

		if(!TileEntityPileCore.meltingDown) {
			TileEntity tile = world.getTileEntity(pos);

			if(tile instanceof TileEntityPileBaseMK2) {
				TileEntityPileBaseMK2 pile = (TileEntityPileBaseMK2) tile;
				world.removeTileEntity(pos);
				if(pile.coreY >= 0) world.setBlockState(pos, ModBlocks.pile_brick.getDefaultState());

				TileEntityPileCore core = pile.getCore();
				if(core != null && !core.isInvalid()) core.destroy();

			} else {
				world.removeTileEntity(pos);
				world.setBlockState(pos, ModBlocks.pile_brick.getDefaultState());
			}
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {

		if(tool == ToolType.HAND_DRILL) {

			BlockPos pos = new BlockPos(x, y, z);
			TileEntity tile = world.getTileEntity(pos);

			if(tile instanceof TileEntityPileCore || world.getBlockState(pos).getValue(META) == META_CORE) {
				MachinePWRController.sendError(world, pos, "Cannot intersect core", player);
				return false;
			}

			if(tile instanceof TileEntityPileBaseMK2) {
				if(world.isRemote) return true;
				TileEntityPileCore core = ((TileEntityPileBaseMK2) tile).getCore();
				if(core != null) {
					ForgeDirection dir = ForgeDirection.getOrientation(side).getOpposite();
					return core.drillChannel(x, y, z, dir, player);
				}
			}

			MachinePWRController.sendError(world, pos, "No core found", player);
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
		int meta = world.getBlockState(pos).getValue(META);
		List<String> text = new ArrayList<>();
		if(meta == META_FUEL_IN) text.add("Fuel Loading Port");
		if(meta == META_FUEL_OUT) text.add("Fuel Ejection Port");
		if(meta == META_AIR_IN) text.add("Air Inlet");
		if(meta == META_AIR_OUT) text.add("Air Outlet");
		if(meta == META_CONTROL) text.add("Control Rod Channel");

		if(meta == META_CORE) {
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof TileEntityPileCore) {
				TileEntityPileCore core = (TileEntityPileCore) tile;
				text.add("Max Temp: " + Math.round(core.highestHeat) + " / " + TileEntityPileCore.MAX_HEAT + "°C");
			}
		}

		if(!text.isEmpty()) ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
}
