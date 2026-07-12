package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineTurbofan;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderTurbofan extends TileEntitySpecialRenderer<TileEntityMachineTurbofan>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityMachineTurbofan turbo,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.disableCull();
    switch (turbo.getBlockMetadata() - 10) {
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
    float spin = turbo.lastSpin + (turbo.spin - turbo.lastSpin) * partialTicks;
    GlStateManager.disableLighting();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.turbofan_tex);

    GlStateManager.pushMatrix();
    GlStateManager.translate(0, 1.5, 0);
    GlStateManager.rotate(spin, 0F, 0F, -1F);
    GlStateManager.translate(0, -1.5, 0);
    ResourceManager.turbofan.renderPart("Blades");
    GlStateManager.popMatrix();

    GlStateManager.enableLighting();
    ResourceManager.turbofan.renderPart("Body");
    if (turbo.afterburner == 0) bindTexture(ResourceManager.turbofan_back_tex);
    else bindTexture(ResourceManager.turbofan_afterburner_tex);

    ResourceManager.turbofan.renderPart("Afterburner");
    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_turbofan);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.scale(2.25, 2.25, 2.25);
      }

      public void renderCommon() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.turbofan_tex);
        ResourceManager.turbofan.renderPart("Body");
        ResourceManager.turbofan.renderPart("Blades");
        bindTexture(ResourceManager.turbofan_back_tex);
        ResourceManager.turbofan.renderPart("Afterburner");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
