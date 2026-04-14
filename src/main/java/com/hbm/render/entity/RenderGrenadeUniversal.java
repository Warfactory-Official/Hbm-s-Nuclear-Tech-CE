package com.hbm.render.entity;

import com.hbm.entity.grenade.EntityGrenadeUniversal;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderGrenade;
import com.hbm.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@AutoRegister(entity = EntityGrenadeUniversal.class, factory = "FACTORY")
public class RenderGrenadeUniversal extends Render<EntityGrenadeUniversal> {

    public static final IRenderFactory<EntityGrenadeUniversal> FACTORY = RenderGrenadeUniversal::new;

    protected RenderGrenadeUniversal(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityGrenadeUniversal grenade, double x, double y, double z, float entityYaw, float partialTicks) {
        final boolean prevCull     = RenderUtil.isCullEnabled();
        final boolean prevLighting = RenderUtil.isLightingEnabled();
        final int     prevShade    = RenderUtil.getShadeModel();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        double scale = 0.0625D;
        GlStateManager.scale(scale, scale, scale);

        double yaw = grenade.prevRotationYaw + (grenade.rotationYaw - grenade.prevRotationYaw) * partialTicks;
        GlStateManager.rotate((float) yaw, 0, 1, 0);

        double spin = grenade.prevSpin + (grenade.spin - grenade.prevSpin) * partialTicks;
        GlStateManager.rotate((float) spin, 1, 0, 0);

        if (grenade.getBounces() > 0) {
            GlStateManager.rotate(-80, 0, 0, 1);
        }

        ItemStack stack = grenade.getGrenadeItem();
        ItemRenderGrenade.renderGrenade(stack, null);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GlStateManager.shadeModel(prevShade);
        if (!prevLighting) GlStateManager.disableLighting();
        if (!prevCull)     GlStateManager.disableCull();

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGrenadeUniversal grenade) {
        return ResourceManager.grenade_frag_tex;
    }
}
