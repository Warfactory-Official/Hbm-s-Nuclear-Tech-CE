package com.hbm.particle.gluon;

import com.hbm.main.ClientProxy;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class ParticleGluonParticle extends Particle {

	float workingAlpha;
	
	protected ParticleGluonParticle(World worldIn, double posXIn, double posYIn, double posZIn, float scale, int maxAge) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleMaxAge = maxAge;
		this.particleScale = scale;
		this.particleRed = 0.4F;
		this.particleGreen = 0.7F;
	}

	@Override
	public void onUpdate() {
		this.particleAge++;
		if(particleAge >= particleMaxAge){
			this.setExpired();
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
	
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ, FloatBuffer mat) {
		GlStateManager.pushMatrix();
		float timeScale = (this.particleAge+partialTicks)/(float)this.particleMaxAge;
		float shrink = MathHelper.clamp(1-BobMathUtil.remap((float)MathHelper.clamp(timeScale, 0, 1), 0.6F, 1F, 0.6F, 1F), 0, 1);
		this.workingAlpha = shrink*particleAlpha*0.9F;
		
		float f4 = 0.1F * (this.particleScale+shrink*particleScale*4);
        
        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks);
        
        GlStateManager.translate(f5, f6, f7);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER2);
		ClientProxy.AUX_GL_BUFFER2.rewind();
		float[] trans = new float[3];
		ClientProxy.AUX_GL_BUFFER2.position(12);
		ClientProxy.AUX_GL_BUFFER2.get(trans);
		ClientProxy.AUX_GL_BUFFER2.rewind();
		mat.position(12);
		mat.put(trans);
		mat.rewind();
		GL11.glLoadMatrix(mat);
		
        float rX = rotationX * f4;
        float rZ = rotationZ * f4;
        float rXY = rotationXY * f4;
        float rYZ = rotationYZ * f4;
        float rXZ = rotationXZ * f4;
        //I can't figure out a way to batch these particles without screwing up the hacky rotation fixes I'm doing.
        NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, 4);
        int packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, this.workingAlpha);
        int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rX - rXY, -rZ, -rYZ - rXZ, 1, 1, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(-rX + rXY, rZ, -rYZ + rXZ, 1, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(rX + rXY, rZ, rYZ + rXZ, 0, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(rX - rXY, -rZ, rYZ - rXZ, 0, 1, packedColor, packedLightmap);
        NTMImmediate.INSTANCE.draw();
        GlStateManager.popMatrix();
	}
	
}
