package com.hbm.inventory.recipes;

import com.hbm.inventory.fluid.Fluids;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.HashMap;

public class EngineRecipes {

	public static HashMap<Fluid, Long> combustionEnergies = new HashMap<Fluid, Long>();
	public static HashMap<Fluid, FuelGrade> fuelGrades = new HashMap<Fluid, FuelGrade>();

	//for 1000 mb
	public static void registerEngineRecipes() {
		addFuel(Fluids.HYDROGEN.getFF(), FuelGrade.HIGH, 10_000);
		addFuel(Fluids.DEUTERIUM.getFF(), FuelGrade.HIGH, 10_000);
		addFuel(Fluids.TRITIUM.getFF(), FuelGrade.HIGH, 10_000);
		addFuel(Fluids.HEAVYOIL.getFF(), FuelGrade.LOW, 25_000);
		addFuel(Fluids.HEAVYOIL.getFF(), FuelGrade.LOW, 100_000);
		addFuel(Fluids.RECLAIMED.getFF(), FuelGrade.LOW, 200_000);
		addFuel(Fluids.PETROIL.getFF(), FuelGrade.MEDIUM, 300_000);
		addFuel(Fluids.NAPHTHA.getFF(), FuelGrade.MEDIUM, 200_000);
		addFuel(Fluids.DIESEL.getFF(), FuelGrade.HIGH, 500_000);
		addFuel(Fluids.LIGHTOIL.getFF(), FuelGrade.MEDIUM, 500_000);
		addFuel(Fluids.KEROSENE.getFF(), FuelGrade.AERO, 1_250_000);
		addFuel(Fluids.KEROSENE_REFORM.getFF(), FuelGrade.AERO, 1_750_000);
		addFuel(Fluids.BIOGAS.getFF(), FuelGrade.AERO, 500_000);
		addFuel(Fluids.BIOFUEL.getFF(), FuelGrade.HIGH, 400_000);
		addFuel(Fluids.NITAN.getFF(), FuelGrade.HIGH, 5_000_000);
		addFuel(Fluids.BALEFIRE.getFF(), FuelGrade.HIGH, 2_500_000);
		addFuel(Fluids.GASOLINE.getFF(), FuelGrade.HIGH, 1_000_000);
		addFuel(Fluids.ETHANOL.getFF(), FuelGrade.HIGH, 200_000);
		addFuel(Fluids.FISHOIL.getFF(), FuelGrade.LOW, 50_000);
		addFuel(Fluids.SUNFLOWEROIL.getFF(), FuelGrade.LOW, 80_000);
		addFuel(Fluids.GAS.getFF(), FuelGrade.GAS, 100_000);
		addFuel(Fluids.PETROLEUM.getFF(), FuelGrade.GAS, 300_000);
		addFuel(Fluids.AROMATICS.getFF(), FuelGrade.GAS, 150_000);
		addFuel(Fluids.UNSATURATEDS.getFF(), FuelGrade.GAS, 250_000);

		//Compat
		addFuel("biofuel", FuelGrade.HIGH, 400_000); //galacticraft & industrialforegoing
		addFuel("petroil", FuelGrade.MEDIUM, 300_000); //galacticraft
		addFuel("refined_fuel", FuelGrade.HIGH, 1_000_000); //thermalfoundation
		addFuel("refined_biofuel", FuelGrade.HIGH, 400_000); //thermalfoundation

	}

	public enum FuelGrade {
		LOW("trait.fuelgrade.low"),			//heating and industrial oil				< star engine, iGen
		MEDIUM("trait.fuelgrade.medium"),	//petroil									< diesel generator
		HIGH("trait.fuelgrade.high"),		//diesel, gasoline							< HP engine
		AERO("trait.fuelgrade.aero"),	//kerosene and other light aviation fuels	< turbofan
		GAS("trait.fuelgrade.gas");		//fuel gasses like NG, PG and syngas		< gas turbine

		private final String grade;

        FuelGrade(String grade) {
			this.grade = grade;
		}

		public String getGrade() {
			return this.grade;
		}
	}

	public static long getEnergy(Fluid f){
		if(f != null)
			return combustionEnergies.get(f);
		return 0;
	}

	public static FuelGrade getFuelGrade(Fluid f){
		if(f != null)
			return fuelGrades.get(f);
		return null;
	}

	public static boolean isAero(Fluid f){
		return getFuelGrade(f) == FuelGrade.AERO;
	}

	public static void addFuel(Fluid f, FuelGrade g, long power){
		if(f != null && power > 0){
			combustionEnergies.put(f, power);
			fuelGrades.put(f, g);
		}
	}

	public static boolean hasFuelRecipe(Fluid f){
		if(f == null) return false;
		return combustionEnergies.containsKey(f);
	}

	public static void addFuel(String f, FuelGrade g, long power){
		if(FluidRegistry.isFluidRegistered(f)){
			addFuel(FluidRegistry.getFluid(f), g, power);
		}
	}

	public static void removeFuel(Fluid f){
		if(f != null){
			combustionEnergies.remove(f);
			fuelGrades.remove(f);
		}
	}

	public static void removeFuel(String f){
		if(FluidRegistry.isFluidRegistered(f)){
			removeFuel(FluidRegistry.getFluid(f));
		}
	}
}