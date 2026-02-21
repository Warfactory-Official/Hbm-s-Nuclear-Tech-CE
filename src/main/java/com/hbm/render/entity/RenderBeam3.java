package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.projectile.EntityMinerBeam;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
@AutoRegister(factory = "FACTORY")
public class RenderBeam3 extends Render<EntityMinerBeam> {

	public static final IRenderFactory<EntityMinerBeam> FACTORY = (RenderManager man) -> {return new RenderBeam3(man);};
	public static final ResourceLocation beam_rl = new ResourceLocation(Tags.MODID + ":textures/models/projectiles/PlasmaBeam.png");
	
	protected RenderBeam3(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityMinerBeam rocket, double x, double y, double z, float entityYaw, float partialTicks) {
		float radius = 0.12F;
		//float radius = 0.06F;
		int distance = 4;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();

		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		GlStateManager.translate((float) x, (float) y, (float) z);

		GlStateManager.rotate(rocket.rotationYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-rocket.rotationPitch, 1.0F, 0.0F, 0.0F);

		boolean red = true;
		boolean green = false;
		boolean blue = true;

		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		for (float o = 0; o <= radius; o += radius / 8) {
			float color = 1f - (o * 8.333f);
			if (color < 0)
				color = 0;
			buf.pos(0 + o, 0 - o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 + o, 0 + o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 + o, 0 + o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 + o, 0 - o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			
			buf.pos(0 - o, 0 - o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 + o, 0 - o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 + o, 0 - o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 - o, 0 - o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			
			buf.pos(0 - o, 0 + o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 - o, 0 - o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 - o, 0 - o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 - o, 0 + o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			
			buf.pos(0 + o, 0 + o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 - o, 0 + o, 0).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 - o, 0 + o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
			buf.pos(0 + o, 0 + o, 0 + distance).color(red ? 1 : color, green ? 1 : color, blue ? 1 : color, 1f).endVertex();
		}
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMinerBeam entity) {
		return beam_rl;
	}

}
