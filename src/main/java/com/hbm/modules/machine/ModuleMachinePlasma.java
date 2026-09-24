package com.hbm.modules.machine;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.recipes.PlasmaForgeRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.util.BobMathUtil;
import net.minecraftforge.items.ItemStackHandler;

public class ModuleMachinePlasma extends ModuleMachineBase {

    public ModuleMachinePlasma(int index, IEnergyHandlerMK2 battery, ItemStackHandler inventory) {
        super(index, battery, inventory);
        this.inputSlots = new int[12];
        this.outputSlots = new int[1];
        this.inputTanks = new FluidTankNTM[1];
        this.outputTanks = new FluidTankNTM[0];
    }

    @Override
    public GenericRecipes getRecipeSet() {
        return PlasmaForgeRecipes.INSTANCE;
    }

    @Override
    public void setupTanks(GenericRecipe recipe) {
        super.setupTanks(recipe);
        if(recipe == null) return;
        for(int i = 0; i < inputTanks.length; i++) {
            if(recipe.inputFluid != null && recipe.inputFluid.length > i) {
                inputTanks[i].changeTankSize(BobMathUtil.max(inputTanks[i].getFill(), recipe.inputFluid[i].fill * 2, 16_000));
            }
        }
    }

    public ModuleMachinePlasma itemInput(int from) {
        for(int i = 0; i < inputSlots.length; i++) inputSlots[i] = from + i;
        return this;
    }

    public ModuleMachinePlasma itemOutput(int slot) {
        outputSlots[0] = slot;
        return this;
    }

    public ModuleMachinePlasma fluidInput(FluidTankNTM tank) {
        inputTanks[0] = tank;
        return this;
    }
}
