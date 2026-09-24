package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IDoor;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityBlastDoor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
@AutoRegister
public class RenderBlastDoor extends TileEntitySpecialRenderer<TileEntityBlastDoor>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityBlastDoor tileEntity,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.enableLighting();
    GlStateManager.disableCull();
    GlStateManager.rotate(180, 0F, 1F, 0F);

    double timer;

    if (tileEntity.getBlockMetadata() == 2 || tileEntity.getBlockMetadata() == 3)
      GlStateManager.rotate(90, 0F, 1F, 0F);

    if (tileEntity.state == IDoor.DoorState.CLOSED) timer = getAnimationFromSysTime(5000);
    else if (tileEntity.state == IDoor.DoorState.OPEN) timer = 0;
    else if (tileEntity.state == IDoor.DoorState.OPENING)
      timer = getAnimationFromSysTime(tileEntity.sysTime + 5000 - System.currentTimeMillis());
    else timer = getAnimationFromSysTime(System.currentTimeMillis() - tileEntity.sysTime);

    bindTexture(ResourceManager.blast_door_base_tex);
    ResourceManager.blast_door_base.renderAll();

    GlStateManager.translate(0, 3, 0);
    bindTexture(ResourceManager.blast_door_block_tex);
    ResourceManager.blast_door_block.renderAll();

    GlStateManager.translate(0, -timer, 0);

    GlStateManager.translate(0, 2, 0);
    bindTexture(ResourceManager.blast_door_tooth_tex);
    ResourceManager.blast_door_tooth.renderAll();

    if (timer > 1D) {
      bindTexture(ResourceManager.blast_door_slider_tex);
      ResourceManager.blast_door_slider.renderAll();
    }
    if (timer > 2D) {
      GlStateManager.translate(0, 1, 0);
      bindTexture(ResourceManager.blast_door_slider_tex);
      ResourceManager.blast_door_slider.renderAll();
    }
    if (timer > 3D) {
      GlStateManager.translate(0, 1, 0);
      bindTexture(ResourceManager.blast_door_slider_tex);
      ResourceManager.blast_door_slider.renderAll();
    }
    if (timer > 4D) {
      GlStateManager.translate(0, 1, 0);
      bindTexture(ResourceManager.blast_door_slider_tex);
      ResourceManager.blast_door_slider.renderAll();
    }

    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.blast_door);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -3, 0);
        GlStateManager.scale(3, 3, 3);
      }

      public void renderCommon() {
        bindTexture(ResourceManager.blast_door_base_tex);
        ResourceManager.blast_door_base.renderAll();
        bindTexture(ResourceManager.blast_door_tooth_tex);
        ResourceManager.blast_door_tooth.renderAll();
        bindTexture(ResourceManager.blast_door_slider_tex);
        ResourceManager.blast_door_slider.renderAll();
        bindTexture(ResourceManager.blast_door_block_tex);
        ResourceManager.blast_door_block.renderAll();
      }
    };
  }

  private static double getAnimationFromSysTime(long time) {

    double duration = 5000D;
    double extend = 5.0D;

    return Math.max(Math.min(time, duration) / duration * extend, 0.0D);
  }
}
