package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.RenderSparks;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineReactorBreeding;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderBreeder extends TileEntitySpecialRenderer<TileEntityMachineReactorBreeding>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityMachineReactorBreeding breeder,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.disableCull();

    GlStateManager.rotate(90, 0F, 1F, 0F);

    switch (breeder.getBlockMetadata() - BlockDummyable.offset) {
      case 2:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
    }

    if (breeder.progress > 0.0F)
      for (int i = 0; i < 3; i++) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) (Math.PI * i), 0F, 1F, 0F);
        RenderSparks.renderSpark(
            (int) ((System.currentTimeMillis() % 10000) / 100 + i),
            0,
            1.875,
            0,
            0.15F,
            3,
            4,
            0x00ff00,
            0xffffff);
        GlStateManager.popMatrix();
      }

    GL11.glScaled(0.5, 0.5, 0.5);

    bindTexture(ResourceManager.breeder_tex);

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    ResourceManager.breeder.renderAll();
    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.enableCull();

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_reactor_breeding);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -4, 0);
        GlStateManager.scale(4.5, 4.5, 4.5);
      }

      public void renderCommon() {
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableCull();
        bindTexture(ResourceManager.breeder_tex);
        ResourceManager.breeder.renderAll();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
