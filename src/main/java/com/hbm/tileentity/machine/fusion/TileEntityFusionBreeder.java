package com.hbm.tileentity.machine.fusion;

import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerFusionBreeder;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIFusionBreeder;
import com.hbm.inventory.recipes.FluidBreederRecipes;
import com.hbm.inventory.recipes.OutgasserRecipes;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.networkproviders.PlasmaNetwork.PlasmaNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PlasmaNetwork;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityFusionBreeder extends TileEntityMachineBase implements ITickable, IFluidStandardTransceiverMK2, IFusionPowerReceiver, IGUIProvider {

    protected PlasmaNode plasmaNode;

    public FluidTankNTM[] tanks;

    public double neutronEnergy;
    public double neutronEnergySync;
    public double progress;
    public static final double capacity = 10_000D;

    public TileEntityFusionBreeder() {
        super(3, true, false);

        tanks = new FluidTankNTM[2];
        tanks[0] = new FluidTankNTM(Fluids.NONE, 16_000);
        tanks[1] = new FluidTankNTM(Fluids.NONE, 16_000);
    }

    @Override
    public String getDefaultName() {
        return "container.fusionBreeder";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            tanks[0].setType(0, inventory);

            if(!canProcessSolid() && !canProcessLiquid()) {
                this.progress = 0;
            }

            // because tile updates may happen in any order and the value that needs
            // to be synced needs to persist until the next tick due to the batched packets
            this.neutronEnergySync = this.neutronEnergy;

            for(DirPos pos : getConPos()) {
                if(tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(tanks[0].getTankType(), world, pos);
                if(tanks[1].getFill() > 0) this.tryProvide(tanks[1], world, pos);
            }

            if(plasmaNode == null || plasmaNode.expired) {
                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite();
                plasmaNode = UniNodespace.getNode(world, pos.add(dir.offsetX * 2, 2, dir.offsetZ * 2), PlasmaNetwork.THE_PROVIDER);

                if(plasmaNode == null) {
                    plasmaNode = (PlasmaNode) new PlasmaNode(PlasmaNetwork.THE_PROVIDER, new BlockPos(pos.getX() + dir.offsetX * 2, pos.getY() + 2, pos.getZ() + dir.offsetZ * 2))
                            .setConnections(new DirPos(pos.getX() + dir.offsetX * 3, pos.getY() + 2, pos.getZ() + dir.offsetZ * 3, dir));

                    UniNodespace.createNode(world, plasmaNode);
                }
            }

            if(plasmaNode != null && plasmaNode.hasValidNet()) plasmaNode.net.addReceiver(this);

            this.networkPackNT(25);

            this.neutronEnergy = 0;
        }
    }

    public boolean canProcessSolid() {
        if(inventory.getStackInSlot(1).isEmpty()) return false;

        Tuple.Pair<ItemStack, FluidStack> output = OutgasserRecipes.getOutput(inventory.getStackInSlot(1));
        if(output == null) return false;

        FluidStack fluid = output.getValue();

        if(fluid != null) {
            if(tanks[1].getTankType() != fluid.type && tanks[1].getFill() > 0) return false;
            tanks[1].setTankType(fluid.type);
            if(tanks[1].getFill() + fluid.fill > tanks[1].getMaxFill()) return false;
        }

        ItemStack out = output.getKey();
        if(inventory.getStackInSlot(2).isEmpty() || out == null) return true;

        return inventory.getStackInSlot(2).getItem() == out.getItem() && inventory.getStackInSlot(2).getItemDamage() == out.getItemDamage() && inventory.getStackInSlot(2).getCount() + out.getCount() <= inventory.getStackInSlot(2).getMaxStackSize();
    }

    public boolean canProcessLiquid() {

        Tuple.Pair<Integer, FluidStack> output = FluidBreederRecipes.getOutput(tanks[0].getTankType());
        if(output == null) return false;
        if(tanks[0].getFill() < output.getKey()) return false;

        FluidStack fluid = output.getValue();

        if(tanks[1].getTankType() != fluid.type && tanks[1].getFill() > 0) return false;
        tanks[1].setTankType(fluid.type);
        return tanks[1].getFill() + fluid.fill <= tanks[1].getMaxFill();
    }

    private void processSolid() {

        Tuple.Pair<ItemStack, FluidStack> output = OutgasserRecipes.getOutput(inventory.getStackInSlot(1));
        ItemStack stack = this.inventory.getStackInSlot(1);
        if (!stack.isEmpty()) {
            ItemStack newStack = stack.copy();
            newStack.shrink(1);
            this.inventory.setStackInSlot(1, newStack);
        }
        this.progress = 0;

        if(output.getValue() != null) {
            tanks[1].setFill(tanks[1].getFill() + output.getValue().fill);
        }

        ItemStack out = output.getKey();

        if(out != null) {
            if(inventory.getStackInSlot(2).isEmpty()) {
                inventory.setStackInSlot(2, out.copy());
            } else {
                ItemStack newStack = inventory.getStackInSlot(2).copy();
                newStack.grow(out.getCount());
                inventory.setStackInSlot(2, newStack);
            }
        }
    }

    private void processLiquid() {

        Tuple.Pair<Integer, FluidStack> output = FluidBreederRecipes.getOutput(tanks[0].getTankType());
        tanks[0].setFill(tanks[0].getFill() - output.getKey());
        tanks[1].setFill(tanks[1].getFill() + output.getValue().fill);
    }

    public void doProgress() {

        if(canProcessSolid()) {
            this.progress += this.neutronEnergy;
            if(progress > capacity) {
                processSolid();
                progress = 0;
                this.markDirty();
            }
        } else if(canProcessLiquid()) {
            this.progress += this.neutronEnergy;
            if(progress > capacity) {
                processLiquid();
                progress = 0;
                this.markDirty();
            }
        } else {
            progress = 0;
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if(slot == 0) return stack.getItem() instanceof IItemFluidIdentifier; // fluid ID
        if(slot == 1) return OutgasserRecipes.getOutput(stack) != null; // input
        return false;
    }

    @Override public boolean canExtractItem(int slot, ItemStack itemStack, int side) { return slot == 2; }
    @Override public int[] getAccessibleSlotsFromSide(EnumFacing side) { return new int[] {1, 2}; }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(pos.getX() + dir.offsetX * 3, pos.getY() + 2, pos.getZ() + dir.offsetZ * 3, dir),
                new DirPos(pos.getX() + rot.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() - rot.offsetX * 2, pos.getY(), pos.getZ() - rot.offsetZ * 2, rot.getOpposite()),
                new DirPos(pos.getX() + dir.offsetX + rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() + dir.offsetX - rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ - rot.offsetZ * 2, rot.getOpposite())
        };
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if(!world.isRemote) {
            if(this.plasmaNode != null) UniNodespace.destroyNode(world, plasmaNode);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(neutronEnergySync);
        buf.writeDouble(progress);

        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.neutronEnergy = buf.readDouble();
        this.progress = buf.readDouble();

        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.progress = nbt.getDouble("progress");
        this.tanks[0].readFromNBT(nbt, "t0");
        this.tanks[1].readFromNBT(nbt, "t1");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setDouble("progress", this.progress);
        this.tanks[0].writeToNBT(nbt, "t0");
        this.tanks[1].writeToNBT(nbt, "t1");
        return super.writeToNBT(nbt);
    }

    @Override public boolean receivesFusionPower() { return false; }
    @Override public void receiveFusionPower(long fusionPower, double neutronPower) { this.neutronEnergy = neutronPower; doProgress(); }

    @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {tanks[0]}; }
    @Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {tanks[1]}; }
    @Override public FluidTankNTM[] getAllTanks() { return tanks; }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerFusionBreeder(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIFusionBreeder(player.inventory, this);
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 2,
                    pos.getY(),
                    pos.getZ() - 2,
                    pos.getX() + 3,
                    pos.getY() + 4,
                    pos.getZ() + 3
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
