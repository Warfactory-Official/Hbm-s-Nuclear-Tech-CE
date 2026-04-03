package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.BufPacket;
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
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;

@AutoRegister
public class TileEntityMachineSteamEngine extends TileEntityLoadedBase
    implements ITickable,
        IEnergyProviderMK2,
        IFluidStandardTransceiver,
        IFluidCopiable,
        IBufPacketReceiver,
        IConfigurableMachine {
  private AxisAlignedBB bb;
  public long powerBuffer;

  public float rotor;
  public float lastRotor;
  private float syncRotor;
  public FluidTankNTM[] tanks;

  private int turnProgress;
  private float acceleration = 0F;

  // Configurable values
  private static int steamCap = 2_000;
  private static int ldsCap = 20;
  private static double efficiency = 0.85D;

  public TileEntityMachineSteamEngine() {
    super();

    tanks = new FluidTankNTM[2];
    tanks[0] = new FluidTankNTM(Fluids.STEAM, steamCap);
    tanks[1] = new FluidTankNTM(Fluids.SPENTSTEAM, ldsCap);
  }

  @Override
  public String getConfigName() {
    return "steam_engine";
  }

  @Override
  public void readIfPresent(JsonObject obj) {
    steamCap = IConfigurableMachine.grab(obj, "I:steamCap", steamCap);
    ldsCap = IConfigurableMachine.grab(obj, "I:ldsCap", ldsCap);
    efficiency = IConfigurableMachine.grab(obj, "D:efficiency", efficiency);
  }

  @Override
  public void writeConfig(JsonWriter writer) throws IOException {
    writer.name("I:steamCap").value(steamCap);
    writer.name("I:ldsCap").value(ldsCap);
    writer.name("D:efficiency").value(efficiency);
  }

  ByteBuf buf;

  @Override
  public void update() {

    if (!world.isRemote) {

      if (this.buf != null) this.buf.release();
      this.buf = Unpooled.buffer();

      this.powerBuffer = 0;

      tanks[0].setTankType(Fluids.STEAM);
      tanks[1].setTankType(Fluids.SPENTSTEAM);

      tanks[0].serialize(buf);

      FT_Coolable trait = tanks[0].getTankType().getTrait(FT_Coolable.class);
      double eff = trait.getEfficiency(FT_Coolable.CoolingType.TURBINE) * efficiency;

      int inputOps = tanks[0].getFill() / trait.amountReq;
      int outputOps = (tanks[1].getMaxFill() - tanks[1].getFill()) / trait.amountProduced;
      int ops = Math.min(inputOps, outputOps);
      tanks[0].setFill(tanks[0].getFill() - ops * trait.amountReq);
      tanks[1].setFill(tanks[1].getFill() + ops * trait.amountProduced);
      this.powerBuffer += (long) (ops * trait.heatEnergy * eff);

      if (ops > 0) {
        this.acceleration += 0.1F;
      } else {
        this.acceleration -= 0.1F;
      }

      this.acceleration = MathHelper.clamp(this.acceleration, 0F, 40F);
      this.rotor += this.acceleration;

      if (this.rotor >= 360F) {
        this.rotor -= 360F;

        this.world.playSound(
            null,
            this.pos,
            HBMSoundHandler.steamEngineOperate,
            SoundCategory.BLOCKS,
            1F,
            0.5F + (acceleration / 80F));
      }

      buf.writeLong(this.powerBuffer);
      buf.writeFloat(this.rotor);
      tanks[1].serialize(buf);

      for (DirPos dirPos : getConPos()) {
        if (this.powerBuffer > 0)
          this.tryProvide(
              world,
              dirPos.getPos().getX(),
              dirPos.getPos().getY(),
              dirPos.getPos().getZ(),
              dirPos.getDir());
        this.trySubscribe(
            tanks[0].getTankType(),
            world,
            dirPos.getPos().getX(),
            dirPos.getPos().getY(),
            dirPos.getPos().getZ(),
            dirPos.getDir());
        this.sendFluid(
            tanks[1],
            world,
            dirPos.getPos().getX(),
            dirPos.getPos().getY(),
            dirPos.getPos().getZ(),
            dirPos.getDir());
      }

      NBTTagCompound data = new NBTTagCompound();
      data.setLong("powerBuffer", powerBuffer);
      data.setFloat("acceleration", acceleration);
      tanks[0].writeToNBT(data, "s");
      tanks[1].writeToNBT(data, "w");

      PacketThreading.createAllAroundThreadedPacket(
          new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this),
          new NetworkRegistry.TargetPoint(
              this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 150));
    } else {
      this.lastRotor = this.rotor;

      if (this.turnProgress > 0) {
        double d = MathHelper.wrapDegrees(this.syncRotor - (double) this.rotor);
        this.rotor = (float) ((double) this.rotor + d / (double) this.turnProgress);
        --this.turnProgress;
      } else {
        this.rotor = this.syncRotor;
      }
    }
  }

  protected DirPos[] getConPos() {
    ForgeDirection dir =
        ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
    ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

    float x = this.pos.getX();
    float y = this.pos.getY();
    float z = this.pos.getZ();

    return new DirPos[] {
      new DirPos(x + rot.offsetX * 2, y + 1, z + rot.offsetZ * 2, rot),
      new DirPos(x + rot.offsetX * 2 + dir.offsetX, y + 1, z + rot.offsetZ * 2 + dir.offsetZ, rot),
      new DirPos(x + rot.offsetX * 2 - dir.offsetX, y + 1, z + rot.offsetZ * 2 - dir.offsetZ, rot)
    };
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);

    this.powerBuffer = nbt.getLong("powerBuffer");
    this.acceleration = nbt.getFloat("acceleration");
    this.tanks[0].readFromNBT(nbt, "s");
    this.tanks[1].readFromNBT(nbt, "w");
  }

  @Override
  public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setLong("powerBuffer", powerBuffer);
    nbt.setFloat("acceleration", acceleration);
    tanks[0].writeToNBT(nbt, "s");
    tanks[1].writeToNBT(nbt, "w");
    return nbt;
  }

  @Override
  public @NotNull AxisAlignedBB getRenderBoundingBox() {
    if (bb == null) bb = new AxisAlignedBB(pos.getX() - 5, pos.getY(), pos.getZ() - 5, pos.getX() + 6, pos.getY() + 2, pos.getZ() + 6);
    return bb;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public double getMaxRenderDistanceSquared() {
    return 65536.0D;
  }

  @Override
  public boolean canConnect(ForgeDirection dir) {
    return dir != ForgeDirection.UP && dir != ForgeDirection.DOWN && dir != ForgeDirection.UNKNOWN;
  }

  @Override
  public long getPower() {
    return powerBuffer;
  }

  @Override
  public long getMaxPower() {
    return powerBuffer;
  }

  @Override
  public void setPower(long power) {
    this.powerBuffer = power;
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
  public void serialize(ByteBuf buf) {
    buf.writeBytes(this.buf);
  }

  @Override
  public void deserialize(ByteBuf buf) {
    this.tanks[0].deserialize(buf);
    this.powerBuffer = buf.readLong();
    this.syncRotor = buf.readFloat();
    this.tanks[1].deserialize(buf);
    this.turnProgress = 3; // use 3-ply for extra smoothness
  }

  @Override
  public FluidTankNTM getTankToPaste() {
    return null;
  }

  @Override
  public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY) {
      return true;
    }
    return super.hasCapability(capability, facing);
  }

  @Override
  public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
      return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
              new NTMFluidHandlerWrapper(this)
      );
    }
    if (capability == CapabilityEnergy.ENERGY) {
      return CapabilityEnergy.ENERGY.cast(
              new NTMEnergyCapabilityWrapper(this)
      );
    }
    return super.getCapability(capability, facing);
  }
}
