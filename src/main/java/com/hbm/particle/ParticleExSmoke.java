package com.hbm.particle;

import com.hbm.main.client.NTMClientRegistry;
import com.hbm.render.util.NTMBufferBuilder;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ParticleExSmoke extends Particle {

	private int age;
	private int maxAge;
	private int randomSeed;
	
	public ParticleExSmoke(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		maxAge = 100 + rand.nextInt(40);
		randomSeed = worldIn.rand.nextInt();
		this.setParticleTexture(NTMClientRegistry.contrail);
	}
	
	public void setMotion(double x, double y, double z){
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
	}
	
	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		particleAlpha = 1 - ((float) age / (float) maxAge);
		
		++this.age;

		if (this.age == this.maxAge) {
			this.setExpired();
		}

		this.motionX *= 0.7599999785423279D;
		this.motionY *= 0.7599999785423279D;
		this.motionZ *= 0.7599999785423279D;
		
        this.move(this.motionX, this.motionY, this.motionZ);
	}
	
	@Override
	public int getFXLayer() {
		return 1;
	}
	
	@Override
	public boolean shouldDisableDepth() {
		return true;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		
		NTMBufferBuilder fastBuffer = (NTMBufferBuilder) buffer;
		Random urandom = new Random(randomSeed);
		
		for(int i = 0; i < 6; i++) {
			
	        this.particleRed = this.particleGreen = this.particleBlue = urandom.nextFloat() * 0.5F + 0.4F;
	        
	        int j = this.getBrightnessForRender(partialTicks);
	        int k = j >> 16 & 65535;
	        int l = j & 65535;
	        
			float scale = urandom.nextFloat() + 0.5F;
	        float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX) + (urandom.nextGaussian() - 1D) * 0.75F);
	        float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY) + (urandom.nextGaussian() - 1D) * 0.75F);
	        float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ) + (urandom.nextGaussian() - 1D) * 0.75F);

			int packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
			int packedLightmap = NTMBufferBuilder.packLightmap(k, l);
			fastBuffer.appendParticlePositionTexColorLmap(pX - rotationX * scale - rotationXY * scale, pY - rotationZ * scale, pZ - rotationYZ * scale - rotationXZ * scale, particleTexture.getMaxU(), particleTexture.getMaxV(), packedColor, packedLightmap);
			fastBuffer.appendParticlePositionTexColorLmap(pX - rotationX * scale + rotationXY * scale, pY + rotationZ * scale, pZ - rotationYZ * scale + rotationXZ * scale, particleTexture.getMaxU(), particleTexture.getMinV(), packedColor, packedLightmap);
			fastBuffer.appendParticlePositionTexColorLmap(pX + rotationX * scale + rotationXY * scale, pY + rotationZ * scale, pZ + rotationYZ * scale + rotationXZ * scale, particleTexture.getMinU(), particleTexture.getMinV(), packedColor, packedLightmap);
			fastBuffer.appendParticlePositionTexColorLmap(pX + rotationX * scale - rotationXY * scale, pY - rotationZ * scale, pZ + rotationYZ * scale - rotationXZ * scale, particleTexture.getMinU(), particleTexture.getMaxV(), packedColor, packedLightmap);
		}
	}

	@Override
	public int getBrightnessForRender(float p_189214_1_) {
		return 240;
	}
}
