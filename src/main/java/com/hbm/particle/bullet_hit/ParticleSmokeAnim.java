package com.hbm.particle.bullet_hit;

import com.hbm.main.ResourceManager;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleSmokeAnim extends ParticleLayerBase {
	
	public float speed;
	public int offset;
	public float scaleOverLife;
	public float prevScale;
	public float gravity;
	public float opacity;
	
	public ParticleSmokeAnim(World worldIn, double posXIn, double posYIn, double posZIn, float speed, float scale, float scaleOverLife, int lifetime) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = scale;
		prevScale = scale;
		this.particleAngle = (float) (rand.nextFloat()*Math.PI*2);
		this.prevParticleAngle = this.particleAngle;
		this.speed = speed;
		this.particleMaxAge = lifetime*2;
		this.particleGravity = 0;
		gravity = 0.04F;
		this.offset = rand.nextInt(81);
		this.scaleOverLife = scaleOverLife;
		opacity = 1;
	}
	
	public ParticleSmokeAnim color(float r, float g, float b){
		return color(r, g, b, 1);
	}
	
	public ParticleSmokeAnim color(float r, float g, float b, float a){
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		this.opacity = a;
		return this;
	}
	
	public ParticleSmokeAnim motion(float x, float y, float z){
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		return this;
	}
	
	public ParticleSmokeAnim gravity(float g){
		this.gravity = g;
		return this;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		this.motionX *= 0.9F;
		this.motionY *= 0.9F;
		this.motionZ *= 0.9F;
		this.motionY -= gravity;
		prevScale = particleScale;
		particleScale += scaleOverLife;
		scaleOverLife *= 0.95F;
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
		float fade = MathHelper.clamp((particleAge+partialTicks)/particleMaxAge, 0, 1);
		fade = (float) (1-Math.log10(fade*9+1));
		this.particleAlpha = fade*0.3F*opacity;
		int index = (int) ((this.particleAge+partialTicks)*25F*speed+offset) % 81;
		float size = 1/9F;
        float u = (index%9)*size;
        float v = (index/9)*size;
        
        float sc = prevScale + (particleScale-prevScale)*partialTicks;
        float f4 = 0.1F * sc;

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
		return layer;
	}
	
	public static ParticleRenderLayer layer = new ParticleRenderLayer(){
		@Override
		public void preRender() {
			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.smoke_anim0);
			//Makes it not pixelated when looking at it up close by using linear interpolation as the magnification filter.
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GlStateManager.enableColorMaterial();
			GlStateManager.enableRescaleNormal();
			net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
			GlStateManager.glNormal3f(0, 1, 0);
			GlStateManager.enableBlend();
			GlStateManager.enableLighting();
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
	};
	
}
