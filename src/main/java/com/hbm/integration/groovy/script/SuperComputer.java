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
import com.hbm.inventory.recipes.SuperComputerRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutput;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutputMulti;
import com.hbm.inventory.recipes.loader.GenericRecipes.IOutput;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hbm.inventory.recipes.SuperComputerRecipes.INSTANCE;

/**
 * Dedicated GroovyScript integration for the Super Computer (com.hbm.inventory.recipes.SuperComputerRecipes).
 * Exposed as mods.hbm.superComputer, following the same "one dedicated class per machine, native builder,
 * no config-JSON-override hack" approach as AssemblyMachine/ChemicalPlant/ArcWelder/Soldering - see
 * AssemblyMachine's own class javadoc for why the explicit alias matters and why this shape was chosen.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class SuperComputer extends VirtualizedRegistry<GenericRecipe> {

    public SuperComputer() {
        super(Collections.singletonList("superComputer"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(GenericRecipe recipe) {
        // See AssemblyMachine#addRecipe: singleplayer runs postInit scripts twice (client + server pass
        // over the same shared INSTANCE), so a script using a deterministic .name(...) calls register()
        // twice with the same internal name - treat a same-name re-registration as an update rather than
        // letting GenericRecipes.register()'s hard "duplicate ID" throw take the whole load down.
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
    // See AssemblyMachine for the twin implementation and why it's not shared (deliberately one dedicated
    // class per machine, no hidden shared-behavior surprises).

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
        GroovyLog.get().infoMC("[hbm] superComputer: {} total recipes ({} stock/loaded, {} added via groovy this session)",
                INSTANCE.recipeOrderedList.size(), INSTANCE.recipeOrderedList.size() - this.getScriptedRecipes().size(), this.getScriptedRecipes().size());
        for (GenericRecipe recipe : this.getScriptedRecipes()) {
            String name = recipe.getInternalName();
            boolean inMap = INSTANCE.recipeNameMap.get(name) == recipe;
            boolean inOrderedList = INSTANCE.recipeOrderedList.contains(recipe);
            boolean guiSelectorVisible = !recipe.isPooled();
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
        // A separate output "slot" from the base class's plain output(...) list (which is always
        // guaranteed, 100% chance): entries added here compete against each other by weight, and
        // exactly ONE of them drops per completed craft - e.g. a 90/10 success-vs-broken-drive pair,
        // matching how every stock Super Computer recipe (registerTriplet/registerCopy in
        // SuperComputerRecipes.java) actually works. Counts as a single output slot regardless of how
        // many alternatives are in the pool.
        private final List<ChanceOutput> chancePool = new ArrayList<>();

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_supercomputer_";
        }

        /**
         * The Super Computer uses hbm's own FluidStack type, not Forge's - so unlike items, fluid
         * can't go through the base builder's fluidInput()/fluidOutput() (Forge-typed, unused here).
         * Construct with e.g. new FluidStack(Fluids.WATER, 16_000).
         */
        public RecipeBuilder inputFluid(FluidStack fluid) {
            this.fluidIn = fluid;
            return this;
        }

        public RecipeBuilder outputFluid(FluidStack fluid) {
            this.fluidOut = fluid;
            return this;
        }

        /**
         * Adds one weighted alternative to this recipe's chance-output slot. Call this two or more
         * times to build a weighted pool - whichever entries are registered here compete against each
         * other by weight every time the recipe completes, and exactly one of them is produced (never
         * more than one, never none). Weights don't need to sum to 100, but doing so keeps them
         * readable as percentages, matching the stock recipes' own convention (e.g. 90 and 10 for a
         * 90% success / 10% chance of a broken drive instead).
         * <p>
         * This is a wholly separate slot from output(...): stacks added via output(...) always drop
         * in addition to whatever wins this pool.
         */
        public RecipeBuilder chanceOutput(ItemStack stack, int weight) {
            this.chancePool.add(new ChanceOutput(stack, weight));
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
            return "Error adding Super Computer recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 0, 3, 0, 3);
            if (this.input.isEmpty() && this.fluidIn == null) {
                msg.add("Super Computer recipe needs at least one item or fluid input");
            }
            if (this.output.isEmpty() && this.chancePool.isEmpty() && this.fluidOut == null) {
                msg.add("Super Computer recipe needs at least one item or fluid output");
            }
            if (this.chancePool.size() == 1) {
                msg.add("chanceOutput(...) was only called once - a single entry always wins its own pool no matter the weight, so this always behaves like a guaranteed output. Did you mean output(...), or did you forget a second chanceOutput(...) call (e.g. a broken-item fallback)?");
            }
            int outputSlots = this.output.size() + (this.chancePool.isEmpty() ? 0 : 1);
            if (outputSlots > 3) {
                msg.add("Super Computer recipe has too many output slots: {} guaranteed output(s) + {} chance-pool slot, {} total, max 3", this.output.size(), this.chancePool.isEmpty() ? 0 : 1, outputSlots);
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

            List<IOutput> outputs = new ArrayList<>();
            for (ItemStack stack : this.output) outputs.add(new ChanceOutput(stack));
            if (!this.chancePool.isEmpty()) outputs.add(new ChanceOutputMulti(this.chancePool.toArray(new ChanceOutput[0])));
            if (!outputs.isEmpty()) recipe.outputItems(outputs.toArray(new IOutput[0]));
            if (this.fluidOut != null) recipe.outputFluids(this.fluidOut);

            recipe.setup(this.duration, this.power);
            if (this.icon != null) recipe.setIcon(this.icon);
            if (this.named) recipe.setNamed();

            HbmGroovyPropertyContainer.SUPERCOMPUTER.addRecipe(recipe);
            return recipe;
        }
    }
}
