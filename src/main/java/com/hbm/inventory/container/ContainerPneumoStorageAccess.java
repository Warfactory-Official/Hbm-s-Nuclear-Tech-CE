package com.hbm.inventory.container;

import com.hbm.api.ntl.StackCache;
import com.hbm.api.ntl.StackCache.CacheSlot;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.tileentity.network.TileEntityPneumoStorageAccess;
import com.hbm.util.InventoryUtil;
import com.hbm.util.ItemStackUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ContainerPneumoStorageAccess extends Container {

    public static final int DISPLAY_ROWS = 6;
    public static final int DISPLAY_COLUMNS = 8;
    public static final int DISPLAY_SLOTS = DISPLAY_ROWS * DISPLAY_COLUMNS;

    public static final int SORT_STACK_SIZE = 0;
    public static final int SORT_ID = 1;
    public static final int SORT_LOCALIZED = 2;
    public static final int SORT_INTERNAL = 3;

    public static final String STACK_SIZE_KEY = "PNEUMO_STACK_SIZE";
    /** Holds the stack's own display tag while the grid overwrites it with the amount label */
    public static final String ORIGINAL_DISPLAY_KEY = "PNEUMO_DISPLAY";

    /**
     * Grid stacks carry the total amount as a tag so that the vanilla slot sync can ship it to the client.
     * That tag is part of the stack's identity as far as the stack cache is concerned, so it has to be
     * stripped again before the stack is used for any lookup, insertion or extraction.
     */
    public static ItemStack toDisplayStack(CacheSlot cacheSlot) {
        ItemStack display = cacheSlot.displayStack.copy();

        if (display.hasTagCompound() && display.getTagCompound().hasKey("display")) {
            display.getTagCompound().setTag(ORIGINAL_DISPLAY_KEY, display.getTagCompound().getTag("display").copy());
        }

        ItemStackUtil.addTooltipToStack(display, "x" + cacheSlot.stacksize, "in " + cacheSlot.monitors.size() + " stacks");
        display.getTagCompound().setLong(STACK_SIZE_KEY, cacheSlot.stacksize);

        return display;
    }

    public static ItemStack toRealStack(ItemStack display) {
        ItemStack stack = display.copy();
        stack.setCount(1);

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return stack;

        tag.removeTag(STACK_SIZE_KEY);

        if (tag.hasKey(ORIGINAL_DISPLAY_KEY)) {
            tag.setTag("display", tag.getTag(ORIGINAL_DISPLAY_KEY));
            tag.removeTag(ORIGINAL_DISPLAY_KEY);
        } else {
            tag.removeTag("display");
        }

        if (tag.isEmpty()) stack.setTagCompound(null);

        return stack;
    }

    public static long getDisplayedAmount(ItemStack display) {
        if (!display.hasTagCompound()) return 0;
        return display.getTagCompound().getLong(STACK_SIZE_KEY);
    }

    protected TileEntityPneumoStorageAccess access;
    protected InventoryPneumoStorageAccess inventory;
    protected EntityPlayer player;

    protected int sorting = SORT_STACK_SIZE;
    protected String searchString = "";
    protected boolean detailedSearch = false;
    protected int listingStart = 0;

    private int stackCount = 0;
    private int lastStackCount = -1;

    public ContainerPneumoStorageAccess(InventoryPlayer invPlayer, TileEntityPneumoStorageAccess access) {
        this.access = access;
        this.player = invPlayer.player;
        this.inventory = new InventoryPneumoStorageAccess(access);

        InvWrapper displayWrapper = new InvWrapper(inventory);
        for (int i = 0; i < DISPLAY_ROWS; i++) {
            for (int j = 0; j < DISPLAY_COLUMNS; j++) {
                this.addSlotToContainer(new SlotPneumo(displayWrapper, j + i * DISPLAY_COLUMNS, 34 + 8 + j * 18, 17 + i * 18));
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 34 + 8 + j * 18, 169 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 34 + 8 + i * 18, 227));
        }

        this.rebuildListing();
        this.detectAndSendChanges();
    }

    public TileEntityPneumoStorageAccess getAccess() {
        return this.access;
    }

    public int getStackCount() {
        return this.stackCount;
    }

    public int getListingStart() {
        return this.listingStart;
    }

    public int getSorting() {
        return this.sorting;
    }

    public boolean isDetailedSearch() {
        return this.detailedSearch;
    }

    public void setSorting(int sorting) {
        this.sorting = sorting < 0 || sorting > SORT_INTERNAL ? SORT_STACK_SIZE : sorting;
        this.listingStart = 0;
        this.rebuildListing();
    }

    public void setSearchString(String search) {
        this.searchString = search.toLowerCase(Locale.US);
        this.listingStart = 0;
        this.rebuildListing();
    }

    public void setDetailedSearch(boolean detailed) {
        this.detailedSearch = detailed;
        this.listingStart = 0;
        this.rebuildListing();
    }

    public void setListingStart(int start) {
        this.listingStart = Math.max(0, start);
        this.rebuildListing();
    }

    public void rebuildListing() {
        this.stackCount = this.inventory.updateListing(this.getSorter(), this.searchString, this.detailedSearch, this.listingStart, this.player);
    }

    protected Comparator<CacheSlot> getSorter() {
        switch (this.sorting) {
            case SORT_ID: return SORT_BY_ID;
            case SORT_LOCALIZED: return SORT_BY_LOCALIZED;
            case SORT_INTERNAL: return SORT_BY_INTERNAL;
            default: return SORT_BY_STACK_SIZE;
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (!this.access.getWorld().isRemote) this.rebuildListing();

        super.detectAndSendChanges();

        if (this.lastStackCount != this.stackCount) {
            this.lastStackCount = this.stackCount;
            for (IContainerListener listener : this.listeners) {
                listener.sendWindowProperty(this, 0, this.stackCount);
            }
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, this.stackCount);
    }

    @Override
    public void updateProgressBar(int id, int value) {
        if (id == 0) this.stackCount = value;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(access.getPos().getX() + 0.5D, access.getPos().getY() + 0.5D, access.getPos().getZ() + 0.5D) <= 15D * 15D;
    }

    @Override
    public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {

        boolean client = player.world.isRemote;

        // the grid is virtual, so every interaction with it is resolved on the server and synced back
        if (index >= 0 && index < DISPLAY_SLOTS) {

            if (client || (mode != ClickType.PICKUP && mode != ClickType.QUICK_MOVE)) return ItemStack.EMPTY;

            StackCache cache = this.access.cache;
            if (cache == null || cache.hasExpired) return ItemStack.EMPTY;

            ItemStack held = player.inventory.getItemStack();

            // dropping a held stack onto the grid deposits it, right click deposits a single item
            if (mode == ClickType.PICKUP && !held.isEmpty()) {
                int toDeposit = button == 1 ? 1 : held.getCount();
                int leftover = (int) cache.addItemsAndReturnQuantity(held, toDeposit);
                held.shrink(toDeposit - leftover);
                player.inventory.setItemStack(held.isEmpty() ? ItemStack.EMPTY : held);
                this.detectAndSendChanges();
                return ItemStack.EMPTY;
            }

            Slot slot = this.getSlot(index);
            if (!slot.getHasStack()) return ItemStack.EMPTY;

            ItemStack request = toRealStack(slot.getStack());
            long available = getDisplayedAmount(slot.getStack());
            if (available <= 0) return ItemStack.EMPTY;

            int toGrab = (int) Math.min(request.getMaxStackSize(), available);
            if (mode == ClickType.PICKUP && button == 1) toGrab = 1;

            int grabbed = (int) cache.consumeItemsAndReturnQuantity(request, toGrab);
            if (grabbed <= 0) return ItemStack.EMPTY;

            ItemStack payload = request.copy();
            payload.setCount(grabbed);

            if (mode == ClickType.QUICK_MOVE) {
                ItemStack remainder = InventoryUtil.tryAddItemToInventory(player.inventory.mainInventory, payload);
                // whatever doesn't fit goes right back into the system instead of being voided
                if (!remainder.isEmpty()) cache.addItemsAndReturnQuantity(remainder, remainder.getCount());
            } else {
                player.inventory.setItemStack(payload);
            }

            this.detectAndSendChanges();
            return ItemStack.EMPTY;
        }

        // shift clicking out of the player inventory deposits into the system
        if (mode == ClickType.QUICK_MOVE && index >= DISPLAY_SLOTS && index < this.inventorySlots.size()) {

            if (client) return ItemStack.EMPTY;

            StackCache cache = this.access.cache;
            if (cache == null || cache.hasExpired) return ItemStack.EMPTY;

            Slot slot = this.getSlot(index);
            if (!slot.getHasStack()) return ItemStack.EMPTY;

            ItemStack stack = slot.getStack();
            int leftover = (int) cache.addItemsAndReturnQuantity(stack, stack.getCount());

            if (leftover < stack.getCount()) {
                slot.decrStackSize(stack.getCount() - leftover);
                slot.onSlotChanged();
            }

            this.detectAndSendChanges();
            return ItemStack.EMPTY;
        }

        return super.slotClick(index, button, mode, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return ItemStack.EMPTY;
    }

    public static final Comparator<CacheSlot> SORT_BY_STACK_SIZE = (o1, o2) -> {
        if (o1.stacksize > o2.stacksize)        return -1;  if (o1.stacksize < o2.stacksize)        return 1;
        if (o1.itemId < o2.itemId)              return -1;  if (o1.itemId > o2.itemId)              return 1;
        if (o1.meta < o2.meta)                  return -1;  if (o1.meta > o2.meta)                  return 1;
        if (o1.nbt == null && o2.nbt != null)   return -1;  if (o1.nbt != null && o2.nbt == null)   return 1;
        return 0;
    };

    public static final Comparator<CacheSlot> SORT_BY_ID = (o1, o2) -> {
        if (o1.itemId < o2.itemId)              return -1;  if (o1.itemId > o2.itemId)              return 1;
        if (o1.meta < o2.meta)                  return -1;  if (o1.meta > o2.meta)                  return 1;
        if (o1.stacksize > o2.stacksize)        return -1;  if (o1.stacksize < o2.stacksize)        return 1;
        if (o1.nbt == null && o2.nbt != null)   return -1;  if (o1.nbt != null && o2.nbt == null)   return 1;
        return 0;
    };

    public static final Comparator<CacheSlot> SORT_BY_INTERNAL = (o1, o2) -> {
        if (o1.displayStack == null || o2.displayStack == null) return SORT_BY_ID.compare(o1, o2);
        String name1 = o1.displayStack.getItem().getTranslationKey(o1.displayStack);
        String name2 = o2.displayStack.getItem().getTranslationKey(o2.displayStack);
        int compare = name1.compareToIgnoreCase(name2);
        if (compare != 0) return compare;
        return SORT_BY_ID.compare(o1, o2);
    };

    public static final Comparator<CacheSlot> SORT_BY_LOCALIZED = (o1, o2) -> {
        if (o1.displayStack == null || o2.displayStack == null) return SORT_BY_ID.compare(o1, o2);
        String name1 = o1.displayStack.getDisplayName();
        String name2 = o2.displayStack.getDisplayName();
        int compare = name1.compareToIgnoreCase(name2);
        if (compare != 0) return compare;
        return SORT_BY_ID.compare(o1, o2);
    };

    public static class SlotPneumo extends SlotNonRetarded {

        public long amount;

        public SlotPneumo(net.minecraftforge.items.IItemHandler inventory, int id, int x, int y) {
            super(inventory, id, x, y);
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return true;
        }
    }

    /** This inventory instance only exists to prepare the contents of a StackCache in such a way that we can use it in a container. */
    public static class InventoryPneumoStorageAccess implements IInventory {

        public StackCache cache;
        public ItemStack[] slots;

        private final TileEntityPneumoStorageAccess access;

        public InventoryPneumoStorageAccess(TileEntityPneumoStorageAccess access) {
            this.slots = new ItemStack[getSizeInventory()];
            for (int i = 0; i < slots.length; i++) slots[i] = ItemStack.EMPTY;
            this.access = access;
            this.cache = access.cache;
        }

        public int updateListing(Comparator<CacheSlot> sorter, String search, boolean detailed, int listingStart, EntityPlayer player) {

            this.cache = access.cache;
            this.clear();

            if (this.cache == null) return 0;

            List<CacheSlot> cacheSlots = new ArrayList<>(cache.cacheSlots.size());
            cacheSlots.addAll(cache.cacheSlots.values());
            cacheSlots.removeIf(x -> x.stacksize <= 0 || x.displayStack == null);
            if (!search.isEmpty()) cacheSlots.removeIf(x -> !matches(x, search, detailed, player));
            cacheSlots.sort(sorter);

            int size = cacheSlots.size();
            int offset = listingStart * DISPLAY_COLUMNS;

            for (int i = 0; i < slots.length; i++) {
                int index = i + offset;
                if (index >= size) break;

                CacheSlot cacheSlot = cacheSlots.get(index);
                slots[i] = toDisplayStack(cacheSlot);
            }

            return size;
        }

        private static boolean matches(CacheSlot slot, String search, boolean detailed, EntityPlayer player) {

            if (slot.displayStack.getDisplayName().toLowerCase(Locale.US).contains(search)) return true;

            if (detailed) {
                try {
                    List<String> tooltip = slot.displayStack.getTooltip(player, ITooltipFlag.TooltipFlags.NORMAL);
                    for (String line : tooltip) {
                        if (line.toLowerCase(Locale.US).contains(search)) return true;
                    }
                } catch (Exception ignored) { }
            }

            return false;
        }

        @Override public int getSizeInventory() { return DISPLAY_SLOTS; }
        @Override public ItemStack getStackInSlot(int slot) { return slots[slot]; }
        @Override public int getInventoryStackLimit() { return 64; }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : slots) if (!stack.isEmpty()) return false;
            return true;
        }

        @Override public ItemStack decrStackSize(int slot, int amount) { return ItemStack.EMPTY; }

        @Override
        public ItemStack removeStackFromSlot(int slot) {
            ItemStack stack = slots[slot];
            slots[slot] = ItemStack.EMPTY;
            return stack;
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            this.slots[slot] = stack;
        }

        @Override public String getName() { return "null"; }
        @Override public boolean hasCustomName() { return false; }
        @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }
        @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return false; }

        @Override public void markDirty() { }

        @Override public boolean isUsableByPlayer(EntityPlayer player) { return true; }

        @Override public void openInventory(EntityPlayer player) { }
        @Override public void closeInventory(EntityPlayer player) { }

        @Override public int getField(int id) { return 0; }
        @Override public void setField(int id, int value) { }
        @Override public int getFieldCount() { return 0; }

        @Override
        public void clear() {
            for (int i = 0; i < slots.length; i++) slots[i] = ItemStack.EMPTY;
        }
    }
}
