package com.hbm.inventory.container;

import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerWeaponTable extends Container {

    public ItemStackHandler mods = new ItemStackHandler(7);
    public ItemStackHandler gun = new ItemStackHandler(1);
    public int configIndex = 0;

    public ContainerWeaponTable(InventoryPlayer inventory) {

        for (int i = 0; i < 7; i++) {
            this.addSlotToContainer(new ModSlot(mods, i, 44 + 18 * i, 108));
        }

        this.addSlotToContainer(new SlotItemHandler(gun, 0, 8, 108) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return gun.getStackInSlot(0).isEmpty() && stack.getItem() instanceof ItemGunBaseNT;
            }

            @Override
            public void putStack(ItemStack stack) {
                ContainerWeaponTable.this.configIndex = 0;

                if (!stack.isEmpty()) {
                    ItemStack[] upgrades = XWeaponModManager.getUpgradeItems(stack, ContainerWeaponTable.this.configIndex);

                    for (int i = 0; i < Math.min(upgrades.length, 7); i++) {
                        ContainerWeaponTable.this.mods.setStackInSlot(i, upgrades[i]);
                    }
                }

                super.putStack(stack);
            }

            @Override
            public ItemStack onTake(EntityPlayer player, ItemStack stack) {
                ItemStack ret = super.onTake(player, stack);

                XWeaponModManager.install(
                        stack, ContainerWeaponTable.this.configIndex,
                        mods.getStackInSlot(0),
                        mods.getStackInSlot(1),
                        mods.getStackInSlot(2),
                        mods.getStackInSlot(3),
                        mods.getStackInSlot(4),
                        mods.getStackInSlot(5),
                        mods.getStackInSlot(6));

                for (int i = 0; i < 7; i++) {
                    ItemStack mod = ContainerWeaponTable.this.mods.getStackInSlot(i);
                    if (XWeaponModManager.isApplicable(stack, mod, ContainerWeaponTable.this.configIndex, false)) {
                        ContainerWeaponTable.this.mods.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }

                ContainerWeaponTable.this.configIndex = 0;

                return ret;
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 158 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 216));
        }

        this.detectAndSendChanges();
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        // Custom action: use dragType == 999_999 and slotId as desired config index
        if (dragType == 999_999) {
            if (player.world.isRemote) return ItemStack.EMPTY;

            ItemStack stack = gun.getStackInSlot(0);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemGunBaseNT) {
                int configs = ((ItemGunBaseNT) stack.getItem()).getConfigCount();
                if (configs < slotId) return ItemStack.EMPTY;

                XWeaponModManager.install(
                        stack, this.configIndex,
                        mods.getStackInSlot(0),
                        mods.getStackInSlot(1),
                        mods.getStackInSlot(2),
                        mods.getStackInSlot(3),
                        mods.getStackInSlot(4),
                        mods.getStackInSlot(5),
                        mods.getStackInSlot(6));

                for (int i = 0; i < 7; i++) {
                    ItemStack mod = this.mods.getStackInSlot(i);
                    if (XWeaponModManager.isApplicable(stack, mod, this.configIndex, false)) {
                        this.mods.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }

                this.configIndex = slotId;

                if (!stack.isEmpty()) {
                    ItemStack[] upgrades = XWeaponModManager.getUpgradeItems(stack, this.configIndex);
                    for (int i = 0; i < Math.min(upgrades.length, 7); i++) {
                        this.mods.setStackInSlot(i, upgrades[i]);
                    }
                }

                this.detectAndSendChanges();
            }
            return ItemStack.EMPTY;
        }

        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);

        if (!player.world.isRemote) {
            for (int i = 0; i < this.mods.getSlots(); ++i) {
                ItemStack itemstack = this.mods.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    player.dropItem(itemstack, false);
                    this.mods.setStackInSlot(i, ItemStack.EMPTY);
                }
            }

            ItemStack itemstack = this.gun.getStackInSlot(0);
            if (!itemstack.isEmpty()) {
                XWeaponModManager.uninstall(itemstack, this.configIndex);
                player.dropItem(itemstack, false);
                this.gun.setStackInSlot(0, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();

            if (index < 8) {
                if (index == 7) {
                    XWeaponModManager.install(
                            stack, this.configIndex,
                            mods.getStackInSlot(0),
                            mods.getStackInSlot(1),
                            mods.getStackInSlot(2),
                            mods.getStackInSlot(3),
                            mods.getStackInSlot(4),
                            mods.getStackInSlot(5),
                            mods.getStackInSlot(6));

                    for (int i = 0; i < 7; i++) {
                        ItemStack mod = this.mods.getStackInSlot(i);
                        if (XWeaponModManager.isApplicable(stack, mod, this.configIndex, false)) {
                            this.mods.setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }

                    this.configIndex = 0;
                    this.detectAndSendChanges();
                }

                if (!this.mergeItemStack(stack, 8, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                if (index != 7) {
                    slot.onTake(player, copy);
                }
            } else {
                if (stack.getItem() instanceof ItemGunBaseNT) {
                    if (!this.mergeItemStack(stack, 7, 8, false)) return ItemStack.EMPTY;
                } else {
                    if (!this.mergeItemStack(stack, 0, 7, false)) return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return copy;
    }

    public class ModSlot extends SlotItemHandler {

        public ModSlot(IItemHandler inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return !gun.getStackInSlot(0).isEmpty() && XWeaponModManager.isApplicable(gun.getStackInSlot(0), stack, ContainerWeaponTable.this.configIndex, true);
        }

        @Override
        public void putStack(ItemStack stack) {
            super.putStack(stack);
            refreshInstalledMods();
            XWeaponModManager.onInstallStack(gun.getStackInSlot(0), stack, ContainerWeaponTable.this.configIndex);
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            ItemStack ret = super.onTake(player, stack);
            refreshInstalledMods();
            XWeaponModManager.onUninstallStack(gun.getStackInSlot(0), stack, ContainerWeaponTable.this.configIndex);
            return ret;
        }

        public void refreshInstalledMods() {
            ItemStack gunStack = gun.getStackInSlot(0);
            if (gunStack.isEmpty()) return;

            XWeaponModManager.uninstall(gunStack, ContainerWeaponTable.this.configIndex);
            XWeaponModManager.install(
                    gunStack, ContainerWeaponTable.this.configIndex,
                    mods.getStackInSlot(0),
                    mods.getStackInSlot(1),
                    mods.getStackInSlot(2),
                    mods.getStackInSlot(3),
                    mods.getStackInSlot(4),
                    mods.getStackInSlot(5),
                    mods.getStackInSlot(6));
        }
    }
}
