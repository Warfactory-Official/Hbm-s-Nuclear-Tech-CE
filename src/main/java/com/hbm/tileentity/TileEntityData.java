package com.hbm.tileentity;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

@AutoRegister
public class TileEntityData extends TileEntity {

    public int metadata;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        metadata = nbt.getInteger("meta");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("meta", metadata);
        return nbt;
    }
}
