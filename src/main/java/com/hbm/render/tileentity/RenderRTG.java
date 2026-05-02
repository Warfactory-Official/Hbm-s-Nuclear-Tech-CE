package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.Library;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineMiniRTG;
import com.hbm.tileentity.machine.TileEntityMachineRTG;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

@AutoRegister(tileentity = TileEntityMachineRTG.class)
@AutoRegister(tileentity = TileEntityMachineMiniRTG.class)
public class RenderRTG extends TileEntitySpecialRenderer<TileEntity> {

    @Override
    public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.rotate(180, 0F, 1F, 0F);

        if (te.getBlockType() == ModBlocks.machine_rtg_grey) {
            bindTexture(ResourceManager.rtg_tex);
        } else if (te.getBlockType() == ModBlocks.machine_powerrtg) {
            bindTexture(ResourceManager.rtg_polonium_tex);
        } else {
            bindTexture(ResourceManager.rtg_cell_tex);
        }

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
}
