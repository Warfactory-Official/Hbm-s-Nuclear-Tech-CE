package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityAshpit;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
@AutoRegister
public class RenderAshpit extends TileEntitySpecialRenderer<TileEntityAshpit>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityAshpit oven,
      double x,
      double y,
      double z,
      float interp,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();

    switch (oven.getBlockMetadata() - BlockDummyable.offset) {
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
    GlStateManager.rotate(-90, 0F, 1F, 0F);

    bindTexture(ResourceManager.ashpit_tex);
    ResourceManager.heater_oven.renderPart("Main");

    GlStateManager.pushMatrix();
    float door = oven.prevDoorAngle + (oven.doorAngle - oven.prevDoorAngle) * interp;
    GlStateManager.translate(0, 0, door * 0.75D / 135D);
    ResourceManager.heater_oven.renderPart("Door");
    GlStateManager.popMatrix();

    if (oven.isFull) {
      ResourceManager.heater_oven.renderPart("InnerBurning");
    } else {
      ResourceManager.heater_oven.renderPart("Inner");
    }

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_ashpit);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -1, 0);
        GlStateManager.scale(3.25, 3.25, 3.25);
      }

      public void renderCommon() {
        bindTexture(ResourceManager.ashpit_tex);
        ResourceManager.heater_oven.renderPart("Main");
        ResourceManager.heater_oven.renderPart("Door");
      }
    };
  }
}
