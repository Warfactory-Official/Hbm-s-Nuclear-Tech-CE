package com.hbm.particle;

import com.hbm.main.client.NTMClientRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class ParticleSmokePlume extends Particle {

	private int age;
	private int maxAge;

	public ParticleSmokePlume(TextureManager p_i1213_1_, World p_i1218_1_, double p_i1218_2_, double p_i1218_4_, double p_i1218_6_) {
		super(p_i1218_1_, p_i1218_2_, p_i1218_4_, p_i1218_6_);
		maxAge = 100 + rand.nextInt(40);
		
		int r = rand.nextInt(4);
		float veloc = 0.5F;
		
		if(r == 1) {
			motionX = rand.nextGaussian() * veloc + veloc;
			motionZ = rand.nextGaussian() * 0.05;
		} else if(r == 2) {
			motionX = rand.nextGaussian() * veloc - veloc;
			motionZ = rand.nextGaussian() * 0.05;
		} else if(r == 3) {
			motionZ = rand.nextGaussian() * veloc + veloc;
			motionX = rand.nextGaussian() * 0.05;
		} else if(r == 0) {
			motionZ = rand.nextGaussian() * veloc - veloc;
			motionX = rand.nextGaussian() * 0.05;
		}
		this.particleScale = 0.5F;
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
        
		double bak = new Vec3d(motionX, motionY, motionZ).length();
		
        this.move(this.motionX, this.motionY, this.motionZ);
        
        if (Math.abs(motionX) < 0.025 && Math.abs(motionZ) < 0.025)
        {
        	motionY = bak;
        }

        motionX *= 0.925;
        motionY *= 0.925;
        motionZ *= 0.925;
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	/*public void renderParticle(Tessellator p_70539_1_, float p_70539_2_, float p_70539_3_, float p_70539_4_, float p_70539_5_, float p_70539_6_, float p_70539_7_) {
		this.theRenderEngine.bindTexture(texture);
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		RenderHelper.disableStandardItemLighting();
		
		Random urandom = new Random(this.getEntityId());
		
		for(int i = 0; i < 6; i++) {
			
			p_70539_1_.startDrawingQuads();
			
	        this.particleRed = this.particleGreen = this.particleBlue = urandom.nextFloat() * 0.7F + 0.2F;
	        
			p_70539_1_.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
			p_70539_1_.setNormal(0.0F, 1.0F, 0.0F);
			p_70539_1_.setBrightness(240);
			
			float scale = 0.5F;
	        float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double)p_70539_2_ - interpPosX) + urandom.nextGaussian() * 0.5);
	        float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double)p_70539_2_ - interpPosY) + urandom.nextGaussian() * 0.5);
	        float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double)p_70539_2_ - interpPosZ) + urandom.nextGaussian() * 0.5);
	        
			p_70539_1_.addVertexWithUV((double)(pX - p_70539_3_ * scale - p_70539_6_ * scale), (double)(pY - p_70539_4_ * scale), (double)(pZ - p_70539_5_ * scale - p_70539_7_ * scale), 1, 1);
			p_70539_1_.addVertexWithUV((double)(pX - p_70539_3_ * scale + p_70539_6_ * scale), (double)(pY + p_70539_4_ * scale), (double)(pZ - p_70539_5_ * scale + p_70539_7_ * scale), 1, 0);
			p_70539_1_.addVertexWithUV((double)(pX + p_70539_3_ * scale + p_70539_6_ * scale), (double)(pY + p_70539_4_ * scale), (double)(pZ + p_70539_5_ * scale + p_70539_7_ * scale), 0, 0);
			p_70539_1_.addVertexWithUV((double)(pX + p_70539_3_ * scale - p_70539_6_ * scale), (double)(pY - p_70539_4_ * scale), (double)(pZ + p_70539_5_ * scale - p_70539_7_ * scale), 0, 1);
			p_70539_1_.draw();
		}
		
		GlStateManager.polygonOffset(0.0F, 0.0F);
		GlStateManager.enableLighting();
	}*/
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		
		this.particleTexture = NTMClientRegistry.contrail;
		float f = (float) this.particleTextureIndexX / 16.0F;
		float f1 = f + 0.0624375F;
		float f2 = (float) this.particleTextureIndexY / 16.0F;
		float f3 = f2 + 0.0624375F;
		float f4 = 0.75F * this.particleScale;

		if (this.particleTexture != null) {
			f = this.particleTexture.getMinU();
			f1 = this.particleTexture.getMaxU();
			f2 = this.particleTexture.getMinV();
			f3 = this.particleTexture.getMaxV();
		}
		
		Random urandom = new Random(this.hashCode());
		for (int ii = 0; ii < 6; ii++) {
			
			float f5 = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX) + urandom.nextGaussian() * 0.5);
			float f6 = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY) + urandom.nextGaussian() * 0.5);
			float f7 = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ) + urandom.nextGaussian() * 0.5);
			
			this.particleRed = this.particleGreen = this.particleBlue = urandom.nextFloat() * 0.7F + 0.2F;
			
			int i = this.getBrightnessForRender(partialTicks);
			int j = i >> 16 & 65535;
			int k = i & 65535;
			Vec3d[] avec3d = new Vec3d[] { new Vec3d((double) (-rotationX * f4 - rotationXY * f4), (double) (-rotationZ * f4), (double) (-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double) (-rotationX * f4 + rotationXY * f4), (double) (rotationZ * f4), (double) (-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double) (rotationX * f4 + rotationXY * f4), (double) (rotationZ * f4), (double) (rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double) (rotationX * f4 - rotationXY * f4), (double) (-rotationZ * f4), (double) (rotationYZ * f4 - rotationXZ * f4)) };

			if (this.particleAngle != 0.0F) {
				float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
				float f9 = MathHelper.cos(f8 * 0.5F);
				float f10 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.x;
				float f11 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.y;
				float f12 = MathHelper.sin(f8 * 0.5F) * (float) cameraViewDir.z;
				Vec3d vec3d = new Vec3d((double) f10, (double) f11, (double) f12);

				for (int l = 0; l < 4; ++l) {
					avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double) (f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double) (2.0F * f9)));
				}
			}

			buffer.pos((double) f5 + avec3d[0].x, (double) f6 + avec3d[0].y, (double) f7 + avec3d[0].z).tex((double) f1, (double) f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double) f5 + avec3d[1].x, (double) f6 + avec3d[1].y, (double) f7 + avec3d[1].z).tex((double) f1, (double) f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double) f5 + avec3d[2].x, (double) f6 + avec3d[2].y, (double) f7 + avec3d[2].z).tex((double) f, (double) f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
			buffer.pos((double) f5 + avec3d[3].x, (double) f6 + avec3d[3].y, (double) f7 + avec3d[3].z).tex((double) f, (double) f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
		
		}
	}
	
	@Override
	public int getBrightnessForRender(float p_189214_1_) {
		return 240;
	}

}
