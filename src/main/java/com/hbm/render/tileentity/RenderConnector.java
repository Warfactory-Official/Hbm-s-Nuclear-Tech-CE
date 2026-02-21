package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.network.TileEntityConnector;
import com.hbm.tileentity.network.energy.TileEntityPylonBase;
import net.minecraft.client.renderer.GlStateManager;

@AutoRegister(tileentity = TileEntityConnector.class)
public class RenderConnector extends RenderPylonBase {

    @Override
    public void render(TileEntityPylonBase tile, double x, double y, double z, float f, int destroyStage, float alpha) {
        if (!(tile instanceof TileEntityConnector te)) return;
        GlStateManager.enableLighting();

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);

        switch (te.getBlockMetadata()) {
            case 0:
                GlStateManager.rotate(180, 1, 0, 0);
                break;
            case 1:
                break;
            case 2:
                GlStateManager.rotate(90, 1, 0, 0);
                GlStateManager.rotate(180, 0, 0, 1);
                break;
            case 3:
                GlStateManager.rotate(90, 1, 0, 0);
                break;
            case 4:
                GlStateManager.rotate(90, 1, 0, 0);
                GlStateManager.rotate(90, 0, 0, 1);
                break;
            case 5:
                GlStateManager.rotate(90, 1, 0, 0);
                GlStateManager.rotate(270, 0, 0, 1);
                break;
        }

        GlStateManager.translate(0, -0.5F, 0);

        bindTexture(ResourceManager.connector_tex);
        ResourceManager.connector.renderAll();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        this.renderLinesGeneric(tile, x, y, z);
        GlStateManager.popMatrix();
    }
}
