package com.hbm.modules.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.recipes.SuperComputerRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.util.BobMathUtil;
import net.minecraftforge.items.ItemStackHandler;

public class ModuleMachineSuperComputer extends ModuleMachineBase {

	public ModuleMachineSuperComputer(int index, IEnergyHandlerMK2 battery, ItemStackHandler slots) {
		super(index, battery, slots);
		this.inputSlots = new int[3];
		this.outputSlots = new int[3];
		this.inputTanks = new FluidTankNTM[1];
		this.outputTanks = new FluidTankNTM[1];
	}

	@Override
	public GenericRecipes getRecipeSet() {
		return SuperComputerRecipes.INSTANCE;
	}

	@Override
	public void setupTanks(GenericRecipe recipe) {
		super.setupTanks(recipe);
		if(recipe == null) return;
		for(int i = 0; i < inputTanks.length; i++) if(recipe.inputFluid != null && recipe.inputFluid.length > i) inputTanks[i].changeTankSize(BobMathUtil.max(inputTanks[i].getFill(), recipe.inputFluid[i].fill * 2, 4_000));
		for(int i = 0; i < outputTanks.length; i++) if(recipe.outputFluid != null && recipe.outputFluid.length > i) outputTanks[i].changeTankSize(BobMathUtil.max(outputTanks[i].getFill(), recipe.outputFluid[i].fill * 2, 4_000));
	}

	public ModuleMachineSuperComputer itemInput(int from) { for(int i = 0; i < inputSlots.length; i++) inputSlots[i] = from + i; return this; }
	public ModuleMachineSuperComputer itemOutput(int from) { for(int i = 0; i < outputSlots.length; i++) outputSlots[i] = from + i; return this; }
	public ModuleMachineSuperComputer fluidInput(FluidTankNTM a) { inputTanks[0] = a; return this; }
	public ModuleMachineSuperComputer fluidOutput(FluidTankNTM a) { outputTanks[0] = a; return this; }
}
