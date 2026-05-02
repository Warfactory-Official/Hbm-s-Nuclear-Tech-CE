package com.hbm.particle.tau;

import com.hbm.main.ResourceManager;
import com.hbm.particle.ParticleFirstPerson;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleTauParticleFirstPerson extends ParticleFirstPerson {

	public boolean fadesIn = true;
	
	public ParticleTauParticleFirstPerson(World worldIn, double posXIn, double posYIn, double posZIn, float scale) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = scale;
	}
	
	public ParticleTauParticleFirstPerson color(float colR, float colG, float colB, float colA){
		this.particleRed = colR;
		this.particleGreen = colG;
		this.particleBlue = colB;
		this.particleAlpha = colA;
		return this;
	}
	
	public ParticleTauParticleFirstPerson lifetime(int lifetime){
		this.particleMaxAge = lifetime;
		return this;
	}
	
	public ParticleTauParticleFirstPerson fadeIn(boolean fadeIn){
		fadesIn = fadeIn;
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
		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.fresnel_ms);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		RenderHelper.enableStandardItemLighting();
		float timeScale = (this.particleAge+partialTicks)/(float)this.particleMaxAge;
		float shrink = MathHelper.clamp(1-BobMathUtil.remap((float)MathHelper.clamp(timeScale, 0.5, 1), 0.5F, 1F, 0F, 1.1F), 0, 1);
		if(fadesIn)
			shrink *= MathHelper.clamp(BobMathUtil.remap((float)MathHelper.clamp(timeScale, 0, 0.5), 0F, 0.5F, 0F, 1.1F), 0, 1);
		float workingAlpha = shrink*particleAlpha;
		
		float f4 = 0.1F * this.particleScale*(1-shrink*0.25F);
        
        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks);
        GlStateManager.translate(f5, f6, f7);
        GL11.glScaled(f4, f4, f4);
        
        NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, MathHelper.ceil(workingAlpha) * 4);
        
        while(workingAlpha > 0){
        	int packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, Math.min(workingAlpha, 1));
        	int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
        	fastBuffer.appendParticlePositionTexColorLmapUnchecked(0, 0.5F, 0.5F, 1, 1, packedColor, packedLightmap);
        	fastBuffer.appendParticlePositionTexColorLmapUnchecked(0, 0.5F, -0.5F, 1, 0, packedColor, packedLightmap);
        	fastBuffer.appendParticlePositionTexColorLmapUnchecked(0, -0.5F, -0.5F, 0, 0, packedColor, packedLightmap);
        	fastBuffer.appendParticlePositionTexColorLmapUnchecked(0, -0.5F, 0.5F, 0, 1, packedColor, packedLightmap);
        	workingAlpha -= 1;
        }
        
        NTMImmediate.INSTANCE.draw();
        
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        
        GlStateManager.popMatrix();
	}
	
	@Override
	public ParticleType getType() {
		return ParticleType.TAU;
	}

}
