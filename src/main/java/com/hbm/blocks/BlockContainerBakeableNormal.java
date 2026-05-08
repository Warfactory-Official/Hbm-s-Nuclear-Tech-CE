package com.hbm.blocks;

import com.google.common.collect.ImmutableMap;
import com.hbm.items.ClaimedModelLocationRegistry;
import com.hbm.items.IDynamicModels;
import com.hbm.main.client.NTMClientRegistry;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public abstract class BlockContainerBakeableNormal extends BlockContainer implements IDynamicModels {

    protected final BlockBakeFrame blockFrame;

    protected BlockContainerBakeableNormal(Material material, String registryName, BlockBakeFrame blockFrame) {
        super(material);
        this.setTranslationKey(registryName);
        this.setRegistryName(registryName);
        this.setHarvestLevel("pickaxe", 0);
        this.blockFrame = blockFrame;
        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    @Override
    public EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(blockFrame.getBaseModelLocation());
            ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();
            blockFrame.putTextures(textureMap);
            IModel retexturedModel = baseModel.retexture(textureMap.build());
            IBakedModel bakedModel = retexturedModel.bake(
                    ModelRotation.X0_Y0,
                    DefaultVertexFormats.BLOCK,
                    ModelLoader.defaultTextureGetter()
            );

            ModelResourceLocation worldLocation = new ModelResourceLocation(getRegistryName(), "normal");
            event.getModelRegistry().putObject(worldLocation, bakedModel);

            if (!ClaimedModelLocationRegistry.hasSyntheticTeisrBinding(Item.getItemFromBlock(this))) {
                IModel itemBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("item/generated"));
                ImmutableMap<String, String> itemTextures = ImmutableMap.of("layer0", blockFrame.getTextureLocation(0).toString());
                IModel itemRetextured = itemBaseModel.retexture(itemTextures);
                IBakedModel itemBaked = itemRetextured.bake(
                        ModelRotation.X0_Y0,
                        DefaultVertexFormats.ITEM,
                        ModelLoader.defaultTextureGetter()
                );
                ModelResourceLocation inventoryLocation = new ModelResourceLocation(getRegistryName(), "inventory");
                event.getModelRegistry().putObject(inventoryLocation, itemBaked);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        Item item = Item.getItemFromBlock(this);
        ModelResourceLocation syntheticLocation = NTMClientRegistry.getSyntheticTeisrModelLocation(item);
        ModelLoader.setCustomModelResourceLocation(item, 0, syntheticLocation != null ? syntheticLocation : new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        blockFrame.registerBlockTextures(map);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull net.minecraft.block.state.IBlockState state) {
                return new ModelResourceLocation(loc, "normal");
            }
        };
    }
}
