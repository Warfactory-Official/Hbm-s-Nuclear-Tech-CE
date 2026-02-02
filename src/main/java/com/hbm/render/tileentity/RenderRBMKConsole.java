package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.RBMKScreen;
import com.hbm.tileentity.machine.rbmk.RBMKColumn;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
@AutoRegister
public class RenderRBMKConsole extends TileEntitySpecialRenderer<TileEntityRBMKConsole>
    implements IItemRendererProvider {

  @Override
  public boolean isGlobalRenderer(TileEntityRBMKConsole te) {
    return true;
  }

  @Override
  public void render(
      TileEntityRBMKConsole te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();

    GlStateManager.translate((float) x + 0.5F, (float) y, (float) z + 0.5F);

    GlStateManager.enableCull();
    GlStateManager.enableLighting();

    switch (te.getBlockMetadata() - BlockDummyable.offset) {
      case 2:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
    }

    GlStateManager.translate(0.5, 0, 0);

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.rbmk_console_tex);
    ResourceManager.rbmk_console.renderAll();
    GlStateManager.shadeModel(GL11.GL_FLAT);

    /// New part
    TileEntityRBMKConsole console = (TileEntityRBMKConsole) te;

    Tessellator tess = Tessellator.getInstance();
    BufferBuilder buf = tess.getBuffer();
    buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

    GlStateManager.disableTexture2D();

    for (int i = 0; i < console.columns.length; i++) {

      RBMKColumn col = console.columns[i];

      if (col == null) continue;

      double kx = -0.37D;
      double ky = -(i / 15) * 0.125 + 3.625;
      double kz = -(i % 15) * 0.125 + 0.125D * 7;

      drawColumn(
          buf,
          kx,
          ky,
          kz,
          (float) (0.75D + (i % 2) * 0.05D),
          col.heat / col.maxHeat);

      switch (col.type) {
        case FUEL:
        case FUEL_SIM:
          drawFuel(buf, kx + 0.01, ky, kz, ((RBMKColumn.FuelColumn) col).enrichment);
          break;
        case CONTROL:
          drawControl(buf, kx + 0.01, ky, kz, ((RBMKColumn.ControlColumn) col).level);
          break;
        case CONTROL_AUTO:
          drawControlAuto(buf, kx + 0.01, ky, kz, ((RBMKColumn.ControlColumn) col).level);
          break;
        default:
      }
    }

    tess.draw();
    GlStateManager.enableTexture2D();

    FontRenderer font = Minecraft.getMinecraft().fontRenderer;
    GlStateManager.translate(-0.42F, 3.5F, 1.75F);
    GlStateManager.depthMask(false);
    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    GlStateManager.color(1, 1, 1, 1);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

    for (int i = 0; i < console.screens.length; i++) {

      GlStateManager.pushMatrix();

      if (i % 2 == 1) GlStateManager.translate(0, 0, 1.75F * -2);

      GlStateManager.translate(0, -0.75F * (i / 2), 0);

      RBMKScreen screen = console.screens[i];
      String text = screen.display;

      if (text != null && !text.isEmpty()) {

        String[] parts = text.split("=");

        if (parts.length == 2) {
          text = I18nUtil.resolveKey(parts[0], parts[1]);
        }

        int width = font.getStringWidth(text);
        int height = font.FONT_HEIGHT;

        float f3 = Math.min(0.03F, 0.8F / Math.max(width, 1));
        GlStateManager.scale(f3, -f3, f3);
        GlStateManager.color(0.0F, 0.0F, -1.0F);
        GlStateManager.rotate(90, 0, 1, 0);

        font.drawString(text, -width / 2, -height / 2, 0x00ff00);
      }
      GlStateManager.popMatrix();
    }

    GlStateManager.depthMask(true);
    GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    ///

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.rbmk_console);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -3, 0);
        GlStateManager.scale(2.5, 2.5, 2.5);
      }

      public void renderCommon() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.rbmk_console_tex);
        ResourceManager.rbmk_console.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }

  private void drawColumn(
      BufferBuilder buf, double x, double y, double z, float color, double heat) {

    double width = 0.0625D * 0.75;
    float r = (float) (color + ((1 - color) * Math.max(0, heat - 0.4)));
    float d = (float) (color - (color * Math.max(0, heat - 0.6)));
    buf.pos(x, y + width, z - width).color(r, d, d, 1F).endVertex();
    buf.pos(x, y + width, z + width).color(r, d, d, 1F).endVertex();
    buf.pos(x, y - width, z + width).color(r, d, d, 1F).endVertex();
    buf.pos(x, y - width, z - width).color(r, d, d, 1F).endVertex();
  }

  private void drawFuel(BufferBuilder buf, double x, double y, double z, double enrichment) {
    this.drawDot(buf, x, y, z, 0F, 0.25F + (float) (enrichment * 0.75D), 0F);
  }

  private void drawControl(BufferBuilder buf, double x, double y, double z, double level) {
    this.drawDot(buf, x, y, z, (float) level, (float) level, 0F);
  }

  private void drawControlAuto(BufferBuilder buf, double x, double y, double z, double level) {
    this.drawDot(buf, x, y, z, (float) level, 0F, (float) level);
  }

  private void drawDot(BufferBuilder buf, double x, double y, double z, float r, float g, float b) {

    double width = 0.03125D;
    double edge = 0.022097D;

    buf.pos(x, y + width, z).color(r, g, b, 1F).endVertex();
    buf.pos(x, y + edge, z + edge).color(r, g, b, 1F).endVertex();
    buf.pos(x, y, z + width).color(r, g, b, 1F).endVertex();
    buf.pos(x, y - edge, z + edge).color(r, g, b, 1F).endVertex();

    buf.pos(x, y + edge, z - edge).color(r, g, b, 1F).endVertex();
    buf.pos(x, y + width, z).color(r, g, b, 1F).endVertex();
    buf.pos(x, y - edge, z - edge).color(r, g, b, 1F).endVertex();
    buf.pos(x, y, z - width).color(r, g, b, 1F).endVertex();

    buf.pos(x, y + width, z).color(r, g, b, 1F).endVertex();
    buf.pos(x, y - edge, z + edge).color(r, g, b, 1F).endVertex();
    buf.pos(x, y - width, z).color(r, g, b, 1F).endVertex();
    buf.pos(x, y - edge, z - edge).color(r, g, b, 1F).endVertex();
  }
}
