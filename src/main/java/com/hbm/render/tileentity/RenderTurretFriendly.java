package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.turret.TileEntityTurretFriendly;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
@AutoRegister(tileentity = TileEntityTurretFriendly.class)
public class RenderTurretFriendly extends RenderTurretBase<TileEntityTurretFriendly>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityTurretFriendly turret,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    Vec3d pos = turret.byHorizontalIndexOffset();

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + pos.x, y, z + pos.z);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);

    this.renderConnectors(turret, true, false, null);

    bindTexture(ResourceManager.turret_base_friendly_tex);
    ResourceManager.turret_chekhov.renderPart("Base");
    double yaw =
        -Math.toDegrees(
                turret.lastRotationYaw
                    + (turret.rotationYaw - turret.lastRotationYaw) * partialTicks)
            - 90D;
    double pitch =
        Math.toDegrees(
            turret.lastRotationPitch
                + (turret.rotationPitch - turret.lastRotationPitch) * partialTicks);

    GlStateManager.rotate((float) (yaw), 0, 1, 0);
    bindTexture(ResourceManager.turret_carriage_friendly_tex);
    ResourceManager.turret_chekhov.renderPart("Carriage");

    GlStateManager.translate(0, 1.5, 0);
    GlStateManager.rotate((float) (pitch), 0, 0, 1);
    GlStateManager.translate(0, -1.5, 0);
    bindTexture(ResourceManager.turret_chekhov_tex);
    ResourceManager.turret_chekhov.renderPart("Body");

    float rot = turret.lastSpin + (turret.spin - turret.lastSpin) * partialTicks;

    GlStateManager.translate(0, 1.5, 0);
    GlStateManager.rotate((float) (rot), -1, 0, 0);
    GlStateManager.translate(0, -1.5, 0);
    bindTexture(ResourceManager.turret_chekhov_barrels_tex);
    ResourceManager.turret_chekhov.renderPart("Barrels");

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.turret_friendly);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -3, 0);
        GlStateManager.scale(4, 4, 4);
      }

      public void renderCommon() {
        GlStateManager.translate(-0.75, 0, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.turret_base_friendly_tex);
        ResourceManager.turret_chekhov.renderPart("Base");
        bindTexture(ResourceManager.turret_carriage_friendly_tex);
        ResourceManager.turret_chekhov.renderPart("Carriage");
        bindTexture(ResourceManager.turret_chekhov_tex);
        ResourceManager.turret_chekhov.renderPart("Body");
        bindTexture(ResourceManager.turret_chekhov_barrels_tex);
        ResourceManager.turret_chekhov.renderPart("Barrels");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
