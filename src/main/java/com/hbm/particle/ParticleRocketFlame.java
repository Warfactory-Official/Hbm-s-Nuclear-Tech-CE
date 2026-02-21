package com.hbm.particle;

import com.hbm.main.client.NTMClientRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ParticleRocketFlame extends Particle {

	private int age;
	private int maxAge;
	private int randSeed;

	public void updateInterpPos(){
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
	}


	public ParticleRocketFlame(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		maxAge = 300 + rand.nextInt(50);
		this.particleTexture = NTMClientRegistry.particle_base;
		this.randSeed = worldIn.rand.nextInt();
	}


	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}


	public void setMotionY(double y){
		this.motionY = y;
	}

	public void setMotion(double x, double y, double z){
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
	}

	public ParticleRocketFlame setScale(float scale) {
		this.particleScale = scale;
		return this;
	}

	public void setCollision(boolean bool){
		this.canCollide = bool;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		this.age++;

		if (this.age == this.maxAge) {
			this.setExpired();
		}

		this.motionX *= 0.9099999785423279D;
		this.motionY *= 0.9099999785423279D;
		this.motionZ *= 0.9099999785423279D;
		
        this.move(this.motionX, this.motionY, this.motionZ);
	}
	
	@Override
	public boolean shouldDisableDepth() {
		return true;
	}
	
	@Override
	public int getFXLayer() {
		return 1;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		
		Random urandom = new Random(randSeed);
		
		for(int i = 0; i < 10; i++) {
			
			float add = urandom.nextFloat() * 0.3F;
			float dark = 1 - Math.min(((float)(age) / (float)(maxAge * 0.25F)), 1);
			
	        this.particleRed = MathHelper.clamp(1 * dark + add, 0, 1);
	        this.particleGreen = MathHelper.clamp(0.6F * dark + add, 0, 1);
	        this.particleBlue = MathHelper.clamp(0 + add, 0, 1);
	        
	        this.particleAlpha = MathHelper.clamp((float) Math.pow(1 - Math.min(((float)(age) / (float)(maxAge)), 1), 0.5), 0, 1);
	        
			int j = this.getBrightnessForRender(partialTicks);
			int k = j >> 16 & 65535;
			int l = j & 65535;
			
			float spread = (float) Math.pow(((float)(age) / (float)maxAge) * 4F, 1.5) + 1F;
			
			float scale = urandom.nextFloat() * 0.5F + 0.1F + ((float)(age) / (float)maxAge) * 2F;
	        float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX) + (urandom.nextGaussian() - 1D) * 0.2F * spread);
	        float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY) + (urandom.nextGaussian() - 1D) * 0.5F * spread);
	        float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ) + (urandom.nextGaussian() - 1D) * 0.2F * spread);
	        
	        buffer.pos((double)(pX - rotationX * scale - rotationXY * scale), (double)(pY - rotationZ * scale), (double)(pZ - rotationYZ * scale - rotationXZ * scale)).tex(particleTexture.getMaxU(), particleTexture.getMaxV()).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.75F).lightmap(k, l).endVertex();
			buffer.pos((double)(pX - rotationX * scale + rotationXY * scale), (double)(pY + rotationZ * scale), (double)(pZ - rotationYZ * scale + rotationXZ * scale)).tex(particleTexture.getMaxU(), particleTexture.getMinV()).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.75F).lightmap(k, l).endVertex();
			buffer.pos((double)(pX + rotationX * scale + rotationXY * scale), (double)(pY + rotationZ * scale), (double)(pZ + rotationYZ * scale + rotationXZ * scale)).tex(particleTexture.getMinU(), particleTexture.getMinV()).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.75F).lightmap(k, l).endVertex();
			buffer.pos((double)(pX + rotationX * scale - rotationXY * scale), (double)(pY - rotationZ * scale), (double)(pZ + rotationYZ * scale - rotationXZ * scale)).tex(particleTexture.getMinU(), particleTexture.getMaxV()).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.75F).lightmap(k, l).endVertex();
		}
		
	}
	
	@Override
	public int getBrightnessForRender(float p_189214_1_) {
		return 240;
	}

}
