package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.bomb.TileEntityLandmine;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.world.biome.Biome;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderLandmine extends TileEntitySpecialRenderer<TileEntityLandmine> implements IItemRendererProvider {

  @Override
  public void render(TileEntityLandmine te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.disableCull();

    GlStateManager.rotate(180, 0F, 1F, 0F);

    Block block = te.getWorld().getBlockState(te.getPos()).getBlock();

    if (block == ModBlocks.mine_ap) {
      GL11.glScaled(0.375D, 0.375D, 0.375D);
      GlStateManager.translate(0, -0.0625 * 3.5, 0);
      Biome biome = te.getWorld().getBiome(te.getPos());
      if (te.getWorld().getHeight(te.getPos()).getY() > te.getPos().getY() + 2)
        bindTexture(ResourceManager.mine_ap_stone_tex);
      else if (biome.getEnableSnow()) bindTexture(ResourceManager.mine_ap_snow_tex);
      else if (biome.getDefaultTemperature() >= 1.5F && biome.getRainfall() <= 0.1F)
        bindTexture(ResourceManager.mine_ap_desert_tex);
      else bindTexture(ResourceManager.mine_ap_grass_tex);
      ResourceManager.mine_ap.renderAll();
    }
    if (block == ModBlocks.mine_he) {
      GlStateManager.rotate(-90, 0F, 1F, 0F);
      GlStateManager.shadeModel(GL11.GL_SMOOTH);
      bindTexture(ResourceManager.mine_marelet_tex);
      ResourceManager.mine_marelet.renderAll();
      GlStateManager.shadeModel(GL11.GL_FLAT);
    }
    if (block == ModBlocks.mine_shrap) {
      GL11.glScaled(0.375D, 0.375D, 0.375D);
      GlStateManager.translate(0, -0.0625 * 3.5, 0);
      bindTexture(ResourceManager.mine_shrap_tex);
      ResourceManager.mine_ap.renderAll();
    }
    if (block == ModBlocks.mine_fat) {
      GL11.glScaled(0.25D, 0.25D, 0.25D);
      bindTexture(ResourceManager.mine_fat_tex);
      ResourceManager.mine_fat.renderAll();
    }
    if (block == ModBlocks.mine_naval) {
      GlStateManager.scale(1.0D, 1.0D, 1.0D);
      GlStateManager.translate(0.0D, 0.5D, 0.0D);
      GlStateManager.shadeModel(GL11.GL_SMOOTH);
      bindTexture(ResourceManager.mine_naval_tex);
      ResourceManager.mine_naval.renderAll();
      GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.mine_ap);
  }

  @Override
  public Item[] getItemsForRenderer() {
    return new Item[] {
      Item.getItemFromBlock(ModBlocks.mine_ap),
      Item.getItemFromBlock(ModBlocks.mine_he),
      Item.getItemFromBlock(ModBlocks.mine_shrap),
      Item.getItemFromBlock(ModBlocks.mine_fat),
      Item.getItemFromBlock(ModBlocks.mine_naval)
    };
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    if (item == Item.getItemFromBlock(ModBlocks.mine_ap)) {
      return new ItemRenderBase() {
        public void renderInventory() {
          GlStateManager.scale(8, 8, 8);
        }

        public void renderCommon() {
          GlStateManager.scale(1.25, 1.25, 1.25);
          GlStateManager.rotate(22.5F, 0F, 1F, 0F);
          GlStateManager.disableCull();
          GlStateManager.shadeModel(GL11.GL_SMOOTH);
          bindTexture(ResourceManager.mine_ap_grass_tex);
          ResourceManager.mine_ap.renderAll();
          GlStateManager.shadeModel(GL11.GL_FLAT);
          GlStateManager.enableCull();
        }
      };
    } else if (item == Item.getItemFromBlock(ModBlocks.mine_he)) {
      return new ItemRenderBase() {
        public void renderInventory() {
          GlStateManager.scale(6, 6, 6);
        }

        public void renderNonInv() {
          GlStateManager.translate(0.25, 0.625, 0);
          GlStateManager.rotate(45, 0, 1, 0);
          GlStateManager.rotate(-15, 0, 0, 1);
        }

        public void renderCommon() {
          GlStateManager.scale(4, 4, 4);
          GlStateManager.disableCull();
          GlStateManager.shadeModel(GL11.GL_SMOOTH);
          bindTexture(ResourceManager.mine_marelet_tex);
          ResourceManager.mine_marelet.renderAll();
          GlStateManager.shadeModel(GL11.GL_FLAT);
          GlStateManager.enableCull();
        }
      };
    } else if (item == Item.getItemFromBlock(ModBlocks.mine_shrap)) {
      return new ItemRenderBase() {
        public void renderInventory() {
          GlStateManager.scale(8, 8, 8);
        }

        public void renderCommon() {
          GlStateManager.scale(1.25, 1.25, 1.25);
          bindTexture(ResourceManager.mine_shrap_tex);
          ResourceManager.mine_ap.renderAll();
        }
      };
    } else if(item == Item.getItemFromBlock(ModBlocks.mine_naval)) {
      return new ItemRenderBase() {
        @Override
        public void renderInventory() {
          GlStateManager.translate(0.0D, 2.0D, -1.0D);
          GlStateManager.scale(5.0D, 5.0D, 5.0D);
        }

        @Override
        public void renderCommon() {
          GlStateManager.translate(0.0D, 0.0D, 0.0D);
          GlStateManager.disableCull();
          GlStateManager.shadeModel(GL11.GL_SMOOTH);
          bindTexture(ResourceManager.mine_naval_tex);
          ResourceManager.mine_naval.renderAll();
          GlStateManager.enableCull();
        }

      };
    }

    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -1, 0);
        GlStateManager.scale(7, 7, 7);
      }

      public void renderCommon() {
        GlStateManager.translate(0.25, 0, 0);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.disableCull();
        bindTexture(ResourceManager.mine_fat_tex);
        ResourceManager.mine_fat.renderAll();
        GlStateManager.enableCull();
      }
    };
  }
}
