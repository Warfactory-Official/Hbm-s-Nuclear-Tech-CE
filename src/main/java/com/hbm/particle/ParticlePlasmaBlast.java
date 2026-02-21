package com.hbm.particle;

import com.hbm.Tags;
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

public class ParticlePlasmaBlast extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/particle/shockwave.png");
	
	private float rotationPitch;
	private float rotationYaw;
	
	public ParticlePlasmaBlast(World worldIn, double posXIn, double posYIn, double posZIn, float r, float g, float b, float pitch, float yaw){
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleMaxAge = 20;
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		this.rotationPitch = pitch;
		this.rotationYaw = yaw;
	}
	
	public void setMaxAge(int maxAge) {
		this.particleMaxAge = maxAge;
	}
	
	public void setScale(float scale) {
		this.particleScale = scale;
	}

	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		GlStateManager.disableCull();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.glNormal3f(0, 1, 0);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		
		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

		GlStateManager.translate(pX, pY, pZ);
		GL11.glRotated(this.rotationYaw, 0, 1, 0);
		GL11.glRotated(this.rotationPitch, 1, 0, 0);
			
		this.particleAlpha = 1 - (((float)this.particleAge + partialTicks) / (float)this.particleMaxAge);
		float scale = (1 - (float)Math.pow(Math.E, (this.particleAge + partialTicks) * -0.125)) * this.particleScale;
		GlStateManager.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
		
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos((double)(- 1 * scale), (double)(- 0.25), (double)(- 1 * scale)).tex(1, 1).endVertex();
		buf.pos((double)(- 1 * scale), (double)(- 0.25), (double)(+ 1 * scale)).tex(1, 0).endVertex();
		buf.pos((double)(+ 1 * scale), (double)(- 0.25), (double)(+ 1 * scale)).tex(0, 0).endVertex();
		buf.pos((double)(+ 1 * scale), (double)(- 0.25), (double)(- 1 * scale)).tex(0, 1).endVertex();
		tes.draw();
		
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableCull();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

}
