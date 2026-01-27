package com.hbm.tileentity;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.ICopiable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IControlReceiverFilter extends IControlReceiver, ICopiable {

    void nextMode(int i);

    @Override
    default void receiveControl(NBTTagCompound data) {
        if (data.hasKey("slot")) {
            setFilterContents(data);
        }
    }

    /**
     * Uses Capabilities to update filter contents.
     * Expects the implementor to be a TileEntity.
     */
    default void setFilterContents(NBTTagCompound nbt) {
        if (!(this instanceof TileEntity tile)) return;

        IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (handler instanceof IItemHandlerModifiable modifiableHandler) {
            int slot = nbt.getInteger("slot");
            ItemStack stack = new ItemStack(nbt.getCompoundTag("stack"));

            modifiableHandler.setStackInSlot(slot, stack);
            nextMode(slot);

            tile.getWorld().markChunkDirty(tile.getPos(), tile);
        }
    }

    int[] getFilterSlots();

    @Override
    default NBTTagCompound getSettings(World world, int x, int y, int z) {
        NBTTagCompound nbt = new NBTTagCompound();
        if (!(this instanceof TileEntity tile)) return nbt;

        IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (handler == null) return nbt;

        NBTTagList tags = new NBTTagList();
        int[] range = getFilterSlots();

        int count = 0;
        for (int i = range[0]; i < range[1]; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                NBTTagCompound slotNBT = new NBTTagCompound();
                slotNBT.setByte("slot", (byte) count);
                stack.writeToNBT(slotNBT);
                tags.appendTag(slotNBT);
            }
            count++;
        }
        nbt.setTag("items", tags);
        return nbt;
    }

    @Override
    default void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
        if (!(this instanceof TileEntity tile)) return;

        IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (!(handler instanceof IItemHandlerModifiable modifiableHandler)) return;

        NBTTagList items = nbt.getTagList("items", 10);
        int listSize = items.tagCount();
        if (listSize <= 0) return;

        int[] range = getFilterSlots();
        int count = 0;

        for (int i = range[0]; i < range[1]; i++) {
            if (count < listSize) {
                NBTTagCompound slotNBT = items.getCompoundTagAt(count);
                byte slotIdx = slotNBT.getByte("slot");
                ItemStack loadedStack = new ItemStack(slotNBT);

                boolean isRouter = nbt.hasKey("modes") && slotIdx > index * 5 && slotIdx < (index + 1) * 5;

                if (!loadedStack.isEmpty() && (slotIdx < range[1] || isRouter)) {
                    int targetSlot = slotIdx + range[0];
                    if (targetSlot < modifiableHandler.getSlots()) {
                        modifiableHandler.setStackInSlot(targetSlot, loadedStack);
                        nextMode(slotIdx);
                    }
                }
            }
            count++;
        }
        tile.getWorld().markChunkDirty(tile.getPos(), tile);
    }

    @Override
    default String[] infoForDisplay(World world, int x, int y, int z) {
        return new String[]{"copytool.filter"};
    }
}
