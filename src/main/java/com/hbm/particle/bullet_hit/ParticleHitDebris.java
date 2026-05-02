package com.hbm.particle.bullet_hit;

import com.hbm.particle.ParticleLayerBase;
import com.hbm.particle.ParticleRenderLayer;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class ParticleHitDebris extends ParticleLayerBase {

	public ResourceLocation tex;
	public float rotationOverLifetime;
	public int texIdx;
	
	public ParticleHitDebris(World worldIn, double posXIn, double posYIn, double posZIn, ResourceLocation tex, int idx, float scale, int lifetime) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = 0.5F + worldIn.rand.nextFloat()*1F;
		particleScale *= scale;
		this.particleAngle = (float) (rand.nextFloat()*Math.PI*2);
		rotationOverLifetime = world.rand.nextFloat()*3-1.5F;
		this.prevParticleAngle = this.particleAngle;
		this.tex = tex;
		this.texIdx = idx;
		this.particleMaxAge = lifetime;
		this.particleGravity = 1F;
	}
	
	public ParticleHitDebris color(float r, float g, float b){
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		return this;
	}
	
	public ParticleHitDebris motion(float x, float y, float z){
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		return this;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		this.prevParticleAngle = this.particleAngle;
		this.particleAngle += rotationOverLifetime;
		this.rotationOverLifetime *= onGround ? 0.7F : 0.95F;
		
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
		float size = 0.25F;
        float u = (texIdx%4)*size;
        float v = (texIdx/4)*size;
        
        //Fades it out at the end.
      	this.particleAlpha = 1-MathHelper.clamp((particleAge+partialTicks)-(particleMaxAge-10), 0, 10)*0.1F;
        
        float f4 = 0.1F * this.particleScale;

        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        Vec3d[] avec3d = new Vec3d[] {new Vec3d((double)(-rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double)(-rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(rotationYZ * f4 - rotationXZ * f4))};

        if (this.particleAngle != 0.0F)
        {
            float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
            float f9 = MathHelper.cos(f8 * 0.5F);
            float f10 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.x;
            float f11 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.y;
            float f12 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.z;
            Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

            for (int l = 0; l < 4; ++l)
            {
                avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
            }
        }

        NTMBufferBuilder fastBuffer = (NTMBufferBuilder) buffer;
        int packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
        int packedLightmap = NTMBufferBuilder.packLightmap(j, k);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + (float) avec3d[0].x, f6 + (float) avec3d[0].y, f7 + (float) avec3d[0].z, u + size, v + size, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + (float) avec3d[1].x, f6 + (float) avec3d[1].y, f7 + (float) avec3d[1].z, u + size, v, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + (float) avec3d[2].x, f6 + (float) avec3d[2].y, f7 + (float) avec3d[2].z, u, v, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked(f5 + (float) avec3d[3].x, f6 + (float) avec3d[3].y, f7 + (float) avec3d[3].z, u, v + size, packedColor, packedLightmap);
	}

	@Override
	public ParticleRenderLayer getRenderLayer() {
		ParticleRenderLayer layer = layers.get(tex);
		if(layer == null){
			layer = new ParticleRenderLayerDebris(tex);
			layers.put(tex, layer);
		}
		return layer;
	}
	
	public static Map<ResourceLocation, ParticleRenderLayer> layers = new HashMap<>();
	
	public static class ParticleRenderLayerDebris extends ParticleRenderLayer {
		
		public ResourceLocation texture;
		
		public ParticleRenderLayerDebris(ResourceLocation tex) {
			this.texture = tex;
		}
		
		@Override
		public void preRender() {
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
			//Makes it not pixelated when looking at it up close by using linear interpolation as the magnification filter.
			//GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GlStateManager.enableBlend();
			GlStateManager.enableColorMaterial();
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableLighting();
			net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
			GlStateManager.glNormal3f(0, 1, 0);
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
			GlStateManager.depthMask(false);
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GlStateManager.enableTexture2D();
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			
			NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, particles.size() * 4);
		}
		
		@Override
		public void postRender() {
			NTMImmediate.INSTANCE.draw();
		       
	        GlStateManager.disableBlend();
	        GlStateManager.depthMask(true);
	        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		}
	}
}
