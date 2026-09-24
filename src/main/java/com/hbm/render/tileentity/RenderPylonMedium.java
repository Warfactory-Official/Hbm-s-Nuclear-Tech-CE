package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.network.energy.TileEntityPylonBase;
import com.hbm.tileentity.network.energy.TileEntityPylonMedium;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(tileentity = TileEntityPylonMedium.class)
public class RenderPylonMedium extends RenderPylonBase implements IItemRendererProvider {

  @Override
  public void render(TileEntityPylonBase tile, double x, double y, double z, float f, int destroyStage, float alpha) {
    if(!(tile instanceof TileEntityPylonMedium pyl)) return;
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5, y, z + 0.5);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();

    switch (tile.getBlockMetadata() - BlockDummyable.offset) {
      case 2 -> GlStateManager.rotate(180, 0F, 1F, 0F);
      case 4 -> GlStateManager.rotate(270, 0F, 1F, 0F);
      case 3 -> GlStateManager.rotate(0, 0F, 1F, 0F);
      case 5 -> GlStateManager.rotate(90, 0F, 1F, 0F);
    }

    if (tile.getBlockType() == ModBlocks.red_pylon_medium_steel || tile.getBlockType() == ModBlocks.red_pylon_medium_steel_transformer)
      bindTexture(ResourceManager.pylon_medium_steel_tex);
    else bindTexture(ResourceManager.pylon_medium_tex);

    ResourceManager.pylon_medium.renderPart("Pylon");
    if (pyl.hasTransformer()) ResourceManager.pylon_medium.renderPart("Transformer");

    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    this.renderLinesGeneric(pyl, x, y, z);
    GlStateManager.popMatrix();
  }

  @Override
  public Item[] getItemsForRenderer() {
    return new Item[] {
      Item.getItemFromBlock(ModBlocks.red_pylon_medium_wood),
      Item.getItemFromBlock(ModBlocks.red_pylon_medium_wood_transformer),
      Item.getItemFromBlock(ModBlocks.red_pylon_medium_steel),
      Item.getItemFromBlock(ModBlocks.red_pylon_medium_steel_transformer)
    };
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.red_pylon_medium_wood);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(1, -5, 0);
        GlStateManager.scale(4.5, 4.5, 4.5);
      }

      public void renderCommon(ItemStack stack) {
        GlStateManager.rotate(90, 0F, 1F, 0F);
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.translate(0.75, 0, 0);

        if (stack.getItem() == Item.getItemFromBlock(ModBlocks.red_pylon_medium_steel) || stack.getItem() == Item.getItemFromBlock(ModBlocks.red_pylon_medium_steel_transformer))
          bindTexture(ResourceManager.pylon_medium_steel_tex);
        else bindTexture(ResourceManager.pylon_medium_tex);

        ResourceManager.pylon_medium.renderPart("Pylon");

        if (stack.getItem() == Item.getItemFromBlock(ModBlocks.red_pylon_medium_wood_transformer) || stack.getItem() == Item.getItemFromBlock(ModBlocks.red_pylon_medium_steel_transformer))
          ResourceManager.pylon_medium.renderPart("Transformer");
      }
    };
  }
}
