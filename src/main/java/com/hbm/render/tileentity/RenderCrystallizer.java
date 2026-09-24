package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineCrystallizer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
@AutoRegister
public class RenderCrystallizer extends TileEntitySpecialRenderer<TileEntityMachineCrystallizer>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityMachineCrystallizer crys,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.enableAlpha();
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();

    switch (crys.getBlockMetadata() - 10) {
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
    bindTexture(ResourceManager.crystallizer_tex);
    ResourceManager.crystallizer.renderPart("Body");

    GlStateManager.pushMatrix();
    GlStateManager.rotate(crys.prevAngle + (crys.angle - crys.prevAngle) * partialTicks, 0, 1, 0);
    ResourceManager.crystallizer.renderPart("Spinner");
    GlStateManager.popMatrix();

    if (crys.prevAngle != crys.angle) {
      GlStateManager.enableBlend();
      GlStateManager.depthMask(false);
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      bindTexture(crys.tankNew.getTankType().getTexture());
      ResourceManager.crystallizer.renderPart("Fluid");
      GlStateManager.depthMask(true);
      GlStateManager.disableBlend();
    }

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_crystallizer);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderNonInv() {
        GlStateManager.scale(0.5, 0.5, 0.5);
      }

      public void renderInventory() {
        GlStateManager.translate(0, -4, 0);
        GlStateManager.scale(2, 2, 2);
      }

      public void renderCommon() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.crystallizer_tex);
        ResourceManager.crystallizer.renderPart("Body");
        ResourceManager.crystallizer.renderPart("Spinner");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
