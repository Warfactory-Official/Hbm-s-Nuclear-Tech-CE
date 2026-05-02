package com.hbm.render.entity.effect;

import com.hbm.Tags;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.effect.EntityNukeTorex.Cloudlet;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ClientProxy;
import com.hbm.main.MainRegistry;
import com.hbm.main.ModEventHandlerClient;
import com.hbm.render.InstancedBillboardBatch;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import com.hbm.util.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Random;

@AutoRegister(factory = "FACTORY")
public class RenderTorex extends Render<EntityNukeTorex> {

	public static final IRenderFactory<EntityNukeTorex> FACTORY = man -> new RenderTorex(man);
	
	private static final ResourceLocation cloudlet = new ResourceLocation(Tags.MODID + ":textures/particle/particle_base.png");
	private static final ResourceLocation flare = new ResourceLocation(Tags.MODID + ":textures/particle/flare.png");
	private static final float CLOUDLET_MIN_U = 0F;
	private static final float CLOUDLET_MIN_V = 0F;
	private static final float CLOUDLET_SIZE_U = 1F;
	private static final float CLOUDLET_SIZE_V = 1F;

	public static final int flashBaseDuration = 30;
	public static final int flareBaseDuration = 100;
	private static final InstancedBillboardBatch INSTANCED_CLOUDLET_BATCH = new InstancedBillboardBatch();
    private static final float ONE_THIRD = 1F / 3F;

	protected RenderTorex(RenderManager renderManager){
		super(renderManager);
	}

    private static float getCloudAlphaBase(EntityNukeTorex cloud) {
        int fadeOut = cloud.maxAge * 3 / 4;
        int life = cloud.ticksExisted;
        if (life > fadeOut) {
            return 1F - (float) (life - fadeOut) / (float) (cloud.maxAge - fadeOut);
        }
        return 1F;
    }
	
	private static final Comparator<Cloudlet> CLOUD_SORTER = (first, second) -> Double.compare(second.renderSortDistanceSq, first.renderSortDistanceSq);

    private static float getCloudletLifeFrac(Cloudlet cloudlet) {
        return (float) cloudlet.age / (float) cloudlet.cloudletLife;
    }

    private static float getCloudletAlpha(Cloudlet cloudlet, float lifeFrac, float cloudAlphaBase) {
        float alpha = (1F - lifeFrac) * cloudAlphaBase;
        if (cloudlet.type == EntityNukeTorex.TorexType.CONDENSATION) {
            alpha *= 0.25F;
        }
        if (alpha < 0.0001F) {
            return 0.0001F;
        }
        return alpha > 1F ? 1F : alpha;
    }

    private static float getCloudletScale(Cloudlet cloudlet, float lifeFrac) {
        return cloudlet.startingScale + lifeFrac * cloudlet.growingScale;
    }

    private static float getCloudletBrightness(Cloudlet cloudlet) {
        return cloudlet.type == EntityNukeTorex.TorexType.CONDENSATION ? 0.9F : 0.75F * cloudlet.colorMod;
    }

    private static double getCloudletGreying(Cloudlet cloudlet) {
        return cloudlet.type == EntityNukeTorex.TorexType.RING ? 0.05D : 0D;
    }

    private static double getCloudletColor(Cloudlet cloudlet, double prevColor, double color, float partialTicks, double greying) {
        if (cloudlet.type == EntityNukeTorex.TorexType.CONDENSATION) {
            return 1D;
        }
        return (prevColor + (color - prevColor) * partialTicks) + greying;
    }

    private static float clampCloudletColor(double color, float brightness) {
        float channel = (float) color * brightness;
        if (channel < 0.15F) {
            return 0.15F;
        }
        return channel > 1F ? 1F : channel;
    }

    private static int getCloudletLightmap(float r, float g, float b) {
        float avgBrightness = (r + g + b) * ONE_THIRD;
        if (avgBrightness > 1F) {
            avgBrightness = 1F;
        }
        int br = (int) (avgBrightness * 240F);
        return br < 48 ? 48 : br;
    }

    private static float getCloudletRenderPos(double prevPos, double pos, float partialTicks, double cloudPos) {
        return (float) (prevPos + (pos - prevPos) * partialTicks - cloudPos);
    }

