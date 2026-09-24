package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.turret.TileEntityTurretHowardDamaged;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
@AutoRegister(tileentity = TileEntityTurretHowardDamaged.class)
public class RenderTurretHowardDamaged extends RenderTurretBase<TileEntityTurretHowardDamaged>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityTurretHowardDamaged turret,
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

    bindTexture(ResourceManager.turret_base_rusted);
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
    bindTexture(ResourceManager.turret_carriage_ciws_rusted);
    ResourceManager.turret_howard_damaged.renderPart("Carriage");

    GlStateManager.translate(0, 2.25, 0);
    GlStateManager.rotate((float) (pitch), 0, 0, 1);
    GlStateManager.translate(0, -2.25, 0);
    bindTexture(ResourceManager.turret_howard_rusted);
    ResourceManager.turret_howard_damaged.renderPart("Body");

    float rot = turret.lastSpin + (turret.spin - turret.lastSpin) * partialTicks;

    bindTexture(ResourceManager.turret_howard_barrels_rusted);

    GlStateManager.pushMatrix();
    GlStateManager.translate(0, 2.5, 0);
    GlStateManager.rotate((float) (rot), -1, 0, 0);
    GlStateManager.translate(0, -2.5, 0);
    ResourceManager.turret_howard_damaged.renderPart("BarrelsTop");
    GlStateManager.popMatrix();

    ResourceManager.turret_howard_damaged.renderPart("BarrelsBottom");

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.turret_howard_damaged);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -4.5, 0);
        GlStateManager.scale(4, 4, 4);
      }

      public void renderCommon() {
        GlStateManager.translate(-0.75, 0, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.turret_base_rusted);
        ResourceManager.turret_chekhov.renderPart("Base");
        bindTexture(ResourceManager.turret_carriage_ciws_rusted);
        ResourceManager.turret_howard.renderPart("Carriage");
        bindTexture(ResourceManager.turret_howard_rusted);
        ResourceManager.turret_howard_damaged.renderPart("Body");
        bindTexture(ResourceManager.turret_howard_barrels_rusted);
        ResourceManager.turret_howard_damaged.renderPart("BarrelsTop");
        bindTexture(ResourceManager.turret_howard_barrels_rusted);
        ResourceManager.turret_howard_damaged.renderPart("BarrelsBottom");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
