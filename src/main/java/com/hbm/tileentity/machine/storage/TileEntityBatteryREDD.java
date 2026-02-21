package com.hbm.tileentity.machine.storage;

import java.math.BigInteger;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerBatteryREDD;
import com.hbm.inventory.gui.GUIBatteryREDD;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;

import com.hbm.tileentity.IPersistentNBT;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityBatteryREDD extends TileEntityBatteryBase implements IPersistentNBT {

    public float prevRotation = 0F;
    public float rotation = 0F;

    public BigInteger[] log = new BigInteger[20];
    public BigInteger delta = BigInteger.valueOf(0);

    public BigInteger power = BigInteger.valueOf(0);

    private AudioWrapper audio;

    public TileEntityBatteryREDD() {
        super(2);
    }

    @Override
    public String getDefaultName() {
        return "container.batteryREDD";
    }

    @Override
    public void update() {
        BigInteger prevPower = new BigInteger(power.toByteArray());

        super.update();

        if (!world.isRemote) {

            long toAdd = Library.chargeTEFromItems(inventory, 0, 0, this.getMaxPower());
            if (toAdd > 0) this.power = this.power.add(BigInteger.valueOf(toAdd));

            long toRemove = this.getPower() - Library.chargeItemsFromTE(inventory, 1, this.getPower(), this.getMaxPower());
            if (toRemove > 0) this.power = this.power.subtract(BigInteger.valueOf(toRemove));

            // same implementation as for batteries, however retooled to use bigints because fuck
            BigInteger avg = this.power.add(prevPower).divide(BigInteger.valueOf(2));
            this.delta = avg.subtract(this.log[0] == null ? BigInteger.ZERO : this.log[0]);

            for (int i = 1; i < this.log.length; i++) {
                this.log[i - 1] = this.log[i];
            }

            this.log[19] = avg;

        } else {
            this.prevRotation = this.rotation;
            this.rotation += this.getSpeed();

            if (rotation >= 360) {
                rotation -= 360;
                prevRotation -= 360;
            }

            float pitch = 0.5F + this.getSpeed() / 15F * 1.5F;

            if (this.prevRotation != this.rotation && MainRegistry.proxy.me().getDistanceSq(pos.getX() + 0.5, pos.getY() + 5.5, pos.getZ() + 0.5) < 30 * 30) {
                if (this.audio == null) {
                    this.audio = createAudioLoop();
                    this.audio.startSound();
                } else if(!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }

                this.audio.updateVolume(this.getVolume(1.5F));
                this.audio.updatePitch(pitch);
                this.audio.keepAlive();

            } else {
                if (this.audio != null) {
                    this.audio.stopSound();
                    this.audio = null;
                }
            }
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        float pitch = 0.5F + this.getSpeed() / 15F * 1.5F;
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.fensuHum, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), this.getVolume(1.5F), 25F, pitch, 5);
    }

    public float getSpeed() {
        return (float) Math.min(Math.pow(Math.log(this.power.doubleValue() * 0.05 + 1) * 0.05F, 5), 15F);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);

        byte[] array0 = this.power.toByteArray();
        buf.writeInt(array0.length);
        for (byte b : array0) buf.writeByte(b);

        byte[] array1 = this.delta.toByteArray();
        buf.writeInt(array1.length);
        for (byte b : array1) buf.writeByte(b);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);

        byte[] array0 = new byte[buf.readInt()];
        for (int i = 0; i < array0.length; i++) array0[i] = buf.readByte();
        this.power = new BigInteger(array0);

        byte[] array1 = new byte[buf.readInt()];
        for (int i = 0; i < array1.length; i++) array1[i] = buf.readByte();
        this.delta = new BigInteger(array1);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        byte[] pwrArr = nbt.getByteArray("power");
        if (pwrArr.length > 0)
            this.power = new BigInteger(pwrArr);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setByteArray("power", this.power.toByteArray());
        return super.writeToNBT(nbt);
    }

    @Override
    public BlockPos[] getPortPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new BlockPos[]{
                new BlockPos(pos.getX() + dir.offsetX * 2 + rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 2),
                new BlockPos(pos.getX() + dir.offsetX * 2 - rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 2 - rot.offsetZ * 2),
                new BlockPos(pos.getX() - dir.offsetX * 2 + rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2 + rot.offsetZ * 2),
                new BlockPos(pos.getX() - dir.offsetX * 2 - rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2 - rot.offsetZ * 2),
                new BlockPos(pos.getX() + rot.offsetX * 4, pos.getY(), pos.getZ() + rot.offsetZ * 4),
                new BlockPos(pos.getX() - rot.offsetX * 4, pos.getY(), pos.getZ() - rot.offsetZ * 4),
        };
    }

    @Override
    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new DirPos[]{
                new DirPos(pos.getX() + dir.offsetX * 3 + rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 3 + rot.offsetZ * 2, dir),
                new DirPos(pos.getX() + dir.offsetX * 3 - rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 3 - rot.offsetZ * 2, dir),
                new DirPos(pos.getX() - dir.offsetX * 3 + rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 3 + rot.offsetZ * 2, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 3 - rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 3 - rot.offsetZ * 2, dir.getOpposite()),
                new DirPos(pos.getX() + rot.offsetX * 5, pos.getY(), pos.getZ() + rot.offsetZ * 5, rot),
                new DirPos(pos.getX() - rot.offsetX * 5, pos.getY(), pos.getZ() - rot.offsetZ * 5, rot.getOpposite()),
        };
    }

    @Override
    public void usePower(long power) {
        this.power = this.power.subtract(BigInteger.valueOf(power));
    }

    @Override
    public long transferPower(long power, boolean simulate) {
        this.power = this.power.add(BigInteger.valueOf(power));
        return 0L;
    }

    @Override
    public long getPower() {
        return this.power.min(BigInteger.valueOf(getMaxPower() / 2)).longValue();
    } // for provision

    @Override
    public void setPower(long power) {
    } // not needed since we use transferPower and usePower directly

    @Override
    public long getMaxPower() {
        return Long.MAX_VALUE / 100L;
    } // for connection speed

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerBatteryREDD(player.inventory, this);
    }
    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIBatteryREDD(player.inventory, this);
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 4,
                    pos.getY(),
                    pos.getZ() - 4,
                    pos.getX() + 5,
                    pos.getY() + 10,
                    pos.getZ() + 5
            );
        }

        return bb;
    }

    // do some opencomputer stuff
    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getEnergyInfo(Context context, Arguments args) {
        return new Object[] {this.power.doubleValue(), this.delta.longValue()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getInfo(Context context, Arguments args) {
        return new Object[] {this.power.doubleValue(), this.delta.longValue(), redLow, redHigh, getPriority().ordinal()-1};
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public String[] methods() {
        return new String[] {
                "getEnergyInfo",
                "getModeInfo",
                "setModeLow",
                "setModeHigh",
                "setPriority",
                "getInfo"
        };
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        return switch (method) {
            case "getEnergyInfo" -> getEnergyInfo(context, args);
            case "getModeInfo" -> getModeInfo(context, args);
            case "setModeLow" -> setModeLow(context, args);
            case "setModeHigh" -> setModeHigh(context, args);
            case "setPriority" -> setPriority(context, args);
            case "getInfo" -> getInfo(context, args);
            default -> throw new NoSuchMethodException();
        };
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        NBTTagCompound data = new NBTTagCompound();
        data.setByteArray("power", this.power.toByteArray());
        data.setBoolean("muffled", muffled);
        nbt.setTag(NBT_PERSISTENT_KEY, data);
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        NBTTagCompound data = nbt.getCompoundTag(NBT_PERSISTENT_KEY);
        this.power = new BigInteger(data.getByteArray("power"));
        this.muffled = data.getBoolean("muffled");
    }
}
