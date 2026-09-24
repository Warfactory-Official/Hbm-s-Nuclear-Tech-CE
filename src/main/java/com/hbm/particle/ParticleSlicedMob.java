package com.hbm.particle;

import com.hbm.config.GeneralConfig;
import com.hbm.handler.HbmShaderManager2;
import com.hbm.main.ClientProxy;
import com.hbm.physics.RigidBody;
import com.hbm.render.NTMRenderHelper;
import com.hbm.util.Vec3NT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class ParticleSlicedMob extends Particle {

	public ResourceLocation capTex;
	public ResourceLocation texture;
	public float capBloom;
	public int cutMob;
	public int cap;
	public RigidBody body;
	
	public ParticleSlicedMob(World worldIn, RigidBody body, int cutMob, int cap, ResourceLocation tex, ResourceLocation capTex, float capBloom) {
		super(worldIn, body.globalCentroid.x, body.globalCentroid.y, body.globalCentroid.z);
		this.body = body;
		this.cutMob = cutMob;
		this.cap = cap;
		this.capTex = capTex;
		this.capBloom = capBloom;
		this.texture = tex;
		this.particleMaxAge = 80 + worldIn.rand.nextInt(20);
	}
	
	@Override
	public void onUpdate() {
		body.minecraftTimestep();
		this.posX = body.globalCentroid.x;
		this.posY = body.globalCentroid.y;
		this.posZ = body.globalCentroid.z;
		this.particleAge ++;
		if(particleAge >= particleMaxAge){
			setExpired();
			GlStateManager.glDeleteLists(cutMob, 1);
			GlStateManager.glDeleteLists(cap, 1);
		}
	}
	
	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.pushMatrix();
		GlStateManager.enableCull();
		GlStateManager.enableLighting();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableRescaleNormal();
		net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
		int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, k, j);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		NTMRenderHelper.resetParticleInterpPos(entityIn, partialTicks);
		NTMRenderHelper.resetColor();
		body.doGlTransform(new Vec3NT(interpPosX, interpPosY, interpPosZ), partialTicks);
		GlStateManager.callList(cutMob);
		Minecraft.getMinecraft().getTextureManager().bindTexture(capTex);
		GlStateManager.enablePolygonOffset();
		GlStateManager.doPolygonOffset(-1, -1);
		float lx = OpenGlHelper.lastBrightnessX;
		float ly = OpenGlHelper.lastBrightnessY;
		if(capBloom > 0){
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		}
		GlStateManager.callList(cap);
		
		
		if(capBloom > 0 && GeneralConfig.bloom){
			float[] matrix = new float[16];
			GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
			ClientProxy.AUX_GL_BUFFER.get(matrix);
			ClientProxy.AUX_GL_BUFFER.rewind();
			ClientProxy.deferredRenderers.add(() -> {
				GlStateManager.pushMatrix();
				ClientProxy.AUX_GL_BUFFER.put(matrix);
				ClientProxy.AUX_GL_BUFFER.rewind();
				GL11.glLoadMatrix(ClientProxy.AUX_GL_BUFFER);
				HbmShaderManager2.bloomData.bindFramebuffer(false);
				
				Minecraft.getMinecraft().getTextureManager().bindTexture(capTex);
				GlStateManager.enablePolygonOffset();
				GlStateManager.disableLighting();
				float x = OpenGlHelper.lastBrightnessX;
				float y = OpenGlHelper.lastBrightnessY;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
				GlStateManager.doPolygonOffset(-1, -1);
				GlStateManager.callList(cap);
				GlStateManager.disablePolygonOffset();
				GlStateManager.enableLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, x, y);
				
				Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
				GlStateManager.popMatrix();
			});
		}
		
		if(capBloom > 0){
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lx, ly);
		}
		GlStateManager.disablePolygonOffset();
		GlStateManager.disableRescaleNormal();
		net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		GlStateManager.popMatrix();
	}

}
