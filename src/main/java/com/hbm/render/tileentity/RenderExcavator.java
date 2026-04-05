package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineExcavator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderExcavator extends TileEntitySpecialRenderer<TileEntityMachineExcavator>
    implements IItemRendererProvider {

  public static final ResourceLocation cobble =
      new ResourceLocation("minecraft:textures/blocks/cobblestone.png");
  public static final ResourceLocation gravel =
      new ResourceLocation("minecraft:textures/blocks/gravel.png");
  @Override
  public void render(
      TileEntityMachineExcavator drill,
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

    switch (drill.getBlockMetadata() - BlockDummyable.offset) {
      case 3:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 2:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
    }

    GlStateManager.translate(0, -3, 0);

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.excavator_tex);
    ResourceManager.excavator.renderPart("Main");

    float crusher =
        drill.prevCrusherRotation
            + (drill.crusherRotation - drill.prevCrusherRotation) * partialTicks;
    GlStateManager.pushMatrix();
    GlStateManager.translate(0.0F, 2.0F, 2.8125F);
    GlStateManager.rotate(-crusher, 1, 0, 0);
    GlStateManager.translate(0.0F, -2.0F, -2.8125F);
    ResourceManager.excavator.renderPart("Crusher1");
    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    GlStateManager.translate(0.0F, 2.0F, 2.1875F);
    GlStateManager.rotate(crusher, 1, 0, 0);
    GlStateManager.translate(0.0F, -2.0F, -2.1875F);
    ResourceManager.excavator.renderPart("Crusher2");
    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    GlStateManager.rotate(
        drill.prevDrillRotation + (drill.drillRotation - drill.prevDrillRotation) * partialTicks,
        0F,
        -1F,
        0F);
    float ext =
        drill.prevDrillExtension + (drill.drillExtension - drill.prevDrillExtension) * partialTicks;
    GlStateManager.translate(0.0F, -ext, 0.0F);
    ResourceManager.excavator.renderPart("Drillbit");

    while (ext >= -1.5) {
      ResourceManager.excavator.renderPart("Shaft");
      GlStateManager.translate(0.0D, 2.0D, 0.0D);
      ext -= 2;
    }
    GlStateManager.popMatrix();

    GlStateManager.shadeModel(GL11.GL_FLAT);

    if (drill.chuteTimer > 0) {
      bindTexture(cobble);
      double widthX = 0.125;
      double widthZ = 0.125;
      double speed = 250D;
      double dropU = -System.currentTimeMillis() % speed / speed;
      double dropL = dropU + 4;
      NTMRenderHelper.startDrawingTexturedQuads();
      NTMRenderHelper.addVertexWithUV(widthX, 3, 2.5 + widthZ, 0, dropU);
      NTMRenderHelper.addVertexWithUV(-widthX, 3, 2.5 + widthZ, 1, dropU);
      NTMRenderHelper.addVertexWithUV(-widthX, 2, 2.5 + widthZ, 1, dropL);
      NTMRenderHelper.addVertexWithUV(widthX, 2, 2.5 + widthZ, 0, dropL);

      NTMRenderHelper.addVertexWithUV(-widthX, 3, 2.5 - widthZ, 1, dropU);
      NTMRenderHelper.addVertexWithUV(widthX, 3, 2.5 - widthZ, 0, dropU);
      NTMRenderHelper.addVertexWithUV(widthX, 2, 2.5 - widthZ, 0, dropL);
      NTMRenderHelper.addVertexWithUV(-widthX, 2, 2.5 - widthZ, 1, dropL);

      NTMRenderHelper.addVertexWithUV(-widthX, 3, 2.5 + widthZ, 0, dropU);
      NTMRenderHelper.addVertexWithUV(-widthX, 3, 2.5 - widthZ, 1, dropU);
      NTMRenderHelper.addVertexWithUV(-widthX, 2, 2.5 - widthZ, 1, dropL);
      NTMRenderHelper.addVertexWithUV(-widthX, 2, 2.5 + widthZ, 0, dropL);

      NTMRenderHelper.addVertexWithUV(widthX, 3, 2.5 - widthZ, 1, dropU);
      NTMRenderHelper.addVertexWithUV(widthX, 3, 2.5 + widthZ, 0, dropU);
      NTMRenderHelper.addVertexWithUV(widthX, 2, 2.5 + widthZ, 0, dropL);
      NTMRenderHelper.addVertexWithUV(widthX, 2, 2.5 - widthZ, 1, dropL);
      NTMRenderHelper.draw();

      boolean smoosh = drill.enableCrusher;
      widthX = smoosh ? 0.5 : 0.25;
      widthZ = 0.0625;
      double uU = smoosh ? 4 : 2;
      double uL = 0.5;
      bindTexture(smoosh ? gravel : cobble);
      NTMRenderHelper.startDrawingTexturedQuads();
      NTMRenderHelper.addVertexWithUV(widthX, 2, 2.5 + widthZ, 0, dropU);
      NTMRenderHelper.addVertexWithUV(-widthX, 2, 2.5 + widthZ, uU, dropU);
      NTMRenderHelper.addVertexWithUV(-widthX, 1, 2.5 + widthZ, uU, dropL);
      NTMRenderHelper.addVertexWithUV(widthX, 1, 2.5 + widthZ, 0, dropL);

      NTMRenderHelper.addVertexWithUV(-widthX, 2, 2.5 - widthZ, uU, dropU);
      NTMRenderHelper.addVertexWithUV(widthX, 2, 2.5 - widthZ, 0, dropU);
      NTMRenderHelper.addVertexWithUV(widthX, 1, 2.5 - widthZ, 0, dropL);
      NTMRenderHelper.addVertexWithUV(-widthX, 1, 2.5 - widthZ, uU, dropL);

      NTMRenderHelper.addVertexWithUV(-widthX, 2, 2.5 + widthZ, 0, dropU);
      NTMRenderHelper.addVertexWithUV(-widthX, 2, 2.5 - widthZ, uL, dropU);
      NTMRenderHelper.addVertexWithUV(-widthX, 1, 2.5 - widthZ, uL, dropL);
      NTMRenderHelper.addVertexWithUV(-widthX, 1, 2.5 + widthZ, 0, dropL);

      NTMRenderHelper.addVertexWithUV(widthX, 2, 2.5 - widthZ, uL, dropU);
      NTMRenderHelper.addVertexWithUV(widthX, 2, 2.5 + widthZ, 0, dropU);
      NTMRenderHelper.addVertexWithUV(widthX, 1, 2.5 + widthZ, 0, dropL);
      NTMRenderHelper.addVertexWithUV(widthX, 1, 2.5 - widthZ, uL, dropL);
      NTMRenderHelper.draw();
    }

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_excavator);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -2, 0);
        GlStateManager.scale(3, 3, 3);
      }

      public void renderCommon() {
        GlStateManager.rotate(90, 0F, 1F, 0F);
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.excavator_tex);
        ResourceManager.excavator.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
