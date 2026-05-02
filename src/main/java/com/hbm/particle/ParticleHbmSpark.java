package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class ParticleHbmSpark extends Particle {

	public List<double[]> steps = new ArrayList<>();
	public int thresh;
	
	public ParticleHbmSpark(World worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ){
		super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ);
		thresh = 4 + rand.nextInt(3);
		steps.add(new double[] { motionX, motionY, motionZ });
		this.particleMaxAge = 20 + rand.nextInt(10);
		this.particleGravity = 0.5F;
	}
	
	public ParticleHbmSpark makeSmall(boolean small) {
		if(!small) return this;
		this.setSize(0.01F, 0.01F);
		this.thresh = 3;
		this.particleMaxAge = 2 + rand.nextInt(3);
		this.motionY = -Math.abs(motionY);
		return this;
	}

	@Override
	public int getFXLayer(){
		return 3;
	}
	
	@Override
	public void onUpdate(){
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}

		steps.add(new double[] { motionX, motionY, motionZ });

		while(steps.size() > thresh)
			steps.remove(0);

		this.motionY -= 0.04D * (double) this.particleGravity;
		double lastY = this.motionY;
		this.move(this.motionX, this.motionY, this.motionZ);

		if(this.onGround) {
			this.onGround = false;
			motionY = -lastY * 0.8D;
		}
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
		if(steps.size() < 2)
			return;

		GlStateManager.pushMatrix();

		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.glLineWidth(3);

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
		
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.color(1, 1, 1, 1);
		buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

		double[] prev = new double[] { pX, pY, pZ };

		for(int i = 1; i < steps.size(); i++) {

			double[] curr = new double[] { prev[0] + steps.get(i)[0], prev[1] + steps.get(i)[1], prev[2] + steps.get(i)[2] };

			buf.pos(prev[0], prev[1], prev[2]).endVertex();

			prev = curr;
		}
		tes.draw();

		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}

}
