package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.network.TileEntityConnectorSuper;
import com.hbm.tileentity.network.energy.TileEntityPylonBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister(tileentity = TileEntityConnectorSuper.class)
public class RenderConnectorSuper extends RenderPylonBase implements IItemRendererProvider {

    @Override
    public void render(TileEntityPylonBase tile, double x, double y, double z, float f, int destroyStage, float alpha) {
        if (!(tile instanceof TileEntityConnectorSuper te)) return;
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

        bindTexture(ResourceManager.connector_super_tex);
        ResourceManager.connector_super.renderAll();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        this.renderLinesGeneric(tile, x, y, z);
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.red_connector_super);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -5, 0);
                double scale = 7;
                GlStateManager.scale(scale, scale, scale);
            }
            public void renderCommon() {
                GlStateManager.scale(2, 2, 2);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.connector_super_tex);
                ResourceManager.connector_super.renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }};
    }
}
