package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.control_panel.ControlPanel;
import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityControlPanel;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderControlPanel extends TileEntitySpecialRenderer<TileEntityControlPanel>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityControlPanel te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();

    switch (te.panelType) {
      case CUSTOM_PANEL:
        renderCustomPanel(te, x, y, z, partialTicks, destroyStage, alpha);
        break;
      case FRONT_PANEL:
        renderFrontPanel(te, x, y, z, partialTicks, destroyStage, alpha);
        break;
    }

    GlStateManager.enableRescaleNormal();
    GlStateManager.pushMatrix();
    GlStateManager.translate(-0.5, 0, -0.5);
    te.panel.transform.store(ClientProxy.AUX_GL_BUFFER);
    ClientProxy.AUX_GL_BUFFER.rewind();
    GL11.glMultMatrix(ClientProxy.AUX_GL_BUFFER);
    te.updateTransform();
    te.panel.render();
    GlStateManager.popMatrix();
    GlStateManager.disableRescaleNormal();
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.control_panel_custom);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(-2.5, -1, 0);
        GlStateManager.scale(12, 12, 12);
      }

      public void renderCommon() {
        GlStateManager.translate(1.5, 1, -1);
        bindTexture(ResourceManager.control_panel_custom_tex);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.control_panel_custom.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }

      public boolean doNullTransform() {
        return true;
      }
    };
  }

  public void renderCustomPanel(
      TileEntityControlPanel te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.translate(x + .5, y, z + .5);
    GlStateManager.pushMatrix();
    GlStateManager.rotate(-90, 0, 0, 1);

      int metadata = te.getBlockMetadata();
      switch ((metadata & 3) + 2) {
      case 4:
        GlStateManager.rotate(-180, 1, 0, 0);
        break;
      case 2:
        GlStateManager.rotate(-90, 1, 0, 0);
        break;
      case 5:
        GlStateManager.rotate(-0, 1, 0, 0);
        break;
      case 3:
        GlStateManager.rotate(-270, 1, 0, 0);
        break;
    }
      boolean isUp = ((metadata >> 3) == 1);
      boolean isDown = ((metadata >> 2) == 1);
    if (isDown) {
      GlStateManager.rotate(-180, 1, 0, 0);
      GlStateManager.rotate(-90, 0, 0, 1);
      GlStateManager.translate(0, -1, 0);
    } else if (isUp) {
      GlStateManager.rotate(90, 0, 0, 1);
    } else {
      GlStateManager.translate(-.5, -.5, 0);
    }

    GlStateManager.enableRescaleNormal();
    //		bindTexture(ResourceManager.control_panel_custom_tex);

    GlStateManager.pushMatrix();
    GlStateManager.translate(-0.5, 0, -0.5);

    float a_off = te.panel.a_off;
    float b_off = te.panel.b_off;
    float c_off = te.panel.c_off;
    float d_off = te.panel.d_off;
    float height = te.panel.height;
    float angle = te.panel.angle;

    float height1 = ControlPanel.getSlopeHeightFromZ(1 - c_off, height, -angle);
    float height0 = ControlPanel.getSlopeHeightFromZ(a_off, height, -angle);

    if (height != 0) {
      GlStateManager.disableLighting();
      ResourceLocation texxy =
          new ResourceLocation(Tags.MODID + ":textures/models/misc/control_panel.png");
      bindTexture(texxy);
      net.minecraft.client.renderer.Tessellator tess =
          net.minecraft.client.renderer.Tessellator.getInstance();
      BufferBuilder buf = tess.getBuffer();
      // back
      buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
      buf.pos(a_off, 0, 1 - d_off).tex(1, 1).endVertex();
      buf.pos(a_off, height0, 1 - d_off).tex(1, 0).endVertex();
      buf.pos(a_off, height0, b_off).tex(0, 0).endVertex();
      buf.pos(a_off, 0, b_off).tex(0, 1).endVertex();
      // left
      buf.pos(1 - c_off, 0, 1 - d_off).tex(1, 1).endVertex();
      buf.pos(1 - c_off, height1, 1 - d_off).tex(1, 0).endVertex();
      buf.pos(a_off, height0, 1 - d_off).tex(0, 0).endVertex();
      buf.pos(a_off, 0, 1 - d_off).tex(0, 1).endVertex();
      // right
      buf.pos(a_off, 0, b_off).tex(1, 1).endVertex();
      buf.pos(a_off, height0, b_off).tex(1, 0).endVertex();
      buf.pos(1 - c_off, height1, b_off).tex(0, 0).endVertex();
      buf.pos(1 - c_off, 0, b_off).tex(0, 1).endVertex();
      // front
      buf.pos(1 - c_off, 0, b_off).tex(1, 1).endVertex();
      buf.pos(1 - c_off, height1, b_off).tex(1, 0).endVertex();
      buf.pos(1 - c_off, height1, 1 - d_off).tex(0, 0).endVertex();
      buf.pos(1 - c_off, 0, 1 - d_off).tex(0, 1).endVertex();
      //			// top
      buf.pos(1 - c_off, height1, b_off).tex(1, 1).endVertex();
      buf.pos(a_off, height0, b_off).tex(1, 0).endVertex();
      buf.pos(a_off, height0, 1 - d_off).tex(0, 0).endVertex();
      buf.pos(1 - c_off, height1, 1 - d_off).tex(0, 1).endVertex();
      // bottom
      buf.pos(1 - c_off, 0, 1 - d_off).tex(0, 1).endVertex();
      buf.pos(a_off, 0, 1 - d_off).tex(0, 0).endVertex();
      buf.pos(a_off, 0, b_off).tex(1, 0).endVertex();
      buf.pos(1 - c_off, 0, b_off).tex(1, 1).endVertex();
      tess.draw();
    }

    GlStateManager.enableLighting();
    GlStateManager.disableRescaleNormal();
    GlStateManager.popMatrix();
    GlStateManager.popMatrix();
  }

  public void renderFrontPanel(
      TileEntityControlPanel te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.translate(x + .5F, y, z + .5F);
    GlStateManager.pushMatrix();

    switch (te.getBlockMetadata()) {
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

    GlStateManager.pushMatrix();
    bindTexture(
        new ResourceLocation(
            Tags.MODID + ":textures/models/control_panel/control_panel_front.png"));
    ResourceManager.control_panel_front.renderAll();
    GlStateManager.popMatrix();

    GlStateManager.popMatrix();
  }
}
