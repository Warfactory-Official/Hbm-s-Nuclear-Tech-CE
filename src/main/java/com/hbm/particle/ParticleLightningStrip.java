package com.hbm.particle;

import com.hbm.handler.HbmShaderManager2;
import com.hbm.handler.LightningGenerator;
import com.hbm.handler.LightningGenerator.LightningGenInfo;
import com.hbm.handler.LightningGenerator.LightningNode;
import com.hbm.main.ResourceManager;
import com.hbm.particle.lightning_test.TrailRenderer2;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class ParticleLightningStrip extends Particle {

	public List<LightningPoint> points = new ArrayList<>();
	public float forkChance = 0.2F;
	public float minNewPointDist = 0.1F;
	public float motionScaleTan = 0.03F;
	public float motionScaleNorm = 0.01F;
	public float width = 0.004F;
	public boolean doTransform = false;
	
	public ParticleLightningStrip(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.particleMaxAge = 122;
	}
	
	public void setNewPoint(Vec3d point){
		float scale = 0.01F;
		float scale2 = 0.002F;
		Vec3d pos = point.add((world.rand.nextFloat()*2-1)*scale, (world.rand.nextFloat()*2-1)*scale, (world.rand.nextFloat()*2-1)*scale);
		Vec3d motion = new Vec3d((world.rand.nextFloat()*2-1)*scale2, (world.rand.nextFloat()*2-1)*scale2, (world.rand.nextFloat()*2-1)*scale2);
		LightningNode fork = null;
		if(points.size() >= 1){
			Vec3d direction = point.subtract(points.get(points.size()-1).ogPos);
			double dot = direction.dotProduct(pos.subtract(points.get(points.size()-1).ogPos));
			Vec3d project = direction.scale(dot/direction.lengthSquared());
			direction = direction.normalize();
			motion = motion.add(pos.subtract(project).normalize().scale(motionScaleTan)).add(direction.scale(motionScaleNorm));
			if(world.rand.nextFloat() < forkChance){
				LightningGenInfo i = new LightningGenInfo();
				i.randAmount = 0.03F;
				i.subdivisions = 3;
				i.subdivRecurse = 1;
				i.forkChance = 0.1F;
				fork = LightningGenerator.generateLightning(new Vec3d(0, 0, 0), BobMathUtil.randVecInCone(direction, 20).scale(-0.3F), i);
			}
		}
		LightningPoint lPoint = new LightningPoint(point, pos, motion);
		
		lPoint.fork = fork;
		points.add(lPoint);
		if(points.size() >= 3 && points.get(points.size()-3).pos.squareDistanceTo(point) < minNewPointDist*minNewPointDist){
			points.remove(points.size()-2);
		}
	}
	
	@Override
	public void onUpdate() {
		this.particleAge ++;
		if(this.particleAge > this.particleMaxAge){
			this.setExpired();
		}
		for(LightningPoint p : points){
			p.prevPos = p.pos;
			p.pos = p.pos.add(p.motion);
			p.motion = p.motion.scale(0.96);
		}
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if(points.size() >= 2){
			if(doTransform){
				GlStateManager.pushMatrix();
				GlStateManager.translate(-interpPosX, -interpPosY, -interpPosZ);
			}
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
			ResourceManager.lightning.use();
		    ResourceManager.lightning.uniform4f("duck", 1F, 1F, 1F, 1F);
		    ResourceManager.lightning.uniform1f("age", this.particleAge+partialTicks);
		    int list = GlStateManager.glGenLists(1);
		    GlStateManager.glNewList(list, GL11.GL_COMPILE);
		    float time = (this.particleAge+partialTicks)*0.012F;
		    List<Vec3d> currentPoints = new ArrayList<>(points.size());
		    for(int i = 0; i < points.size(); i++){
		    	LightningPoint p = points.get(i);
		    	Vec3d pos = BobMathUtil.lerp(p.prevPos, p.pos, partialTicks);
		    	float override = (float)(i)/(float)points.size();
		    	
		    	override = 1-MathHelper.clamp(override-time*time*time, 0.001F, 1F);
		    	ResourceManager.lightning.uniform1f("fadeoverride", override);
		    	if(p.fork != null){
		    		if(doTransform){
		    			LightningGenerator.render(p.fork, new Vec3d(interpPosX, interpPosY+entityIn.getEyeHeight(), interpPosZ), width*0.5F, (float)pos.x, (float)pos.y, (float)pos.z, false, null);
					} else {
						LightningGenerator.render(p.fork, new Vec3d(0, 0, 0), width*0.5F, (float)pos.x, (float)pos.y, (float)pos.z, false, null);
					}
		    	}
		    	currentPoints.add(pos);
		    }
		    ResourceManager.lightning.uniform1f("fadeoverride", 1F);
		    ResourceManager.lightning.uniform1i("vertices", currentPoints.size()*3+2);
			if(doTransform){
				TrailRenderer2.draw(new Vec3d(interpPosX, interpPosY+entityIn.getEyeHeight(), interpPosZ), currentPoints, width);
			} else {
				TrailRenderer2.draw(new Vec3d(0, 0, 0), currentPoints, width);
			}
			GlStateManager.glEndList();
			GlStateManager.callList(list);
			HbmShaderManager2.bloomData.bindFramebuffer(false);
			ResourceManager.lightning.uniform4f("duck", 0.6F, 0.8F, 1F, 1F);
			GlStateManager.callList(list);
			GlStateManager.callList(list);
			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
			GlStateManager.glDeleteLists(list, 1);
			HbmShaderManager2.releaseShader();
			GlStateManager.disableBlend();
			if(doTransform){
				GlStateManager.popMatrix();
			}
		}
	}
	
	public static class LightningPoint {
		Vec3d ogPos;
		Vec3d pos;
		Vec3d prevPos;
		Vec3d motion;
		LightningNode fork = null;
		
		public LightningPoint(Vec3d ogPos, Vec3d pos, Vec3d motion) {
			this.ogPos = ogPos;
			this.pos = pos;
			this.prevPos = pos;
			this.motion = motion;
		}
	}

}
