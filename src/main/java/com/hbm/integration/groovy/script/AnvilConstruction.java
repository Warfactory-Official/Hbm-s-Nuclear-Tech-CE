package com.hbm.integration.groovy.script;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import com.cleanroommc.groovyscript.registry.StandardListRegistry;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.integration.groovy.script.recipes.GroovyAnvilConstructionRecipe;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@RegistryDescription(linkGenerator = "hbm", isFullyDocumented = false)
public class AnvilConstruction extends StandardListRegistry<AnvilRecipes.AnvilConstructionRecipe> {
    @Override
    public Collection<AnvilRecipes.AnvilConstructionRecipe> getRecipes() {
        return AnvilRecipes.getConstruction();
    }

    @RecipeBuilderDescription(example = {
            @Example(".input(item('minecraft:clay'), item('minecraft:clay')).OverlayConstruction().output(item('minecraft:diamond')).register()"),
            @Example(".input(item('minecraft:clay')).output(item('minecraft:diamond')*2, item('minecraft:diamond')*2).OverlayRecycling().maxTier(4).register()")
    })
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL)
    public void removeAllConstruction(){
        this.removeAllRecipeFromType(AnvilRecipes.OverlayType.CONSTRUCTION);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL)
    public void removeAllRecycling(){
        this.removeAllRecipeFromType(AnvilRecipes.OverlayType.RECYCLING);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL)
    public void removeAllSmithing(){
        this.removeAllRecipeFromType(AnvilRecipes.OverlayType.SMITHING);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL)
    public void removeAllNone(){
        this.removeAllRecipeFromType(AnvilRecipes.OverlayType.NONE);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('minecraft:clay')"))
    public void removeAllConstruction(IIngredient ingredient){
        this.removeAllRecipeFromTypeAndOutput(AnvilRecipes.OverlayType.CONSTRUCTION, ingredient);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('minecraft:clay')"))
    public void removeAllRecycling(IIngredient ingredient){
        this.removeAllRecipeFromTypeAndOutput(AnvilRecipes.OverlayType.RECYCLING, ingredient);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('minecraft:clay')"))
    public void removeAllSmithing(IIngredient ingredient){
        this.removeAllRecipeFromTypeAndOutput(AnvilRecipes.OverlayType.SMITHING, ingredient);
    }

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("item('minecraft:clay')"))
    public void removeAllNone(IIngredient ingredient){
        this.removeAllRecipeFromTypeAndOutput(AnvilRecipes.OverlayType.NONE, ingredient);
    }

    public void removeAllRecipeFromType(AnvilRecipes.OverlayType type){
        removeAllRecipeFromTypeAndOutput(type, null);
    }

    public void removeAllRecipeFromTypeAndOutput(AnvilRecipes.OverlayType type, @Nullable IIngredient ingredient){
        Iterator<AnvilRecipes.AnvilConstructionRecipe> recipe = this.getRecipes().iterator();
        while(recipe.hasNext()) {
            AnvilRecipes.AnvilConstructionRecipe rec = recipe.next();
            for(AnvilRecipes.AnvilOutput out : rec.output) {
                if (rec.getOverlay() == type && (ingredient == null || ingredient.test(out.stack))) {
                    this.remove(rec);
                    break;
                }
            }
        }
    }

    public static class RecipeBuilder extends AbstractRecipeBuilder<AnvilRecipes.AnvilConstructionRecipe> {
        @Property(defaultValue = "1", comp = @Comp(gt = 0))
        private int minTier = 1;
        @Property(defaultValue = "1916169", comp = @Comp(lte = 0))
        private int maxTier = 1916169;
        @Property(defaultValue = "NONE", comp = @Comp(not = "null"))
        private AnvilRecipes.OverlayType overlayType = AnvilRecipes.OverlayType.NONE;
        public List<Float> chances = new ArrayList<>();

        @RecipeBuilderMethodDescription(field = "overlayType")
        public RecipeBuilder OverlayConstruction(){
            this.overlayType = AnvilRecipes.OverlayType.CONSTRUCTION;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "overlayType")
        public RecipeBuilder OverlayNone(){
            this.overlayType = AnvilRecipes.OverlayType.NONE;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "overlayType")
        public RecipeBuilder OverlayRecycling(){
            this.overlayType = AnvilRecipes.OverlayType.RECYCLING;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "overlayType")
        public RecipeBuilder OverlaySmithing(){
            this.overlayType = AnvilRecipes.OverlayType.SMITHING;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "minTier")
        public RecipeBuilder minTier(int tier){
            this.minTier = tier;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "minTier")
        public RecipeBuilder minTierIron(){
            this.minTier = 1;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "minTier")
        public RecipeBuilder minTierSteel(){
            this.minTier = 1;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "minTier")
        public RecipeBuilder minTierMeteorite(){
            this.minTier = 2;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "minTier")
        public RecipeBuilder minTierStarmetal(){
            this.minTier = 3;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "minTier")
        public RecipeBuilder minTierFerrouranium(){
            this.minTier = 4;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "minTier")
        public RecipeBuilder minTierBismuth(){
            this.minTier = 5;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "minTier")
        public RecipeBuilder minTierSchrabidate(){
            this.minTier = 6;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "minTier")
        public RecipeBuilder minTierDNT(){
            this.minTier = 7;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "minTier")
        public RecipeBuilder minTierOsmiridium(){
            this.minTier = 8;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "minTier")
        public RecipeBuilder minTierMurky(){
            this.minTier = 1916169;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "maxTier")
        public RecipeBuilder maxTier(int tier){
            this.maxTier = tier;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "maxTier")
        public RecipeBuilder maxTierIron(){
            this.maxTier = 1;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "maxTier")
        public RecipeBuilder maxTierSteel(){
            this.maxTier = 1;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "maxTier")
        public RecipeBuilder maxTierMeteorite(){
            this.maxTier = 2;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "maxTier")
        public RecipeBuilder maxTierStarmetal(){
            this.maxTier = 3;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "maxTier")
        public RecipeBuilder maxTierFerrouranium(){
            this.maxTier = 4;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "maxTier")
        public RecipeBuilder maxTierBismuth(){
            this.maxTier = 5;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "maxTier")
        public RecipeBuilder maxTierSchrabidate(){
            this.maxTier = 6;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "maxTier")
        public RecipeBuilder maxTierDNT(){
            this.maxTier = 7;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "maxTier")
        public RecipeBuilder maxTierOsmiridium(){
            this.maxTier = 8;
            return this;
        }
        @RecipeBuilderMethodDescription(field = "maxTier")
        public RecipeBuilder maxTierMurky(){
            this.maxTier = 1916169;
            return this;
        }
        @RecipeBuilderMethodDescription
        public RecipeBuilder output(ItemStack stack, float chance){
            super.output(stack);
            this.chances.add(chance);
            return this;
        }
        @RecipeBuilderMethodDescription
        public AbstractRecipeBuilder<AnvilRecipes.AnvilConstructionRecipe> output(float chance, ItemStack... outputs) {
            for(ItemStack stack:outputs){
                this.output(stack, chance);
            }
            return this;
        }
        @RecipeBuilderMethodDescription
        public AbstractRecipeBuilder<AnvilRecipes.AnvilConstructionRecipe> output(float chance, Collection<ItemStack> outputs) {
            for(ItemStack stack:outputs){
                this.output(stack, chance);
            }
            return this;
        }
        @RecipeBuilderMethodDescription
        public RecipeBuilder output(ItemStack stack){
            return this.output(stack, 1.0f);
        }
        @RecipeBuilderMethodDescription
        @Override
        public AbstractRecipeBuilder<AnvilRecipes.AnvilConstructionRecipe> output(ItemStack... outputs) {
            return this.output(1.0f, outputs);
        }

        @RecipeBuilderMethodDescription
        @Override
        public AbstractRecipeBuilder<AnvilRecipes.AnvilConstructionRecipe> output(Collection<ItemStack> outputs) {
            return this.output(1.0f, outputs);
        }

        @Override
        public String getErrorMsg() {
            return "Error adding NTM anvil construction recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            int maxOutput = this.overlayType == AnvilRecipes.OverlayType.RECYCLING? Integer.MAX_VALUE:1;
            this.validateItems(msg, 1, Integer.MAX_VALUE, 1, maxOutput);
        }

        @Override
        @RecipeBuilderRegistrationMethod
        public AnvilRecipes.AnvilConstructionRecipe register() {
            if (!this.validate()) {
                return null;
            }

            ArrayList<AnvilRecipes.AnvilOutput> outputs = new ArrayList<>();
            for(ItemStack stack:this.output){
                outputs.add(new AnvilRecipes.AnvilOutput(stack, this.chances.get(this.output.indexOf(stack))));
            }

            GroovyAnvilConstructionRecipe recipe = new GroovyAnvilConstructionRecipe(this.input, outputs.toArray(new AnvilRecipes.AnvilOutput[0]), this.overlayType, this.minTier, this.maxTier);
            HbmGroovyPropertyContainer.ANVILCONSTRUCTION.add(recipe);
            return recipe;
        }
    }

}