package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineDiFurnaceRTG;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerDiFurnaceRTG;
import com.hbm.inventory.gui.GUIDiFurnaceRTG;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.RTGUtil;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityDiFurnaceRTG extends TileEntityMachineBase implements ITickable, ICapabilityProvider, IGUIProvider {

    public static final int maxRTGPower = 6000;
    private static final short progressRequired = 2400;
    private static final int[] slots_top = new int[]{0, 1};
    private static final int[] slots_bottom = new int[]{2};
    private static final int[] slots_side = new int[]{3, 4, 5, 6, 7, 8};
    public int rtgPower;
    public short progress;
    private boolean lastTrigger = false;

    public TileEntityDiFurnaceRTG() {
        super(9);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.progress = compound.getShort("progress");
        this.rtgPower = compound.getInteger("rtgPower");
        if (compound.hasKey("inventory")) inventory.deserializeNBT((NBTTagCompound) compound.getTag("inventory"));
        super.readFromNBT(compound);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setShort("progress", progress);
        compound.setInteger("rtgPower", rtgPower);
        compound.setTag("inventory", inventory.serializeNBT());
        return super.writeToNBT(compound);
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            rtgPower = Math.min(RTGUtil.updateRTGs(inventory, new int[]{3, 4, 5, 6, 7, 8}), maxRTGPower);

            if (hasPower() && canProcess()) {
                progress += rtgPower;
                if (progress >= progressRequired) {
                    processItem();
                    progress = 0;
                }
            } else {
                progress = 0;
            }


            boolean trigger = isProcessing() || (canProcess() && hasPower());
            if (trigger != lastTrigger) MachineDiFurnaceRTG.updateBlockState(trigger, this.world, pos);
            lastTrigger = trigger;

            networkPackNT(10);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeShort(progress);
        buf.writeInt(rtgPower);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        progress = buf.readShort();
        rtgPower = buf.readInt();
    }

    @Override
    public String getDefaultName() {
        return "container.diFurnaceRTG";
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        int i = e.ordinal();
        return i == 0 ? slots_bottom : (i == 1 ? slots_top : slots_side);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return i != 2;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack) {
        if (slot == 0 && isItemValidForSlot(slot, itemStack)) return inventory.getStackInSlot(1).getItem() != itemStack.getItem();
        if (slot == 1 && isItemValidForSlot(slot, itemStack)) return inventory.getStackInSlot(0).getItem() != itemStack.getItem();
        return isItemValidForSlot(slot, itemStack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
        return slot == 2;
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
        }
    }

    public int getDiFurnaceProgressScaled(int i) {
        return (progress * i) / progressRequired;
    }

    public int getPowerRemainingScaled(int i) {
        return (rtgPower * i) / maxRTGPower;
    }

    public int getPower() {
        return rtgPower;
    }

    public boolean canProcess() {
        ItemStack in0 = inventory.getStackInSlot(0);
        ItemStack in1 = inventory.getStackInSlot(1);
        if (in0.isEmpty() || in1.isEmpty()) return false;
        if (!hasPower()) return false;

        Tuple.Triplet<Integer, Integer, ItemStack> match = BlastFurnaceRecipes.getRequiredCounts(in0, in1);
        if (match == null) return false;

        int req0 = match.getX();
        int req1 = match.getY();
        if (in0.getCount() < req0 || in1.getCount() < req1) return false;

        ItemStack recipeResult = match.getZ();
        ItemStack outputStack = inventory.getStackInSlot(2);
        if (outputStack.isEmpty()) {
            return recipeResult.getCount() <= inventory.getSlotLimit(2) && recipeResult.getCount() <= recipeResult.getMaxStackSize();
        }
        if (!outputStack.isItemEqual(recipeResult)) return false;
        int newCount = outputStack.getCount() + recipeResult.getCount();
        return newCount <= inventory.getSlotLimit(2) && newCount <= outputStack.getMaxStackSize();
    }

    private void processItem() {
        if (!canProcess()) return;

        ItemStack in0 = inventory.getStackInSlot(0);
        ItemStack in1 = inventory.getStackInSlot(1);
        Tuple.Triplet<Integer, Integer, ItemStack> match = BlastFurnaceRecipes.getRequiredCounts(in0, in1);
        if (match == null) return;

        int req0 = match.getX();
        int req1 = match.getY();
        ItemStack recipeResult = match.getZ();

        ItemStack outputStack = inventory.getStackInSlot(2);
        if (outputStack.isEmpty()) {
            inventory.setStackInSlot(2, recipeResult.copy());
        } else if (outputStack.isItemEqual(recipeResult)) {
            ItemStack newOutput = outputStack.copy();
            newOutput.grow(recipeResult.getCount());
            inventory.setStackInSlot(2, newOutput);
        }

        if (!in0.isEmpty()) {
            ItemStack new0 = in0.copy();
            new0.shrink(req0);
            if (new0.getCount() <= 0) new0 = ItemStack.EMPTY;
            inventory.setStackInSlot(0, new0);
        }
        if (!in1.isEmpty()) {
            ItemStack new1 = in1.copy();
            new1.shrink(req1);
            if (new1.getCount() <= 0) new1 = ItemStack.EMPTY;
            inventory.setStackInSlot(1, new1);
        }

        this.markDirty();
    }

    public boolean hasPower() {
        return rtgPower > 0;
    }

    public boolean isProcessing() {
        return this.progress > 0;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerDiFurnaceRTG(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIDiFurnaceRTG(player.inventory, this);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if (Library.isSwappingBetweenVariants(oldState, newState, ModBlocks.machine_difurnace_rtg_off, ModBlocks.machine_difurnace_rtg_on)) return false;
        return super.shouldRefresh(world, pos, oldState, newState);
    }
}