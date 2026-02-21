package com.hbm.util;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import com.hbm.render.misc.EnumSymbol;
import net.minecraft.util.ResourceLocation;

public class CompatFluidRegistry {

    /**
     * Registers a fluid with a custom ID.
     * ForgeFluid compatibility is NOT automatic; call one of the setupForgeFluidCompat(...) methods explicitly.
     */
    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r, EnumSymbol symbol, ResourceLocation texture) {
        FluidType type = Fluids.fluidMigration.get(name);
        if(type == null) type = new FluidType(name, id, color, p, f, r, symbol, texture);
        else type.setupForeign(name, id, color, p, f, r, symbol, texture);
        return type;
    }

    /**
     * Registers a fluid with a custom ID and optionally sets up ForgeFluid compatibility immediately.
     * For explicit textures, use {@link #setupForgeFluidCompat(FluidType, ResourceLocation, ResourceLocation)}.
     */
    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r, EnumSymbol symbol, ResourceLocation texture, boolean setupForgeFluidCompat) {
        FluidType type = registerFluid(name, id, color, p, f, r, symbol, texture);
        if (setupForgeFluidCompat) setupForgeFluidCompat(type);
        return type;
    }

    /**
     * Registers a fluid and immediately sets up ForgeFluid compatibility using explicit still/flowing textures.
     */
    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r, EnumSymbol symbol, ResourceLocation texture,
                                          ResourceLocation forgeStill, ResourceLocation forgeFlowing) {
        FluidType type = registerFluid(name, id, color, p, f, r, symbol, texture);
        setupForgeFluidCompat(type, forgeStill, forgeFlowing);
        return type;
    }

    /**
     * Registers a fluid and immediately sets up ForgeFluid compatibility using explicit textures and tint color.
     */
    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r, EnumSymbol symbol, ResourceLocation texture,
                                          ResourceLocation forgeStill, ResourceLocation forgeFlowing, int forgeColor) {
        FluidType type = registerFluid(name, id, color, p, f, r, symbol, texture);
        setupForgeFluidCompat(type, forgeStill, forgeFlowing, forgeColor);
        return type;
    }

    /**
     * Sets up ForgeFluid compatibility for a registered NTM fluid.
     * Call this after all desired traits are added so gaseous/viscous flags are reflected.
     */
    public static void setupForgeFluidCompat(FluidType fluid) {
        Fluids.setupForgeFluidCompat(fluid);
    }

    /**
     * Sets up ForgeFluid compatibility for a registered NTM fluid using explicit textures.
     */
    public static void setupForgeFluidCompat(FluidType fluid, ResourceLocation forgeStill, ResourceLocation forgeFlowing) {
        Fluids.setupForgeFluidCompat(fluid, forgeStill, forgeFlowing);
    }

    /**
     * Sets up ForgeFluid compatibility for a registered NTM fluid using explicit textures and tint color.
     */
    public static void setupForgeFluidCompat(FluidType fluid, ResourceLocation forgeStill, ResourceLocation forgeFlowing, int forgeColor) {
        Fluids.setupForgeFluidCompat(fluid, forgeStill, forgeFlowing, forgeColor);
    }
}
