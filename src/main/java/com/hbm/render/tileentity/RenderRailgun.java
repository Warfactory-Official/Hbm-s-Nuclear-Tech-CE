package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.RenderSparks;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.item.ItemRenderBaseLegacy;
import com.hbm.tileentity.bomb.TileEntityRailgun;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderRailgun extends TileEntitySpecialRenderer<TileEntityRailgun>
    implements IItemRendererProvider {

  @Override
  public void render(
      TileEntityRailgun gun,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();
    GlStateManager.rotate(180, 0F, 1F, 0F);

    bindTexture(ResourceManager.railgun_base_tex);
    ResourceManager.railgun_base.renderAll();

    float yaw = gun.yaw;
    float pitch = gun.pitch;

    if (System.currentTimeMillis() < gun.startTime + TileEntityRailgun.cooldownDurationMillis) {
      float interpolation =
          (float) (System.currentTimeMillis() - gun.startTime)
              / (float) TileEntityRailgun.cooldownDurationMillis
              * 100F;

      float yi = (gun.yaw - gun.lastYaw) * interpolation / 100F;
      yaw = gun.lastYaw + yi;

      float pi = (gun.pitch - gun.lastPitch) * interpolation / 100F;
      pitch = gun.lastPitch + pi;
    }

    int max = 5;
    int count =
        max
            - (int)
                (((gun.fireTime + TileEntityRailgun.cooldownDurationMillis)
                        - System.currentTimeMillis())
                    * max
                    / TileEntityRailgun.cooldownDurationMillis);

    if (System.currentTimeMillis() < gun.fireTime + TileEntityRailgun.cooldownDurationMillis) {
      Vec3 vec = Vec3.createVectorHelper(5.375, 0, 0);
      vec.rotateAroundZ((float) (pitch * Math.PI / 180D));
      vec.rotateAroundY((float) (yaw * Math.PI / 180D));

      double fX = vec.xCoord;
      double fY = 1 + vec.yCoord;
      double fZ = vec.zCoord;
      GlStateManager.rotate(180, 0F, 1F, 0F);
      for (int i = 0; i < count; i++)
        RenderSparks.renderSpark(
            (int) System.currentTimeMillis() / 100 + i * 10000,
            fX,
            fY,
            fZ,
            0.75F,
            5,
            6,
            0x0088FF,
            0xDFDFFF);
      for (int i = 0; i < count; i++)
        RenderSparks.renderSpark(
            (int) System.currentTimeMillis() / 50 + i * 10000,
            fX,
            fY,
            fZ,
            0.75F,
            5,
            6,
            0x0088FF,
            0xDFDFFF);
      GlStateManager.rotate(180, 0F, 1F, 0F);
    }

    GlStateManager.rotate(yaw, 0, 1, 0);
    bindTexture(ResourceManager.railgun_rotor_tex);
    ResourceManager.railgun_rotor.renderAll();

    GlStateManager.translate(0, 1F, 0);
    GlStateManager.rotate(pitch, 0, 0, 1);
    GlStateManager.translate(0, -1F, 0);
    bindTexture(ResourceManager.railgun_main_tex);
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    ResourceManager.railgun_main.renderAll();
    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.railgun_plasma);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBaseLegacy() {
      public void renderInventory() {
        GlStateManager.translate(0, 2, -4);
        GlStateManager.scale(2.5, 2.5, 2.5);
      }

      public void renderCommon() {
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.railgun_base_tex);
        ResourceManager.railgun_base.renderAll();
        bindTexture(ResourceManager.railgun_rotor_tex);
        ResourceManager.railgun_rotor.renderAll();
        bindTexture(ResourceManager.railgun_main_tex);
        ResourceManager.railgun_main.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
