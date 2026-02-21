package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.BlockBase;
import com.hbm.items.IDynamicModels;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import static com.hbm.render.block.BlockBakeFrame.BlockForm.ALL;

//Simple class for my baking system, automatically bakes block models from BlockBakeFrame
public class BlockBakeBase extends BlockBase implements IDynamicModels {

    protected BlockBakeFrame blockFrame;

    public BlockBakeBase(
    Material m, String s, BlockBakeFrame blockFrame) {
        super(m, s);
        this.blockFrame = blockFrame;
        IDynamicModels.INSTANCES.add(this);

    }

    public BlockBakeBase(
            Material m, String s, String texture) {
        super(m, s);
        this.blockFrame = new BlockBakeFrame(ALL, texture);
        IDynamicModels.INSTANCES.add(this);

    }

    public BlockBakeBase(
            Material m, String s) {
        super(m, s);
        this.blockFrame = new BlockBakeFrame(ALL, s);
        IDynamicModels.INSTANCES.add(this);

    }

    public BlockBakeBase(
            Material m, String s, String textureTop, String textureSide) {
        super(m, s);
        this.blockFrame = new BlockBakeFrame(textureTop, textureSide);
        IDynamicModels.INSTANCES.add(this);

    }

    @Override
    public void bakeModel(ModelBakeEvent event) {

            try {
                IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation(blockFrame.getBaseModel()));
                ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();

                blockFrame.putTextures(textureMap);
                IModel retexturedModel = baseModel.retexture(textureMap.build());
                IBakedModel bakedModel = retexturedModel.bake(
                        ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
                );

                ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "inventory");
                event.getModelRegistry().putObject(modelLocation, bakedModel);
                ModelResourceLocation worldLocation = new ModelResourceLocation(getRegistryName(), "normal");
                event.getModelRegistry().putObject(worldLocation, bakedModel);

            } catch (Exception e) {
                e.printStackTrace();
            }


    }

    @Override
    public void registerModel() {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this),0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }

    @Override
    public void registerSprite(TextureMap map) {
            blockFrame.registerBlockTextures(map);
    }
}
