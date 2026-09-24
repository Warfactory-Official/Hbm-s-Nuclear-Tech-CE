package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDoorGeneric;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

@AutoRegister(name = "tileentity_vault_door_migration")
public class TileEntityVaultDoorMigration extends TileEntity implements ITickable {

	@Override
	public void update() {

		if(!world.isRemote) {

			int meta = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));

			if(meta <= 5) {
				BlockDoorGeneric door = (BlockDoorGeneric) ModBlocks.vault_door;
				world.setBlockState(pos, door.getStateFromMeta(meta + 10), 3);
				door.fillSpace(world, pos.getX(), pos.getY(), pos.getZ(), ForgeDirection.getOrientation(meta), 0);
			}
		}
	}
}
