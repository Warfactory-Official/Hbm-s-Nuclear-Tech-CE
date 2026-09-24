package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.recipes.HeatRecipes;

import java.util.Collections;

import static com.hbm.inventory.recipes.HeatRecipes.boilRecipes;
import static com.hbm.inventory.recipes.HeatRecipes.coolRecipes;

/**
 * Dedicated GroovyScript integration for Heat/boiling (com.hbm.inventory.recipes.HeatRecipes).
 * Exposed as mods.hbm.heat. By default registers both a boil recipe (cold -&gt; hot) and its
 * reverse cool recipe (hot -&gt; cold) - call boilOnly()/coolOnly() to register just one direction.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class Heat extends VirtualizedRegistry<Heat.HeatEntry> {

    public Heat() {
        super(Collections.singletonList("heat"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(HeatEntry entry) {
        if (entry.registerBoil) boilRecipes.put(entry.cold.type, new HeatRecipes.HeatRecipe(entry.cold, entry.hot, entry.heat));
        if (entry.registerCool) coolRecipes.put(entry.hot.type, new HeatRecipes.HeatRecipe(entry.hot, entry.cold, entry.heat));
        this.addScripted(entry);
    }

    private void removeRecipe(HeatEntry entry) {
        if (entry.registerBoil) boilRecipes.remove(entry.cold.type);
        if (entry.registerCool) coolRecipes.remove(entry.hot.type);
        this.addBackup(entry);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return boilRecipes.size() + coolRecipes.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    /** Plain data holder for one boil/cool registration - not a hbm type, just this integration's bookkeeping unit. */
    public static class HeatEntry {
        final FluidStack cold;
        final FluidStack hot;
        final int heat;
        final boolean registerBoil;
        final boolean registerCool;

        HeatEntry(FluidStack cold, FluidStack hot, int heat, boolean registerBoil, boolean registerCool) {
            this.cold = cold;
            this.hot = hot;
            this.heat = heat;
            this.registerBoil = registerBoil;
            this.registerCool = registerCool;
        }
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<HeatEntry> {

        private FluidStack cold;
        private FluidStack hot;
        private int heat = 100;
        private boolean registerBoil = true;
        private boolean registerCool = true;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_heat_";
        }

        public RecipeBuilder cold(FluidStack cold) {
            this.cold = cold;
            return this;
        }

        public RecipeBuilder hot(FluidStack hot) {
            this.hot = hot;
            return this;
        }

        public RecipeBuilder heat(int heat) {
            this.heat = heat;
            return this;
        }

        /** Only register the cold -> hot boil direction, skip the reverse cool recipe. */
        public RecipeBuilder boilOnly() {
            this.registerCool = false;
            return this;
        }

        /** Only register the hot -> cold cool direction, skip the boil recipe. */
        public RecipeBuilder coolOnly() {
            this.registerBoil = false;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Heat recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.cold == null || this.hot == null) {
                msg.add("Heat recipe needs both cold(...) and hot(...) fluids");
            }
        }

        @Override
        public HeatEntry register() {
            if (!this.validate()) return null;

            HeatEntry entry = new HeatEntry(this.cold, this.hot, this.heat, this.registerBoil, this.registerCool);
            HbmGroovyPropertyContainer.HEAT.addRecipe(entry);
            return entry;
        }
    }
}
