package com.hbm.particle.vortex;

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

public class ParticleVortexCircle extends Particle {

	public float workingAlpha;
	
	public ParticleVortexCircle(World worldIn, double posXIn, double posYIn, double posZIn, float scale) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = scale;
	}
	
	public ParticleVortexCircle color(float colR, float colG, float colB, float colA){
		this.particleRed = colR;
		this.particleGreen = colG;
		this.particleBlue = colB;
		this.particleAlpha = colA;
		workingAlpha = colA;
		return this;
	}
	
	public ParticleVortexCircle lifetime(int lifetime){
		this.particleMaxAge = lifetime;
		return this;
	}
	
	@Override
	public void onUpdate() {
		this.particleAge ++;
		if(this.particleAge >= this.particleMaxAge){
			this.setExpired();
		}
	}
	
	@Override
	public boolean shouldDisableDepth() {
		return true;
	}
	
	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.vortex_beam_circle_2);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		float timeScale = (this.particleAge+partialTicks)/(float)this.particleMaxAge;
		this.workingAlpha = MathHelper.clamp(1-BobMathUtil.remap((float)MathHelper.clamp(timeScale, 0.6, 1), 0.6F, 1F, 0F, 1F), 0, 1)*particleAlpha;
		
		float f4 = 0.1F * (this.particleScale+timeScale*0.5F);
        
        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        float rX = rotationX * f4;
        float rZ = rotationZ * f4;
        float rXY = rotationXY * f4;
        float rYZ = rotationYZ * f4;
        float rXZ = rotationXZ * f4;

        NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, 4);
        int packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, this.workingAlpha);
        int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 - rX - rXY, f6 - rZ, f7 - rYZ - rXZ, 1, 1, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 - rX + rXY, f6 + rZ, f7 - rYZ + rXZ, 1, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + rX + rXY, f6 + rZ, f7 + rYZ + rXZ, 0, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + rX - rXY, f6 - rZ, f7 + rYZ - rXZ, 0, 1, packedColor, packedLightmap);

        NTMImmediate.INSTANCE.draw();
        
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
	}

}
