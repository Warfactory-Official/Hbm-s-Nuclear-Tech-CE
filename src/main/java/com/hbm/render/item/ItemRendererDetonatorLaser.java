package com.hbm.render.item;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@AutoRegister(item = "detonator_laser")
@SideOnly(Side.CLIENT)
public class ItemRendererDetonatorLaser extends TEISRBase {

    // Uses the identity-assuming ItemRenderFrames17 frames, so it must route through BakedModelNoFPV;
    // otherwise this non-block item gets defaultItemTransforms() pre-multiplied onto the frame.
    @Override
    public boolean useIdentityTransform(Item item) {
        return true;
    }

    @Override
    public void renderByItem(ItemStack itemStackIn) {
        GlStateManager.pushMatrix();

        final boolean prevCull = RenderUtil.isCullEnabled();
        final int prevShade = RenderUtil.getShadeModel();

        if (!prevCull) GlStateManager.enableCull();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.detonator_laser_tex);

        switch (type) {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                GlStateManager.multMatrix(type == TransformType.FIRST_PERSON_LEFT_HAND ? ItemRenderFrames17.FIRST_PERSON_LEFT : ItemRenderFrames17.FIRST_PERSON);
                double s0 = 0.25D;
                GlStateManager.scale(s0, s0, s0);
                GlStateManager.rotate(80F, 0F, 1F, 0F);
                GlStateManager.rotate(-20F, 1F, 0F, 0F);
                GlStateManager.translate(1F, 0.5F, 3F);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND, HEAD -> {
                // detonator_laser's 1.7 item was setFull3D(), so third-person uses the full-3D held branch.
                GlStateManager.multMatrix(type == TransformType.HEAD ? ItemRenderFrames17.HEAD
                        : type == TransformType.THIRD_PERSON_LEFT_HAND ? ItemRenderFrames17.THIRD_PERSON_FULL3D_LEFT
                        : ItemRenderFrames17.THIRD_PERSON_FULL3D);
                double scale = 0.125D;
                GlStateManager.scale(-scale, -scale, -scale);
                GlStateManager.rotate(85F, 0F, 1F, 0F);
                GlStateManager.rotate(145F, 1F, 0F, 0F);
                GlStateManager.translate(-0.5F, -1.0F, 6.5F);
            }
            case GROUND -> {
                GlStateManager.multMatrix(ItemRenderFrames17.GROUND);
                double s1 = 0.25D;
                GlStateManager.rotate(-90F, 0F, 1F, 0F);
                GlStateManager.scale(s1, s1, s1);
            }
            case FIXED -> {
                GlStateManager.multMatrix(ItemRenderFrames17.FIXED);
                double s1 = 0.25D;
                GlStateManager.rotate(-90F, 0F, 1F, 0F);
                GlStateManager.scale(s1, s1, s1);
            }
            case GUI -> {
                GlStateManager.multMatrix(ItemRenderFrames17.GUI);
                GlStateManager.enableLighting();
                double s = 3.5D;
                GlStateManager.scale(s, s, -s);
                GlStateManager.translate(1.5F, 2.75F, 0.0F);
                GlStateManager.rotate(180F, 1F, 0F, 0F);
                GlStateManager.rotate(-90F, 0F, 1F, 0F);
                GlStateManager.rotate(-45F, 1F, 0F, 0F);
            }
            default -> {
            }
        }

        ResourceManager.detonator_laser.renderPart("Main");
        GlStateManager.pushMatrix();
        final boolean prevLighting = RenderUtil.isLightingEnabled();
        final boolean prevCull2 = RenderUtil.isCullEnabled();
        final boolean prevTex2D = RenderUtil.isTexture2DEnabled();
        final boolean prevBlend = RenderUtil.isBlendEnabled();
        final int prevSrc = RenderUtil.getBlendSrcFactor();
        final int prevDst = RenderUtil.getBlendDstFactor();
        final int prevSrcAlpha = RenderUtil.getBlendSrcAlphaFactor();
        final int prevDstAlpha = RenderUtil.getBlendDstAlphaFactor();
        final float prevR = RenderUtil.getCurrentColorRed();
        final float prevG = RenderUtil.getCurrentColorGreen();
        final float prevB = RenderUtil.getCurrentColorBlue();
        final float prevA = RenderUtil.getCurrentColorAlpha();
        if (prevLighting) GlStateManager.disableLighting();
        if (prevCull2) GlStateManager.disableCull();
        if (!prevBlend) GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        if (prevTex2D) GlStateManager.disableTexture2D();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        GlStateManager.color(1F, 0F, 0F, 1F);
        ResourceManager.detonator_laser.renderPart("Lights");
        GlStateManager.color(1F, 1F, 1F, 1F);
        final float px = 0.0625F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5626F, px * 18, -px * 14);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        int sub = 32;
        double width = px * 8;
        double len = width / sub;
        double time = System.currentTimeMillis() / -100D;
        double amplitude = 0.075;
        float red = 1.0F, green = 1.0F, blue = 0.0F; // Yellow color (0xFFFF00)

        for (int i = 0; i < sub; i++) {
            double h0 = Math.sin(i * 0.5 + time) * amplitude;
            double h1 = Math.sin((i + 1) * 0.5 + time) * amplitude;

            buffer.pos(0, -px * 0.25 + h1, len * (i + 1)).color(red, green, blue, 1.0F).endVertex();
            buffer.pos(0, px * 0.25 + h1, len * (i + 1)).color(red, green, blue, 1.0F).endVertex();
            buffer.pos(0, px * 0.25 + h0, len * i).color(red, green, blue, 1.0F).endVertex();
            buffer.pos(0, -px * 0.25 + h0, len * i).color(red, green, blue, 1.0F).endVertex();
        }

        tessellator.draw();
        GlStateManager.popMatrix();
        final boolean texWasEnabledForHud = RenderUtil.isTexture2DEnabled();
        if (!texWasEnabledForHud) GlStateManager.enableTexture2D();

        GlStateManager.pushMatrix();
        String s;
        Random rand = new Random(System.currentTimeMillis() / 500);
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        float f3 = 0.01F;
        GlStateManager.translate(0.5625F, 1.3125F, 0.875F);
        GlStateManager.scale(f3, -f3, f3);
        GlStateManager.rotate(90F, 0F, 1F, 0F);
        GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F * f3);

        GlStateManager.translate(3F, -2F, 0.2F);

        for (int i = 0; i < 3; i++) {
            s = (rand.nextInt(900000) + 100000) + "";
            font.drawString(s, 0, 0, 0xff0000);
            GlStateManager.translate(0F, 12.5F, 0F);
        }
        GlStateManager.popMatrix();

        if (!texWasEnabledForHud) GlStateManager.disableTexture2D();
        GlStateManager.color(prevR, prevG, prevB, prevA);
        GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcAlpha, prevDstAlpha);
        if (!prevBlend) GlStateManager.disableBlend();
        if (prevCull2) GlStateManager.enableCull();
        if (prevTex2D) GlStateManager.enableTexture2D();
        if (prevLighting) GlStateManager.enableLighting();

        GlStateManager.popMatrix();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(prevShade);
        if (!prevCull) GlStateManager.disableCull();

        GlStateManager.popMatrix();
    }
}
