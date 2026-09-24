package com.hbm.tileentity.bomb;

import com.hbm.blocks.bomb.BlockChargeBase;

import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.ModContext;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.SoundCategory;

import java.util.UUID;

@AutoRegister
public class TileEntityCharge extends TileEntityLoadedBase implements ITickable {

    public boolean started;
    public int timer;
    public UUID placerID;
    private AxisAlignedBB bb;

    @Override
    public void update() {

        if(!world.isRemote) {

            if(started) {
                timer--;

                if(timer % 20 == 0 && timer > 0)
                    world.playSound(null, pos, HBMSoundHandler.fstbmbPing, SoundCategory.BLOCKS, 10.0F, 1.0F);

                if(timer <= 0) {
                    Entity detonator;
                    if (ModContext.DETONATOR_CONTEXT.get() != null)
                        detonator = ModContext.DETONATOR_CONTEXT.get();
                    else detonator = world.getMinecraftServer().getPlayerList().getPlayerByUUID(placerID);
                    ((BlockChargeBase)world.getBlockState(this.pos).getBlock()).explode(world, pos, detonator);
                }
            }

            networkPackNT(100);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(this.timer);
        buf.writeBoolean(this.started);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.timer = buf.readInt();
        this.started = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("timer", timer);
        compound.setBoolean("started", started);
        if (placerID != null)
            compound.setUniqueId("placer", placerID);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        timer = compound.getInteger("timer");
        started = compound.getBoolean("started");
        if (compound.hasKey("placer"))
            placerID = compound.getUniqueId("placer");
    }

    public String getMinutes() {

        String mins = "" + (timer / 1200);

        if(mins.length() == 1)
            mins = "0" + mins;

        return mins;
    }

    public String getSeconds() {

        String mins = "" + ((timer / 20) % 60);

        if(mins.length() == 1)
            mins = "0" + mins;

        return mins;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        return bb;
    }
}
