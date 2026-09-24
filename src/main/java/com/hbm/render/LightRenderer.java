package com.hbm.render;

import com.hbm.config.GeneralConfig;
import com.hbm.handler.HbmShaderManager2;
import com.hbm.handler.HbmShaderManager2.Shader;
import com.hbm.lib.Library;
import com.hbm.main.ClientProxy;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.Project;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;
import java.util.*;

public class LightRenderer {

	public static final int MAX_DIRECTIONAL_LIGHTS = 4;
	public static final int MAX_POINT_LIGHTS = 4;

	
	private static Set<Class<?>> blacklistedObjects = new HashSet<>();

	private static List<DirectionalLight> flashlights = new ArrayList<>();
	private static List<PointLight> point_lights = new ArrayList<>();
	
	public static int width;
	public static int height;

	public static int albedoFbo = -1;
	public static int albedoTex = -1;
	public static int albedoDepth = -1;
	
	public static int lightAccFbo = -1;
	public static int lightAccTex = -1;
	
	public static int volAccFbo = -1;
	public static int volAccTex = -1;
	
	public static int shadowFbo = -1;
	public static int shadowTex = -1;
	
	public static boolean init = false;
	
	private static boolean lock = false;

	public static void addPointLight(Vec3d pos, float energy){
		addPointLight(pos, new Vec3d(1, 1, 1), energy);
	}
	
	public static void addPointLight(Vec3d pos, Vec3d color, float energy){
		addPointLight(new PointLight(pos, color, energy));
	}
	public static void addPointLight(PointLight light){
		if(!GeneralConfig.flashlight || lock || point_lights.size() > MAX_POINT_LIGHTS)
			return;
		point_lights.add(light);
	}
	
	public static void addFlashlight(Vec3d start, Vec3d end, float degrees, float brightness, ResourceLocation cookie) {
		addFlashlight(start, end, degrees, brightness, cookie, false, false);
	}

	public static void addFlashlight(Vec3d start, Vec3d end, float degrees, float brightness, ResourceLocation cookie, boolean ent, boolean tes) {
		addFlashlight(start, end, degrees, brightness, cookie, true, true, ent, tes);
	}

	public static void addFlashlight(Vec3d start, Vec3d end, float degrees, float brightness, ResourceLocation cookie, boolean vol, boolean sha, boolean ent, boolean tes) {
		if(!GeneralConfig.flashlight || lock || flashlights.size() > MAX_DIRECTIONAL_LIGHTS)
			return;
		flashlights.add(new DirectionalLight(start, end, degrees, brightness, cookie, vol, sha, ent, tes));
	}

	public static void worldRender() {
		if(lock)
			return;
		if(!GeneralConfig.flashlight || (flashlights.isEmpty() && point_lights.isEmpty()))
			return;
		lock = true;
		//long lTime = System.nanoTime();
		Framebuffer mcFbo = Minecraft.getMinecraft().getFramebuffer();
		if(width != mcFbo.framebufferWidth || height != mcFbo.framebufferHeight) {
			width = mcFbo.framebufferWidth;
			height = mcFbo.framebufferHeight;
			recreateBuffers();
		}
		clearAlbedoBuffer();
		GlStateManager.color(1, 1, 1, 1);
		float partialTicks = MainRegistry.proxy.partialTicks();
		Entity renderView = Minecraft.getMinecraft().getRenderViewEntity();
		Set<RenderChunk> chunksToRender = new HashSet<>();
		Set<Entity> entitiesToRender = new HashSet<>();
		Set<TileEntity> tilesToRender = new HashSet<>();
		for(DirectionalLight l : flashlights){
			accumulateRenderObjects(l);
			chunksToRender.addAll(l.chunksToRender);
			if(l.entities){
				entitiesToRender.addAll(l.entsToRender);
			}
			if(l.tileentities){
				tilesToRender.addAll(l.tilesToRender);
			}
		}
		for(PointLight p : point_lights){
			accumulateRenderObjects(p);
			chunksToRender.addAll(p.chunksToRender);
			if(p.entities){
				entitiesToRender.addAll(p.entsToRender);
			}
			if(p.tileentities){
				tilesToRender.addAll(p.tilesToRender);
			}
		}
		
		double entPosX = renderView.lastTickPosX + (renderView.posX - renderView.lastTickPosX) * partialTicks;
		double entPosY = renderView.lastTickPosY + (renderView.posY - renderView.lastTickPosY) * partialTicks;
		double entPosZ = renderView.lastTickPosZ + (renderView.posZ - renderView.lastTickPosZ) * partialTicks;
		Vec3d playerPos = new Vec3d(entPosX, entPosY, entPosZ);
		
		GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, albedoFbo);
		renderObjects(playerPos, chunksToRender, entitiesToRender, tilesToRender, ResourceManager.albedo, partialTicks);
		
