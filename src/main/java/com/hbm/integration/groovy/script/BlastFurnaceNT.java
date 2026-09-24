package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.recipes.BlastFurnaceRecipe;
import com.hbm.inventory.recipes.BlastFurnaceRecipesNT;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.BlastFurnaceRecipesNT.INSTANCE;

/**
 * Dedicated GroovyScript integration for the (newer) Blast Furnace, com.hbm.inventory.recipes.
 * BlastFurnaceRecipesNT - a separate, additional machine from the legacy one already covered by
 * this mod's own BlastFurnace/BlastFurnaceFuel builders (different file: hbmBlastFurnace.json vs
 * hbmBlastFurnaceLegacy.json). Exposed as mods.hbm.blastFurnaceNT to avoid ambiguity with those.
 * <p>
 * GenericRecipes&lt;BlastFurnaceRecipe&gt; - BlastFurnaceRecipe adds no extra fields over the base
 * GenericRecipe, so this is otherwise identical in shape to AssemblyMachine/ChemicalPlant.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class BlastFurnaceNT extends VirtualizedRegistry<BlastFurnaceRecipe> {

    public BlastFurnaceNT() {
        super(Collections.singletonList("blastFurnaceNT"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(BlastFurnaceRecipe recipe) {
        if (INSTANCE.recipeNameMap.containsKey(recipe.getInternalName())) {
            INSTANCE.removeRecipeByName(recipe.getInternalName());
        }
        INSTANCE.register(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(BlastFurnaceRecipe recipe) {
        INSTANCE.removeRecipeByName(recipe.getInternalName());
        this.addBackup(recipe);
    }

    public void removeByName(String name) {
        BlastFurnaceRecipe recipe = INSTANCE.recipeNameMap.get(name);
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

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<BlastFurnaceRecipe> {

        private int duration = 100;
        private long power = 100;
        private ItemStack icon;
        private boolean named = false;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_blastfurnacent_";
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
            return "Error adding Blast Furnace (NT) recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 1, 2, 1, 2);
        }

        @Override
        public BlastFurnaceRecipe register() {
            if (!this.validate()) return null;
            this.validateName();

            BlastFurnaceRecipe recipe = INSTANCE.instantiateRecipe(this.name.toString());

            RecipesCommon.AStack[] items = new RecipesCommon.AStack[this.input.size()];
            for (int i = 0; i < items.length; i++) items[i] = IngredientUtils.convertIngredient2Astack(this.input.get(i));
            recipe.inputItems(items);
            recipe.outputItems(this.output.toArray(new ItemStack[0]));

            recipe.setup(this.duration, this.power);
            if (this.icon != null) recipe.setIcon(this.icon);
            if (this.named) recipe.setNamed();

            HbmGroovyPropertyContainer.BLASTFURNACENT.addRecipe(recipe);
            return recipe;
        }
    }
}
