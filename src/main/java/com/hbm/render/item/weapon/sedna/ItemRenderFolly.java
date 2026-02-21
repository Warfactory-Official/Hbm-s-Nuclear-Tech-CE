package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@AutoRegister(item = "gun_folly")
public class ItemRenderFolly extends ItemRenderWeaponBase {

    public static long timeAiming;
    public static boolean jingle = false;
    public static boolean wasAiming = false;

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 2F : 2.5F; }

    @Override
    public float getViewFOV(ItemStack stack, float fov) {
        float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
        return fov * (1 - aimingProgress * 0.33F);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0, 0, 0.875);

        float offset = 0.8F;
        float aim = 0.75F;
        standardAimingTransform(stack,
                -2.5F * offset, -1.5F * offset, 2.75F * offset,
                -2 * aim, -1 * aim, 2.25F * offset);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        EntityPlayer player = Minecraft.getMinecraft().player;
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.folly_tex);
        double scale = 0.75D;
        GlStateManager.scale(scale, scale, scale);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
        double[] load = HbmAnimationsSedna.getRelevantTransformation("LOAD");
        double[] shell = HbmAnimationsSedna.getRelevantTransformation("SHELL");
        double[] screw = HbmAnimationsSedna.getRelevantTransformation("SCREW");
        double[] breech = HbmAnimationsSedna.getRelevantTransformation("BREECH");

        GlStateManager.translate(0, 1, -4);
        GlStateManager.rotate(-equip[0], 1, 0, 0);
        GlStateManager.translate(0, -1, 4);

        GlStateManager.translate(0, -2, -2);
        GlStateManager.rotate(load[0], 1, 0, 0);
        GlStateManager.translate(0, 2, 2);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        ResourceManager.folly.renderPart("Cannon");

        GlStateManager.pushMatrix();
        GlStateManager.translate(recoil[0], recoil[1], recoil[2]);
        ResourceManager.folly.renderPart("Barrel");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(shell[0], shell[1], shell[2]);
        ResourceManager.folly.renderPart("Shell");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(breech[0], breech[1], breech[2]);
        ResourceManager.folly.renderPart("Breech");
        GlStateManager.translate(0, 1, 0);
        GlStateManager.rotate(screw[2], 0, 0, 1);
        GlStateManager.translate(0, -1, 0);
        ResourceManager.folly.renderPart("Cog");
        GlStateManager.popMatrix();

        boolean isAiming = gun.prevAimingProgress >= 1F && gun.aimingProgress >= 1F;
        if (isAiming & !wasAiming) timeAiming = System.currentTimeMillis();

        if (isAiming) {

            String splash = getBootSplash();

            if (!jingle && !splash.isEmpty()) {
                MainRegistry.proxy.playSoundClient(player.posX, player.posY, player.posZ,
                        new SoundEvent(new ResourceLocation("hbm:weapon.fire.vstar")),
                        SoundCategory.PLAYERS, 0.5F, 1F);
                jingle = true;
            }

            GlStateManager.pushMatrix();

            final boolean prevLighting = RenderUtil.isLightingEnabled();
            final boolean prevCull     = RenderUtil.isCullEnabled();
            final boolean prevBlend    = RenderUtil.isBlendEnabled();
            final int prevSrc          = RenderUtil.getBlendSrcFactor();
            final int prevDst          = RenderUtil.getBlendDstFactor();
            final int prevSrcAlpha     = RenderUtil.getBlendSrcAlphaFactor();
            final int prevDstAlpha     = RenderUtil.getBlendDstAlphaFactor();

            if (prevLighting) GlStateManager.disableLighting();
            if (prevCull)     GlStateManager.disableCull();
            if (!prevBlend)   GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            float variance = 0.85F + player.getRNG().nextFloat() * 0.15F;

            if (System.currentTimeMillis() - timeAiming > 5000 && load[0] == 0) {
                IMagazine mag = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
                String msg = mag.getAmount(stack, player.inventory) > 0 ? "+" : "No ammo";
                GlStateManager.pushMatrix();
                float crosshairSize = 0.01F;
                GlStateManager.translate((font.getStringWidth(msg) / 2) * crosshairSize + 2, 1F + font.FONT_HEIGHT * crosshairSize / 2F, -2.75F);
                GlStateManager.scale(crosshairSize, -crosshairSize, crosshairSize);
                GlStateManager.rotate(180D, 0, 1, 0);
                GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F * crosshairSize);
                font.drawString(msg, 0, 0, new Color(variance, variance * 0.5F, 0F).getRGB());
                GlStateManager.popMatrix();
            }

            GlStateManager.pushMatrix();
            float splashSize = 0.02F;
            GlStateManager.translate((font.getStringWidth(splash) / 2) * splashSize + 2, 1F + font.FONT_HEIGHT * splashSize / 2F, -2.75F);
            GlStateManager.scale(splashSize, -splashSize, splashSize);
            GlStateManager.rotate(180D, 0, 1, 0);
            GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F * splashSize);
            font.drawString(splash, 0, 0, new Color(variance, variance * 0.5F, 0F).getRGB());
            GlStateManager.popMatrix();

            List<String> tty = getTTY();
            if (!tty.isEmpty()) {
                GlStateManager.pushMatrix();
                float fontSize = 0.005F;
                GlStateManager.translate(2.5F, 1.375F, -2.75F);
                GlStateManager.scale(fontSize, -fontSize, fontSize);
                GlStateManager.rotate(180D, 0, 1, 0);
                GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F * fontSize);
                for (String line : tty) {
                    font.drawString(line, 0, 0, new Color(variance, variance * 0.5F, 0F).getRGB());
                    GL11.glTranslated(0, (font.FONT_HEIGHT + 2), 0);
                }
                GlStateManager.popMatrix();
            }

            GlStateManager.color(1F, 1F, 1F);

            GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcAlpha, prevDstAlpha);
            if (!prevBlend)   GlStateManager.disableBlend();
            if (prevCull)     GlStateManager.enableCull();
            if (prevLighting) GlStateManager.enableLighting();

            GlStateManager.popMatrix();

            int brightness = player.world.getCombinedLight(new BlockPos(player.posX, player.posY, player.posZ), 0);
            int j = brightness % 65536;
            int k = brightness / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
        } else {
            jingle = false;
        }

        wasAiming = isAiming;

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        double scale = 3D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-0.25, 0.5, 3);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 1.25D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.translate(0, -0.5, 0);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -8.75D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, -1, 0);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.folly_tex);
        ResourceManager.folly.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    public static String getBootSplash() {
        long now = System.currentTimeMillis();
        if (timeAiming + 5000 < now) return "";
        if (timeAiming + 3000 > now) return "";
        int splashIndex = (int) ((now - timeAiming - 3000) * 35 / 2000) - 10;
        char[] letters = "VStarOS".toCharArray();
        String splash = "";
        for (int i = 0; i < letters.length; i++) {
            if (i < splashIndex - 1) splash += TextFormatting.LIGHT_PURPLE;
            if (i == splashIndex - 1) splash += TextFormatting.AQUA;
            if (i == splashIndex) splash += TextFormatting.WHITE;
            if (i == splashIndex + 1) splash += TextFormatting.AQUA;
            if (i == splashIndex + 2) splash += TextFormatting.LIGHT_PURPLE;
            if (i > splashIndex + 2) splash += TextFormatting.BLACK;
            splash += letters[i];
        }
        return splash;
    }

    public static List<String> getTTY() {
        List<String> tty = new ArrayList<>();
        long now = System.currentTimeMillis();
        int time = (int) ((now - timeAiming));
        if (time < 3000) {
            if (time > 250) tty.add(TextFormatting.GREEN + "POST successful - Code 0");
            if (time > 500) tty.add(TextFormatting.GREEN + "8,388,608 bytes of RAM installed");
            if (time > 500) tty.add(TextFormatting.GREEN + "5,187,427 bytes available");
            if (time > 750) tty.add(TextFormatting.GREEN + "Reticulating splines...");
            if (time > 1500) tty.add(TextFormatting.GREEN + "No keyboard found!");
            if (time > 2000) tty.add(TextFormatting.GREEN + "Booting from /dev/sda1...");
        }
        if (time > 5000) {
            EntityPlayer player = MainRegistry.proxy.me();
            RayTraceResult mop = EntityDamageUtil.getMouseOver(player, 250);
            String target = TextFormatting.GREEN + "Target: ";
            if (mop.typeOfHit == RayTraceResult.Type.MISS) target += "N/A";
            if (mop.typeOfHit == RayTraceResult.Type.BLOCK)
                target += mop.getBlockPos().getX() + "/" + mop.getBlockPos().getY() + "/" + mop.getBlockPos().getZ();
            if (mop.typeOfHit == RayTraceResult.Type.ENTITY) target += mop.entityHit.getName();
            tty.add(target);
            tty.add(TextFormatting.GREEN + "Angle: " + ((int) (-player.rotationPitch * 100) / 100D));
        }
        return tty;
    }
}
