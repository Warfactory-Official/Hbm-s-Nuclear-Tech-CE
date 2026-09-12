package com.hbm.render.entity.missile;

import com.hbm.entity.missile.EntitySatellitePod;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

@AutoRegister(factory = "FACTORY")
public class RenderDropship extends Render<EntitySatellitePod> {

	public static final IRenderFactory<EntitySatellitePod> FACTORY = (RenderManager man) -> {return new RenderDropship(man);};

	protected RenderDropship(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntitySatellitePod pod, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		this.bindTexture(ResourceManager.dropship_tex);
		ResourceManager.dropship.renderPart("Pod");

		float legs = pod.prevLegs + (pod.legs - pod.prevLegs) * partialTicks;

		for(int i = 0; i < 4; i++) {
			GlStateManager.pushMatrix();
			GlStateManager.rotate(45 + 90 * i, 0, 1, 0);
			GlStateManager.translate(0.5, 1.75, 0);
			GlStateManager.rotate(150 * (1F - legs), 0, 0, 1);
			GlStateManager.translate(-0.5, -1.75, 0);
			ResourceManager.dropship.renderPart("Leg");
			GlStateManager.popMatrix();
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySatellitePod pod) {
		return ResourceManager.dropship_tex;
	}
}
