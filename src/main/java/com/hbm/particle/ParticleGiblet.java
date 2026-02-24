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
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class ParticleGiblet extends Particle {

	private static final ResourceLocation textureMeat = new ResourceLocation(Tags.MODID + ":textures/particle/meat.png");
	private static final ResourceLocation textureSlime = new ResourceLocation(Tags.MODID + ":textures/particle/slime.png");
	private static final ResourceLocation textureMetal = new ResourceLocation(Tags.MODID + ":textures/particle/metal.png");
	
	private final float momentumYaw;
	private final float momentumPitch;
	private final int gibType;
	
	public ParticleGiblet(World worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ, int gibType){
		super(worldIn, posXIn, posYIn, posZIn);
		this.motionX = mX;
		this.motionY = mY;
		this.motionZ = mZ;
		this.particleMaxAge = 140 + rand.nextInt(20);
		this.particleGravity = 2F;
		this.gibType = gibType;

		if(gibType == 2) this.particleGravity *= 2;

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

			if(gibType == 2) return;
			
			Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, posX, posY, posZ, 0, 0, 0, gibType == 1 ? Block.getStateId(Blocks.MELON_BLOCK.getDefaultState()) : Block.getStateId(Blocks.REDSTONE_BLOCK.getDefaultState()) );
			if (fx == null) return;
			fx.particleMaxAge = 20 + rand.nextInt(20);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}
	
	@Override
	public void renderParticle(@NotNull BufferBuilder buff, @NotNull Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		Minecraft.getMinecraft().getTextureManager().bindTexture(gibType == 2 ? textureMetal : gibType == 1 ? textureSlime : textureMeat);

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
		buf.pos(f11 - rotationX * f10 - rotationXY * f10, f12 - rotationZ * f10, f13 - rotationYZ * f10 - rotationXZ * f10).tex(0, 0).endVertex();
		buf.pos(f11 - rotationX * f10 + rotationXY * f10, f12 + rotationZ * f10, f13 - rotationYZ * f10 + rotationXZ * f10).tex(0, 1).endVertex();
		buf.pos(f11 + rotationX * f10 + rotationXY * f10, f12 + rotationZ * f10, f13 + rotationYZ * f10 + rotationXZ * f10).tex(1, 1).endVertex();
		buf.pos(f11 + rotationX * f10 - rotationXY * f10, f12 - rotationZ * f10, f13 + rotationYZ * f10 - rotationXZ * f10).tex(1, 0).endVertex();
		tes.draw();
		GlStateManager.popMatrix();
	}

}
