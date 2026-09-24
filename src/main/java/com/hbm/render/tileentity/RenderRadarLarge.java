package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineRadarLarge;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
@AutoRegister
public class RenderRadarLarge extends TileEntitySpecialRenderer<TileEntityMachineRadarLarge>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityMachineRadarLarge radar,
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
    GlStateManager.rotate(180, 0F, 1F, 0F);

    bindTexture(ResourceManager.radar_large_tex);
    ResourceManager.radar_large.renderPart("Radar");
    
    GlStateManager.rotate(
        radar.prevRotation + (radar.rotation - radar.prevRotation) * partialTicks, 0F, -1F, 0F);

    ResourceManager.radar_large.renderPart("Dish");

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_radar_large);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -5, 0);
        GlStateManager.scale(3, 3, 3);
      }

      public void renderCommon() {
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.scale(0.5, 0.5, 0.5);
        bindTexture(ResourceManager.radar_large_tex);
        ResourceManager.radar_large.renderPart("Radar");
        GlStateManager.rotate(System.currentTimeMillis() % 3600 * 0.1F, 0, -1, 0);
        ResourceManager.radar_large.renderPart("Dish");
      }
    };
  }
}