    private static void writeCloudlet(long address, Cloudlet cloudlet, EntityNukeTorex cloud,
                                      float partialTicks, float cloudAlphaBase, float rotationX,
                                      float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float lifeFrac = getCloudletLifeFrac(cloudlet);
        float alpha = getCloudletAlpha(cloudlet, lifeFrac, cloudAlphaBase);
        float scale = getCloudletScale(cloudlet, lifeFrac);
        float brightness = getCloudletBrightness(cloudlet);
        double greying = getCloudletGreying(cloudlet);
        double colorR = getCloudletColor(cloudlet, cloudlet.prevColorR, cloudlet.colorR, partialTicks, greying);
        double colorG = getCloudletColor(cloudlet, cloudlet.prevColorG, cloudlet.colorG, partialTicks, greying);
        double colorB = getCloudletColor(cloudlet, cloudlet.prevColorB, cloudlet.colorB, partialTicks, greying);
        float r = clampCloudletColor(colorR, brightness);
        float g = clampCloudletColor(colorG, brightness);
        float b = clampCloudletColor(colorB, brightness);
        int br = getCloudletLightmap(r, g, b);

        int red = (int) (r * 255F);
        int green = (int) (g * 255F);
        int blue = (int) (b * 255F);
        int alphaByte = (int) (alpha * 255F);
        int packedColor = (alphaByte << 24) | (blue << 16) | (green << 8) | red;
        int packedLightmap = br | (br << 16);

        float posX = getCloudletRenderPos(cloudlet.prevPosX, cloudlet.posX, partialTicks, cloud.posX);
        float posY = getCloudletRenderPos(cloudlet.prevPosY, cloudlet.posY, partialTicks, cloud.posY);
        float posZ = getCloudletRenderPos(cloudlet.prevPosZ, cloudlet.posZ, partialTicks, cloud.posZ);
        float rotX = rotationX * scale;
        float rotZ = rotationZ * scale;
        float rotYZ = rotationYZ * scale;
        float rotXY = rotationXY * scale;
        float rotXZ = rotationXZ * scale;

        NTMBufferBuilder.writeParticlePositionTexColorLmapQuad(address,
                posX - rotX - rotYZ, posY - rotXZ, posZ - rotZ - rotXY, 1F, 1F,
                posX - rotX + rotYZ, posY + rotXZ, posZ - rotZ + rotXY, 1F, 0F,
                posX + rotX + rotYZ, posY + rotXZ, posZ + rotZ + rotXY, 0F, 0F,
                posX + rotX - rotYZ, posY - rotXZ, posZ + rotZ - rotXY, 0F, 1F,
                packedColor, packedLightmap);
    }

	@Override
	public void doRender(EntityNukeTorex cloud, double x, double y, double z, float entityYaw, float partialTicks){
        if (!ClientProxy.renderingConstant) return;

        Minecraft mc = Minecraft.getMinecraft();
        boolean useInstancedCloudlets = GeneralConfig.instancedParticles && !ShaderHelper.areShadersActive();
        float scale = (float)cloud.getScale();
		float flashDuration = scale * flashBaseDuration;
		float flareDuration = scale * flareBaseDuration;

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		if(fog)
			GL11.glDisable(GL11.GL_FOG);

		if(useInstancedCloudlets) {
			cloudletWrapperInstanced(cloud, partialTicks);
		} else {
            mc.entityRenderer.enableLightmap();
            try {
			    cloudletWrapper(cloud, partialTicks);
            } finally {
                mc.entityRenderer.disableLightmap();
            }
		}

		if(cloud.ticksExisted < flareDuration+1) {
            mc.entityRenderer.enableLightmap();
            try {
                flareWrapper(cloud, partialTicks, flareDuration);
            } finally {
                mc.entityRenderer.disableLightmap();
            }
        }
		if(cloud.ticksExisted < flashDuration+1) flashWrapper(cloud, partialTicks, flashDuration);
		if(cloud.ticksExisted < (flashDuration / 10) && System.currentTimeMillis() - ModEventHandlerClient.flashTimestamp > 1_000) ModEventHandlerClient.flashTimestamp = System.currentTimeMillis();
		if(cloud.didPlaySound && !cloud.didShake && System.currentTimeMillis() - ModEventHandlerClient.shakeTimestamp > 1_000) {
			ModEventHandlerClient.shakeTimestamp = System.currentTimeMillis();
			cloud.didShake = true;
			EntityPlayer player = MainRegistry.proxy.me();
			float dist = player.getDistance(cloud);
			player.hurtTime = (100 - (int) dist) > 0 ? (int) ((float) (100 - (int) dist) * 1.5F) : 0;
			player.maxHurtTime = (100 - (int) dist) > 0 ? (100 - (int) dist) : 0;
			player.attackedAtYaw = 0F;
		}

		if(fog)
			GL11.glEnable(GL11.GL_FOG);

		GlStateManager.popMatrix();
	}

