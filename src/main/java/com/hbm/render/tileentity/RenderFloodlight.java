package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.Floodlight.TileEntityFloodlight;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

@AutoRegister
public class RenderFloodlight extends TileEntitySpecialRenderer<TileEntityFloodlight>
    implements IItemRendererProvider {

  public static final IModelCustom floodlight =
      new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/floodlight.obj"));
  public static final ResourceLocation tex =
      new ResourceLocation(Tags.MODID, "textures/models/machines/floodlight.png");

  @Override
  public void render(
      TileEntityFloodlight tile,
      double x,
      double y,
      double z,
      float interp,
      int destroyStage,
      float alpha) {

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);

    int meta = tile.getBlockMetadata();
    switch (meta) {
      case 0:
      case 6:
        GlStateManager.rotate(180, 1, 0, 0);
        break;
      case 1:
      case 7:
        break;
      case 2:
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.rotate(180, 0, 0, 1);
        break;
      case 3:
        GlStateManager.rotate(90, 1, 0, 0);
        break;
      case 4:
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.rotate(90, 0, 0, 1);
        break;
      case 5:
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.rotate(270, 0, 0, 1);
        break;
    }

    GlStateManager.translate(0, -0.5, 0);

    if (meta != 0 && meta != 1) GlStateManager.rotate(90, 0, 1, 0);

    bindTexture(tex);
    floodlight.renderPart("Base");

    float rotation = tile.rotation;
    if (meta == 0 || meta == 6) rotation -= 90;
    if (meta == 1 || meta == 7) rotation += 90;
    GlStateManager.translate(0, 0.5, 0);
    GlStateManager.rotate(rotation, 0, 0, 1);
    GlStateManager.translate(0, -0.5, 0);

    floodlight.renderPart("Lights");

    if (tile.isOn) {
      RenderArcFurnace.fullbright(true);
      floodlight.renderPart("Lamps");
      RenderArcFurnace.fullbright(false);
    } else {
      GlStateManager.color(0.25F, 0.25F, 0.25F, 1F);
      floodlight.renderPart("Lamps");
      GlStateManager.color(1F, 1F, 1F, 1F);
    }

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.floodlight);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -1.5, 0);
        GlStateManager.scale(6.5, 6.5, 6.5);
      }

      public void renderCommon() {
        bindTexture(tex);
        floodlight.renderPart("Base");
        GlStateManager.translate(0, 0.5, 0);
        GlStateManager.rotate(-30, 0, 0, 1);
        GlStateManager.translate(0, -0.5, 0);
        floodlight.renderPart("Lights");
        floodlight.renderPart("Lamps");
      }
    };
  }
}
