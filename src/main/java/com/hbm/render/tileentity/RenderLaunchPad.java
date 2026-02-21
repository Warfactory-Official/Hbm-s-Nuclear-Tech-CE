package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.item.ItemRenderMissileGeneric;
import com.hbm.tileentity.bomb.TileEntityLaunchPad;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

@AutoRegister
public class RenderLaunchPad extends TileEntitySpecialRenderer<TileEntityLaunchPad>
    implements IItemRendererProvider {

  public static final float h_1 = 1F;
  public static final float h_2 = 1F;
  public static final float h_3 = 0.8F;
  public static final float w_2 = 1F;

  @Override
  public boolean isGlobalRenderer(@NotNull TileEntityLaunchPad te) {
    return true;
  }

  @Override
  public void render(TileEntityLaunchPad te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();

    switch (te.getBlockMetadata() - BlockDummyable.offset) {
      case 2 -> GlStateManager.rotate(90, 0F, 1F, 0F);
      case 4 -> GlStateManager.rotate(180, 0F, 1F, 0F);
      case 3 -> GlStateManager.rotate(270, 0F, 1F, 0F);
      case 5 -> GlStateManager.rotate(0, 0F, 1F, 0F);
    }

    bindTexture(ResourceManager.missile_pad_tex);
    ResourceManager.missile_pad.renderAll();

    ItemStack toRender = te.toRender;

    if(toRender != null) {
      GlStateManager.translate(0, 1, 0);
      Consumer<TextureManager> renderer = ItemRenderMissileGeneric.renderers.get(new RecipesCommon.ComparableStack(toRender).makeSingular());
      if(renderer != null) renderer.accept(this.rendererDispatcher.renderEngine);
    }

    GlStateManager.enableCull();

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.launch_pad);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -1, 0);
        GlStateManager.scale(3, 3, 3);
      }

      public void renderCommon() {
        bindTexture(ResourceManager.missile_pad_tex);
        ResourceManager.missile_pad.renderAll();
      }
    };
  }
}
