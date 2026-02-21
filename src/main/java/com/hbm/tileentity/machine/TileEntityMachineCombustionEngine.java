package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerCombustionEngine;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.gui.GUICombustionEngine;
import com.hbm.inventory.gui.GuiInfoContainer;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemPistons;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@Optional.InterfaceList({
@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
})
@AutoRegister
public class TileEntityMachineCombustionEngine extends TileEntityMachinePolluting
    implements ITickable,
        IEnergyProviderMK2,
        IFluidStandardTransceiver,
        IControlReceiver,
        IGUIProvider,
        SimpleComponent,
        CompatHandler.OCComponent,
        IFluidCopiable {

  public boolean isOn = false;
  public static long maxPower = 2_500_000;
  public long power;
  private int playersUsing = 0;
  public int setting = 0;
  public boolean wasOn = false;

  public float doorAngle = 0;
  public float prevDoorAngle = 0;

  private AudioWrapper audio;

  public FluidTankNTM tank;
  public int tenth = 0;

  public TileEntityMachineCombustionEngine() {
    super(5, 50, true, true);
    this.tank = new FluidTankNTM(Fluids.DIESEL, 24_000);
  }

  @Override
  public String getDefaultName() {
    return "container.combustionEngine";
  }

  @Override
  public void update() {

    if (!world.isRemote) {

      this.tank.loadTank(0, 1, inventory);
      if (this.tank.setType(4, inventory)) {
        this.tenth = 0;
      }

      wasOn = false;

      int fill = tank.getFill() * 10 + tenth;
      ItemStack stack = inventory.getStackInSlot(2);

      if (isOn
          && setting > 0
          && !stack.isEmpty()
          && stack.getItem() == ModItems.piston_set
          && fill > 0
          && tank.getTankType().hasTrait(FT_Combustible.class)) {
        ItemPistons.EnumPistonType piston =
            EnumUtil.grabEnumSafely(
                ItemPistons.EnumPistonType.VALUES, inventory.getStackInSlot(2).getItemDamage());
        FT_Combustible trait = tank.getTankType().getTrait(FT_Combustible.class);

        double eff = piston.eff[trait.getGrade().ordinal()];

        if (eff > 0) {
          int speed = setting * 2;

          int toBurn = Math.min(fill, speed);
          this.power += (long) (toBurn * (trait.getCombustionEnergy() / 10_000D) * eff);
          fill -= toBurn;

          if (world.getTotalWorldTime() % 5 == 0 && toBurn > 0) {
            super.pollute(tank.getTankType(), FluidTrait.FluidReleaseType.BURN, toBurn * 0.5F);
          }

          if (toBurn > 0) {
            wasOn = true;
          }

          tank.setFill(fill / 10);
          tenth = fill % 10;
        }
      }

      NBTTagCompound data = new NBTTagCompound();
      data.setLong("power", Math.min(power, maxPower));

      this.power = Library.chargeItemsFromTE(inventory, 3, power, power);

      for (DirPos dirPos : getConPos()) {
        this.tryProvide(
            world,
            dirPos.getPos().getX(),
            dirPos.getPos().getY(),
            dirPos.getPos().getZ(),
            dirPos.getDir());
        this.trySubscribe(
            tank.getTankType(),
            world,
            dirPos.getPos().getX(),
            dirPos.getPos().getY(),
            dirPos.getPos().getZ(),
            dirPos.getDir());
        this.sendSmoke(
            dirPos.getPos().getX(),
            dirPos.getPos().getY(),
            dirPos.getPos().getZ(),
            dirPos.getDir());
      }

      if (power > maxPower) power = maxPower;

      this.networkPackNT(50);

    } else {
      this.prevDoorAngle = this.doorAngle;
      float swingSpeed = (doorAngle / 10F) + 3;

      if (this.playersUsing > 0) {
        this.doorAngle += swingSpeed;
      } else {
        this.doorAngle -= swingSpeed;
      }

      this.doorAngle = MathHelper.clamp(this.doorAngle, 0F, 135F);

      if (wasOn) {

        if (audio == null) {
          audio = createAudioLoop();
          audio.startSound();
        } else if (!audio.isPlaying()) {
          audio = rebootAudio(audio);
        }

        audio.keepAlive();
        audio.updateVolume(this.getVolume(1F));

      } else {

        if (audio != null) {
          audio.stopSound();
          audio = null;
        }
      }
    }
  }

  private DirPos[] getConPos() {
    ForgeDirection dir =
        ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
    ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

    return new DirPos[] {
      new DirPos(
          pos.getX() + dir.offsetX + rot.offsetX,
          pos.getY(),
          pos.getZ() + dir.offsetZ + rot.offsetZ,
          dir),
      new DirPos(
          pos.getX() + dir.offsetX - rot.offsetX,
          pos.getY(),
          pos.getZ() + dir.offsetZ - rot.offsetZ,
          dir),
      new DirPos(
          pos.getX() - dir.offsetX * 2 + rot.offsetX,
          pos.getY(),
          pos.getZ() - dir.offsetZ * 2 + rot.offsetZ,
          dir.getOpposite()),
      new DirPos(
          pos.getX() - dir.offsetX * 2 - rot.offsetX,
          pos.getY(),
          pos.getZ() - dir.offsetZ * 2 - rot.offsetZ,
          dir.getOpposite())
    };
  }

  @Override
  public AudioWrapper createAudioLoop() {
    return MainRegistry.proxy.getLoopedSound(
        HBMSoundHandler.iGeneratorOperate,
        SoundCategory.BLOCKS,
        pos.getX(),
        pos.getY(),
        pos.getZ(),
        1.0F, 10F,
        1.0F, 20);
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
  public boolean canConnect(ForgeDirection dir) {
    return dir != ForgeDirection.DOWN;
  }

  @Override
  public boolean canConnect(FluidType type, ForgeDirection dir) {
    return dir != ForgeDirection.DOWN;
  }

  @Override
  public void serialize(ByteBuf buf) {
    super.serialize(buf);
    buf.writeInt(this.playersUsing);
    buf.writeInt(this.setting);
    buf.writeLong(this.power);
    buf.writeBoolean(this.isOn);
    buf.writeBoolean(this.wasOn);
    tank.serialize(buf);
  }

  @Override
  public void deserialize(ByteBuf buf) {
    super.deserialize(buf);
    this.playersUsing = buf.readInt();
    this.setting = buf.readInt();
    this.power = buf.readLong();
    this.isOn = buf.readBoolean();
    this.wasOn = buf.readBoolean();
    tank.deserialize(buf);
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.setting = nbt.getInteger("setting");
    this.power = nbt.getLong("power");
    this.isOn = nbt.getBoolean("isOn");
    this.tank.readFromNBT(nbt, "tank");
    this.tenth = nbt.getInteger("tenth");
  }

  @Override
  public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setInteger("setting", setting);
    nbt.setLong("power", power);
    nbt.setBoolean("isOn", isOn);
    tank.writeToNBT(nbt, "tank");
    nbt.setInteger("tenth", tenth);
    return nbt;
  }

  @Override
  public void setPower(long power) {
    this.power = power;
  }

  @Override
  public long getPower() {
    return power;
  }

  @Override
  public long getMaxPower() {
    return maxPower;
  }

  @Override
  public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new ContainerCombustionEngine(player.inventory, this);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public GuiInfoContainer provideGUI(
      int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new GUICombustionEngine(player.inventory, this);
  }

  @Override
  public FluidTankNTM[] getAllTanks() {
    return new FluidTankNTM[] {tank};
  }

  @Override
  public FluidTankNTM[] getReceivingTanks() {
    return new FluidTankNTM[] {tank};
  }

  @Override
  public FluidTankNTM[] getSendingTanks() {
    return this.getSmokeTanks();
  }

  AxisAlignedBB bb = null;

  @Override
  public @NotNull AxisAlignedBB getRenderBoundingBox() {

    if (bb == null) {
      bb =
          new AxisAlignedBB(
              pos.getX() - 3,
              pos.getY(),
              pos.getZ() - 3,
              pos.getX() + 4,
              pos.getY() + 2,
              pos.getZ() + 4);
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
    return player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 25;
  }

  @Override
  public void receiveControl(NBTTagCompound data) {
    if (data.hasKey("turnOn")) this.isOn = !this.isOn;
    if (data.hasKey("setting")) this.setting = data.getInteger("setting");

    this.markDirty();
  }

  @Override
  public NBTTagCompound getSettings(World world, int x, int y, int z) {
    NBTTagCompound tag = new NBTTagCompound();
    tag.setIntArray("fluidID", new int[] {tank.getTankType().getID()});
    tag.setBoolean("isOn", isOn);
    tag.setInteger("burnRate", setting);
    return tag;
  }

  @Override
  public void pasteSettings(
      NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
    int id = nbt.getIntArray("fluidID")[index];
    tank.setTankType(Fluids.fromID(id));
    if (nbt.hasKey("isOn")) isOn = nbt.getBoolean("isOn");
    if (nbt.hasKey("burnRate")) setting = nbt.getInteger("burnRate");
  }

  @Override
  @Optional.Method(modid = "opencomputers")
  public String getComponentName() {
    return "ntm_combustion_engine";
  }

  @Callback(direct = true)
  @Optional.Method(modid = "opencomputers")
  public Object[] getFluid(Context context, Arguments args) {
    return new Object[] {tank.getFill(), tank.getMaxFill()};
  }

  @Callback(direct = true)
  @Optional.Method(modid = "opencomputers")
  public Object[] getType(Context context, Arguments args) {
    return new Object[] {tank.getTankType().getName()};
  }

  @Callback(direct = true)
  @Optional.Method(modid = "opencomputers")
  public Object[] getPower(Context context, Arguments args) {
    return new Object[] {power};
  }

  @Callback(direct = true)
  @Optional.Method(modid = "opencomputers")
  public Object[] getThrottle(Context context, Arguments args) {
    return new Object[] {setting};
  }

  @Callback(direct = true)
  @Optional.Method(modid = "opencomputers")
  public Object[] getState(Context context, Arguments args) {
    return new Object[] {isOn};
  }

  @Callback(direct = true)
  @Optional.Method(modid = "opencomputers")
  public Object[] getEfficiency(Context context, Arguments args) {
    ItemPistons.EnumPistonType piston =
        EnumUtil.grabEnumSafely(
            ItemPistons.EnumPistonType.VALUES, inventory.getStackInSlot(2).getItemDamage());
    FT_Combustible trait = tank.getTankType().getTrait(FT_Combustible.class);
    double eff = piston.eff[trait.getGrade().ordinal()];
    return new Object[] {eff};
  }

  @Callback(direct = true, limit = 4)
  @Optional.Method(modid = "opencomputers")
  public Object[] setThrottle(Context context, Arguments args) {
    int throttleRequest = args.checkInteger(0);
    if ((throttleRequest < 0)
        || (throttleRequest
            > 30)) { // return false without doing anything if number is outside normal
      return new Object[] {false, "Throttle request outside of range 0-30"};
    }
    ;
    setting = throttleRequest;
    return new Object[] {true};
  }

  @Callback(direct = true, limit = 4)
  @Optional.Method(modid = "opencomputers")
  public Object[] start(Context context, Arguments args) {
    isOn = true;
    return new Object[] {};
  }

  @Callback(direct = true, limit = 4)
  @Optional.Method(modid = "opencomputers")
  public Object[] stop(Context context, Arguments args) {
    isOn = false;
    return new Object[] {};
  }

  @Callback(direct = true)
  @Optional.Method(modid = "opencomputers")
  public Object[] getInfo(Context context, Arguments args) {
    ItemPistons.EnumPistonType piston =
        EnumUtil.grabEnumSafely(
            ItemPistons.EnumPistonType.VALUES, inventory.getStackInSlot(2).getItemDamage());
    FT_Combustible trait = tank.getTankType().getTrait(FT_Combustible.class);
    double eff = piston.eff[trait.getGrade().ordinal()];
    return new Object[] {
      setting, isOn, power, eff, tank.getFill(), tank.getMaxFill(), tank.getTankType().getName()
    };
  }

  @Override
  @Optional.Method(modid = "opencomputers")
  public String[] methods() {
    return new String[] {
      "getFluid",
      "getType",
      "getPower",
      "getThrottle",
      "getState",
      "getEfficiency",
      "setThrottle",
      "start",
      "stop",
      "getInfo"
    };
  }

  @Override
  @Optional.Method(modid = "opencomputers")
  public Object[] invoke(String method, Context context, Arguments args) throws Exception {
    return switch (method) {
      case ("getFluid") -> getFluid(context, args);
      case ("getType") -> getType(context, args);
      case ("getPower") -> getPower(context, args);
      case ("getThrottle") -> getThrottle(context, args);
      case ("getState") -> getState(context, args);
      case ("getEfficiency") -> getEfficiency(context, args);
      case ("setThrottle") -> setThrottle(context, args);
      case ("start") -> start(context, args);
      case ("stop") -> stop(context, args);
      case ("getInfo") -> getInfo(context, args);
      default -> throw new NoSuchMethodException();
    };
  }
}
