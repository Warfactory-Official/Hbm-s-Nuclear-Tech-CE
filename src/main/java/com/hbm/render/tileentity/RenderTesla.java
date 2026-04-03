package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityTesla;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
@AutoRegister
public class RenderTesla extends TileEntitySpecialRenderer<TileEntityTesla>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityTesla tesla,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.rotate(180, 0F, 1F, 0F);

    bindTexture(ResourceManager.tesla_tex);
    ResourceManager.tesla.renderAll();
    GlStateManager.enableCull();

    double sx = tesla.getPos().getX() + 0.5D;
    double sy = tesla.getPos().getY() + TileEntityTesla.offset;
    double sz = tesla.getPos().getZ() + 0.5D;

    GlStateManager.translate(0.0D, TileEntityTesla.offset, 0.0D);
    for (double[] target : tesla.targets) {

      double length =
          Math.sqrt(
              Math.pow(target[0] - sx, 2)
                  + Math.pow(target[1] - sy, 2)
                  + Math.pow(target[2] - sz, 2));

      BeamPronter.prontBeam(
          Vec3.createVectorHelper(-target[0] + sx, target[1] - sy, -target[2] + sz).toVec3d(),
          EnumWaveType.RANDOM,
          EnumBeamType.SOLID,
          0x0051C4,
          0x606060,
          (int) tesla.getWorld().getTotalWorldTime() % 1000 + 1,
          (int) (length * 5),
          0.125F,
          2,
          0.03125F);
    }

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.tesla);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -3, 0);
        GlStateManager.scale(6, 6, 6);
      }

      public void renderCommon() {
        GlStateManager.disableCull();
        bindTexture(ResourceManager.tesla_tex);
        ResourceManager.tesla.renderAll();
        GlStateManager.enableCull();
      }
    };
  }
}
