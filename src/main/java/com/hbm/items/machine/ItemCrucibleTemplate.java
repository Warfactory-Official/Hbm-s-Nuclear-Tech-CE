package com.hbm.items.machine;

import com.hbm.Tags;
import com.hbm.interfaces.IHasCustomModel;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ItemCrucibleTemplate extends Item implements IHasCustomModel {

    public static final ModelResourceLocation location = new ModelResourceLocation(
            Tags.MODID + ":crucible_template", "inventory");

    public ItemCrucibleTemplate(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(MainRegistry.templateTab);

        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if(tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH) {
            for (int i = 0; i < CrucibleRecipes.recipes.size(); i++) {
                list.add(new ItemStack(this, 1, CrucibleRecipes.recipes.get(i).getId()));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {

        CrucibleRecipes.CrucibleRecipe recipe = CrucibleRecipes.indexMapping.get(stack.getItemDamage());

        if(recipe == null) {
            return super.getItemStackDisplayName(stack);
        }

        String s = ("" + I18n.format(this.getTranslationKey() + ".name")).trim();
        String s1 = ("" + I18n.format(recipe.getName())).trim();

        if(s1 != null) {
            s = s + " " + s1;
        }

        return s;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {

        CrucibleRecipes.CrucibleRecipe recipe = CrucibleRecipes.indexMapping.get(stack.getItemDamage());

        if(recipe == null) {
            return;
        }

        list.add(TextFormatting.BOLD + I18nUtil.resolveKey("info.template_out_p"));
        for(Mats.MaterialStack out : recipe.output) {
            list.add(I18nUtil.resolveKey(out.material.getTranslationKey()) + ": " + Mats.formatAmount(out.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)));
        }

        list.add(TextFormatting.BOLD + I18nUtil.resolveKey("info.template_in_p"));

        for(Mats.MaterialStack in : recipe.input) {
            list.add(I18nUtil.resolveKey(in.material.getTranslationKey()) + ": " + Mats.formatAmount(in.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)));
        }
    }

    @Override
    public ModelResourceLocation getResourceLocation() {
        return location;
    }
}
