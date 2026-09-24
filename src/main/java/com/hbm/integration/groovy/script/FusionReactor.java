package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.recipes.FusionRecipe;
import com.hbm.inventory.recipes.FusionRecipes;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.FusionRecipes.INSTANCE;

/**
 * Dedicated GroovyScript integration for the Fusion Reactor (com.hbm.inventory.recipes.FusionRecipes).
 * Exposed as mods.hbm.fusionReactor - the recipeOverrides RecipeFileBinding already claims "fusion"
 * (see AssemblyMachine's javadoc for the full rationale).
 * <p>
 * No item input at all (inputItemLimit() == 0) - this machine runs on fluids only, up to 3 in and up
 * to 11 out, plus a single item output. FusionRecipe also carries plasma-specific fields (ignition
 * temp, output temp, neutron flux, glow color) with no equivalent on the base GenericRecipe.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class FusionReactor extends VirtualizedRegistry<FusionRecipe> {

    public FusionReactor() {
        super(Collections.singletonList("fusionReactor"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(FusionRecipe recipe) {
        if (INSTANCE.recipeNameMap.containsKey(recipe.getInternalName())) {
            INSTANCE.removeRecipeByName(recipe.getInternalName());
        }
        INSTANCE.register(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(FusionRecipe recipe) {
        INSTANCE.removeRecipeByName(recipe.getInternalName());
        this.addBackup(recipe);
    }

    public void removeByName(String name) {
        FusionRecipe recipe = INSTANCE.recipeNameMap.get(name);
        if (recipe == null) return;
        this.removeRecipe(recipe);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return INSTANCE.recipeOrderedList.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<FusionRecipe> {

        private FluidStack[] fluidsIn = new FluidStack[0];
        private FluidStack[] fluidsOut = new FluidStack[0];
        private int duration = 100;
        private long power = 100;
        private long ignitionTemp = 0;
        private long outputTemp = 0;
        private double neutronFlux = 0;
        private float r = 1F, g = 0.2F, b = 0.6F;
        private ItemStack icon;
        private boolean named = false;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_fusionreactor_";
        }

        /** Uses hbm's own FluidStack type, not Forge's - construct with e.g. new FluidStack(Fluids.DEUTERIUM, 2000). Up to 3. */
        public RecipeBuilder inputFluid(FluidStack... fluids) {
            this.fluidsIn = fluids;
            return this;
        }

        /** Up to 11 output fluids. */
        public RecipeBuilder outputFluid(FluidStack... fluids) {
            this.fluidsOut = fluids;
            return this;
        }

        public RecipeBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public RecipeBuilder power(long power) {
            this.power = power;
            return this;
        }

        /** Minimum klystron energy to ignite the plasma. */
        public RecipeBuilder ignitionTemp(long ignitionTemp) {
            this.ignitionTemp = ignitionTemp;
            return this;
        }

        /** Plasma output energy at full blast. */
        public RecipeBuilder outputTemp(long outputTemp) {
            this.outputTemp = outputTemp;
            return this;
        }

        /** Neutron output energy at full blast. */
        public RecipeBuilder neutronFlux(double neutronFlux) {
            this.neutronFlux = neutronFlux;
            return this;
        }

        /** Plasma glow color, defaults to a pinkish-purple (1, 0.2, 0.6). */
        public RecipeBuilder color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            return this;
        }

        public RecipeBuilder icon(ItemStack icon) {
            this.icon = icon;
            return this;
        }

        public RecipeBuilder named() {
            this.named = true;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Fusion Reactor recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.fluidsIn.length == 0) {
                msg.add("Fusion Reactor recipe needs at least one input fluid");
            }
            if (this.fluidsIn.length > 3) {
                msg.add("{} input fluids given, but the Fusion Reactor allows at most 3", this.fluidsIn.length);
            }
            if (this.fluidsOut.length > 11) {
                msg.add("{} output fluids given, but the Fusion Reactor allows at most 11", this.fluidsOut.length);
            }
            if (this.output.isEmpty() && this.fluidsOut.length == 0) {
                msg.add("Fusion Reactor recipe needs at least one item or fluid output");
            }
            if (this.output.size() > 1) {
                msg.add("Fusion Reactor recipe allows at most 1 item output");
            }
        }

        @Override
        public FusionRecipe register() {
            if (!this.validate()) return null;
            this.validateName();

            FusionRecipe recipe = INSTANCE.instantiateRecipe(this.name.toString());

            recipe.inputFluids(this.fluidsIn);
            if (!this.output.isEmpty()) recipe.outputItems(this.output.toArray(new ItemStack[0]));
            if (this.fluidsOut.length > 0) recipe.outputFluids(this.fluidsOut);

            recipe.setup(this.duration, this.power);
            recipe.setInputEnergy(this.ignitionTemp);
            recipe.setOutputEnergy(this.outputTemp);
            recipe.setOutputFlux(this.neutronFlux);
            recipe.setRGB(this.r, this.g, this.b);
            if (this.icon != null) recipe.setIcon(this.icon);
            if (this.named) recipe.setNamed();

            HbmGroovyPropertyContainer.FUSIONREACTOR.addRecipe(recipe);
            return recipe;
        }
    }
}
