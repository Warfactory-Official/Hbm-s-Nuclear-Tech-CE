package com.hbm.tileentity.machine.oil;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardSender;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerLiquefactor;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUILiquefactor;
import com.hbm.inventory.recipes.LiquefactionRecipes;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMachineLiquefactor extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardSender, IGUIProvider, ITickable {

    public static final long maxPower = 100000;
    public static final int usageBase = 500;
    public static final int processTimeBase = 100;
    private final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    public long power;
    public int usage;
    public int progress;
    public int processTime;
    public FluidTankNTM tank;
    AxisAlignedBB bb = null;

    public TileEntityMachineLiquefactor() {
        super(4, true, true);
        tank = new FluidTankNTM(Fluids.NONE, 24000);
    }

    @Override
    public String getDefaultName() {
        return "container.machineLiquefactor";
    }

    @Override
    public void update() {

        if (!world.isRemote) {
            this.power = Library.chargeTEFromItems(inventory, 1, power, maxPower);

            this.updateConnections();

            upgradeManager.checkSlots(inventory, 2, 3);
            int speed = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3);
            int power = Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 3);

            this.processTime = processTimeBase - (processTimeBase / 4) * speed;
            this.usage = (usageBase + (usageBase * speed)) / (power + 1);

            if (this.canProcess())
                this.process();
            else
                this.progress = 0;

            this.sendFluid();

            networkPackNT(50);
        }
    }

    private void updateConnections() {
        for (DirPos pos : getConPos()) {
            this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    private void sendFluid() {
        for (DirPos pos : getConPos()) {
            this.sendFluid(tank, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    private DirPos[] getConPos() {
        return new DirPos[]{
                new DirPos(pos.getX(), pos.getY() + 4, pos.getZ(), Library.POS_Y),
                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
                new DirPos(pos.getX() + 2, pos.getY() + 1, pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() - 2, pos.getY() + 1, pos.getZ(), Library.NEG_X),
                new DirPos(pos.getX(), pos.getY() + 1, pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX(), pos.getY() + 1, pos.getZ() - 2, Library.NEG_Z)
        };
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        return i == 0 && LiquefactionRecipes.getOutput(itemStack) != null;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[]{0};
    }

    public boolean canProcess() {

        if (this.power < usage)
            return false;

        if (inventory.getStackInSlot(0).isEmpty())
            return false;

        FluidStack out = LiquefactionRecipes.getOutput(inventory.getStackInSlot(0));

        if (out == null)
            return false;

        if (out.type != tank.getTankType() && tank.getFill() > 0)
            return false;

        if (out.fill + tank.getFill() > tank.getMaxFill())
            return false;

        return true;
    }

    public void process() {

        this.power -= usage;

        progress++;

        if (progress >= processTime) {

            FluidStack out = LiquefactionRecipes.getOutput(inventory.getStackInSlot(0));
            tank.setTankType(out.type);
            tank.setFill(tank.getFill() + out.fill);
            this.inventory.getStackInSlot(0).shrink(1);

            progress = 0;

            this.markDirty();
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.progress);
        buf.writeInt(this.usage);
        buf.writeInt(this.processTime);
        tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.progress = buf.readInt();
        this.usage = buf.readInt();
        this.processTime = buf.readInt();
        tank.deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.readFromNBT(nbt, "tank");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        tank.writeToNBT(nbt, "tank");
        return nbt;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
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
        return new FluidTankNTM[]{tank};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerLiquefactor(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUILiquefactor(player.inventory, this);
    }
}
