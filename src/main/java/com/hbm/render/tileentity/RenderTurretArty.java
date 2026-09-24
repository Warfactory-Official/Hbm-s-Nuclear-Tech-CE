package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.turret.TileEntityTurretArty;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderTurretArty extends TileEntitySpecialRenderer<TileEntityTurretArty>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityTurretArty turret,
      double x,
      double y,
      double z,
      float interp,
      int destroyStage,
      float alpha) {
    Vec3d pos = turret.byHorizontalIndexOffset();

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + pos.x, y, z + pos.z);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);

    bindTexture(ResourceManager.turret_arty_tex);
    ResourceManager.turret_arty.renderPart("Base");
    double yaw =
        -Math.toDegrees(
                turret.lastRotationYaw + (turret.rotationYaw - turret.lastRotationYaw) * interp)
            - 90D;
    double pitch =
        Math.toDegrees(
            turret.lastRotationPitch + (turret.rotationPitch - turret.lastRotationPitch) * interp);

    GlStateManager.rotate((float) (yaw - 90), 0, 1, 0);
    ResourceManager.turret_arty.renderPart("Carriage");

    GlStateManager.translate(0, 3, 0);
    GlStateManager.rotate((float) (pitch), 1, 0, 0);
    GlStateManager.translate(0, -3, 0);
    ResourceManager.turret_arty.renderPart("Cannon");
    double barrel = turret.lastBarrelPos + (turret.barrelPos - turret.lastBarrelPos) * interp;
    double length = 2.5;
    GlStateManager.translate(0, 0, barrel * length);
    ResourceManager.turret_arty.renderPart("Barrel");

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.turret_arty);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(-3, -4, 0);
        GlStateManager.scale(3.5, 3.5, 3.5);
      }

      public void renderCommon() {
        GlStateManager.rotate(-90, 0, 1, 0);
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.turret_arty_tex);
        ResourceManager.turret_arty.renderPart("Base");
        ResourceManager.turret_arty.renderPart("Carriage");
        GlStateManager.translate(0, 3, 0);
        GlStateManager.rotate(45, 1, 0, 0);
        GlStateManager.translate(0, -3, 0);
        ResourceManager.turret_arty.renderPart("Cannon");
        ResourceManager.turret_arty.renderPart("Barrel");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
