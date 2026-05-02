package com.hbm.particle.bullet_hit;

import com.hbm.config.GeneralConfig;
import com.hbm.handler.HbmShaderManager2;
import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import com.hbm.particle.ParticleLayerBase;
import com.hbm.particle.ParticleRenderLayer;
import com.hbm.render.GLCompat;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.util.BakedModelUtil;
import com.hbm.render.util.BakedModelUtil.DecalType;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

public class ParticleBulletImpact extends ParticleLayerBase {

	float yaw;
	float pitch;
	float roll;
	Vec3d norm;
	Vec3d[] vertices;
	int[] vbo;
	
	public ParticleBulletImpact(World worldIn, double posXIn, double posYIn, double posZIn, float scale, int maxAge, Vec3d normal) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = scale;
		this.particleMaxAge = maxAge;
		Vec3d angles = BobMathUtil.getEulerAngles(normal);
		yaw = (float) angles.x;
		pitch = (float) angles.y;
		roll = worldIn.rand.nextFloat()*360;
		this.norm = normal;
		this.particleRed = 1F;
		this.particleGreen = 1F;
		this.particleBlue = 1F;
		
		vertices = new Vec3d[4];
		if(GeneralConfig.bulletHoleNormalMapping){
			vertices[0] = new Vec3d(-0.5, 0, -0.5);
			vertices[1] = new Vec3d(0.5, 0, -0.5);
			vertices[2] = new Vec3d(0.5, 0, 0.5);
			vertices[3] = new Vec3d(-0.5, 0, 0.5);
			vbo = BakedModelUtil.generateDecalMesh(worldIn, normal.scale(-1), scale, (float)posX, (float)posY, (float)posZ, DecalType.VBO);
		} else {
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			GL11.glRotated(yaw, 0, 1, 0);
		    GL11.glRotated(pitch, 1, 0, 0);
		    GL11.glRotated(roll, 0, 1, 0);
		    GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
		    Matrix4f mat = new Matrix4f();
		    mat.load(ClientProxy.AUX_GL_BUFFER);
		    ClientProxy.AUX_GL_BUFFER.rewind();
			GlStateManager.popMatrix();
			Vector4f out = new Vector4f();
			Matrix4f.transform(mat, new Vector4f(-0.5F, 0, -0.5F, 1), out);
			vertices[0] = new Vec3d(out.x, out.y, out.z);
			Matrix4f.transform(mat, new Vector4f(0.5F, 0, -0.5F, 1), out);
			vertices[1] = new Vec3d(out.x, out.y, out.z);
			Matrix4f.transform(mat, new Vector4f(0.5F, 0, 0.5F, 1), out);
			vertices[2] = new Vec3d(out.x, out.y, out.z);
			Matrix4f.transform(mat, new Vector4f(-0.5F, 0, 0.5F, 1), out);
			vertices[3] = new Vec3d(out.x, out.y, out.z);
		}
		
	}
	
	public ParticleBulletImpact color(float r, float g, float b){
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
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
	public boolean shouldDisableDepth() {
		return true;
	}
	
	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if(GeneralConfig.bulletHoleNormalMapping && vbo == null)
			return;
		float fade = 1-MathHelper.clamp((particleAge+partialTicks)-(particleMaxAge-10), 0, 10)*0.1F;
		this.particleAlpha = fade;
		
        double entPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX)*partialTicks;
        double entPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY)*partialTicks;
        double entPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ)*partialTicks;
        
        interpPosX = entPosX;
        interpPosY = entPosY;
        interpPosZ = entPosZ;
        
		float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - entPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - entPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - entPosZ);
        
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        
        float scale = particleScale;
        if(GeneralConfig.bulletHoleNormalMapping){
        	GlStateManager.pushMatrix();
        	GlStateManager.translate(f5, f6, f7);
        	GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, vbo[0]);
        	BakedModelUtil.enableBlockShaderVBOs();
        	GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vbo[1]);
        	BakedModelUtil.disableBlockShaderVBOs();
        	//GL11.glCallList(vbo[0]);
        	GlStateManager.popMatrix();
        } else {
        	scale *= 2;
        	NTMBufferBuilder fastBuffer = (NTMBufferBuilder) buffer;
            int packedColor = NTMBufferBuilder.packColor(particleRed, particleGreen, particleBlue, particleAlpha);
            int packedLightmap = NTMBufferBuilder.packLightmap(j, k);
        	fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) vertices[0].x * scale + f5, (float) vertices[0].y * scale + f6, (float) vertices[0].z * scale + f7, 0, 0, packedColor, packedLightmap);
            fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) vertices[1].x * scale + f5, (float) vertices[1].y * scale + f6, (float) vertices[1].z * scale + f7, 1, 0, packedColor, packedLightmap);
            fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) vertices[2].x * scale + f5, (float) vertices[2].y * scale + f6, (float) vertices[2].z * scale + f7, 1, 1, packedColor, packedLightmap);
            fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) vertices[3].x * scale + f5, (float) vertices[3].y * scale + f6, (float) vertices[3].z * scale + f7, 0, 1, packedColor, packedLightmap);
        }
	}

	@Override
	public ParticleRenderLayer getRenderLayer() {
		return layer;
	}
	
	public static ParticleRenderLayer layer = new ParticleRenderLayer(){
		@Override
		public void preRender() {
			GlStateManager.enableColorMaterial();
			GlStateManager.enableRescaleNormal();
			net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
			GlStateManager.glNormal3f(0, 0, -1F);
			GlStateManager.disableCull();
			NTMRenderHelper.resetColor();
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
			GlStateManager.depthMask(false);
			GlStateManager.enablePolygonOffset();
			GlStateManager.doPolygonOffset(-4, -4);
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.bullet_impact);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			Minecraft.getMinecraft().entityRenderer.enableLightmap();
			
			if(GeneralConfig.bulletHoleNormalMapping){
				Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				ResourceManager.bimpact.use();
				GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+3);
				Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.bullet_impact_normal);
				GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+4);
				Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.bullet_impact_occlusion);
				GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+5);
				GlStateManager.bindTexture(HbmShaderManager2.depthTexture);
				GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
				ResourceManager.bimpact.uniform1i("normalMap", 3);
				ResourceManager.bimpact.uniform1i("occlusionMap", 4);
				ResourceManager.bimpact.uniform1i("depthBuffer", 5);
			} else {
				NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, particles.size() * 4);
			}
			
		}
		@Override
		public void postRender() {
			if(GeneralConfig.bulletHoleNormalMapping){
				HbmShaderManager2.releaseShader();
				GLCompat.bindBuffer(GLCompat.GL_ARRAY_BUFFER, 0);
			} else {
				NTMImmediate.INSTANCE.draw();
			}
			
			GlStateManager.disablePolygonOffset();
			NTMRenderHelper.resetColor();
			GlStateManager.enableCull();
			GlStateManager.enableAlpha();
			GlStateManager.depthMask(true);
			GlStateManager.disableBlend();
		}
	};
}
