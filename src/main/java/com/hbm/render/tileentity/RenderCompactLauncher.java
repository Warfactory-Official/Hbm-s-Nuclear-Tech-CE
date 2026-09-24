package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.MissilePronter;
import com.hbm.tileentity.bomb.TileEntityCompactLauncher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
@AutoRegister
public class RenderCompactLauncher extends TileEntitySpecialRenderer<TileEntityCompactLauncher>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityCompactLauncher launcher,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate((float) x + 0.5F, (float) y, (float) z + 0.5F);

    GlStateManager.enableCull();

    bindTexture(ResourceManager.compact_launcher_tex);
    ResourceManager.compact_launcher.renderAll();

    GlStateManager.translate(0F, 1.0625F, 0F);

    /// DRAW MISSILE START
    GlStateManager.pushMatrix();
    if (launcher.load != null && launcher.clearingTimer == 0) {
      // ItemStack custom = launcher.getStackInSlot(0);

      // missile = ItemCustomMissile.getMultipart(custom);

      MissilePronter.prontMissile(
          launcher.load.multipart(), Minecraft.getMinecraft().getTextureManager());
      //
    }

    GlStateManager.popMatrix();
    /// DRAW MISSILE END

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.compact_launcher);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -4, 0);
        GlStateManager.scale(3.5, 3.5, 3.5);
      }

      public void renderCommon() {
        GlStateManager.scale(0.5, 0.5, 0.5);
        bindTexture(ResourceManager.compact_launcher_tex);
        ResourceManager.compact_launcher.renderAll();
      }
    };
  }
}
