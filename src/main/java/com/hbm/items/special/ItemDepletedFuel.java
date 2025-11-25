package com.hbm.items.special;

import com.hbm.items.IDynamicModels;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemDepletedFuel extends ItemNuclearWaste implements IDynamicModels {


    public ItemDepletedFuel(String s) {
        super(s);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        IDynamicModels.INSTANCES.add(this);

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list){
        if (this.isInCreativeTab(tab)) {
            for (int i = 0; i < 2; i++) {
                list.add(new ItemStack(this, 1, i));
            }
        }
    }



    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn){
        if (stack.getItemDamage() > 0)
            tooltip.add(TextFormatting.GOLD + I18nUtil.resolveKey("desc.item.wasteCooling"));
    }


    @Override
    public IItemColor getItemColorHandler() {
        return new ColorHandler();
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {

    }

    @Override
    public void registerModel() {
        for (int i = 0; i <= 1; i++) {
            ModelLoader.setCustomModelResourceLocation(this, i,
                    new ModelResourceLocation(Objects.requireNonNull(this.getRegistryName()), "inventory"));
        }
        return;
    }

    @Override
    public void registerSprite(TextureMap map) {

    }


    private static class ColorHandler implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            return stack.getMetadata() > 0 ? 0xFFBFA5 : 0xFFFFFF;
        }

    }
}
