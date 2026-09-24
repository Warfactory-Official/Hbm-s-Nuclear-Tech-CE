package com.hbm.physics;

import com.hbm.util.Vec3NT;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class PhysicsTestParticle extends Particle {

	public RigidBody body;
	public AxisAlignedBB[] boxes;
	boolean collided = false;

	public PhysicsTestParticle(World worldIn, RigidBody b, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		body = b;
		//boxes = new AxisAlignedBB[1];
		//float size = 0.3F;
		//boxes[0] = new AxisAlignedBB(-size, -size*0.5, -size, size, size*0.5, size);
		//boxes[1] = new AxisAlignedBB(-size*0.5, -size*4-size, -size*0.5, size*0.5, size*0.5-size, size*0.5);
		//boxes[0] = new AxisAlignedBB(0, 0, 0, 0.0625F*2, 0.0625F*16, 0.0625F*2);
		//body.addColliders(new AABBCollider(boxes[0], 4));
		//body.impulseVelocity(new Vec3NT(0, 0, 0.01), body.globalCentroid);
		//body.friction = 0.3F;
		particleMaxAge = 200;
	}
	
	@Override
	public void onUpdate() {
		body.minecraftTimestep();
		this.particleAge ++;
		if(particleAge >= particleMaxAge){
			setExpired();
		}
	}
	
	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.pushMatrix();
		double entPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX)*partialTicks;
        double entPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY)*partialTicks;
        double entPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ)*partialTicks;
        
        interpPosX = entPosX;
        interpPosY = entPosY;
        interpPosZ = entPosZ;
        GlStateManager.pushMatrix();
        if(collided)
        	partialTicks = 1;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        
        GlStateManager.disableTexture2D();
		GlStateManager.glLineWidth(4);
		RenderGlobal.drawSelectionBoundingBox(body.boundingBox.offset(-interpPosX, -interpPosY, -interpPosZ), 0, 1, 0, 1);
		GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        body.renderDebugInfo(new Vec3NT(interpPosX, interpPosY, interpPosZ), partialTicks);
		GlStateManager.popMatrix();
	}
	
}
