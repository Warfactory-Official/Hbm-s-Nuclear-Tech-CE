package com.hbm.particle_instanced;

import com.hbm.Tags;
import com.hbm.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MODID)
public class InstancedParticleRenderer {

	private static int faceCount = 0;
	private static final int[] renderCounts = new int[ParticleInstanced.RenderType.size()];
	private static final int[] renderOrder = new int[ParticleInstanced.RenderType.size()];
	
	protected static ArrayDeque<ParticleInstanced> particles = new ArrayDeque<>();
	private static final Queue<ParticleInstanced> queue = new ArrayDeque<>();
	
	public static void addParticle(ParticleInstanced p) {
		if(p != null)
			queue.add(p);
	}
	
	public static void updateParticles() {
		Iterator<ParticleInstanced> itr = particles.iterator();
		while(itr.hasNext()) {
			ParticleInstanced p = itr.next();
			p.onUpdate();
			if(!p.isAlive()) {
				faceCount -= p.getFaceCount();
				itr.remove();
			}
		}
		if(!queue.isEmpty()) {
			for(ParticleInstanced particle = queue.poll(); particle != null; particle = queue.poll()) {
				if(particles.size() > 16384){
					ParticleInstanced p = particles.removeFirst();
					faceCount -= p.getFaceCount();
				}
				faceCount += particle.getFaceCount();
				particles.add(particle);
			}
			
		}
	}
	
	public static void renderParticles(Entity entityIn, float partialTicks) {
		if(faceCount <= 0) {
			return;
		}

		Particle.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
		Particle.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
		Particle.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
		Particle.cameraViewDir = entityIn.getLook(partialTicks);

		GlStateManager.depthMask(false);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
		int renderOrderSize = 0;
		for(ParticleInstanced particle : particles) {
			int particleFaceCount = particle.getFaceCount();
			if(particleFaceCount <= 0) {
				continue;
			}

			int renderTypeId = particle.getRenderType().ordinal();
			if(renderCounts[renderTypeId] == 0) {
				renderOrder[renderOrderSize++] = renderTypeId;
			}
			renderCounts[renderTypeId] += particleFaceCount;
		}

		try {
			for(int i = 0; i < renderOrderSize; i++) {
				int renderTypeId = renderOrder[i];
				ParticleInstanced.RenderType renderType = ParticleInstanced.RenderType.byId(renderTypeId);
				int renderCount = renderCounts[renderTypeId];

				if(renderType.shouldDisableFog() && fog) {
					GlStateManager.disableFog();
				}
				if(renderType.shouldDisableDepth()) {
					GlStateManager.disableDepth();
				}
				if(renderType.shouldDisableLighting() && lighting) {
					GlStateManager.disableLighting();
				}

				GlStateManager.enableBlend();
				GlStateManager.blendFunc(renderType.getSourceFactor(), renderType.getDestFactor());
				GlStateManager.alphaFunc(GL11.GL_GREATER, renderType.getAlphaThreshold());
				renderType.bindTexture();

				ByteBuffer particleBuffer = renderType.getBatch().begin(renderCount);
				for(ParticleInstanced particle : particles){
					if(particle.getRenderType() == renderType) {
						particle.addDataToBuffer(particleBuffer, partialTicks);
					}
				}
				renderType.getBatch().draw(renderCount);

				if(renderType.shouldDisableLighting() && lighting) {
					GlStateManager.enableLighting();
				}
				if(renderType.shouldDisableDepth()) {
					GlStateManager.enableDepth();
				}
				if(renderType.shouldDisableFog() && fog) {
					GlStateManager.enableFog();
				}
			}
		} finally {
			for(int i = 0; i < renderOrderSize; i++) {
				renderCounts[renderOrder[i]] = 0;
			}
		}
		
		GlStateManager.depthMask(true);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
	}
	
	@SubscribeEvent
	public static void renderLast(RenderWorldLastEvent event) {
		if(GeneralConfig.instancedParticles){
			renderParticles(Minecraft.getMinecraft().getRenderViewEntity(), event.getPartialTicks());
		}
	}
	
	@SubscribeEvent
	public static void clientTick(ClientTickEvent event) {
		if(GeneralConfig.instancedParticles && event.phase == Phase.START) {
			if(!Minecraft.getMinecraft().isGamePaused())
				updateParticles();
		}
	}
}
