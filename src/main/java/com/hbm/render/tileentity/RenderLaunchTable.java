package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.ItemMissile;
import com.hbm.items.weapon.ItemMissile.PartSize;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.MissileMultipart;
import com.hbm.render.misc.MissilePronter;
import com.hbm.tileentity.bomb.TileEntityLaunchTable;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
@AutoRegister
public class RenderLaunchTable extends TileEntitySpecialRenderer<TileEntityLaunchTable>
    implements IItemRendererProvider {

  @Override
  public boolean isGlobalRenderer(TileEntityLaunchTable te) {
    return true;
  }

  @Override
  public void render(
      TileEntityLaunchTable launcher,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();

    GlStateManager.translate((float) x + 0.5F, (float) y, (float) z + 0.5F);
    GlStateManager.enableCull();

    switch (launcher.getWorld().getBlockState(launcher.getPos()).getValue(BlockHorizontal.FACING)) {
      case NORTH:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case SOUTH:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case EAST:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case WEST:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      default:
        break;
    }

    bindTexture(ResourceManager.launch_table_base_tex);
    ResourceManager.launch_table_base.renderAll();

    if (launcher.padSize == PartSize.SIZE_10 || launcher.padSize == PartSize.SIZE_15) {
      bindTexture(ResourceManager.launch_table_small_pad_tex);
      ResourceManager.launch_table_small_pad.renderAll();
    }
    if (launcher.padSize == PartSize.SIZE_20) {
      bindTexture(ResourceManager.launch_table_large_pad_tex);
      ResourceManager.launch_table_large_pad.renderAll();
    }

    GlStateManager.pushMatrix();

    if (launcher.load != null) {
      MissileMultipart mp = MissileMultipart.loadFromStruct(launcher.load);

      if (mp != null && mp.fuselage != null) launcher.height = (int) mp.getHeight();
    }

    int height = (int) (launcher.height * 0.75);
    ResourceLocation base = ResourceManager.launch_table_large_scaffold_base_tex;
    ResourceLocation connector = ResourceManager.launch_table_large_scaffold_connector_tex;
    IModelCustom baseM = ResourceManager.launch_table_large_scaffold_base;
    IModelCustom connectorM = ResourceManager.launch_table_large_scaffold_connector;
    IModelCustom emptyM = ResourceManager.launch_table_large_scaffold_empty;

    if (launcher.padSize == PartSize.SIZE_10) {
      base = ResourceManager.launch_table_small_scaffold_base_tex;
      connector = ResourceManager.launch_table_small_scaffold_connector_tex;
      baseM = ResourceManager.launch_table_small_scaffold_base;
      connectorM = ResourceManager.launch_table_small_scaffold_connector;
      emptyM = ResourceManager.launch_table_small_scaffold_empty;
      GlStateManager.translate(0F, 0F, -1F);
    }
    GlStateManager.translate(0F, 1F, 3.5F);
    for (int i = 0; i < launcher.height + 1; i++) {

      if (i < height) {
        bindTexture(base);
        baseM.renderAll();
      } else if (i > height) {
        bindTexture(base);
        emptyM.renderAll();
      } else {

        if (launcher.load != null
            && launcher.load.fuselage != null
            && ((ItemMissile) launcher.load.fuselage).top == launcher.padSize) {
          bindTexture(connector);
          connectorM.renderAll();
        } else {
          bindTexture(base);
          baseM.renderAll();
        }
      }

      GlStateManager.translate(0F, 1F, 0F);
    }
    GlStateManager.popMatrix();

    GlStateManager.translate(0F, 2.0625F, 0F);

    /// DRAW MISSILE START
    GlStateManager.pushMatrix();

    if (launcher.load != null
        && launcher.load.fuselage != null
        && launcher.load.fuselage.top == launcher.padSize
        && launcher.clearingTimer == 0)
      MissilePronter.prontMissile(
          MissileMultipart.loadFromStruct(launcher.load),
          Minecraft.getMinecraft().getTextureManager());

    GlStateManager.popMatrix();
    /// DRAW MISSILE END

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.launch_table);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -2, 0);
        GlStateManager.scale(2.5, 2.5, 2.5);
      }

      public void renderCommon() {
        GlStateManager.scale(0.5, 0.5, 0.5);
        bindTexture(ResourceManager.launch_table_base_tex);
        ResourceManager.launch_table_base.renderAll();
        bindTexture(ResourceManager.launch_table_small_pad_tex);
        ResourceManager.launch_table_small_pad.renderAll();
        GlStateManager.translate(0F, 0F, 2.5F);
        for (int i = 0; i < 8; i++) {
          GlStateManager.translate(0F, 1F, 0.F);
          if (i < 6) {
            bindTexture(ResourceManager.launch_table_small_scaffold_base_tex);
            ResourceManager.launch_table_small_scaffold_base.renderAll();
          }
          if (i == 6) {
            bindTexture(ResourceManager.launch_table_small_scaffold_connector_tex);
            ResourceManager.launch_table_small_scaffold_connector.renderAll();
          }
          if (i > 6) {
            bindTexture(ResourceManager.launch_table_small_scaffold_base_tex);
            ResourceManager.launch_table_small_scaffold_empty.renderAll();
          }
        }
      }
    };
  }
}
