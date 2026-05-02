package com.hbm.particle;

import com.google.common.collect.Queues;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;

public class ParticleRenderLayer {

	public static final ParticleRenderLayer NORMAL = new ParticleRenderLayer();
	public static final ParticleRenderLayer ADDITIVE_FRESNEL = new ParticleRenderLayer(){
		@Override
		public void preRender(){
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.fresnel_ms);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
			NTMImmediate.INSTANCE.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		};
		@Override
		public void postRender(){
			NTMImmediate.INSTANCE.draw();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		};
	};
	
	public boolean isRegistered = false;
	protected ArrayDeque<ParticleLayerBase> particles = Queues.newArrayDeque();
	
	public void preRender(){
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		NTMImmediate.INSTANCE.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}
	
	public void postRender(){
		NTMImmediate.INSTANCE.draw();
	}
	
	public static void register(){
		ParticleBatchRenderer.layers.add(NORMAL);
		ParticleBatchRenderer.layers.add(ADDITIVE_FRESNEL);
	}
	
}
