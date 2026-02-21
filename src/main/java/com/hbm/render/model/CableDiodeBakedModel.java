package com.hbm.render.model;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hbm.blocks.network.energy.CableDiode.FACING;

@SideOnly(Side.CLIENT)
public class CableDiodeBakedModel extends AbstractBakedModel {

  private static final float WIDTH = 0.875f;
  private static final float MIN_XYZ = 0.5f - 0.375f;
  private static final float MAX_XYZ = 0.5f + 0.375f;

  private final TextureAtlasSprite sprite;
  private final TextureAtlasSprite padSprite;
  private final boolean isInventory;

  @SuppressWarnings("unchecked")
  private final List<BakedQuad>[] cache = new List[64];

  private List<BakedQuad> inventoryCache;

  private static final ItemCameraTransforms STANDARD_BLOCK = BakedModelTransforms.standardBlock();

  public CableDiodeBakedModel(TextureAtlasSprite sprite, boolean isInventory) {
    super(makeItemTransforms());
    this.sprite = sprite;
    this.isInventory = isInventory;
    this.padSprite =
        Minecraft.getMinecraft()
            .getTextureMapBlocks()
            .getAtlasSprite(
                new ResourceLocation(
                        Tags.MODID,
                        "blocks/" + ModBlocks.hadron_coil_alloy.getRegistryName().getPath())
                    .toString());
  }

  @SuppressWarnings("deprecation")
  private static ItemCameraTransforms makeItemTransforms() {
    ItemTransformVec3f gui =
        new ItemTransformVec3f(
            new Vector3f(30, -135, 0),
            new Vector3f(0, -0.1f, 0),
            new Vector3f(0.575f, 0.575f, 0.575f));

    ItemTransformVec3f thirdPerson =
        new ItemTransformVec3f(
            new Vector3f(75, 45, 0),
            new Vector3f(0, 1.5f / 16f, -2.5f / 16f),
            new Vector3f(0.4f, 0.4f, 0.4f));

    ItemTransformVec3f firstPerson =
        new ItemTransformVec3f(
            new Vector3f(0, 45, 0), new Vector3f(0, 0, 0), new Vector3f(0.4f, 0.4f, 0.4f));

    ItemTransformVec3f ground =
        new ItemTransformVec3f(
            new Vector3f(0, 0, 0), new Vector3f(0, 2f / 16f, 0), new Vector3f(0.4f, 0.4f, 0.4f));

    return new ItemCameraTransforms(
        thirdPerson,
        thirdPerson,
        firstPerson,
        firstPerson,
        STANDARD_BLOCK.head,
        gui,
        ground,
        STANDARD_BLOCK.fixed);
  }

  @Override
  public @NotNull List<BakedQuad> getQuads(
      @Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
    if (side != null) return Collections.emptyList();

    List<BakedQuad> quads = new ArrayList<>();

    if (isInventory) {
      if (inventoryCache != null) return inventoryCache;
      addBox(quads, 0, 0.875f, 0, 1, 1, 1, sprite);
      addBox(quads, MIN_XYZ, MIN_XYZ, MIN_XYZ, MAX_XYZ, MAX_XYZ, MAX_XYZ, padSprite);
      return inventoryCache = Collections.unmodifiableList(quads);
    }

    EnumFacing facing = state.getValue(FACING);

    boolean pX = facing == EnumFacing.EAST;
    boolean pY = facing == EnumFacing.UP;
    boolean pZ = facing == EnumFacing.SOUTH;
    boolean nX = facing == EnumFacing.WEST;
    boolean nY = facing == EnumFacing.DOWN;
    boolean nZ = facing == EnumFacing.NORTH;

    int mask =
        (pX ? 1 : 0) | (nX ? 2 : 0) | (pY ? 4 : 0) | (nY ? 8 : 0) | (pZ ? 16 : 0) | (nZ ? 32 : 0);

    if (cache[mask] != null) return cache[mask];

    addBox(
        quads,
        0 + (nX ? WIDTH : 0),
        0 + (nY ? WIDTH : 0),
        0 + (nZ ? WIDTH : 0),
        1 - (pX ? WIDTH : 0),
        1 - (pY ? WIDTH : 0),
        1 - (pZ ? WIDTH : 0),
        sprite);

    addBox(quads, MIN_XYZ, MIN_XYZ, MIN_XYZ, MAX_XYZ, MAX_XYZ, MAX_XYZ, padSprite);
    return cache[mask] = Collections.unmodifiableList(quads);
  }

  @Override
  public @NotNull TextureAtlasSprite getParticleTexture() {
    return sprite;
  }
}
