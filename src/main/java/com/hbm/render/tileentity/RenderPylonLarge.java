package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.network.energy.TileEntityPylonBase;
import com.hbm.tileentity.network.energy.TileEntityPylonLarge;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;

@AutoRegister(tileentity = TileEntityPylonLarge.class)
public class RenderPylonLarge extends RenderPylonBase implements IItemRendererProvider {
  @Override
  public void render(TileEntityPylonBase tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
    if(!(tile instanceof TileEntityPylonLarge pyl)) return;
    GlStateManager.pushMatrix();
    GlStateManager.translate((float) x + 0.5F, (float) y, (float) z + 0.5F);
    switch (pyl.getBlockMetadata() - BlockDummyable.offset) {
      case 2 -> GlStateManager.rotate(90, 0F, 1F, 0F);
      case 4 -> GlStateManager.rotate(135, 0F, 1F, 0F);
      case 3 -> GlStateManager.rotate(0, 0F, 1F, 0F);
      case 5 -> GlStateManager.rotate(45, 0F, 1F, 0F);
    }
    bindTexture(ResourceManager.pylon_large_tex);
    ResourceManager.pylon_large.renderAll();
    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    this.renderLinesGeneric(pyl, x, y, z);
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.red_pylon_large);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -5, 0);
        GlStateManager.scale(2.25, 2.25, 2.25);
      }

      public void renderCommon() {
        GlStateManager.scale(0.5, 0.5, 0.5);
        bindTexture(ResourceManager.pylon_large_tex);
        ResourceManager.pylon_large.renderAll();
      }
    };
  }
}
