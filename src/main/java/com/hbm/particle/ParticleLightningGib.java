package com.hbm.particle;

import com.hbm.handler.HbmShaderManager2;
import com.hbm.main.ClientProxy;
import com.hbm.main.ResourceManager;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParticleLightningGib extends Particle {

	ModelBox box;
	int dl = -1;
	float[] matrix;
	//float[] invRot = new float[16];
	List<Particle> subParticles = new ArrayList<>();
	ParticleLightningStrip[] trails;
	Vec3d rotation;
	float rotX, rotY, rotZ, prevRotX, prevRotY, prevRotZ  = 0;
	ResourceLocation tex;
	float cubeMidX, cubeMidY, cubeMidZ;
	int numParticles;
	float boxScale;
	
	public ParticleLightningGib(World worldIn, double posXIn, double posYIn, double posZIn, ModelBox box, float[] matrix, ResourceLocation tex, float cubeMidX, float cubeMidY, float cubeMidZ, float scale, int trailNum) {
		super(worldIn, posXIn, posYIn, posZIn);
		this.box = box;
		this.matrix = matrix;
		/*Matrix4f mat = new Matrix4f();
		AUX_GL_BUFFER.put(matrix);
		AUX_GL_BUFFER.rewind();
		mat.load(AUX_GL_BUFFER);
		AUX_GL_BUFFER.rewind();
		mat.invert();
		mat.store(AUX_GL_BUFFER);
		AUX_GL_BUFFER.rewind();
		AUX_GL_BUFFER.get(invRot);
		AUX_GL_BUFFER.rewind();
		invRot[12] = 0;
		invRot[13] = 0;
		invRot[14] = 0;
		invRot[15] = 1;*/
		rotation = new Vec3d((worldIn.rand.nextFloat()-0.5)*50, (worldIn.rand.nextFloat()-0.5)*50, (worldIn.rand.nextFloat()-0.5)*50);
		this.tex = tex;
		this.particleMaxAge = 90;
		this.cubeMidX = cubeMidX;
		this.cubeMidY = cubeMidY;
		this.cubeMidZ = cubeMidZ;
		boxScale = scale;
		numParticles = (int) Math.cbrt((Math.abs(box.posX1-box.posX2)*Math.abs(box.posY1-box.posY2)*Math.abs(box.posZ1-box.posZ2)*0.6))+1;
		if(numParticles > 15)
			numParticles = 15;
		
		trails = new ParticleLightningStrip[trailNum];
		for(int i = 0; i < trails.length; i ++){
			trails[i] = new ParticleLightningStrip(world, posX, posY, posZ);
			trails[i].motionScaleNorm = 0.1F;
			trails[i].motionScaleTan = 0.0F;
			trails[i].forkChance = 0;
			trails[i].minNewPointDist = 1;
			trails[i].width = 0.025F;
			trails[i].doTransform = true;
		}
	}
	
	public void motion(Vec3d motion){
		this.motionX = motion.x;
		this.motionY = motion.y;
		this.motionZ = motion.z;
	}
	
	@Override
	public void onUpdate() {
		this.particleAge++;
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		prevRotX = rotX;
		prevRotY = rotY;
		prevRotZ = rotZ;
		move(motionX, motionY, motionZ);
		this.motionX *= onGround ? 0.85 : 0.9;
		this.motionY *= onGround ? 0.85 : 0.9;
		this.motionZ *= onGround ? 0.85 : 0.9;
		if(particleAge >= particleMaxAge){
			this.setExpired();
			GlStateManager.glDeleteLists(dl, 1);
			return;
		}
		Iterator<Particle> itr = subParticles.iterator();
		while(itr.hasNext()){
			Particle p = itr.next();
			p.onUpdate();
			if(!p.isAlive())
				itr.remove();
		}
		for(ParticleLightningStrip p : trails){
			p.onUpdate();
			if(particleAge < 20)
				p.setNewPoint(new Vec3d(this.posX+(rand.nextFloat()-0.5)*0.5, this.posY+(rand.nextFloat()-0.5)*0.5, this.posZ+(rand.nextFloat()-0.5)*0.5));
		}
		rotX += rotation.x;
		rotY += rotation.y;
		rotZ += rotation.z;
		rotation = rotation.scale(onGround ? 0.08 : 0.95);
		motionY -= 0.05;
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
		ClientProxy.deferredRenderers.add(() -> {
			GlStateManager.pushMatrix();
			GlStateManager.enableLighting();
			GlStateManager.enableRescaleNormal();
			float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
	        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
	        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
	        float rotateX = (float)(this.prevRotX + (this.rotX - this.prevRotX) * (double)partialTicks);
	        float rotateY = (float)(this.prevRotY + (this.rotY - this.prevRotY) * (double)partialTicks);
	        float rotateZ = (float)(this.prevRotZ + (this.rotZ - this.prevRotZ) * (double)partialTicks);
	        
	        GlStateManager.translate(f5, f6, f7);
	        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
			if(dl == -1){
				dl = GlStateManager.glGenLists(1);
				GlStateManager.glNewList(dl, GL11.GL_COMPILE);
				//Moves it so the origin is in the middle. I hope this makes for slightly better rotations.
				buffer.setTranslation(-cubeMidX, -cubeMidY, -cubeMidZ);
				box.render(buffer, 0.0625F);
				buffer.setTranslation(0, 0, 0);
				GlStateManager.glEndList();
			}
			GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, ClientProxy.AUX_GL_BUFFER);
			//Ah yes, spaghetti code.
			ClientProxy.AUX_GL_BUFFER2.put(matrix);
			ClientProxy.AUX_GL_BUFFER2.rewind();
			GlStateManager.multMatrix(ClientProxy.AUX_GL_BUFFER2);
			GlStateManager.rotate((float) (rotateX), 1, 0, 0);
			GlStateManager.rotate((float) (rotateY), 0, 1, 0);
			GlStateManager.rotate((float) (rotateZ), 0, 0, 1);
			
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableBlend();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.001F);
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			float a = 1-BobMathUtil.remap01_clamp(particleAge+partialTicks, 65, 67);
			GlStateManager.color(1F, 1F, 1F, a);
			ResourceManager.lightning_gib.use();
			float age = this.particleAge + partialTicks;
			ResourceManager.lightning_gib.uniform1f("age", age);
			int i = this.getBrightnessForRender(partialTicks);
			int j = i >> 16 & 65535;
	        int k = i & 65535;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, k, j);
			ResourceManager.lightning_gib.uniform1i("bloom", 0);
			GlStateManager.callList(dl);
			HbmShaderManager2.bloomData.bindFramebuffer(false);
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
			ResourceManager.lightning_gib.uniform1i("bloom", 1);
			GlStateManager.callList(dl);
			Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
			HbmShaderManager2.releaseShader();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			RenderHelper.disableStandardItemLighting();
			//AUX_GL_BUFFER.put(invRot);
			//AUX_GL_BUFFER.rewind();
			//GL11.glMultMatrix(AUX_GL_BUFFER);
			//GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, AUX_GL_BUFFER);
			//AUX_GL_BUFFER.rewind();
			//AUX_GL_BUFFER.put(matrix, 0, 12);
			//AUX_GL_BUFFER.rewind();
			//GL11.glLoadMatrix(AUX_GL_BUFFER);
			
			
			
			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.fresnel_ms);
			GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GlStateManager.depthMask(false);
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
			for(Particle p : subParticles){
				p.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
			}
			GlStateManager.enableAlpha();
	        GlStateManager.disableBlend();
	        GlStateManager.depthMask(true);
			
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
			for(ParticleLightningStrip p : trails){
				p.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
			}
		});
	}
	
}