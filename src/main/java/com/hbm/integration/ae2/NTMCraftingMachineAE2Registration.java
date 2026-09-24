package com.hbm.integration.ae2;

import com.hbm.Tags;
import com.hbm.util.Compat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Registers the AE2-only TileEntity variants (see com.hbm.integration.ae2.tileentity) with the game's
 * TileEntity registry, but ONLY when AE2 is actually loaded.
 *
 * Class names are passed as plain Strings and resolved via Class.forName, never as a ".class" literal -
 * see NTMCraftingMachineFactory's javadoc for why that distinction is load-bearing here (a ".class"
 * literal is still a bytecode-level class reference that Forge's LaunchClassLoader resolves eagerly
 * regardless of the isModLoaded guard around it; a reflectively-looked-up class name is just an opaque
 * String and carries none of that risk). This class is always loaded regardless of AE2's presence -
 * that's fine specifically because it never references an AE2 subclass by anything other than a String.
 *
 * Call registerIfPresent() once, from the same preInit stage as AutoRegistry.registerTileEntities()
 * (see MainRegistry) - registration has to happen before any world/chunk data referencing these classes
 * is loaded, same as every other TileEntity in the mod.
 */
public final class NTMCraftingMachineAE2Registration {

    private static final String PKG = "com.hbm.integration.ae2.tileentity.";

    private static final String[][] MACHINES = {
            {PKG + "TileEntityMachineAssemblyMachineAE2", "tileentity_assemblymachine_ae2"},
            {PKG + "TileEntityMachineChemicalPlantAE2", "tileentity_chemicalplant_ae2"},
            {PKG + "TileEntityMachinePrecAssAE2", "tileentity_precass_ae2"},
            {PKG + "TileEntityMachineRockMillAE2", "tileentity_rockmill_ae2"},
            {PKG + "TileEntityFusionTorusAE2", "tileentity_fusion_torus_ae2"},
            {PKG + "TileEntityMachinePUREXAE2", "tileentity_purex_ae2"},
            {PKG + "TileEntityFusionPlasmaForgeAE2", "tileentity_fusion_plasma_forge_ae2"},
            {PKG + "TileEntityProxyComboAE2", "tileentity_proxy_combo_ae2"},
    };

    private NTMCraftingMachineAE2Registration() { }

    public static void registerIfPresent() {
        if (!Loader.isModLoaded(Compat.ModIds.AE2)) return;

        for (String[] entry : MACHINES) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends TileEntity> clazz = (Class<? extends TileEntity>) Class.forName(entry[0]);
                GameRegistry.registerTileEntity(clazz, new ResourceLocation(Tags.MODID, entry[1]));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to register AE2-aware NTM tile entity: " + entry[0], e);
            }
        }
    }
}
