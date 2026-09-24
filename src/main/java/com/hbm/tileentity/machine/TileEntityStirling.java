package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.tile.IHeatSource;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.entity.projectile.EntityCog;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AutoRegister
public class TileEntityStirling extends TileEntityLoadedBase implements IEnergyProviderMK2, IConfigurableMachine, ITickable, IConnectionAnchors {

    /* CONFIGURABLE CONSTANTS */
    public static double diffusion = 0.1D;
    public static double efficiency = 0.5D;
    public static int maxHeatNormal = 300;
    public static int maxHeatSteel = 1500;
    public static int overspeedLimit = 300;
    public long powerBuffer;
    public int heat;
    public boolean hasCog = true;
    public float spin;
    public float lastSpin;
    AxisAlignedBB bb = null;
    private int warnCooldown = 0;
    private int overspeed = 0;
    private int syncHeat = 0;
    private boolean destroyedByCreativePlayer = false;

    @Override
    public void update() {

        if (!world.isRemote) {

            if (hasCog) {
                this.powerBuffer = 0;
                tryPullHeat();

                this.powerBuffer = (long) (this.heat * (this.isCreative() ? 1 : efficiency));

                if (warnCooldown > 0)
                    warnCooldown--;

                if (heat > maxHeat() && !isCreative()) {

                    this.overspeed++;

                    if (overspeed > 60 && warnCooldown == 0) {
                        warnCooldown = 100;
                        world.playSound(null, this.pos, HBMSoundHandler.warnOverspeed, SoundCategory.BLOCKS, 2F, 1F);
                    }

                    if (overspeed > overspeedLimit) {
                        this.hasCog = false;
                        this.world.newExplosion(null, pos.getX() + 0.5F, pos.getY() + 1f, pos.getZ() + 0.5f, 5F, false, false);

                        int orientation = this.getBlockMetadata() - BlockDummyable.offset;
                        ForgeDirection dir = ForgeDirection.getOrientation(orientation);
                        EntityCog cog = new EntityCog(world, pos.getX() + 0.5 + dir.offsetX, pos.getY() + 1, pos.getZ() + 0.5 + dir.offsetZ).setOrientation(orientation).setMeta(this.getGeatMeta());
                        ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

                        cog.motionX = rot.offsetX;
                        cog.motionY = 1 + (heat - maxHeat()) * 0.0001D;
                        cog.motionZ = rot.offsetZ;
                        world.spawnEntity(cog);

                        this.markDirty();
                    }

                } else {
                    this.overspeed = 0;
                }
            } else {
                this.overspeed = 0;
                this.warnCooldown = 0;
            }
            syncHeat = heat;
            networkPackNT(150);

            if (hasCog) {
                for (DirPos pos : getConPos()) {
                    this.tryProvide(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                }
            } else {

                if (this.powerBuffer > 0)
                    this.powerBuffer--;
            }

            this.heat = 0;
        } else {

            float momentum = powerBuffer * 50F / ((float) maxHeat());

            if (this.isCreative()) momentum = Math.min(momentum, 45F);

            this.lastSpin = this.spin;
            this.spin += momentum;

            if (this.spin >= 360F) {
                this.spin -= 360F;
                this.lastSpin -= 360F;
            }
        }
    }

    public int getGeatMeta() {
        return this.getBlockType() == ModBlocks.machine_stirling ? 0 : this.getBlockType() == ModBlocks.machine_stirling_creative ? 2 : 1;
    }

    public int maxHeat() {
        return this.getBlockType() == ModBlocks.machine_stirling ? 300 : 1500;
    }

    public boolean isCreative() {
        return this.getBlockType() == ModBlocks.machine_stirling_creative;
    }

    public DirPos[] getConPos() {
        return new DirPos[]{
                new DirPos(this.pos.east(2), Library.POS_X),
                new DirPos(this.pos.west(2), Library.NEG_X),
                new DirPos(this.pos.south(2), Library.POS_Z),
                new DirPos(this.pos.north(2), Library.NEG_Z)
        };
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeLong(powerBuffer);
        buf.writeInt(syncHeat);
        buf.writeBoolean(hasCog);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.powerBuffer = buf.readLong();
        this.heat = buf.readInt();
        this.hasCog = buf.readBoolean();
    }


    protected void tryPullHeat() {
        TileEntity con = world.getTileEntity(this.getPos().down());

        if (con instanceof IHeatSource source) {
            int heatSrc = (int) (source.getHeatStored() * diffusion);

            if (heatSrc > 0) {
                source.useUpHeat(heatSrc);
                this.heat += heatSrc;
                return;
            }
        }

        this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.powerBuffer = nbt.getLong("powerBuffer");
        this.hasCog = nbt.getBoolean("hasCog");
        this.overspeed = nbt.getInteger("overspeed");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("powerBuffer", powerBuffer);
        nbt.setBoolean("hasCog", hasCog);
        nbt.setInteger("overspeed", overspeed);
        return nbt;
    }

    @Override
    public long getPower() {
        return powerBuffer;
    }

    @Override
    public void setPower(long power) {
        this.powerBuffer = power;
    }

    @Override
    public long getMaxPower() {
        return powerBuffer;
    }

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) {
            bb = new AxisAlignedBB(
                    pos.add(-1, 0, -1),
                    pos.add(2, 2, 2)
            );
        }
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public String getConfigName() {
        return "stirling";
    }

    @Override
    public void readIfPresent(JsonObject obj) {
        diffusion = IConfigurableMachine.grab(obj, "D:diffusion", diffusion);
        efficiency = IConfigurableMachine.grab(obj, "D:efficiency", efficiency);
        maxHeatNormal = IConfigurableMachine.grab(obj, "I:maxHeatNormal", maxHeatNormal);
        maxHeatSteel = IConfigurableMachine.grab(obj, "I:maxHeatSteel", maxHeatSteel);
        overspeedLimit = IConfigurableMachine.grab(obj, "I:overspeedLimit", overspeedLimit);
    }

    @Override
    public void writeConfig(JsonWriter writer) throws IOException {
        writer.name("D:diffusion").value(diffusion);
        writer.name("D:efficiency").value(efficiency);
        writer.name("I:maxHeatNormal").value(maxHeatNormal);
        writer.name("I:maxHeatSteel").value(maxHeatSteel);
        writer.name("I:overspeedLimit").value(overspeedLimit);
    }

    public void setDestroyedByCreativePlayer() {
        destroyedByCreativePlayer = true;
    }

    public boolean isDestroyedByCreativePlayer() {
        return destroyedByCreativePlayer;
    }

    public boolean shouldDrop() {
        return !isDestroyedByCreativePlayer();
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(
                    new NTMEnergyCapabilityWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }
}
