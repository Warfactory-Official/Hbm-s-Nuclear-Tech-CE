package com.hbm.tileentity.machine.storage;

import com.hbm.api.tile.ILootContainerModifiable;
import com.hbm.api.tile.IWorldRenameable;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ItemStackHandlerWrapper;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Now with loot table support!
 */
public abstract class TileEntityCrateBase extends TileEntityLockableBase implements IWorldRenameable, ILootContainerModifiable {

    public ItemStackHandler inventory;
    public String customName;
    protected @Nullable ResourceLocation lootTable;
    protected long lootTableSeed;

    public TileEntityCrateBase(int scount) {
        this(scount, 64);
    }

    public TileEntityCrateBase(int scount, int slotlimit) {
        inventory = getNewInventory(scount, slotlimit);
    }

    private static List<Integer> getEmptySlotsRandomized(ItemStackHandler inv, Random rand) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).isEmpty()) list.add(i);
        }
        Collections.shuffle(list, rand);
        return list;
    }

    private static void shuffleItems(List<ItemStack> stacks, int emptySlotsCount, Random rand) {
        List<ItemStack> splitCandidates = new ArrayList<>();
        for (int i = stacks.size() - 1; i >= 0; i--) {
            ItemStack s = stacks.get(i);
            if (s.isEmpty()) {
                stacks.remove(i);
            } else if (s.getCount() > 1) {
                splitCandidates.add(s);
                stacks.remove(i);
            }
        }

        int needed = emptySlotsCount - stacks.size();
        while (needed > 0 && !splitCandidates.isEmpty()) {
            ItemStack src = splitCandidates.remove(rand.nextInt(splitCandidates.size()));
            int take = 1 + rand.nextInt(src.getCount() / 2);
            ItemStack part = src.splitStack(take);

            if (src.getCount() > 1 && rand.nextBoolean()) splitCandidates.add(src);
            else stacks.add(src);
            if (part.getCount() > 1 && rand.nextBoolean()) splitCandidates.add(part);
            else stacks.add(part);

            needed = emptySlotsCount - stacks.size();
        }

        stacks.addAll(splitCandidates);
        Collections.shuffle(stacks, rand);
    }

    protected ItemStackHandler getNewInventory(int scount, int slotlimit) {
        return new ItemStackHandler(scount) {
            @Override
            public ItemStack getStackInSlot(int slot) {
                ensureFilled();
                return super.getStackInSlot(slot);
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                ensureFilled();
                super.setStackInSlot(slot, stack);
            }

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

    public ItemStack getStackInSlotOnClosing(int i) {
        if (!inventory.getStackInSlot(i).isEmpty()) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            inventory.setStackInSlot(i, ItemStack.EMPTY);
            return itemStack;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    @Override
    public void setCustomName(String name) {
        this.customName = name;
    }

    @Override
    public @Nullable ResourceLocation getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(ResourceLocation table, long seed) {
        this.lootTable = table;
        this.lootTableSeed = seed;
    }

    @Override
    public void fillWithLoot(@Nullable EntityPlayer player) {
        if (this.lootTable == null) return;
        if (!(this.world instanceof WorldServer)) return;

        LootTable table = this.world.getLootTableManager().getLootTableFromLocation(this.lootTable);
        this.lootTable = null;

        final Random rng = (this.lootTableSeed == 0L) ? new Random() : new Random(this.lootTableSeed);
        LootContext.Builder ctx = new LootContext.Builder((WorldServer) this.world);
        if (player != null) {
            ctx.withLuck(player.getLuck()).withPlayer(player);
        }
        List<ItemStack> drops = table.generateLootForPools(rng, ctx.build());
        List<Integer> empties = getEmptySlotsRandomized(inventory, rng);
        shuffleItems(drops, empties.size(), rng);

        for (ItemStack stack : drops) {
            if (empties.isEmpty()) break;
            int slot = empties.remove(empties.size() - 1);
            inventory.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
        }

        markDirty();
    }

    protected void ensureFilled() {
        if (this.lootTable != null && !this.world.isRemote) {
            fillWithLoot(null);
        }
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) return false;
        return player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }

    /**
     * {@inheritDoc}
     * @implNote this method is only called client-side. Use com.hbm.interfaces.IContainerOpenEventListener to make it work server-side.
     */
    public void openInventory(EntityPlayer player) {
        player.world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.crateOpen, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
    }

    /**
     * {@inheritDoc}
     * @implNote this method is only called client-side. Override Container#onContainerClosed to make it work server-side.
     */
    public void closeInventory(EntityPlayer player) {
        player.world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.crateClose, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
    }

    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (!checkLootAndRead(compound)) {
            if (compound.hasKey("inventory")) {
                inventory.deserializeNBT(compound.getCompoundTag("inventory"));
            }
        }
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (!checkLootAndWrite(compound)) {
            compound.setTag("inventory", inventory.serializeNBT());
        }
        return super.writeToNBT(compound);
    }

    protected boolean checkLootAndRead(NBTTagCompound nbt) {
        if (nbt.hasKey("LootTable")) {
            this.lootTable = new ResourceLocation(nbt.getString("LootTable"));
            this.lootTableSeed = nbt.getLong("LootTableSeed");
            return true;
        }
        return false;
    }

    protected boolean checkLootAndWrite(NBTTagCompound nbt) {
        if (this.lootTable != null) {
            nbt.setString("LootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0L) {
                nbt.setLong("LootTableSeed", this.lootTableSeed);
            }
            return true;
        }
        return false;
    }

    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        int[] slots = new int[this.inventory.getSlots()];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
        return slots;
    }

    public boolean canInsertItem(int slot, ItemStack itemStack, int amount) {
        return this.isItemValidForSlot(slot, itemStack);
    }

    public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
        return true;
    }

    /**
     * Somehow in this base class the lock isn't checked for capabilities.
     * Override this if that behavior is desired.
     */
    protected boolean checkLock(EnumFacing facing){
        return true;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null && checkLock(facing)) {
            ensureFilled();
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemStackHandlerWrapper(inventory, facing == null ? null : getAccessibleSlotsFromSide(facing)) {
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    ensureFilled();
                    if (facing == null || canExtractItem(slot, inventory.getStackInSlot(slot), amount))
                        return super.extractItem(slot, amount, simulate);
                    return ItemStack.EMPTY;
                }

                @Override
                public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                    ensureFilled();
                    if (facing == null || canInsertItem(slot, stack, stack.getCount()))
                        return super.insertItem(slot, stack, simulate);
                    return stack;
                }
            });
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory != null && checkLock(facing)) || super.hasCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }
}
