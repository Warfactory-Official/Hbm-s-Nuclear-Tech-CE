package com.hbm.particle.tau;

//import com.hbm.items.weapon.ItemGunGauss;
import com.hbm.main.ResourceManager;
import com.hbm.particle.ParticleFirstPerson;
import com.hbm.render.NTMRenderHelper;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
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

import java.util.ArrayList;
import java.util.List;

public class ParticleTauMuzzleLightning extends ParticleFirstPerson {

	float randU;
	int nextPositionTime;
	List<Vec3d> positions = new ArrayList<>(10);
	
	public ParticleTauMuzzleLightning(World worldIn, double posX, double posY, double posZ, float width) {
		super(worldIn, posX, posY, posZ);
		this.particleMaxAge = 120;
		this.randU = rand.nextFloat();
		this.particleScale = width;
		nextPositionTime = 3 + rand.nextInt(3);
	}
	
	@Override
	public void onUpdate() {
		this.particleAge ++;
		
		if(this.particleAge >= this.particleMaxAge){
			this.setExpired();
			return;
		}
//		if(ItemGunGauss.firstPersonFireCounter == -1){
//			particleAge = Math.max(particleAge, particleMaxAge-40);
//			return;
//		}
		for(int i = 0; i < positions.size(); i ++){
			if(rand.nextInt(6) == 0){
				positions.set(i, positions.get(i).add(new Vec3d(rand.nextFloat()-0.5, rand.nextFloat()-0.5, rand.nextFloat()-0.5).scale(0.05)));
			}
		}
		if(nextPositionTime == 0){
			nextPositionTime = 3 + rand.nextInt(3);
			positions.add(new Vec3d(rand.nextFloat()-0.5, rand.nextFloat()-0.5, rand.nextFloat()-0.5).scale(0.3).add(posX, posY, posZ));
			if(positions.size() > 9){
				positions.remove(0);
			}
		}
		nextPositionTime--;
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
		if(positions.size() < 2){
			return;
		}
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.bfg_core_lightning);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		float lifeN = (float)(particleAge+partialTicks)/(float)particleMaxAge;
		float fade = MathHelper.clamp(2.5F-lifeN*2.5F, 0, 1);
		
		NTMBufferBuilder fastBuffer = NTMImmediate.INSTANCE.beginPositionTexColorQuads(positions.size() - 1);
		for(int i = 0; i < positions.size()-1; i ++){
			Vec3d current = positions.get(i);
			Vec3d next = positions.get(i+1);
			Vec3d axis = next.subtract(current);
			Vec3d toPlayer = current;
			Vec3d pos1 = axis.crossProduct(toPlayer).normalize().scale(particleScale*Math.max(fade, 0.75));
			Vec3d pos2 = pos1.scale(-1);
			float al = i == 0 || i == 9 ? 0.5F : 1;
			int packedColor = NTMBufferBuilder.packColor(1.0F, 0.7F, 0.1F, fade * al);
			float pos1CurrentX = (float) (pos1.x + current.x);
			float pos1CurrentY = (float) (pos1.y + current.y);
			float pos1CurrentZ = (float) (pos1.z + current.z);
			float pos2CurrentX = (float) (pos2.x + current.x);
			float pos2CurrentY = (float) (pos2.y + current.y);
			float pos2CurrentZ = (float) (pos2.z + current.z);
			float pos2NextX = (float) (pos2.x + next.x);
			float pos2NextY = (float) (pos2.y + next.y);
			float pos2NextZ = (float) (pos2.z + next.z);
			float pos1NextX = (float) (pos1.x + next.x);
			float pos1NextY = (float) (pos1.y + next.y);
			float pos1NextZ = (float) (pos1.z + next.z);
			fastBuffer.appendPositionTexColorQuadUnchecked(
					pos1CurrentX, pos1CurrentY, pos1CurrentZ, randU, 0, packedColor,
					pos2CurrentX, pos2CurrentY, pos2CurrentZ, randU, 1, packedColor,
					pos2NextX, pos2NextY, pos2NextZ, randU, 1, packedColor,
					pos1NextX, pos1NextY, pos1NextZ, randU, 0, packedColor
			);
		}
		NTMImmediate.INSTANCE.draw();
		
		NTMRenderHelper.resetColor();
		GlStateManager.enableCull();
		GlStateManager.enableAlpha();
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
	}

	@Override
	public ParticleType getType() {
		return ParticleType.TAU;
	}
	
}
