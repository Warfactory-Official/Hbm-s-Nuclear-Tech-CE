package com.hbm.render.entity.item;

import com.hbm.entity.item.EntityParachuteCrate;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

@AutoRegister(entity = EntityParachuteCrate.class, factory = "FACTORY")
public class RenderParachuteCrate extends Render<EntityParachuteCrate> {

    public static final IRenderFactory<EntityParachuteCrate> FACTORY = RenderParachuteCrate::new;

    public RenderParachuteCrate(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityParachuteCrate entity, double x, double y, double z, float i, float j) {

        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, z);

        double time = (entity.world.getTotalWorldTime());
        double sine = Math.sin(time * 0.05) * 5;
        double sin3 = Math.sin(time * 0.05 + Math.PI * 0.5) * 5;

        int height = 7;

        GlStateManager.translate(0.0F, height, 0.0F);
        GlStateManager.rotate(sine, 0, 0, 1);
        GlStateManager.rotate(sin3, 1, 0, 0);
        GlStateManager.translate(0.0F, -height, 0.0F);

        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        bindTexture(ResourceManager.supply_crate);
        ResourceManager.conservecrate.renderAll();

        GlStateManager.translate(0, -1, 0);

        bindTexture(ResourceManager.soyuz_chute_tex);
        ResourceManager.soyuz_lander.renderPart("Chute");
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityParachuteCrate p_110775_1_) {
        return ResourceManager.soyuz_lander_tex;
    }
}
