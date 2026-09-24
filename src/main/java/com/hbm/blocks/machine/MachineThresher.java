package com.hbm.blocks.machine;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineThresher;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MachineThresher extends BlockContainer implements ILookOverlay, ITooltipProvider, IToolable {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public MachineThresher(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityMachineThresher();
	}

	@Override public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) { return EnumBlockRenderType.ENTITYBLOCK_ANIMATED; }
	@Override public boolean isOpaqueCube(@NotNull IBlockState state) { return false; }
	@Override public boolean isFullCube(@NotNull IBlockState state) { return false; }

	@Override
	protected @NotNull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.byIndex(meta);
		if(facing.getAxis() == EnumFacing.Axis.Y) facing = EnumFacing.NORTH;
		return this.getDefaultState().withProperty(FACING, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	@Override
	public @NotNull IBlockState getStateForPlacement(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(!world.isRemote && !player.isSneaking()) {

			ItemStack held = player.getHeldItem(hand);

			if(!held.isEmpty() && held.getItem() instanceof IItemFluidIdentifier) {

				TileEntity te = world.getTileEntity(pos);
				if(!(te instanceof TileEntityMachineThresher)) return false;
				TileEntityMachineThresher thresher = (TileEntityMachineThresher) te;

				FluidType type = ((IItemFluidIdentifier) held.getItem()).getType(world, pos.getX(), pos.getY(), pos.getZ(), held);
				if(type == null) return false;

				thresher.tank.setTankType(type);
				thresher.markDirty();
				player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Set fluid to " + I18nUtil.resolveKey(type.getTranslationKey())));
				return true;
			}
		}

		return !world.isRemote;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER) return false;

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if(!(te instanceof TileEntityMachineThresher)) return false;
		TileEntityMachineThresher thresher = (TileEntityMachineThresher) te;

		thresher.isSuspended = !thresher.isSuspended;
		thresher.markDirty();

		if(!world.isRemote) {
			player.sendMessage(new TextComponentString(TextFormatting.YELLOW + (thresher.isSuspended ? "Suspended" : "Resumed")));
		}

		return true;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(TextFormatting.GOLD + "Use screwdriver to suspend operation");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {

		TileEntity te = world.getTileEntity(pos);
		if(!(te instanceof TileEntityMachineThresher)) return;
		TileEntityMachineThresher thresher = (TileEntityMachineThresher) te;

		List<String> text = new ArrayList<>();
		text.add(thresher.tank.getFill() + " / " + thresher.tank.getMaxFill() + "mB " + I18nUtil.resolveKey(thresher.tank.getTankType().getTranslationKey()));
		if(thresher.isSuspended) text.add(TextFormatting.RED + "Suspended");

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
}
