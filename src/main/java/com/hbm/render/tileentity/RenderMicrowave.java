package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMicrowave;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderMicrowave extends TileEntitySpecialRenderer<TileEntityMicrowave>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityMicrowave mic,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();

    GlStateManager.enableLighting();

    double offsetX = 0.485D;
    final double offsetY = -0.77D;
    double offsetZ = 0.5D;

    switch (mic.getBlockMetadata()) {
      case 2:
        offsetX = 0.51D;
        GlStateManager.translate(x + offsetX, y + offsetY, z + offsetZ);
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.translate(x + offsetX, y + offsetY, z + offsetZ);
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 4:
        offsetX = 0.5D;
        offsetZ = 0.49D;
        GlStateManager.translate(x + offsetX, y + offsetY, z + offsetZ);
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 5:
        offsetX = 0.5D;
        offsetZ = 0.51D;
        GlStateManager.translate(x + offsetX, y + offsetY, z + offsetZ);
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
    }

    GlStateManager.translate(-0.5D, 0.0D, 0.5D);

    bindTexture(ResourceManager.microwave_tex);
    ResourceManager.microwave.renderPart("mainbody_Cube.001");
    ResourceManager.microwave.renderPart("window_Cube.002");

    double rot = (System.currentTimeMillis() * mic.speed / 10D) % 360;

    if (mic.time > 0) {
      GlStateManager.translate(0.575D, 0.0D, -0.45D);
      GL11.glRotated(rot, 0D, 1D, 0D);
      GlStateManager.translate(-0.575D, 0.0D, 0.45D);
    }
    ResourceManager.microwave.renderPart("plate_Cylinder");

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_microwave);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -4, 4);
        GlStateManager.scale(5, 5, 5);
      }

      public void renderCommon() {
        GlStateManager.translate(-2, -2, 1);
        GlStateManager.scale(3, 3, 3);
        bindTexture(ResourceManager.microwave_tex);
        ResourceManager.microwave.renderPart("mainbody_Cube.001");
        ResourceManager.microwave.renderPart("window_Cube.002");
      }
    };
  }
}
