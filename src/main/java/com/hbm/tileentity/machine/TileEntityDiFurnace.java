package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardSender;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineDiFurnace;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerDiFurnace;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIDiFurnace;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityDiFurnace extends TileEntityMachinePolluting implements ITickable, IFluidStandardSender, IGUIProvider {


    private static final int maxFuel = 12800;
    private static final int processingSpeed = 400;
    private static final int[] slots_io = new int[]{0, 1, 2, 3};
    public int progress;


    //	private static final int[] slots_top = new int[] {0, 1};
//	private static final int[] slots_bottom = new int[] {3};
//	private static final int[] slots_side = new int[] {2};
    public int fuel;
    // mlbv: yeah 1.7 have them all set to 1 as well
    public byte sideFuel = 1;
    public byte sideUpper = 1;
    public byte sideLower = 1;

    public TileEntityDiFurnace() {
        super(4, 50, true, false);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.fuel = compound.getInteger("powerTime");
        this.progress = compound.getShort("cookTime");

        byte[] modes = compound.getByteArray("modes");
        if (modes.length != 3) return;
        this.sideFuel = modes[0];
        this.sideUpper = modes[1];
        this.sideLower = modes[2];
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("powerTime", fuel);
        compound.setShort("cookTime", (short) progress);
        compound.setByteArray("modes", new byte[]{sideFuel, sideUpper, sideLower});
        return compound;
    }

    @Override
    public void update() {


        if (!world.isRemote) {

            boolean extension = world.getBlockState(pos.offset(EnumFacing.UP)).getBlock() == ModBlocks.machine_difurnace_ext;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                this.sendSmoke(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
            }

            if (extension) this.sendSmoke(pos.getX(), pos.getY() + 2, pos.getZ(), ForgeDirection.UP);

            boolean markDirty = false;
            ItemStack fuelSim = inventory.extractItem(2, 1, true);
            int fuelPower = BlastFurnaceRecipes.getItemPower(fuelSim);
            if (!fuelSim.isEmpty() && fuelPower > 0 && this.fuel <= (TileEntityDiFurnace.maxFuel - fuelPower)) {
                ItemStack fuelConsumed = inventory.extractItem(2, 1, false);
                this.fuel += fuelPower;
                markDirty = true;
                ItemStack container = fuelConsumed.getItem().getContainerItem(fuelConsumed);
                if (!container.isEmpty()) {
                    ItemStack rem = inventory.insertItem(2, container, false);
                    if (!rem.isEmpty()) {
                        inventory.insertItem(3, rem, false);
                    }
                }
            }

            if (canProcess()) {

                //fuel -= extension ? 2 : 1;
                fuel -= 1; //switch it up on me, fuel efficiency, on fumes i'm running - running - running - running
                progress += extension ? 3 : 1;

                if (this.progress >= TileEntityDiFurnace.processingSpeed) {
                    this.progress -= TileEntityDiFurnace.processingSpeed;
                    this.processItem();
                    markDirty = true;
                }

                if (fuel < 0) {
                    fuel = 0;
                }

                if (world.getTotalWorldTime() % 20 == 0)
                    this.pollute(PollutionHandler.PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * (extension ? 3 : 1));

            } else {
                progress = 0;
            }

            boolean trigger = !canProcess() || this.progress != 0;

            if (trigger) {
                markDirty = true;
                MachineDiFurnace.updateBlockState(this.progress > 0, extension, world, pos);
            }

            networkPackNT(15);

            if (markDirty) {
                this.markDirty();
            }
        }
    }

    public boolean hasItemPower(ItemStack itemStack) {
        return BlastFurnaceRecipes.getItemPower(itemStack) > 0;
    }

    @Override
    public String getDefaultName() {
        return "container.diFurnace";
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        int i = e.ordinal();
        return i == 0 ? new int[]{3} : (i == 1 ? new int[]{0, 1} : new int[]{2});
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        if (i == 3) return false;
        if (i == 2) return hasItemPower(stack);
        return true;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack) {
        if (!isItemValidForSlot(slot, stack)) return false;

        if (slot == 0) {
            ItemStack other = inventory.getStackInSlot(1);
            return other.isEmpty() || other.getItem() != stack.getItem();
        }
        if (slot == 1) {
            ItemStack other = inventory.getStackInSlot(0);
            return other.isEmpty() || other.getItem() != stack.getItem();
        }
        return true;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
        return slot == 3;
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
        }
    }

    public int getDiFurnaceProgressScaled(int i) {
        return (progress * i) / processingSpeed;
    }

    public int getPowerRemainingScaled(int i) {
        return (fuel * i) / maxFuel;
    }

    public boolean canProcess() {

        ItemStack inputA = inventory.getStackInSlot(0);
        ItemStack inputB = inventory.getStackInSlot(1);

        if (inputA.isEmpty() || inputB.isEmpty()) return false;
        if (!this.hasPower()) return false;

        Tuple.Triplet<Integer, Integer, ItemStack> match = BlastFurnaceRecipes.getRequiredCounts(inputA, inputB);
        if (match == null) return false;

        int reqA = match.getX();
        int reqB = match.getY();
        ItemStack result = match.getZ();

        ItemStack simA = inventory.extractItem(0, reqA, true);
        if (simA.isEmpty() || simA.getCount() < reqA) return false;
        ItemStack simB = inventory.extractItem(1, reqB, true);
        if (simB.isEmpty() || simB.getCount() < reqB) return false;

        ItemStack remainder = inventory.insertItem(3, result.copy(), true);
        if (!remainder.isEmpty()) return false;

        ItemStack contA = inputA.getItem().getContainerItem(inputA);
        if (!contA.isEmpty()) {
            contA = contA.copy();
            contA.setCount(contA.getCount() * reqA);
            ItemStack rA = inventory.insertItem(3, contA, true);
            if (!rA.isEmpty()) return false;
        }
        ItemStack contB = inputB.getItem().getContainerItem(inputB);
        if (!contB.isEmpty()) {
            contB = contB.copy();
            contB.setCount(contB.getCount() * reqB);
            ItemStack rB = inventory.insertItem(3, contB, true);
            return rB.isEmpty();
        }

        return true;
    }

    private void processItem() {

        ItemStack slotA = inventory.getStackInSlot(0);
        ItemStack slotB = inventory.getStackInSlot(1);
        Tuple.Triplet<Integer, Integer, ItemStack> match = BlastFurnaceRecipes.getRequiredCounts(slotA, slotB);
        if (match == null) return;

        int reqA = match.getX();
        int reqB = match.getY();

        ItemStack usedA = inventory.extractItem(0, reqA, false);
        ItemStack usedB = inventory.extractItem(1, reqB, false);

        ItemStack result = match.getZ();
        if (!result.isEmpty()) {
            inventory.insertItem(3, result.copy(), false);
        }

        ItemStack contA = usedA.getItem().getContainerItem(usedA);
        if (!contA.isEmpty()) {
            contA = contA.copy();
            contA.setCount(contA.getCount() * usedA.getCount());
            inventory.insertItem(3, contA, false);
        }
        ItemStack contB = usedB.getItem().getContainerItem(usedB);
        if (!contB.isEmpty()) {
            contB = contB.copy();
            contB.setCount(contB.getCount() * usedB.getCount());
            inventory.insertItem(3, contB, false);
        }
    }

    public boolean hasPower() {
        return fuel > 0;
    }

    public boolean isProcessing() {
        return this.progress > 0;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeShort(this.progress);
        buf.writeShort(this.fuel);
        buf.writeBytes(new byte[]{this.sideFuel, this.sideUpper, this.sideLower});
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.progress = buf.readShort();
        this.fuel = buf.readShort();
        byte[] modes = new byte[3];
        buf.readBytes(modes);
        this.sideFuel = modes[0];
        this.sideUpper = modes[1];
        this.sideLower = modes[2];
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerDiFurnace(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIDiFurnace(player.inventory, this);
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[0];
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return this.getSmokeTanks();
    }

    private ItemStack getFuelStack() {
        return inventory.getStackInSlot(2);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if (Library.isSwappingBetweenVariants(oldState, newState, ModBlocks.machine_difurnace_off, ModBlocks.machine_difurnace_on)) return false;
        return super.shouldRefresh(world, pos, oldState, newState);
    }
}
