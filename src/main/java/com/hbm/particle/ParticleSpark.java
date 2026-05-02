package com.hbm.particle;

import com.hbm.main.ResourceManager;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class ParticleSpark extends Particle {

	public float stretch;
	public float gravity;
	
	public ParticleSpark(World worldIn, double posXIn, double posYIn, double posZIn, float stretch, float scale, int lifetime, float gravity) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.stretch = stretch;
		this.particleScale = scale;
		this.particleMaxAge = lifetime;
		this.gravity = gravity;
	}
	
	public ParticleSpark color(float colR, float colG, float colB, float colA){
		this.particleRed = colR;
		this.particleGreen = colG;
		this.particleBlue = colB;
		this.particleAlpha = colA;
		return this;
	}
	
	public ParticleSpark motion(float mX, float mY, float mZ){
		this.motionX = mX;
		this.motionY = mY-gravity;
		this.motionZ = mZ;
		this.posX += mX;
		this.posY += mY-gravity;
		this.posZ += mZ;
		return this;
	}

	@Override
	public void onUpdate() {
		this.particleAge++;
		if(this.particleAge >= this.particleMaxAge || new Vec3d(motionX, motionY, motionZ).lengthSquared() < 0.001){
			setExpired();
			return;
		}
		if(this.particleAge < 4){
			this.canCollide = false;
		} else {
			this.canCollide = true;
		}
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.move(motionX, motionY, motionZ);
		final float airResistance = 0.95F;
		this.motionX *= airResistance;
		this.motionY = motionY*airResistance-gravity;
		this.motionZ *= airResistance;
	}
	
	@Override
	public void move(double x, double y, double z) {
		double d0 = y;
        double origX = x;
        double origZ = z;

        if (this.canCollide)
        {
            List<AxisAlignedBB> list = this.world.getCollisionBoxes((Entity)null, this.getBoundingBox().expand(x, y, z));

            for (AxisAlignedBB axisalignedbb : list)
            {
                y = axisalignedbb.calculateYOffset(this.getBoundingBox(), y);
            }

            this.setBoundingBox(this.getBoundingBox().offset(0.0D, y, 0.0D));

            for (AxisAlignedBB axisalignedbb1 : list)
            {
                x = axisalignedbb1.calculateXOffset(this.getBoundingBox(), x);
            }

            this.setBoundingBox(this.getBoundingBox().offset(x, 0.0D, 0.0D));

            for (AxisAlignedBB axisalignedbb2 : list)
            {
                z = axisalignedbb2.calculateZOffset(this.getBoundingBox(), z);
            }

            this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, z));
        }
        else
        {
            this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
        }

        this.resetPositionToBB();
        this.onGround = d0 != y && d0 < 0.0D;
        
        if(d0 != y){
        	this.motionY = -motionY*0.75*(rand.nextFloat()*0.8+0.25);
        }

        if (origX != x)
        {
            this.motionX = -motionX*0.75*(rand.nextFloat()*1.12+0.25);
        }

        if (origZ != z)
        {
            this.motionZ = -motionY*0.75*(rand.nextFloat()*1.12+0.25);
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
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.bfg_particle);
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableLighting();
		
        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        float mX = (float)(this.posX + (this.posX+this.motionX - this.posX) * (double)partialTicks - interpPosX);
        float mY = (float)(this.posY + (this.posY+this.motionY - this.posY) * (double)partialTicks - interpPosY);
        float mZ = (float)(this.posZ + (this.posZ+this.motionZ - this.posZ) * (double)partialTicks - interpPosZ);
        
        Vec3d particleAxis = new Vec3d(mX, mY, mZ).subtract(f5, f6, f7);
        Vec3d toPlayer = new Vec3d(mX, mY, mZ);
        Vec3d point1 = particleAxis.crossProduct(toPlayer).normalize().scale(0.5*particleScale);
        Vec3d point2 = point1.scale(-1);
        point1 = point1.add(f5, f6, f7);
        point2 = point2.add(f5, f6, f7);
        particleAxis = particleAxis.scale(stretch);
        
        NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, MathHelper.ceil(this.particleAlpha) * 4);
        float alpha = this.particleAlpha;
        int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
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
            int packedColor = NTMBufferBuilder.packColor(particleRed, particleGreen, particleBlue, MathHelper.clamp(alpha, 0, 1));
        	fastBuffer.appendParticlePositionTexColorLmapUnchecked(point2X, point2Y, point2Z, 1, 0, packedColor, packedLightmap);
        	fastBuffer.appendParticlePositionTexColorLmapUnchecked(point1X, point1Y, point1Z, 1, 1, packedColor, packedLightmap);
        	fastBuffer.appendParticlePositionTexColorLmapUnchecked(point1X + axisX, point1Y + axisY, point1Z + axisZ, 0, 1, packedColor, packedLightmap);
        	fastBuffer.appendParticlePositionTexColorLmapUnchecked(point2X + axisX, point2Y + axisY, point2Z + axisZ, 0, 0, packedColor, packedLightmap);
        	alpha -= 1;
        }
        NTMImmediate.INSTANCE.draw();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
	}
	
}
