package com.hbm.tileentity;

import com.hbm.api.tile.IWorldRenameable;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.Spaghetti;
import com.hbm.lib.CapabilityContextProvider;
import com.hbm.lib.DirPos;
import com.hbm.lib.ItemStackHandlerWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

@Spaghetti("Not spaghetti in itself, but for the love of god please use this base class for all machines")
public abstract class TileEntityMachineBase extends TileEntityLoadedBase implements IWorldRenameable {
    /**
     * Internal inventory. All operations are unchecked.
     * Use {@link #getCheckedInventory()} for Container/External classes.
     * Consider making this protected in the future.
     */
    public ItemStackHandler inventory;
    private IItemHandlerModifiable checkedInventory;
    private boolean enablefluidWrapper = false;
    private boolean enableEnergyWrapper = false;
    private String customName;
    private boolean destroyedByCreativePlayer = false;

    @Deprecated
    public TileEntityMachineBase(int scount) {
        this(scount, 64);
    }

    @Deprecated
    public TileEntityMachineBase(int scount, int slotlimit) {
        inventory = getNewInventory(scount, slotlimit);
    }

    public TileEntityMachineBase(int scount, boolean enableFluidWrapper, boolean enableEnergyWrapper){
        this(scount);
        this.enablefluidWrapper = enableFluidWrapper;
        this.enableEnergyWrapper = enableEnergyWrapper;
    }

    public TileEntityMachineBase(int scount, int slotlimit, boolean enableFluidWrapper, boolean enableEnergyWrapper){
        this(scount, slotlimit);
        this.enablefluidWrapper = enableFluidWrapper;
        this.enableEnergyWrapper = enableEnergyWrapper;
    }