		if(!init){
			initShadowBuffer();
			init = true;
		}
		clearAccumulationBuffer();
		boolean didRenderVolume = false;
		for(DirectionalLight l : flashlights){
			l.setupViewProjectionMatrix(playerPos);
			//Render Shadows
			if(l.shadows){
				GlStateManager.viewport(0, 0, 1024, 1024);
				clearShadowBuffer();
				GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, shadowFbo);
				//GlStateManager.viewport(0, 0, 1024, 1024);
				ResourceManager.flashlight_depth.use();
				renderShadowForLight(playerPos, l, partialTicks);
			}
			
			//Render the light into the accumulation buffer
			GlStateManager.viewport(0, 0, width, height);
			ResourceManager.flashlight_post.use();
			sendPostShaderUniforms(l, playerPos, ResourceManager.flashlight_post);
			GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, lightAccFbo);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ONE);
			Vec3d vec = l.end.subtract(l.start).normalize();
			if(playerPos.add(ActiveRenderInfo.getCameraPosition()).subtract(l.start).normalize().dotProduct(vec) < Math.cos(Math.toRadians(l.degrees+10))){
				GlStateManager.enableCull();
				NTMRenderHelper.renderConeMesh(l.start.subtract(playerPos), vec, (float) l.height, (float)l.radius*1.1F, 12);
			} else {
				GlStateManager.pushMatrix();
				GlStateManager.loadIdentity();
				GlStateManager.matrixMode(GL11.GL_PROJECTION);
				GlStateManager.pushMatrix();
				GlStateManager.loadIdentity();
				GlStateManager.matrixMode(GL11.GL_MODELVIEW);
				GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
				NTMRenderHelper.renderFullscreenTriangle();
				GlStateManager.matrixMode(GL11.GL_PROJECTION);
				GlStateManager.popMatrix();
				GlStateManager.matrixMode(GL11.GL_MODELVIEW);
				GlStateManager.popMatrix();
			}
			
			//Render volume
			if(GeneralConfig.flashlightVolumetric && l.volume){
				GlStateManager.viewport(0, 0, width/2, height/2);
				GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, volAccFbo);
				volumetricRender(l, playerPos);
				GlStateManager.viewport(0, 0, width, height);
				didRenderVolume = true;
			}
			GlStateManager.disableBlend();
		}
		for(PointLight light : point_lights){
			ResourceManager.pointlight_post.use();
			sendPointLightUniforms(light, playerPos, ResourceManager.pointlight_post);
			GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, lightAccFbo);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ONE);
			float rad = light.radius + 0.1F;
			if(playerPos.add(ActiveRenderInfo.getCameraPosition()).squareDistanceTo(light.pos) > rad*rad){
				Vec3d diff = light.pos.subtract(playerPos);
				GlStateManager.pushMatrix();
				GlStateManager.translate(diff.x, diff.y, diff.z);
				GlStateManager.scale(light.radius, light.radius, light.radius);
				ResourceManager.sphere_uv.renderAll();
				GlStateManager.popMatrix();
			} else {
				GlStateManager.pushMatrix();
				GlStateManager.loadIdentity();
				GlStateManager.matrixMode(GL11.GL_PROJECTION);
				GlStateManager.pushMatrix();
				GlStateManager.loadIdentity();
				GlStateManager.matrixMode(GL11.GL_MODELVIEW);
				GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
				NTMRenderHelper.renderFullscreenTriangle();
				GlStateManager.matrixMode(GL11.GL_PROJECTION);
				GlStateManager.popMatrix();
				GlStateManager.matrixMode(GL11.GL_MODELVIEW);
				GlStateManager.popMatrix();
			}
			GlStateManager.disableBlend();
		}
		
		if(GeneralConfig.flashlightVolumetric && didRenderVolume){
			//Blit volume data to light data
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ONE);
			GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, lightAccFbo);
			ResourceManager.volume_upscale.use();
			GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+3);
			GlStateManager.bindTexture(HbmShaderManager2.depthTexture);
			GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
			ResourceManager.volume_upscale.uniform1i("depthTex", 3);
			GlStateManager.bindTexture(volAccTex);
			ResourceManager.volume_upscale.uniform2f("zNearFar", 0.05F, Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16 * MathHelper.SQRT_2);
			NTMRenderHelper.renderFullscreenTriangle();
			GlStateManager.disableBlend();
		}
		
		//Blit light data to minecraft with gamma correction
		GlStateManager.bindTexture(lightAccTex);
		mcFbo.bindFramebuffer(true);
		ResourceManager.flashlight_blit.use();
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+3);
		GlStateManager.bindTexture(mcFbo.framebufferTexture);
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
		ResourceManager.flashlight_blit.uniform1i("target", 3);
		NTMRenderHelper.renderFullscreenTriangle();
		
		HbmShaderManager2.releaseShader();
		GlStateManager.color(1, 1, 1, 1);
		
		flashlights.clear();
		point_lights.clear();
		lock = false;
		//System.out.println(System.nanoTime()-lTime);
	}

	private static void volumetricRender(DirectionalLight light, Vec3d playerPos){
		Vec3d vec = light.end.subtract(light.start);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		ResourceManager.cone_volume.use();
		Shader shader = ResourceManager.cone_volume;
		float height = (float) vec.length();
		shader.uniform1f("height", height);
		vec = vec.normalize();
		shader.uniform1f("cosAngle", (float) Math.cos(Math.toRadians(light.degrees)));
		Vec3d pos = light.start.subtract(playerPos);
		shader.uniform3f("pos", (float) pos.x, (float) pos.y, (float) pos.z);
		shader.uniform3f("direction", (float) vec.x, (float) vec.y, (float) vec.z);
		shader.uniform1f("radius", (float) light.radius * 0.5F);
		shader.uniform1f("useShadows", light.shadows ? 1 : 0);
	
		Vec3d camPos = ActiveRenderInfo.getCameraPosition();
		shader.uniform3f("camPos", (float) camPos.x, (float) camPos.y, (float) camPos.z);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(light.cookie);
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+3);
		GlStateManager.bindTexture(shadowTex);
		shader.uniform1i("shadow", 3);
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+4);
		GlStateManager.bindTexture(HbmShaderManager2.depthTexture);
		shader.uniform1i("depth", 4);
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
		
		ClientProxy.AUX_GL_BUFFER.put(HbmShaderManager2.inv_ViewProjectionMatrix);
		ClientProxy.AUX_GL_BUFFER.rewind();
		shader.uniformMatrix4("inv_ViewProjectionMatrix", false, ClientProxy.AUX_GL_BUFFER);
		
		light.viewProjectionMatrix.store(ClientProxy.AUX_GL_BUFFER);
		ClientProxy.AUX_GL_BUFFER.rewind();
		shader.uniformMatrix4("flashlight_ViewProjectionMatrix", false, ClientProxy.AUX_GL_BUFFER);
		
		GlStateManager.enableCull();
		//renderConeMesh(pos, vec, height, radius, 8);
		if(playerPos.add(ActiveRenderInfo.getCameraPosition()).subtract(light.start).normalize().dotProduct(vec) < Math.cos(Math.toRadians(light.degrees+10))){
			NTMRenderHelper.renderConeMesh(light.start.subtract(playerPos), vec, height, (float)light.radius*1.1F, 12);
		} else {
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			NTMRenderHelper.renderFullscreenTriangle();
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			GlStateManager.popMatrix();
		}
		
		HbmShaderManager2.releaseShader();
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableBlend();
	}
	
	private static void renderObjects(Vec3d playerPos, Collection<RenderChunk> chunksToRender, Collection<Entity> entitiesToRender, Collection<TileEntity> tilesToRender, Shader shader, float partialTicks) {
		NTMRenderHelper.bindBlockTexture();
		GlStateManager.enableTexture2D();
		shader.use();
		NTMRenderHelper.enableBlockVBOs();
		NTMRenderHelper.renderChunks(chunksToRender, playerPos.x, playerPos.y, playerPos.z);
		NTMRenderHelper.disableBlockVBOs();
		OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);

		for(Entity ent : entitiesToRender){
        	Minecraft.getMinecraft().getRenderManager().renderEntityStatic(ent, partialTicks, false);
        	if(GlStateManager.glGetInteger(GLCompat.GL_CURRENT_PROGRAM) != shader.getShaderId()){
        		blacklistedObjects.add(ent.getClass());
        		shader.use();
        	}
        }
        for(TileEntity te : tilesToRender){
        	TileEntityRendererDispatcher.instance.render(te, partialTicks, -1);
        	if(GlStateManager.glGetInteger(GLCompat.GL_CURRENT_PROGRAM) != shader.getShaderId()){
        		blacklistedObjects.add(te.getClass());
        		shader.use();
        	}
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        HbmShaderManager2.releaseShader();
	}

	private static void accumulateRenderObjects(ILight d) {
		AxisAlignedBB box = d.getBoundingBox();
		box = new AxisAlignedBB(MathHelper.floor(box.minX / 16.0D) * 16, MathHelper.floor(MathHelper.clamp(box.minY, 0, 255) / 16.0D) * 16, MathHelper.floor(box.minZ / 16.0D) * 16, MathHelper.ceil(box.maxX / 16.0D) * 16, MathHelper.ceil(MathHelper.clamp(box.maxY, 0, 255) / 16.0D) * 16, MathHelper.ceil(box.maxZ / 16.0D) * 16);
		Entity renderView = Minecraft.getMinecraft().getRenderViewEntity();
		for(int i = (int) box.minX; i < box.maxX; i += 16) {
			for(int j = (int) box.minY; j < box.maxY; j += 16) {
				for(int k = (int) box.minZ; k < box.maxZ; k += 16) {
					if(!Minecraft.getMinecraft().world.isBlockLoaded(new BlockPos(i, j, k)))
						continue;
					RenderChunk chunk = NTMRenderHelper.getRenderChunk(new BlockPos(i, j, k));
					ClassInheritanceMultiMap<Entity> classinheritancemultimap = Minecraft.getMinecraft().world.getChunk(chunk.getPosition()).getEntityLists()[chunk.getPosition().getY() / 16];
					if(d.intersects(chunk.boundingBox)) {
						d.addChunkToRender(chunk);
						if(d.doesTiles()) {
							for(TileEntity te : chunk.compiledChunk.getTileEntities()) {
								if(blacklistedObjects.contains(te.getClass()))
									continue;
								if(d.intersects(te.getRenderBoundingBox())) {
									d.addTileToRender(te);
								}
							}
						}
						if(d.doesEnts()) {
							for(Entity ent : classinheritancemultimap) {
								if(blacklistedObjects.contains(ent.getClass()))
									continue;
								if(ent != renderView && d.intersects(ent.getRenderBoundingBox().grow(0.5D))) {
									d.addEntToRender(ent);
								}
							}
						}
					}
				}
			}
		}
		if(d.doesTiles()) {
			Set<TileEntity> globals = Minecraft.getMinecraft().renderGlobal.setTileEntities;
			synchronized(globals) {
				for(TileEntity te : globals) {
					if(blacklistedObjects.contains(te.getClass()))
						continue;
					if(d.intersects(te.getRenderBoundingBox())) {
						d.addTileToRender(te);
					}
				}
			}
		}
	}
	
	private static void renderShadowForLight(Vec3d entPos, DirectionalLight l, float partialTicks) {
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.pushMatrix();
		l.projectionMatrix.store(ClientProxy.AUX_GL_BUFFER);
		ClientProxy.AUX_GL_BUFFER.rewind();
		GL11.glLoadMatrix(ClientProxy.AUX_GL_BUFFER);
		
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.pushMatrix();
		l.viewMatrix.store(ClientProxy.AUX_GL_BUFFER);
		ClientProxy.AUX_GL_BUFFER.rewind();
		GL11.glLoadMatrix(ClientProxy.AUX_GL_BUFFER);
		
		renderObjects(entPos, l.chunksToRender, l.entsToRender, l.tilesToRender, ResourceManager.flashlight_depth, partialTicks);
		
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.popMatrix();
	}
	
	private static void sendPostShaderUniforms(DirectionalLight light, Vec3d entityPos, Shader shader){
		Vec3d pos = light.start.subtract(entityPos);
		float height = (float) light.end.subtract(light.start).length();
		shader.uniform1f("height", height);
		shader.uniform3f("fs_Pos", (float)pos.x, (float)pos.y, (float)pos.z);
		shader.uniform2f("zNearFar", 0.05F, Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16 * MathHelper.SQRT_2);
		shader.uniform1f("eyeHeight", Minecraft.getMinecraft().player.getEyeHeight());
		shader.uniform1f("brightness", light.brightness);
		shader.uniform1f("useShadows", light.shadows ? 1 : 0);
		shader.uniform2f("shadowTexSize", 1024, 1024);
		
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
		GlStateManager.bindTexture(albedoTex);
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+3);
		GlStateManager.bindTexture(HbmShaderManager2.depthTexture);
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+4);
		Minecraft.getMinecraft().getTextureManager().bindTexture(light.cookie);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+5);
		GlStateManager.bindTexture(shadowTex);
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
		shader.uniform1i("mc_tex", 0);
		shader.uniform1i("depthBuffer", 3);
		shader.uniform1i("flashlightTex", 4);
		shader.uniform1i("shadowTex", 5);
		
		
		light.viewProjectionMatrix.store(ClientProxy.AUX_GL_BUFFER);
		ClientProxy.AUX_GL_BUFFER.rewind();
		shader.uniformMatrix4("flashlight_ViewProjectionMatrix", false, ClientProxy.AUX_GL_BUFFER);
		
		ClientProxy.AUX_GL_BUFFER.put(HbmShaderManager2.inv_ViewProjectionMatrix);
		ClientProxy.AUX_GL_BUFFER.rewind();
		shader.uniformMatrix4("inv_ViewProjectionMatrix", false, ClientProxy.AUX_GL_BUFFER);
	}
	
	private static void sendPointLightUniforms(PointLight light, Vec3d entityPos, Shader shader){
		Vec3d pos = light.pos.subtract(entityPos);
		Vec3d camPos = ActiveRenderInfo.getCameraPosition();
		shader.uniform3f("light_pos", (float)pos.x, (float)pos.y, (float)pos.z);
		shader.uniform3f("cam_pos", (float)camPos.x, (float)camPos.y, (float)camPos.z);
		shader.uniform1f("brightness", light.energy);
		shader.uniform1f("radius", light.radius);
		shader.uniform3f("light_color", (float)light.color.x, (float)light.color.y, (float)light.color.z);
		
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
		GlStateManager.bindTexture(albedoTex);
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0+3);
		GlStateManager.bindTexture(HbmShaderManager2.depthTexture);
		GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
		shader.uniform1i("mc_tex", 0);
		shader.uniform1i("depthBuffer", 3);
		
		ClientProxy.AUX_GL_BUFFER.put(HbmShaderManager2.inv_ViewProjectionMatrix);
		ClientProxy.AUX_GL_BUFFER.rewind();
		shader.uniformMatrix4("inv_ViewProjectionMatrix", false, ClientProxy.AUX_GL_BUFFER);
	}

	private static void initShadowBuffer(){
		shadowFbo = GLCompat.genFramebuffers();
		shadowTex = GlStateManager.generateTexture();
		
		GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, shadowFbo);
		GlStateManager.bindTexture(shadowTex);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLCompat.GL_DEPTH_COMPONENT24, 1024, 1024, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (FloatBuffer)null);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GLCompat.framebufferTexture2D(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, shadowTex, 0);
	}
	
	private static void clearShadowBuffer(){
		GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, shadowFbo);
		GlStateManager.clearDepth(1);
		GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
	}
	
	private static void recreateBuffers() {
		GlStateManager.deleteTexture(albedoTex);
		GLCompat.deleteFramebuffers(albedoFbo);
		GLCompat.deleteRenderbuffers(albedoDepth);
		albedoFbo = GLCompat.genFramebuffers();
		albedoTex = GlStateManager.generateTexture();
		albedoDepth = GLCompat.genRenderbuffers();

		GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, albedoFbo);

		GlStateManager.bindTexture(albedoTex);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (FloatBuffer) null);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GLCompat.framebufferTexture2D(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, albedoTex, 0);

		GLCompat.bindRenderbuffer(GLCompat.GL_RENDERBUFFER, albedoDepth);
		GLCompat.renderbufferStorage(GLCompat.GL_RENDERBUFFER, GLCompat.GL_DEPTH_COMPONENT24, width, height);
		GLCompat.framebufferRenderbuffer(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_DEPTH_ATTACHMENT, GLCompat.GL_RENDERBUFFER, albedoDepth);

		
		GlStateManager.deleteTexture(lightAccTex);
		GLCompat.deleteFramebuffers(lightAccFbo);
		lightAccFbo = GLCompat.genFramebuffers();
		lightAccTex = GlStateManager.generateTexture();
		
		GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, lightAccFbo);
		GlStateManager.bindTexture(lightAccTex);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLCompat.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer) null);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GLCompat.framebufferTexture2D(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, lightAccTex, 0);
		
		
		GlStateManager.deleteTexture(volAccTex);
		GLCompat.deleteFramebuffers(volAccFbo);
		volAccFbo = GLCompat.genFramebuffers();
		volAccTex = GlStateManager.generateTexture();
		
		GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, volAccFbo);
		GlStateManager.bindTexture(volAccTex);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GLCompat.GL_RGBA16F, width/2, height/2, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer) null);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GLCompat.framebufferTexture2D(GLCompat.GL_FRAMEBUFFER, GLCompat.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, volAccTex, 0);
		
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
	}
	
	private static void clearAccumulationBuffer(){
		GlStateManager.clearColor(0, 0, 0, 1);
		GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, lightAccFbo);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
		GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, volAccFbo);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
	}
	
	private static void clearAlbedoBuffer(){
		GlStateManager.clearColor(0, 0, 0, 1);
		GlStateManager.clearDepth(1);
		GLCompat.bindFramebuffer(GLCompat.GL_FRAMEBUFFER, albedoFbo);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
	}

	private static class DirectionalLight implements ILight {

		public Matrix4f projectionMatrix;
		public Matrix4f viewMatrix;
		public Matrix4f viewProjectionMatrix;
		public List<RenderChunk> chunksToRender = new ArrayList<>();
		public List<TileEntity> tilesToRender;
		public List<Entity> entsToRender;
		public Vec3d start;
		public Vec3d end;
		public float degrees;
		double height;
		double radius;
		public float brightness;
		public ResourceLocation cookie;
		public boolean shadows;
		public boolean volume;
		public boolean entities;
		public boolean tileentities;

		public DirectionalLight(Vec3d start, Vec3d end, float deg, float b, ResourceLocation cookie, boolean volume, boolean shadows, boolean entities, boolean tileentities) {
			this.start = start;
			this.end = end;
			this.degrees = deg;
			this.brightness = b;
			this.cookie = cookie;
			this.volume = volume;
			this.shadows = shadows;
			this.entities = entities;
			this.tileentities = tileentities;

			if(tileentities) {
				tilesToRender = new ArrayList<>();
			}
			if(entities) {
				entsToRender = new ArrayList<>();
			}

			double radians = Math.toRadians(degrees);
			Vec3d startToEnd = end.subtract(start);
			height = startToEnd.length();
			radius = height * Math.tan(radians);
			
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			//Multiply by 2 because the FOV should be the diameter. Why is height multiplied by sqrt2? I honestly have no idea, but it doesn't work
			//correctly if I use height directly, and minecraft also multiplies by sqrt2, so I am, too.
			Project.gluPerspective(degrees * 2, 1, 0.05F, (float) height * MathHelper.SQRT_2);
			GlStateManager.getFloat(GL11.GL_PROJECTION_MATRIX, ClientProxy.AUX_GL_BUFFER);
			projectionMatrix = new Matrix4f();
			projectionMatrix.load(ClientProxy.AUX_GL_BUFFER);
			ClientProxy.AUX_GL_BUFFER.rewind();
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}
		
		public void setupViewProjectionMatrix(Vec3d entPos){
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			Vec3d startToEnd = end.subtract(start);
			Vec3d angles = BobMathUtil.getEulerAngles(startToEnd.normalize());
			
		    GlStateManager.rotate((float) (-angles.y+270), 1, 0, 0);
		    GlStateManager.rotate((float) (-angles.x+180), 0, 1, 0);
		    GlStateManager.translate(-(start.x-entPos.x), -(start.y-entPos.y), -(start.z-entPos.z));
		    
		    viewMatrix = new Matrix4f();
		    GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
			viewMatrix.load(ClientProxy.AUX_GL_BUFFER);
			ClientProxy.AUX_GL_BUFFER.rewind();
			
			viewProjectionMatrix = new Matrix4f();
			Matrix4f.mul(projectionMatrix, viewMatrix, viewProjectionMatrix);
			GlStateManager.popMatrix();
		}

		@Override
		public AxisAlignedBB getBoundingBox() {
			return new AxisAlignedBB(start.x, start.y, start.z, end.x, end.y, end.z).grow(radius);
		}

		@Override
		public void addEntToRender(Entity e) {
			entsToRender.add(e);
		}

		@Override
		public void addTileToRender(TileEntity t) {
			tilesToRender.add(t);
		}

		@Override
		public void addChunkToRender(RenderChunk r) {
			chunksToRender.add(r);
		}

		@Override
		public boolean doesEnts() {
			return entities;
		}

		@Override
		public boolean doesTiles() {
			return tileentities;
		}

		@Override
		public boolean intersects(AxisAlignedBB box) {
			return Library.isBoxCollidingCone(box, start, end, degrees);
		}
	}
	
	public static class PointLight implements ILight {
		
		public static final float cutoff = 0.15F;
		
		public Vec3d pos;
		public Vec3d color;
		public float energy;
		public float radius;
		
		public boolean entities = true;
		public boolean tileentities = true;
		public List<RenderChunk> chunksToRender = new ArrayList<>();
		public List<TileEntity> tilesToRender;
		public List<Entity> entsToRender;
		
		public PointLight(Vec3d pos, float energy) {
			this(pos, new Vec3d(1, 1, 1), energy);
		}
		
		public PointLight(Vec3d pos, Vec3d color, float energy) {
			this(pos, color, energy, true, true);
		}
		
		public PointLight(Vec3d pos, Vec3d color, float energy, boolean tile, boolean ent) {
			this.pos = pos;
			this.color = color;
			this.energy = energy;
			this.radius = (float) Math.sqrt(Math.max(this.energy/cutoff-1, 0));
			
			this.entities = ent;
			this.tileentities = tile;
			
			if(tileentities) {
				tilesToRender = new ArrayList<>();
			}
			if(entities) {
				entsToRender = new ArrayList<>();
			}
		}

		@Override
		public AxisAlignedBB getBoundingBox() {
			return new AxisAlignedBB(-radius, -radius, -radius, radius, radius, radius).offset(pos);
		}

		@Override
		public void addEntToRender(Entity e) {
			entsToRender.add(e);
		}

		@Override
		public void addTileToRender(TileEntity t) {
			tilesToRender.add(t);
		}

		@Override
		public void addChunkToRender(RenderChunk r) {
			chunksToRender.add(r);
		}

		@Override
		public boolean doesEnts() {
			return entities;
		}

		@Override
		public boolean doesTiles() {
			return tileentities;
		}

		@Override
		public boolean intersects(AxisAlignedBB box) {
			Vec3d closestOnBox = new Vec3d(MathHelper.clamp(pos.x, box.minX, box.maxX), MathHelper.clamp(pos.y, box.minY, box.maxY), MathHelper.clamp(pos.z, box.minZ, box.maxZ));
			return closestOnBox.squareDistanceTo(pos) < radius*radius;
		}
		
	}
	
	private static interface ILight {
		public AxisAlignedBB getBoundingBox();
		public void addEntToRender(Entity e);
		public void addTileToRender(TileEntity t);
		public void addChunkToRender(RenderChunk r);
		public boolean doesEnts();
		public boolean doesTiles();
		public boolean intersects(AxisAlignedBB box);
	}
}
