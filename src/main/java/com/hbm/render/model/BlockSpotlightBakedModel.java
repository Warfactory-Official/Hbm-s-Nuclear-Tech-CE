package com.hbm.render.model;

import com.hbm.blocks.BlockEnums.LightType;
import com.hbm.blocks.machine.Spotlight;
import com.hbm.blocks.machine.SpotlightModular;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.util.FacingUtil;
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
import java.util.HashMap;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockSpotlightBakedModel extends AbstractWavefrontBakedModel {
  private final TextureAtlasSprite sprite;
  private final float itemYaw;
  private final boolean isInventory;
  private final LightType type;

  public BlockSpotlightBakedModel(
      HFRWavefrontObject model,
      TextureAtlasSprite sprite,
      boolean isInventory,
      LightType type,
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
        makeItemTransforms(type));

    this.sprite = sprite;
    this.isInventory = isInventory;
    this.type = type;
    this.itemYaw = itemYaw;
  }

  @SuppressWarnings("deprecation")
  private static ItemCameraTransforms makeItemTransforms(LightType type) {
    ItemTransformVec3f gui =
        new ItemTransformVec3f(
            new Vector3f(30, 45, 0), new Vector3f(0.15f, 0.05f, 0), new Vector3f(1.5f, 1.5f, 1.5f));

    switch(type) {
        case HALOGEN -> {
            gui.translation.set(0.25f, 0.15f);
            gui.scale.set(1.4f, 1.4f, 1.4f);
        }
        case FLUORESCENT -> {
            gui.translation.set(0.15f, 0.05f);
            gui.scale.set(1.2f, 1.2f, 1.2f);
        }
    }

    ItemTransformVec3f thirdPersonLeft =
        new ItemTransformVec3f(
            new Vector3f(45, 90, 0),
            new Vector3f(0, -0.015f, 0.1f),
            new Vector3f(0.45f, 0.45f, 0.45f));

    ItemTransformVec3f thirdPersonRight =
        new ItemTransformVec3f(
            new Vector3f(45, -90, 0),
            new Vector3f(0, -0.015f, 0.1f),
            new Vector3f(0.45f, 0.45f, 0.45f));

    ItemTransformVec3f firstPersonLeft =
        new ItemTransformVec3f(
            new Vector3f(0, -135, 0), new Vector3f(0.03f, 0.1f, 0), new Vector3f(0.5f, 0.5f, 0.5f));

    ItemTransformVec3f firstPersonRight =
        new ItemTransformVec3f(
            new Vector3f(0, 45, 0), new Vector3f(0.1f, 0.1f, 0), new Vector3f(0.5f, 0.5f, 0.5f));

    ItemTransformVec3f ground =
        new ItemTransformVec3f(
            new Vector3f(0, 0, 0), new Vector3f(0, 0.25f, 0), new Vector3f(0.5f, 0.5f, 0.5f));

    return new ItemCameraTransforms(
        thirdPersonLeft,
        thirdPersonRight,
        firstPersonLeft,
        firstPersonRight,
        BakedModelTransforms.standardBlock().head,
        gui,
        ground,
        BakedModelTransforms.standardBlock().fixed);
  }

  public static BlockSpotlightBakedModel forBlock(
      HFRWavefrontObject model, TextureAtlasSprite sprite, LightType type) {
    return new BlockSpotlightBakedModel(model, sprite, false, type, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F);
  }

  public static BlockSpotlightBakedModel forItem(
      HFRWavefrontObject model, TextureAtlasSprite sprite, LightType type) {
    return new BlockSpotlightBakedModel(
        model, sprite, true, type, 0.9F, 0.0F, 0.0F, 0.0F, (float) Math.PI);
  }

  @Override
  public @NotNull List<BakedQuad> getQuads(
      @Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
    if (side != null) return Collections.emptyList();

    if (isInventory) {
      return buildItemQuads();
    }

    if (state == null) {
      return Collections.emptyList();
    }

    Spotlight spotlight = (Spotlight) state.getBlock();
    EnumFacing facing = state.getValue(Spotlight.FACING);

    float roll = 0;
    float pitch = FacingUtil.getPitch(facing);
    float yaw = FacingUtil.getYaw(facing);

    tx = 0.5F - facing.getXOffset() * 0.5F;
    ty = 0.5F - facing.getYOffset() * 0.5F;
    tz = 0.5F - facing.getZOffset() * 0.5F;

    int connectionCount = 0;

    if (state.getBlock() instanceof SpotlightModular) {
      EnumFacing connectionFacing = null;
      HashMap<EnumFacing, Boolean> connectedFacing = new HashMap<>();

      for (EnumFacing key : EnumFacing.VALUES) {
        boolean connected =
            switch (key) {
              case UP -> state.getValue(Spotlight.CONN_UP);
              case DOWN -> state.getValue(Spotlight.CONN_DOWN);
              case NORTH -> state.getValue(Spotlight.CONN_NORTH);
              case SOUTH -> state.getValue(Spotlight.CONN_SOUTH);
              case WEST -> state.getValue(Spotlight.CONN_WEST);
              case EAST -> state.getValue(Spotlight.CONN_EAST);
            };

        connectedFacing.put(key, connected);
      }

      for (EnumFacing availableFacing : connectedFacing.keySet()) {
        if (availableFacing == facing || availableFacing == facing.getOpposite()) {
          continue;
        }

        if (connectedFacing.get(availableFacing)) {
          connectionCount++;
          connectionFacing = availableFacing;
          break;
        }
      }

      if (connectionFacing != null) {
        if (connectedFacing.get(connectionFacing.getOpposite())) {
          connectionCount++;
        }

        roll = getRotation(connectionFacing, facing);
      }
    }

    return buildWorldQuads(spotlight.getPartName(connectionCount), roll, pitch, yaw);
  }

  private List<BakedQuad> buildWorldQuads(String partName, float roll, float pitch, float yaw) {
    return new ArrayList<>(
        bakeSimpleQuads(Collections.singleton(partName), roll, pitch, yaw, false, false, sprite));
  }

  private List<BakedQuad> buildItemQuads() {
    String partName = "CageLamp";

    partName =
        switch (type) {
          case FLUORESCENT -> "FluoroSingle";
          case HALOGEN -> "FloodLamp";
          default -> partName;
        };

    return new ArrayList<>(
        bakeSimpleQuads(Collections.singletonList(partName), 0, 0, itemYaw, false, true, sprite));
  }

  @Override
  public @NotNull TextureAtlasSprite getParticleTexture() {
    return sprite;
  }

  @Override
  public boolean isAmbientOcclusion(@NotNull IBlockState state) {
    return true;
  }

  private float getRotation(EnumFacing facing, EnumFacing axis) {
    float flipX =
        axis == EnumFacing.DOWN || axis == EnumFacing.NORTH || axis == EnumFacing.WEST
            ? -0.5F
            : 0.5F;
    float addX = axis == EnumFacing.NORTH || axis == EnumFacing.SOUTH ? -0.5F : 0;
    boolean flipNS = axis == EnumFacing.WEST;
    return switch (facing) {
      case NORTH -> flipNS ? (float) Math.PI : 0;
      case SOUTH -> !flipNS ? (float) Math.PI : 0;
      case EAST -> (float) Math.PI * (flipX + addX);
      case WEST -> (float) Math.PI * (-flipX + addX);
      case UP -> (float) Math.PI * 0.5F;
      case DOWN -> (float) Math.PI * -0.5F;
    };
  }
}
