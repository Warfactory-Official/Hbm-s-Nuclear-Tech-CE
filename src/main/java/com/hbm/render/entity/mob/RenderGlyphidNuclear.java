package com.hbm.render.entity.mob;

import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.entity.mob.glyphid.EntityGlyphidNuclear;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@AutoRegister(entity = EntityGlyphidNuclear.class, factory = "FACTORY")
public class RenderGlyphidNuclear extends RenderLiving<EntityGlyphid> {
    public static final IRenderFactory<EntityGlyphidNuclear> FACTORY = RenderGlyphidNuclear::new;

    public RenderGlyphidNuclear(RenderManager renderManager) {
        super(renderManager, new ModelNuclearGlyphid(), 1.0F);
        this.shadowOpaque = 0.0F;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGlyphid entity) {
        return entity.getSkin();
    }

    @Override
    protected void preRenderCallback(@NotNull EntityGlyphid entity, float interp) {
        this.preRenderCallback((EntityGlyphidNuclear) entity, interp);
    }

    protected void preRenderCallback(EntityGlyphidNuclear entity, float interp) {
        float swell = (entity.deathTicks + interp) / 95F;
        float flash = 1.0F + MathHelper.sin(swell * 100.0F) * swell * 0.01F;

        if(swell < 0.0F) {
            swell = 0.0F;
        }

        if(swell > 1.0F) {
            swell = 1.0F;
        }

        swell *= swell;
        swell *= swell;

        float scaleHorizontal = (1.0F + swell * 0.4F) * flash;
        float scaleVertical = (1.0F + swell * 0.1F) / flash;
        GlStateManager.scale(scaleHorizontal, scaleVertical, scaleHorizontal);
    }

    @Override
    protected int getColorMultiplier(@NotNull EntityGlyphid entity, float lightBrightness, float interp) {
        return this.getColorMultiplier((EntityGlyphidNuclear) entity, lightBrightness, interp);
    }

    protected int getColorMultiplier(EntityGlyphidNuclear entity, float lightBrightness, float interp) {
        float swell = (entity.deathTicks + interp) / 20F;




        int a = (int) (swell * 0.2F * 255.0F);

        if((int) (swell * 10.0F) % 4 < 2)
            return a *= 0.75;

        if(a < 0) {
            a = 0;
        }

        if(a > 255) {
            a = 255;
        }

        short r = 255;
        short g = 255;
        short b = 255;
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static class ModelNuclearGlyphid extends ModelBase {

        double bite = 0;

        @Override
        public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float interp) {
            bite = entity.getSwingProgress(interp);
        }

        @Override
        public void render(@NotNull Entity entity, float limbSwing, float limbSwingAmount, float rotationYaw, float rotationHeadYaw, float rotationPitch, float scale) {
            GlStateManager.pushMatrix();

            GlStateManager.rotate(180, 1, 0, 0);
            GlStateManager.translate(0, -1.5F, 0);
            GlStateManager.enableLighting();
            GlStateManager.disableCull();

            double s = ((EntityGlyphid) entity).getScale();
            GlStateManager.scale(s, s, s);

            EntityLivingBase living = (EntityLivingBase) entity;
            byte armor = living.getDataManager().get(EntityGlyphid.ARMOR);

            double cy0 = Math.sin((double) limbSwing % (Math.PI * 2));
            double cy1 = Math.sin((double) limbSwing % (Math.PI * 2) - Math.PI * 0.5);
            double cy2 = Math.sin((double) limbSwing % (Math.PI * 2) - Math.PI);
            double cy3 = Math.sin((double) limbSwing % (Math.PI * 2) - Math.PI * 0.75);

            double bite = MathHelper.clamp(Math.sin(this.bite * Math.PI * 2 - Math.PI * 0.5), 0, 1) * 20;
            double headTilt = Math.sin(this.bite * Math.PI) * 30;

            ResourceManager.glyphid.renderPart("Body");
            if((armor & 1) > 0) ResourceManager.glyphid.renderPart("ArmorFront");
            if((armor & (1 << 1)) > 0) ResourceManager.glyphid.renderPart("ArmorLeft");
            if((armor & (1 << 2)) > 0) ResourceManager.glyphid.renderPart("ArmorRight");

            /// LEFT ARM ///
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.25, 0.625, 0.0625);
            GlStateManager.rotate(10, 0, 1, 0);
            GlStateManager.rotate(35 + cy1 * 20, 1, 0, 0);
            GlStateManager.translate(-0.25, -0.625, -0.0625);
            ResourceManager.glyphid.renderPart("ArmLeftUpper");
            GlStateManager.translate(0.25, 0.625, 0.4375);
            GlStateManager.rotate(-75 - cy1 * 20 + cy0 * 20, 1, 0, 0);
            GlStateManager.translate(-0.25, -0.625, -0.4375);
            ResourceManager.glyphid.renderPart("ArmLeftMid");
            GlStateManager.translate(0.25, 0.625, 0.9375);
            GlStateManager.rotate(90 - cy0 * 45, 1, 0, 0);
            GlStateManager.translate(-0.25, -0.625, -0.9375);
            ResourceManager.glyphid.renderPart("ArmLeftLower");
            if((armor & (1 << 3)) > 0) ResourceManager.glyphid.renderPart("ArmLeftArmor");
            GlStateManager.popMatrix();

            /// RIGHT ARM ///
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.25, 0.625, 0.0625);
            GlStateManager.rotate(-10, 0, 1, 0);
            GlStateManager.rotate(35 + cy2 * 20, 1, 0, 0);
            GlStateManager.translate(0.25, -0.625, -0.0625);
            ResourceManager.glyphid.renderPart("ArmRightUpper");
            GlStateManager.translate(-0.25, 0.625, 0.4375);
            GlStateManager.rotate(-75 - cy2 * 20 + cy3 * 20, 1, 0, 0);
            GlStateManager.translate(0.25, -0.625, -0.4375);
            ResourceManager.glyphid.renderPart("ArmRightMid");
            GlStateManager.translate(-0.25, 0.625, 0.9375);
            GlStateManager.rotate(90 - cy3 * 45, 1, 0, 0);
            GlStateManager.translate(0.25, -0.625, -0.9375);
            ResourceManager.glyphid.renderPart("ArmRightLower");
            if((armor & (1 << 4)) > 0) ResourceManager.glyphid.renderPart("ArmRightArmor");
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();

