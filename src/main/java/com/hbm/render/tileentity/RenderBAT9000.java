package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.DiamondPronter;
import com.hbm.render.misc.EnumSymbol;
import com.hbm.tileentity.machine.TileEntityMachineBAT9000;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderBAT9000 extends TileEntitySpecialRenderer<TileEntityMachineBAT9000>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityMachineBAT9000 bat,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.enableAlpha();
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5F, y, z + 0.5F);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();

    bindTexture(ResourceManager.bat9000_tex);

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    ResourceManager.bat9000.renderAll();
    GlStateManager.shadeModel(GL11.GL_FLAT);

    FluidType type = bat.tankNew.getTankType();

    if (type != null && type != Fluids.NONE) {
      RenderHelper.disableStandardItemLighting();
      GlStateManager.pushMatrix();
      int poison = type.poison;
      int flammability = type.flammability;
      int reactivity = type.reactivity;
      EnumSymbol symbol = type.symbol;

      GlStateManager.rotate(45F, 0F, 1F, 0F);

      for (int j = 0; j < 4; j++) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(2.5F, 2.25F, 0F);
        GlStateManager.scale(1.0F, 0.75F, 0.75F);
        DiamondPronter.pront(poison, flammability, reactivity, symbol);
        GlStateManager.popMatrix();
        GlStateManager.rotate(90F, 0F, 1F, 0F);
      }
      GlStateManager.popMatrix();
      RenderHelper.enableStandardItemLighting();
    }

    GlStateManager.disableTexture2D();
    GlStateManager.disableCull();
    GlStateManager.disableLighting();
    GlStateManager.color(1F, 1F, 1F, 1F);

    float height = bat.tankNew.getFill() * 1.5F / bat.tankNew.getMaxFill();
    float off = 2.2F;
    int color = type.getColor();

    Tessellator tess = Tessellator.getInstance();
    BufferBuilder buffer = tess.getBuffer();

    int r = (color >> 16) & 0xFF;
    int g = (color >> 8) & 0xFF;
    int b = color & 0xFF;

    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

    NTMRenderHelper.addQuad(
        buffer,
        -off,
        1.5F,
        -0.5F,
        -off,
        1.5F + height,
        -0.5F,
        -off,
        1.5F + height,
        0.5F,
        -off,
        1.5F,
        0.5F,
        r,
        g,
        b);
    NTMRenderHelper.addQuad(
        buffer,
        off,
        1.5F,
        -0.5F,
        off,
        1.5F + height,
        -0.5F,
        off,
        1.5F + height,
        0.5F,
        off,
        1.5F,
        0.5F,
        r,
        g,
        b);
    NTMRenderHelper.addQuad(
        buffer,
        -0.5F,
        1.5F,
        -off,
        -0.5F,
        1.5F + height,
        -off,
        0.5F,
        1.5F + height,
        -off,
        0.5F,
        1.5F,
        -off,
        r,
        g,
        b);
    NTMRenderHelper.addQuad(
        buffer,
        -0.5F,
        1.5F,
        off,
        -0.5F,
        1.5F + height,
        off,
        0.5F,
        1.5F + height,
        off,
        0.5F,
        1.5F,
        off,
        r,
        g,
        b);

    tess.draw();

    GlStateManager.enableLighting();
    GlStateManager.enableCull();
    GlStateManager.enableTexture2D();

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_bat9000);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -3, 0);
        GlStateManager.scale(2, 2, 2);
      }

      public void renderCommon() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.bat9000_tex);
        ResourceManager.bat9000.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
