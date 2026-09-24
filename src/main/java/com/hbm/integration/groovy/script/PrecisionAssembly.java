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
import com.hbm.inventory.recipes.PrecAssRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.PrecAssRecipes.INSTANCE;

/**
 * Dedicated GroovyScript integration for the Precision Assembly Machine (com.hbm.inventory.recipes.PrecAssRecipes).
 * Exposed as mods.hbm.precisionAssembly - explicit name to avoid the recipeOverrides RecipeFileBinding
 * alias "precisionassembly" (see AssemblyMachine's javadoc for the full rationale).
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class PrecisionAssembly extends VirtualizedRegistry<GenericRecipe> {

    public PrecisionAssembly() {
        super(Collections.singletonList("precisionAssembly"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(GenericRecipe recipe) {
        if (INSTANCE.recipeNameMap.containsKey(recipe.getInternalName())) {
            INSTANCE.removeRecipeByName(recipe.getInternalName());
        }
        INSTANCE.register(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(GenericRecipe recipe) {
        INSTANCE.removeRecipeByName(recipe.getInternalName());
        this.addBackup(recipe);
    }

    public void removeByName(String name) {
        GenericRecipe recipe = INSTANCE.recipeNameMap.get(name);
        if (recipe == null) return;
        this.removeRecipe(recipe);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return INSTANCE.recipeOrderedList.size();
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public boolean hasRecipe(String internalName) {
        return INSTANCE.recipeNameMap.containsKey(internalName);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public void debugDump() {
        GroovyLog.get().infoMC("[hbm] precisionAssembly: {} total recipes ({} stock/loaded, {} added via groovy this session)",
                INSTANCE.recipeOrderedList.size(), INSTANCE.recipeOrderedList.size() - this.getScriptedRecipes().size(), this.getScriptedRecipes().size());
        for (GenericRecipe recipe : this.getScriptedRecipes()) {
            String name = recipe.getInternalName();
            boolean inMap = INSTANCE.recipeNameMap.get(name) == recipe;
            boolean inOrderedList = INSTANCE.recipeOrderedList.contains(recipe);
            GroovyLog.get().infoMC("[hbm]   scripted recipe '{}': inRecipeNameMap={} inRecipeOrderedList={}", name, inMap, inOrderedList);
        }
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<GenericRecipe> {

        private FluidStack fluidIn;
        private FluidStack fluidOut;
        private int duration = 100;
        private long power = 100;
        private ItemStack icon;
        private boolean named = false;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_precisionassembly_";
        }

        /** Uses hbm's own FluidStack type, not Forge's - construct with e.g. new FluidStack(Fluids.WATER, 2000). */
        public RecipeBuilder inputFluid(FluidStack fluid) {
            this.fluidIn = fluid;
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
            return "Error adding Precision Assembly recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 0, 9, 0, 9);
            if (this.input.isEmpty() && this.fluidIn == null) {
                msg.add("Precision Assembly recipe needs at least one item or fluid input");
            }
            if (this.output.isEmpty() && this.fluidOut == null) {
                msg.add("Precision Assembly recipe needs at least one item or fluid output");
            }
        }

        @Override
        public GenericRecipe register() {
            if (!this.validate()) return null;
            this.validateName();

            GenericRecipe recipe = INSTANCE.instantiateRecipe(this.name.toString());

            if (!this.input.isEmpty()) {
                RecipesCommon.AStack[] items = new RecipesCommon.AStack[this.input.size()];
                for (int i = 0; i < items.length; i++) items[i] = IngredientUtils.convertIngredient2Astack(this.input.get(i));
                recipe.inputItems(items);
            }
            if (this.fluidIn != null) recipe.inputFluids(this.fluidIn);
            if (!this.output.isEmpty()) recipe.outputItems(this.output.toArray(new ItemStack[0]));
            if (this.fluidOut != null) recipe.outputFluids(this.fluidOut);

            recipe.setup(this.duration, this.power);
            if (this.icon != null) recipe.setIcon(this.icon);
            if (this.named) recipe.setNamed();

            HbmGroovyPropertyContainer.PRECISIONASSEMBLY.addRecipe(recipe);
            return recipe;
        }
    }
}
