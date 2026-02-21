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

@AutoRegister(item = "gun_missile_launcher")
public class ItemRenderMissileLauncher extends ItemRenderWeaponBase {

    protected static String label = "AUTO";

    @Override
    protected float getTurnMagnitude(ItemStack stack) {
        return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F;
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0, 0, 0.875);

        float offset = 0.8F;
        standardAimingTransform(stack, -1.5F * offset, -1.25F * offset, 0.5F * offset, -1F * offset, -1.25F * offset, 0F * offset);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        EntityPlayer player = Minecraft.getMinecraft().player;
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.missile_launcher_tex);
        double scale = 0.5D;
        GlStateManager.scale(scale, scale, scale);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] barrel = HbmAnimationsSedna.getRelevantTransformation("BARREL");
        double[] open = HbmAnimationsSedna.getRelevantTransformation("OPEN");
        double[] missile = HbmAnimationsSedna.getRelevantTransformation("MISSILE");

        GlStateManager.translate(0, -2, -2);
        GlStateManager.rotate((float) equip[0], 1, 0, 0);
        GlStateManager.translate(0, 2, 2);
        final int prevShade = RenderUtil.getShadeModel();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(GL11.GL_SMOOTH);

        ResourceManager.missile_launcher.renderPart("Launcher");

        GlStateManager.pushMatrix();

        GlStateManager.translate(0, 0.25, 1.6875);
        GlStateManager.rotate((float) open[0], 1, 0, 0);
        GlStateManager.translate(0, -0.25, -1.6875);

        ResourceManager.missile_launcher.renderPart("Front");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, barrel[2]);
        ResourceManager.missile_launcher.renderPart("Barrel");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(missile[0], missile[1], missile[2]);
        ResourceManager.missile_launcher.renderPart("Missile");
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();

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
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0); // SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            float f3 = 0.04F;
            GlStateManager.translate(0.9375F, 2.25F, -0.5625F + (font.getStringWidth(label) / 2F) * f3);
            GlStateManager.scale(f3, -f3, f3);
            GlStateManager.rotate(90F, 0, 1, 0);
            GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F * f3);
            float variance = 0.7F + player.getRNG().nextFloat() * 0.3F;
            font.drawString(label, 0, 0, new Color(variance, 0F, 0F).getRGB());
            GlStateManager.color(1F, 1F, 1F);

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

        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(prevShade);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1, 6.75);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.rotate(gun.shotRand * 90, 1, 0, 0);
        GlStateManager.scale(0.75, 0.75, 0.75);
        renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
        GlStateManager.popMatrix();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        double scale = 2.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0, -0.5, -2);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 1.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.translate(0, -0.5, 0);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -10D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, -1, 0);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();

        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.missile_launcher_tex);
        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();

        final int prevShade = RenderUtil.getShadeModel();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(GL11.GL_SMOOTH);

        ResourceManager.missile_launcher.renderPart("Launcher");
        ResourceManager.missile_launcher.renderPart("Barrel");
        ResourceManager.missile_launcher.renderPart("Front");
        if (gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, null) > 0)
            ResourceManager.missile_launcher.renderPart("Missile");
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(prevShade);
    }
}