    protected ItemStackHandler getNewInventory(int scount, int slotlimit) {
        return new ItemStackHandler(scount) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slotlimit;
            }

        };
    }

    protected void resizeInventory(int newSlotCount) {
        ItemStackHandler newInventory = getNewInventory(newSlotCount, inventory.getSlotLimit(0));
        for (int i = 0; i < Math.min(inventory.getSlots(), newSlotCount); i++) {
            newInventory.setStackInSlot(i, inventory.getStackInSlot(i));
        }
        this.inventory = newInventory;
        markDirty();
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : getDefaultName();
    }

    public abstract String getDefaultName();

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    @Override
    public void setCustomName(String name) {
        this.customName = name;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 128;
        }
    }

    /**
     * It mimics the 1.7 IConditionalInvAccess behavior.
     *
     * @param side The side of the block being accessed.
     * @param accessorPos The position of the block DOING the accessing (the proxy).
     * @return An array of slots accessible from this proxy at this side. null -> full access. Empty array -> no access.
     */
    public int[] getAccessibleSlotsFromSide(EnumFacing side, BlockPos accessorPos) {
        return getAccessibleSlotsFromSide(side);
    }

    /**
     * @return An array of slots accessible at this side. null -> full access. Empty array -> no access.
     */
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[]{};
    }

    public int getGaugeScaled(int i, FluidTank tank) {
        return tank.getFluidAmount() * i / tank.getCapacity();
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(muffled);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.muffled = buf.readBoolean();
    }

    public void handleButtonPacket(int value, int meta) {
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", inventory.serializeNBT());
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("inventory"))
            inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        super.readFromNBT(compound);
    }

    /**
     * Checks if an item can be inserted into a slot.
     * <p>
     * Only affects the {@link IItemHandlerModifiable} obtained via {@link #getCheckedInventory()}
     * and the capability exposed externally.
     */
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return true;
    }

    /**
     * Checks if an item can be inserted into a slot from a specific side and accessor position.
     * Mimics the 1.7 IConditionalInvAccess behavior.
     * <p>
     * Only affects the capability exposed externally.
     */
    public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side, BlockPos accessorPos) {
        return canInsertItem(slot, stack);
    }

    /**
     * Only affects the capability exposed externally.
     */
    public boolean canInsertItem(int slot, ItemStack itemStack) {
        return this.isItemValidForSlot(slot, itemStack);
    }

    /**
     * Checks if an item can be extracted from a slot from a specific side and accessor position.
     * Mimics the 1.7 IConditionalInvAccess behavior.
     * <p>
     * Only affects the capability exposed externally.
     */
    public boolean canExtractItem(int slot, ItemStack stack, int amount, EnumFacing side, BlockPos accessorPos) {
        return canExtractItem(slot, stack, amount);
    }

    /**
     * Only affects the capability exposed externally.
     */
    public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
        return true;
    }

    public int countMufflers() {

        int count = 0;

        for (EnumFacing dir : EnumFacing.VALUES)
            if (world.getBlockState(pos.offset(dir)).getBlock() == ModBlocks.muffler)
                count++;

        return count;
    }

    public float getVolume(int toSilence) {

        float volume = 1 - (countMufflers() / (float) toSilence);

        return Math.max(volume, 0);
    }

    /**
     * @return a checked wrapper around the inventory. Intended for Container and GUI class.
     */
    public IItemHandlerModifiable getCheckedInventory() {
        if (checkedInventory == null)
            checkedInventory = new CheckedInventory();
        return checkedInventory;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        // Contract: facing == null -> internal
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && enablefluidWrapper) {
            BlockPos accessorPos = facing == null ? null : CapabilityContextProvider.getAccessor(this.pos);
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new NTMFluidHandlerWrapper(this, accessorPos));
        } else if (capability == CapabilityEnergy.ENERGY && enableEnergyWrapper) {
            BlockPos accessorPos = facing == null ? null : CapabilityContextProvider.getAccessor(this.pos);
            return CapabilityEnergy.ENERGY.cast(new NTMEnergyCapabilityWrapper(this, accessorPos));
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null) {
            if (facing == null)
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
            final BlockPos accessorPos = CapabilityContextProvider.getAccessor(this.pos);
            final EnumFacing side = facing;
            int[] accessibleSlots = getAccessibleSlotsFromSide(side, accessorPos);
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemStackHandlerWrapper(inventory, accessibleSlots) {
                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return isItemValidForSlot(slot, stack);
                }

                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (canExtractItem(slot, inventory.getStackInSlot(slot), amount, side, accessorPos)) {
                        return super.extractItem(slot, amount, simulate);
                    }
                    return ItemStack.EMPTY;
                }

                @Override
                public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                    if (canInsertItem(slot, stack, side, accessorPos)) {
                        return super.insertItem(slot, stack, simulate);
                    }
                    return stack;
                }
            });
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && enablefluidWrapper) return true;
        if (capability == CapabilityEnergy.ENERGY && enableEnergyWrapper) return true;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null) {
            if (facing == null) return true;
            BlockPos accessorPos = CapabilityContextProvider.getAccessor(this.pos);
            int[] accessible = getAccessibleSlotsFromSide(facing, accessorPos);
            return accessible == null || accessible.length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    protected void updateRedstoneConnection(DirPos pos) {
        BlockPos blockPos = pos.getPos();
        IBlockState state1 = world.getBlockState(blockPos);
        Block block1 = state1.getBlock();

        block1.onNeighborChange(world, blockPos, this.getPos());

        block1.neighborChanged(state1, world, blockPos, this.getBlockType(), this.getPos());

        if (state1.isNormalCube()) {
            BlockPos offsetPos = blockPos.offset(pos.getDir().toEnumFacing());
            Block block2 = world.getBlockState(offsetPos).getBlock();

            if (block2.getWeakChanges(world, offsetPos)) {
                block2.onNeighborChange(world, offsetPos, this.getPos());
                block2.neighborChanged(world.getBlockState(offsetPos), world, offsetPos, this.getBlockType(), this.getPos());
            }
        }
    }

    public void setDestroyedByCreativePlayer() {
        destroyedByCreativePlayer = true;
    }

    public boolean isDestroyedByCreativePlayer() {
        return destroyedByCreativePlayer;
    }

    private final class CheckedInventory implements IItemHandlerModifiable {
        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            if (!isItemValidForSlot(slot, stack)) return stack;
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) return ItemStack.EMPTY;
            return inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            inventory.setStackInSlot(slot, stack);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isItemValidForSlot(slot, stack);
        }
    }
}
