package com.hbm.render.entity.mob;

import com.hbm.Tags;
import com.hbm.entity.mob.EntityFBIDrone;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import java.util.Random;
@AutoRegister(entity = EntityFBIDrone.class, factory = "FACTORY")
public class RenderDrone extends Render<EntityFBIDrone> {

    public static final IRenderFactory<EntityFBIDrone> FACTORY = man -> new RenderDrone(man);

    public RenderDrone(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityFBIDrone entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.25F, (float) z);

        this.bindTexture(this.getEntityTexture(entity));

        Random rand = new Random(entity.getEntityId());
        GlStateManager.rotate((float) (rand.nextDouble() * 360.0D), 0.0F, 1.0F, 0.0F);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableCull();
        ResourceManager.drone.renderAll();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFBIDrone entity) {
        return new ResourceLocation(Tags.MODID, "textures/entity/quadcopter.png");
    }
}
