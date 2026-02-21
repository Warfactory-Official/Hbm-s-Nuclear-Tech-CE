package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.api.tile.IHeatSource;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.TomSaveData;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;

@AutoRegister
public class TileEntityHeatBoiler extends TileEntityLoadedBase implements ITickable, IFluidStandardTransceiver, IBufPacketReceiver, IConfigurableMachine, IFluidCopiable, IPersistentNBT {

    public Fluid[] types = new Fluid[2];
    public FluidTankNTM[] tanks;
    public FluidTankNTM[] tanksSync;
    public int heat;
    public boolean isOn;
    public boolean hasExploded = false;
    public static int maxHeat = 12_800_000;
    public static double diffusion = 0.1D;
    public static boolean canExplode = true;
    private int lastHeat;
    private boolean destroyedByCreativePlayer = false;

    private AudioWrapper audio;
    private int audioTime;

    public TileEntityHeatBoiler() {
        super();
        tanks = new FluidTankNTM[2];
        tanksSync = new FluidTankNTM[2];
        this.tanks[0] = new FluidTankNTM(Fluids.WATER, 16_000);
        this.tanks[1] = new FluidTankNTM(Fluids.STEAM, 16_000 * 100);

        types[0] = FluidRegistry.WATER;
        types[1] = Fluids.STEAM.getFF();
    }

