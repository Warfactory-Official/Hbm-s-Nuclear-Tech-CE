package com.hbm.render.model;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.generic.BlockReeds;
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
import net.minecraftforge.client.model.pipeline.TRSRTransformer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector3f;
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

        ResourceLocation TINTED_CROSS = new ResourceLocation("minecraft:block/tinted_cross");

        this.quadsBottom = bakeCross(TINTED_CROSS, sprites[0]);
        this.quadsMid = bakeCross(TINTED_CROSS, sprites[1]);
        this.quadsTop = bakeCross(TINTED_CROSS, sprites[2]);
    }

    private static List<BakedQuad> bakeCross(ResourceLocation base, TextureAtlasSprite sprite) throws Exception {
        IModel model = ModelLoaderRegistry.getModel(base);
        IModel retextured = model.retexture(ImmutableMap.of("cross", sprite.getIconName()));
        Function<ResourceLocation, TextureAtlasSprite> getter = loc -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc.toString());
        IBakedModel baked = retextured.bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK, getter);

        return new ArrayList<>(baked.getQuads(null, null, 0));
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        if (!(state instanceof IExtendedBlockState ex)) {
            return quadsTop;
        }

        Integer depth = ex.getValue(BlockReeds.DEPTH);
        Boolean isTop = ex.getValue(BlockReeds.IS_TOP);

        if (depth == null || isTop == null) {
            return quadsTop;
        }

        if (!isTop) return Collections.emptyList();

        List<BakedQuad> out = new ArrayList<>(depth * 4);

        for (int i = 0; i < depth; i++) {
            List<BakedQuad> base = quadsMid;
            if (i == 0) base = quadsTop;
            else if (i == depth - 1) base = quadsBottom;

            // EOS: The problem is render reeds under the main reed, because it is limited normalized coords from 0 to 1.
            TRSRTransformation transform = new TRSRTransformation(new Vector3f(0f, -i, 0f), null, null, null);
            transformAppend(base, transform, out);
        }

        return out;
    }

    private static void transformAppend(List<BakedQuad> src, TRSRTransformation t, List<BakedQuad> out) {
        for (BakedQuad q : src) {
            UnpackedBakedQuad.Builder b = new UnpackedBakedQuad.Builder(q.getFormat());
            b.setQuadOrientation(q.getFace());
            b.setApplyDiffuseLighting(q.shouldApplyDiffuseLighting());
            b.setQuadTint(q.getTintIndex());
            b.setTexture(q.getSprite());

            q.pipe(new TRSRTransformer(b, t));
            out.add(b.build());
        }
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return quadsTop.getFirst().getSprite();
    }
}
