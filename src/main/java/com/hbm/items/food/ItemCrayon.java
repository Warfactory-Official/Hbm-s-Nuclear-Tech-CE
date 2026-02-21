package com.hbm.items.food;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemChemicalDye;
import com.hbm.util.EnumUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

//mlbv: the original base texture from 1.7 has overlapping parts with overlay, causing z-fighting as noted in #1325;
//I made the base's overlapping parts transparent to fix it. There should be no behavioral changes except the flickering.
public class ItemCrayon extends ItemFood implements IDynamicModels {
    protected String baseName;

    public ItemCrayon(String s) {
        super(3, false);
        baseName = s;
        this.setHasSubtypes(true);
        this.setAlwaysEdible();
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModItems.ALL_ITEMS.add(this);
        INSTANCES.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        ResourceLocation base = new ResourceLocation(Tags.MODID, ROOT_PATH + baseName);
        ResourceLocation overlay = new ResourceLocation(Tags.MODID, ROOT_PATH + baseName + "_overlay");

        map.registerSprite(base);
        map.registerSprite(overlay);
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelResourceLocation mrl = new ModelResourceLocation(
                new ResourceLocation(Tags.MODID, ROOT_PATH + baseName),
                "inventory"
        );

        for (int i = 0; i < ItemChemicalDye.EnumChemDye.VALUES.length; i++) {
            ModelLoader.setCustomModelResourceLocation(this, i, mrl);
        }
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));

            ResourceLocation layer0 = new ResourceLocation(Tags.MODID, ROOT_PATH + baseName);
            ResourceLocation layer1 = new ResourceLocation(Tags.MODID, ROOT_PATH + baseName + "_overlay");

            IModel retexturedModel = baseModel.retexture(ImmutableMap.of(
                    "layer0", layer0.toString(),
                    "layer1", layer1.toString()
            ));

            IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());

            ModelResourceLocation bakedModelLocation = new ModelResourceLocation(
                    new ResourceLocation(Tags.MODID, ROOT_PATH + baseName),
                    "inventory"
            );

            event.getModelRegistry().putObject(bakedModelLocation, bakedModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == CreativeTabs.SEARCH || tab == this.getCreativeTab()) {
            for (int i = 0; i < ItemChemicalDye.EnumChemDye.VALUES.length; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        Enum num = EnumUtil.grabEnumSafely(ItemChemicalDye.EnumChemDye.VALUES, stack.getItemDamage());
        return super.getTranslationKey() + "." + num.name().toLowerCase(Locale.US);
    }
}
