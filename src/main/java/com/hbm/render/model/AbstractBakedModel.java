package com.hbm.render.model;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class AbstractBakedModel implements IBakedModel {

  private final boolean ambientOcclusion;
  private final boolean gui3d;
  private final boolean builtInRenderer;
  private final ItemCameraTransforms transforms;
  private final ItemOverrideList overrides;

  protected AbstractBakedModel(ItemCameraTransforms transforms) {
    this(true, true, false, transforms, ItemOverrideList.NONE);
  }

  protected AbstractBakedModel(
      boolean ambientOcclusion,
      boolean gui3d,
      boolean builtInRenderer,
      ItemCameraTransforms transforms) {
    this(ambientOcclusion, gui3d, builtInRenderer, transforms, ItemOverrideList.NONE);
  }

  protected AbstractBakedModel(
      boolean ambientOcclusion,
      boolean gui3d,
      boolean builtInRenderer,
      ItemCameraTransforms transforms,
      ItemOverrideList overrides) {
    this.ambientOcclusion = ambientOcclusion;
    this.gui3d = gui3d;
    this.builtInRenderer = builtInRenderer;
    this.transforms = transforms != null ? transforms : ItemCameraTransforms.DEFAULT;
    this.overrides = overrides != null ? overrides : ItemOverrideList.NONE;
  }

  @Override
  public final boolean isAmbientOcclusion() {
    return ambientOcclusion;
  }

  @Override
  public final boolean isGui3d() {
    return gui3d;
  }

  @Override
  public final boolean isBuiltInRenderer() {
    return builtInRenderer;
  }

  @Override
  public final ItemCameraTransforms getItemCameraTransforms() {
    return transforms;
  }

  @Override
  public final ItemOverrideList getOverrides() {
    return overrides;
  }

  public static void addBox(
      List<BakedQuad> quads,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ,
      TextureAtlasSprite sprite) {
    FaceBakery bakery = new FaceBakery();

    Vector3f from = new Vector3f(minX * 16.0f, minY * 16.0f, minZ * 16.0f);
    Vector3f to = new Vector3f(maxX * 16.0f, maxY * 16.0f, maxZ * 16.0f);

    for (EnumFacing face : EnumFacing.VALUES) {
      BlockFaceUV uv = makeFaceUV(face, from, to);
      BlockPartFace partFace = new BlockPartFace(face, -1, "", uv);
      BakedQuad quad =
          bakery.makeBakedQuad(
              from, to, partFace, sprite, face, TRSRTransformation.identity(), null, true, true);
      quads.add(quad);
    }
  }

  public static BlockFaceUV makeFaceUV(EnumFacing face, Vector3f from, Vector3f to) {
    float u1, v1, u2, v2;
    switch (face) {
      case DOWN -> {
        u1 = from.x;
        v1 = 16f - to.z;
        u2 = to.x;
        v2 = 16f - from.z;
      }
      case UP -> {
        u1 = from.x;
        v1 = from.z;
        u2 = to.x;
        v2 = to.z;
      }
      case NORTH -> { // Z-
        u1 = 16f - to.x;
        v1 = 16f - to.y;
        u2 = 16f - from.x;
        v2 = 16f - from.y;
      }
      case SOUTH -> { // Z+
        u1 = from.x;
        v1 = 16f - to.y;
        u2 = to.x;
        v2 = 16f - from.y;
      }
      case WEST -> { // X-
        u1 = from.z;
        v1 = 16f - to.y;
        u2 = to.z;
        v2 = 16f - from.y;
      }
      case EAST -> { // X+
        u1 = 16f - to.z;
        v1 = 16f - to.y;
        u2 = 16f - from.z;
        v2 = 16f - from.y;
      }
      default -> {
        u1 = 0f;
        v1 = 0f;
        u2 = 16f;
        v2 = 16f;
      }
    }
    return new BlockFaceUV(new float[] {u1, v1, u2, v2}, 0);
  }
}
