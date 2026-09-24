package com.hbm.render.entity;

import com.hbm.entity.logic.EntityOrbitalLaser;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ClientProxy;
import com.hbm.util.Vec3NT;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

@AutoRegister(factory = "FACTORY")
public class RenderOrbitalLaser extends Render<EntityOrbitalLaser> {

	public static final IRenderFactory<EntityOrbitalLaser> FACTORY = (RenderManager man) -> new RenderOrbitalLaser(man);

	protected RenderOrbitalLaser(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityOrbitalLaser entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if(!ClientProxy.renderingConstant) return;

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.disableLighting();
		GlStateManager.enableCull();
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.depthMask(false);

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

		Vec3NT vector = Vec3NT.createVectorHelper(0.5D, 0, 0);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		RenderHelper.disableStandardItemLighting();

		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		for(int i = 0; i < 8; i++) {
			buf.pos(vector.x, 250.0D, vector.z).color(1F, 0F, 0F, 1F).endVertex();
			buf.pos(vector.x, 0.0D, vector.z).color(1F, 0F, 0F, 1F).endVertex();
			vector.rotateYawSelf(45);
			buf.pos(vector.x, 0.0D, vector.z).color(1F, 0F, 0F, 1F).endVertex();
			buf.pos(vector.x, 250.0D, vector.z).color(1F, 0F, 0F, 1F).endVertex();
		}

		for(int i = 0; i < 8; i++) {
			buf.pos(vector.x / 2, 250.0D, vector.z / 2).color(1F, 1F, 1F, 1F).endVertex();
			buf.pos(vector.x / 2, 0.0D, vector.z / 2).color(1F, 1F, 1F, 1F).endVertex();
			vector.rotateYawSelf(45);
			buf.pos(vector.x / 2, 0.0D, vector.z / 2).color(1F, 1F, 1F, 1F).endVertex();
			buf.pos(vector.x / 2, 250.0D, vector.z / 2).color(1F, 1F, 1F, 1F).endVertex();
		}

		tessellator.draw();
		GlStateManager.popMatrix();

		GlStateManager.depthMask(true);
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.popMatrix();
	}

	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) { }

	@Override
	protected ResourceLocation getEntityTexture(EntityOrbitalLaser entity) {
		return null;
	}
}
