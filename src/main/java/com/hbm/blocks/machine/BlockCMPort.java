package com.hbm.blocks.machine;

import com.hbm.tileentity.TileEntityProxyCombo;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BlockCMPort<E extends Enum<E>> extends BlockCM<E> implements ITileEntityProvider {

	public BlockCMPort(String registryName, E[] blockEnum) {
		super(registryName, blockEnum);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityProxyCombo().inventory().power().fluid();
	}
}
