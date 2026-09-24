package com.hbm.blocks.network;

import com.hbm.blocks.machine.BlockMachineBase;
import com.hbm.tileentity.network.TileEntityPneumoStorageExporter;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class PneumoStorageExporter extends BlockMachineBase {

	public PneumoStorageExporter(Material materialIn, String s) {
		super(materialIn, 0, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityPneumoStorageExporter();
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);

		if(te instanceof TileEntityPneumoStorageExporter) {
			TileEntityPneumoStorageExporter exporter = (TileEntityPneumoStorageExporter) te;
			for(int i = 0; i < 9; i++) exporter.inventory.setStackInSlot(i, ItemStack.EMPTY);
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
