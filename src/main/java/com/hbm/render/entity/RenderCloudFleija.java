package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
@AutoRegister(factory = "FACTORY")
public class RenderCloudFleija extends Render<EntityCloudFleija> {

    public static final IRenderFactory<EntityCloudFleija> FACTORY = (RenderManager man) -> {
        return new RenderCloudFleija(man);
    };
    private static final ResourceLocation objTesterModelRL = new ResourceLocation(/*"/assets/" + */Tags.MODID, "models/Sphere.obj");
    public float scale = 0;
    public float ring = 0;
    private IModelCustom blastModel;
    private ResourceLocation blastTexture;

    protected RenderCloudFleija(RenderManager renderManager) {
        super(renderManager);
        blastModel = new HFRWavefrontObject(objTesterModelRL);
        blastTexture = new ResourceLocation(Tags.MODID, "textures/models/explosion/BlastFleija.png");
        scale = 0;
    }

    @Override
    public void doRender(EntityCloudFleija cloud, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.disableLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableCull();
        // GlStateManager.shadeModel(GL11.GL_SMOOTH);
        // GlStateManager.enableBlend();
        //GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        // GlStateManager.disableAlpha();

        float s = cloud.age + partialTicks;
        GlStateManager.scale(s, s, s);


        bindTexture(blastTexture);
        blastModel.renderAll();
       /* ResourceManager.normal_fadeout.use();
        GL20.glUniform4f(GL20.glGetUniformLocation(ResourceManager.normal_fadeout.getShaderId(), "color"), 0.2F*2, 0.92F*2, 0.83F*2, 1F);
        GL20.glUniform1f(GL20.glGetUniformLocation(ResourceManager.normal_fadeout.getShaderId(), "fadeout_mult"), 2.5F);
        ResourceManager.sphere_hq.renderAll();
        GlStateManager.scale(1.5F, 1.5F, 1.5F);
        GL20.glUniform1f(GL20.glGetUniformLocation(ResourceManager.normal_fadeout.getShaderId(), "fadeout_mult"), 0.5F);
        ResourceManager.sphere_hq.renderAll();
        HbmShaderManager2.releaseShader();*/

        //GlStateManager.enableAlpha();
        // GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableLighting();
        // GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCloudFleija entity) {
        return blastTexture;
    }

}
