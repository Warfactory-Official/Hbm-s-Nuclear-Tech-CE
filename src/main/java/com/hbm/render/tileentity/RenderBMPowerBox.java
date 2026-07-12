package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.item.ItemRenderBaseLegacy;
import com.hbm.tileentity.machine.TileEntityBMPowerBox;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderBMPowerBox extends TileEntitySpecialRenderer<TileEntityBMPowerBox>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityBMPowerBox te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5, y, z + 0.5);
    switch (te.getBlockMetadata() >> 1) {
      case 2:
        GL11.glRotated(90, 0, 1, 0);
        break;
      case 3:
        GL11.glRotated(270, 0, 1, 0);
        break;
      case 4:
        GL11.glRotated(180, 0, 1, 0);
        break;
      case 5:
        break;
    }
    GlStateManager.translate(-0.5, 0, 0);
    bindTexture(ResourceManager.bm_box_lever_tex);
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    ResourceManager.bm_box_lever.renderPart("box");
    float rx = 0.077964F;
    float ry = 0.456887F;
    float rz = 0.104706F;
    GlStateManager.translate(rx, ry, rz);
    float time =
        (float)
            MathHelper.clamp(
                (double) (te.getWorld().getTotalWorldTime() - te.ticksPlaced + partialTicks) * 0.1,
                0,
                1);
    GL11.glRotated((te.getBlockMetadata() & 1) == 1 ? time * 180 : (1 - time) * 180, 0, 0, 1);
    GlStateManager.translate(-rx, -ry, -rz);
    ResourceManager.bm_box_lever.renderPart("lever");
    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.bm_power_box);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBaseLegacy() {
      public void renderInventory() {
        GlStateManager.translate(0, -6, 0);
        GlStateManager.scale(18, 18, 18);
      }

      public void renderCommon() {
        bindTexture(ResourceManager.bm_box_lever_tex);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.bm_box_lever.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
