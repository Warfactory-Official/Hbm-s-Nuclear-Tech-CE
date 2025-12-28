package com.hbm.tileentity.machine.fusion;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerFusionKlystron;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIFusionKlystron;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.KlystronNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AutoRegister
public class TileEntityFusionKlystron extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardReceiverMK2, IControlReceiver, IGUIProvider {

    protected KlystronNetwork.KlystronNode klystronNode;
    public static final long MAX_OUTPUT = 1_000_000;
    public static final int AIR_CONSUMPTION = 2_500;
    public long outputTarget;
    public long output;
    public long power;
    public long maxPower;

    public float fan;
    public float prevFan;
    public float fanSpeed;
    public static final float FAN_ACCELERATION = 0.125F;

    public FluidTankNTM compair;

    private AudioWrapper audio;

    public TileEntityFusionKlystron() {
        super(1, true, true);

        compair = new FluidTankNTM(Fluids.AIR, AIR_CONSUMPTION * 60);
    }

    @Override
    public String getDefaultName() {
        return "container.fusionKlystron";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            this.maxPower = Math.max(1_000_000L, this.outputTarget * 100L);

            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

            for(DirPos pos : getConPos()) {
                this.trySubscribe(world, pos);
                this.trySubscribe(compair.getTankType(), world, pos);
            }

            this.output = 0;

            double powerFactor = TileEntityFusionTorus.getSpeedScaled(maxPower, power);
            double airFactor = TileEntityFusionTorus.getSpeedScaled(compair.getMaxFill(), compair.getFill());
            double factor = Math.min(powerFactor, airFactor);

            long powerReq = (long) Math.ceil(outputTarget * factor);
            int airReq = (int) Math.ceil(AIR_CONSUMPTION * factor);

            if(outputTarget > 0 && power >= powerReq && compair.getFill() >= airReq) {
                this.output = powerReq;

                this.power -= powerReq;
                this.compair.setFill(this.compair.getFill() - airReq);
            }

            if(output < outputTarget / 50) output = 0;

            if(klystronNode == null || klystronNode.expired) {
                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite();
                klystronNode = UniNodespace.getNode(world, pos.add(dir.offsetX * 4, 2, dir.offsetZ * 4), KlystronNetwork.THE_PROVIDER);

                if(klystronNode == null) {
                    klystronNode = (KlystronNetwork.KlystronNode) new KlystronNetwork.KlystronNode(KlystronNetwork.THE_PROVIDER,
                            new BlockPos(pos.getX() + dir.offsetX * 4, pos.getY() + 2, pos.getZ() + dir.offsetZ * 4))
                            .setConnections(new DirPos(pos.getX() + dir.offsetX * 5, pos.getY() + 2, pos.getZ() + dir.offsetZ * 5, dir));

                    UniNodespace.createNode(world, klystronNode);
                }
            }

            if(klystronNode.net != null) klystronNode.net.addProvider(this);

            if(klystronNode != null && klystronNode.net != null) {
                KlystronNetwork net = klystronNode.net;

                for(Map.Entry<TileEntity, Long> o : net.receiverEntries.entrySet()) {
                    if(o.getKey() instanceof TileEntityFusionTorus torus) { // replace this with an interface should we ever get more acceptors
                        if(torus.isLoaded() && !torus.isInvalid()) { // check against zombie network members
                            torus.klystronEnergy += this.output;
                            break; // we only do one anyway
                        }
                    }
                }
            }

            this.networkPackNT(100);

        } else {

            double mult = TileEntityFusionTorus.getSpeedScaled(outputTarget, output);
            if(this.output > 0) this.fanSpeed += (float) (FAN_ACCELERATION * mult);
            else this.fanSpeed -= FAN_ACCELERATION;

            this.fanSpeed = MathHelper.clamp(this.fanSpeed, 0F, 5F * (float) mult);

            this.prevFan = this.fan;
            this.fan += this.fanSpeed;

            if(this.fan >= 360F) {
                this.fan -= 360F;
                this.prevFan -= 360F;
            }

            if(this.fanSpeed > 0 && MainRegistry.proxy.me().getDistanceSq(pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5) < 30 * 30) {

                float speed = this.fanSpeed / 5F;

                if(audio == null) {
                    audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.fel, SoundCategory.BLOCKS, pos.getX() + 0.5F, pos.getY() + 2.5F, pos.getZ() + 0.5F, getVolume(speed), 15F, speed, 20);
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

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(pos.getX() + dir.offsetX * 4, pos.getY() + 2, pos.getZ() + dir.offsetZ * 4, dir),
                new DirPos(pos.getX() + rot.offsetX * 3, pos.getY(), pos.getZ() + rot.offsetZ * 3, rot),
                new DirPos(pos.getX() - rot.offsetX * 3, pos.getY(), pos.getZ() - rot.offsetZ * 3, rot.getOpposite())
        };
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

        if(!world.isRemote && this.klystronNode != null) {
            UniNodespace.destroyNode(world, klystronNode);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(power);
        buf.writeLong(maxPower);
        buf.writeLong(outputTarget);
        buf.writeLong(output);
        this.compair.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.outputTarget = buf.readLong();
        this.output = buf.readLong();
        this.compair.deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.power = nbt.getLong("power");
        this.maxPower = nbt.getLong("maxPower");
        this.outputTarget = nbt.getLong("outputTarget");

        this.compair.readFromNBT(nbt, "t");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("power", power);
        nbt.setLong("maxPower", maxPower);
        nbt.setLong("outputTarget", outputTarget);

        this.compair.writeToNBT(nbt, "t");
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 0; // battery
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = power; }
    @Override public long getMaxPower() { return maxPower; }

    @Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {compair}; }
    @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {compair}; }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerFusionKlystron(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIFusionKlystron(player.inventory, this);
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 4,
                    pos.getY(),
                    pos.getZ() - 4,
                    pos.getX() + 5,
                    pos.getY() + 5,
                    pos.getZ() + 5
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
    public boolean hasPermission(EntityPlayer player) {
        return player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5) < 20 * 20;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {

        if(data.hasKey("amount")) {
            this.outputTarget = data.getLong("amount");
            if(this.outputTarget < 0) this.outputTarget = 0;
            if(this.outputTarget > MAX_OUTPUT) this.outputTarget = MAX_OUTPUT;
        }
    }
}
