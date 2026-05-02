package com.hbm.particle.gluon;

import com.hbm.main.ResourceManager;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleGluonParticleSmol extends Particle {

	float workingAlpha;
	public int timeUntilChange = 0;
	
	protected ParticleGluonParticleSmol(World worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = 0.1F;
		this.particleMaxAge = 6 + rand.nextInt(5);
		this.motionX = mX + (rand.nextFloat()-0.5)*0.04;
		this.motionY = mY + (rand.nextFloat()-0.5)*0.04;
		this.motionZ = mZ + (rand.nextFloat()-0.5)*0.04;
		this.particleRed = 0.5F;
		this.particleGreen = 0.8F;
		timeUntilChange = rand.nextInt(5)+1;
	}
	
	public ParticleGluonParticleSmol color(float colR, float colG, float colB, float colA){
		this.particleRed = colR;
		this.particleGreen = colG;
		this.particleBlue = colB;
		this.particleAlpha = colA;
		workingAlpha = colA;
		return this;
	}
	
	public ParticleGluonParticleSmol lifetime(int lifetime){
		this.particleMaxAge = lifetime;
		return this;
	}

	@Override
	public void onUpdate() {
		this.particleAge++;
		if(particleAge >= particleMaxAge){
			this.setExpired();
			return;
		}
		this.prevPosX = posX;
		this.prevPosY = posY;
		this.prevPosZ = posZ;
		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		this.motionX*=0.8;
		this.motionY*=0.8;
		this.motionZ*=0.8;
		if(timeUntilChange == 0){
			timeUntilChange = rand.nextInt(5)+1;
			this.motionX += (rand.nextFloat()-0.5)*0.04;
			this.motionY += (rand.nextFloat()-0.5)*0.04;
			this.motionZ += (rand.nextFloat()-0.5)*0.04;
		}
	}
	
	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public boolean shouldDisableDepth() {
		return true;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.fresnel_ms);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		float timeScale = (this.particleAge+partialTicks)/(float)this.particleMaxAge;
		float shrink = MathHelper.clamp(1-BobMathUtil.remap((float)MathHelper.clamp(timeScale, 0, 1), 0.6F, 1F, 0.6F, 1F), 0, 1);
		this.workingAlpha = shrink*particleAlpha;
		
		float f4 = 0.1F * (this.particleScale+shrink*particleScale*4);
        
        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        
        GlStateManager.translate(f5, f6, f7);
		
        float rX = rotationX * f4;
        float rZ = rotationZ * f4;
        float rXY = rotationXY * f4;
        float rYZ = rotationYZ * f4;
        float rXZ = rotationXZ * f4;
        NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, 4);
        int packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, this.workingAlpha);
        int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rX - rXY, -rZ, -rYZ - rXZ, 1, 1, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rX + rXY, rZ, -rYZ + rXZ, 1, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(rX + rXY, rZ, rYZ + rXZ, 0, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(rX - rXY, -rZ, rYZ - rXZ, 0, 1, packedColor, packedLightmap);
        NTMImmediate.INSTANCE.draw();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
	}
	
}
