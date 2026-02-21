package com.hbm.particle_instanced;

import com.hbm.main.client.NTMClientRegistry;
import net.minecraft.world.World;

import java.nio.ByteBuffer;
import java.util.Random;

public class ParticleExSmokeInstanced extends ParticleInstanced {

	private int age;
	private int maxAge;
	private float[] vals = new float[6*5];
	
	public ParticleExSmokeInstanced(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		maxAge = 100 + rand.nextInt(40);
		this.particleTexture = NTMClientRegistry.contrail;
		initVals(worldIn.rand.nextInt());
	}
	
	private void initVals(int seed){
		Random urandom = new Random(seed);
		for(int i = 0; i < 6; i ++){
			//The three random values that are added to the position when rendering
			vals[i*5] = (float) (urandom.nextGaussian() - 1D) * 0.75F;
			vals[i*5+1] = (float) (urandom.nextGaussian() - 1D) * 0.75F;
			vals[i*5+2] = (float) (urandom.nextGaussian() - 1D) * 0.75F;
			//Random scale
			vals[i*5+3] = (urandom.nextFloat() + 0.5F)*4;
			//Random color
			vals[i*5+4] = urandom.nextFloat() * 0.5F + 0.4F;
		}
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
	public void addDataToBuffer(ByteBuffer buf, float partialTicks) {
		float x = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX));
		float y = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY));
		float z = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ));
		
		for(int ii = 0; ii < 6; ii ++){
			buf.putFloat(x+vals[ii*5]);
			buf.putFloat(y+vals[ii*5+1]);
			buf.putFloat(z+vals[ii*5+2]);
			
			buf.putFloat(vals[ii*5+3]);
			
			buf.putFloat(this.particleTexture.getMinU());
			buf.putFloat(this.particleTexture.getMinV());
			buf.putFloat(this.particleTexture.getMaxU()-this.particleTexture.getMinU());
			buf.putFloat(this.particleTexture.getMaxV()-this.particleTexture.getMinV());
			
			this.particleRed = this.particleGreen = this.particleBlue = vals[ii*5+4];
			buf.put((byte) (this.particleRed*255));
			buf.put((byte) (this.particleGreen*255));
			buf.put((byte) (this.particleBlue*255));
			buf.put((byte) (this.particleAlpha*255));
			
			//int i = this.getBrightnessForRender(partialTicks);
			//int j = i >> 16 & 65535;
			//int k = i & 65535;
			//Bruh I have no clue how these lightmap coords work. They don't seem to be like regular uvs.
			buf.put((byte) 240);
			buf.put((byte) 240);
		}
	}
	
	@Override
	public int getFaceCount() {
		return 6;
	}

}
