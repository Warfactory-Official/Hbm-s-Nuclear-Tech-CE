package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.network.TileEntityCraneSplitter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
@AutoRegister
public class RenderCraneSplitter extends TileEntitySpecialRenderer<TileEntityCraneSplitter>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityCraneSplitter te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();
    switch (te.getBlockMetadata() - BlockDummyable.offset) {
      case 3:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case 2:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
    }

    GlStateManager.translate(-0.5D, 0, 0.5D);

    bindTexture(ResourceManager.splitter_tex);
    ResourceManager.crane_splitter.renderAll();

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.crane_splitter);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(3.25D, 1.125D, 0D);
        GlStateManager.scale(6.5, 6.5, 6.5);
      }

      public void renderCommon() {
        bindTexture(ResourceManager.splitter_tex);
        ResourceManager.crane_splitter.renderAll();
      }
    };
  }
}
