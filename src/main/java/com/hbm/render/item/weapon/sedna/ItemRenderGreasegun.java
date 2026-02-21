package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mods.WeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_greasegun")
public class ItemRenderGreasegun extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public float getViewFOV(ItemStack stack, float fov) {
        float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
        return  fov * (1 - aimingProgress * 0.33F);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0, 0, 0.875);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.5F * offset, -1F * offset, 1.75F * offset,
                0, -2.625 / 8D, 1.125);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        Minecraft.getMinecraft().renderEngine.bindTexture(isRefurbished(stack) ? ResourceManager.greasegun_clean_tex : ResourceManager.greasegun_tex);
        double scale = 0.375D;
        GlStateManager.scale(scale, scale, scale);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] stock = HbmAnimationsSedna.getRelevantTransformation("STOCK");
        double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
        double[] flap = HbmAnimationsSedna.getRelevantTransformation("FLAP");
        double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
        double[] handle = HbmAnimationsSedna.getRelevantTransformation("HANDLE");
        double[] mag = HbmAnimationsSedna.getRelevantTransformation("MAG");
        double[] turn = HbmAnimationsSedna.getRelevantTransformation("TURN");
        double[] bullet = HbmAnimationsSedna.getRelevantTransformation("BULLET");

        GlStateManager.translate(0, -3, -3);
        GlStateManager.rotate(equip[0], 1, 0, 0);
        GlStateManager.translate(0, 3, 3);

        GlStateManager.translate(0, -3, -3);
        GlStateManager.rotate(lift[0], 1, 0, 0);
        GlStateManager.translate(0, 3, 3);

        if(ItemGunBaseNT.aimingProgress < 1F) GlStateManager.rotate(turn[2], 0, 0, 1);

        GlStateManager.translate(0, 0, recoil[2]);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.greasegun.renderPart("Gun");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, -4 - stock[2]);
        ResourceManager.greasegun.renderPart("Stock");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(mag[0], mag[1], mag[2]);
        ResourceManager.greasegun.renderPart("Magazine");
        if(bullet[0] != 1) ResourceManager.greasegun.renderPart("Bullet");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -1.4375, -0.125);
        GlStateManager.rotate(handle[0], 1, 0, 0);
        GlStateManager.translate(0, 1.4375, 0.125);
        ResourceManager.greasegun.renderPart("Handle");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.53125, 0);
        GlStateManager.rotate(flap[2], 0, 0, 1);
        GlStateManager.translate(0, -0.5125, 0);
        ResourceManager.greasegun.renderPart("Flap");
        GlStateManager.popMatrix();

        double smokeScale = 0.25;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.25, 0, 1.5);
        GlStateManager.rotate(turn[2], 0, 0, -1);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 1D);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 8);
        GlStateManager.rotate(turn[2], 0, 0, -1);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 1D);
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 8);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.rotate(90 * gun.shotRand, 1, 0, 0);
        GlStateManager.scale(0.5, 0.5, 0.5);
        renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
        GlStateManager.popMatrix();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        GlStateManager.translate(0, 1, 3);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 1.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.translate(-0.5, 2, 0);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -7.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, 2, 0);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        int prevShade = RenderUtil.getShadeModel();
        GlStateManager.enableLighting();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(isRefurbished(stack) ? ResourceManager.greasegun_clean_tex : ResourceManager.greasegun_tex);
        ResourceManager.greasegun.renderAll();
        GlStateManager.shadeModel(prevShade);
    }

    public boolean isRefurbished(ItemStack stack) {
        return WeaponModManager.hasUpgrade(stack, 0, WeaponModManager.ID_GREASEGUN_CLEAN);
    }
}
