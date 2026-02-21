package com.hbm.inventory.container;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.hbm.tileentity.machine.TileEntityCrateTemplate;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

// ISortableContainer is for bogoSorter compatibility. Not needed for other crate containers because they have already done it via mixin,
// see all classes under package com.cleanroommc.bogosorter.core.mixin.hbm
// Ideally move these to bogosorter
@Optional.Interface(iface = "com.cleanroommc.bogosorter.api.ISortableContainer", modid = "bogosorter")
public class ContainerCrateTemplate extends Container implements ISortableContainer {

    private final TileEntityCrateTemplate crate;

    public ContainerCrateTemplate(InventoryPlayer invPlayer, TileEntityCrateTemplate te) {
        crate = te;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new SlotItemHandler(te.inventory, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 32 + i * 18 + (18 * 3)));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 90 + (18 * 3)));
        }
    }

    @NotNull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(this.inventorySlots, index, this.crate.inventory.getSlots());
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return crate.isUseableByPlayer(player);
    }

    @Override
    @Optional.Method(modid = "bogosorter")
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, crate.inventory.getSlots(), 9);
    }
}
