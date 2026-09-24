package com.hbm.integration.ae2;

import com.hbm.tileentity.TileEntityProxyCombo;
import net.minecraft.tileentity.TileEntity;

/**
 * Reflective factory for the AE2-only TileEntity subclasses (see com.hbm.integration.ae2.tileentity),
 * used by the various Block#createNewTileEntity methods when AE2 is loaded.
 *
 * This class deliberately imports NOTHING from appeng.* - not even indirectly, so keep it that way.
 * The 7 machine Block classes call createAE2TileEntity() unconditionally in their source (guarded only
 * by an isModLoaded check at runtime), and those Block classes are always loaded regardless of whether
 * AE2 is installed. Forge's LaunchClassLoader runs every loaded class through ASM transformers that
 * compute stack map frames, which resolves every class referenced ANYWHERE in a class's bytecode -
 * including inside a branch that never executes at runtime - to find common supertypes for merged jump
 * targets. A direct "new TileEntityXAE2()" (or even a bare ".class" literal) sitting in a Block class,
 * or transitively in any class that Block class's bytecode references via a normal method call, is
 * therefore resolved eagerly at class-load time no matter what runtime guard surrounds it, and crashes
 * mod load with NoClassDefFoundError on any install without AE2 (confirmed the hard way during
 * development - see NTMCraftingMachineHelper's javadoc for the actual AE2-referencing logic, which is
 * safe specifically because nothing always-loaded ever calls into it directly).
 *
 * A reflectively-resolved class name is just an opaque String constant to the bytecode verifier, so
 * routing every AE2 subclass construction through Class.forName here - in a class with no AE2 type in
 * its own signature - is what actually breaks the chain.
 */
public final class NTMCraftingMachineFactory {

    private NTMCraftingMachineFactory() { }

    public static TileEntity createAE2TileEntity(String className) {
        try {
            return (TileEntity) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate AE2-aware NTM tile entity: " + className, e);
        }
    }

    /**
     * Same idea as createAE2TileEntity, but for the multiblock proxy positions (see
     * TileEntityProxyComboAE2's javadoc for why the plain proxy alone isn't enough): a multiblock
     * machine exposes far more proxy-covered faces than the one actual core block, so an ME
     * Interface placed against "the machine" almost always ends up touching a proxy, not the core.
     */
    public static TileEntity createAE2Proxy(boolean inventory, boolean power, boolean fluid) {
        try {
            TileEntityProxyCombo proxy = (TileEntityProxyCombo) Class.forName("com.hbm.integration.ae2.tileentity.TileEntityProxyComboAE2").getDeclaredConstructor().newInstance();
            if (inventory) proxy.inventory();
            if (power) proxy.power();
            if (fluid) proxy.fluid();
            return proxy;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate AE2-aware NTM proxy tile entity", e);
        }
    }
}
