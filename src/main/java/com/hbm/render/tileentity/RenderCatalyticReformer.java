package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.HorsePronter;
import com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticReformer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
@AutoRegister
public class RenderCatalyticReformer
    extends TileEntitySpecialRenderer<TileEntityMachineCatalyticReformer>
    implements IItemRendererProvider {

  private static ResourceLocation extra =
      new ResourceLocation(Tags.MODID, "textures/models/horse/dyx.png");

  @Override
  public void render(
      TileEntityMachineCatalyticReformer tile,
      double x,
      double y,
      double z,
      float interp,
      int destroyStage,
      float alpha) {

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5, y, z + 0.5);
    GlStateManager.enableLighting();
    GlStateManager.disableCull();

    switch (tile.getBlockMetadata() - BlockDummyable.offset) {
      case 2:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
    }

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.catalytic_reformer_tex);
    ResourceManager.catalytic_reformer.renderAll();
    GlStateManager.shadeModel(GL11.GL_FLAT);

    if (MainRegistry.polaroidID == 11) {
      /// rapidly spinning dicks ///
      GL11.glEnable(GL12.GL_RESCALE_NORMAL);
      GlStateManager.translate(-1.125, 1.375, 1);
      double s = 0.125D;
      GL11.glScaled(s, s, s);
      GL11.glRotated(System.currentTimeMillis() / 5D % 360D, 0, -1, 0);
      GlStateManager.translate(0, 0.1, -0.5);

      this.bindTexture(extra);
      HorsePronter.reset();
      double r = 60;
      HorsePronter.pose(HorsePronter.id_body, 0, -r, 0);
      HorsePronter.pose(HorsePronter.id_tail, 0, 45, 90);
      HorsePronter.pose(HorsePronter.id_lbl, 0, -90 + r, 35);
      HorsePronter.pose(HorsePronter.id_rbl, 0, -90 + r, -35);
      HorsePronter.pose(HorsePronter.id_lfl, 0, r - 10, 5);
      HorsePronter.pose(HorsePronter.id_rfl, 0, r - 10, -5);
      HorsePronter.pose(HorsePronter.id_head, 0, r, 0);
      HorsePronter.enableHorn();
      HorsePronter.enableWings();
      HorsePronter.pront();
    }

    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_catalytic_reformer);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -3, 0);
        GlStateManager.scale(3.5, 3.5, 3.5);
      }

      public void renderCommon() {
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.catalytic_reformer_tex);
        ResourceManager.catalytic_reformer.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
