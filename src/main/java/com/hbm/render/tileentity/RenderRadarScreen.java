package com.hbm.render.tileentity;

import com.hbm.api.entity.RadarEntry;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.gui.GUIMachineRadarNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineRadarScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderRadarScreen extends TileEntitySpecialRenderer<TileEntityMachineRadarScreen>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityMachineRadarScreen screen,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.disableCull();

    switch (screen.getBlockMetadata() - BlockDummyable.offset) {
      case 2 -> GlStateManager.rotate(90, 0F, 1F, 0F);
      case 4 -> GlStateManager.rotate(180, 0F, 1F, 0F);
      case 3 -> GlStateManager.rotate(270, 0F, 1F, 0F);
      case 5 -> GlStateManager.rotate(0, 0F, 1F, 0F);
    }

    this.bindTexture(ResourceManager.radar_screen_tex);
    ResourceManager.radar_screen.renderAll();

    this.bindTexture(GUIMachineRadarNT.texture);
    Tessellator tess = Tessellator.getInstance();
    BufferBuilder buffer = tess.getBuffer();

    if (screen.linked) {
      GlStateManager.depthMask(false);

      double offset = ((screen.getWorld().getTotalWorldTime() % 56) + partialTicks) / 30D;

      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GlStateManager.disableAlpha();
      GlStateManager.shadeModel(GL11.GL_SMOOTH);

      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(0.38, 2 - offset, 1.375).color(0, 255, 0, 0).endVertex();
      buffer.pos(0.38, 2 - offset, -0.375).color(0, 255, 0, 0).endVertex();
      buffer.pos(0.38, 2 - offset - 0.125, -0.375).color(0, 255, 0, 50).endVertex();
      buffer.pos(0.38, 2 - offset - 0.125, 1.375).color(0, 255, 0, 50).endVertex();
      tess.draw();

      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.shadeModel(GL11.GL_FLAT);

      if (!screen.entries.isEmpty()) {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (RadarEntry entry : screen.entries) {
          double sX = (entry.posX - screen.refX) / ((double) screen.range + 1) * 0.875D;
          double sZ = (entry.posZ - screen.refZ) / ((double) screen.range + 1) * 0.875D;
          double size = 0.0625D;

          float u1 = 216F / 256F;
          float u2 = 224F / 256F;
          float v1 = (entry.blipLevel * 8F + 8F) / 256F;
          float v2 = (entry.blipLevel * 8F) / 256F;

          buffer
              .pos(0.38, 1 - sZ + size, 0.5 - sX + size)
              .tex(u1, v1)
              .normal(0F, 1F, 0F)
              .endVertex();
          buffer
              .pos(0.38, 1 - sZ + size, 0.5 - sX - size)
              .tex(u2, v1)
              .normal(0F, 1F, 0F)
              .endVertex();
          buffer
              .pos(0.38, 1 - sZ - size, 0.5 - sX - size)
              .tex(u2, v2)
              .normal(0F, 1F, 0F)
              .endVertex();
          buffer
              .pos(0.38, 1 - sZ - size, 0.5 - sX + size)
              .tex(u1, v2)
              .normal(0F, 1F, 0F)
              .endVertex();
        }
        tess.draw();
      }

      GlStateManager.depthMask(true);
    } else {
      int offset = 118 + screen.getWorld().rand.nextInt(81);
      float u1 = 216F / 256F;
      float u2 = 1.0f;
      float v1 = (offset + 40F) / 256F;
      float v2 = offset / 256F;

      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
      buffer.pos(0.38, 1.875, 1.375).tex(u1, v1).normal(0F, 1F, 0F).endVertex();
      buffer.pos(0.38, 1.875, -0.375).tex(u2, v1).normal(0F, 1F, 0F).endVertex();
      buffer.pos(0.38, 0.125, -0.375).tex(u2, v2).normal(0F, 1F, 0F).endVertex();
      buffer.pos(0.38, 0.125, 1.375).tex(u1, v2).normal(0F, 1F, 0F).endVertex();
      tess.draw();
    }

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.radar_screen);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -3, 0);
        GlStateManager.scale(5.5, 5.5, 5.5);
      }

      public void renderCommon() {
        GlStateManager.translate(0, 0, -0.5);
        bindTexture(ResourceManager.radar_screen_tex);
        ResourceManager.radar_screen.renderAll();
      }
    };
  }
}
