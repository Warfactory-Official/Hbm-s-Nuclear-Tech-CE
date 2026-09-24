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
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.AssemblyMachineRecipes.INSTANCE;

/**
 * Dedicated GroovyScript integration for the Assembly Machine (com.hbm.inventory.recipes.AssemblyMachineRecipes).
 * Exposed as mods.hbm.assemblyMachine. The name is explicit (not derived from this class's simple
 * name) because the recipeOverrides system already auto-registers a RecipeFileBinding under the alias
 * "assemblymachine" (all lowercase) for this same machine - using the class-name-derived default would
 * silently collide with, and lose to, that existing binding.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class AssemblyMachine extends VirtualizedRegistry<GenericRecipe> {

    public AssemblyMachine() {
        super(Collections.singletonList("assemblyMachine"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(GenericRecipe recipe) {
        // Singleplayer runs postInit scripts once for the client-side registry and once for the
        // server-side one (same shared INSTANCE, different passes) - a script using a deterministic
        // .name(...) therefore calls register() twice with the same internal name. GenericRecipes.
        // register() hard-throws on a name already present, so treat a same-name re-registration as
        // an update (remove-then-add) rather than a conflict.
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

    /** Removes a recipe (stock or previously scripted) by its internal name, e.g. as read off recipe.getInternalName(). */
    public void removeByName(String name) {
        GenericRecipe recipe = INSTANCE.recipeNameMap.get(name);
        if (recipe == null) return;
        this.removeRecipe(recipe);
    }

    // ==================== Debug / verification ====================
    // Callable directly from a groovy script (mods.hbm.assemblyMachine.debugDump()) or a
    // one-off diagnostic line - lets us confirm the live registration state without opening
    // any in-game GUI or guessing from script log output. See ChemicalPlant for the twin
    // implementation - deliberately not shared, per the "one dedicated class per machine"
    // decision (a shared debug helper would reintroduce the same kind of hidden-behavior
    // surprise that sank the original GenericMachine attempt).

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return INSTANCE.recipeOrderedList.size();
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public boolean hasRecipe(String internalName) {
        return INSTANCE.recipeNameMap.containsKey(internalName);
    }

    /**
     * Logs (to groovy.log AND the in-game chat/console via infoMC) the total recipe count plus,
     * for every recipe added by a groovy script this session, whether it is still actually present
     * in the live recipeNameMap/recipeOrderedList and whether it would pass the recipe-selector
     * GUI's own visibility filter (GUIScreenRecipeSelector.regenerateRecipes(): !isPooled() unless
     * the currently installed blueprint's pool matches). A scripted recipe reported here but not
     * showing up in-game points at something GUI-side; one NOT reported here (or reported as
     * missing from the live map) points at a registration/reload bug.
     */
    @MethodDescription(type = MethodDescription.Type.QUERY)
    public void debugDump() {
        GroovyLog.get().infoMC("[hbm] assemblyMachine: {} total recipes ({} stock/loaded, {} added via groovy this session)",
                INSTANCE.recipeOrderedList.size(), INSTANCE.recipeOrderedList.size() - this.getScriptedRecipes().size(), this.getScriptedRecipes().size());
        for (GenericRecipe recipe : this.getScriptedRecipes()) {
            String name = recipe.getInternalName();
            boolean inMap = INSTANCE.recipeNameMap.get(name) == recipe;
            boolean inOrderedList = INSTANCE.recipeOrderedList.contains(recipe);
            boolean guiSelectorVisible = !recipe.isPooled(); // matches GUIScreenRecipeSelector's own filter when no blueprint is installed
            GroovyLog.get().infoMC("[hbm]   scripted recipe '{}': inRecipeNameMap={} inRecipeOrderedList={} visibleInSelectorWithNoBlueprint={}",
                    name, inMap, inOrderedList, guiSelectorVisible);
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
            return "groovyscript_assemblymachine_";
        }

        /**
         * The Assembly Machine uses hbm's own FluidStack type, not Forge's - so unlike items, fluid
         * can't go through the base builder's fluidInput()/fluidOutput() (Forge-typed, unused here).
         * Construct with e.g. new FluidStack(Fluids.WATER, 2000).
         */
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

        /** Marks the recipe as using a custom localized name (resolved via I18n) instead of the icon's item name. */
        public RecipeBuilder named() {
            this.named = true;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Assembly Machine recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 0, 12, 0, 1);
            if (this.input.isEmpty() && this.fluidIn == null) {
                msg.add("Assembly Machine recipe needs at least one item or fluid input");
            }
            if (this.output.isEmpty() && this.fluidOut == null) {
                msg.add("Assembly Machine recipe needs at least one item or fluid output");
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

            HbmGroovyPropertyContainer.ASSEMBLYMACHINE.addRecipe(recipe);
            return recipe;
        }
    }
}
