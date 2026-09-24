package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineEPress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.ForgeHooksClient;
@AutoRegister
public class RenderEPress extends TileEntitySpecialRenderer<TileEntityMachineEPress>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityMachineEPress tileentity,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.rotate(180, 0F, 1F, 0F);

    switch (tileentity.getBlockMetadata() - BlockDummyable.offset) {
      case 2:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
    }

    this.bindTexture(ResourceManager.epress_body_tex);

    ResourceManager.epress_body.renderAll();

    GlStateManager.popMatrix();
    GlStateManager.enableAlpha();

    renderTileEntityAt2(tileentity, x, y, z, partialTicks);
  }

  public void renderTileEntityAt2(TileEntity tileentity, double x, double y, double z, float f) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y + 1 + 1 - 0.125, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.rotate(180, 0F, 1F, 0F);

    switch (tileentity.getBlockMetadata() - BlockDummyable.offset) {
      case 2:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
    }

    TileEntityMachineEPress press = (TileEntityMachineEPress) tileentity;
    float f1 = press.progress * (1 - 0.125F) / TileEntityMachineEPress.maxProgress;
    GlStateManager.translate(0, -f1, 0);

    this.bindTexture(ResourceManager.epress_head_tex);

    ResourceManager.epress_head.renderAll();

    GlStateManager.popMatrix();

    renderTileEntityAt3(tileentity, x, y, z, f);
  }

  public void renderTileEntityAt3(TileEntity tileentity, double x, double y, double z, float f) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y + 1, z + 0.5);
    GlStateManager.enableLighting();
    GlStateManager.rotate(180, 0F, 1F, 0F);

    switch (tileentity.getBlockMetadata() - BlockDummyable.offset) {
      case 2:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
    }

    GlStateManager.rotate(90, 0F, 1F, 0F);
    GlStateManager.rotate(-90, 1F, 0F, 0F);
    GlStateManager.translate(1.0F, 1.0F - 0.0625F * 165 / 100, 0.0F);
    GlStateManager.translate(-1, -1.15F, 0);

    TileEntityMachineEPress press = (TileEntityMachineEPress) tileentity;
    ItemStack stack = press.syncStack;

    if (stack != null && !(stack.getItem() instanceof ItemBlock)) {
      IBakedModel model =
          Minecraft.getMinecraft()
              .getRenderItem()
              .getItemModelWithOverrides(stack, tileentity.getWorld(), null);
      model = ForgeHooksClient.handleCameraTransforms(model, TransformType.FIXED, false);
      Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      GlStateManager.translate(0.0F, 0.125F, 0.0F);
      GlStateManager.rotate(180, 0F, 1F, 0F);
      GlStateManager.scale(0.5F, 0.5F, 0.5F);
      Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
    }

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_epress);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -4, 0);
        GlStateManager.scale(4.5, 4.5, 4.5);
      }

      public void renderCommon() {
        bindTexture(ResourceManager.epress_body_tex);
        ResourceManager.epress_body.renderAll();
        GlStateManager.translate(0, 1.5, 0);
        bindTexture(ResourceManager.epress_head_tex);
        ResourceManager.epress_head.renderAll();
      }
    };
  }
}
