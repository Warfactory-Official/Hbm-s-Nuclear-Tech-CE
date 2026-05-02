package com.hbm.particle.gluon;

import com.hbm.items.weapon.ItemGunEgon;
import com.hbm.lib.Library;
import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import com.hbm.render.misc.LensVisibilityHandler;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleGluonFlare extends Particle {

	int visibilityId = -1;
	float rot;
	EntityPlayer player;
	
	public ParticleGluonFlare(World worldIn, double posXIn, double posYIn, double posZIn) {
		this(worldIn, posXIn, posYIn, posZIn, null);
	}
	
	public ParticleGluonFlare(World worldIn, double posXIn, double posYIn, double posZIn, EntityPlayer player) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleScale = 7;
		this.particleMaxAge = 3+worldIn.rand.nextInt(3);
		rot = rand.nextFloat()*0.1F + 0.1F;
		this.particleAngle = rand.nextFloat()*6F;
		this.prevParticleAngle = this.particleAngle;
		this.particleGreen = 0.7F;
		this.particleRed = 0.5F;
		this.player = player;
	}
	
	@Override
	public void onUpdate() {
		this.particleAge ++;
		this.prevParticleAngle = this.particleAngle;
		this.particleAngle += rot;
		if(particleAge >= particleMaxAge){
			setExpired();
			LensVisibilityHandler.delete(visibilityId);
			return;
		}
	}

	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public boolean shouldDisableDepth() {
		return true;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.flare2);
		GlStateManager.disableDepth();
		GlStateManager.disableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		
		double entPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX)*partialTicks;
        double entPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY)*partialTicks;
        double entPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ)*partialTicks;
        
        interpPosX = entPosX;
        interpPosY = entPosY;
        interpPosZ = entPosZ;
		
		float f5 = (float)(this.posX - interpPosX);
        float f6 = (float)(this.posY - interpPosY);
        float f7 = (float)(this.posZ - interpPosZ);
        if(player != null){
        	float[] angles = ItemGunEgon.getBeamDirectionOffset(player.world.getTotalWorldTime()+partialTicks);
			Vec3d look = Library.changeByAngle(player.getLook(partialTicks), angles[0], angles[1]);
        	RayTraceResult r = Library.rayTraceIncludeEntitiesCustomDirection(player, look, 50, partialTicks);
        	Vec3d pos = null;
			if(r != null && r.hitVec != null && r.typeOfHit != Type.MISS && r.sideHit != null){
				Vec3i norm = r.sideHit.getDirectionVec();
				pos = r.hitVec.add(norm.getX()*0.1F, norm.getY()*0.1F, norm.getZ()*0.1F);
			} else {
				pos = player.getPositionEyes(partialTicks).add(look.scale(50));
			}
			f5 = (float)(pos.x - interpPosX);
	        f6 = (float)(pos.y - interpPosY);
	        f7 = (float)(pos.z - interpPosZ);
        }
        
        GlStateManager.translate(f5, f6, f7);
		if(visibilityId == -1){
			GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
			visibilityId = LensVisibilityHandler.generate(ClientProxy.AUX_GL_BUFFER);
		}
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
		LensVisibilityHandler.putMatrixBuf(visibilityId, ClientProxy.AUX_GL_BUFFER);
		
		float visibility = LensVisibilityHandler.getVisibility(visibilityId);
		visibility *= visibility;
		
		float ageN = (float)(this.particleAge+partialTicks)/(float)this.particleMaxAge;
		float scale = MathHelper.clamp(ageN*2, 0, 1)* MathHelper.clamp(2-ageN*2+0.1F, 0, 1);
		float f4 = 0.1F * this.particleScale * visibility*scale;
        
        Vec3d[] avec3d = new Vec3d[] {new Vec3d((double)(-rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double)(-rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(rotationYZ * f4 - rotationXZ * f4))};
        f4*=5;
        Vec3d[] avec3d2 = new Vec3d[] {new Vec3d((double)(-rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(-rotationYZ * f4 - rotationXZ * f4)), new Vec3d((double)(-rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(-rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 + rotationXY * f4), (double)(rotationZ * f4), (double)(rotationYZ * f4 + rotationXZ * f4)), new Vec3d((double)(rotationX * f4 - rotationXY * f4), (double)(-rotationZ * f4), (double)(rotationYZ * f4 - rotationXZ * f4))};

        if (this.particleAngle != 0.0F)
        {
            float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
            float f9 = MathHelper.cos(f8 * 0.5F);
            float f10 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.x;
            float f11 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.y;
            float f12 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.z;
            Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

            for (int l = 0; l < 4; ++l)
            {
                avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
            }
        }

        NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, 4);
        int packedLightmap = NTMBufferBuilder.packLightmap(240, 240);
        int packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha*visibility*visibility*0.8F);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) avec3d[0].x, (float) avec3d[0].y, (float) avec3d[0].z, 1, 1, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) avec3d[1].x, (float) avec3d[1].y, (float) avec3d[1].z, 1, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) avec3d[2].x, (float) avec3d[2].y, (float) avec3d[2].z, 0, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) avec3d[3].x, (float) avec3d[3].y, (float) avec3d[3].z, 0, 1, packedColor, packedLightmap);
		NTMImmediate.INSTANCE.draw();
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.fresnel_ms);
		fastBuffer = NTMImmediate.INSTANCE.beginParticlePositionTexColorLmap(GL11.GL_QUADS, 4);
        packedColor = NTMBufferBuilder.packColor(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha*visibility*0.2F);
		fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) avec3d2[0].x, (float) avec3d2[0].y, (float) avec3d2[0].z, 1, 1, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) avec3d2[1].x, (float) avec3d2[1].y, (float) avec3d2[1].z, 1, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) avec3d2[2].x, (float) avec3d2[2].y, (float) avec3d2[2].z, 0, 0, packedColor, packedLightmap);
        fastBuffer.appendParticlePositionTexColorLmapUnchecked((float) avec3d2[3].x, (float) avec3d2[3].y, (float) avec3d2[3].z, 0, 1, packedColor, packedLightmap);
        NTMImmediate.INSTANCE.draw();
		GlStateManager.disableBlend();
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}
	
}