            GlStateManager.translate(0, 0.5, 0.25);
            GlStateManager.rotate(headTilt, 0, 0, 1);
            GlStateManager.translate(0, -0.5, -0.25);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.5, 0.25);
            GlStateManager.rotate(-bite, 1, 0, 0);
            GlStateManager.translate(0, -0.5, -0.25);
            ResourceManager.glyphid.renderPart("JawTop");
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.5, 0.25);
            GlStateManager.rotate(bite, 0, 1, 0);
            GlStateManager.rotate(bite, 1, 0, 0);
            GlStateManager.translate(0, -0.5, -0.25);
            ResourceManager.glyphid.renderPart("JawLeft");
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.5, 0.25);
            GlStateManager.rotate(-bite, 0, 1, 0);
            GlStateManager.rotate(bite, 1, 0, 0);
            GlStateManager.translate(0, -0.5, -0.25);
            ResourceManager.glyphid.renderPart("JawRight");
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();

            double steppy = 15;
            double bend = 60;

            for(int i = 0; i < 3; i++) {

                double c0 = cy0 * (i == 1 ? -1 : 1);
                double c1 = cy1 * (i == 1 ? -1 : 1);

                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0.25, 0);
                GlStateManager.rotate(i * 30 - 15 + c0 * 7.5, 0, 1, 0);
                GlStateManager.rotate(steppy + c1 * steppy, 0, 0, 1);
                GlStateManager.translate(0, -0.25, 0);
                ResourceManager.glyphid.renderPart("LegLeftUpper");
                GlStateManager.translate(0.5625, 0.25, 0);
                GlStateManager.rotate(-bend - c1 * steppy, 0, 0, 1);
                GlStateManager.translate(-0.5625, -0.25, 0);
                ResourceManager.glyphid.renderPart("LegLeftLower");
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0.25, 0);
                GlStateManager.rotate(i * 30 - 45 + c0 * 7.5, 0, 1, 0);
                GlStateManager.rotate(-steppy + c1 * steppy, 0, 0, 1);
                GlStateManager.translate(0, -0.25, 0);
                ResourceManager.glyphid.renderPart("LegRightUpper");
                GlStateManager.translate(-0.5625, 0.25, 0);
                GlStateManager.rotate(bend - c1 * steppy, 0, 0, 1);
                GlStateManager.translate(0.5625, -0.25, 0);
                ResourceManager.glyphid.renderPart("LegRightLower");
                GlStateManager.popMatrix();
            }


            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.translate(0, 1, 0);
            GlStateManager.rotate(90, 1, 0, 0);
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.mini_nuke_tex);
            ResourceManager.projectiles.renderPart("MiniNuke");
            GlStateManager.shadeModel(GL11.GL_FLAT);

            GlStateManager.popMatrix();
        }
    }
}