	private void writeCloudletInstance(ByteBuffer buf, EntityNukeTorex cloud, Cloudlet cloudlet, float partialTicks) {
		float lifeFrac = getCloudletLifeFrac(cloudlet);
		float alpha = getCloudletAlpha(cloudlet, lifeFrac, getCloudAlphaBase(cloud));
		float scale = getCloudletScale(cloudlet, lifeFrac);
		float brightness = getCloudletBrightness(cloudlet);
		double greying = getCloudletGreying(cloudlet);
		double colorR = getCloudletColor(cloudlet, cloudlet.prevColorR, cloudlet.colorR, partialTicks, greying);
		double colorG = getCloudletColor(cloudlet, cloudlet.prevColorG, cloudlet.colorG, partialTicks, greying);
		double colorB = getCloudletColor(cloudlet, cloudlet.prevColorB, cloudlet.colorB, partialTicks, greying);
		float r = clampCloudletColor(colorR, brightness);
		float g = clampCloudletColor(colorG, brightness);
		float b = clampCloudletColor(colorB, brightness);
		int br = getCloudletLightmap(r, g, b);
		float posX = getCloudletRenderPos(cloudlet.prevPosX, cloudlet.posX, partialTicks, cloud.posX);
		float posY = getCloudletRenderPos(cloudlet.prevPosY, cloudlet.posY, partialTicks, cloud.posY);
		float posZ = getCloudletRenderPos(cloudlet.prevPosZ, cloudlet.posZ, partialTicks, cloud.posZ);

		InstancedBillboardBatch.writeInstance(buf, posX, posY, posZ, scale * 2F,
				CLOUDLET_MIN_U, CLOUDLET_MIN_V, CLOUDLET_SIZE_U, CLOUDLET_SIZE_V,
				r, g, b, alpha, br, br);
	}
	
