package com.hbm.tileentity.network;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.entity.item.EntityDeliveryDrone;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerDroneCrate;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIDroneCrate;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.ParticleUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister
public class TileEntityDroneCrate extends TileEntityMachineBase implements IGUIProvider, IControlReceiver, IDroneLinkable, IFluidStandardTransceiver, IFluidCopiable, ITickable {

    public FluidTankNTM tank;

    public int nextX = -1;
    public int nextY = -1;
    public int nextZ = -1;

    public boolean sendingMode = false;
    public boolean itemType = true;

    public TileEntityDroneCrate() {
        super(19, false, false);
        this.tank = new FluidTankNTM(Fluids.NONE, 64_000).withOwner(this);
    }

    @Override
    public String getDefaultName() {
        return "container.droneCrate";
    }

    @Override
    public void update() {
        if(!world.isRemote) {
            BlockPos dronePos = getCoord();
            this.tank.setType(18, inventory);

            if(sendingMode && !itemType && world.getTotalWorldTime() % 20 == 0) {
                this.subscribeToAllAround(tank.getTankType(), this);
            }

            if(!sendingMode && !itemType && world.getTotalWorldTime() % 20 == 0) {
                this.sendFluidToAll(tank, this);
            }

            if(nextY != -1) {

                List<EntityDeliveryDrone> drones = world.getEntitiesWithinAABB(EntityDeliveryDrone.class, new AxisAlignedBB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1));
                for(EntityDeliveryDrone drone : drones) {
                    if(new Vec3d(drone.motionX, drone.motionY, drone.motionZ).length() < 0.05) {
                        drone.setTarget(nextX + 0.5, nextY, nextZ + 0.5);

                        if(sendingMode && itemType) loadItems(drone);
                        if(!sendingMode && itemType) unloadItems(drone);
                        if(sendingMode && !itemType) loadFluid(drone);
                        if(!sendingMode && !itemType) unloadFluid(drone);
                    }
                }

                ParticleUtil.spawnDroneLine(world,
                        dronePos.getX() + 0.5, dronePos.getY() + 0.5, dronePos.getZ() + 0.5,
                        (nextX  - dronePos.getX()), (nextY - dronePos.getY()), (nextZ - dronePos.getZ()), 0x00ffff);
            }

            networkPackNT(25);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        BufferUtil.writeIntArray(buf, new int[] {
                this.nextX,
                this.nextY,
                this.nextZ
        });
        buf.writeBoolean(this.sendingMode);
        buf.writeBoolean(this.itemType);
        tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        int[] pos = BufferUtil.readIntArray(buf);
        this.nextX = pos[0];
        this.nextY = pos[1];
        this.nextZ = pos[2];
        this.sendingMode = buf.readBoolean();
        this.itemType = buf.readBoolean();
        tank.deserialize(buf);
    }

