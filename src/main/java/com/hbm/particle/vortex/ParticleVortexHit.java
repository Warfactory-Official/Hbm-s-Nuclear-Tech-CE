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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleVortexHit extends Particle {

	public float workingAlpha;
	public float rotationOverLife;
	
	public ParticleVortexHit(World worldIn, double posXIn, double posYIn, double posZIn, float scale, float rot) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = scale;
		this.rotationOverLife = (float) Math.toRadians(rot);
	}
	
	public ParticleVortexHit color(float colR, float colG, float colB, float colA){
		this.particleRed = colR;
		this.particleGreen = colG;
		this.particleBlue = colB;
		this.particleAlpha = colA;
		workingAlpha = colA;
		return this;
	}
	
	public ParticleVortexHit lifetime(int lifetime){
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
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.vortex_hit);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		float timeScale = (this.particleAge+partialTicks)/(float)this.particleMaxAge;
		this.workingAlpha = MathHelper.clamp(1-BobMathUtil.remap((float)MathHelper.clamp(timeScale, 0, 1), 0.6F, 1F, 0.6F, 1F), 0, 1)*particleAlpha;
		workingAlpha *= MathHelper.clamp(BobMathUtil.remap((float)MathHelper.clamp(timeScale, 0, 0.2), 0F, 0.2F, 0F, 1F), 0, 1);
		this.particleAngle = timeScale*rotationOverLife;
		
		float f4 = 0.1F * (this.particleScale+timeScale*6F);
        
        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        Vec3d[] avec3d = new Vec3d[] {new Vec3d((double)(-rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double)(-rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(rotationYZ * f4 - rotationXZ * f4))};

        if (this.particleAngle != 0.0F)
        {
            float f9 = MathHelper.cos(particleAngle * 0.5F);
            float f10 = MathHelper.sin(particleAngle * 0.5F) * (float)cameraViewDir.x;
            float f11 = MathHelper.sin(particleAngle * 0.5F) * (float)cameraViewDir.y;
            float f12 = MathHelper.sin(particleAngle * 0.5F) * (float)cameraViewDir.z;
            Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

            for (int l = 0; l < 4; ++l)
            {
                avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
            }
        }
        
        NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, 4);
        int packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, this.workingAlpha);
        int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + (float) avec3d[0].x, f6 + (float) avec3d[0].y, f7 + (float) avec3d[0].z, 1, 1, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + (float) avec3d[1].x, f6 + (float) avec3d[1].y, f7 + (float) avec3d[1].z, 1, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + (float) avec3d[2].x, f6 + (float) avec3d[2].y, f7 + (float) avec3d[2].z, 0, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + (float) avec3d[3].x, f6 + (float) avec3d[3].y, f7 + (float) avec3d[3].z, 0, 1, packedColor, packedLightmap);

        NTMImmediate.INSTANCE.draw();
        
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
	}

}
