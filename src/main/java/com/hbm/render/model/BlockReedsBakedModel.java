package com.hbm.render.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class BlockReedsBakedModel extends AbstractBakedModel {

    private final List<BakedQuad> quadsBottom;
    private final List<BakedQuad> quadsMid;
    private final List<BakedQuad> quadsTop;

    public BlockReedsBakedModel(TextureAtlasSprite[] sprites) throws Exception {
        super(false, false, false, ItemCameraTransforms.DEFAULT, ItemOverrideList.NONE);

        this.quadsBottom = bakeCross(sprites[0]);
        this.quadsMid = bakeCross(sprites[1]);
        this.quadsTop = bakeCross(sprites[2]);
    }

    private static List<BakedQuad> bakeCross(TextureAtlasSprite sprite) throws Exception {
        IModel model = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/tinted_cross"));
        IModel retextured = model.retexture(ImmutableMap.of("cross", sprite.getIconName()));
        Function<ResourceLocation, TextureAtlasSprite> getter = loc -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc.toString());
        IBakedModel baked = retextured.bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK, getter);

        List<BakedQuad> src = baked.getQuads(null, null, 0);
        List<BakedQuad> result = new ArrayList<>();

        for (int i = 0; i < src.size(); i += 2) {
            result.add(src.get(i));
        }

        return result;
    }

    public List<BakedQuad> getQuadsTop() {
        return quadsTop;
    }

    public List<BakedQuad> getQuadsMid() {
        return quadsMid;
    }

    public List<BakedQuad> getQuadsBottom() {
        return quadsBottom;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return quadsTop.getFirst().getSprite();
    }
}
