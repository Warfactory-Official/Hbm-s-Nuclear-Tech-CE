package com.hbm.particle;

import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import com.hbm.render.misc.LensVisibilityHandler;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class ParticleFakeBrightness extends Particle {

	int visibilityId = -1;
	boolean local;
	public float fadeInKoeff = 2;
	
	public ParticleFakeBrightness(World worldIn, double posXIn, double posYIn, double posZIn, float scale, int age) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = scale;
		this.particleMaxAge = age;
	}
	
	public ParticleFakeBrightness color(float r, float g, float b, float a){
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		this.particleAlpha = a;
		return this;
	}
	
	public ParticleFakeBrightness enableLocalSpaceCorrection(){
		local = true;
		return this;
	}
	
	@Override
	public void onUpdate() {
		this.particleAge ++;
		if(particleAge >= particleMaxAge){
			setExpired();
			LensVisibilityHandler.delete(visibilityId);
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
		
        if(local){
			float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks);
	        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks);
	        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks);
	        GlStateManager.translate(f5, f6, f7);
			FloatBuffer view_mat = ActiveRenderInfo.MODELVIEW;
			view_mat.rewind();
			GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
			for(int i = 0; i < 12; i ++){
				ClientProxy.AUX_GL_BUFFER.put(i, view_mat.get(i));
			}
			ClientProxy.AUX_GL_BUFFER.rewind();
			GL11.glLoadMatrix(ClientProxy.AUX_GL_BUFFER);
        } else {
        	double entPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX)*partialTicks;
            double entPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY)*partialTicks;
            double entPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ)*partialTicks;
            
            interpPosX = entPosX;
            interpPosY = entPosY;
            interpPosZ = entPosZ;
        	float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
            float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
            float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
            GlStateManager.translate(f5, f6, f7);
        }
		if(visibilityId == -1){
			GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
			visibilityId = LensVisibilityHandler.generate(ClientProxy.AUX_GL_BUFFER);
		}
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
		LensVisibilityHandler.putMatrixBuf(visibilityId, ClientProxy.AUX_GL_BUFFER);
		
		float visibility = LensVisibilityHandler.getVisibility(visibilityId);
		visibility *= visibility;
		
		float ageN = (float)(this.particleAge+partialTicks)/(float)this.particleMaxAge;
		float scale = MathHelper.clamp(ageN*fadeInKoeff, 0, 1)* MathHelper.clamp(2-ageN*fadeInKoeff+0.1F, 0, 1);
		float f4 = 0.1F * this.particleScale * visibility*scale;
        
        float rX = rotationX * f4;
        float rZ = rotationZ * f4;
        float rXY = rotationXY * f4;
        float rYZ = rotationYZ * f4;
        float rXZ = rotationXZ * f4;
        if(!local){
        	GlStateManager.enableBlend();
        	GlStateManager.disableAlpha();
        	GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        }
        
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.fresnel_ms);
		NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, 4);
		int packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha*visibility);
		int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rX - rXY, -rZ, -rYZ - rXZ, 1, 1, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rX + rXY, rZ, -rYZ + rXZ, 1, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(rX + rXY, rZ, rYZ + rXZ, 0, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(rX - rXY, -rZ, rYZ - rXZ, 0, 1, packedColor, packedLightmap);
        NTMImmediate.INSTANCE.draw();
        
        if(!local){
        	GlStateManager.disableBlend();
        	GlStateManager.enableAlpha();
        }
		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}
}
