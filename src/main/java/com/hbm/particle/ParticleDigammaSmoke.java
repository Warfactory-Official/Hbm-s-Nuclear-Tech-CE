package com.hbm.particle;

import com.hbm.main.client.NTMClientRegistry;
import com.hbm.render.util.NTMBufferBuilder;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ParticleDigammaSmoke extends Particle {

	public ParticleDigammaSmoke(World worldIn, double posXIn, double posYIn, double posZIn){
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleTexture = NTMClientRegistry.particle_base;
		particleMaxAge = 100 + rand.nextInt(40);
		this.canCollide = false;
		
		this.particleScale = 5;
		
		this.particleRed = 0.5F + rand.nextFloat() * 0.2F;
		this.particleGreen = 0.0F;
		this.particleBlue = 0.0F;
	}
	
	public void motion(float x, float y, float z){
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
	}
	
	@Override
	public void onUpdate(){
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		particleAlpha = 1 - ((float) particleAge / (float) particleMaxAge);

		++this.particleAge;

		if(this.particleAge == this.particleMaxAge) {
			this.setExpired();
		}

		this.motionX *= 0.99D;
		this.motionY *= 0.99D;
		this.motionZ *= 0.99D;

		this.move(this.motionX, this.motionY, this.motionZ);
	}
	
	@Override
	public int getFXLayer(){
		return 1;
	}
	
	@Override
	public void renderParticle(BufferBuilder buf, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
		float scale = this.particleScale;
		float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX));
		float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY));
		float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ));
		NTMBufferBuilder fastBuffer = (NTMBufferBuilder) buf;
		int packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
		int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
		fastBuffer.appendParticlePositionTexColorLmap(pX - rotationX * scale - rotationXY * scale, pY - rotationZ * scale, pZ - rotationYZ * scale - rotationXZ * scale, particleTexture.getMaxU(), particleTexture.getMaxV(), packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmap(pX - rotationX * scale + rotationXY * scale, pY + rotationZ * scale, pZ - rotationYZ * scale + rotationXZ * scale, particleTexture.getMaxU(), particleTexture.getMinV(), packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmap(pX + rotationX * scale + rotationXY * scale, pY + rotationZ * scale, pZ + rotationYZ * scale + rotationXZ * scale, particleTexture.getMinU(), particleTexture.getMinV(), packedColor, packedLightmap);
		fastBuffer.appendParticlePositionTexColorLmap(pX + rotationX * scale - rotationXY * scale, pY - rotationZ * scale, pZ + rotationYZ * scale - rotationXZ * scale, particleTexture.getMinU(), particleTexture.getMaxV(), packedColor, packedLightmap);
	}

	@Override
	public int getBrightnessForRender(float p_189214_1_){
		return 240;
	}
}
