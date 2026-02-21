package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.RenderMiscEffects;
import com.hbm.tileentity.bomb.TileEntityNukeBalefire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderNukeFstbmb extends TileEntitySpecialRenderer<TileEntityNukeBalefire>
    implements IItemRendererProvider {

  @Override
  public boolean isGlobalRenderer(TileEntityNukeBalefire te) {
    return true;
  }

  @Override
  public void render(
      TileEntityNukeBalefire bf,
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
    switch (bf.getBlockMetadata()) {
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

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.fstbmb_tex);
    ResourceManager.fstbmb.renderPart("Body");
    ResourceManager.fstbmb.renderPart("Balefire");

    if (bf.loaded) {
      bindTexture(new ResourceLocation(Tags.MODID + ":textures/misc/glintBF.png"));
      RenderMiscEffects.renderClassicGlint(
          bf.getWorld(),
          partialTicks,
          ResourceManager.fstbmb,
          "Balefire",
          0.0F,
          0.8F,
          0.15F,
          5,
          2F);

      FontRenderer font = Minecraft.getMinecraft().fontRenderer;
      float f3 = 0.04F;
      GlStateManager.translate(0.815F, 0.9275F, 0.5F);
      GlStateManager.scale(f3, -f3, f3);
      GlStateManager.color(0.0F, 0.0F, -1.0F * f3);
      GlStateManager.rotate(90, 0, 1, 0);
      GlStateManager.depthMask(false);
      GlStateManager.translate(0, 1, 0);
      font.drawString(bf.getMinutes() + ":" + bf.getSeconds(), 0, 0, 0xff0000);
      GlStateManager.depthMask(true);
    }

    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.nuke_fstbmb);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.scale(2.25, 2.25, 2.25);
      }

      public void renderCommon() {
        GlStateManager.translate(1, 0, 0);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.fstbmb_tex);
        ResourceManager.fstbmb.renderPart("Body");
        ResourceManager.fstbmb.renderPart("Balefire");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
