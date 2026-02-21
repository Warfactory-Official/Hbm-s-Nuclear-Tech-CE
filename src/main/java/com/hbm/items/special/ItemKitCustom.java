package com.hbm.items.special;

import com.hbm.items.ModItems;
import com.hbm.util.ItemStackUtil;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemKitCustom extends ItemKitNBT {
    public ItemKitCustom(String s) {
        super(s);
    }

    public static ItemStack create(String name, String lore, int color1, int color2, ItemStack... contents) {
        ItemStack stack = new ItemStack(ModItems.kit_custom);

        stack.setTagCompound(new NBTTagCompound());

        setColor(stack, color1, 1);
        setColor(stack, color2, 2);

        if(lore != null) ItemStackUtil.addTooltipToStack(stack, lore.split("\\$"));
        stack.setStackDisplayName(TextFormatting.RESET + name);
        ItemStackUtil.addStacksToNBT(stack, contents);

        return stack;
    }

    public static void setColor(ItemStack stack, int color, int index) {

        if(!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setInteger("color" + index, color);
    }

    public static int getColor(ItemStack stack, int index) {

        if(!stack.hasTagCompound())
            return 0;

        return stack.getTagCompound().getInteger("color" + index);
    }

    @SideOnly(Side.CLIENT)
    public static void registerColorHandlers(ColorHandlerEvent.Item event) {
        ItemColors colors = event.getItemColors();
        colors.registerItemColorHandler(new ColorHandler(), ModItems.kit_custom);
    }

    public static class ColorHandler implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            if (tintIndex == 1 || tintIndex == 2) {
                return ItemKitCustom.getColor(stack, tintIndex);
            }
            return 0xffffff;
        }
    }
}
