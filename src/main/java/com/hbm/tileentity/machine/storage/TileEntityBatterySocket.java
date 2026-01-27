package com.hbm.tileentity.machine.storage;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerBatterySocket;
import com.hbm.inventory.gui.GUIBatterySocket;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;

import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityBatterySocket extends TileEntityBatteryBase {

    public long[] log = new long[20];
    public long delta = 0;

    public ItemStack syncStack;

    public TileEntityBatterySocket() {
        super(1);
    }

    @Override
    public String getDefaultName() {
        return "container.batterySocket";
    }

    @Override
    public void update() {
        long prevPower = this.getPower();

        super.update();
        if (!world.isRemote) {

            long avg = (this.getPower() + prevPower) / 2;
            this.delta = avg - this.log[0];

            for (int i = 1; i < this.log.length; i++) {
                this.log[i - 1] = this.log[i];
            }

            this.log[19] = avg;
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(delta);
        BufferUtil.writeItemStack(buf, this.inventory.getStackInSlot(0));
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        delta = buf.readLong();
        this.syncStack = BufferUtil.readItemStack(buf);
    }

    @Override
    public boolean canExtractItem(int i, ItemStack stack, int j) {
        if (stack.getItem() instanceof IBatteryItem) {
            if (i == mode_input && ((IBatteryItem) stack.getItem()).getCharge(stack) == 0) return true;
            return i == mode_output && ((IBatteryItem) stack.getItem()).getCharge(stack) == ((IBatteryItem) stack.getItem()).getMaxCharge(stack);
        }
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[]{0};
    }

    @Override public long getPower() { return powerFromStack(this.inventory.getStackInSlot(0)); }
    @Override public long getMaxPower() { return maxPowerFromStack(this.inventory.getStackInSlot(0)); }

    @Override
    public void setPower(long power) {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem))
            return;
        ((IBatteryItem) inventory.getStackInSlot(0).getItem()).setCharge(inventory.getStackInSlot(0), power);
    }

    public static long powerFromStack(ItemStack stack) {
        if(stack == null || !(stack.getItem() instanceof IBatteryItem)) return 0;
        return ((IBatteryItem) stack.getItem()).getCharge(stack);
    }

    public static long maxPowerFromStack(ItemStack stack) {
        if(stack == null || !(stack.getItem() instanceof IBatteryItem)) return 0;
        return ((IBatteryItem) stack.getItem()).getMaxCharge(stack);
    }

    @Override
    public long getProviderSpeed() {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem))
            return 0;
        int mode = this.getRelevantMode(true);
        return mode == mode_output || mode == mode_buffer ? ((IBatteryItem) inventory.getStackInSlot(0).getItem()).getDischargeRate(inventory.getStackInSlot(0)) : 0;
    }

    @Override
    public long getReceiverSpeed() {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem))
            return 0;
        int mode = this.getRelevantMode(true);
        return mode == mode_input || mode == mode_buffer ? ((IBatteryItem) inventory.getStackInSlot(0).getItem()).getChargeRate(inventory.getStackInSlot(0)) : 0;
    }

    @Override
    public BlockPos[] getPortPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new BlockPos[]{
                new BlockPos(pos.getX(), pos.getY(), pos.getZ()),
                new BlockPos(pos.getX() - dir.offsetX, pos.getY(), pos.getZ() - dir.offsetZ),
                new BlockPos(pos.getX() + rot.offsetX, pos.getY(), pos.getZ() + rot.offsetZ),
                new BlockPos(pos.getX() - dir.offsetX + rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ + rot.offsetZ)
        };
    }

    @Override
    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new DirPos[]{
                new DirPos(pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ, dir),
                new DirPos(pos.getX() + dir.offsetX + rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ + rot.offsetZ, dir),

                new DirPos(pos.getX() - dir.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 2 + rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite()),

                new DirPos(pos.getX() + rot.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() + rot.offsetX * 2 - dir.offsetX, pos.getY(), pos.getZ() + rot.offsetZ * 2 - dir.offsetZ, rot),

                new DirPos(pos.getX() - rot.offsetX, pos.getY(), pos.getZ() - rot.offsetZ, rot.getOpposite()),
                new DirPos(pos.getX() - rot.offsetX - dir.offsetX, pos.getY(), pos.getZ() - rot.offsetZ - dir.offsetZ, rot.getOpposite())
        };
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerBatterySocket(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIBatterySocket(player.inventory, this);
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 2,
                    pos.getZ() + 2
            );
        }

        return bb;
    }

    // do some opencomputer stuff
    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getEnergyInfo(Context context, Arguments args) {
        return new Object[] {getPower(), getMaxPower(), this.delta};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getPackInfo(Context context, Arguments args) {
        if(inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem bat)) return new Object[] {"", 0, 0};
        return new Object[] {inventory.getStackInSlot(0).getTranslationKey(), bat.getChargeRate(inventory.getStackInSlot(0)), bat.getDischargeRate(inventory.getStackInSlot(0))};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInfo(Context context, Arguments args) {
        Object[] energyInfo = getEnergyInfo(context, args);
        Object[] packInfo = getPackInfo(context, args);
        Object[] modeInfo = getModeInfo(context, args);
        return new Object[] {energyInfo[0], energyInfo[1], energyInfo[2], modeInfo[0], modeInfo[1], modeInfo[2], packInfo[0], packInfo[1], packInfo[2]};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[] {
                "getEnergyInfo",
                "getPackInfo",
                "getModeInfo",
                "setModeLow",
                "setModeHigh",
                "setPriority",
                "getInfo"
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        return switch (method) {
            case "getEnergyInfo" -> getEnergyInfo(context, args);
            case "getPackInfo" -> getPackInfo(context, args);
            case "getModeInfo" -> getModeInfo(context, args);
            case "setModeLow" -> setModeLow(context, args);
            case "setModeHigh" -> setModeHigh(context, args);
            case "setPriority" -> setPriority(context, args);
            case "getInfo" -> getInfo(context, args);
            default -> throw new NoSuchMethodException();
        };
    }
}
