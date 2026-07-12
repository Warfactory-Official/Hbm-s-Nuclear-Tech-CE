package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineCyclotron;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderCyclotron extends TileEntitySpecialRenderer<TileEntityMachineCyclotron>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityMachineCyclotron cyc,
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
    GlStateManager.shadeModel(GL11.GL_SMOOTH);

    bindTexture(ResourceManager.cyclotron_tex);
    ResourceManager.cyclotron.renderPart("Body");

    GlStateManager.shadeModel(GL11.GL_FLAT);

    boolean plugged = true;

    if (cyc.getPlug(0)) {
      bindTexture(ResourceManager.cyclotron_ashes_filled);
    } else {
      bindTexture(ResourceManager.cyclotron_ashes);
      plugged = false;
    }
    ResourceManager.cyclotron.renderPart("B1");
    if (cyc.getPlug(1)) {
      bindTexture(ResourceManager.cyclotron_book_filled);
    } else {
      bindTexture(ResourceManager.cyclotron_book);
      plugged = false;
    }
    ResourceManager.cyclotron.renderPart("B2");
    if (cyc.getPlug(2)) {
      bindTexture(ResourceManager.cyclotron_gavel_filled);
    } else {
      bindTexture(ResourceManager.cyclotron_gavel);
      plugged = false;
    }
    ResourceManager.cyclotron.renderPart("B3");
    if (cyc.getPlug(3)) {
      bindTexture(ResourceManager.cyclotron_coin_filled);
    } else {
      bindTexture(ResourceManager.cyclotron_coin);
      plugged = false;
    }
    ResourceManager.cyclotron.renderPart("B4");

    if (plugged) {

      GlStateManager.pushMatrix();
      RenderHelper.enableStandardItemLighting();
      GL11.glRotated(System.currentTimeMillis() * 0.025 % 360, 0, 1, 0);

      GlStateManager.enableBlend();
      GlStateManager.disableLighting();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
      GlStateManager.disableAlpha();

      String msg = "plures necat crapula quam gladius";

      GlStateManager.translate(0, 2, 0);
      GL11.glRotated(180, 1, 0, 0);

      float rot = 0F;

      // looks dumb but we'll use this technology for the cyclotron
      for (char c : msg.toCharArray()) {

        GlStateManager.pushMatrix();

        GlStateManager.rotate(rot, 0, 1, 0);

        rot -= Minecraft.getMinecraft().fontRenderer.getCharWidth(c) * 2F;

        GlStateManager.translate(2.75, 0, 0);

        GlStateManager.rotate(-90, 0, 1, 0);

        float scale = 0.1F;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.disableCull();
        Minecraft.getMinecraft()
            .standardGalacticFontRenderer
            .drawString(String.valueOf(c), 0, 0, 0x600060);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
      }

      GlStateManager.enableLighting();
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();

      GlStateManager.popMatrix();

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      RenderHelper.enableStandardItemLighting();
    }

    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_cyclotron);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -1, 0);
        GlStateManager.scale(2.75, 2.75, 2.75);
      }

      public void renderCommon() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.cyclotron_tex);
        ResourceManager.cyclotron.renderPart("Body");
        bindTexture(ResourceManager.cyclotron_ashes);
        ResourceManager.cyclotron.renderPart("B1");
        bindTexture(ResourceManager.cyclotron_book);
        ResourceManager.cyclotron.renderPart("B2");
        bindTexture(ResourceManager.cyclotron_gavel);
        ResourceManager.cyclotron.renderPart("B3");
        bindTexture(ResourceManager.cyclotron_coin);
        ResourceManager.cyclotron.renderPart("B4");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
