package com.hbm.tileentity.machine;

import com.hbm.api.block.ICrucibleAcceptor;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineStrandCaster;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineStrandCaster;
import com.hbm.inventory.gui.GuiInfoContainer;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMold;
import com.hbm.items.machine.ItemScraps;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegister
public class TileEntityMachineStrandCaster extends TileEntityFoundryCastingBase implements IGUIProvider, ICrucibleAcceptor, IFluidStandardTransceiver {

  public FluidTankNTM water;
  public FluidTankNTM steam;
  private long lastProgressTick = 0;

  private static final int invSize = 7;

  public @NotNull String getName() {
    return "container.machineStrandCaster";
  }

  public TileEntityMachineStrandCaster() {
    super(invSize);
    water = new FluidTankNTM(Fluids.WATER, 64_000);
    steam = new FluidTankNTM(Fluids.SPENTSTEAM, 64_000);
  }

  @Override
  public NBTTagCompound getUpdateTag() {
    return this.writeToNBT(super.getUpdateTag());
  }

  @Override
  public SPacketUpdateTileEntity getUpdatePacket() {
    return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
  }

  @Override
  public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    this.readFromNBT(pkt.getNbtCompound());
  }

  @Override
  public void update() {

    if (!world.isRemote) {

      if (this.lastType != this.type || this.lastAmount != this.amount) {
        IBlockState state = world.getBlockState(pos);
        world.markAndNotifyBlock(pos, world.getChunk(pos), state, state, 3);
        this.lastType = this.type;
        this.lastAmount = this.amount;
      }

      // In case of overfill problems, spit out the excess as scrap
      if (amount > getCapacity()) {
        ItemStack scrap = ItemScraps.create(new Mats.MaterialStack(type, Math.max(amount - getCapacity(), 0)));
        EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, scrap);
        world.spawnEntity(item);
        this.amount = this.getCapacity();
      }

      if (this.amount == 0) {
        this.type = null;
      }

      this.updateConnections();

      int moldsToCast = maxProcessable();

      // Makes it flush the buffers after 10 seconds of inactivity, or when they're full
      if (moldsToCast > 0 && (moldsToCast >= 9 || world.getWorldTime() >= lastProgressTick + 200)) {

        ItemMold.Mold mold = this.getInstalledMold();

        this.amount -= moldsToCast * mold.getCost();

        ItemStack out = mold.getOutput(type);
        final int itemsPerCast = out.getCount();
        final int initialRemaining = itemsPerCast * moldsToCast;
        int remaining = initialRemaining;

        final int itemMaxStackSize = out.getMaxStackSize();

        for (int i = 1; i < 7 && remaining > 0; i++) {
          ItemStack slotStack = inventory.getStackInSlot(i);
          int slotLimit = Math.min(inventory.getSlotLimit(i), itemMaxStackSize);

          if (slotStack.isEmpty()) {
            int toDeposit = Math.min(remaining, slotLimit);
            if (toDeposit > 0) {
              ItemStack put = out.copy();
              put.setCount(toDeposit);
              inventory.setStackInSlot(i, put);
              remaining -= toDeposit;
            }
            continue;
          }

          if (ItemHandlerHelper.canItemStacksStack(slotStack, out)) {
            int toDeposit = Math.min(remaining, slotLimit - slotStack.getCount());
            if (toDeposit > 0) {
              slotStack.grow(toDeposit);
              inventory.setStackInSlot(i, slotStack);
              remaining -= toDeposit;
            }
          }
        }

        int producedItems = initialRemaining - remaining;
        int castsMade = producedItems / itemsPerCast;

        if (castsMade > 0) {
          water.setFill(water.getFill() - getWaterRequired() * castsMade);
          steam.setFill(steam.getFill() + getWaterRequired() * castsMade);

          markDirty();
          lastProgressTick = world.getWorldTime();
        }
      }

      networkPackNT(150);
    }
  }

  private int maxProcessable() {
    ItemMold.Mold mold = this.getInstalledMold();
    if (type == null || mold == null || mold.getOutput(type).isEmpty()) {
      return 0;
    }

    int freeSlots = 0;
    final int stackLimit = mold.getOutput(type).getMaxStackSize();

    for (int i = 1; i < 7; i++) {
      ItemStack itemStack = inventory.getStackInSlot(i);
      if (itemStack.isEmpty()) {
        freeSlots += stackLimit;
      } else if (itemStack.isItemEqual(mold.getOutput(type))) {
        freeSlots += stackLimit - itemStack.getCount();
      }
    }

    int moldsToCast = amount / mold.getCost();
    moldsToCast = Math.min(moldsToCast, freeSlots / mold.getOutput(type).getCount());
    moldsToCast = Math.min(moldsToCast, water.getFill() / getWaterRequired());
    moldsToCast =
        Math.min(moldsToCast, (steam.getMaxFill() - steam.getFill()) / getWaterRequired());

    return moldsToCast;
  }

  public DirPos[] getFluidConPos() {

    ForgeDirection dir =
        ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
    ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

    return new DirPos[] {
      new DirPos(
          pos.getX() + rot.offsetX * 2 - dir.offsetX,
          pos.getY(),
          pos.getZ() + rot.offsetZ * 2 - dir.offsetZ,
          rot),
      new DirPos(
          pos.getX() - rot.offsetX - dir.offsetX,
          pos.getY(),
          pos.getZ() - rot.offsetZ - dir.offsetZ,
          rot.getOpposite()),
      new DirPos(
          pos.getX() + rot.offsetX * 2 - dir.offsetX * 5,
          pos.getY(),
          pos.getZ() + rot.offsetZ * 2 - dir.offsetZ * 5,
          rot),
      new DirPos(
          pos.getX() - rot.offsetX - dir.offsetX * 5,
          pos.getY(),
          pos.getZ() - rot.offsetZ - dir.offsetZ * 5,
          rot.getOpposite())
    };
  }

  public int[][] getMetalPourPos() {

    ForgeDirection dir =
        ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
    ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

    return new int[][] {
      new int[] {
        pos.getX() + rot.offsetX - dir.offsetX,
        pos.getY() + 2,
        pos.getZ() + rot.offsetZ - dir.offsetZ
      },
      new int[] {pos.getX() - dir.offsetX, pos.getY() + 2, pos.getZ() - dir.offsetZ},
      new int[] {pos.getX() + rot.offsetX, pos.getY() + 2, pos.getZ() + rot.offsetZ},
      new int[] {pos.getX(), pos.getY() + 2, pos.getZ()}
    };
  }

  @Override
  public ItemMold.Mold getInstalledMold() {
    ItemStack itemStack = inventory.getStackInSlot(0);
    if (itemStack.isEmpty()) return null;

    if (itemStack.getItem() == ModItems.mold) {
      return ((ItemMold) itemStack.getItem()).getMold(itemStack);
    }

    return null;
  }

  @Override
  public int getMoldSize() {
    return getInstalledMold().size;
  }

  @Override
  public boolean canAcceptPartialPour(World world, BlockPos pos, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {
    if (side != ForgeDirection.UP) return false;
    for (int[] pourPos : getMetalPourPos()) {
      if (pourPos[0] == pos.getX() && pourPos[1] == pos.getY() && pourPos[2] == pos.getZ()) {
        return this.standardCheck(world, pos, side, stack);
      }
    }
    return false;
  }

  @Override
  public boolean standardCheck(World world, BlockPos pos, ForgeDirection side, Mats.MaterialStack stack) {
    if (this.type != null && this.type != stack.material) return false;
    int limit = this.getInstalledMold() != null ? this.getInstalledMold().getCost() * 9 : this.getCapacity();
    return !(this.amount >= limit || getInstalledMold() == null);
  }

  @Override
  public int getCapacity() {
    ItemMold.Mold mold = this.getInstalledMold();
    return mold == null ? 50000 : mold.getCost() * 10;
  }

  private int getWaterRequired() {
    return getInstalledMold() != null ? 5 * getInstalledMold().getCost() : 50;
  }

  private void updateConnections() {
    for (DirPos dirPos : getFluidConPos()) {
      this.trySubscribe(water.getTankType(), world, dirPos.getPos().getX(), dirPos.getPos().getY(), dirPos.getPos().getZ(), dirPos.getDir());
      this.sendFluid(steam, world, dirPos.getPos().getX(), dirPos.getPos().getY(), dirPos.getPos().getZ(), dirPos.getDir());
    }
  }

  @Override
  public Mats.MaterialStack standardAdd(World world, BlockPos pos, ForgeDirection side, Mats.MaterialStack stack) {
    this.type = stack.material;
    int limit = this.getInstalledMold() != null ? this.getInstalledMold().getCost() * 9 : this.getCapacity();
    if (stack.amount + this.amount <= limit) {
      this.amount += stack.amount;
      return null;
    }

    int required = limit - this.amount;
    this.amount = limit;

    stack.amount -= required;

    lastProgressTick = world.getWorldTime();

    return stack;
  }

  @Override
  public FluidTankNTM[] getSendingTanks() {
    return new FluidTankNTM[] {steam};
  }

  @Override
  public FluidTankNTM[] getReceivingTanks() {
    return new FluidTankNTM[] {water};
  }

  @Override
  public FluidTankNTM[] getAllTanks() {
    return new FluidTankNTM[] {water, steam};
  }

  @Override
  public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new ContainerMachineStrandCaster(player.inventory, this);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public GuiInfoContainer provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new GUIMachineStrandCaster(player.inventory, this);
  }

  @Override
  public void serialize(ByteBuf buf) {
    water.serialize(buf);
    steam.serialize(buf);
  }

  @Override
  public void deserialize(ByteBuf buf) {
    water.deserialize(buf);
    steam.deserialize(buf);
  }

  @Override
  public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    water.writeToNBT(nbt, "w");
    steam.writeToNBT(nbt, "s");
    nbt.setLong("t", lastProgressTick);
    return nbt;
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    water.readFromNBT(nbt, "w");
    steam.readFromNBT(nbt, "s");
    lastProgressTick = nbt.getLong("t");
  }

  @Override
  public boolean isItemValidForSlot(int i, @NotNull ItemStack stack) {
    if (i == 0) return stack.getItem() == ModItems.mold;
    return false;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public double getMaxRenderDistanceSquared() {
    return 65536.0D;
  }

  @Override
  public int[] getAccessibleSlotsFromSide(EnumFacing side) {
    return new int[] {1, 2, 3, 4, 5, 6};
  }

  @Override
  public boolean isUseableByPlayer(EntityPlayer player) {
    if (world.getTileEntity(pos) != this) {
      return false;
    } else {
      return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 128;
    }
  }

  @Override
  public boolean canInsertItem(int slot, ItemStack itemStack, int side) {
    return this.isItemValidForSlot(slot, itemStack);
  }

  @Override
  public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
    return !this.isItemValidForSlot(slot, itemStack);
  }

  AxisAlignedBB bb = null;

  @Override
  public @NotNull AxisAlignedBB getRenderBoundingBox() {

    if (bb == null) {
      bb = new AxisAlignedBB(
              pos.getX() - 7,
              pos.getY(),
              pos.getZ() - 7,
              pos.getX() + 7,
              pos.getY() + 3,
              pos.getZ() + 7);
    }
    return bb;
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
      return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
          new NTMFluidHandlerWrapper(this));
    }

    return super.getCapability(capability, facing);
  }
}
