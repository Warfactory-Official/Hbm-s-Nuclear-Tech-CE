package com.hbm.blocks.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ModFluids {
    public static Fluid toxic_fluid = new ToxicFluid().setDensity(2500).setViscosity(2000).setTemperature(70+273);
    public static Fluid mud_fluid = new MudFluid().setDensity(2500).setViscosity(3000).setLuminosity(5).setTemperature(1773);
    public static Fluid acid_fluid = new AcidFluid().setDensity(2500).setViscosity(1500).setLuminosity(5).setTemperature(2773);
    public static Fluid schrabidic_fluid = new SchrabidicFluid().setDensity(31200).setViscosity(500);
	public static Fluid corium_fluid = new CoriumFluid().setDensity(31200).setViscosity(2000).setTemperature(3000);
	public static Fluid volcanic_lava_fluid = new VolcanicFluid().setLuminosity(15).setDensity(3000).setViscosity(3000).setTemperature(1300);
    public static Fluid bromine_fluid = new BromineFluid().setDensity(3000).setViscosity(3000).setTemperature(273);
    public static Fluid sulfuric_acid_fluid = new SulfuricAcidFluid().setDensity(1840).setViscosity(1000).setTemperature(273);

    public static void init() {
        registerFluid(toxic_fluid);
		registerFluid(mud_fluid);
        registerFluid(acid_fluid);
		registerFluid(schrabidic_fluid);
		registerFluid(corium_fluid);
		registerFluid(volcanic_lava_fluid);
		registerFluid(bromine_fluid);
		registerFluid(sulfuric_acid_fluid);
    }

    private static void registerFluid(Fluid fluid) {
        FluidRegistry.registerFluid(fluid);
        FluidRegistry.addBucketForFluid(fluid);
    }

    public static void setFromRegistry() {
		toxic_fluid = FluidRegistry.getFluid("toxic_fluid");
		mud_fluid = FluidRegistry.getFluid("mud_fluid");
        acid_fluid = FluidRegistry.getFluid("acid_fluid");
		schrabidic_fluid = FluidRegistry.getFluid("schrabidic_fluid");
        corium_fluid = FluidRegistry.getFluid("corium_fluid");
        volcanic_lava_fluid = FluidRegistry.getFluid("volcanic_lava_fluid");
        bromine_fluid = FluidRegistry.getFluid("bromine");
		sulfuric_acid_fluid = FluidRegistry.getFluid("sulfuric_acid");
	}
}
