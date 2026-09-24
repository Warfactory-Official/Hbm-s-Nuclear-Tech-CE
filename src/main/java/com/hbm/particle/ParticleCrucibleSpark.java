package com.hbm.particle;

import com.hbm.render.item.weapon.ItemRenderCrucible;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleCrucibleSpark extends ParticleFirstPerson {

	public float stretch;
	public int timeUntilChange = 0;
	//Actual motion without the randomness
	public float amx, amy, amz;
	
	public ParticleCrucibleSpark(World worldIn, float s, float scale, double posXIn, double posYIn, double posZIn, float mx, float my, float mz) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.stretch = s;
		this.particleScale = scale;
		Vec3d am = new Vec3d(amx, amy, amz);
		if(am.lengthSquared() != 0){
			Vec3d rand = BobMathUtil.randVecInCone(am.normalize(), 30).scale(am.length());
			motionX = rand.x;
			motionY = rand.y;
			motionZ = rand.z;
		} else {
			this.motionX = (rand.nextFloat()-0.5)*0.015;
			this.motionY = (rand.nextFloat()-0.5)*0.015;
			this.motionZ = (rand.nextFloat()-0.5)*0.015;
		}
		timeUntilChange = rand.nextInt(6)+1;
		this.amx = mx;
		this.amy = my;
		this.amz = mz;
	}
	
	public ParticleCrucibleSpark color(float r, float g, float b, float a){
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		this.particleAlpha = a;
		return this;
	}
	
	public ParticleCrucibleSpark lifetime(int lifetime){
		this.particleMaxAge = lifetime;
		return this;
	}
	
	@Override
	public void onUpdate() {
		this.particleAge ++;
		timeUntilChange --;
		if(this.particleAge >= this.particleMaxAge){
			this.setExpired();
		}
		this.prevPosX = posX;
		this.prevPosY = posY;
		this.prevPosZ = posZ;
		this.posX += this.motionX + amx;
		this.posY += this.motionY + amy;
		this.posZ += this.motionZ + amz;
		if(timeUntilChange == 0){
			timeUntilChange = rand.nextInt(6)+1;
			Vec3d am = new Vec3d(motionX, motionY, motionZ);
			if(am.lengthSquared() != 0){
				Vec3d rand = BobMathUtil.randVecInCone(am.normalize(), 30).scale(am.length());
				motionX = rand.x;
				motionY = rand.y;
				motionZ = rand.z;
			} else {
				this.motionX = (rand.nextFloat()-0.5)*0.015;
				this.motionY = (rand.nextFloat()-0.5)*0.015;
				this.motionZ = (rand.nextFloat()-0.5)*0.015;
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
		
		
        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks);
        float mX = (float)(this.posX + (this.posX+this.motionX - this.posX) * (double)partialTicks);
        float mY = (float)(this.posY + (this.posY+this.motionY - this.posY) * (double)partialTicks);
        float mZ = (float)(this.posZ + (this.posZ+this.motionZ - this.posZ) * (double)partialTicks);
        
        Vec3d particleAxis = new Vec3d(mX, mY, mZ).subtract(f5, f6, f7);
        Vec3d toPlayer = new Vec3d(mX, mY, mZ).subtract(ItemRenderCrucible.playerPos);
        Vec3d point1 = particleAxis.crossProduct(toPlayer).normalize().scale(0.5*particleScale);
        Vec3d point2 = point1.scale(-1);
        point1 = point1.add(f5, f6, f7);
        point2 = point2.add(f5, f6, f7);
        particleAxis = particleAxis.scale(stretch);
        
        NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginPositionTex(GL11.GL_QUADS, MathHelper.ceil(this.particleAlpha) * 4);
        float alpha = this.particleAlpha;
        float point1X = (float) point1.x;
        float point1Y = (float) point1.y;
        float point1Z = (float) point1.z;
        float point2X = (float) point2.x;
        float point2Y = (float) point2.y;
        float point2Z = (float) point2.z;
        float axisX = (float) particleAxis.x;
        float axisY = (float) particleAxis.y;
        float axisZ = (float) particleAxis.z;
        while(alpha > 0){
        	fastBuffer.appendPositionTexUnchecked(point2X, point2Y, point2Z, 1, 0);
        	fastBuffer.appendPositionTexUnchecked(point1X, point1Y, point1Z, 1, 1);
        	fastBuffer.appendPositionTexUnchecked(point1X + axisX, point1Y + axisY, point1Z + axisZ, 0, 1);
        	fastBuffer.appendPositionTexUnchecked(point2X + axisX, point2Y + axisY, point2Z + axisZ, 0, 0);
        	alpha -= 1;
        }
        NTMImmediate.INSTANCE.draw();
       
	}
	
	@Override
	public ParticleType getType() {
		return ParticleType.CRUCIBLE;
	}

	
}
