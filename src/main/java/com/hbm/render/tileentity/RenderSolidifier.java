package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderSolidifier extends TileEntitySpecialRenderer<TileEntityMachineSolidifier>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityMachineSolidifier liq,
      double x,
      double y,
      double z,
      float f,
      int destroyStage,
      float alpha) {

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.disableCull();

    switch (liq.getBlockMetadata() - BlockDummyable.offset) {
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
    bindTexture(ResourceManager.solidifier_tex);
    ResourceManager.solidifier.renderPart("Main");

    GlStateManager.disableLighting();
    GlStateManager.disableTexture2D();

    if (liq.tank.getFill() > 0) {
      int color = liq.tank.getTankType().getColor();
      GL11.glColor3ub(
          (byte) ((color & 0xFF0000) >> 16),
          (byte) ((color & 0x00FF00) >> 8),
          (byte) ((color & 0x0000FF) >> 0));

      double height = (double) liq.tank.getFill() / (double) liq.tank.getMaxFill();
      GlStateManager.pushMatrix();
      GlStateManager.translate(0, 1.25, 0);
      GlStateManager.scale(1, height, 1);
      GlStateManager.translate(0, -1.25, 0);
      ResourceManager.solidifier.renderPart("Fluid");
      GlStateManager.popMatrix();
    }

    GlStateManager.enableBlend();
    GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.color(0.75F, 1.0F, 1.0F, 0.15F);
    GlStateManager.depthMask(false);

    ResourceManager.solidifier.renderPart("Glass");

    GlStateManager.depthMask(true);
    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    GlStateManager.disableBlend();
    GlStateManager.enableTexture2D();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.enableCull();
    GlStateManager.enableLighting();
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_solidifier);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -2.5, 0);
        GlStateManager.scale(3, 3, 3);
      }

      public void renderCommon() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.solidifier_tex);
        ResourceManager.solidifier.renderPart("Main");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
