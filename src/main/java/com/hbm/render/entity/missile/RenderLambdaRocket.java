package com.hbm.render.entity.missile;

import com.hbm.entity.missile.EntityRocketLambda;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.factory.LegoClient;
import com.hbm.main.ResourceManager;
import com.hbm.util.Vec3NT;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

@AutoRegister(factory = "FACTORY")
public class RenderLambdaRocket extends Render<EntityRocketLambda> {

	public static final IRenderFactory<EntityRocketLambda> FACTORY = (RenderManager man) -> {return new RenderLambdaRocket(man);};

	protected static final Vec3NT vec = new Vec3NT(0, 0, 0);

	protected RenderLambdaRocket(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityRocketLambda entity, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		this.bindTexture(ResourceManager.lambda_rocket_tex);
		ResourceManager.lambda_rocket.renderAll();

		vec.setComponents(x, y, z).normalizeSelf().multiply(-1);
		GlStateManager.translate(vec.x, vec.y, vec.z);

		GlStateManager.scale(2, 2, 2);
		LegoClient.renderFlare(entity, partialTicks, 1F, 0.75F, 0.5F);

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityRocketLambda entity) {
		return ResourceManager.lambda_rocket_tex;
	}
}
