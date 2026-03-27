package com.hbm.particle.tau;

import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
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

public class ParticleTauHit extends Particle {

	float yaw;
	float pitch;
	Vec3d norm;
	
	public ParticleTauHit(World worldIn, double posXIn, double posYIn, double posZIn, float scale, Vec3d normal) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = scale;
		this.particleMaxAge = (int) (50*scale);
		Vec3d angles = BobMathUtil.getEulerAngles(normal);
		yaw = (float) angles.x;
		pitch = (float) angles.y;
		this.norm = normal;
	}
	
	@Override
	public void onUpdate() {
		this.particleAge ++;
		if(particleAge >= particleMaxAge){
			setExpired();
			return;
		}
		if(particleAge == 1){
			for(int i = 0; i < 2 + rand.nextInt(2); i ++){
				Vec3d randPos = new Vec3d(rand.nextFloat()-0.5, 0, rand.nextFloat()-0.5).rotatePitch(pitch).rotateYaw(yaw).scale(0.05);
				ParticleTauParticle p = new ParticleTauParticle(world, posX+randPos.x, posY+randPos.y, posZ+randPos.z, 0.2F, 0.01F, 1, 6, 0F);
				p.motion((float)norm.x*0.015F, (float)norm.y*0.015F, (float)norm.z*0.015F);
				p.lifetime(20);
				p.color(0.9F, 0.6F, 0.1F, 1F);
				Minecraft.getMinecraft().effectRenderer.addEffect(p);
			}
			for(int i = 0; i < 2 + rand.nextInt(3); i ++){
				Vec3d randPos = new Vec3d(rand.nextFloat()-0.5, 0, rand.nextFloat()-0.5).rotatePitch(pitch).rotateYaw(yaw).scale(0.05);
				ParticleTauParticle p = new ParticleTauParticle(world, posX+randPos.x, posY+randPos.y, posZ+randPos.z, 0.15F, 0.1F, 2, 6, 0.05F);
				randPos = randPos.scale(2);
				p.motion((float)norm.x*0.35F+(float)randPos.x, (float)norm.y*0.35F+(float)randPos.y, (float)norm.z*0.35F+(float)randPos.z);
				p.lifetime(30);
				p.color(0.9F, 0.6F, 0.1F, 1F);
				Minecraft.getMinecraft().effectRenderer.addEffect(p);
			}
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
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.enablePolygonOffset();
		GlStateManager.doPolygonOffset(-1, -1);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.fresnel_ms);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		float lifeN = (float)(particleAge+partialTicks)/(float)particleMaxAge;
		float fade = MathHelper.clamp(1F-lifeN*1.2F, 0, 1);
		GlStateManager.color(0.9F, 0.6F, 0.1F, 1.2F*fade);
		
        double entPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX)*partialTicks;
        double entPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY)*partialTicks;
        double entPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ)*partialTicks;
        
        interpPosX = entPosX;
        interpPosY = entPosY;
        interpPosZ = entPosZ;
        
		float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - entPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - entPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - entPosZ);
        GlStateManager.translate(f5, f6, f7);
        
        GL11.glRotated(yaw, 0, 1, 0);
        GL11.glRotated(pitch, 1, 0, 0);
        
        float scale = particleScale * (1-lifeN);
		NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginPositionTexQuads(2);
		fastBuffer.appendPositionTexQuadUnchecked(
				-0.5 * scale, 0, -0.5 * scale, 0, 0,
				0.5 * scale, 0, -0.5 * scale, 1, 0,
				0.5 * scale, 0, 0.5 * scale, 1, 1,
				-0.5 * scale, 0, 0.5 * scale, 0, 1
		);
		scale *= 0.1;
		fastBuffer.appendPositionTexQuadUnchecked(
				-0.5 * scale, 0, -0.5 * scale, 0, 0,
				0.5 * scale, 0, -0.5 * scale, 1, 0,
				0.5 * scale, 0, 0.5 * scale, 1, 1,
				-0.5 * scale, 0, 0.5 * scale, 0, 1
		);
		NTMImmediate.INSTANCE.draw();
		
		GlStateManager.disablePolygonOffset();
		NTMRenderHelper.resetColor();
		GlStateManager.enableCull();
		GlStateManager.enableAlpha();
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

}
