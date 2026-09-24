package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.turret.TileEntityTurretFritz;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
@AutoRegister(tileentity = TileEntityTurretFritz.class)
public class RenderTurretFritz extends RenderTurretBase<TileEntityTurretFritz>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityTurretFritz turret,
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

    this.renderConnectors(
        turret, true, true, turret.tank.getTankType());

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

    GlStateManager.rotate((float) (yaw), 0, 1, 0);
    bindTexture(ResourceManager.turret_carriage_tex);
    ResourceManager.turret_chekhov.renderPart("Carriage");

    GlStateManager.translate(0, 1.5, 0);
    GlStateManager.rotate((float) (pitch), 0, 0, 1);
    GlStateManager.translate(0, -1.5, 0);
    bindTexture(ResourceManager.turret_fritz_tex);
    ResourceManager.turret_fritz.renderPart("Gun");

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.turret_fritz);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -2, 0);
        GlStateManager.scale(4, 4, 4);
      }

      public void renderCommon() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.turret_base_tex);
        ResourceManager.turret_chekhov.renderPart("Base");
        bindTexture(ResourceManager.turret_carriage_tex);
        ResourceManager.turret_chekhov.renderPart("Carriage");
        bindTexture(ResourceManager.turret_fritz_tex);
        ResourceManager.turret_fritz.renderPart("Gun");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
