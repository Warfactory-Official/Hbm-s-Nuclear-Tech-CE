package com.hbm.tileentity.machine;

import com.hbm.config.MachineConfig;
import com.hbm.lib.Library;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class HandHeldTileEntityCrate extends TileEntityCrate implements IHandHeldCrate {

    private final ItemStack boundItem;

    public HandHeldTileEntityCrate(TileEntityCrate template, ItemStack boundItem) {
        super(template.inventory.getSlots(), template.name, template.getCrateColumns(), template.getCrateRows(), template.getCrateX(), template.getCrateY(),
                template.getPlayerInventoryX(), template.getPlayerInventoryY(), template.getHotbarY(), template.getGuiWidth(), template.getGuiHeight(),
                template.getInventoryLabelX(), template.getTitleColor(), template.getInventoryLabelColor(), template.getTexture());
        this.boundItem = boundItem;
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
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (!boundItem.isEmpty() && (stack == boundItem || ItemStack.areItemStacksEqual(stack, boundItem))) {
                    return false;
                }
                return super.isItemValid(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                if (suppressInventoryCallbacks) {
                    return;
                }
                markDirty();
                syncBoundItem();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slotlimit;
            }
        };
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return !boundItem.isEmpty() && player.getHeldItemMainhand() == boundItem;
    }

    @Override
    public void finishHeldInventorySession(EntityPlayer player) {
        syncBoundItem();

        if (player == null || world == null || world.isRemote) {
            return;
        }

        NBTTagCompound nbt = boundItem.getTagCompound();
        if (nbt == null || Library.getCompressedNbtSize(nbt) <= MachineConfig.crateByteSize) {
            return;
        }

        player.sendMessage(new TextComponentString("§cWarning: Container NBT exceeds 6kB, contents will be ejected!"));
        ejectAndClearInventory();
        syncBoundItem();
    }

    private void syncBoundItem() {
        if (boundItem.isEmpty()) {
            return;
        }

        NBTTagCompound nbt = boundItem.hasTagCompound() ? boundItem.getTagCompound() : new NBTTagCompound();
        writeNBT(nbt);
        boundItem.setTagCompound(nbt.isEmpty() ? null : nbt);
    }
}
