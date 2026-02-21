package com.hbm.items;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemBakedBase extends ItemBase implements IDynamicModels {
    String texturePath;

    public ItemBakedBase(String s, String texturePath) {
        super(s);
        this.texturePath = texturePath;
        INSTANCES.add(this);
    }

    public ItemBakedBase(String s) {
        super(s);
        this.texturePath = s;
        INSTANCES.add(this);
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
            ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath);
            IModel retexturedModel = baseModel.retexture(
                    ImmutableMap.of(
                            "layer0", spriteLoc.toString()
                    )

            );
            IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
            ModelResourceLocation bakedModelLocation = new ModelResourceLocation(spriteLoc, "inventory");
            event.getModelRegistry().putObject(bakedModelLocation, bakedModel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath), "inventory"));
    }

    @Override
    public void registerSprite(TextureMap map) {
        map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath));
    }
}
