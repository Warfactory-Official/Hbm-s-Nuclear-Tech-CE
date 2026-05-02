package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerICFPress;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIICFPress;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemICFPellet;
import com.hbm.tileentity.IFluidCopiable;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AutoRegister
public class TileEntityICFPress extends TileEntityMachineBase implements IFluidStandardReceiver, IGUIProvider, IFluidCopiable, ITickable {

    public static final int maxMuon = 16;
    private static final int[] SLOTS_TOP_BOTTOM = new int[]{0, 1, 2, 3, 4};
    private static final int[] SLOTS_SIDES = new int[]{0, 1, 2, 3, 5};
    public FluidTankNTM[] tanks;
    public int muon;
    public boolean[] usedFluid = new boolean[2];

    public TileEntityICFPress() {
        super(8, true, false);
        this.tanks = new FluidTankNTM[2];
        this.tanks[0] = new FluidTankNTM(Fluids.DEUTERIUM, 16_000).withOwner(this);
        this.tanks[1] = new FluidTankNTM(Fluids.TRITIUM, 16_000).withOwner(this);
    }

    @Override
    public String getDefaultName() {
        return "container.machineICFPress";
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            this.tanks[0].setType(6, inventory);
            this.tanks[1].setType(7, inventory);

            if (world.getTotalWorldTime() % 20 == 0) {
                this.subscribeToAllAround(tanks[0].getTankType(), this);
                this.subscribeToAllAround(tanks[1].getTankType(), this);
            }

            ItemStack muonStack = inventory.getStackInSlot(2);
            if (muon <= 0 && !muonStack.isEmpty() && muonStack.getItem() == ModItems.particle_muon) {
                ItemStack container = muonStack.getItem().getContainerItem(muonStack);
                ItemStack outputContainerStack = inventory.getStackInSlot(3);
                boolean canStore = false;

                if (container.isEmpty()) {
                    canStore = true;
                } else if (outputContainerStack.isEmpty()) {
                    inventory.setStackInSlot(3, container.copy());
                    canStore = true;
                } else if (ItemStack.areItemsEqual(outputContainerStack, container) && outputContainerStack.getCount() < outputContainerStack.getMaxStackSize()) {
                    outputContainerStack.grow(1);
                    canStore = true;
                }

                if (canStore) {
                    this.muon = maxMuon;
                    muonStack.shrink(1);
                    this.markDirty();
                }
            }

            press();
            this.networkPackNT(15);
        }
    }

    public void press() {
        ItemStack emptyPelletSlot = inventory.getStackInSlot(0);
        ItemStack outputSlot = inventory.getStackInSlot(1);

        if (emptyPelletSlot.isEmpty() || emptyPelletSlot.getItem() != ModItems.icf_pellet_empty) return;
        if (!outputSlot.isEmpty()) return;

        ItemICFPellet.init();

        ItemICFPellet.EnumICFFuel fuel1 = getFuel(tanks[0], inventory.getStackInSlot(4), 0);
        ItemICFPellet.EnumICFFuel fuel2 = getFuel(tanks[1], inventory.getStackInSlot(5), 1);

        if (fuel1 == null || fuel2 == null || fuel1 == fuel2) return;

        ItemStack newPellet = ItemICFPellet.setup(fuel1, fuel2, muon > 0);
        inventory.setStackInSlot(1, newPellet);

        if (muon > 0) muon--;

        emptyPelletSlot.shrink(1);

        if (usedFluid[0]) {
            tanks[0].drain(1000, true);
        } else {
            inventory.getStackInSlot(4).shrink(1);
        }
        if (usedFluid[1]) {
            tanks[1].drain(1000, true);
        } else {
            inventory.getStackInSlot(5).shrink(1);
        }

        this.markDirty();
    }

    @Nullable
    public ItemICFPellet.EnumICFFuel getFuel(@NotNull FluidTankNTM tank, @NotNull ItemStack slot, int index) {
        usedFluid[index] = false;
        if (tank.getFill() >= 1000 && ItemICFPellet.fluidMap.containsKey(tank.getTankType())) {
            usedFluid[index] = true;
            return ItemICFPellet.fluidMap.get(tank.getTankType());
        }
        if (slot.isEmpty()) return null;
        List<Mats.MaterialStack> mats = Mats.getMaterialsFromItem(slot);
        if (mats.size() != 1) return null;

        Mats.MaterialStack mat = mats.get(0);
        if (mat.amount != MaterialShapes.INGOT.q(1)) return null;
        return ItemICFPellet.materialMap.get(mat.material);
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeByte((byte) muon);
        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.muon = buf.readByte();
        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tanks[0].readFromNBT(nbt, "t0");
        tanks[1].readFromNBT(nbt, "t1");
        this.muon = nbt.getByte("muon");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        tanks[0].writeToNBT(nbt, "t0");
        tanks[1].writeToNBT(nbt, "t1");
        nbt.setByte("muon", (byte) muon);
        return nbt;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerICFPress(player.inventory, this);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIICFPress(player.inventory, this);
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return tanks;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack.getItem() == ModItems.icf_pellet_empty) return slot == 0;
        if (stack.getItem() == ModItems.particle_muon) return slot == 2;
        return slot == 4 || slot == 5;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return side == EnumFacing.UP || side == EnumFacing.DOWN ? SLOTS_TOP_BOTTOM : SLOTS_SIDES;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
        return slot == 1 || slot == 3;
    }
}
