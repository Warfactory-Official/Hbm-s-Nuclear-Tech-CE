package com.hbm.particle;

import com.hbm.handler.HbmShaderManager2;
import com.hbm.handler.HbmShaderManager2.Shader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class ParticleDecal extends Particle {

	public ResourceLocation tex;
	public int displayList;
	public int texIdx = -1;
	public int rows;
	public Shader shader;
	
	public ParticleDecal(World worldIn, int dl, ResourceLocation texture, int maxAge, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.displayList = dl;
		this.tex = texture;
		this.particleMaxAge = maxAge;
	}
	
	public ParticleDecal textureIndex(int idx, int rows){
		this.rows = rows;
		this.texIdx = idx;
		return this;
	}
	
	public ParticleDecal shader(Shader shader){
		this.shader = shader;
		return this;
	}
	
	@Override
	public void onUpdate() {
		this.particleAge ++;
		if(this.particleAge > this.particleMaxAge){
			setExpired();
			GlStateManager.glDeleteLists(displayList, 1);
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
		Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		GlStateManager.enableLighting();
		GlStateManager.enableColorMaterial();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enablePolygonOffset();
		float fade = (this.particleAge-particleMaxAge+30+partialTicks) /30F;
		GlStateManager.color(0.5F, 0.1F, 0.1F, 1-fade);
		GlStateManager.doPolygonOffset(-5, -5);
		GlStateManager.callList(displayList);
		GlStateManager.disablePolygonOffset();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();
		
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
