package com.hbm.render.model;

import com.hbm.blocks.generic.BlockScaffold;
import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockScaffoldBakedModel extends AbstractWavefrontBakedModel {
    private final TextureAtlasSprite sprite;
    private final boolean isInventory;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[4];
    private List<BakedQuad> inventoryCache;

    public BlockScaffoldBakedModel(HFRWavefrontObject model, TextureAtlasSprite sprite, boolean isInventory, float baseScale, float tx, float ty, float tz) {
        super(model, isInventory ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK, baseScale, tx, ty, tz, makeItemTransforms());

        this.sprite = sprite;
        this.isInventory = isInventory;
    }

    @SuppressWarnings("deprecation")
    private static ItemCameraTransforms makeItemTransforms() {
        ItemTransformVec3f gui = new ItemTransformVec3f(new Vector3f(30, 45, 0), new Vector3f(0.0f, -0.03f, 0), new Vector3f(0.6f, 0.6f, 0.6f));
        ItemTransformVec3f thirdPersonLeft = new ItemTransformVec3f(new Vector3f(75, 45, 0), new Vector3f(0, 0.15f, 0.1f), new Vector3f(0.35f, 0.35f, 0.35f));
        ItemTransformVec3f thirdPersonRight = new ItemTransformVec3f(new Vector3f(75, -45, 0), new Vector3f(0, 0.15f, -0.1f), new Vector3f(0.35f, 0.35f, 0.35f));
        ItemTransformVec3f firstPersonLeft = new ItemTransformVec3f(new Vector3f(0, -135, 0), new Vector3f(0.03f, 0.1f, 0), new Vector3f(0.35f, 0.35f, 0.35f));
        ItemTransformVec3f firstPersonRight = new ItemTransformVec3f(new Vector3f(0, 45, 0), new Vector3f(0.0f, 0.1f, 0), new Vector3f(0.35f, 0.35f, 0.35f));
        ItemTransformVec3f ground = new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0, 0.25f, 0), new Vector3f(0.3f, 0.3f, 0.3f));

        return new ItemCameraTransforms(thirdPersonLeft, thirdPersonRight, firstPersonLeft, firstPersonRight, BakedModelTransforms.standardBlock().head, gui, ground, BakedModelTransforms.standardBlock().fixed);
    }

    public static BlockScaffoldBakedModel forBlock(HFRWavefrontObject model, TextureAtlasSprite sprite) {
        return new BlockScaffoldBakedModel(model, sprite, false, 1.0F, 0.0F, 0.0F, 0.0F);
    }

    public static BlockScaffoldBakedModel forItem(HFRWavefrontObject model, TextureAtlasSprite sprite) {
        return new BlockScaffoldBakedModel(model, sprite, true, 1.0F, 0.0F, -0.5F, 0.0F);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        if (isInventory) {
            if (inventoryCache != null) return inventoryCache;
            return inventoryCache = Collections.unmodifiableList(buildItemQuads());
        }

        if (state == null) {
            return Collections.emptyList();
        }

        int orient = state.getValue(BlockScaffold.ORIENT);

        float yaw = (float) (-Math.PI * 0.5);
        float pitch = 0.0f;
        tx = 0.0f;
        ty = -0.5f;
        tz = 0.0f;

        switch (orient) {
            case 3:
                pitch = (float) (Math.PI * 0.5);
                yaw = (float) (-Math.PI);
                tx = -0.5f;
                ty = 0.0f;
                break;
            case 1:
                yaw = (float) (-Math.PI);
                break;
            case 2:
                pitch = (float) (Math.PI * 0.5);
                ty = 0.0f;
                tz = 0.5f;
                break;
            case 0:
            default:
                break;
        }

        if (cache[orient] != null) return cache[orient];
        return cache[orient] = Collections.unmodifiableList(buildWorldQuads(pitch, yaw));
    }

    private List<BakedQuad> buildWorldQuads(float pitch, float yaw) {
        return new ArrayList<>(bakeSimpleQuads(Collections.singleton("Scaffold"), 0, pitch, yaw, false, true, sprite));
    }

    private List<BakedQuad> buildItemQuads() {
        return new ArrayList<>(bakeSimpleQuads(Collections.singletonList("Scaffold"), 0, 0, (float) Math.PI, false, true, sprite));
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sprite;
    }

    @Override
    public boolean isAmbientOcclusion(@NotNull IBlockState state) {
        return true;
    }
}
