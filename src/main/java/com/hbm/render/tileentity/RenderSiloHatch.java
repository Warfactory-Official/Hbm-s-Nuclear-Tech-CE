package com.hbm.render.tileentity;

import com.hbm.animloader.AnimationWrapper;
import com.hbm.animloader.AnimationWrapper.EndResult;
import com.hbm.animloader.AnimationWrapper.EndType;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IDoor;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntitySiloHatch;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderSiloHatch extends TileEntitySpecialRenderer<TileEntitySiloHatch>
    implements IItemRendererProvider {

  @Override
  public boolean isGlobalRenderer(TileEntitySiloHatch te) {
    return true;
  }

  @Override
  public void render(
      TileEntitySiloHatch te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5, y + 0.595, z + 0.5);
    switch (te.getBlockMetadata() - 2) {
      case 0:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case 1:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 2:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
    }
    GlStateManager.translate(3, 0, 0);
    GlStateManager.enableLighting();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.hatch_tex);

    long time = System.currentTimeMillis();
    long startTime = te.state.isMovingState() ? te.sysTime : time;
    boolean reverse = te.state == IDoor.DoorState.OPEN || te.state == IDoor.DoorState.CLOSING;
    AnimationWrapper w = new AnimationWrapper(startTime, ResourceManager.silo_hatch_open);
    if (reverse) {
      w.reverse();
    }
    w.onEnd(new EndResult(EndType.STAY, null));
    bindTexture(ResourceManager.hatch_tex);
    ResourceManager.silo_hatch_drillgon.controller.setAnim(w);
    ResourceManager.silo_hatch_drillgon.renderAnimated(time);

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.silo_hatch_drillgon);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      @Override
      public void renderInventory() {
        GlStateManager.translate(15, -10, 10);
        GlStateManager.scale(2.5, 2.5, 2.5);
      }

      @Override
      public void renderCommon() {
        GlStateManager.translate(0.5F, 2, -2);
        GlStateManager.rotate(-120, 0, 1, 0);
        bindTexture(ResourceManager.hatch_tex);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.silo_hatch_drillgon.render();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
