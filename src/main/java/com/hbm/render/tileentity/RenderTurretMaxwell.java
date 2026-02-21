package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.BeamPronter;
import com.hbm.tileentity.turret.TileEntityTurretMaxwell;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

@AutoRegister(tileentity = TileEntityTurretMaxwell.class)
public class RenderTurretMaxwell extends RenderTurretBase<TileEntityTurretMaxwell>
    implements IItemRendererProvider {

  @Override
  public boolean isGlobalRenderer(TileEntityTurretMaxwell te) {
    return te.beam > 0;
  }

  @Override
  public void render(
      TileEntityTurretMaxwell turret,
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

    bindTexture(ResourceManager.turret_base_tex);
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

    GL11.glRotated(yaw, 0, 1, 0);
    bindTexture(ResourceManager.turret_carriage_ciws_tex);
    ResourceManager.turret_howard.renderPart("Carriage");

    GlStateManager.translate(0, 1.5, 0);
    GL11.glRotated(pitch, 0, 0, 1);
    GlStateManager.translate(0, -1.5, 0);
    bindTexture(ResourceManager.turret_maxwell_tex);
    ResourceManager.turret_maxwell.renderPart("Microwave");

    if (turret.beam > 0) {
      double length = turret.lastDist - turret.getBarrelLength();

      GlStateManager.pushMatrix();
      GlStateManager.color(1, 1, 1, 1);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
      GlStateManager.translate(turret.getBarrelLength(), 2D, 0);

      for (int i = 0; i < 8; i++) {
        BeamPronter.prontBeam(
            new Vec3d(length, 0, 0),
            BeamPronter.EnumWaveType.SPIRAL,
            BeamPronter.EnumBeamType.SOLID,
            0x2020FF,
            0x2020FF,
            (int) ((turret.getWorld().getTotalWorldTime() + partialTicks * -50 + i * 45) % 360),
            (int) (turret.lastDist + 1),
            0.375F,
            2,
            0.05F);
      }

      GlStateManager.popMatrix();
    }

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.turret_maxwell);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(-1, -3, 0);
        GlStateManager.scale(4, 4, 4);
      }

      public void renderCommon() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.turret_base_tex);
        ResourceManager.turret_chekhov.renderPart("Base");
        bindTexture(ResourceManager.turret_carriage_ciws_tex);
        ResourceManager.turret_howard.renderPart("Carriage");
        bindTexture(ResourceManager.turret_maxwell_tex);
        ResourceManager.turret_maxwell.renderPart("Microwave");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
