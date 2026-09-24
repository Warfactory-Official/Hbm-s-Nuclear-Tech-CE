package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.util.Vec3NT;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.oil.TileEntityMachinePumpjack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

@AutoRegister
public class RenderPumpjack extends TileEntitySpecialRenderer<TileEntityMachinePumpjack> implements IItemRendererProvider {
    @Override
    public void render(TileEntityMachinePumpjack pj, double x, double y, double z, float f, int destroyStage, float alpha) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();

        switch (pj.getBlockMetadata() - BlockDummyable.offset) {
            case 3:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
            case 2:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
        }

        float rotation = (pj.prevRot + (pj.rot - pj.prevRot) * f);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        bindTexture(ResourceManager.pumpjack_tex);
        ResourceManager.pumpjack.renderPart("Base");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.5, -5.5);
        GlStateManager.rotate(rotation - 90, 1, 0, 0);
        GlStateManager.translate(0, -1.5, 5.5);
        ResourceManager.pumpjack.renderPart("Rotor");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 3.5, -3.5);
        GlStateManager.rotate((float) (Math.toDegrees(Math.sin(Math.toRadians(rotation))) * 0.25), 1, 0, 0);
        GlStateManager.translate(0, -3.5, 3.5);
        ResourceManager.pumpjack.renderPart("Head");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -Math.sin(Math.toRadians(rotation)), 0);
        ResourceManager.pumpjack.renderPart("Carriage");
        GlStateManager.popMatrix();

        Vec3NT backPos = Vec3NT.createVectorHelper(0, 0, -2);
        backPos.rotatePitchSelf(-(float) Math.sin(Math.toRadians(rotation)) * 0.25F);

        Vec3NT rot = Vec3NT.createVectorHelper(0, 0.5, 0);
        rot.rotatePitchSelf(-(float) Math.toRadians(rotation - 90));

        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        final float la = 1.0F;

        final float lr = 0.5F, lg = 0.5F, lb = 0.5F;
        for (int i = -1; i <= 1; i += 2) {
            double xi = 0.53125 * i;

            double y1 = 1.5D + rot.y;
            double z1 = -5.5D + rot.z;

            double y2 = 3.5 + backPos.y;
            double z2 = -3.5 + backPos.z;

            buf.pos(xi, y1, z1 - 0.0625).color(lr, lg, lb, la).endVertex();
            buf.pos(xi, y1, z1 + 0.0625).color(lr, lg, lb, la).endVertex();
            buf.pos(xi, y2, z2 + 0.0625).color(lr, lg, lb, la).endVertex();
            buf.pos(xi, y2, z2 - 0.0625).color(lr, lg, lb, la).endVertex();
        }

        final float dr = 0.2F, dg = 0.2F, db = 0.2F;

        double pd = 0.03125D;
        double width = 0.25D;
        double height = -Math.sin(Math.toRadians(rotation));

        for (int i = -1; i <= 1; i += 2) {

            float pRot = -(float) (Math.sin(Math.toRadians(rotation)) * 0.25);

            Vec3NT frontPos = Vec3NT.createVectorHelper(0, 0, 1);
            frontPos.rotatePitchSelf(pRot);

            double dist = 0.03125D;
            Vec3NT frontRad = Vec3NT.createVectorHelper(0, 0, 2.5 + dist);
            double cutlet = 360D / 32D;
            frontRad.rotatePitchSelf(pRot);
            frontRad.rotatePitchSelf(-(float) Math.toRadians(cutlet * -3));

            for (int j = 0; j < 4; j++) {

                double sumY1 = frontPos.y + frontRad.y;
                double sumZ1 = frontPos.z + frontRad.z;
                if (frontRad.y < 0) sumZ1 = 3.5 + dist * 0.5;

                frontRad.rotatePitchSelf(-(float) Math.toRadians(cutlet));

                double sumY2 = frontPos.y + frontRad.y;
                double sumZ2 = frontPos.z + frontRad.z;
                if (frontRad.y < 0) sumZ2 = 3.5 + dist * 0.5;

                double xL = (width - pd) * i;
                double xR = (width + pd) * i;

                buf.pos(xL, 3.5 + sumY1, -3.5 + sumZ1).color(dr, dg, db, la).endVertex();
                buf.pos(xR, 3.5 + sumY1, -3.5 + sumZ1).color(dr, dg, db, la).endVertex();
                buf.pos(xR, 3.5 + sumY2, -3.5 + sumZ2).color(dr, dg, db, la).endVertex();
                buf.pos(xL, 3.5 + sumY2, -3.5 + sumZ2).color(dr, dg, db, la).endVertex();
            }

            double sumY = frontPos.y + frontRad.y;
            double sumZ = frontPos.z + frontRad.z;
            if (frontRad.y < 0) sumZ = 3.5 + dist * 0.5;

            double xR = (width + pd) * i;
            double xL = (width - pd) * i;

            buf.pos(xR, 3.5 + sumY, -3.5 + sumZ).color(dr, dg, db, la).endVertex();
            buf.pos(xL, 3.5 + sumY, -3.5 + sumZ).color(dr, dg, db, la).endVertex();
            buf.pos(xL, 2 + height, 0).color(dr, dg, db, la).endVertex();
            buf.pos(xR, 2 + height, 0).color(dr, dg, db, la).endVertex();
        }

        double p = 0.03125;
        buf.pos(p, height + 1.5, p).color(dr, dg, db, la).endVertex();
        buf.pos(-p, height + 1.5, -p).color(dr, dg, db, la).endVertex();
        buf.pos(-p, 0.75, -p).color(dr, dg, db, la).endVertex();
        buf.pos(p, 0.75, p).color(dr, dg, db, la).endVertex();

        buf.pos(-p, height + 1.5, p).color(dr, dg, db, la).endVertex();
        buf.pos(p, height + 1.5, -p).color(dr, dg, db, la).endVertex();
        buf.pos(p, 0.75, -p).color(dr, dg, db, la).endVertex();
        buf.pos(-p, 0.75, p).color(dr, dg, db, la).endVertex();

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_pumpjack);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            public void renderInventory() {
                GlStateManager.translate(0, -2, 0);
                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.scale(4, 4, 4);
            }

            public void renderCommon() {
                GlStateManager.disableCull();
                GlStateManager.scale(0.5, 0.5, 0.5);
                GlStateManager.translate(0, 0, 3);
                bindTexture(ResourceManager.pumpjack_tex);
                ResourceManager.pumpjack.renderAll();
                GlStateManager.enableCull();
            }
        };
    }
}
