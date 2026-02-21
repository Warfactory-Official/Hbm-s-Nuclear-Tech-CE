package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.StandardListRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.script.recipes.GroovyAnvilSmithingRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilSmithingRecipe;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class AnvilSmithing extends StandardListRegistry<AnvilSmithingRecipe> {

    @Override
    public Collection<AnvilSmithingRecipe> getRecipes() {
        return AnvilRecipes.getSmithing();
    }

    @RecipeBuilderDescription(example = {
            @Example(".input(item('minecraft:clay'), item('minecraft:clay')).tierIron().output(item('minecraft:diamond'))"),
            @Example(".input(item('minecraft:clay'), item('minecraft:clay')).tier(2).output(item('minecraft:diamond')*2)")
    })
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @MethodDescription(example = @Example("item('minecraft:clay')"))
    public void removeByOutput(ItemStack item) {
        for (AnvilSmithingRecipe recipe : AnvilRecipes.getSmithing().stream().filter((x) -> x.getSimpleOutput().isItemEqual(item)).collect(Collectors.toList())) {
            this.remove(recipe);
        }
    }

    @Property(property = "input", comp = @Comp(gte=1))
    @Property(property = "input", comp = @Comp(lte=2))
    @Property(property = "output", comp = @Comp(eq=1))
    public static class RecipeBuilder extends AbstractRecipeBuilder<AnvilSmithingRecipe> {

        @Property(defaultValue = "1", comp = @Comp(gt = 0))
        int tier = 1;

        @RecipeBuilderMethodDescription(field = "tier")
        public RecipeBuilder tier(int tier){
            this.tier = tier;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "tier")
        public RecipeBuilder tierIron(){
            this.tier = 1;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "tier")
        public RecipeBuilder tierSteel(){
            this.tier = 1;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "tier")
        public RecipeBuilder tierMeteorite(){
            this.tier = 2;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "tier")
        public RecipeBuilder tierStarmetal(){
            this.tier = 3;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "tier")
        public RecipeBuilder tierFerrouranium(){
            this.tier = 4;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "tier")
        public RecipeBuilder tierBismuth(){
            this.tier = 5;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "tier")
        public RecipeBuilder tierSchrabidate(){
            this.tier = 6;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "tier")
        public RecipeBuilder tierDNT(){
            this.tier = 7;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "tier")
        public RecipeBuilder tierOsmiridium(){
            this.tier = 8;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "tier")
        public RecipeBuilder tierMurky(){
            this.tier = 1916169;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding NTM anvil recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            this.validateItems(msg, 1, 2, 1, 1);
            msg.add(this.tier < 1 || this.tier > 1916169, "tier must be between 0 (iron) and 1916169 (Murky), yet it was {}. (Highest tier that player can obtain in vanilla setup is 8.)", this.tier);
        }

        @Override
        @RecipeBuilderRegistrationMethod
        public AnvilSmithingRecipe register() {
            if (!this.validate()) {
                return null;
            }
            GroovyAnvilSmithingRecipes recipe = new GroovyAnvilSmithingRecipes(this.tier, this.output.get(0), this.input);
            HbmGroovyPropertyContainer.ANVILSMITHING.add(recipe);
            return recipe;
        }
    }

}
