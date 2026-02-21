package com.hbm.render.world;

import com.hbm.capability.HbmLivingProps;
import com.hbm.main.ModEventHandlerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

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

        final Tessellator tess = Tessellator.getInstance();
        final BufferBuilder buf = tess.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableFog();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE); // additive

        if (ModEventHandlerClient.renderLodeStar) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-75.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(10.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.color(1F, 1F, 1F, 1F);
            mc.getTextureManager().bindTexture(lodeStar);

            double size = 0.5D + world.rand.nextFloat() * 0.25D;
            double dist = 100.0D;

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buf.pos(-size, dist, -size).tex(0, 0).endVertex();
            buf.pos( size, dist, -size).tex(0, 1).endVertex();
            buf.pos( size, dist,  size).tex(1, 1).endVertex();
            buf.pos(-size, dist,  size).tex(1, 0).endVertex();
            tess.draw();

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
        double size = (1.0D + digamma * 0.25D);
        double dist = 100.0D - digamma * 2.5D;

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-size, dist, -size).tex(0, 0).endVertex();
        buf.pos( size, dist, -size).tex(0, 1).endVertex();
        buf.pos( size, dist,  size).tex(1, 1).endVertex();
        buf.pos(-size, dist,  size).tex(1, 0).endVertex();
        tess.draw();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-40.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((System.currentTimeMillis() % (360 * 1000)) / 1000F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((System.currentTimeMillis() % (360 * 100)) / 100F, 1.0F, 0.0F, 0.0F);

        mc.getTextureManager().bindTexture(bobmazonSat);

        size = 0.5D;
        dist = 100.0D;

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-size, dist, -size).tex(0, 0).endVertex();
        buf.pos( size, dist, -size).tex(0, 1).endVertex();
        buf.pos( size, dist,  size).tex(1, 1).endVertex();
        buf.pos(-size, dist,  size).tex(1, 0).endVertex();
        tess.draw();
        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.enableFog();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.color(1F, 1F, 1F, 1F);

        GlStateManager.popMatrix();
    }
}
