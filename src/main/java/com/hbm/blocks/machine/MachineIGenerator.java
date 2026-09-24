package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineIGenerator;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MachineIGenerator extends BlockDummyable implements ILookOverlay {

	public MachineIGenerator(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {

		if(meta >= 12)
			return new TileEntityMachineIGenerator();

		if(meta >= extra)
			return new TileEntityProxyCombo(false, true, true);

		return new TileEntityProxyCombo(true, false, false);
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
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
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			return this.findCore(world, pos.getX(), pos.getY(), pos.getZ()) != null;
		} else {
			return false;
		}
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
		List<String> text = new ArrayList<>();
		text.add("In memory of all that we have lost");
		ILookOverlay.printGeneric(event, "Industrial Generator Memorial", 0xff8000, 0x804000, text);
	}
}
