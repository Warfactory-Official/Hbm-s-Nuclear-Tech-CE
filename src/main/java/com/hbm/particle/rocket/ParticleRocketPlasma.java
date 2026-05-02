package com.hbm.particle.rocket;

import com.hbm.main.ClientProxy;
import com.hbm.particle.ParticleLayerBase;
import com.hbm.particle.ParticleRenderLayer;
import com.hbm.render.misc.ColorGradient;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class ParticleRocketPlasma extends ParticleLayerBase {

	public ColorGradient color;
	
	public ParticleRocketPlasma(World worldIn, double posXIn, double posYIn, double posZIn, float scale, ColorGradient color) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.color = color;
		this.particleMaxAge = 5;
		this.particleScale = scale;
	}

	public ParticleRocketPlasma motion(float mX, float mY, float mZ){
		this.motionX = mX;
		this.motionY = mY;
		this.motionZ = mZ;
		return this;
	}
	
	@Override
	public void onUpdate() {
		this.particleAge++;
		if(this.particleAge >= this.particleMaxAge){
			setExpired();
			return;
		}
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.pushMatrix();
		float timeScale = (this.particleAge+partialTicks)/(float)this.particleMaxAge;
		float[] currentCol = color.getColor(timeScale);
		
		float f4 = (float) (0.1F * this.particleScale * (1-Math.pow(timeScale, 2)));
        
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
        float rX = rotationX * f4;
        float rZ = rotationZ * f4;
        float rXY = rotationXY * f4;
        float rYZ = rotationYZ * f4;
        float rXZ = rotationXZ * f4;

        NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, 4);
        int packedColor = NTMBufferBuilder.packColor(currentCol[0], currentCol[1], currentCol[2], currentCol[3]);
        int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(rX - rXY, -rZ, rYZ - rXZ, 0, 1, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(rX + rXY, rZ, rYZ + rXZ, 0, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rX + rXY, rZ, -rYZ + rXZ, 1, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rX - rXY, -rZ, -rYZ - rXZ, 1, 1, packedColor, packedLightmap);

        NTMImmediate.INSTANCE.draw();
        GlStateManager.popMatrix();
	}
	
	@Override
	public ParticleRenderLayer getRenderLayer() {
		return ParticleRenderLayer.ADDITIVE_FRESNEL;
	}

}
