package com.hbm.tileentity.rail;

import com.hbm.interfaces.AutoRegister;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityRailSwitch extends TileEntity {

    public boolean isSwitched = false;

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public @NotNull NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.isSwitched = nbt.getBoolean("isSwitched");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("isSwitched", this.isSwitched);
        return nbt;
    }
}
