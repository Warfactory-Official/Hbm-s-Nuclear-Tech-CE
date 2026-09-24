package com.hbm.render.entity;

import com.hbm.entity.effect.EntityCloudSolinium;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.util.ColorUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

@AutoRegister(factory = "FACTORY")
public class RenderCloudSolinium extends Render<EntityCloudSolinium> {

	public static final IRenderFactory<EntityCloudSolinium> FACTORY = (RenderManager man) -> {return new RenderCloudSolinium(man);};

	protected RenderCloudSolinium(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityCloudSolinium entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableTexture2D();

		int color = 0x27FFDA;

		float scale = entity.age + partialTicks;
		GlStateManager.scale(scale, scale, scale);

		GlStateManager.color(ColorUtil.fr(color), ColorUtil.fg(color), ColorUtil.fb(color));
		ResourceManager.sphere_new.renderAll();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.color(ColorUtil.fr(color), ColorUtil.fg(color), ColorUtil.fb(color), 0.125F);

		GlStateManager.enableCull();

		double outerScale = 1.025;
		for(int i = 0; i < 3; i++) {
			GlStateManager.scale(outerScale, outerScale, outerScale);
			ResourceManager.sphere_new.renderAll();
		}

		GlStateManager.color(1F, 1F, 1F, 1F);

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCloudSolinium entity) {
		return null;
	}
}
