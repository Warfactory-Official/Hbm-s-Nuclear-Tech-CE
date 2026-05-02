package com.hbm.tileentity.machine;

import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.inventory.container.ContainerCrateBase;
import com.hbm.inventory.gui.GUICrateBase;
import com.hbm.hazard.HazardSystem;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemKeyPin;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.InventoryHelper;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.machine.storage.TileEntityCrateBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityCrate extends TileEntityCrateBase implements IGUIProvider, IPersistentNBT {

    protected boolean suppressInventoryCallbacks = false;
    protected String name;
    private final int crateColumns;
    private final int crateRows;
    private final int crateX;
    private final int crateY;
    private final int playerInventoryX;
    private final int playerInventoryY;
    private final int hotbarY;
    private final int guiWidth;
    private final int guiHeight;
    private final int inventoryLabelX;
    private final int titleColor;
    private final int inventoryLabelColor;
    private final ResourceLocation texture;
    private boolean destroyedByCreativePlayer = false;

    private record CrateDropData(NBTTagCompound persistentData, double radiation) {
    }

    public TileEntityCrate(int scount, String name, int crateColumns, int crateRows, int crateX, int crateY,
                           int playerInventoryX, int playerInventoryY, int hotbarY, int guiWidth, int guiHeight,
                           int inventoryLabelX, int titleColor, int inventoryLabelColor, ResourceLocation texture) {
        super(scount);
        this.name = name;
        this.crateColumns = crateColumns;
        this.crateRows = crateRows;
        this.crateX = crateX;
        this.crateY = crateY;
        this.playerInventoryX = playerInventoryX;
        this.playerInventoryY = playerInventoryY;
        this.hotbarY = hotbarY;
        this.guiWidth = guiWidth;
        this.guiHeight = guiHeight;
        this.inventoryLabelX = inventoryLabelX;
        this.titleColor = titleColor;
        this.inventoryLabelColor = inventoryLabelColor;
        this.texture = texture;
    }

    @Override
    protected ItemStackHandler getNewInventory(int scount, int slotlimit) {
        return new ItemStackHandler(scount) {
            @Override
            public @NotNull ItemStack getStackInSlot(int slot) {
                ensureFilled();
                return super.getStackInSlot(slot);
            }

            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                ensureFilled();
                super.setStackInSlot(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                if (suppressInventoryCallbacks) {
                    return;
                }
                markDirty();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slotlimit;
            }
        };
    }

    @Override
    public void fillWithLoot(@Nullable EntityPlayer player) {
        if (this.lootTable == null || !(this.world instanceof WorldServer)) {
            return;
        }

        suppressInventoryCallbacks = true;
        try {
            super.fillWithLoot(player);
        } finally {
            suppressInventoryCallbacks = false;
        }

        markDirty();
    }

    private static void applyDropData(NBTTagCompound nbt, CrateDropData data) {
        if (!data.persistentData.isEmpty()) {
            nbt.setTag(NBT_PERSISTENT_KEY, data.persistentData);
        } else {
            nbt.removeTag(NBT_PERSISTENT_KEY);
        }

        if (data.radiation > 0D) {
            nbt.setDouble(BlockStorageCrate.CRATE_RAD_KEY, data.radiation);
        } else {
            nbt.removeTag(BlockStorageCrate.CRATE_RAD_KEY);
        }
    }

    protected final void ejectAndClearInventory() {
        InventoryHelper.dropInventoryItems(world, pos, this);
        suppressInventoryCallbacks = true;
        try {
            for (int i = 0; i < inventory.getSlots(); i++) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        } finally {
            suppressInventoryCallbacks = false;
        }
        super.markDirty();
    }

    public long getSize() {
        return Library.getCompressedNbtSize(assembleDropTag(buildDropData()));
    }

    private CrateDropData buildDropData() {
        NBTTagCompound persistentData = new NBTTagCompound();
        double radiation = 0D;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            radiation += HazardSystem.getTotalRadsFromStack(stack) * stack.getCount();
            NBTTagCompound slot = new NBTTagCompound();
            stack.writeToNBT(slot);
            persistentData.setTag("slot" + i, slot);
        }
        if (this.isLocked()) {
            persistentData.setInteger("lock", this.getPins());
            persistentData.setDouble("lockMod", this.getMod());
        }
        return new CrateDropData(persistentData, radiation);
    }

    private static NBTTagCompound assembleDropTag(CrateDropData data) {
        NBTTagCompound root = new NBTTagCompound();
        if (!data.persistentData.isEmpty()) {
            root.setTag(NBT_PERSISTENT_KEY, data.persistentData.copy());
        }
        if (data.radiation > 0D) {
            root.setDouble(BlockStorageCrate.CRATE_RAD_KEY, data.radiation);
        }
        return root;
    }

    @Override
    public boolean canAccess(EntityPlayer player) {

        if (!this.isLocked() || player == null) {
            return true;
        } else {
            ItemStack stack = player.getHeldItemMainhand();

            if (stack.getItem() instanceof ItemKeyPin && ItemKeyPin.getPins(stack) == this.lock) {
                world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }

            if (stack.getItem() == ModItems.key_red) {
                world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }

            return this.tryPick(player);
        }
    }

    @Override
    public @NotNull String getName() {
        return this.hasCustomName() ? this.customName : name;
    }

    public int getCrateColumns() {
        return crateColumns;
    }

    public int getCrateRows() {
        return crateRows;
    }

    public int getCrateX() {
        return crateX;
    }

    public int getCrateY() {
        return crateY;
    }

    public int getPlayerInventoryX() {
        return playerInventoryX;
    }

    public int getPlayerInventoryY() {
        return playerInventoryY;
    }

    public int getHotbarY() {
        return hotbarY;
    }

    public int getGuiWidth() {
        return guiWidth;
    }

    public int getGuiHeight() {
        return guiHeight;
    }

    public int getInventoryLabelX() {
        return inventoryLabelX;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public int getInventoryLabelColor() {
        return inventoryLabelColor;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        suppressInventoryCallbacks = true;
        try {
            super.readFromNBT(compound);
        } finally {
            suppressInventoryCallbacks = false;
        }
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        applyDropData(nbt, buildDropData());
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        NBTTagCompound data = nbt.hasKey(NBT_PERSISTENT_KEY) ? nbt.getCompoundTag(NBT_PERSISTENT_KEY) : nbt;
        if (data.hasKey("lock")) {
            this.setPins(data.getInteger("lock"));
            this.setMod(data.getDouble("lockMod"));
            this.lock();
        }
        suppressInventoryCallbacks = true;
        try {
            for (int i = 0; i < inventory.getSlots(); i++) {
                String key = "slot" + i;
                if (data.hasKey(key)) {
                    inventory.setStackInSlot(i, new ItemStack(data.getCompoundTag(key)));
                } else {
                    inventory.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        } finally {
            suppressInventoryCallbacks = false;
        }
    }

    @Override
    public void setDestroyedByCreativePlayer() {
        destroyedByCreativePlayer = true;
    }

    @Override
    public boolean isDestroyedByCreativePlayer() {
        return destroyedByCreativePlayer;
    }

    @Override
    protected boolean checkLock(EnumFacing facing) {
        return facing == null || !isLocked();
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCrateBase(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICrateBase(player.inventory, this);
    }

}
