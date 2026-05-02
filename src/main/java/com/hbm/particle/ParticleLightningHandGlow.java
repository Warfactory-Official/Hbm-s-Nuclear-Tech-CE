package com.hbm.particle;

import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
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

public class ParticleLightningHandGlow extends Particle {

	public ParticleLightningHandGlow(World worldIn, double posXIn, double posYIn, double posZIn, float scale, int age) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = scale;
		this.particleMaxAge = age;
	}
	
	public ParticleLightningHandGlow color(float r, float g, float b, float a){
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		this.particleAlpha = a;
		return this;
	}
	
	@Override
	public void onUpdate() {
		this.particleAge ++;
		if(particleAge >= particleMaxAge){
			setExpired();
			return;
		}
	}

	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public boolean shouldDisableDepth() {
		return true;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.pushMatrix();
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		GlStateManager.disableAlpha();
		
		float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks);
	    float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks);
	    float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks);
	    GlStateManager.translate(f5, f6, f7);
	    
	    
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
		ClientProxy.AUX_GL_BUFFER.put(0, 1);
		ClientProxy.AUX_GL_BUFFER.put(1, 0);
		ClientProxy.AUX_GL_BUFFER.put(2, 0);
			
		ClientProxy.AUX_GL_BUFFER.put(4, 0);
		ClientProxy.AUX_GL_BUFFER.put(5, 1);
		ClientProxy.AUX_GL_BUFFER.put(6, 0);
			
		ClientProxy.AUX_GL_BUFFER.put(8, 0);
		ClientProxy.AUX_GL_BUFFER.put(9, 0);
		ClientProxy.AUX_GL_BUFFER.put(10, 1);
			
		GL11.glLoadMatrix(ClientProxy.AUX_GL_BUFFER);

		
		float ageN = (float)(this.particleAge+partialTicks)/(float)this.particleMaxAge;
		float scale = MathHelper.clamp(ageN*2, 0, 1)* MathHelper.clamp(2-ageN*2+0.1F, 0, 1);
		float f4 = 0.1F * this.particleScale * scale;
        
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.fresnel_ms);
		NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, 4);
        int packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
        int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f4, f4, 0, 1, 1, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(-f4, f4, 0, 1, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(-f4, -f4, 0, 0, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f4, -f4, 0, 0, 1, packedColor, packedLightmap);
        NTMImmediate.INSTANCE.draw();
        
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}
	
}
