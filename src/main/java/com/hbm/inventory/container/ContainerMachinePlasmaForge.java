package com.hbm.inventory.container;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.TransferStrategy;
import com.hbm.inventory.recipes.PlasmaForgeRecipe;
import com.hbm.inventory.recipes.PlasmaForgeRecipes;
import com.hbm.inventory.slot.SlotBattery;
import com.hbm.inventory.slot.SlotCraftingOutput;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.fusion.TileEntityFusionPlasmaForge;
import com.hbm.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerMachinePlasmaForge extends ContainerBase {

    private final TileEntityFusionPlasmaForge forge;
    private final TransferStrategy plasmaTransfer;

    public ContainerMachinePlasmaForge(InventoryPlayer invPlayer, TileEntityFusionPlasmaForge forge) {
        super(invPlayer, forge.inventory);
        this.forge = forge;

        addSlotToContainer(new SlotBattery(forge.inventory, 0, 152, 82));
        addSlotToContainer(new SlotNonRetarded(forge.inventory, 1, 35, 81));
        addSlotToContainer(new SlotNonRetarded(forge.inventory, 2, 98, 116));
        addSlots(forge.inventory, 3, 8, 18, 3, 4);
        addSlotToContainer(new SlotCraftingOutput(invPlayer.player, forge.inventory, 15, 116, 36));

        playerInv(invPlayer, 8, 162);
        plasmaTransfer = TransferStrategy.builder(forge.inventory.getSlots())
                .rule(0, 1, Library::isBattery)
                .rule(1, 2, stack -> stack.getItem() == ModItems.blueprints)
                .rule(3, 15, this::matchesRecipeInput)
                .genericMachineRange(2, 15)
                .ruleDispatchMode(TransferStrategy.RuleDispatchMode.FIRST_MATCH_WINS)
                .build();
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return InventoryUtil.transferStack(inventorySlots, index, plasmaTransfer, player);
    }

    private boolean matchesRecipeInput(ItemStack stack) {
        PlasmaForgeRecipe recipe = PlasmaForgeRecipes.INSTANCE.recipeNameMap.get(forge.plasmaModule.recipe);
        if(recipe == null || recipe.inputItem == null) return false;
        for(AStack input : recipe.inputItem) {
            if(input != null && input.matchesRecipe(stack, true)) return true;
        }
        return false;
    }
}
