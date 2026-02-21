package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityRefueler;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.DoubleBuffer;
@AutoRegister
public class RenderRefueler extends TileEntitySpecialRenderer<TileEntityRefueler> implements IItemRendererProvider {

    private static DoubleBuffer clip = null;

    @Override
    public void render(TileEntityRefueler tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
        {
            GlStateManager.translate(x + 0.5, y, z + 0.5);
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.rotate(90F, 0F, 1F, 0F);
            switch (tile.getBlockMetadata()) {
                case 4 -> GlStateManager.rotate(90F, 0F, 1F, 0F);
                case 3 -> GlStateManager.rotate(180F, 0F, 1F, 0F);
                case 5 -> GlStateManager.rotate(270F, 0F, 1F, 0F);
                case 2 -> GlStateManager.rotate(0F, 0F, 1F, 0F);
            }

            GlStateManager.shadeModel(GL11.GL_SMOOTH);

            this.bindTexture(ResourceManager.refueler_tex);
            ResourceManager.refueler.renderPart("Fueler");

            if (clip == null) {
                clip = GLAllocation.createDirectByteBuffer(8 * 4).asDoubleBuffer();
                clip.put(new double[] { 0, 1, 0, -0.125 });
                clip.rewind();
            }

            GL11.glEnable(GL11.GL_CLIP_PLANE0);
            GL11.glClipPlane(GL11.GL_CLIP_PLANE0, clip);

            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

            double fillLevel = tile.prevFillLevel + (tile.fillLevel - tile.prevFillLevel) * partialTicks;
            GlStateManager.translate(0, (1 - fillLevel) * -0.625, 0);

            Color color = new Color(tile.tank.getTankType().getColor());
            GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 0.75F);
            ResourceManager.refueler.renderPart("Fluid");
            GlStateManager.color(1F, 1F, 1F, 1F);

            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();

            GL11.glDisable(GL11.GL_CLIP_PLANE0);

            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.refueler);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -3, 0);
                GlStateManager.scale(6, 6, 6);
            }
            public void renderCommon() {
                GlStateManager.scale(2, 2, 2);
                GlStateManager.translate(0.5, 0, 0);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                RenderRefueler.this.bindTexture(ResourceManager.refueler_tex);
                ResourceManager.refueler.renderPart("Fueler");
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