	private void flareWrapper(EntityNukeTorex cloud, float partialTicks, float flareDuration) {

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		RenderHelper.disableStandardItemLighting();
			
		bindTexture(flare);

		Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		
		double age = Math.min(cloud.ticksExisted + partialTicks, flareDuration);
		float alpha = (float) Math.min(1, (flareDuration - age) / flareDuration);
		
		Random rand = new Random(cloud.getEntityId());
		
		for(int i = 0; i < 3; i++) {
			float x = (float) (rand.nextGaussian() * 0.5F * cloud.rollerSize);
			float y = (float) (rand.nextGaussian() * 0.5F * cloud.rollerSize);
			float z = (float) (rand.nextGaussian() * 0.5F * cloud.rollerSize);
			tessellateFlare(buf, x, y + cloud.coreHeight, z, (float) (10 * cloud.rollerSize), alpha, partialTicks);
		}

		tess.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private void cloudletWrapper(EntityNukeTorex cloud, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		// To prevent particles cutting off before fully fading out
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		RenderHelper.disableStandardItemLighting();

		bindTexture(cloudlet);
        int cloudletCount = cloud.cloudlets.size();
        BufferBuilder raw = NTMImmediate.INSTANCE.begin(GL11.GL_QUADS,
                DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP, cloudletCount * 4);

        sortCloudlets(cloud, partialTicks);
		float rotationX = ActiveRenderInfo.getRotationX();
		float rotationZ = ActiveRenderInfo.getRotationZ();
		float rotationYZ = ActiveRenderInfo.getRotationYZ();
		float rotationXY = ActiveRenderInfo.getRotationXY();
		float rotationXZ = ActiveRenderInfo.getRotationXZ();
        long writeAddress = NTMBufferBuilder.address(raw.getByteBuffer());
        float cloudAlphaBase = getCloudAlphaBase(cloud);

        for (int i = 0; i < cloudletCount; i++) {
            writeCloudlet(writeAddress, cloud.cloudlets.get(i), cloud, partialTicks, cloudAlphaBase,
                    rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
            writeAddress += NTMBufferBuilder.PARTICLE_POSITION_TEX_COLOR_LMAP_QUAD_BYTES;
        }
        ((NTMBufferBuilder) raw).setVertexCount(cloudletCount << 2);

        NTMImmediate.INSTANCE.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private void cloudletWrapperInstanced(EntityNukeTorex cloud, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		RenderHelper.disableStandardItemLighting();

        sortCloudlets(cloud, partialTicks);
		int cloudletCount = cloud.cloudlets.size();
		if(cloudletCount == 0) {
			GlStateManager.depthMask(true);
			GlStateManager.enableAlpha();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
			return;
		}
		ByteBuffer instancedCloudletBuffer = INSTANCED_CLOUDLET_BATCH.begin(cloudletCount);
		for(Cloudlet cloudlet : cloud.cloudlets) {
			writeCloudletInstance(instancedCloudletBuffer, cloud, cloudlet, partialTicks);
		}

		bindTexture(cloudlet);
		INSTANCED_CLOUDLET_BATCH.draw(cloudletCount);

		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

    private void sortCloudlets(EntityNukeTorex cloud, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        int clientTick = mc.ingameGUI.getUpdateCounter();
        if (cloud.lastRenderSortTick == clientTick) {
            return;
        }

        Entity viewEntity = mc.getRenderViewEntity();
        if (viewEntity == null) {
			return;
		}

        Vec3d cameraPos = ActiveRenderInfo.projectViewFromEntity(viewEntity, partialTicks);
        double playerX = cameraPos.x;
        double playerY = cameraPos.y;
        double playerZ = cameraPos.z;

		for(Cloudlet cloudlet : cloud.cloudlets) {
			double dx = playerX - cloudlet.posX;
			double dy = playerY - cloudlet.posY;
			double dz = playerZ - cloudlet.posZ;
			cloudlet.renderSortDistanceSq = dx * dx + dy * dy + dz * dz;
		}

		cloud.cloudlets.sort(CLOUD_SORTER);
        cloud.lastRenderSortTick = clientTick;
	}

	private void tessellateFlare(BufferBuilder buf, double posX, double posY, double posZ, float scale, float a, float partialTicks) {

		float f1 = ActiveRenderInfo.getRotationX();
		float f2 = ActiveRenderInfo.getRotationZ();
		float f3 = ActiveRenderInfo.getRotationYZ();
		float f4 = ActiveRenderInfo.getRotationXY();
		float f5 = ActiveRenderInfo.getRotationXZ();
		int br = (int)(a * 240);
		buf.pos((double) (posX - f1 * scale - f3 * scale), (double) (posY - f5 * scale), (double) (posZ - f2 * scale - f4 * scale)).tex(1, 1).color(1F, 1F, 1F, a).lightmap(br, br).endVertex();
		buf.pos((double) (posX - f1 * scale + f3 * scale), (double) (posY + f5 * scale), (double) (posZ - f2 * scale + f4 * scale)).tex(1, 0).color(1F, 1F, 1F, a).lightmap(br, br).endVertex();
		buf.pos((double) (posX + f1 * scale + f3 * scale), (double) (posY + f5 * scale), (double) (posZ + f2 * scale + f4 * scale)).tex(0, 0).color(1F, 1F, 1F, a).lightmap(br, br).endVertex();
		buf.pos((double) (posX + f1 * scale - f3 * scale), (double) (posY - f5 * scale), (double) (posZ + f2 * scale - f4 * scale)).tex(0, 1).color(1F, 1F, 1F, a).lightmap(br, br).endVertex();

	}

	private void flashWrapper(EntityNukeTorex cloud, float interp, float flashDuration) {

        if(cloud.ticksExisted < flashDuration) {

    		GlStateManager.pushMatrix();
    		//Function [0, 1] that determines the scale and intensity (inverse!) of the flash
        	double intensity = (cloud.ticksExisted + interp) / flashDuration;
        	GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);

        	//Euler function to slow down the scale as it progresses
        	//Makes it start fast and the fade-out is nice and smooth
        	intensity = intensity * Math.pow(Math.E, -intensity) * 2.717391304D;

        	renderFlash(50F * (float)flashDuration/(float)flashBaseDuration, intensity, cloud.coreHeight);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    		GlStateManager.popMatrix();
        }
	}

	private void renderFlash(float scale, double intensity, double height) {

    	GlStateManager.scale(0.2F, 0.2F, 0.2F);
    	GlStateManager.translate(0, height * 4, 0);

    	double inverse = 1.0D - intensity;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
		RenderHelper.disableStandardItemLighting();

        Random random = new Random(432L);
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        GlStateManager.disableAlpha();
        GlStateManager.enableCull();
        GlStateManager.depthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		
        GlStateManager.pushMatrix();

        for(int i = 0; i < 300; i++) {

            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);

            float vert1 = (random.nextFloat() * 20.0F + 5.0F + 1 * 10.0F) * (float)(intensity * scale);
            float vert2 = (random.nextFloat() * 2.0F + 1.0F + 1 * 2.0F) * (float)(intensity * scale);

            buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(0D, 0D, 0D).color(1.0F, 1.0F, 1.0F, (float) inverse).endVertex();
            buf.pos(-0.866D * vert2, vert1, -0.5D * vert2).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
            buf.pos(0.866D * vert2, vert1, -0.5D * vert2).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
            buf.pos(0.0D, vert1, 1.0D * vert2).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
            buf.pos(-0.866D * vert2, vert1, -0.5D * vert2).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
            tessellator.draw();
        }

        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        RenderHelper.enableStandardItemLighting();
	}

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
    }

    @Override
	protected ResourceLocation getEntityTexture(EntityNukeTorex entity) {
		return null;
	}
}
