package com.hbm.render.entity.projectile;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityChemical;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import java.awt.*;
@AutoRegister(factory = "FACTORY")
public class RenderChemical extends Render<EntityChemical> {
    private static ResourceLocation gas = new ResourceLocation(Tags.MODID + ":textures/particle/particle_base.png");

    public static final IRenderFactory<EntityChemical> FACTORY =
            man -> new RenderChemical(man);

    protected RenderChemical(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityChemical chem, double x, double y, double z, float f0, float f1) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        EntityChemical.ChemicalStyle style = chem.getStyle();

        if(style == EntityChemical.ChemicalStyle.AMAT || style == EntityChemical.ChemicalStyle.LIGHTNING)
            renderAmatBeam(chem, f1);

        if(style == EntityChemical.ChemicalStyle.GAS) {
            this.bindEntityTexture(chem);
            renderGasCloud(chem, f1);
        }

        if(style == EntityChemical.ChemicalStyle.GASFLAME) {
            this.bindEntityTexture(chem);
            renderGasFire(chem, f1);
        }

        GL11.glPopMatrix();
    }

    private void renderGasFire(EntityChemical chem, float interp) {

        float exp = (float) (chem.ticksExisted + interp) / (float) chem.getMaxAge();
        double size = 0.0 + exp * 2;
        Color color = Color.getHSBColor(Math.max((60 - exp * 100) / 360F, 0.0F), 1 - exp * 0.25F, 1 - exp * 0.5F);

        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GlStateManager.depthMask(false);

        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = (int) Math.max(255.0F * (1.0F - exp), 0.0F);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buf.pos(-size, -size, 0.0D).tex(1.0D, 1.0D).color(r, g, b, a).endVertex();
        buf.pos(size, -size, 0.0D).tex(0.0D, 1.0D).color(r, g, b, a).endVertex();
        buf.pos(size, size, 0.0D).tex(0.0D, 0.0D).color(r, g, b, a).endVertex();
        buf.pos(-size, size, 0.0D).tex(1.0D, 0.0D).color(r, g, b, a).endVertex();
        tess.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);
    }

    private void renderGasCloud(EntityChemical chem, float interp) {

        double exp = (double) (chem.ticksExisted + interp) / (double) chem.getMaxAge();
        double size = 0.0 + exp * 10;
        int color = chem.getType().getColor();

        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(false);

        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        java.util.Random rand = new java.util.Random(chem.getEntityId());
        int i = rand.nextInt(2);
        int j = rand.nextInt(2);

        double u0 = 1 - i;
        double v0 = 1 - j;
        double u1 = i;
        double v1 = j;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int) Math.max(127.0D * (1.0D - exp), 0.0D);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buf.pos(-size, -size, 0.0D).tex(u0, v0).color(r, g, b, a).endVertex();
        buf.pos(size, -size, 0.0D).tex(u1, v0).color(r, g, b, a).endVertex();
        buf.pos(size, size, 0.0D).tex(u1, v1).color(r, g, b, a).endVertex();
        buf.pos(-size, size, 0.0D).tex(u0, v1).color(r, g, b, a).endVertex();
        tess.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);
    }

    private void renderAmatBeam(EntityChemical chem, float interp) {

        float yaw = chem.prevRotationYaw + (chem.rotationYaw - chem.prevRotationYaw) * interp;
        float pitch = chem.prevRotationPitch + (chem.rotationPitch - chem.prevRotationPitch) * interp;
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-pitch - 90.0F, 1.0F, 0.0F, 0.0F);

        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.depthMask(false);

        double motionLen = Math.sqrt(chem.motionX * chem.motionX + chem.motionY * chem.motionY + chem.motionZ * chem.motionZ);
        double length = motionLen * (chem.ticksExisted + interp) * 0.75D;
        double size = 0.0625D;
        float o = 0.2F;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        buf.pos(-size, 0.0D, -size).color(1.0F, 1.0F, 1.0F, o).endVertex();
        buf.pos(size, 0.0D, -size).color(1.0F, 1.0F, 1.0F, o).endVertex();
        buf.pos(size, length, -size).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
        buf.pos(-size, length, -size).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();

        buf.pos(-size, 0.0D, size).color(1.0F, 1.0F, 1.0F, o).endVertex();
        buf.pos(size, 0.0D, size).color(1.0F, 1.0F, 1.0F, o).endVertex();
        buf.pos(size, length, size).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
        buf.pos(-size, length, size).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();

        buf.pos(-size, 0.0D, -size).color(1.0F, 1.0F, 1.0F, o).endVertex();
        buf.pos(-size, 0.0D, size).color(1.0F, 1.0F, 1.0F, o).endVertex();
        buf.pos(-size, length, size).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
        buf.pos(-size, length, -size).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();

        buf.pos(size, 0.0D, -size).color(1.0F, 1.0F, 1.0F, o).endVertex();
        buf.pos(size, 0.0D, size).color(1.0F, 1.0F, 1.0F, o).endVertex();
        buf.pos(size, length, size).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
        buf.pos(size, length, -size).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();

        tess.draw();

        GlStateManager.depthMask(true);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityChemical entity) {
        return gas;
    }
}
