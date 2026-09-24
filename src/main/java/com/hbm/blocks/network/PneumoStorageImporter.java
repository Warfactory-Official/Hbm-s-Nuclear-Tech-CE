package com.hbm.blocks.network;

import com.hbm.blocks.machine.BlockMachineBase;
import com.hbm.tileentity.network.TileEntityPneumoStorageImporter;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class PneumoStorageImporter extends BlockMachineBase {

	public PneumoStorageImporter(Material materialIn, String s) {
		super(materialIn, 0, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityPneumoStorageImporter();
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