    @Override
    public void update() {
        if(!world.isRemote) {
            if(!this.hasExploded) {
                setupTanks();
                updateConnections();
                tryPullHeat();
                lastHeat = this.heat;

                int light = this.world.getLightFor(EnumSkyBlock.SKY, this.pos);
                if(light > 7 && TomSaveData.forWorld(world).fire > 1e-5) {
                    this.heat += ((maxHeat - heat) * 0.000005D);
                }
                tanksSync[0] = tanks[0].clone();
                this.isOn = false;
                this.tryConvert();
                tanksSync[1] = tanks[1].clone();

                if(this.tanks[1].getFill() > 0) {
                    for(DirPos pos : getConPos()) {
                        this.sendFluid(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir().getOpposite());
                    }
                }
            }
            networkPackNT(25);
        } else {
            if(this.isOn) audioTime = 20;

            if(audioTime > 0) {
                audioTime--;
                if(audio == null) {
                    audio = createAudioLoop();
                    audio.startSound();
                } else if(!audio.isPlaying()) {
                    audio = rebootAudio(audio);
                }
                audio.updateVolume(getVolume(1F));
                audio.keepAlive();
            } else {
                if(audio != null) {
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

    private void updateConnections() {
        for(DirPos pos : getConPos()) {
            this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    protected DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);
        return new DirPos[] {
                new DirPos(pos.getX() + dir.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 2, dir),
                new DirPos(pos.getX() - dir.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2, dir.getOpposite()),
                new DirPos(pos.getX(), pos.getY() + 4, pos.getZ(), Library.POS_Y)
        };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tanks[0].readFromNBT(nbt, "water");
        tanks[1].readFromNBT(nbt, "steam");
        heat = nbt.getInteger("heat");
        hasExploded = nbt.getBoolean("exploded");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        tanks[0].writeToNBT(nbt, "water");
        tanks[1].writeToNBT(nbt, "steam");
        nbt.setInteger("heat", heat);
        nbt.setBoolean("exploded", hasExploded);
        return nbt;
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        NBTTagCompound data = new NBTTagCompound();
        tanks[0].writeToNBT(data, "water");
        tanks[1].writeToNBT(data, "steam");
        data.setInteger("heat", heat);
        data.setBoolean("exploded", hasExploded);
        nbt.setTag(NBT_PERSISTENT_KEY, data);
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        if (!nbt.hasKey(NBT_PERSISTENT_KEY)) return;
        NBTTagCompound data = nbt.getCompoundTag(NBT_PERSISTENT_KEY);
        tanks[0].readFromNBT(data, "water");
        tanks[1].readFromNBT(data, "steam");
        if (data.hasKey("heat")) {
            heat = data.getInteger("heat");
        }
        if (data.hasKey("exploded")) {
            hasExploded = data.getBoolean("exploded");
        }
    }

    @Override
    public boolean shouldDrop() {
        return !destroyedByCreativePlayer && !hasExploded;
    }

    @Override
    public void setDestroyedByCreativePlayer() {
        this.destroyedByCreativePlayer = true;
    }

    @Override
    public boolean isDestroyedByCreativePlayer() {
        return this.destroyedByCreativePlayer;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(hasExploded);
        if(!this.hasExploded) {
            buf.writeInt(lastHeat);
            tanksSync[0].serialize(buf);
            tanksSync[1].serialize(buf);
            buf.writeBoolean(this.muffled);
            buf.writeBoolean(this.isOn);
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.hasExploded = buf.readBoolean();
        if (!this.hasExploded) {
            this.heat = buf.readInt();
            this.tanks[0].deserialize(buf);
            this.tanks[1].deserialize(buf);
            this.muffled = buf.readBoolean();
            this.isOn = buf.readBoolean();
        }
    }

    protected void setupTanks() {
        if(tanks[0].getTankType().hasTrait(FT_Heatable.class)) {
            FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
            if(trait.getEfficiency(FT_Heatable.HeatingType.BOILER) > 0) {
                FT_Heatable.HeatingStep entry = trait.getFirstStep();
                tanks[1].setTankType(entry.typeProduced);
                tanks[1].changeTankSize(tanks[0].getMaxFill() * entry.amountProduced / entry.amountReq);
                return;
            }
        }
        tanks[0].setTankType(Fluids.NONE);
        tanks[1].setTankType(Fluids.NONE);
    }

    protected void tryConvert() {
        if(tanks[0].getTankType().hasTrait(FT_Heatable.class)) {
            FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
            if(trait.getEfficiency(FT_Heatable.HeatingType.BOILER) > 0) {

                FT_Heatable.HeatingStep entry = trait.getFirstStep();
                int inputOps = this.tanks[0].getFill() / entry.amountReq;
                int outputOps = (this.tanks[1].getMaxFill() - this.tanks[1].getFill()) / entry.amountProduced;
                int heatOps = this.heat / entry.heatReq;

                int ops = Math.min(inputOps, Math.min(outputOps, heatOps));

                this.tanks[0].setFill(this.tanks[0].getFill() - entry.amountReq * ops);
                this.tanks[1].setFill(this.tanks[1].getFill() + entry.amountProduced * ops);
                this.heat -= entry.heatReq * ops;

                if(ops > 0 && world.rand.nextInt(400) == 0) {
                    world.playSound(null, pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, new SoundEvent(new ResourceLocation("hbm:block.boilerGroan")), SoundCategory.BLOCKS, 0.5F, 1.0F);
                }

                if(ops > 0) {
                    this.isOn = true;
                }

                if (outputOps == 0 && canExplode) {
                    this.hasExploded = true;
                    BlockDummyable.safeRem = true;

                    BlockPos base = this.pos;
                    for (int x = base.getX() - 1; x <= base.getX() + 1; x++) {
                        for (int y = base.getY() + 2; y <= base.getY() + 3; y++) {
                            for (int z = base.getZ() - 1; z <= base.getZ() + 1; z++) {
                                this.world.setBlockToAir(new BlockPos(x, y, z));
                            }
                        }
                    }
                    this.world.setBlockToAir(this.pos.up());

                    ExplosionVNT xnt = new ExplosionVNT(this.world, base.getX() + 0.5D, base.getY() + 2D, base.getZ() + 0.5D, 5F);
                    xnt.setEntityProcessor(new EntityProcessorStandard().withRangeMod(3F));
                    xnt.setPlayerProcessor(new PlayerProcessorStandard());
                    xnt.setSFX(new ExplosionEffectStandard());
                    xnt.explode();

                    BlockDummyable.safeRem = false;
                }
            }
        }
    }

    protected void tryPullHeat() {
        if(this.heat >= TileEntityHeatBoiler.maxHeat) return;
        BlockPos blockBelow = pos.down();
        TileEntity con = world.getTileEntity(blockBelow);

        if(con instanceof IHeatSource source) {
            int diff = source.getHeatStored() - this.heat;
            if(diff == 0) return;

            if(diff > 0) {
                diff = (int) Math.ceil(diff * diffusion);
                source.useUpHeat(diff);
                this.heat += diff;
                if(this.heat > this.maxHeat)
                    this.heat = this.maxHeat;
                return;
            }
        }
        this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 4,
                    pos.getZ() + 2
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
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[] {tanks[1]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[] {tanks[0]};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public String getConfigName() {
        return "boiler";
    }

    @Override
    public void readIfPresent(JsonObject obj) {
        maxHeat = IConfigurableMachine.grab(obj, "I:maxHeat", maxHeat);
        diffusion = IConfigurableMachine.grab(obj, "D:diffusion", diffusion);
        canExplode = IConfigurableMachine.grab(obj, "B:canExplode", canExplode);
    }

    @Override
    public void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:maxHeat").value(maxHeat);
        writer.name("D:diffusion").value(diffusion);
        writer.name("B:canExplode").value(canExplode);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
                    new NTMFluidHandlerWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }
}
