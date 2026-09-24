package com.hbm.integration.ae2.tileentity;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.hbm.integration.ae2.NTMCraftingMachineHelper;
import com.hbm.tileentity.machine.TileEntityMachineChemicalPlant;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.util.EnumFacing;

/** AE2-only variant, see TileEntityMachineAssemblyMachineAE2 for the general rationale. */
public class TileEntityMachineChemicalPlantAE2 extends TileEntityMachineChemicalPlant implements ICraftingMachine {

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table, EnumFacing ejectionDirection) {
        return NTMCraftingMachineHelper.pushPattern(this.chemplantModule, patternDetails, table);
    }

    @Override
    public boolean acceptsPlans() {
        return NTMCraftingMachineHelper.acceptsPlans(this.chemplantModule);
    }
}
