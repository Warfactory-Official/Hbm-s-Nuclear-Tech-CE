package com.hbm.render.entity.item;

import com.hbm.entity.logic.EntityC130;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
@AutoRegister(entity = EntityC130.class, factory = "FACTORY")
public class RenderC130 extends Render<EntityC130> {

    public static final IRenderFactory<EntityC130> FACTORY = RenderC130::new;

    public RenderC130(RenderManager renderManager) {
        super(renderManager);
    }
    @Override
    public void doRender(EntityC130 entity, double x, double y, double z, float entityYaw, float partialTicks) {

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90, 0F, 0F, 1F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableCull();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.c130_0_tex);
        ResourceManager.c130.renderPart("Plane");

        double spin = System.currentTimeMillis() * 15D % 360D;

        GlStateManager.pushMatrix();
        GlStateManager.translate(10, 4.2, -20.5);
        GlStateManager.rotate(spin, 1, 0, 0);
        GlStateManager.translate(-10, -4.2, 20.5);
        ResourceManager.c130.renderPart("Prop1");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(10, 4.2, -11.16);
        GlStateManager.rotate(spin, 1, 0, 0);
        GlStateManager.translate(-10, -4.2, 11.16);
        ResourceManager.c130.renderPart("Prop2");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(10, 4.2, 11.16);
        GlStateManager.rotate(spin, 1, 0, 0);
        GlStateManager.translate(-10, -4.2, -11.16);
        ResourceManager.c130.renderPart("Prop3");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(10, 4.2, 20.5);
        GlStateManager.rotate(spin, 1, 0, 0);
        GlStateManager.translate(-10, -4.2, -20.5);
        ResourceManager.c130.renderPart("Prop4");
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityC130 entity) {
        return ResourceManager.c130_0_tex;
    }

}
