package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineRockMill;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineRockMill;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.modules.machine.ModuleMachineRockMill;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMachineRockMill extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IControlReceiver, IGUIProvider {

    public static final float ACCELERATION = 0.1F;
    public static final float MAX_SPEED = 15F;
    public FluidTankNTM[] inputTanks;
    public FluidTankNTM[] outputTanks;
    public long power;
    public long maxPower = 2_500;
    public boolean didProcess = false;
    public float rotation;
    public float prevRotation;
    public float rotationSpeed = 0F;
    public boolean frame = false;

    public ModuleMachineRockMill rockMillModule;
    AxisAlignedBB bb = null;

    public TileEntityMachineRockMill() {
        super(8, true, true);

        this.inputTanks = new FluidTankNTM[1];
        this.outputTanks = new FluidTankNTM[1];

        this.inputTanks[0] = new FluidTankNTM(Fluids.NONE, 4_000);
        this.outputTanks[0] = new FluidTankNTM(Fluids.NONE, 4_000);

        this.rockMillModule = new ModuleMachineRockMill(0, this, inventory)
                .itemInput(2).itemOutput(5)
                .fluidInput(inputTanks[0]).fluidOutput(outputTanks[0]);
    }

    @Override
    public String getDefaultName() {
        return "container.machineRockMill";
    }

    @Override
    public void update() {

        if (maxPower <= 0) this.maxPower = 2_500;

        if (!world.isRemote) {

            GenericRecipe recipe = rockMillModule.getRecipe();
            if (recipe != null) {
                this.maxPower = recipe.power * 100;
            }

            this.maxPower = BobMathUtil.max(this.power, this.maxPower, 1_000_000);

            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

            for (DirPos pos : getConPos()) {
                this.trySubscribe(world, pos);
                for (FluidTankNTM tank : inputTanks)
                    if (tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), world, pos);
                for (FluidTankNTM tank : outputTanks) if (tank.getFill() > 0) this.tryProvide(tank, world, pos);
            }

            this.rockMillModule.update(1D, 1D, true, inventory.getStackInSlot(1));
            this.didProcess = this.rockMillModule.didProcess;
            if (this.rockMillModule.markDirty) this.markDirty();

            this.networkPackNT(100);

        } else {

            this.prevRotation = this.rotation;

            this.rotationSpeed += ACCELERATION * (this.didProcess ? 1 : -1);
            this.rotationSpeed = MathHelper.clamp(this.rotationSpeed, 0F, MAX_SPEED);

            this.rotation += this.rotationSpeed;

            if (this.rotation >= 360F) {
                this.prevRotation -= 360F;
                this.rotation -= 360F;
            }

            if (world.getTotalWorldTime() % 20 == 0) {
                frame = world.getBlockState(pos.up(3)).getMaterial() != Material.AIR;
            }
        }
    }

    public DirPos[] getConPos() {
        return new DirPos[]{

                new DirPos(pos.getX() + 3, pos.getY(), pos.getZ() + 1, Library.POS_X),
                new DirPos(pos.getX() + 3, pos.getY(), pos.getZ() - 1, Library.POS_X),
                new DirPos(pos.getX() - 3, pos.getY(), pos.getZ() + 1, Library.NEG_X),
                new DirPos(pos.getX() - 3, pos.getY(), pos.getZ() - 1, Library.NEG_X),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 3, Library.POS_Z),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 3, Library.POS_Z),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 3, Library.NEG_Z),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 3, Library.NEG_Z),
        };
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        for (FluidTankNTM tank : inputTanks) tank.serialize(buf);
        for (FluidTankNTM tank : outputTanks) tank.serialize(buf);
        buf.writeLong(power);
        buf.writeLong(maxPower);
        buf.writeBoolean(didProcess);
        this.rockMillModule.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        for (FluidTankNTM tank : inputTanks) tank.deserialize(buf);
        for (FluidTankNTM tank : outputTanks) tank.deserialize(buf);
        this.power = buf.readLong();
        this.maxPower = buf.readLong();
        this.didProcess = buf.readBoolean();
        this.rockMillModule.deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.inputTanks[0].readFromNBT(nbt, "i" + 0);
        this.outputTanks[0].readFromNBT(nbt, "o" + 0);

        this.power = nbt.getLong("power");
        this.maxPower = nbt.getLong("maxPower");
        this.rockMillModule.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.inputTanks[0].writeToNBT(nbt, "i" + 0);
        this.outputTanks[0].writeToNBT(nbt, "o" + 0);

        nbt.setLong("power", power);
        nbt.setLong("maxPower", maxPower);
        this.rockMillModule.writeToNBT(nbt);
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == 0) return stack.getItem() instanceof IBatteryItem; // battery
        if (slot == 1 && stack.getItem() == ModItems.blueprints) return true;
        return this.rockMillModule.isItemValid(slot, stack); // recipe input crap
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return (i >= 5 && i <= 7) || this.rockMillModule.isSlotClogged(i);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[]{2, 3, 4, 5, 6, 7};
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
    public FluidTankNTM[] getReceivingTanks() {
        return inputTanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return outputTanks;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{inputTanks[0], outputTanks[0]};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineRockMill(player.inventory, this.inventory);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineRockMill(player.inventory, this);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {
        if (data.hasKey("index") && data.hasKey("selection")) {
            int index = data.getInteger("index");
            String selection = data.getString("selection");
            if (index == 0) {
                this.rockMillModule.setRecipe(selection, false);
                this.markChanged();
            }
        }
    }

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        if (bb == null)
            bb = new AxisAlignedBB(pos.getX() - 2, pos.getY(), pos.getZ() - 2, pos.getX() + 3, pos.getY() + 3, pos.getZ() + 3);
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
