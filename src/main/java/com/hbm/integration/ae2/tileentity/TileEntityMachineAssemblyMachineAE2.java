package com.hbm.integration.ae2.tileentity;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.hbm.integration.ae2.NTMCraftingMachineHelper;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.util.EnumFacing;

/**
 * AE2-only variant of {@link TileEntityMachineAssemblyMachine}, swapped in by
 * MachineAssemblyMachine#createNewTileEntity only when AE2 is actually loaded (see
 * NTMCraftingMachineAE2Registration). This class references AE2 API types in its
 * "implements" clause, which the JVM must resolve the moment this specific class is
 * loaded - so it must never be referenced from code that runs regardless of whether
 * AE2 is installed. Keep this class a thin shell: no new fields, no new behavior
 * beyond the two ICraftingMachine methods, so there's nothing meaningful lost if a
 * world is ever loaded back without AE2 present.
 */
public class TileEntityMachineAssemblyMachineAE2 extends TileEntityMachineAssemblyMachine implements ICraftingMachine {

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table, EnumFacing ejectionDirection) {
        return NTMCraftingMachineHelper.pushPattern(this.assemblerModule, patternDetails, table);
    }

    @Override
    public boolean acceptsPlans() {
        return NTMCraftingMachineHelper.acceptsPlans(this.assemblerModule);
    }
}
