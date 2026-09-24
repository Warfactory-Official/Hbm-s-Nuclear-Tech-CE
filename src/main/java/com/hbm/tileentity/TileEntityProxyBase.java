package com.hbm.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.machine.MachineDiFurnaceExtension;
import com.hbm.tileentity.machine.TileEntityDiFurnace;
import com.hbm.util.Compat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class TileEntityProxyBase extends TileEntityLoadedBase {

	public BlockPos cachedPosition;

	public TileEntity getTE() {

		if(cachedPosition != null) {
			TileEntity te = Compat.getTileStandard(world, cachedPosition.getX(), cachedPosition.getY(), cachedPosition.getZ());
			if(te != null && !(te instanceof TileEntityProxyBase)) return te;
			cachedPosition = null;
			this.markDirty();
		}

		if(this.getBlockType() instanceof BlockDummyable dummy) {

            int[] pos = dummy.findCore(world, this.pos.getX(), this.pos.getY(), this.pos.getZ());

			if(pos != null) {

				TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));

				if(te != null)
					return te;
			}
		}

		/// this spares me the hassle of registering a new child class TE that aims at the right target ///
		//Drillgon200: Incidentally, it's also a gateway to some very messy code, the very thing this class is supposed to prevent.

		if (this.getBlockType() instanceof MachineDiFurnaceExtension) {
			TileEntity te = world.getTileEntity(pos.down());

			if (te instanceof TileEntityDiFurnace) {
				return te;
			}
		}

		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		if(nbt.getBoolean("hasPos")) cachedPosition = new BlockPos(nbt.getInteger("pX"), nbt.getInteger("pY"), nbt.getInteger("pZ"));
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if(this.cachedPosition != null) {
			nbt.setBoolean("hasPos", true);
			nbt.setInteger("pX", this.cachedPosition.getX());
			nbt.setInteger("pY", this.cachedPosition.getY());
			nbt.setInteger("pZ", this.cachedPosition.getZ());
		}
		return nbt;
	}
}