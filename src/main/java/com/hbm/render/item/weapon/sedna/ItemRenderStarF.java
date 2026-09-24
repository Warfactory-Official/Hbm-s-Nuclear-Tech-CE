package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister(item = "gun_star_f")
public class ItemRenderStarF extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F; }

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
                -1.75F * offset, -1.75F * offset, 2.5F * offset,
                0, -7.625 / 8D, 1);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.star_f_tex);
        double scale = 0.25D;
        GlStateManager.scale(scale, scale, scale);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
        double[] hammer = HbmAnimationsSedna.getRelevantTransformation("HAMMER");
        double[] tilt = HbmAnimationsSedna.getRelevantTransformation("TILT");
        double[] turn = HbmAnimationsSedna.getRelevantTransformation("TURN");
        double[] mag = HbmAnimationsSedna.getRelevantTransformation("MAG");
        double[] bullet = HbmAnimationsSedna.getRelevantTransformation("BULLET");
        double[] slide = HbmAnimationsSedna.getRelevantTransformation("SLIDE");

        GlStateManager.translate(0, -2, -8);
        GlStateManager.rotate((float) equip[0], 1, 0, 0);
        GlStateManager.translate(0, 2, 8);

        GlStateManager.translate(0, 1, -3);
        GlStateManager.rotate((float) turn[2], 0, 0, 1);
        GlStateManager.rotate((float) tilt[0], 1, 0, 0);
        GlStateManager.translate(0, -1, 3);

        GlStateManager.translate(0, 0, recoil[2]);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.star_f.renderPart("Gun");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.75, -4.25);
        GlStateManager.rotate((float) (60 * (hammer[0] - 1)), 1, 0, 0);
        GlStateManager.translate(0, -1.75, 4.25);
        ResourceManager.star_f.renderPart("Hammer");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, slide[2] * 2.3125);
        ResourceManager.star_f.renderPart("Slide");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(mag[0], mag[1], mag[2]);
        ResourceManager.star_f.renderPart("Mag");
        GlStateManager.translate(bullet[0], bullet[1], bullet[2]);
        ResourceManager.star_f.renderPart("Bullet");
        GlStateManager.popMatrix();

        if(hasSilencer(stack)) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 2.375, -0.25);
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.uzi_tex);
            ResourceManager.uzi.renderPart("Silencer");
            GlStateManager.popMatrix();

        } else {
            double smokeScale = 0.5;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 3, 6.125);
            GlStateManager.rotate(90, 0, 1, 0);
            GlStateManager.scale(smokeScale, smokeScale, smokeScale);
            this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.75D);
            GlStateManager.popMatrix();

            GlStateManager.shadeModel(GL11.GL_FLAT);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 3, 6.125);
            GlStateManager.scale(0.75, 0.75, 0.75);
            GlStateManager.rotate(90, 0, 1, 0);
            GlStateManager.rotate((float) (90 * gun.shotRand), 1, 0, 0);
            this.renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        GlStateManager.translate(0, -0.25, 1.75);
        double scale = 0.75D;
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 1.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.translate(-1, -0.5, 0);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -6.25D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, -0.25, -5);
    }

    @Override
    public void renderModTable(ItemStack stack, int index) {
        GlStateManager.enableLighting();

        renderStandardGun(stack);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();

        boolean silenced = hasSilencer(stack);

        if(silenced && type == ItemCameraTransforms.TransformType.GUI) {
            double scale = 0.625D;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.translate(0, 0, -6);
        }

        renderStandardGun(stack);
    }

    public boolean hasSilencer(ItemStack stack) {
        return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SILENCER);
    }

    public void renderStandardGun(ItemStack stack) {

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.star_f_tex);
        ResourceManager.star_f.renderPart("Gun");
        ResourceManager.star_f.renderPart("Slide");
        ResourceManager.star_f.renderPart("Mag");
        ResourceManager.star_f.renderPart("Hammer");
        boolean silenced = hasSilencer(stack);
        if(silenced) {
            GlStateManager.translate(0, 2.375, -0.25);
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.uzi_tex);
            ResourceManager.uzi.renderPart("Silencer");
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }
}
