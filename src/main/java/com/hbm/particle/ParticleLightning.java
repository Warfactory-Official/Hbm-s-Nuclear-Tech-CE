package com.hbm.particle;

import com.hbm.main.ResourceManager;
import com.hbm.render.NTMRenderHelper;
import com.hbm.util.Vec3NT;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class ParticleLightning extends Particle {

	public int divisions = 7;
	public Vec3NT direction = Vec3NT.createVectorHelper(0, -40, 0);
	private float[] positions;
	
	public ParticleLightning(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.canCollide = false;
		this.particleMaxAge = 60;
		this.particleScale = 20F;
		regenerateLightning();
	}
	
	@Override
	public void onUpdate() {
		this.particleAge++;
		if(this.particleAge >= this.particleMaxAge){
			this.setExpired();
		}
	}
	
	public void regenerateLightning(){
		positions = new float[(divisions+2)*3];
		for(int i = 0; i < positions.length; i += 3){
			float magnitude = (i/3)/(divisions+1F);
			Vec3NT pos = direction.mult(magnitude);
			positions[i] = (float) pos.x;
			positions[i+1] = (float) pos.y;
			positions[i+2] = (float) pos.z;
		}
		
		for(int i = 3; i < positions.length-3; i += 3){
			Vec3NT randPos = Vec3NT.createVectorHelper((world.rand.nextDouble()-0.5)*4, (rand.nextDouble()-0.5)*2, (rand.nextDouble()-0.5)*4);
			positions[i] += randPos.x;
			positions[i+1] += randPos.y;
			positions[i+2] += randPos.z;
		}
	}
	
	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.pushMatrix();
		
		double d0 = this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks;
		double d1 = this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks;
		double d2 = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks;
		
		double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
		double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
		double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

		GlStateManager.translate(d0 - d3, d1 - d4, d2 - d5);
		
		float[] vertices = new float[positions.length*2];
		
		Vec3d look = entity.getPositionEyes(partialTicks).subtract(d0, d1, d2);
		for(int i = 0; i < positions.length-3; i += 3){
			//Vec3NT toNextSegment = Vec3NT.createVectorHelper(positions[i+3], positions[i+4], positions[i+5]).subtract(Vec3NT.createVectorHelper(positions[i], positions[i+1], positions[i+2]));
			Vec3NT point1 = Vec3NT.createVectorHelper(look.x, look.y, look.z).crossProduct(direction).normalize().mult((float) (0.2*particleScale));
		    Vec3NT point2 = point1.mult(-1);
		    
		    vertices[i*2] = (float) point1.x + positions[i];
		    vertices[i*2+1] = (float) point1.y + positions[i+1];
		    vertices[i*2+2] = (float) point1.z + positions[i+2];
		    vertices[i*2+3] = (float) point2.x + positions[i];
		    vertices[i*2+4] = (float) point2.y + positions[i+1];
		    vertices[i*2+5] = (float) point2.z + positions[i+2];
		    
		    if(i == positions.length - 6){
		    	int i2 = i + 3;
		    	vertices[i2*2] = (float) point1.x + positions[i2];
			    vertices[i2*2+1] = (float) point1.y + positions[i2+1];
			    vertices[i2*2+2] = (float) point1.z + positions[i2+2];
			    vertices[i2*2+3] = (float) point2.x + positions[i2];
			    vertices[i2*2+4] = (float) point2.y + positions[i2+1];
			    vertices[i2*2+5] = (float) point2.z + positions[i2+2];
		    }
		}
		
		NTMRenderHelper.bindTexture(ResourceManager.bfg_core_lightning);
		
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		float[] prevPositions = ArrayUtils.subarray(vertices, 0, 6);
		
		float uStep = 1/(divisions + 1F);
		for(int i = 6; i < vertices.length; i += 6){
			float u = (i/6-1)*uStep;
			float u2 = u + uStep;
			buf.pos(prevPositions[0], prevPositions[1], prevPositions[2]).tex(u, 0).endVertex();
			buf.pos(prevPositions[3], prevPositions[4], prevPositions[5]).tex(u, 1).endVertex();
			buf.pos(vertices[i+3], vertices[i+4], vertices[i+5]).tex(u2, 1).endVertex();
			buf.pos(vertices[i], vertices[i+1], vertices[i+2]).tex(u2, 0).endVertex();

			prevPositions = ArrayUtils.subarray(vertices, i, i+6);
		}
		
		tes.draw();
		
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		
		GlStateManager.popMatrix();
	}
	
	
}
