package com.hbm.render.model;

import com.hbm.render.loader.HFRWavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockFunnelBakedModel extends AbstractWavefrontBakedModel {

  private final TextureAtlasSprite spriteTop;
  private final TextureAtlasSprite spriteSide;
  private final TextureAtlasSprite spriteBottom;
  private final float itemYaw;
  private final boolean isInventory;

  public BlockFunnelBakedModel(
      HFRWavefrontObject model,
      TextureAtlasSprite[] sprites,
      boolean isInventory,
      float baseScale,
      float tx,
      float ty,
      float tz,
      float itemYaw) {
    super(
        model,
        isInventory ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK,
        baseScale,
        tx,
        ty,
        tz,
        BakedModelTransforms.standardBlock());
    this.spriteTop = sprites[0];
    this.spriteSide = sprites[1];
    this.spriteBottom = sprites[2];
    this.isInventory = isInventory;
    this.itemYaw = itemYaw;
  }

  public static BlockFunnelBakedModel forBlock(
      HFRWavefrontObject model, TextureAtlasSprite[] sprites) {
    return new BlockFunnelBakedModel(model, sprites, false, 1.0F, 0.0F, -0.5F, 0.0F, 0.0F);
  }

  public static BlockFunnelBakedModel forItem(
      HFRWavefrontObject model, TextureAtlasSprite[] sprites) {
    return new BlockFunnelBakedModel(model, sprites, true, 0.9F, 0.5F, 0.0F, 0.5F, (float) Math.PI);
  }

  public static BlockFunnelBakedModel empty(TextureAtlasSprite[] sprites) {
    return new BlockFunnelBakedModel(
        new HFRWavefrontObject(new ResourceLocation("minecraft:empty")),
        sprites,
        true,
        1.0F,
        0,
        0,
        0,
        0);
  }

  @Override
  public @NotNull List<BakedQuad> getQuads(
      @Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

    if (side != null) return Collections.emptyList();

    if (isInventory) {
      return buildItemQuads();
    }

    return buildWorldQuads();
  }

  private List<BakedQuad> buildWorldQuads() {
    List<BakedQuad> out = new ArrayList<>();

    out.addAll(bakeSimpleQuads(Collections.singleton("Top"), 0, 0, 0, true, true, spriteTop));
    out.addAll(bakeSimpleQuads(Collections.singleton("Side"), 0, 0, 0, true, true, spriteSide));
    out.addAll(bakeSimpleQuads(Collections.singleton("Bottom"), 0, 0, 0, true, true, spriteBottom));
    return out;
  }

  private List<BakedQuad> buildItemQuads() {
    List<BakedQuad> out = new ArrayList<>();

    out.addAll(
        bakeSimpleQuads(Collections.singleton("Top"), 0, 0, itemYaw, false, false, spriteTop));
    out.addAll(
        bakeSimpleQuads(Collections.singleton("Side"), 0, 0, itemYaw, false, false, spriteSide));
    out.addAll(
        bakeSimpleQuads(
            Collections.singleton("Bottom"), 0, 0, itemYaw, false, false, spriteBottom));
    return out;
  }

  @Override
  public @NotNull TextureAtlasSprite getParticleTexture() {
    return spriteBottom;
  }

  @Override
  public boolean isAmbientOcclusion(IBlockState state) {
    return true;
  }
}
