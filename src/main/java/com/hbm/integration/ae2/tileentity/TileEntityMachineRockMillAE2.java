package com.hbm.integration.ae2.tileentity;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.hbm.integration.ae2.NTMCraftingMachineHelper;
import com.hbm.tileentity.machine.TileEntityMachineRockMill;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.util.EnumFacing;

/** AE2-only variant, see TileEntityMachineAssemblyMachineAE2 for the general rationale. */
public class TileEntityMachineRockMillAE2 extends TileEntityMachineRockMill implements ICraftingMachine {

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table, EnumFacing ejectionDirection) {
        return NTMCraftingMachineHelper.pushPattern(this.rockMillModule, patternDetails, table);
    }

    @Override
    public boolean acceptsPlans() {
        return NTMCraftingMachineHelper.acceptsPlans(this.rockMillModule);
    }
}
