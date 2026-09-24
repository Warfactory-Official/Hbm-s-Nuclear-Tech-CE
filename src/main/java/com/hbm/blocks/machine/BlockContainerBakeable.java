package com.hbm.blocks.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IDynamicModels;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import java.util.Objects;

public abstract class BlockContainerBakeable extends BlockContainer implements IDynamicModels {
    protected BlockBakeFrame blockFrame;
    public EnumBlockRenderType getRenderType(IBlockState state){
        return EnumBlockRenderType.MODEL;
    }

    public BlockContainerBakeable(Material material, String regName, BlockBakeFrame blockFrame) {
        super(material);
        this.setTranslationKey(regName);
        this.setRegistryName(regName);
        this.setHarvestLevel("pickaxe", 0);
        this.blockFrame = blockFrame;
        IDynamicModels.INSTANCES.add(this);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(blockFrame.getBaseModelLocation());
            ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();

            blockFrame.putTextures(textureMap);
            IModel retexturedModel = baseModel.retexture(textureMap.build());
            IBakedModel[] models = new IBakedModel[4];
            for (int i = 0; i < EnumFacing.HORIZONTALS.length; i++) {
                EnumFacing facing = EnumFacing.HORIZONTALS[i];
                models[i] = retexturedModel.bake(
                        ModelRotation.getModelRotation(0, BlockBakeFrame.getYRotationForFacing(facing)), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
                );
            }
            ModelResourceLocation modelLocation = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory");
            event.getModelRegistry().putObject(modelLocation, models[2]);
            for (int index = 0; index < models.length; index++) {
                ModelResourceLocation worldLocation = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "facing=" + EnumFacing.HORIZONTALS[index].getName());
                event.getModelRegistry().putObject(worldLocation, models[index]);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(Objects.requireNonNull(this.getRegistryName()), "inventory"));
    }

    @Override
    public void registerSprite(TextureMap map) {
        blockFrame.registerBlockTextures(map);
    }

}
