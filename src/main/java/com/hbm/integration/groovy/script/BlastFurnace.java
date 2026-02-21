package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.StandardListRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.util.IngredientUtils;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;

import java.util.Collection;

import static com.hbm.inventory.recipes.BlastFurnaceRecipes.blastFurnaceRecipes;

@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class BlastFurnace extends StandardListRegistry<Tuple.Triplet<Object, Object, ItemStack>> {
    @Override
    public Collection<Tuple.Triplet<Object, Object, ItemStack>> getRecipes() {
        return blastFurnaceRecipes;
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<Tuple.Triplet<Object, Object, ItemStack>>{

        @Override
        public String getErrorMsg() {
            return "Error adding NTM BlastFurnace Recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 2, 2, 1, 1);
        }

        @Override
        public Tuple.Triplet<Object, Object, ItemStack> register() {
            if (!this.validate()) {
                return null;
            }
            Tuple.Triplet<Object, Object, ItemStack> recipe = new Tuple.Triplet<>(IngredientUtils.convertIngredient2Astack(this.input.get(0)), IngredientUtils.convertIngredient2Astack(this.input.get(1)), this.output.get(0));
            HbmGroovyPropertyContainer.BLASTFURNACE.add(recipe);
            return recipe;
        }
    }
}
