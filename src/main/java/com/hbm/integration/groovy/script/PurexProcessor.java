package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.recipes.PUREXRecipe;
import com.hbm.inventory.recipes.PUREXRecipes;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.PUREXRecipes.INSTANCE;

/**
 * Dedicated GroovyScript integration for the PUREX Plant (com.hbm.inventory.recipes.PUREXRecipes).
 * Exposed as mods.hbm.purexProcessor - the recipeOverrides RecipeFileBinding already claims "purex"
 * (see AssemblyMachine's javadoc for the full rationale).
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class PurexProcessor extends VirtualizedRegistry<PUREXRecipe> {

    public PurexProcessor() {
        super(Collections.singletonList("purexProcessor"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(PUREXRecipe recipe) {
        if (INSTANCE.recipeNameMap.containsKey(recipe.getInternalName())) {
            INSTANCE.removeRecipeByName(recipe.getInternalName());
        }
        INSTANCE.register(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(PUREXRecipe recipe) {
        INSTANCE.removeRecipeByName(recipe.getInternalName());
        this.addBackup(recipe);
    }

    public void removeByName(String name) {
        PUREXRecipe recipe = INSTANCE.recipeNameMap.get(name);
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

    public static class RecipeBuilder extends AbstractRecipeBuilder<PUREXRecipe> {

        private FluidStack[] fluidsIn = new FluidStack[0];
        private FluidStack fluidOut;
        private int duration = 100;
        private long power = 100;
        private ItemStack icon;
        private boolean named = false;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_purexprocessor_";
        }

        /** Uses hbm's own FluidStack type, not Forge's. Up to 3. */
        public RecipeBuilder inputFluid(FluidStack... fluids) {
            this.fluidsIn = fluids;
            return this;
        }

        public RecipeBuilder outputFluid(FluidStack fluid) {
            this.fluidOut = fluid;
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
            return "Error adding PUREX Plant recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 0, 3, 0, 6);
            if (this.fluidsIn.length > 3) {
                msg.add("{} input fluids given, but the PUREX Plant allows at most 3", this.fluidsIn.length);
            }
            if (this.input.isEmpty() && this.fluidsIn.length == 0) {
                msg.add("PUREX Plant recipe needs at least one item or fluid input");
            }
            if (this.output.isEmpty() && this.fluidOut == null) {
                msg.add("PUREX Plant recipe needs at least one item or fluid output");
            }
        }

        @Override
        public PUREXRecipe register() {
            if (!this.validate()) return null;
            this.validateName();

            PUREXRecipe recipe = INSTANCE.instantiateRecipe(this.name.toString());

            if (!this.input.isEmpty()) {
                RecipesCommon.AStack[] items = new RecipesCommon.AStack[this.input.size()];
                for (int i = 0; i < items.length; i++) items[i] = IngredientUtils.convertIngredient2Astack(this.input.get(i));
                recipe.inputItems(items);
            }
            if (this.fluidsIn.length > 0) recipe.inputFluids(this.fluidsIn);
            if (!this.output.isEmpty()) recipe.outputItems(this.output.toArray(new ItemStack[0]));
            if (this.fluidOut != null) recipe.outputFluids(this.fluidOut);

            recipe.setup(this.duration, this.power);
            if (this.icon != null) recipe.setIcon(this.icon);
            if (this.named) recipe.setNamed();

            HbmGroovyPropertyContainer.PUREXPROCESSOR.addRecipe(recipe);
            return recipe;
        }
    }
}
