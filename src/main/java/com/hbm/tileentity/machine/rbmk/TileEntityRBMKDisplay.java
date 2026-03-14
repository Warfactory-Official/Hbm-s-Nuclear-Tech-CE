package com.hbm.tileentity.machine.rbmk;

import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.Compat;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityRBMKDisplay extends TileEntityLoadedBase implements ITickable {

    private int targetX;
    private int targetY;
    private int targetZ;

    private byte rotation;

    public RBMKColumn[] columns = new RBMKColumn[7 * 7];

    @Override
    public void update() {

        if(!world.isRemote) {

            if(this.world.getTotalWorldTime() % 10 == 0) {
                rescan();
                this.networkPackNT(50);
            }
        }
    }

    public void setTarget(int x, int y, int z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.markDirty();
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);

        for (RBMKColumn column : this.columns) {
            RBMKColumn.writeToBuf(buf, column);
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);

        for (int i = 0; i < this.columns.length; i++) {
            this.columns[i] = RBMKColumn.readFromBuf(buf);
        }
    }

    private void rescan() {

        for(int index = 0; index < columns.length; index++) {
            int rx = getXFromIndex(index);
            int rz = getZFromIndex(index);

            TileEntity te = Compat.getTileStandard(world, targetX + rx, targetY, targetZ + rz);

            if(te instanceof TileEntityRBMKBase base) {
                columns[index] = base.getConsoleData();
            } else {
                columns[index] = null;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.targetX = nbt.getInteger("tX");
        this.targetY = nbt.getInteger("tY");
        this.targetZ = nbt.getInteger("tZ");
        this.rotation = nbt.getByte("rotation");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("tX", this.targetX);
        nbt.setInteger("tY", this.targetY);
        nbt.setInteger("tZ", this.targetZ);
        nbt.setByte("rotation", this.rotation);
        return super.writeToNBT(nbt);
    }

    public void rotate() {
        rotation = (byte)((rotation + 1) % 4);
    }

    public int getXFromIndex(int col) {
        int i = col % 7 - 3;
        int j = col / 7 - 3;
        return switch (rotation) {
            case 1 -> -j;
            case 2 -> -i;
            case 3 -> j;
            default -> i;
        };
    }

    public int getZFromIndex(int col) {
        int i = col % 7 - 3;
        int j = col / 7 - 3;
        return switch (rotation) {
            case 1 -> i;
            case 2 -> -j;
            case 3 -> -i;
            default -> j;
        };
    }
}
