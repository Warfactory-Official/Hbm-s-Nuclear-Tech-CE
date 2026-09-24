package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes;
import net.minecraft.item.ItemStack;

import java.util.Collections;

import static com.hbm.inventory.recipes.ParticleAcceleratorRecipes.recipes;

/**
 * Dedicated GroovyScript integration for the Particle Accelerator (com.hbm.inventory.recipes.ParticleAcceleratorRecipes).
 * Exposed as mods.hbm.particleAccelerator. Exactly 2 required item inputs (order-independent - the
 * game matches either way round) plus a momentum threshold, 1 required + 1 optional item output.
 */
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class ParticleAccelerator extends VirtualizedRegistry<ParticleAcceleratorRecipes.ParticleAcceleratorRecipe> {

    public ParticleAccelerator() {
        super(Collections.singletonList("particleAccelerator"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(this::removeRecipe);
        restoreFromBackup().forEach(this::addRecipe);
    }

    private void addRecipe(ParticleAcceleratorRecipes.ParticleAcceleratorRecipe recipe) {
        recipes.add(recipe);
        this.addScripted(recipe);
    }

    private void removeRecipe(ParticleAcceleratorRecipes.ParticleAcceleratorRecipe recipe) {
        recipes.remove(recipe);
        this.addBackup(recipe);
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public int size() {
        return recipes.size();
    }

    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<ParticleAcceleratorRecipes.ParticleAcceleratorRecipe> {

        private IIngredient input1;
        private IIngredient input2;
        private int momentum = 0;

        @Override
        public String getRecipeNamePrefix() {
            return "groovyscript_particleaccelerator_";
        }

        /** The two colliding particle/item inputs - order doesn't matter, the game matches either way round. */
        public RecipeBuilder inputs(IIngredient input1, IIngredient input2) {
            this.input1 = input1;
            this.input2 = input2;
            return this;
        }

        /** Minimum collision momentum required for this recipe to trigger. */
        public RecipeBuilder momentum(int momentum) {
            this.momentum = momentum;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Particle Accelerator recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            if (this.input1 == null || this.input2 == null) {
                msg.add("Particle Accelerator recipe needs inputs(a, b)");
            }
            if (this.output.isEmpty()) {
                msg.add("Particle Accelerator recipe needs at least one output");
            }
            if (this.output.size() > 2) {
                msg.add("Particle Accelerator recipe allows at most 2 outputs");
            }
        }

        @Override
        public ParticleAcceleratorRecipes.ParticleAcceleratorRecipe register() {
            if (!this.validate()) return null;

            ItemStack output2 = this.output.size() > 1 ? this.output.get(1) : null;
            ParticleAcceleratorRecipes.ParticleAcceleratorRecipe recipe = new ParticleAcceleratorRecipes.ParticleAcceleratorRecipe(
                    IngredientUtils.convertIngredient2Astack(this.input1),
                    IngredientUtils.convertIngredient2Astack(this.input2),
                    this.momentum, this.output.get(0), output2);
            HbmGroovyPropertyContainer.PARTICLEACCELERATOR.addRecipe(recipe);
            return recipe;
        }
    }
}
