package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.Library;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineRTG;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;

@AutoRegister(tileentity = TileEntityMachineRTG.class)
public class RenderRTG extends TileEntitySpecialRenderer<TileEntityMachineRTG> implements IItemRendererProvider {

    @Override
    public void render(TileEntityMachineRTG te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.rotate(180, 0F, 1F, 0F);

        bindTexture(ResourceManager.rtg_tex);

        ResourceManager.rtg.renderPart("Gen");

        if (Library.canConnect(te.getWorld(), te.getPos().add(1, 0, 0), Library.POS_X))
            ResourceManager.rtg_connector.renderAll();

        if (Library.canConnect(te.getWorld(), te.getPos().add(-1, 0, 0), Library.NEG_X)) {
            GlStateManager.rotate(180, 0F, 1F, 0F);
            ResourceManager.rtg_connector.renderAll();
            GlStateManager.rotate(-180, 0F, 1F, 0F);
        }

        if (Library.canConnect(te.getWorld(), te.getPos().add(0, 0, -1), Library.NEG_Z)) {
            GlStateManager.rotate(90, 0F, 1F, 0F);
            ResourceManager.rtg_connector.renderAll();
            GlStateManager.rotate(-90, 0F, 1F, 0F);
        }

        if (Library.canConnect(te.getWorld(), te.getPos().add(0, 0, 1), Library.POS_Z)) {
            GlStateManager.rotate(-90, 0F, 1F, 0F);
            ResourceManager.rtg_connector.renderAll();
            GlStateManager.rotate(90, 0F, 1F, 0F);
        }

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_rtg_grey);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.translate(0, -4, 0);
                GlStateManager.scale(8, 8, 8);
            }

            @Override
            public void renderCommon() {
                GlStateManager.rotate(180, 0F, 1F, 0F);
                GlStateManager.disableCull();
                bindTexture(ResourceManager.rtg_tex);
                ResourceManager.rtg.renderPart("Gen");
                GlStateManager.enableCull();
            }
        };
    }
}
