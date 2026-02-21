package com.hbm.render.util;

import com.hbm.Tags;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderMiscEffects {

	public static ResourceLocation glint = new ResourceLocation(Tags.MODID + ":textures/misc/glint.png");
    public static ResourceLocation glintBF = new ResourceLocation(Tags.MODID + ":textures/misc/glintBF.png");

	public static void renderClassicGlint(World world, float interpol, IModelCustom model, String part, float colorMod, float r, float g, float b, float speed, float scale) {

        GlStateManager.pushMatrix();
    	float offset = Minecraft.getMinecraft().player.ticksExisted + interpol;
        GlStateManager.enableBlend();
        float color = colorMod;
        GlStateManager.color(color, color, color, 1.0F);
        GlStateManager.depthFunc(GL11.GL_EQUAL);
        GlStateManager.depthMask(false);

        for (int k = 0; k < 2; ++k) {

            GlStateManager.disableLighting();

            float glintColor = 0.76F;

            GlStateManager.color(r * glintColor, g * glintColor, b * glintColor, 1.0F);
            GlStateManager.blendFunc(SourceFactor.SRC_COLOR, DestFactor.ONE);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();

            float movement = offset * (0.001F + (float)k * 0.003F) * speed;

            GlStateManager.scale(scale, scale, scale);
            GlStateManager.rotate(30.0F - (float)k * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, movement, 0.0F);

            GlStateManager.matrixMode(GL11.GL_MODELVIEW);

            if("all".equals(part))
            	model.renderAll();
            else
            	model.renderPart(part);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.depthMask(true);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.popMatrix();
    }

	public static void renderClassicGlint(World world, float interpol, IModelCustom model, String part, float r, float g, float b, float speed, float scale) {
		renderClassicGlint(world, interpol, model, part, 0.5F, r, g, b, speed, scale);
    }

	public static void renderClassicGlint(World world, float interpol, IModelCustom model, String part) {
		renderClassicGlint(world, interpol, model, part, 0.5F, 0.25F, 0.8F, 20.0F, 1F/3F);
    }
}