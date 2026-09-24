package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachinePumpBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderPump extends TileEntitySpecialRenderer<TileEntityMachinePumpBase>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityMachinePumpBase tile,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();

    switch (tile.getBlockMetadata() - BlockDummyable.offset) {
      case 3:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 2:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
    }
    float angle = tile.lastRotor + (tile.rotor - tile.lastRotor) * partialTicks;
    renderCommon(angle, tile.getBlockType() == ModBlocks.pump_steam ? 0 : 1);

    GlStateManager.popMatrix();
  }

  private void renderCommon(double rot, int type) {
    GlStateManager.disableCull();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);

    if (type == 0) bindTexture(ResourceManager.pump_steam_tex);
    else bindTexture(ResourceManager.pump_electric_tex);
    ResourceManager.pump.renderPart("Base");

    GlStateManager.pushMatrix();
    GlStateManager.translate(0, 2.25, 0);
    GlStateManager.rotate((float) (rot - 90), 0, 0, 1);
    GlStateManager.translate(0, -2.25, 0);
    ResourceManager.pump.renderPart("Rotor");
    GlStateManager.popMatrix();

    double sin = Math.sin(rot * Math.PI / 180D) * 0.5D - 0.5D;
    double cos = Math.cos(rot * Math.PI / 180D) * 0.5D;
    double ang = Math.acos(cos / 2D);
    double cath = Math.sqrt(1 + (cos * cos) / 2);

    GlStateManager.pushMatrix();
    GlStateManager.translate(0, 1 - cath + sin, 0);
    GlStateManager.translate(0, 4.75, 0);
    GlStateManager.rotate((float) (ang * 180D / Math.PI - 90D), 0, 0, -1);
    GlStateManager.translate(0, -4.75, 0);
    ResourceManager.pump.renderPart("Arms");
    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    GlStateManager.translate(0, 1 - cath + sin, 0);
    ResourceManager.pump.renderPart("Piston");
    GlStateManager.popMatrix();

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.enableCull();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.pump_steam);
  }

  @Override
  public Item[] getItemsForRenderer() {
    return new Item[] {
      Item.getItemFromBlock(ModBlocks.pump_steam), Item.getItemFromBlock(ModBlocks.pump_electric)
    };
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -3, 0);
        GlStateManager.scale(2.5, 2.5, 2.5);
      }

      public void renderCommon(ItemStack item) {
        RenderPump.this.renderCommon(
            System.currentTimeMillis() % 3600 * 0.1F,
            item.getItem() == Item.getItemFromBlock(ModBlocks.pump_steam) ? 0 : 1);
      }
    };
  }
}
