package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.misc.MissileMultipart;
import com.hbm.render.misc.MissilePart;
import com.hbm.render.misc.MissilePronter;
import com.hbm.tileentity.machine.TileEntityMachineMissileAssembly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
@AutoRegister
public class RenderMissileAssembly
    extends TileEntitySpecialRenderer<TileEntityMachineMissileAssembly>
    implements IItemRendererProvider {
  @Override
  public void render(
      TileEntityMachineMissileAssembly te,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();

    GlStateManager.translate((float) x + 0.5F, (float) y, (float) z + 0.5F);
    GlStateManager.disableCull();

    switch (te.getBlockMetadata()) {
      case 2:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
    }

    bindTexture(ResourceManager.missile_assembly_tex);
    ResourceManager.missile_assembly.renderAll();

    MissileMultipart missile = MissileMultipart.loadFromStruct(te.load);

    if (missile != null) {

      if (!te.inventory.getStackInSlot(1).isEmpty())
        missile.warhead = MissilePart.getPart(te.inventory.getStackInSlot(1).getItem());

      if (!te.inventory.getStackInSlot(2).isEmpty())
        missile.fuselage = MissilePart.getPart(te.inventory.getStackInSlot(2).getItem());

      if (!te.inventory.getStackInSlot(3).isEmpty())
        missile.fins = MissilePart.getPart(te.inventory.getStackInSlot(3).getItem());

      if (!te.inventory.getStackInSlot(4).isEmpty())
        missile.thruster = MissilePart.getPart(te.inventory.getStackInSlot(4).getItem());

      int range = (int) (missile.getHeight() / 2 - 1);

      int step = 1;

      if (range >= 2) step = 2;

      for (int i = -range; i <= range; i += step) {

        if (i != 0) {
          GlStateManager.translate(i, 0F, 0F);
          bindTexture(ResourceManager.strut_tex);
          ResourceManager.strut.renderAll();
          GlStateManager.translate(-i, 0F, 0F);
        }
      }

      GlStateManager.translate(0F, 1.5F, 0F);
      GlStateManager.rotate(180, 0F, 0F, 1F);

      GlStateManager.translate(-missile.getHeight() / 2, 0, 0);
      // GL11.glScaled(scale, scale, scale);

      GlStateManager.rotate(-90, 1, 0, 0);
      GlStateManager.rotate(-90, 0, 0, 1);
      GlStateManager.scale(1, 1, 1);

      GlStateManager.enableCull();
      MissilePronter.prontMissile(missile, Minecraft.getMinecraft().getTextureManager());
    }

    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }

  @Override
  public Item getItemForRenderer() {
    return Item.getItemFromBlock(ModBlocks.machine_missile_assembly);
  }

  @Override
  public ItemRenderBase getRenderer(Item item) {
    return new ItemRenderBase() {
      public void renderInventory() {
        GlStateManager.translate(0, -2.5, 0);
        GlStateManager.scale(10, 10, 10);
      }

      public void renderCommon() {
        GlStateManager.disableCull();
        bindTexture(ResourceManager.missile_assembly_tex);
        ResourceManager.missile_assembly.renderAll();
        GlStateManager.enableCull();
      }
    };
  }
}
