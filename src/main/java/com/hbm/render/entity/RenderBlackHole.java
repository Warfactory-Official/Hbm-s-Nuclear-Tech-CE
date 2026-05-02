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
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
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

	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
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

		int count = 16;

		Vec3 vec = Vec3.createVectorHelper(1, 0, 0);

		float[] color = {0, 0, 0, 0};
		for(int k = 0; k < steps(); k++) {

			GlStateManager.pushMatrix();
			GlStateManager.rotate((entity.ticksExisted + interp % 360) * -((float)Math.pow(k + 1, 1.25)), 0, 1, 0);
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			float s = 3F - k * 0.175F;

			for(int j = 0; j < 2; j++) {

				NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionTexColorQuads(count);
				for(int i = 0; i < count; i++) {

					if(j == 0){
						this.setColorFromIteration(k, 1F, color);
					} else {
						color[0] = 1;
						color[1] = 1;
						color[2] = 1;
						color[3] = glow;
					}
					int innerColor = packCurrentColor(color);
					float vx0 = (float) vec.xCoord;
					float vz0 = (float) vec.zCoord;
					float x0 = vx0 * s;
					float z0 = vz0 * s;
					float u0 = 0.5F + vx0 * 0.25F;
					float v0 = 0.5F + vz0 * 0.25F;

					this.setColorFromIteration(k, 0F, color);
					int outerColor = packCurrentColor(color);
					float x1 = vx0 * s * 2F;
					float z1 = vz0 * s * 2F;
					float u1 = 0.5F + vx0 * 0.5F;
					float v1 = 0.5F + vz0 * 0.5F;

					vec.rotateAroundY((float)(Math.PI * 2 / count));
					float vx1 = (float) vec.xCoord;
					float vz1 = (float) vec.zCoord;
					buf.appendPositionTexColorQuadUnchecked(
							x0, 0, z0, u0, v0, innerColor,
							x1, 0, z1, u1, v1, outerColor,
							vx1 * s * 2F, 0, vz1 * s * 2F, 0.5F + vx1 * 0.5F, 0.5F + vz1 * 0.5F, outerColor,
							vx1 * s, 0, vz1 * s, 0.5F + vx1 * 0.25F, 0.5F + vz1 * 0.25F, innerColor
					);
				}
				NTMImmediate.INSTANCE.draw();

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

		float s = 3F;
		int count = 16;

		float[] color = {0, 0, 0, 0};

		//swirl, inner part (solid)
		for(int j = 0; j < 2; j++) {
			NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionTexColorQuads(count);
			for(int i = 0; i < count; i++) {
				color[0] = 0;
				color[1] = 0;
				color[2] = 0;
				color[3] = 1;
				int innerCoreColor = packCurrentColor(color);
				float vx0 = (float) vec.xCoord;
				float vz0 = (float) vec.zCoord;
				float x0 = vx0 * 0.9F;
				float z0 = vz0 * 0.9F;
				float u0 = 0.5F + vx0 * 0.25F / s * 0.9F;
				float v0 = 0.5F + vz0 * 0.25F / s * 0.9F;

				if(j == 0){
					this.setColorFull(entity, color);
				} else {
					color[0] = 1;
					color[1] = 1;
					color[2] = 1;
					color[3] = glow;
				}
				int ringColor = packCurrentColor(color);
				float x1 = vx0 * s;
				float z1 = vz0 * s;
				float u1 = 0.5F + vx0 * 0.25F;
				float v1 = 0.5F + vz0 * 0.25F;

				vec.rotateAroundY((float)(Math.PI * 2 / count));
				float vx1 = (float) vec.xCoord;
				float vz1 = (float) vec.zCoord;
				buf.appendPositionTexColorQuadUnchecked(
						x0, 0, z0, u0, v0, innerCoreColor,
						x1, 0, z1, u1, v1, ringColor,
						vx1 * s, 0, vz1 * s, 0.5F + vx1 * 0.25F, 0.5F + vz1 * 0.25F, ringColor,
						vx1 * 0.9F, 0, vz1 * 0.9F, 0.5F + vx1 * 0.25F / s * 0.9F, 0.5F + vz1 * 0.25F / s * 0.9F, innerCoreColor
				);
			}

			NTMImmediate.INSTANCE.draw();

			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		}

		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

		//swirl, outer part (fade)
		for(int j = 0; j < 2; j++) {

			NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionTexColorQuads(count);
			for(int i = 0; i < count; i++) {

				if(j == 0){
					this.setColorFull(entity, color);
				}else {
					color[0] = 1;
					color[1] = 1;
					color[2] = 1;
					color[3] = glow;
				}
				int innerRingColor = packCurrentColor(color);
				float vx0 = (float) vec.xCoord;
				float vz0 = (float) vec.zCoord;
				float x0 = vx0 * s;
				float z0 = vz0 * s;
				float u0 = 0.5F + vx0 * 0.25F;
				float v0 = 0.5F + vz0 * 0.25F;
				this.setColorNone(entity, color);
				int outerFadeColor = packCurrentColor(color);
				float x1 = vx0 * s * 2F;
				float z1 = vz0 * s * 2F;
				float u1 = 0.5F + vx0 * 0.5F;
				float v1 = 0.5F + vz0 * 0.5F;

				vec.rotateAroundY((float)(Math.PI * 2 / count));
				float vx1 = (float) vec.xCoord;
				float vz1 = (float) vec.zCoord;
				buf.appendPositionTexColorQuadUnchecked(
						x0, 0, z0, u0, v0, innerRingColor,
						x1, 0, z1, u1, v1, outerFadeColor,
						vx1 * s * 2F, 0, vz1 * s * 2F, 0.5F + vx1 * 0.5F, 0.5F + vz1 * 0.5F, outerFadeColor,
						vx1 * s, 0, vz1 * s, 0.5F + vx1 * 0.25F, 0.5F + vz1 * 0.25F, innerRingColor
				);
			}
			NTMImmediate.INSTANCE.draw();

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

		int centerColor = NTMBufferBuilder.packColor(1.0F, 1.0F, 1.0F, 0.35F);
		int edgeColor = NTMBufferBuilder.packColor(1.0F, 1.0F, 1.0F, 0.0F);
		for(int j = -1; j <= 1; j += 2) {
			NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionColor(GL11.GL_TRIANGLE_FAN, 14);
			buf.appendPositionColorUnchecked(0, 0, 0, centerColor);

			Vec3 jet = Vec3.createVectorHelper(0.5, 0, 0);

			for(int i = 0; i <= 12; i++) {
				buf.appendPositionColorUnchecked((float) jet.xCoord, 10 * j, (float) jet.zCoord, edgeColor);
				jet.rotateAroundY((float)(Math.PI / 6 * -j));
			}

			NTMImmediate.INSTANCE.draw();
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
			NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionColor(GL11.GL_TRIANGLE_FAN, 5);
			float f3 = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
			float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
			setColorFull(entity, color);
			int fullColor = packCurrentColor(color);
			setColorNone(entity, color);
			int fadeColor = packCurrentColor(color);
			buf.appendPositionColorUnchecked(0.0F, 0.0F, 0.0F, fullColor);
			buf.appendPositionColorUnchecked(-0.866F * f4, f3, -0.5F * f4, fadeColor);
			buf.appendPositionColorUnchecked(0.866F * f4, f3, -0.5F * f4, fadeColor);
			buf.appendPositionColorUnchecked(0.0F, f3, 1.0F * f4, fadeColor);
			buf.appendPositionColorUnchecked(-0.866F * f4, f3, -0.5F * f4, fadeColor);
			NTMImmediate.INSTANCE.draw();
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

	private static int packCurrentColor(float[] color){
		return NTMBufferBuilder.packColor(color[0], color[1], color[2], color[3]);
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
