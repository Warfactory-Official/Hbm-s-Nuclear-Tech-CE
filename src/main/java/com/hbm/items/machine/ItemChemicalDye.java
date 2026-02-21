package com.hbm.items.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.util.EnumUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemChemicalDye extends ItemEnumMulti<ItemChemicalDye.EnumChemDye> {
    protected String baseName;
    public ItemChemicalDye(String s) {
        super(s, EnumChemDye.VALUES, true, false);
        baseName = s;
    }

    @SideOnly(Side.CLIENT)
    public static void registerColorHandlers(ColorHandlerEvent.Item evt) {
        ItemColors itemColors = evt.getItemColors();
        itemColors.registerItemColorHandler(new ColorHandler(), ModItems.chemical_dye);
        itemColors.registerItemColorHandler(new ColorHandler(), ModItems.crayon);
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

        for (int i = 0; i < theEnum.length; i++) {
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

    private static class ColorHandler implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            if(tintIndex == 1) {
                EnumChemDye dye = EnumUtil.grabEnumSafely(EnumChemDye.VALUES, stack.getItemDamage());
                return dye.color;
            }

            return 0xffffff;
        }

    }

    public enum EnumChemDye {
        BLACK(1973019, "Black"),
        RED(11743532, "Red"),
        GREEN(3887386, "Green"),
        BROWN(5320730, "Brown"),
        BLUE(2437522, "Blue"),
        PURPLE(8073150, "Purple"),
        CYAN(2651799, "Cyan"),
        SILVER(11250603, "LightGray"),
        GRAY(4408131, "Gray"),
        PINK(14188952, "Pink"),
        LIME(4312372, "Lime"),
        YELLOW(14602026, "Yellow"),
        LIGHTBLUE(6719955, "LightBlue"),
        MAGENTA(12801229, "Magenta"),
        ORANGE(15435844, "Orange"),
        WHITE(15790320, "White");

        public static final EnumChemDye[] VALUES = values();

        public final int color;
        public final String dictName;

        EnumChemDye(int color, String name) {
            this.color = color;
            this.dictName = name;
        }
    }
}
