package com.hbm.integration.ae2.tileentity;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.hbm.integration.ae2.NTMCraftingMachineHelper;
import com.hbm.tileentity.machine.TileEntityMachinePUREX;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.util.EnumFacing;

/**
 * AE2-only variant, see TileEntityMachineAssemblyMachineAE2 for the general rationale.
 * NOTE: item side only - PUREX recipes' fluid inputs still need to be piped in separately
 * for the craft to actually complete.
 */
public class TileEntityMachinePUREXAE2 extends TileEntityMachinePUREX implements ICraftingMachine {

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table, EnumFacing ejectionDirection) {
        return NTMCraftingMachineHelper.pushPattern(this.purexModule, patternDetails, table);
    }

    @Override
    public boolean acceptsPlans() {
        return NTMCraftingMachineHelper.acceptsPlans(this.purexModule);
    }
}
