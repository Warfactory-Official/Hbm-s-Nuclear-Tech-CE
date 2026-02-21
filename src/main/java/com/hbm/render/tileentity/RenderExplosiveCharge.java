package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.*;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.bomb.TileEntityCharge;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

@AutoRegister
public class RenderExplosiveCharge extends TileEntitySpecialRenderer<TileEntityCharge>
    implements IItemRendererProvider {

  // semtex, c4
  private static final IModelCustom MODEL_C4 = ResourceManager.charge_c4;
  // dynamite, miner
  private static final IModelCustom MODEL_DYN = ResourceManager.charge_dynamite;

  private static final ResourceLocation TEX_C4 =
      new ResourceLocation(Tags.MODID, "textures/blocks/charge_c4.png");
  private static final ResourceLocation TEX_SEM =
      new ResourceLocation(Tags.MODID, "textures/blocks/charge_semtex.png");
  private static final ResourceLocation TEX_DYN =
      new ResourceLocation(Tags.MODID, "textures/blocks/charge_dynamite.png");
  private static final ResourceLocation TEX_MIN =
      new ResourceLocation(Tags.MODID, "textures/blocks/charge_miner.png");

  @Override
  public void render(
      TileEntityCharge te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {

    IBlockState state = te.hasWorld() ? te.getWorld().getBlockState(te.getPos()) : null;
    if (state == null || !(state.getBlock() instanceof BlockChargeBase)) return;

    boolean c4Model =
        state.getBlock() instanceof BlockChargeC4 || state.getBlock() instanceof BlockChargeSemtex;
    IModelCustom model = c4Model ? MODEL_C4 : MODEL_DYN;
    ResourceLocation texture;
    if (state.getBlock() instanceof BlockChargeC4) texture = TEX_C4;
    else if (state.getBlock() instanceof BlockChargeDynamite) texture = TEX_DYN;
    else if (state.getBlock() instanceof BlockChargeMiner) texture = TEX_MIN;
    else texture = TEX_SEM;
    EnumFacing facing = state.getValue(BlockChargeBase.FACING);

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
    switch (facing) {
      case DOWN -> GlStateManager.rotate(180F, 0F, 0F, 1F);
      case UP -> {}
      case NORTH -> {
        GlStateManager.rotate(90F, 0F, 1F, 0F);
        GlStateManager.rotate(-90F, 0F, 0F, 1F);
      }
      case SOUTH -> {
        GlStateManager.rotate(-90F, 0F, 1F, 0F);
        GlStateManager.rotate(-90F, 0F, 0F, 1F);
      }
      case WEST -> {
        GlStateManager.rotate(180F, 0F, 1F, 0F);
        GlStateManager.rotate(-90F, 0F, 0F, 1F);
      }
      case EAST -> GlStateManager.rotate(-90F, 0F, 0F, 1F);
    }
    GlStateManager.enableRescaleNormal();
    GlStateManager.disableCull();
    bindTexture(texture);
    model.renderAll();
    GlStateManager.enableCull();
    GlStateManager.disableRescaleNormal();
    renderTimer(te);

    GlStateManager.popMatrix();
  }

  private void renderTimer(TileEntityCharge te) {
    String text = te.getMinutes() + ":" + te.getSeconds();

    GlStateManager.pushMatrix();
    GlStateManager.translate(-0.05F, 0.315F - 0.5F, 0.15F);
    float scale = 0.0125F;
    GlStateManager.scale(scale, -scale, scale);
    GlStateManager.rotate(90F, 0F, 1F, 0F);
    GlStateManager.rotate(90F, 1F, 0F, 0F);
    GlStateManager.disableLighting();
    GlStateManager.depthMask(false);
    this.getFontRenderer().drawString(text, 0, 0, 0x00FF00, false);
    GlStateManager.depthMask(true);
    GlStateManager.enableLighting();

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return null;
  }

  @Override
  public Item[] getItemsForRenderer() {
    return new Item[] {
      Item.getItemFromBlock(ModBlocks.charge_c4),
      Item.getItemFromBlock(ModBlocks.charge_dynamite),
      Item.getItemFromBlock(ModBlocks.charge_semtex),
      Item.getItemFromBlock(ModBlocks.charge_miner)
    };
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      @Override
      public void renderInventory() {
        GlStateManager.translate(0.0F, 2.0F, -3.0F);
        GlStateManager.scale(10.0F, 10.0F, 10.0F);
      }

      @Override
      public void renderNonInv() {
        GlStateManager.rotate(180F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(3.0F, 0.0F, 0.0F);
        GlStateManager.scale(4.0F, 4.0F, 4.0F);
      }

      @Override
      public void renderGround() {
        GlStateManager.scale(5.0F, 5.0F, 5.0F);
      }

      @Override
      public void renderCommon() {
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.disableCull();
        boolean c4Model =
            item == Item.getItemFromBlock(ModBlocks.charge_c4)
                || item == Item.getItemFromBlock(ModBlocks.charge_semtex);
        IModelCustom model = c4Model ? MODEL_C4 : MODEL_DYN;
        ResourceLocation texture;
        if (item == Item.getItemFromBlock(ModBlocks.charge_c4)) texture = TEX_C4;
        else if (item == Item.getItemFromBlock(ModBlocks.charge_dynamite)) texture = TEX_DYN;
        else if (item == Item.getItemFromBlock(ModBlocks.charge_miner)) texture = TEX_MIN;
        else texture = TEX_SEM;
        bindTexture(texture);
        model.renderAll();

        GlStateManager.enableCull();
      }
    };
  }
}
