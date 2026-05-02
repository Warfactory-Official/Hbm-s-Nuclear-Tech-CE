package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityITER;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.Fluid;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderITER extends TileEntitySpecialRenderer<TileEntityITER>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityITER iter,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha2) {
    GlStateManager.pushMatrix();

    GlStateManager.translate((float) x + 0.5F, (float) y - 2, (float) z + 0.5F);

    GlStateManager.enableCull();
    GlStateManager.enableLighting();

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.iter_glass);
    ResourceManager.iter.renderPart("Windows");
    bindTexture(ResourceManager.iter_motor);
    ResourceManager.iter.renderPart("Motors");
    bindTexture(ResourceManager.iter_rails);
    ResourceManager.iter.renderPart("Rails");
    bindTexture(ResourceManager.iter_toroidal);
    ResourceManager.iter.renderPart("Toroidal");
    switch (iter.blanket) {
      case 0:
        bindTexture(ResourceManager.iter_torus);
        break;
      case 1:
        bindTexture(ResourceManager.iter_torus_tungsten);
        break;
      case 2:
        bindTexture(ResourceManager.iter_torus_desh);
        break;
      case 3:
        bindTexture(ResourceManager.iter_torus_chlorophyte);
        break;
      case 4:
        bindTexture(ResourceManager.iter_torus_vaporwave);
        break;
      default:
        bindTexture(ResourceManager.iter_torus);
        break;
    }
    ResourceManager.iter.renderPart("Torus");

    GlStateManager.pushMatrix();
    GL11.glRotated(iter.lastRotor + (iter.rotor - iter.lastRotor) * partialTicks, 0, 1, 0);
    bindTexture(ResourceManager.iter_solenoid);
    ResourceManager.iter.renderPart("Solenoid");
    GlStateManager.popMatrix();

    if (iter.plasma.getFill() > 0) {
      GlStateManager.pushMatrix();
      GL11.glRotated(iter.lastRotor + (iter.rotor - iter.lastRotor) * partialTicks, 0, 1, 0);

      GlStateManager.disableLighting();
      GlStateManager.disableAlpha();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
      GlStateManager.depthMask(false);

      int color = iter.plasma.getTankType().getColor();

      double alpha = (double) iter.plasma.getFill() / (double) iter.plasma.getMaxFill();
      int r = (int) (((color & 0xFF0000) >> 16) / 2F * alpha);
      int g = (int) (((color & 0xFF00) >> 8) / 2F * alpha);
      int b = (int) ((color & 0xFF) / 2F * alpha);

      GlStateManager.color(r / 255F, g / 255F, b / 255F);
      GlStateManager.translate(0, 2.5F, 0);
      GL11.glScaled(1, alpha, 1);
      GlStateManager.translate(0, -2.5F, 0);

      bindTexture(ResourceManager.iter_plasma);
      ResourceManager.iter.renderPart("Plasma");

      GlStateManager.enableLighting();
      GlStateManager.enableAlpha();
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);

      GlStateManager.popMatrix();
    }

    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.popMatrix();
  }

  private int getColor(Fluid type) {
    if (type == Fluids.PLASMA_DT.getFF()) {
      return 0xFF3FC2;
    } else if (type == Fluids.PLASMA_HD.getFF()) {
      return 0xEB3FFF;
    } else if (type == Fluids.PLASMA_HT.getFF()) {
      return 0x9F3FFF;
      //		} else if(type == Fluids.plasma_put){
      //			return 0x3F99FF;
    } else if (type == Fluids.PLASMA_XM.getFF()) {
      return 0x3FFFFF;
    } else if (type == Fluids.PLASMA_BF.getFF()) {
      return 0xB8FF3F;
    }
    return 0;
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.iter);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -1, 0);
        GlStateManager.scale(4.5, 4.5, 4.5);
      }

      public void renderCommon() {
        GlStateManager.scale(0.25, 0.25, 0.25);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.iter_glass);
        ResourceManager.iter.renderPart("Windows");
        bindTexture(ResourceManager.iter_motor);
        ResourceManager.iter.renderPart("Motors");
        bindTexture(ResourceManager.iter_rails);
        ResourceManager.iter.renderPart("Rails");
        bindTexture(ResourceManager.iter_toroidal);
        ResourceManager.iter.renderPart("Toroidal");
        bindTexture(ResourceManager.iter_torus);
        ResourceManager.iter.renderPart("Torus");
        bindTexture(ResourceManager.iter_solenoid);
        ResourceManager.iter.renderPart("Solenoid");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
