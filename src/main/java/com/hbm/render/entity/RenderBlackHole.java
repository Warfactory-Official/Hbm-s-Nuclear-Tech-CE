package com.hbm.render.entity;

import com.hbm.Tags;
import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.entity.effect.EntityRagingVortex;
import com.hbm.entity.effect.EntityVortex;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ClientProxy;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.loader.IModelCustom;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import java.util.Random;
@AutoRegister(factory = "FACTORY")
public class RenderBlackHole extends Render<EntityBlackHole> {

	public static final IRenderFactory<EntityBlackHole> FACTORY = (RenderManager man) -> {
		return new RenderBlackHole(man);
	};

	protected static final ResourceLocation objTesterModelRL = new ResourceLocation(Tags.MODID, "models/Sphere.obj");
	protected IModelCustom blastModel;
	protected ResourceLocation hole = new ResourceLocation(Tags.MODID, "textures/models/explosion/BlackHole.png");
	protected ResourceLocation swirl = new ResourceLocation(Tags.MODID, "textures/entity/bhole.png");
	protected ResourceLocation disc = new ResourceLocation(Tags.MODID, "textures/entity/bholeDisc.png");

	protected RenderBlackHole(RenderManager renderManager){
		super(renderManager);
		blastModel = new HFRWavefrontObject(objTesterModelRL);
	}

	@Override
	public void doRender(EntityBlackHole entity, double x, double y, double z, float entityYaw, float partialTicks){
		if(!ClientProxy.renderingConstant)
			return;
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x, (float)y, (float)z);
		GlStateManager.disableLighting();
		GlStateManager.disableCull();

		float size = entity.getDataManager().get(EntityBlackHole.SIZE);

		GlStateManager.scale(size, size, size);

		bindTexture(hole);
		blastModel.renderAll();

		if(entity instanceof EntityVortex) {
			renderSwirl(entity, partialTicks);

		} else if(entity instanceof EntityRagingVortex) {
			renderSwirl(entity, partialTicks);
			renderJets(entity, partialTicks);

		} else {
			renderDisc(entity, partialTicks);
			renderJets(entity, partialTicks);
		}

