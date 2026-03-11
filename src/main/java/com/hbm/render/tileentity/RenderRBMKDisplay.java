package com.hbm.render.tileentity;

import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKDisplay;
import com.hbm.tileentity.machine.rbmk.RBMKColumn;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
@AutoRegister
public class RenderRBMKDisplay extends TileEntitySpecialRenderer<TileEntityRBMKDisplay> {

    @Override
    public void render(TileEntityRBMKDisplay te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();

        switch(te.getBlockMetadata()) {
            case 2: GlStateManager.rotate(90, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(180, 0F, 1F, 0F); break;
            case 3: GlStateManager.rotate(270, 0F, 1F, 0F); break;
            case 5: GlStateManager.rotate(0, 0F, 1F, 0F); break;
        }

        GlStateManager.translate(0, 0.5, 0);
        GlStateManager.scale(1, 8D / 7D, 8D / 7D);
        GlStateManager.translate(0, -0.5, 0);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        GlStateManager.disableTexture2D();
        GlStateManager.glNormal3f(1.0F, 0.0F, 0.0F);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for(int i = 0; i < te.columns.length; i++) {

            RBMKColumn col = te.columns[i];

            if(col == null) continue;

            double kx = 0.28125D;
            double ky = -((double) i / 7) * 0.125 + 0.875;
            double kz = -(i % 7) * 0.125 + 0.125D * 3;

            float r = 1.0F, g = 1.0F, b = 1.0F;

            if(col instanceof RBMKColumn.ControlColumn && ((RBMKColumn.ControlColumn) col).color >= 0) {
                short colorType = ((RBMKColumn.ControlColumn) col).color;
                if(colorType == 0) { g = 0.0F; b = 0.0F; }
                else if(colorType == 1) { b = 0.0F; }
                else if(colorType == 2) { r = 0.0F; g = 0.5F; b = 0.0F; }
                else if(colorType == 3) { r = 0.0F; g = 0.0F; }
                else if(colorType == 4) { r = 0.5F; g = 0.0F; }
            } else {
                double heat = col.maxHeat > 0 ? col.heat / col.maxHeat : 0;
                double baseColor = 0.65D + (i % 2) * 0.05D;
                r = (float) (baseColor + ((1 - baseColor) * heat));
                g = (float) baseColor;
                b = (float) baseColor;
            }

            drawColumn(buf, kx, ky, kz, r, g, b);

            switch(col.type) {
                case FUEL:
                case FUEL_SIM:
                case BREEDER:
                    if(col instanceof RBMKColumn.FuelColumn) {
                        drawFuel(buf, kx + 0.01, ky, kz, ((RBMKColumn.FuelColumn) col).enrichment);
                    }
                    break;
                case CONTROL:
                    if(col instanceof RBMKColumn.ControlColumn) {
                        drawControl(buf, kx + 0.01, ky, kz, ((RBMKColumn.ControlColumn) col).level);
                    }
                    break;
                case CONTROL_AUTO:
                    if(col instanceof RBMKColumn.ControlColumn) {
                        drawControlAuto(buf, kx + 0.01, ky, kz, ((RBMKColumn.ControlColumn) col).level);
                    }
                    break;
                default:
            }
        }

        tess.draw();
        GlStateManager.enableTexture2D();

        GlStateManager.popMatrix();
    }

    private void drawColumn(BufferBuilder buf, double x, double y, double z, float r, float g, float b) {
        double width = 0.0625D * 0.75;
        buf.pos(x, y + width, z - width).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y + width, z + width).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y - width, z + width).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y - width, z - width).color(r, g, b, 1.0F).endVertex();
    }

    private void drawFuel(BufferBuilder buf, double x, double y, double z, double enrichment) {
        this.drawDot(buf, x, y, z, 0F, 0.25F + (float) (enrichment * 0.75D), 0F);
    }

    private void drawControl(BufferBuilder buf, double x, double y, double z, double level) {
        this.drawDot(buf, x, y, z, (float) level, (float) level, 0F);
    }

    private void drawControlAuto(BufferBuilder buf, double x, double y, double z, double level) {
        this.drawDot(buf, x, y, z, (float) level, 0F, (float) level);
    }

    private void drawDot(BufferBuilder buf, double x, double y, double z, float r, float g, float b) {
        double width = 0.03125D;
        double edge = 0.022097D;

        buf.pos(x, y + width, z).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y + edge, z + edge).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y, z + width).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y - edge, z + edge).color(r, g, b, 1.0F).endVertex();

        buf.pos(x, y + edge, z - edge).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y + width, z).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y - edge, z - edge).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y, z - width).color(r, g, b, 1.0F).endVertex();

        buf.pos(x, y + width, z).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y - edge, z + edge).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y - width, z).color(r, g, b, 1.0F).endVertex();
        buf.pos(x, y - edge, z - edge).color(r, g, b, 1.0F).endVertex();
    }
}
