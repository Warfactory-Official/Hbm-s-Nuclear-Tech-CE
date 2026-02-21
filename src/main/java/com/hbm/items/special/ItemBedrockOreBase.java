package com.hbm.items.special;

import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemOreDensityScanner;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.List;
import java.util.Random;

public class ItemBedrockOreBase extends Item {

    public ItemBedrockOreBase(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.partsTab);

        ModItems.ALL_ITEMS.add(this);
    }

    public static double getOreAmount(ItemStack stack, ItemBedrockOreNew.BedrockOreType type) {
        if(!stack.hasTagCompound()) return 0;
        NBTTagCompound data = stack.getTagCompound();
        return data.getDouble(type.suffix);
    }

    public static void setOreAmount(ItemStack stack, int x, int z) {
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound data = stack.getTagCompound();

        for(ItemBedrockOreNew.BedrockOreType type : ItemBedrockOreNew.BedrockOreType.VALUES) {
            data.setDouble(type.suffix, getOreLevel(x, z, type));
        }
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {

        for(ItemBedrockOreNew.BedrockOreType type : ItemBedrockOreNew.BedrockOreType.VALUES) {
            double amount = this.getOreAmount(stack, type);
            String typeName = I18n.format("item.bedrock_ore.type." + type.suffix + ".name");
            list.add(typeName + ": " + ((int) (amount * 100)) / 100D + " (" + ItemOreDensityScanner.getColor(amount) + I18nUtil.resolveKey(ItemOreDensityScanner.translateDensity(amount)) + TextFormatting.RESET + ")");
        }
    }

    private static NoiseGeneratorPerlin[] ores = new NoiseGeneratorPerlin[ItemBedrockOreNew.BedrockOreType.VALUES.length];
    private static NoiseGeneratorPerlin level;

    public static double getOreLevel(int x, int z, ItemBedrockOreNew.BedrockOreType type) {

        if(level == null) level = new NoiseGeneratorPerlin(new Random(2114043), 4);
        if(ores[type.ordinal()] == null) ores[type.ordinal()] = new NoiseGeneratorPerlin(new Random(2082127 + type.ordinal()), 4);

        double scale = 0.01D;

        return MathHelper.clamp(Math.abs(level.getValue(x * scale, z * scale) * ores[type.ordinal()].getValue(x * scale, z * scale)) * 0.05, 0.0, 2.0);
    }
}