		GlStateManager.enableCull();
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
	}

	protected ResourceLocation discTex(){
		return this.disc;
	}

	protected void renderDisc(EntityBlackHole entity, float interp){

		float glow = 0.75F;

		bindTexture(discTex());

		GlStateManager.pushMatrix();
		GlStateManager.rotate(entity.getEntityId() % 90 - 45, 1, 0, 0);
		GlStateManager.rotate(entity.getEntityId() % 360, 0, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.0F);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();

		int count = 16;

		Vec3 vec = Vec3.createVectorHelper(1, 0, 0);

		float[] color = {0, 0, 0, 0};
		for(int k = 0; k < steps(); k++) {

			GlStateManager.pushMatrix();
			GlStateManager.rotate((entity.ticksExisted + interp % 360) * -((float)Math.pow(k + 1, 1.25)), 0, 1, 0);
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			double s = 3 - k * 0.175D;

			for(int j = 0; j < 2; j++) {

				buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				for(int i = 0; i < count; i++) {

					if(j == 0){
						this.setColorFromIteration(k, 1F, color);
					} else {
						color[0] = 1;
						color[1] = 1;
						color[2] = 1;
						color[3] = glow;
					}
					buf.pos(vec.xCoord * s, 0, vec.zCoord * s).tex(0.5 + vec.xCoord * 0.25, 0.5 + vec.zCoord * 0.25).color(color[0], color[1], color[2], color[3]).endVertex();
					this.setColorFromIteration(k, 0F, color);
					buf.pos(vec.xCoord * s * 2, 0, vec.zCoord * s * 2).tex(0.5 + vec.xCoord * 0.5, 0.5 + vec.zCoord * 0.5).color(color[0], color[1], color[2], color[3]).endVertex();

					vec.rotateAroundY((float)(Math.PI * 2 / count));
					this.setColorFromIteration(k, 0F, color);
					buf.pos(vec.xCoord * s * 2, 0, vec.zCoord * s * 2).tex(0.5 + vec.xCoord * 0.5, 0.5 + vec.zCoord * 0.5).color(color[0], color[1], color[2], color[3]).endVertex();

					if(j == 0){
						this.setColorFromIteration(k, 1F, color);
					} else {
						color[0] = 1;
						color[1] = 1;
						color[2] = 1;
						color[3] = glow;
					}
					buf.pos(vec.xCoord * s, 0, vec.zCoord * s).tex(0.5 + vec.xCoord * 0.25, 0.5 + vec.zCoord * 0.25).color(color[0], color[1], color[2], color[3]).endVertex();
				}
				tes.draw();

				GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
			}

			GlStateManager.popMatrix();
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}

	protected int steps(){
		return 15;
	}

	protected void setColorFromIteration(int iteration, float alpha, float[] col){

		if(iteration < 5) {
			float g = 0.125F + iteration * (1F / 10F);
			col[0] = 1;
			col[1] = g;
			col[2] = 0;
			col[3] = alpha;
			return;
		}

		if(iteration == 5) {
			col[0] = 1.0F;
			col[1] = 1.0F;
			col[2] = 1.0F;
			col[3] = alpha;
			return;
		}

		if(iteration > 5) {
			int i = iteration - 6;
			float r = 1.0F - i * (1F / 9F);
			float g = 1F - i * (1F / 9F);
			float b = i * (1F / 5F);
			col[0] = r;
			col[1] = g;
			col[2] = b;
			col[3] = alpha;
		}
	}

	protected void renderSwirl(EntityBlackHole entity, float interp){

		float glow = 0.75F;

		if(entity instanceof EntityRagingVortex)
			glow = 0.25F;

		bindTexture(swirl);

		GlStateManager.pushMatrix();
		GlStateManager.rotate(entity.getEntityId() % 90 - 45, 1, 0, 0);
		GlStateManager.rotate(entity.getEntityId() % 360, 0, 1, 0);
		GlStateManager.rotate((entity.ticksExisted + interp % 360) * -5, 0, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.0F);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		Vec3 vec = Vec3.createVectorHelper(1, 0, 0);

		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();

		double s = 3;
		int count = 16;

		float[] color = {0, 0, 0, 0};
		
		//swirl, inner part (solid)
		for(int j = 0; j < 2; j++) {
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			for(int i = 0; i < count; i++) {
				color[0] = 0;
				color[1] = 0;
				color[2] = 0;
				color[3] = 1;
				buf.pos(vec.xCoord * 0.9, 0, vec.zCoord * 0.9).tex(0.5 + vec.xCoord * 0.25 / s * 0.9, 0.5 + vec.zCoord * 0.25 / s * 0.9).color(color[0], color[1], color[2], color[3]).endVertex();

				if(j == 0){
					this.setColorFull(entity, color);
				} else {
					color[0] = 1;
					color[1] = 1;
					color[2] = 1;
					color[3] = glow;
				}

				buf.pos(vec.xCoord * s, 0, vec.zCoord * s).tex(0.5 + vec.xCoord * 0.25, 0.5 + vec.zCoord * 0.25).color(color[0], color[1], color[2], color[3]).endVertex();

				vec.rotateAroundY((float)(Math.PI * 2 / count));

				if(j == 0){
					this.setColorFull(entity, color);
				} else {
					color[0] = 1;
					color[1] = 1;
					color[2] = 1;
					color[3] = glow;
				}
				
				buf.pos(vec.xCoord * s, 0, vec.zCoord * s).tex(0.5 + vec.xCoord * 0.25, 0.5 + vec.zCoord * 0.25).color(color[0], color[1], color[2], color[3]).endVertex();
				color[0] = 0;
				color[1] = 0;
				color[2] = 0;
				color[3] = 1;
				buf.pos(vec.xCoord * 0.9, 0, vec.zCoord * 0.9).tex(0.5 + vec.xCoord * 0.25 / s * 0.9, 0.5 + vec.zCoord * 0.25 / s * 0.9).color(color[0], color[1], color[2], color[3]).endVertex();
			}

			tes.draw();

			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		}

		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

		//swirl, outer part (fade)
		for(int j = 0; j < 2; j++) {

			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			for(int i = 0; i < count; i++) {

				if(j == 0){
					this.setColorFull(entity, color);
				}else {
					color[0] = 1;
					color[1] = 1;
					color[2] = 1;
					color[3] = glow;
				}
				buf.pos(vec.xCoord * s, 0, vec.zCoord * s).tex(0.5 + vec.xCoord * 0.25, 0.5 + vec.zCoord * 0.25).color(color[0], color[1], color[2], color[3]).endVertex();
				this.setColorNone(entity, color);
				buf.pos(vec.xCoord * s * 2, 0, vec.zCoord * s * 2).tex(0.5 + vec.xCoord * 0.5, 0.5 + vec.zCoord * 0.5).color(color[0], color[1], color[2], color[3]).endVertex();

				vec.rotateAroundY((float)(Math.PI * 2 / count));
				this.setColorNone(entity, color);
				buf.pos(vec.xCoord * s * 2, 0, vec.zCoord * s * 2).tex(0.5 + vec.xCoord * 0.5, 0.5 + vec.zCoord * 0.5).color(color[0], color[1], color[2], color[3]).endVertex();

				if(j == 0)
					this.setColorFull(entity, color);
				else {
					color[0] = 1;
					color[1] = 1;
					color[2] = 1;
					color[3] = glow;
				}
				buf.pos(vec.xCoord * s, 0, vec.zCoord * s).tex(0.5 + vec.xCoord * 0.25, 0.5 + vec.zCoord * 0.25).color(color[0], color[1], color[2], color[3]).endVertex();
			}
			tes.draw();

			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();

		GlStateManager.popMatrix();
	}

	protected void renderJets(EntityBlackHole entity, float interp){

		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();

		GlStateManager.pushMatrix();
		GlStateManager.rotate(entity.getEntityId() % 90 - 45, 1, 0, 0);
		GlStateManager.rotate(entity.getEntityId() % 360, 0, 1, 0);

		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.0F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableTexture2D();

		for(int j = -1; j <= 1; j += 2) {
			buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

			buf.pos(0, 0, 0).color(1, 1, 1, 0.35F).endVertex();

			Vec3 jet = Vec3.createVectorHelper(0.5, 0, 0);

			for(int i = 0; i <= 12; i++) {
				buf.pos(jet.xCoord, 10 * j, jet.zCoord).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
				jet.rotateAroundY((float)(Math.PI / 6 * -j));
			}

			tes.draw();
		}
		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}

	protected void renderFlare(EntityBlackHole entity){

		GlStateManager.pushMatrix();
		GlStateManager.scale(0.2F, 0.2F, 0.2F);

		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		RenderHelper.disableStandardItemLighting();
		int j = 75;
		float f1 = (j + 2.0F) / 200.0F;
		float f2 = 0.0F;
		int count = 250;

		count = j;

		if(f1 > 0.8F) {
			f2 = (f1 - 0.8F) / 0.2F;
		}

		Random random = new Random(432L);
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		GlStateManager.disableAlpha();
		GlStateManager.enableCull();
		GlStateManager.depthMask(false);

		float[] color = {0, 0, 0, 0};
		for(int i = 0; i < count; i++) {
			GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F + f1 * 90.0F, 0.0F, 0.0F, 1.0F);
			buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
			float f3 = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
			float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
			setColorFull(entity, color);
			buf.pos(0.0D, 0.0D, 0.0D).color(color[0], color[1], color[2], color[3]).endVertex();
			setColorNone(entity, color);
			buf.pos(-0.866D * f4, f3, -0.5F * f4).color(color[0], color[1], color[2], color[3]).endVertex();
			buf.pos(0.866D * f4, f3, -0.5F * f4).color(color[0], color[1], color[2], color[3]).endVertex();
			buf.pos(0.0D, f3, 1.0F * f4).color(color[0], color[1], color[2], color[3]).endVertex();
			buf.pos(-0.866D * f4, f3, -0.5F * f4).color(color[0], color[1], color[2], color[3]).endVertex();
			tes.draw();
		}

		GlStateManager.depthMask(true);
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.popMatrix();
	}

	protected void setColorFull(EntityBlackHole e, float[] color){
		if(e instanceof EntityVortex) {
			NTMRenderHelper.unpackColor(0x3898b3, color);
		} else if(e instanceof EntityRagingVortex) {
			NTMRenderHelper.unpackColor(0xe8390d, color);
		} else {
			NTMRenderHelper.unpackColor(0xFFB900, color);
		}
		color[3] = 1;
	}

	protected void setColorNone(EntityBlackHole e, float[] color){
		if(e instanceof EntityVortex) {
			NTMRenderHelper.unpackColor(0x3898b3, color);
		} else if(e instanceof EntityRagingVortex) {
			NTMRenderHelper.unpackColor(0xe8390d, color);
		} else {
			NTMRenderHelper.unpackColor(0xFFB900, color);
		}
		color[3] = 0;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBlackHole entity){
		return hole;
	}

}
