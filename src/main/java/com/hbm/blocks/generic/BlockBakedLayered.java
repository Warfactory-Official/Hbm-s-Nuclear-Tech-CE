package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.BlockBase;
import com.hbm.items.IDynamicModels;
import com.hbm.render.block.LayeredStateMapper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import static com.hbm.render.block.BlockBakeFrame.ROOT_PATH;

public class BlockBakedLayered extends BlockBase implements IDynamicModels {
    public final int MAX_LAYRES = 8;
    protected String texturePath;
    protected TextureAtlasSprite textureSprite;
    PropertyInteger LAYERS = PropertyInteger.create("layers", 1, MAX_LAYRES);

    public BlockBakedLayered(Material m, SoundType sound, String s, String texturePath) {
        super(m, sound, s);
        this.texturePath = texturePath;
    }

    public BlockBakedLayered(Material m, SoundType sound, String s, String texturePath, TextureAtlasSprite textureAtlasSprite) {
        super(m, sound, s);
        this.textureSprite = textureAtlasSprite;
        this.texturePath = texturePath; //TODO do some at stuff to grab it directly from the sprite
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        int meta = state.getBlock().getMetaFromState(state) & 7;
        float height = (2 * (1 + meta)) / 16.0F;
        return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, height, 1.0D);
    }

    @Override
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new LayeredStateMapper(this.getRegistryName().toString(), LAYERS);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        Block blockBelow = world.getBlockState(pos.down()).getBlock();
        return blockBelow.isOpaqueCube(world.getBlockState(pos.down())) || blockBelow.isLeaves(world.getBlockState(pos.down()), world, pos.down());
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {

        for (int i = 0; i < MAX_LAYRES - 1; i++) {
            IModel layerModel = ModelLoaderRegistry.getModelOrMissing(new ResourceLocation("minecraft", "snow_height" + i * 2));
            layerModel.retexture(ImmutableMap.of("all", texturePath));
            IBakedModel model = layerModel.bake(
                    ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
            );
            event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName().toString(), "layers=" + i), model);
        }

        IModel layerModel = ModelLoaderRegistry.getModelOrMissing(new ResourceLocation("minecraft", "cuble_all"));
        layerModel.retexture(ImmutableMap.of("all", texturePath));
        IBakedModel model = layerModel.bake(
                ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
        );
        event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName().toString(), "layers=7"), model);

    }

    @Override
    public void registerModel() {


    }

    @Override
    public void registerSprite(TextureMap map) {
        if (textureSprite != null) {
            map.setTextureEntry(textureSprite);
        } else {
            map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + texturePath));
        }
    }
}
