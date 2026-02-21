package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@AutoRegister(item = "gun_quadro")
public class ItemRenderQuadro extends ItemRenderWeaponBase {

    protected static String label = ">> <<";

    @Override
    protected float getTurnMagnitude(ItemStack stack) {
        return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F;
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0, 0, 0.875);

        float offset = 0.8F;
        standardAimingTransform(stack, -2.5F * offset, -3.5F * offset, 2.5F * offset, -1.5F * offset, -3F * offset, 2.5F * offset);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {
        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        EntityPlayer player = Minecraft.getMinecraft().player;

        double scale = 1.75D;
        GlStateManager.scale(scale, scale, scale);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
        double[] reloadPush = HbmAnimationsSedna.getRelevantTransformation("RELOAD_PUSH");
        double[] reloadRotate = HbmAnimationsSedna.getRelevantTransformation("RELOAD_ROTATE");

        GlStateManager.translate(0, -1, -1);
        GlStateManager.rotate((float) equip[0], 1, 0, 0);
        GlStateManager.translate(0, 1, 1);

        GlStateManager.translate(0, 0, recoil[2]);

        GlStateManager.translate(0, -1, -1);
        GlStateManager.rotate((float) reloadRotate[2], 1, 0, 0);
        GlStateManager.translate(0, 1, 1);

        final int prevShade = RenderUtil.getShadeModel();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.quadro_tex);
        ResourceManager.quadro.renderPart("Launcher");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -1, 0);
        GlStateManager.translate(0, 3, 0);
        GlStateManager.rotate((float) (reloadPush[1] * 30), 1, 0, 0);
        GlStateManager.translate(0, -3, 0);
        GlStateManager.translate(0, 0, reloadPush[0] * 3);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.quadro_rocket_tex);
        ResourceManager.quadro.renderPart("Rockets");
        GlStateManager.popMatrix();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(prevShade);
        if (ItemGunBaseNT.prevAimingProgress >= 1F && ItemGunBaseNT.aimingProgress >= 1F) {
            GlStateManager.pushMatrix();
            final boolean prevLighting = RenderUtil.isLightingEnabled();
            final boolean prevCull = RenderUtil.isCullEnabled();
            final boolean prevBlend = RenderUtil.isBlendEnabled();
            final int prevSrc = RenderUtil.getBlendSrcFactor();
            final int prevDst = RenderUtil.getBlendDstFactor();
            final int prevSrcA = RenderUtil.getBlendSrcAlphaFactor();
            final int prevDstA = RenderUtil.getBlendDstAlphaFactor();
            final float prevR = RenderUtil.getCurrentColorRed();
            final float prevG = RenderUtil.getCurrentColorGreen();
            final float prevB = RenderUtil.getCurrentColorBlue();
            final float prevA = RenderUtil.getCurrentColorAlpha();

            if (prevLighting) GlStateManager.disableLighting();
            if (prevCull) GlStateManager.disableCull();
            if (!prevBlend) GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            float f3 = 0.04F;
            GlStateManager.translate(-0.375F, 2.25F, 0.875F);
            GlStateManager.rotate((float) (180D + (System.currentTimeMillis() / 2.0) % 360D), 0, -1, 0);
            GlStateManager.translate(-(font.getStringWidth(label) / 2.0) * f3, 0, 0);
            GlStateManager.scale(f3, -f3, f3);
            GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F * f3);
            font.drawString(label, 0, 0, new Color(0F, 1F, 1F).getRGB());

            GlStateManager.color(prevR, prevG, prevB, prevA);
            GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcA, prevDstA);
            if (!prevBlend) GlStateManager.disableBlend();
            if (prevCull) GlStateManager.enableCull();
            if (prevLighting) GlStateManager.enableLighting();

            GlStateManager.popMatrix();

            int brightness = player.world.getCombinedLight(new BlockPos(player.posX, player.posY, player.posZ), 0);
            int j = brightness % 65536;
            int k = brightness / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(-1, 0.75, 6.5);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.rotate(90 * gun.shotRand, 1, 0, 0);
        GlStateManager.scale(0.75, 0.75, 0.75);
        renderMuzzleFlash(gun.lastShot[0], 150, 7.5);
        GlStateManager.popMatrix();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        double scale = 7.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0, -0.5, -0.25);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 4.75D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.translate(0, -1, 0);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -30D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, -1.125, 0);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        final int prevShade = RenderUtil.getShadeModel();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableLighting();
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.quadro_tex);
        ResourceManager.quadro.renderPart("Launcher");

        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(prevShade);
    }
}
