package com.hbm.entity.train;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public abstract class EntityRailCarCargo extends EntityRailCarBase implements IInventory {

    protected static final DataParameter<Integer> OCCUPIED_SLOTS = EntityDataManager.createKey(EntityRailCarCargo.class, DataSerializers.VARINT);

    protected String entityName;
    protected NonNullList<ItemStack> slots = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);

    public EntityRailCarCargo(World world) {
        super(world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(OCCUPIED_SLOTS, 0);
    }

    public int countOccupiedSlots() {
        int count = 0;

        for(int i = 0; i < this.getSizeInventory(); i++) {
            if(!this.getStackInSlot(i).isEmpty()) count++;
        }

        return count;
    }

    public int getOccupiedSlots() {
        return this.dataManager.get(OCCUPIED_SLOTS);
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return slots.get(slot);
    }

    @Override
    public @NotNull ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = slots.get(slot);

        if(stack.isEmpty()) return ItemStack.EMPTY;

        if(stack.getCount() <= amount) {
            slots.set(slot, ItemStack.EMPTY);
            return stack;
        }

        ItemStack split = stack.splitStack(amount);

        if(stack.isEmpty()) {
            slots.set(slot, ItemStack.EMPTY);
        }

        return split;
    }

    @Override
    public @NotNull ItemStack removeStackFromSlot(int slot) {
        ItemStack stack = slots.get(slot);
        slots.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, @NotNull ItemStack stack) {
        slots.set(slot, stack);

        if(!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack stack : slots) {
            if(!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(!this.world.isRemote) this.dataManager.set(OCCUPIED_SLOTS, this.countOccupiedSlots());
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() { }

    @Override
    public boolean isUsableByPlayer(@NotNull EntityPlayer player) {
        return !this.isDead && player.getDistanceSq(this) <= 64.0D;
    }

    @Override
    public void openInventory(@NotNull EntityPlayer player) { }

    @Override
    public void closeInventory(@NotNull EntityPlayer player) { }

    @Override
    public boolean isItemValidForSlot(int slot, @NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) { }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        slots.clear();
    }

    @Override
    protected void writeEntityToNBT(@NotNull NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        NBTTagList list = new NBTTagList();

        for(int i = 0; i < this.slots.size(); ++i) {
            if(!this.slots.get(i).isEmpty()) {
                NBTTagCompound slot = new NBTTagCompound();
                slot.setByte("Slot", (byte) i);
                this.slots.get(i).writeToNBT(slot);
                list.appendTag(slot);
            }
        }

        nbt.setTag("Items", list);
    }

    @Override
    protected void readEntityFromNBT(@NotNull NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        NBTTagList list = nbt.getTagList("Items", 10);
        this.slots = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);

        for(int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound slot = list.getCompoundTagAt(i);
            int j = slot.getByte("Slot") & 255;

            if(j >= 0 && j < this.slots.size()) {
                this.slots.set(j, new ItemStack(slot));
            }
        }

        this.dataManager.set(OCCUPIED_SLOTS, this.countOccupiedSlots());
    }

    @Override
    public boolean hasCustomName() {
        return this.entityName != null;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public void setEntityName(String name) {
        this.entityName = name;
    }

    @Override
    public @NotNull String getName() {
        return this.entityName != null ? this.entityName : super.getName();
    }

    @Override
    public @NotNull ITextComponent getDisplayName() {
        return new TextComponentString(this.getName());
    }
}
