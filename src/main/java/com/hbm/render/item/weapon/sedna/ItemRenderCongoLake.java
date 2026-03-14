package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.particle.SpentCasing;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna.GunAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
@AutoRegister(item = "gun_congolake")
public class ItemRenderCongoLake extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) {
        return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F;
    }

    @Override
    public float getViewFOV(ItemStack stack, float fov) {
        float aimingProgress = ItemGunBaseNT.prevAimingProgress +
                (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
        return fov * (1 - aimingProgress * 0.33F);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0, 0, 0.875);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.5F * offset, -2F * offset, 1.25F * offset,
                0, -10 / 8D, 0.25);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.congolake_tex);
        double scale = 0.5D;
        GlStateManager.scale(scale, scale, scale);

        HbmAnimationsSedna.applyRelevantTransformation("Gun");
        ResourceManager.congolake.renderPart("Gun");

        GlStateManager.pushMatrix();
        HbmAnimationsSedna.applyRelevantTransformation("Pump");
        ResourceManager.congolake.renderPart("Pump");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        float aimingProgress = ItemGunBaseNT.prevAimingProgress +
                (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
        HbmAnimationsSedna.applyRelevantTransformation("Sight");
        GlStateManager.translate(0, 2.125, 3);
        GlStateManager.rotate(aimingProgress * -90, 1, 0, 0);
        GlStateManager.translate(0, -2.125, -3);
        ResourceManager.congolake.renderPart("Sight");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        HbmAnimationsSedna.applyRelevantTransformation("Loop");
        ResourceManager.congolake.renderPart("Loop");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        HbmAnimationsSedna.applyRelevantTransformation("GuardOuter");
        ResourceManager.congolake.renderPart("GuardOuter");

        GlStateManager.pushMatrix();
        HbmAnimationsSedna.applyRelevantTransformation("GuardInner");
        ResourceManager.congolake.renderPart("GuardInner");
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        IMagazine mag = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
        if (gun.getLastAnim(stack, 0) != GunAnimation.INSPECT ||
                mag.getAmount(stack, MainRegistry.proxy.me().inventory) > 0) { //omit when inspecting and no shell is loaded

            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.casings_tex);

            HbmAnimationsSedna.applyRelevantTransformation("Shell");

            SpentCasing casing = mag.getCasing(stack, MainRegistry.proxy.me().inventory);
            int[] colors = casing != null ? casing.getColors() : new int[]{SpentCasing.COLOR_CASE_40MM};

            Color shellColor = new Color(colors[0]);
            GlStateManager.color(shellColor.getRed() / 255F,
                    shellColor.getGreen() / 255F,
                    shellColor.getBlue() / 255F);
            ResourceManager.congolake.renderPart("Shell");

            Color shellForeColor = new Color(colors.length > 1 ? colors[1] : colors[0]);
            GlStateManager.color(shellForeColor.getRed() / 255F,
                    shellForeColor.getGreen() / 255F,
                    shellForeColor.getBlue() / 255F);
            ResourceManager.congolake.renderPart("ShellFore");

            GlStateManager.color(1F, 1F, 1F);
        }
        GlStateManager.popMatrix();

        double smokeScale = 0.25;

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.75, 4.25);
        double[] transform = HbmAnimationsSedna.getRelevantTransformation("Gun");
        GlStateManager.rotate((float) -transform[5], 0, 0, 1);
        GlStateManager.rotate((float) -transform[4], 0, 1, 0);
        GlStateManager.rotate((float) -transform[3], 1, 0, 0);
        GlStateManager.rotate(90F, 0, 1, 0);
        GlStateManager.scale(smokeScale, smokeScale, smokeScale);
        this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 1D);
        GlStateManager.popMatrix();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.75, 4.25);
        GlStateManager.rotate(90F, 0, 1, 0);
        GlStateManager.rotate(90F * gun.shotRand, 1, 0, 0);
        GlStateManager.scale(0.5, 0.5, 0.5);
        this.renderMuzzleFlash(gun.lastShot[0], 150, 7.5);
        GlStateManager.popMatrix();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        GlStateManager.translate(0, -2.5, 4);
        double scale = 2.5D;
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 2.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(25F, 1, 0, 0);
        GlStateManager.rotate(45F, 0, 1, 0);
        GlStateManager.translate(0, -1.25, 0);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -15D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, -1.25, 0);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.congolake_tex);
        ResourceManager.congolake.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }
}

