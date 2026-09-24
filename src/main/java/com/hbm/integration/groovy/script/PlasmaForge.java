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
import com.hbm.inventory.recipes.PlasmaForgeRecipe;
import com.hbm.inventory.recipes.PlasmaForgeRecipes;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.PlasmaForgeRecipes.INSTANCE;

/**
 * Dedicated GroovyScript integration for the Plasma Forge (com.hbm.inventory.recipes.PlasmaForgeRecipes).
 * Exposed as mods.hbm.plasmaForge. PlasmaForgeRecipe adds one extra field over the base GenericRecipe:
 * ignitionTemp (minimum plasma energy needed to run the recipe).
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class PlasmaForge extends VirtualizedRegistry<PlasmaForgeRecipe> {

    public PlasmaForge() {
        super(Collections.singletonList("plasmaForge"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(PlasmaForgeRecipe recipe) {
        if (INSTANCE.recipeNameMap.containsKey(recipe.getInternalName())) {
            INSTANCE.removeRecipeByName(recipe.getInternalName());
        }
        INSTANCE.register(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(PlasmaForgeRecipe recipe) {
        INSTANCE.removeRecipeByName(recipe.getInternalName());
        this.addBackup(recipe);
    }

    public void removeByName(String name) {
        PlasmaForgeRecipe recipe = INSTANCE.recipeNameMap.get(name);
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

    public static class RecipeBuilder extends AbstractRecipeBuilder<PlasmaForgeRecipe> {

        private FluidStack fluidIn;
        private int duration = 100;
        private long power = 100;
        private long ignitionTemp = 0;
        private ItemStack icon;
        private boolean named = false;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_plasmaforge_";
        }

        /** Uses hbm's own FluidStack type, not Forge's. */
        public RecipeBuilder inputFluid(FluidStack fluid) {
            this.fluidIn = fluid;
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

        /** Minimum plasma energy needed to run this recipe. */
        public RecipeBuilder ignitionTemp(long ignitionTemp) {
            this.ignitionTemp = ignitionTemp;
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
            return "Error adding Plasma Forge recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 0, 12, 1, 1);
            if (this.input.isEmpty() && this.fluidIn == null) {
                msg.add("Plasma Forge recipe needs at least one item or fluid input");
            }
        }

        @Override
        public PlasmaForgeRecipe register() {
            if (!this.validate()) return null;
            this.validateName();

            PlasmaForgeRecipe recipe = INSTANCE.instantiateRecipe(this.name.toString());

            if (!this.input.isEmpty()) {
                RecipesCommon.AStack[] items = new RecipesCommon.AStack[this.input.size()];
                for (int i = 0; i < items.length; i++) items[i] = IngredientUtils.convertIngredient2Astack(this.input.get(i));
                recipe.inputItems(items);
            }
            if (this.fluidIn != null) recipe.inputFluids(this.fluidIn);
            recipe.outputItems(this.output.toArray(new ItemStack[0]));

            recipe.setup(this.duration, this.power);
            recipe.setInputEnergy(this.ignitionTemp);
            if (this.icon != null) recipe.setIcon(this.icon);
            if (this.named) recipe.setNamed();

            HbmGroovyPropertyContainer.PLASMAFORGE.addRecipe(recipe);
            return recipe;
        }
    }
}
