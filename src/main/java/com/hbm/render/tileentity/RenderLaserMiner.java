package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.util.Vec3NT;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityMachineMiningLaser;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderLaserMiner extends TileEntitySpecialRenderer<TileEntityMachineMiningLaser>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityMachineMiningLaser laser,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5, y - 1, z + 0.5);

    double tx = x;
    double ty = 0;
    double tz = z;
    if (laser.beam) {
      tx = (laser.targetX - laser.lastTargetX) * partialTicks + laser.lastTargetX;
      ty = (laser.targetY - laser.lastTargetY) * partialTicks + laser.lastTargetY;
      tz = (laser.targetZ - laser.lastTargetZ) * partialTicks + laser.lastTargetZ;
    }
    double vx = tx - laser.getPos().getX();
    double vy = ty - laser.getPos().getY() + 3;
    double vz = tz - laser.getPos().getZ();

    Vec3NT nVec = Vec3NT.createVectorHelper(vx, vy, vz);
    nVec = nVec.normalize();

    double d = 1.5D;
    nVec.setX(nVec.x * (d));
    nVec.setY(nVec.y * (d));
    nVec.setZ(nVec.z * (d));

    Vec3NT vec = Vec3NT.createVectorHelper(vx - nVec.x, vy - nVec.y, vz - nVec.z);

    double length = vec.length();
    double yaw = Math.toDegrees(Math.atan2(vec.x, vec.z));
    double sqrt = MathHelper.sqrt(vec.x * vec.x + vec.z * vec.z);
    double pitch = Math.toDegrees(Math.atan2(vec.y, sqrt));
    // turns out using tan(vec.y, length) was inaccurate,
    // the emitter wouldn't match the laser perfectly when pointing down

    bindTexture(ResourceManager.mining_laser_base_tex);
    ResourceManager.mining_laser.renderPart("Base");

    // GlStateManager.shadeModel(GL11.GL_SMOOTH);
    GlStateManager.pushMatrix();
    GlStateManager.rotate((float) (yaw), 0, 1, 0);
    bindTexture(ResourceManager.mining_laser_pivot_tex);
    ResourceManager.mining_laser.renderPart("Pivot");
    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    GlStateManager.rotate((float) (yaw), 0, 1, 0);
    GlStateManager.translate(0, -1, 0);
    GlStateManager.rotate((float) (pitch + 90), -1, 0, 0);
    GlStateManager.translate(0, 1, 0);
    bindTexture(ResourceManager.mining_laser_laser_tex);
    ResourceManager.mining_laser.renderPart("Laser");
    GlStateManager.popMatrix();
    // GlStateManager.shadeModel(GL11.GL_FLAT);

    if (laser.beam) {
      length = vec.length();
      GlStateManager.translate(nVec.x, nVec.y - 1, nVec.z);
      int range = (int) Math.ceil(length * 0.5);
      BeamPronter.prontBeam(
          vec.toVec3d(), EnumWaveType.STRAIGHT, EnumBeamType.SOLID, 0xa00000, 0xFFFFFF, 0, 1, 0, 3, 0.09F);
    }

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_mining_laser);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -0.5, 0);
        GlStateManager.scale(3, 3, 3);
      }

      public void renderCommon() {
        bindTexture(ResourceManager.mining_laser_base_tex);
        ResourceManager.mining_laser.renderPart("Base");
        bindTexture(ResourceManager.mining_laser_pivot_tex);
        ResourceManager.mining_laser.renderPart("Pivot");
        GlStateManager.translate(0, -1, 0.75);
        GlStateManager.rotate(90, 1, 0, 0);
        bindTexture(ResourceManager.mining_laser_laser_tex);
        ResourceManager.mining_laser.renderPart("Laser");
      }
    };
  }
}
