package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.oil.TileEntityMachineFrackingTower;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderFrackingTower extends TileEntitySpecialRenderer<TileEntityMachineFrackingTower>
    implements IItemRendererProvider {
    public static final ResourceLocation pipe_tex = new ResourceLocation(Tags.MODID, "textures/blocks/pipe_silver.png");

  @Override
  public boolean isGlobalRenderer(TileEntityMachineFrackingTower te) {
    return true;
  }

  @Override
  public void render(
      TileEntityMachineFrackingTower te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {

    GlStateManager.pushMatrix();

    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.enableAlpha();
    GlStateManager.disableCull();
    GlStateManager.rotate(180, 0F, 1F, 0F);

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.fracking_tower_tex);
    ResourceManager.fracking_tower.renderAll();
    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.translate(0, 0.5, 0);

    bindTexture(pipe_tex);
    ResourceManager.pipe_neo.renderPart("pX");
    ResourceManager.pipe_neo.renderPart("nX");
    ResourceManager.pipe_neo.renderPart("pZ");
    ResourceManager.pipe_neo.renderPart("nZ");

    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_fracking_tower);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -4.5, 0);
        GlStateManager.scale(2.5, 2.5, 2.5);
      }

      public void renderCommon() {
        GlStateManager.scale(0.25, 0.25, 0.25);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.fracking_tower_tex);
        ResourceManager.fracking_tower.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
