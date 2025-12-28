package com.hbm.tileentity.machine.fusion;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PlasmaNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
@AutoRegister
public class TileEntityFusionMHDT extends TileEntityLoadedBase implements ITickable, IEnergyProviderMK2, IFluidStandardTransceiverMK2, IFusionPowerReceiver, IConfigurableMachine {

    protected PlasmaNetwork.PlasmaNode plasmaNode;

    public long plasmaEnergy;
    public long plasmaEnergySync;
    public long power;

    public float rotor;
    public float prevRotor;
    public float rotorSpeed;
    public static final float ROTOR_ACCELERATION = 0.125F;

    public static final double PLASMA_EFFICIENCY = 1.35D;
    public static final int COOLANT_USE = 50;
    public static long MINIMUM_PLASMA = 5_000_000L;

    public FluidTankNTM[] tanks;
    private AudioWrapper audio;

    @Override public String getConfigName() { return "mhd-turbine"; }
    @Override public void readIfPresent(JsonObject obj) { MINIMUM_PLASMA = IConfigurableMachine.grab(obj, "L:minimumPlasma", MINIMUM_PLASMA); }
    @Override public void writeConfig(JsonWriter writer) throws IOException { writer.name("L:minimumPlasma").value(MINIMUM_PLASMA); }

    public TileEntityFusionMHDT() {
        this.tanks = new FluidTankNTM[2];
        this.tanks[0] = new FluidTankNTM(Fluids.PERFLUOROMETHYL_COLD, 4_000);
        this.tanks[1] = new FluidTankNTM(Fluids.PERFLUOROMETHYL, 4_000);
    }

    public boolean hasMinimumPlasma() {
        return this.plasmaEnergy >= MINIMUM_PLASMA;
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            this.plasmaEnergySync = this.plasmaEnergy;

            if(isCool()) {
                this.power = (long) Math.floor(this.plasmaEnergy * PLASMA_EFFICIENCY);
                if(!this.hasMinimumPlasma()) this.power /= 2;
                tanks[0].setFill(tanks[0].getFill() - COOLANT_USE);
                tanks[1].setFill(tanks[1].getFill() + COOLANT_USE);
            }

            for(DirPos pos : getConPos()) {
                this.tryProvide(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                if(tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(tanks[0].getTankType(), world, pos);
                if(tanks[1].getFill() > 0) this.tryProvide(tanks[1], world, pos);
            }

            if(plasmaNode == null || plasmaNode.expired) {
                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite();
                plasmaNode = UniNodespace.getNode(world, pos.add(dir.offsetX * 6, 2, dir.offsetZ * 6), PlasmaNetwork.THE_PROVIDER);

                if(plasmaNode == null) {
                    plasmaNode = (PlasmaNetwork.PlasmaNode) new PlasmaNetwork.PlasmaNode(PlasmaNetwork.THE_PROVIDER,
                            new BlockPos(pos.getX() + dir.offsetX * 6, pos.getY() + 2, pos.getZ() + dir.offsetZ * 6))
                            .setConnections(new DirPos(pos.getX() + dir.offsetX * 7, pos.getY() + 2, pos.getZ() + dir.offsetZ * 7, dir));

                    UniNodespace.createNode(world, plasmaNode);
                }
            }

            if(plasmaNode != null && plasmaNode.hasValidNet()) plasmaNode.net.addReceiver(this);

            this.networkPackNT(150);
            this.plasmaEnergy = 0;

        } else {

            if(this.plasmaEnergy > 0 && isCool()) this.rotorSpeed += ROTOR_ACCELERATION;
            else this.rotorSpeed -= ROTOR_ACCELERATION;

            this.rotorSpeed = MathHelper.clamp(this.rotorSpeed, 0F, hasMinimumPlasma() ? 15F : 10F);

            this.prevRotor = this.rotor;
            this.rotor += this.rotorSpeed;

            if(this.rotor >= 360F) {
                this.rotor -= 360F;
                this.prevRotor -= 360F;
            }

            if(this.rotorSpeed > 0 && MainRegistry.proxy.me().getDistanceSq(pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5) < 30 * 30) {

                float speed = this.rotorSpeed / 15F;

                if(audio == null) {
                    audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.largeTurbineRunning, SoundCategory.BLOCKS, pos.getX() + 0.5F, pos.getY() + 1.5F, pos.getZ() + 0.5F, getVolume(speed), 20F, speed, 20);
                    audio.startSound();
                } else {
                    audio.updateVolume(getVolume(speed));
                    audio.updatePitch(speed);
                    audio.keepAlive();
                }

            } else {

                if(audio != null) {
                    if(audio.isPlaying()) audio.stopSound();
                    audio = null;
                }
            }
        }
    }

    public boolean isCool() {
        return tanks[0].getFill() >= COOLANT_USE && tanks[1].getFill() + COOLANT_USE <= tanks[1].getMaxFill();
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(pos.getX() + dir.offsetX * 4 + rot.offsetX * 4, pos.getY(), pos.getZ() + dir.offsetZ * 4 + rot.offsetZ * 4, rot),
                new DirPos(pos.getX() + dir.offsetX * 4 - rot.offsetX * 4, pos.getY(), pos.getZ() + dir.offsetZ * 4 - rot.offsetZ * 4, rot.getOpposite()),
                new DirPos(pos.getX() + dir.offsetX * 8, pos.getY() + 1, pos.getZ() + dir.offsetZ * 8, dir)
        };
    }

    @Override public boolean receivesFusionPower() { return true; }
    @Override public void receiveFusionPower(long fusionPower, double neutronPower) { this.plasmaEnergy = fusionPower; }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(plasmaEnergySync);

        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.plasmaEnergy = buf.readLong();

        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.tanks[0].readFromNBT(nbt, "t0");
        this.tanks[1].readFromNBT(nbt, "t1");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.tanks[0].writeToNBT(nbt, "t0");
        this.tanks[1].writeToNBT(nbt, "t1");
        return super.writeToNBT(nbt);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();

        if(audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if(audio != null) {
            audio.stopSound();
            audio = null;
        }

        if(!world.isRemote) {
            if(this.plasmaNode != null) UniNodespace.destroyNode(world, plasmaNode);
        }
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return power; }

    @Override public FluidTankNTM[] getAllTanks() { return tanks; }
    @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {tanks[0]}; }
    @Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {tanks[1]}; }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 7,
                    pos.getY(),
                    pos.getZ() - 7,
                    pos.getX() + 8,
                    pos.getY() + 4,
                    pos.getZ() + 8
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
