package com.hbm.particle;

import com.hbm.Tags;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class ParticleRBMKFlame extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/particle/rbmk_fire.png");
	
	public ParticleRBMKFlame(World worldIn, double posXIn, double posYIn, double posZIn, int maxAge){
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleMaxAge = maxAge;
		this.particleScale = rand.nextFloat() + 1F;
	}
	
	@Override
	public int getFXLayer(){
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
		Minecraft.getMinecraft().getTextureManager().bindTexture(getTexture());
		NTMRenderHelper.resetParticleInterpPos(entityIn, partialTicks);
		
		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		RenderHelper.disableStandardItemLighting();
		
		if(this.particleAge > this.particleMaxAge)
			this.particleAge = this.particleMaxAge;
		
		int texIndex = this.particleAge * 5 % 14;
		float f0 = 1F / 14F;

		float uMin = texIndex % 5 * f0;
		float uMax = uMin + f0;
		float vMin = 0;
		float vMax = 1;
			
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		
		GlStateManager.glNormal3f(0, 1, 0);
		GlStateManager.color(1.0F, 1.0F, 1.0F, this.particleAlpha);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		this.particleAlpha = 1F;
		
		if(this.particleAge < 20) {
			this.particleAlpha = this.particleAge / 20F;
		}
		
		if(this.particleAge > this.particleMaxAge - 20) {
			this.particleAlpha = (this.particleMaxAge - this.particleAge) / 20F;
		}

		this.particleAlpha *= 0.5F;
		
		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
		
		GlStateManager.translate(pX + rotationX, pY + rotationZ, pZ + rotationYZ);
		GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

		buf.pos((double) (-this.particleScale - 1), (double) (-this.particleScale * 2), (double) (0)).tex(uMax, vMax).endVertex();
		buf.pos((double) (-this.particleScale - 1), (double) (this.particleScale * 2), (double) 0).tex(uMax, vMin).endVertex();
		buf.pos((double) (this.particleScale - 1), (double) (this.particleScale * 2), (double) (0)).tex(uMin, vMin).endVertex();
		buf.pos((double) (this.particleScale - 1), (double) (-this.particleScale * 2), (double) (0)).tex(uMin, vMax).endVertex();
		
		tes.draw();
		
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.doPolygonOffset(0, 0);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	protected ResourceLocation getTexture() {
		return texture;
	}

}
