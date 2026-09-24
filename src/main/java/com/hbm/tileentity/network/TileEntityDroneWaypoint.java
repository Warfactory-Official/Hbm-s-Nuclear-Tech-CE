package com.hbm.tileentity.network;

import com.hbm.entity.item.EntityDeliveryDrone;
import com.hbm.interfaces.AutoRegister;
import com.hbm.util.Vec3NT;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.ParticleUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister
public class TileEntityDroneWaypoint extends TileEntityLoadedBase implements IDroneLinkable, ITickable {

    public int height = 5;
    public int nextX = -1;
    public int nextY = -1;
    public int nextZ = -1;

    @Override
    public void update() {

        if(!world.isRemote) {
            if(nextY != -1) {
                List<EntityDeliveryDrone> drones = world.getEntitiesWithinAABB(EntityDeliveryDrone.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).offset(0, height, 0));
                for(EntityDeliveryDrone drone : drones) {
                    if(Vec3NT.createVectorHelper(drone.motionX, drone.motionY, drone.motionZ).length() < 0.05) {
                        drone.setTarget(nextX + 0.5, nextY, nextZ + 0.5);
                    }
                }
            }

            networkPackNT(15);
        } else {
            BlockPos coord = getCoord();
            if(world.getTotalWorldTime() % 2 == 0) {

                world.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + 0.5, pos.getY() + height + 0.5, pos.getZ() + 0.5, 0, 0, 0);

                if (nextY != -1) {
                    ParticleUtil.spawnDroneLine(world,
                            coord.getX() + 0.5, coord.getY() + 0.5, coord.getZ() + 0.5,
                            (nextX - coord.getX()), (nextY - coord.getY()), (nextZ - coord.getZ()), 0x00ffff);
                }
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(height);
        buf.writeInt(nextX);
        buf.writeInt(nextY);
        buf.writeInt(nextZ);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        height = buf.readInt();
        nextX = buf.readInt();
        nextY = buf.readInt();
        nextZ = buf.readInt();
    }

    @Override
    public BlockPos getPoint() {
        return pos.up(height);
    }

    @Override
    public void setNextTarget(int x, int y, int z) {
        this.nextX = x;
        this.nextY = y;
        this.nextZ = z;
        this.markDirty();
    }

    public void addHeight(int h) {
        height += h;
        height = MathHelper.clamp(height, 1, 15);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.height = nbt.getInteger("height");
        this.nextX = nbt.getInteger("posX");
        this.nextY = nbt.getInteger("posY");;
        this.nextZ = nbt.getInteger("posZ");;
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("height", height);
        nbt.setInteger("posX", nextX);
        nbt.setInteger("posY", nextY);
        nbt.setInteger("posZ", nextZ);
        return nbt;
    }

    public BlockPos getCoord() {
        return new BlockPos(pos.getX() + 0.5, pos.getY() + height + 0.5, pos.getZ() + 0.5);
    }
}
