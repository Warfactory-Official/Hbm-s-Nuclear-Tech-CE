package com.hbm.render.world;

import com.hbm.capability.HbmLivingProps;
import com.hbm.main.ModEventHandlerClient;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;

public class RenderNTMSkyboxChainloader extends IRenderHandler { //why an abstract class uses the I-prefix is beyond me but ok, alright, whatever
	
	/*
	 * To get the terrain render order right, making a sky rendering handler is absolutely necessary. Turns out MC can only handle one of these, so what do we do?
	 * We make out own renderer, grab any existing renderers that are already occupying the slot, doing what is effectively chainloading while adding our own garbage.
	 * If somebody does the exact same thing as we do we might be screwed due to increasingly long recursive loops but we can fix that too, no worries.
	 */
	private IRenderHandler parent;

    private static final ResourceLocation digammaStar = new ResourceLocation("hbm:textures/misc/star_digamma.png");
    private static final ResourceLocation lodeStar = new ResourceLocation("hbm:textures/misc/star_lode.png");
    private static final ResourceLocation bobmazonSat = new ResourceLocation("hbm:textures/misc/sat_bobmazon.png");

    /*
     * Recursion brake for compatible chainloaders: only let parent render once in a chain.
     */
    public static boolean didLastRender = false;

    public RenderNTMSkyboxChainloader(IRenderHandler parent) {
        this.parent = parent;
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {

        if (parent != null) {
            // Prevent infinite loops if other mods also chainload sky renderers.
            if (!didLastRender) {
                didLastRender = true;
                parent.render(partialTicks, world, mc);
                didLastRender = false;
            }
        } else {
            RenderGlobal rg = mc.renderGlobal;
            world.provider.setSkyRenderer(null);
            rg.renderSky(partialTicks, 2);
            world.provider.setSkyRenderer(this);
        }

        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableFog();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO); // additive

        if (ModEventHandlerClient.renderLodeStar) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-75.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(10.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.color(1F, 1F, 1F, 1F);
            mc.getTextureManager().bindTexture(lodeStar);

            float size = (float) (0.5D + world.rand.nextFloat() * 0.25D);
            float dist = 100.0F;

            NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionTexQuads(1);
            buf.appendPositionTexQuadUnchecked(
                    -size, dist, -size, 0, 0,
                     size, dist, -size, 0, 1,
                     size, dist,  size, 1, 1,
                    -size, dist,  size, 1, 0
            );
            NTMImmediate.INSTANCE.draw();

            GlStateManager.popMatrix();
        }

        float brightness = (float) Math.sin(world.getCelestialAngle(partialTicks) * Math.PI);
        brightness *= brightness;
        GlStateManager.color(brightness, brightness, brightness, 1.0F);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.9999F, 0.9999F, 0.9999F);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(140.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-40.0F, 0.0F, 0.0F, 1.0F);

        mc.getTextureManager().bindTexture(digammaStar);

        double digamma = HbmLivingProps.getDigamma(mc.player);
        float size = (float) (1.0D + digamma * 0.25D);
        float dist = (float) (100.0D - digamma * 2.5D);

        NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionTexQuads(1);
        buf.appendPositionTexQuadUnchecked(
                -size, dist, -size, 0, 0,
                 size, dist, -size, 0, 1,
                 size, dist,  size, 1, 1,
                -size, dist,  size, 1, 0
        );
        NTMImmediate.INSTANCE.draw();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-40.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((System.currentTimeMillis() % (360 * 1000)) / 1000F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((System.currentTimeMillis() % (360 * 100)) / 100F, 1.0F, 0.0F, 0.0F);

        mc.getTextureManager().bindTexture(bobmazonSat);

        size = 0.5F;
        dist = 100.0F;

        buf = NTMImmediate.INSTANCE.beginPositionTexQuads(1);
        buf.appendPositionTexQuadUnchecked(
                -size, dist, -size, 0, 0,
                 size, dist, -size, 0, 1,
                 size, dist,  size, 1, 1,
                -size, dist,  size, 1, 0
        );
        NTMImmediate.INSTANCE.draw();
        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.enableFog();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.color(1F, 1F, 1F, 1F);

        GlStateManager.popMatrix();
    }
}
