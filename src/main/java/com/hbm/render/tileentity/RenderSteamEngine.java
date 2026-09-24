package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineSteamEngine;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderSteamEngine extends TileEntitySpecialRenderer<TileEntityMachineSteamEngine>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityMachineSteamEngine tile,
      double x,
      double y,
      double z,
      float interp,
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

    float angle = tile.lastRotor + (tile.rotor - tile.lastRotor) * interp;
    GlStateManager.translate(2, 0, 0);
    renderCommon(angle);

    GlStateManager.popMatrix();
  }

  public void renderCommon(double rot) {
    GlStateManager.disableCull();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);

    bindTexture(ResourceManager.steam_engine_tex);
    ResourceManager.steam_engine.renderPart("Base");

    GlStateManager.pushMatrix();
    GlStateManager.translate(2, 1.375, 0);
    GlStateManager.rotate((float) (rot), 0, 0, -1);
    GlStateManager.translate(-2, -1.375, 0);
    ResourceManager.steam_engine.renderPart("Flywheel");
    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    GlStateManager.translate(0, 1.375, -0.5);
    GlStateManager.rotate((float) (rot * 2D), 1, 0, 0);
    GlStateManager.translate(0, -1.375, 0.5);
    ResourceManager.steam_engine.renderPart("Shaft");
    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    double sin = Math.sin(rot * Math.PI / 180D) * 0.25D - 0.25D;
    double cos = Math.cos(rot * Math.PI / 180D) * 0.25D;
    double ang = Math.acos(cos / 1.875D);
    GlStateManager.translate(sin, cos, 0);
    GlStateManager.translate(2.25, 1.375, 0);
    GlStateManager.rotate((float) (ang * 180D / Math.PI - 90D), 0, 0, -1);
    GlStateManager.translate(-2.25, -1.375, 0);
    ResourceManager.steam_engine.renderPart("Transmission");
    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    double cath = Math.sqrt(3.515625D - (cos * cos) / 2);
    GlStateManager.translate(
        1.875 - cath + sin,
        0,
        0); // the difference that "1.875 - cath" makes is minuscule but very much noticeable
    ResourceManager.steam_engine.renderPart("Piston");
    GlStateManager.popMatrix();

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.enableCull();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_steam_engine);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.rotate(90, 0F, -1F, 0F);
        GlStateManager.translate(0, -1.5, 0);
        GlStateManager.scale(2, 2, 2);
      }

      public void renderCommon(ItemStack item) {
        GlStateManager.rotate(90, 0F, 1F, 0F);
        bindTexture(ResourceManager.steam_engine_tex);

        boolean cog = item.getItemDamage() != 1;
        RenderSteamEngine.this.renderCommon(cog ? System.currentTimeMillis() % 3600 * 0.1F : 0F);
      }
    };
  }
}
