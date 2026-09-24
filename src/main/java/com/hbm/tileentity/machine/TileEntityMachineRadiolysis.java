package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerRadiolysis;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIRadiolysis;
import com.hbm.inventory.recipes.RadiolysisRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.RTGUtil;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityMachineRadiolysis extends TileEntityMachineBase implements ITickable, IEnergyProviderMK2, IFluidStandardTransceiver, IGUIProvider, IFluidCopiable, IConnectionAnchors {

    public long power;
    public static final int maxPower = 1000000;
    public int heat;

    public FluidTankNTM[] tanks;

    private static final int[] slot_io = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 13 };
    private static final int[] slot_rtg = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    public TileEntityMachineRadiolysis() {
        super(15, true, true); //10 rtg slots, 2 fluid ID slots (io), 2 irradiation slots (io), battery slot
        tanks = new FluidTankNTM[3];
        tanks[0] = new FluidTankNTM(Fluids.NONE, 2_000).withOwner(this);
        tanks[1] = new FluidTankNTM(Fluids.NONE, 2_000).withOwner(this);
        tanks[2] = new FluidTankNTM(Fluids.NONE, 2_000).withOwner(this);
    }

    @Override
    public String getDefaultName() {
        return "container.radiolysis";
    }

    /* IO Methods */
    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        return i == 12 || (i < 10 && itemStack.getItem() instanceof ItemRTGPellet);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return slot_io;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return (i < 10 && ItemRTGPellet.pelletMap.containsValue(itemStack)) || i == 13;
    }

    /* NBT Methods */
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.power = nbt.getLong("power");
        this.heat = nbt.getInteger("heat");

        tanks[0].readFromNBT(nbt, "input");
        tanks[1].readFromNBT(nbt, "output1");
        tanks[2].readFromNBT(nbt, "output2");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setLong("power", power);
        nbt.setInteger("heat", heat);

        tanks[0].writeToNBT(nbt, "input");
        tanks[1].writeToNBT(nbt, "output1");
        tanks[2].writeToNBT(nbt, "output2");
        return nbt;
    }

    @Override
    public void update() {

        if(!world.isRemote) {
            power = Library.chargeItemsFromTE(inventory, 14, power, maxPower);

            heat = RTGUtil.updateRTGs(inventory, slot_rtg);
            power += heat * 10;

            if(power > maxPower)
                power = maxPower;

            tanks[0].setType(10, 11, inventory);
            setupTanks();

            if(heat > 100) {
                int crackTime = (int) Math.max(-0.1 * (heat - 100) + 30, 5);

                if(world.getTotalWorldTime() % crackTime == 0)
                    crack();

                if(heat >= 200 && world.getTotalWorldTime() % 100 == 0)
                    sterilize();
            }

            for(DirPos dirPos : getConPos()) {
                BlockPos pos = dirPos.getPos();
                this.tryProvide(world, pos.getX(), pos.getY(), pos.getZ(), dirPos.getDir());
                this.trySubscribe(tanks[0].getTankType(), world, pos.getX(), pos.getY(),pos.getZ(), dirPos.getDir());
                if(tanks[1].getFill() > 0) this.sendFluid(tanks[1], world, pos.getX(), pos.getY(),pos.getZ(), dirPos.getDir());
                if(tanks[2].getFill() > 0) this.sendFluid(tanks[2], world, pos.getX(), pos.getY(),pos.getZ(), dirPos.getDir());
            }

            this.networkPackNT(50);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.heat);
        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
        tanks[2].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.heat = buf.readInt();
        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
        tanks[2].deserialize(buf);
    }

    public DirPos[] getConPos() {
        int xCoord = this.getPos().getX(), yCoord = this.getPos().getY(), zCoord = this.getPos().getZ();
        return new DirPos[] {
                new DirPos(xCoord + 2, yCoord, zCoord, Library.POS_X),
                new DirPos(xCoord - 2, yCoord, zCoord, Library.NEG_X),
                new DirPos(xCoord, yCoord, zCoord + 2, Library.POS_Z),
                new DirPos(xCoord, yCoord, zCoord - 2, Library.NEG_Z)
        };
    }

    /* Processing Methods */
    private void crack() {

        Tuple.Pair<FluidStack, FluidStack> quart = RadiolysisRecipes.getRadiolysis(tanks[0].getTankType());

        if(quart != null) {

            int left = quart.getKey().fill;
            int right = quart.getValue().fill;

            if(tanks[0].getFill() >= 100 && hasSpace(left, right)) {
                tanks[0].setFill(tanks[0].getFill() - 100);
                tanks[1].setFill(tanks[1].getFill() + left);
                tanks[2].setFill(tanks[2].getFill() + right);
            }
        }
    }

    private boolean hasSpace(int left, int right) {
        return tanks[1].getFill() + left <= tanks[1].getMaxFill() && tanks[2].getFill() + right <= tanks[2].getMaxFill();
    }

    private void setupTanks() {

        Tuple.Pair<FluidStack, FluidStack> quart = RadiolysisRecipes.getRadiolysis(tanks[0].getTankType());

        if(quart != null) {
            tanks[1].setTankType(quart.getKey().type);
            tanks[2].setTankType(quart.getValue().type);
        } else {
            tanks[0].setTankType(Fluids.NONE);
            tanks[1].setTankType(Fluids.NONE);
            tanks[2].setTankType(Fluids.NONE);
        }

    }

    // Code: pressure, sword, sterilize.
    private void sterilize() {
        if(!inventory.getStackInSlot(12).isEmpty()) {
            if (inventory.getStackInSlot(12).getItem() instanceof ItemFood && !(inventory.getStackInSlot(12).getItem() == ModItems.pancake)) {
                inventory.extractItem(12, 1, false);
            }

            if (!checkIfValid()) return;

            ItemStack output = inventory.getStackInSlot(12).copy();
            if (output.hasTagCompound() && output.getTagCompound().hasKey("ntmContagion")) {
                output.getTagCompound().removeTag("ntmContagion");
                if (output.getTagCompound().isEmpty())
                    output.setTagCompound(null);
            }
            output.setCount(1);
            if (inventory.insertItem(13, output, true).isEmpty()) {
                inventory.extractItem(12, output.getCount(), false);
                inventory.insertItem(13, output, false);
            }
        }
    }

    private boolean checkIfValid() {
        if(inventory.getStackInSlot(12).isEmpty()) return false;
        if(!inventory.getStackInSlot(12).hasTagCompound()) return false;
        if(!inventory.getStackInSlot(12).getTagCompound().getBoolean("ntmContagion")) return false;
        return true;
    }

    /* Power methods */
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
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[] {tanks[1], tanks[2]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[] {tanks[0]};
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && dir != ForgeDirection.DOWN;
    }

    private AxisAlignedBB bb = null;

    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null){
            int xCoord = this.getPos().getX(), yCoord = this.getPos().getY(), zCoord = this.getPos().getZ();
            bb = new AxisAlignedBB(xCoord - 1, yCoord, zCoord - 1, xCoord + 2, yCoord + 3, zCoord + 2);
        }
        return bb;
    }

    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerRadiolysis(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIRadiolysis(player.inventory, this);
    }
}
