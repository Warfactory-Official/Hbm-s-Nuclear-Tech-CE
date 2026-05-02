package com.hbm.render.tileentity;

import com.hbm.blocks.machine.rbmk.RBMKMiniPanelBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.tileentity.machine.rbmk.RBMKColumn;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKDisplay;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
@AutoRegister
public class RenderRBMKDisplay extends TileEntitySpecialRenderer<TileEntityRBMKDisplay> {

    @Override
    public void render(TileEntityRBMKDisplay te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();

        EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(RBMKMiniPanelBase.FACING);
        switch(facing) {
            case NORTH: GlStateManager.rotate(90, 0F, 1F, 0F); break;
            case WEST: GlStateManager.rotate(180, 0F, 1F, 0F); break;
            case SOUTH: GlStateManager.rotate(270, 0F, 1F, 0F); break;
            case EAST: GlStateManager.rotate(0, 0F, 1F, 0F); break;
            default: break;
        }

        GlStateManager.translate(0, 0.5, 0);
        GlStateManager.scale(1, 8D / 7D, 8D / 7D);
        GlStateManager.translate(0, -0.5, 0);

        GlStateManager.disableTexture2D();
        GlStateManager.glNormal3f(1.0F, 0.0F, 0.0F);

        int expectedQuads = 0;
        for(RBMKColumn col : te.columns) {
            if(col == null) continue;
            expectedQuads++;
            switch(col.type) {
                case FUEL:
                case FUEL_SIM:
                case BREEDER:
                case CONTROL:
                case CONTROL_AUTO:
                    expectedQuads += 3;
                    break;
                default:
            }
        }

        NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionColorQuads(expectedQuads);

        for(int i = 0; i < te.columns.length; i++) {

            RBMKColumn col = te.columns[i];

            if(col == null) continue;

            int row = i / 7;
            int column = i % 7;
            float kx = 0.28125F;
            float ky = -row * 0.125F + 0.875F;
            float kz = -column * 0.125F + 0.125F * 3F;

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

            if(col.indicator > 0) {
                r = 1.0F;
                g = 1.0F;
                b = 0.0F;
            }

            drawColumn(buf, kx, ky, kz, r, g, b);

            switch(col.type) {
                case FUEL:
                case FUEL_SIM:
                case BREEDER:
                    if(col instanceof RBMKColumn.FuelColumn) {
                        drawFuel(buf, kx + 0.01F, ky, kz, (float) ((RBMKColumn.FuelColumn) col).enrichment);
                    }
                    break;
                case CONTROL:
                    if(col instanceof RBMKColumn.ControlColumn) {
                        drawControl(buf, kx + 0.01F, ky, kz, (float) ((RBMKColumn.ControlColumn) col).level);
                    }
                    break;
                case CONTROL_AUTO:
                    if(col instanceof RBMKColumn.ControlColumn) {
                        drawControlAuto(buf, kx + 0.01F, ky, kz, (float) ((RBMKColumn.ControlColumn) col).level);
                    }
                    break;
                default:
            }
        }

        NTMImmediate.INSTANCE.draw();
        GlStateManager.enableTexture2D();

        GlStateManager.popMatrix();
    }

    private void drawColumn(NTMBufferBuilder buf, float x, float y, float z, float r, float g, float b) {
        float width = 0.0625F * 0.75F;
        int packedColor = NTMBufferBuilder.packColor(r, g, b, 1.0F);
        buf.appendPositionColorQuadUnchecked(
                x, y + width, z - width,
                x, y + width, z + width,
                x, y - width, z + width,
                x, y - width, z - width,
                packedColor
        );
    }

    private void drawFuel(NTMBufferBuilder buf, float x, float y, float z, float enrichment) {
        this.drawDot(buf, x, y, z, 0F, 0.25F + enrichment * 0.75F, 0F);
    }

    private void drawControl(NTMBufferBuilder buf, float x, float y, float z, float level) {
        this.drawDot(buf, x, y, z, level, level, 0F);
    }

    private void drawControlAuto(NTMBufferBuilder buf, float x, float y, float z, float level) {
        this.drawDot(buf, x, y, z, level, 0F, level);
    }

    private void drawDot(NTMBufferBuilder buf, float x, float y, float z, float r, float g, float b) {
        float width = 0.03125F;
        float edge = 0.022097F;
        int packedColor = NTMBufferBuilder.packColor(r, g, b, 1.0F);

        buf.appendPositionColorQuadUnchecked(
                x, y + width, z,
                x, y + edge, z + edge,
                x, y, z + width,
                x, y - edge, z + edge,
                packedColor
        );
        buf.appendPositionColorQuadUnchecked(
                x, y + edge, z - edge,
                x, y + width, z,
                x, y - edge, z - edge,
                x, y, z - width,
                packedColor
        );
        buf.appendPositionColorQuadUnchecked(
                x, y + width, z,
                x, y - edge, z + edge,
                x, y - width, z,
                x, y - edge, z - edge,
                packedColor
        );
    }
}
