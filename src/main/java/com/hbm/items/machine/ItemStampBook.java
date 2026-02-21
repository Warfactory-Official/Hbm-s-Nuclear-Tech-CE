package com.hbm.items.machine;

import com.hbm.Tags;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

public class ItemStampBook extends ItemStamp {

    public ItemStampBook(String s) {
        super(s, 0, null);
        this.setHasSubtypes(true);
        for(int i = 0; i < 8; i++) {
            StampType type = getStampType(this, i);
            this.addStampToList(this, i, type);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ResourceLocation loc = new ResourceLocation(Tags.MODID, "items/stamp_book");
        ModelResourceLocation mrl = new ModelResourceLocation(loc, "inventory");

        for (int i = 0; i < 8; i++) {
            ModelLoader.setCustomModelResourceLocation(this, i, mrl);
        }
    }

    @Override
    public StampType getStampType(Item item, int meta) {
        meta %= 8;
        return StampType.values()[StampType.PRINTING1.ordinal() + meta];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (int i = 0; i < 8; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        StampType type = this.getStampType(stack.getItem(), stack.getItemDamage());
        return super.getTranslationKey() + "." + type.name().toLowerCase(Locale.US);
    }
}
