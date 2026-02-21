package com.hbm.particle;

import com.hbm.Tags;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleBlockDust;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleGiblet extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/particle/meat.png");
	
	private float momentumYaw;
	private float momentumPitch;
	
	public ParticleGiblet(World worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ){
		super(worldIn, posXIn, posYIn, posZIn);
		this.motionX = mX;
		this.motionY = mY;
		this.motionZ = mZ;
		this.particleMaxAge = 140 + rand.nextInt(20);
		this.particleGravity = 2F;

		this.momentumYaw = (float) rand.nextGaussian() * 15F;
		this.momentumPitch = (float) rand.nextGaussian() * 15F;
	}
	
	@Override
	public int getFXLayer(){
		return 3;
	}
	
	@Override
	public void onUpdate(){
		super.onUpdate();

		//this.prevRotationPitch = this.rotationPitch;
		//this.prevRotationYaw = this.rotationYaw;
		
		if(!this.onGround) {
			//this.rotationPitch += this.momentumPitch;
			//this.rotationYaw += this.momentumYaw;
			
			Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, posX, posY, posZ, 0, 0, 0, Block.getStateId(Blocks.REDSTONE_BLOCK.getDefaultState()));
			if (fx == null) return;
			fx.particleMaxAge = 20 + rand.nextInt(20);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}
	
	@Override
	public void renderParticle(BufferBuilder buff, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

		float f10 = this.particleScale * 0.1F;
		float f11 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
		float f12 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
		float f13 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);

		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		GlStateManager.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
		GlStateManager.glNormal3f(0, 1, 0);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos((double) (f11 - rotationX * f10 - rotationXY * f10), (double) (f12 - rotationZ * f10), (double) (f13 - rotationYZ * f10 - rotationXZ * f10)).tex((double) 0, (double) 0).endVertex();
		buf.pos((double) (f11 - rotationX * f10 + rotationXY * f10), (double) (f12 + rotationZ * f10), (double) (f13 - rotationYZ * f10 + rotationXZ * f10)).tex((double) 0, (double) 1).endVertex();
		buf.pos((double) (f11 + rotationX * f10 + rotationXY * f10), (double) (f12 + rotationZ * f10), (double) (f13 + rotationYZ * f10 + rotationXZ * f10)).tex((double) 1, (double) 1).endVertex();
		buf.pos((double) (f11 + rotationX * f10 - rotationXY * f10), (double) (f12 - rotationZ * f10), (double) (f13 + rotationYZ * f10 - rotationXZ * f10)).tex((double) 1, (double) 0).endVertex();
		tes.draw();
		GlStateManager.popMatrix();
	}

}