    protected void loadItems(EntityDeliveryDrone drone) {

        if (drone.getAppearance() != 0) return;

        boolean loaded = false;

        for (int i = 0; i < 18; i++) {
            if (!this.inventory.getStackInSlot(i).isEmpty()) {
                loaded = true;
                drone.setInventorySlotContents(i, this.inventory.getStackInSlot(i).copy());
                this.inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        if (loaded) {
            this.markDirty();
            drone.setAppearance(1);
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.itemUnpack, SoundCategory.BLOCKS, 0.5F, 0.75F);
        }
    }

    protected void unloadItems(EntityDeliveryDrone drone) {
        if (drone.getAppearance() != 1) return;

        for (int d = 0; d < 18; d++) {
            ItemStack droneStack = drone.getStackInSlot(d);
            if (droneStack.isEmpty()) continue;

            for (int c = 0; c < 18 && !droneStack.isEmpty(); c++) {
                ItemStack crateStack = this.inventory.getStackInSlot(c);
                if (crateStack.isEmpty()) continue;
                if (ItemStack.areItemsEqual(crateStack, droneStack) &&
                        ItemStack.areItemStackTagsEqual(crateStack, droneStack) &&
                        crateStack.getCount() < crateStack.getMaxStackSize()) {

                    int can = Math.min(crateStack.getMaxStackSize() - crateStack.getCount(), droneStack.getCount());
                    if (can > 0) {
                        crateStack.grow(can);
                        droneStack.shrink(can);
                        drone.setInventorySlotContents(d, droneStack.isEmpty() ? ItemStack.EMPTY : droneStack);
                    }
                }
            }

            if (!droneStack.isEmpty()) {
                for (int c = 0; c < 18; c++) {
                    if (this.inventory.getStackInSlot(c).isEmpty()) {
                        this.inventory.setStackInSlot(c, droneStack.copy());
                        drone.setInventorySlotContents(d, ItemStack.EMPTY);
                        break;
                    }
                }
            }
        }

        this.markDirty();

        boolean emptied = true;
        for (int i = 0; i < 18; i++) {
            if (!drone.getStackInSlot(i).isEmpty()) { emptied = false; break; }
        }

        if (emptied) {
            drone.setAppearance(0);
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    HBMSoundHandler.itemUnpack, SoundCategory.BLOCKS, 0.5F, 0.75F);
        }
    }

    protected void loadFluid(EntityDeliveryDrone drone) {

        if(drone.getAppearance() != 0) return;

        if(this.tank.getFill() > 0) {
            drone.fluid = new FluidStack(tank.getTankType(), tank.getFill());
            this.tank.setFill(0);
            drone.setAppearance(2);
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.itemUnpack, SoundCategory.BLOCKS, 0.5F, 0.75F);

            this.markDirty();
        }
    }

    protected void unloadFluid(EntityDeliveryDrone drone) {

        if(drone.getAppearance() != 2) return;

        if(drone.fluid != null && drone.fluid.type == tank.getTankType()) {

            if(drone.fluid.fill + tank.getFill() <= tank.getMaxFill()) {
                tank.setFill(tank.getFill() + drone.fluid.fill);
                drone.fluid = null;
                drone.setAppearance(0);
            } else {
                int overshoot = drone.fluid.fill + tank.getFill() - tank.getMaxFill();
                tank.setFill(tank.getMaxFill());
                drone.fluid.fill = overshoot;
            }
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.itemUnpack, SoundCategory.BLOCKS, 0.5F, 0.75F);

            this.markDirty();
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };
    }

    @Override
    public BlockPos getPoint() {
        return pos.up();
    }

    @Override
    public void setNextTarget(int x, int y, int z) {
        this.nextX = x;
        this.nextY = y;
        this.nextZ = z;
        this.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.nextX = nbt.getInteger("posX");
        this.nextY = nbt.getInteger("posY");;
        this.nextZ = nbt.getInteger("posZ");;
        this.sendingMode = nbt.getBoolean("mode");
        this.itemType = nbt.getBoolean("type");
        tank.readFromNBT(nbt, "t");
    }

    public BlockPos getCoord() {
        return pos.up();
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("posX", nextX);
        nbt.setInteger("posY", nextY);
        nbt.setInteger("posZ", nextZ);
        nbt.setBoolean("mode", sendingMode);
        nbt.setBoolean("type", itemType);
        tank.writeToNBT(nbt, "t");
        return nbt;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerDroneCrate(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIDroneCrate(player.inventory, this);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {

        if(data.hasKey("mode")) {
            this.sendingMode = !this.sendingMode;
            this.markDirty();
        }

        if(data.hasKey("type")) {
            this.itemType = !this.itemType;
            this.markDirty();
        }
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[] { tank };
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return !sendingMode && !itemType ? new FluidTankNTM[] { tank } : new FluidTankNTM[0];
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return sendingMode && !itemType ? new FluidTankNTM[] { tank } : new FluidTankNTM[0];
    }
}
