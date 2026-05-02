package com.hbm.particle.bullet_hit;

import com.hbm.config.GeneralConfig;
import com.hbm.handler.HbmShaderManager2;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.ResourceManager;
import com.hbm.particle.ParticleDecal;
import com.hbm.particle.ParticleLayerBase;
import com.hbm.particle.ParticleRenderLayer;
import com.hbm.render.util.BakedModelUtil;
import com.hbm.render.util.BakedModelUtil.DecalType;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleBloodParticle extends ParticleLayerBase {

	public float rotationOverLifetime;
	public int texIdx;
	public float scaleOverLifetime;
	public float prevScale;
	public boolean doesDecal = true;
	public boolean doesFlow = true;
	
	public ParticleBloodParticle(World worldIn, double posXIn, double posYIn, double posZIn, int idx, float scale, float scaleOverLife, int lifetime) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = 0.5F + worldIn.rand.nextFloat()*1F;
		particleScale *= scale;
		this.particleAngle = (float) (rand.nextFloat()*Math.PI*2);
		rotationOverLifetime = world.rand.nextFloat()*0.3F-0.15F;
		this.prevParticleAngle = this.particleAngle;
		this.texIdx = idx;
		this.particleMaxAge = lifetime;
		this.particleGravity = 1F;
		this.scaleOverLifetime = scaleOverLife;
		prevScale = particleScale;
	}

	public ParticleBloodParticle color(float r, float g, float b){
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		return this;
	}
	
	public ParticleBloodParticle motion(float x, float y, float z){
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		return this;
	}
	
	public ParticleBloodParticle doDecal(boolean dec){
		this.doesDecal = dec;
		return this;
	}
	
	public ParticleBloodParticle doFlow(boolean flow){
		this.doesFlow = flow;
		return this;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		this.motionY -= 0.04F;
		this.prevParticleAngle = this.particleAngle;
		this.particleAngle += rotationOverLifetime;
		this.rotationOverLifetime *= onGround ? 0.7F : 0.95F;
		this.scaleOverLifetime *= 0.97F;
		prevScale = particleScale;
		this.particleScale += scaleOverLifetime;
		if(particleAge*1.5F < particleMaxAge && this.doesDecal){
			RayTraceResult r = world.rayTraceBlocks(new Vec3d(posX, posY, posZ), new Vec3d(posX+motionX, posY+motionY, posZ+motionZ));
			if(r != null && r.typeOfHit == Type.BLOCK){
				Vec3d hit = r.hitVec;
				Vec3d direction = new Vec3d(motionX, motionY, motionZ).normalize();
				if(ParticleDecalFlow.numParticles >= GeneralConfig.flowingDecalAmountMax || !doesFlow){
					int[] dl = BakedModelUtil.generateDecalMesh(world, direction, particleScale*0.1F, (float)hit.x, (float)hit.y, (float)hit.z, DecalType.REGULAR);
					direction = direction.scale(0.001F);
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDecal(world, dl[0], ResourceManager.blood_particles, 120, (float)hit.x-direction.x, (float)hit.y-direction.y, (float)hit.z-direction.z).textureIndex(texIdx, 4).shader(ResourceManager.blood_dissolve));
				} else {
					int[] data = BakedModelUtil.generateDecalMesh(world, direction, particleScale*0.1F, (float)hit.x, (float)hit.y, (float)hit.z, DecalType.FLOW, ResourceManager.blood_particles, texIdx, 4);
					direction = direction.scale(0.001F);
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDecalFlow(world, data, 150, (float)hit.x-direction.x, (float)hit.y-direction.y, (float)hit.z-direction.z).shader(ResourceManager.blood_dissolve));
				}
				float vel = (float) Math.sqrt(motionX*motionX + motionY*motionY + motionZ*motionZ);
				world.playSound(posX, posY, posZ, HBMSoundHandler.blood_splat, SoundCategory.BLOCKS, vel*0.5F, 0.8F+rand.nextFloat()*0.4F, false);
				setExpired();
				return;
			}
		}
		if(this.onGround){
			this.particleAge += 2;
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
		float size = 0.25F;
        float u = (texIdx%4)*size;
        float v = (texIdx/4)*size;
        
        //Fades it out at the end.
      	this.particleAlpha = 1-MathHelper.clamp((particleAge+partialTicks)-(particleMaxAge-10), 0, 10)*0.1F;
        
      	float s = prevScale + (particleScale - prevScale)*partialTicks;
        float f4 = 0.1F * s;

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
	
	private static final ParticleRenderLayer layer = new ParticleRenderLayer(){
		@Override
		public void preRender() {
			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.blood_particles);
			//Makes it not pixelated when looking at it up close by using linear interpolation as the magnification filter.
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			
			GlStateManager.enableColorMaterial();
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableLighting();
			net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
			GlStateManager.glNormal3f(0, 1, 0);
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
			GlStateManager.depthMask(false);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GlStateManager.enableTexture2D();
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			ResourceManager.blood_dissolve.use();
			
			NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, particles.size() * 4);
		}
		@Override
		public void postRender() {
			NTMImmediate.INSTANCE.draw();
		    
			HbmShaderManager2.releaseShader();
	        GlStateManager.disableBlend();
	        GlStateManager.depthMask(true);
	        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		}
	};

}
