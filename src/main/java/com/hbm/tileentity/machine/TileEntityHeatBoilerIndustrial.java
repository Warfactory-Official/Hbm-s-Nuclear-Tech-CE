package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.api.tile.IHeatSource;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.lib.DirPos;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.TomSaveData;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@AutoRegister
public class TileEntityHeatBoilerIndustrial extends TileEntityLoadedBase implements IBufPacketReceiver, ITickable, IFluidStandardTransceiver,
        IConfigurableMachine, IFluidCopiable {

    /* CONFIGURABLE */
    public static int maxHeat = 12_800_000;
    public static double diffusion = 0.1D;
    public int heat;
    private int lastHeat;
    public FluidTankNTM[] tanks;
    private FluidTankNTM[] tanksSync;
    public boolean isOn;
    AxisAlignedBB bb = null;
    private AudioWrapper audio;
    private int audioTime;

    public TileEntityHeatBoilerIndustrial() {
        this.tanks = new FluidTankNTM[2];
        this.tanksSync = new FluidTankNTM[2];
        this.tanks[0] = new FluidTankNTM(Fluids.WATER, 64_000);
        this.tanks[1] = new FluidTankNTM(Fluids.STEAM, 64_000 * 100);
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            this.setupTanks();
            this.updateConnections();
            this.tryPullHeat();
            lastHeat = this.heat;

            int light = this.world.getLightFor(EnumSkyBlock.SKY, pos);
            if (light > 7 && TomSaveData.forWorld(world).fire > 1e-5) {
                this.heat += (int) ((maxHeat - heat) * 0.000005D); // constantly heat up 0.0005% of the remaining heat buffer for
                // rampant but diminishing heating
            }

            tanksSync[0] = tanks[0].clone();
            this.isOn = false;
            this.tryConvert();
            tanksSync[1] = tanks[1].clone();

            if (this.tanks[1].getFill() > 0) {
                this.sendFluid();
            }

            networkPackNT(25);

        } else {

            if (this.isOn) audioTime = 20;

            if (audioTime > 0) {

                audioTime--;

                if (audio == null) {
                    audio = createAudioLoop();
                    audio.startSound();
                } else if (!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }

                audio.updateVolume(getVolume(1F));
                audio.keepAlive();

            } else {

                if (audio != null) {
                    audio.stopSound();
                    audio = null;
                }
            }
        }
    }

    @Override
    public AudioWrapper createAudioLoop() {
        return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.boiler, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.125F, 10F, 1.0F, 20);
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
        buf.writeInt(this.lastHeat);
        this.tanksSync[0].serialize(buf);
        this.tanksSync[1].serialize(buf);
        buf.writeBoolean(this.isOn);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.heat = buf.readInt();
        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
        this.isOn = buf.readBoolean();
    }

    protected void tryPullHeat() {
        TileEntity con = world.getTileEntity(pos.down(1));

        if (con instanceof IHeatSource source) {
            int diff = source.getHeatStored() - this.heat;

            if (diff == 0) {
                return;
            }

            if (diff > 0) {
                diff = (int) Math.ceil(diff * diffusion);
                diff = Math.min(diff, maxHeat - this.heat);
                source.useUpHeat(diff);
                this.heat += diff;
                if (this.heat > maxHeat) this.heat = maxHeat;
                return;
            }
        }

        this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
    }

    protected void setupTanks() {

        if (tanks[0].getTankType().hasTrait(FT_Heatable.class)) {
            FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
            if (trait.getEfficiency(HeatingType.BOILER) > 0) {
                HeatingStep entry = trait.getFirstStep();
                tanks[1].setTankType(entry.typeProduced);
                tanks[1].changeTankSize(tanks[0].getMaxFill() * entry.amountProduced / entry.amountReq);
                return;
            }
        }

        tanks[0].setTankType(Fluids.NONE);
        tanks[1].setTankType(Fluids.NONE);
    }

    protected void tryConvert() {

        if (tanks[0].getTankType().hasTrait(FT_Heatable.class)) {
            FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
            if (trait.getEfficiency(FT_Heatable.HeatingType.BOILER) > 0) {

                HeatingStep entry = trait.getFirstStep();
                int inputOps = this.tanks[0].getFill() / entry.amountReq;
                int outputOps = (this.tanks[1].getMaxFill() - this.tanks[1].getFill()) / entry.amountProduced;
                int heatOps = this.heat / entry.heatReq;

                int ops = Math.min(inputOps, Math.min(outputOps, heatOps));

                this.tanks[0].setFill(this.tanks[0].getFill() - entry.amountReq * ops);
                this.tanks[1].setFill(this.tanks[1].getFill() + entry.amountProduced * ops);
                this.heat -= entry.heatReq * ops;

                if (ops > 0 && world.rand.nextInt(400) == 0) {
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5,
                            HBMSoundHandler.boilerGroanSounds[world.rand.nextInt(3)], SoundCategory.BLOCKS, 10F, 1.0F);
                }

                if (ops > 0) {
                    this.isOn = true;
                }
            }
        }
    }

    private void updateConnections() {

        for (DirPos dirPos : getConPos()) {
            this.trySubscribe(tanks[0].getTankType(), world, dirPos.getPos().getX(), dirPos.getPos().getY(), dirPos.getPos().getZ(), dirPos.getDir());
        }
    }

    private void sendFluid() {

        for (DirPos dirPos : getConPos()) {
            this.sendFluid(tanks[1], world, dirPos.getPos().getX(), dirPos.getPos().getY(), dirPos.getPos().getZ(), dirPos.getDir().getOpposite());
        }
    }

    private DirPos[] getConPos() {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        return new DirPos[]{new DirPos(x + 2, y, z, Library.POS_X), new DirPos(x - 2, y, z, Library.NEG_X), new DirPos(x, y, z + 2, Library.POS_Z),
                new DirPos(x, y, z - 2, Library.NEG_Z), new DirPos(x, y + 5, z, Library.POS_Y),};
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tanks[0].readFromNBT(nbt, "water");
        tanks[1].readFromNBT(nbt, "steam");
        heat = nbt.getInteger("heat");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        tanks[0].writeToNBT(nbt, "water");
        tanks[1].writeToNBT(nbt, "steam");
        nbt.setInteger("heat", heat);
        return nbt;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tanks[1]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanks[0]};
    }

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 5, pos.getZ() + 2);
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
        return "boilerIndustrial";
    }

    @Override
    public void readIfPresent(JsonObject obj) {
        maxHeat = IConfigurableMachine.grab(obj, "I:maxHeat", maxHeat);
        diffusion = IConfigurableMachine.grab(obj, "D:diffusion", diffusion);
    }

    @Override
    public void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:maxHeat").value(maxHeat);
        writer.name("D:diffusion").value(diffusion);
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new NTMFluidHandlerWrapper(this));
        }
        return super.getCapability(capability, facing);
    }
}
