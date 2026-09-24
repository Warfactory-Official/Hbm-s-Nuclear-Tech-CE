package com.hbm.particle.bullet_hit;

import com.hbm.handler.HbmShaderManager2;
import com.hbm.handler.HbmShaderManager2.Shader;
import com.hbm.main.ResourceManager;
import com.hbm.render.GLCompat;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class ParticleDecalFlow extends Particle {

	public static int numParticles = 0;
	
	//Containing: Geometry display list, gravmap framebuffer 1, gravmap texture 1, gravmap framebuffer 2, gravmap texture 2, width, height
	int[] data;
	public boolean pong;
	public int texIdx = -1;
	public int rows;
	public Shader shader;
	
	public ParticleDecalFlow(World worldIn, int[] data, int maxAge, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.data = data;
		this.particleMaxAge = maxAge;
		numParticles ++;
	}
	
	public ParticleDecalFlow textureIndex(int idx, int rows){
		this.rows = rows;
		this.texIdx = idx;
		return this;
	}
	
	public ParticleDecalFlow shader(Shader shader){
		this.shader = shader;
		return this;
	}
	
	@Override
	public void onUpdate() {
		this.particleAge ++;
		//Flow update
		GlStateManager.viewport(0, 0, data[5], data[6]);
		ResourceManager.blood_flow_update.use();
		ResourceManager.blood_flow_update.uniform2f("size", data[5], data[6]);
		ResourceManager.blood_flow_update.uniform1f("cutoff", 0.05F);
		for(int i = 0; i < 2; i ++){
			GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, data[pong ? 3 : 1]);
			GlStateManager.bindTexture(data[pong ? 2 : 4]);
			NTMRenderHelper.renderFullscreenTriangle();
			pong = !pong;
		}
		GlStateManager.disableLighting();
		HbmShaderManager2.releaseShader();
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
		
		if(this.particleAge > this.particleMaxAge){
			numParticles --;
			setExpired();
			GlStateManager.glDeleteLists(data[0], 1);
			GlStateManager.deleteTexture(data[2]);
			GLCompat.deleteFramebuffers(data[1]);
			GlStateManager.deleteTexture(data[4]);
			GLCompat.deleteFramebuffers(data[3]);
		}
	}
	
	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		
		if(texIdx != -1){
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			float size = 1F/rows;
	        float u = (texIdx%rows)*size;
	        float v = (texIdx/4)*size;
	        GlStateManager.translate(u, v, 0);
	        GlStateManager.scale(size, size, 1);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}
		
		if(shader != null){
			shader.use();
		}
		
		int i = this.getBrightnessForRender(partialTicks);
		int j = i >> 16 & 65535;
        int k = i & 65535;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, k, j);
		GlStateManager.pushMatrix();
		double entPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX)*partialTicks;
        double entPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY)*partialTicks;
        double entPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ)*partialTicks;
		GlStateManager.translate(posX-entPosX, posY-entPosY, posZ-entPosZ);
		GlStateManager.bindTexture(data[pong ? 4 : 2]);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		GlStateManager.enableLighting();
		GlStateManager.enableColorMaterial();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enablePolygonOffset();
		float fade = (float)(this.particleAge-particleMaxAge+30+partialTicks)/30F;
		GlStateManager.color(0.5F, 0.1F, 0.1F, 1-fade);
		GlStateManager.doPolygonOffset(-5, -5);
		GlStateManager.callList(data[0]);
		GlStateManager.disablePolygonOffset();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.turbofan_blades_tex);
		
		if(texIdx != -1){
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}
		if(shader != null){
			HbmShaderManager2.releaseShader();
		}
	}

}