package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityChungus;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderChungus extends TileEntitySpecialRenderer<TileEntityChungus>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityChungus turbine,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();

    GlStateManager.rotate(90, 0F, 1F, 0F);

    switch (turbine.getBlockMetadata() - BlockDummyable.offset) {
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

    GlStateManager.translate(0, 0, -3);

    bindTexture(ResourceManager.chungus_tex);

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    ResourceManager.chungus.renderPart("Body");

    GlStateManager.pushMatrix();
    GlStateManager.translate(0, 0, 4.5);
    int rot = 0;
    FluidType type = turbine.tanks[0].getTankType();
    if (type == Fluids.HOTSTEAM) {
      rot = 1;
    } else if (type == Fluids.SUPERHOTSTEAM) {
      rot = 2;
    } else if (type == Fluids.ULTRAHOTSTEAM) {
      rot = 3;
    }
    GlStateManager.rotate(15 - rot * 10, 1, 0, 0);
    GlStateManager.translate(0, 0, -4.5);
    ResourceManager.chungus.renderPart("Lever");
    GlStateManager.popMatrix();

    GlStateManager.translate(0, 2.5, 0);
    GlStateManager.rotate(
        turbine.lastRotor + (turbine.rotor - turbine.lastRotor) * partialTicks, 0, 0, -1);
    GlStateManager.translate(0, -2.5, 0);

    ResourceManager.chungus.renderPart("Blades");

    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_chungus);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0.5, 0, 0);
        GlStateManager.scale(2.5, 2.5, 2.5);
      }

      public void renderCommon() {
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.rotate(90, 0, 1, 0);
        bindTexture(ResourceManager.chungus_tex);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.chungus.renderPart("Body");
        ResourceManager.chungus.renderPart("Lever");
        ResourceManager.chungus.renderPart("Blades");
        GlStateManager.shadeModel(GL11.GL_FLAT);
      }
    };
  }
}
