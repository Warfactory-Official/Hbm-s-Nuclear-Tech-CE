package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class ItemZirnoxRod extends ItemEnumMulti<ItemZirnoxRod.EnumZirnoxType> {

    public ItemZirnoxRod(String registryName) {
        super(registryName, EnumZirnoxType.VALUES, true, true);
        this.setMaxStackSize(1);
        this.canRepair = false;
    }

    public static void incrementLifeTime(ItemStack stack) {

        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        int time = stack.getTagCompound().getInteger("life");

        stack.getTagCompound().setInteger("life", time + 1);
    }

    public static void setLifeTime(ItemStack stack, int time) {

        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setInteger("life", time);
    }

    public static int getLifeTime(ItemStack stack) {

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
            return 0;
        }

        return stack.getTagCompound().getInteger("life");
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getDurabilityForDisplay(stack) > 0D;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        EnumZirnoxType num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
        return (double) getLifeTime(stack) / (double) num.maxLife;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {

        EnumZirnoxType num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
        list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmk.depletion", ((int) ((((double) getLifeTime(stack)) / (double) num.maxLife) * 100000)) / 1000D + "%"));
        String[] loc;

        if (num.breeding)
            loc = I18nUtil.resolveKeyArray("desc.item.zirnoxBreedingRod", BobMathUtil.getShortNumber(num.maxLife));
        else
            loc = I18nUtil.resolveKeyArray("desc.item.zirnoxRod", num.heat, BobMathUtil.getShortNumber(num.maxLife));

        Collections.addAll(list, loc);
    }

    //TODO: Make this more inline with new ItemEnum standards
//    @SideOnly(Side.CLIENT)
//    public void registerModel() {
//        Enum[] enums = theEnum.getEnumConstants();
//
//        for (Enum num : enums) {
//            ModelResourceLocation modelResourceLocation = new ModelResourceLocation(
//                    new ResourceLocation(this.getRegistryName() + "_" + num.name().toLowerCase(Locale.US)), "inventory");
//            ModelLoader.setCustomModelResourceLocation(this, num.ordinal(), modelResourceLocation);
//        }
//    }

//    @SideOnly(Side.CLIENT)
//    @Override
//    public void bakeModel(ModelBakeEvent event){}
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    public void registerSprite(TextureMap map){}


    public enum EnumZirnoxType {
        NATURAL_URANIUM_FUEL(250_000, 30),
        URANIUM_FUEL(200_000, 50),
        TH232_FUEL(20_000, 0, true),
        THORIUM_FUEL(200_000, 40),
        MOX_FUEL(165_000, 75),
        PLUTONIUM_FUEL(175_000, 65),
        U233_FUEL(150_000, 100),
        U235_FUEL(165_000, 85),
        LES_FUEL(150_000, 150),
        LITHIUM_FUEL(20_000, 0, true),
        ZFB_MOX_FUEL(50_000, 35);

        public static final EnumZirnoxType[] VALUES = values();

        public final int maxLife;
        public final int heat;
        public final boolean breeding;

        EnumZirnoxType(int life, int heat, boolean breeding) {
            this.maxLife = life;
            this.heat = heat;
            this.breeding = breeding;
        }

        EnumZirnoxType(int life, int heat) {
            this.maxLife = life;
            this.heat = heat;
            this.breeding = false;
        }
    }
}
