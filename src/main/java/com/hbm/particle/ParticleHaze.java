package com.hbm.particle;

import com.hbm.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.util.Random;

public class ParticleHaze extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/particle/haze.png");
	private int maxAge;
	
	public ParticleHaze(World worldIn, double posXIn, double posYIn, double posZIn){
		super(worldIn, posXIn, posYIn, posZIn);
		particleMaxAge = 600 + rand.nextInt(100);

		this.particleRed = this.particleGreen = this.particleBlue = 0;
		this.particleScale = 10F;
	}
	
	@Override
	public void onUpdate(){
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.particleAge++;

		if(this.particleAge >= maxAge) {
			this.setExpired();
		}

		this.motionX *= 0.9599999785423279D;
		this.motionY *= 0.9599999785423279D;
		this.motionZ *= 0.9599999785423279D;

		if(this.onGround) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}

		int x = (int)Math.floor(posX) + rand.nextInt(15) - 7;
		int z = (int)Math.floor(posZ) + rand.nextInt(15) - 7;
		int y = world.getHeight(x, z);
		world.spawnParticle(EnumParticleTypes.LAVA, x + rand.nextDouble(), y + 0.1, z + rand.nextDouble(), 0.0, 0.0, 0.0);
	}
	
	@Override
	public int getFXLayer(){
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		float alpha = (float) Math.sin(particleAge * Math.PI / (400F)) * 0.25F;

		GlStateManager.color(1.0F, 1.0F, 1.0F, alpha * 0.1F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderHelper.disableStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.glNormal3f(0, 1, 0);
		
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();

		Random rand = new Random(50);

		for(int i = 0; i < 25; i++) {

			double dX = rand.nextGaussian() * 2.5D;
			double dY = rand.nextGaussian() * 0.15D;
			double dZ = rand.nextGaussian() * 2.5D;
			double size = (rand.nextDouble() * 0.25 + 0.75) * particleScale;

			GlStateManager.translate((float) dX, (float) dY, (float) dZ);

			float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX) + rand.nextGaussian() * 0.5);
			float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY) + rand.nextGaussian() * 0.5);
			float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ) + rand.nextGaussian() * 0.5);

			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buf.pos(pX - rotationX * size - rotationXY * size, pY - rotationZ * size, pZ - rotationYZ * size - rotationXZ * size).tex(1, 1).endVertex();
			buf.pos(pX - rotationX * size + rotationXY * size, pY + rotationZ * size, pZ - rotationYZ * size + rotationXZ * size).tex(1, 0).endVertex();
			buf.pos(pX + rotationX * size + rotationXY * size, pY + rotationZ * size, pZ + rotationYZ * size + rotationXZ * size).tex(0, 0).endVertex();
			buf.pos(pX + rotationX * size - rotationXY * size, pY - rotationZ * size, pZ + rotationYZ * size - rotationXZ * size).tex(0, 1).endVertex();
			tes.draw();

			GlStateManager.translate((float) -dX, (float) -dY, (float) -dZ);
		}

		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
	}

}
