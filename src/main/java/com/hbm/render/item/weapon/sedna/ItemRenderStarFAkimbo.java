package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister(item = "gun_star_f_akimbo")
public class ItemRenderStarFAkimbo extends ItemRenderWeaponBase {

    @Override public boolean isAkimbo() { return true; }

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
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();

        float offset = 0.8F;

        for(int i = -1; i <= 1; i += 2) {
            int index = i == -1 ? 0 : 1;
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.star_f_elite_tex);

            GlStateManager.pushMatrix();
            standardAimingTransform(stack, -2F * offset * i, -1.75F * offset, 2.5F * offset, 0, -7.625 / 8D, 1);

            double scale = 0.25D;
            GlStateManager.scale(scale, scale, scale);

            double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP", index);
            double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL", index);
            double[] hammer = HbmAnimationsSedna.getRelevantTransformation("HAMMER", index);
            double[] tilt = HbmAnimationsSedna.getRelevantTransformation("TILT", index);
            double[] turn = HbmAnimationsSedna.getRelevantTransformation("TURN", index);
            double[] mag = HbmAnimationsSedna.getRelevantTransformation("MAG", index);
            double[] bullet = HbmAnimationsSedna.getRelevantTransformation("BULLET", index);
            double[] slide = HbmAnimationsSedna.getRelevantTransformation("SLIDE", index);

            GlStateManager.translate(0, -2, -8);
            GlStateManager.rotate((float) equip[0], 1, 0, 0);
            GlStateManager.translate(0, 2, 8);

            GlStateManager.translate(0, 1, -3);
            GlStateManager.rotate((float) (turn[2] * i), 0, 0, 1);
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

            if(hasSilencer(stack, index)) {
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
                this.renderSmokeNodes(gun.getConfig(stack, index).smokeNodes, 0.75D);
                GlStateManager.popMatrix();

                GlStateManager.shadeModel(GL11.GL_FLAT);

                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 3, 6.125);
                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.rotate((float) (90 * gun.shotRand), 1, 0, 0);
                this.renderMuzzleFlash(gun.lastShot[index], 75, 7.5);
                GlStateManager.popMatrix();
            }

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
    public void setupThirdPersonAkimbo(ItemStack stack) {
        super.setupThirdPersonAkimbo(stack);
        GlStateManager.translate(0, -0.25, 1.75);
        double scale = 0.75D;
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    public void setupInv(ItemStack stack) {
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
        GlStateManager.enableAlpha();
        GlStateManager.scale(1, 1, -1);
        GlStateManager.translate(8, 8, 0);
        double scale = 1.5D;
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -6.25D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, -0.25, -5);
    }

    @Override
    public void renderEquipped(ItemStack stack) {
        renderStandardGun(stack, 1);
    }

    @Override
    public void renderEquippedAkimbo(ItemStack stack) {
        renderStandardGun(stack, 0);
    }

    @Override
    public void renderModTable(ItemStack stack, int index) {
        GlStateManager.enableLighting();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.star_f_elite_tex);
        ResourceManager.star_f.renderPart("Gun");
        ResourceManager.star_f.renderPart("Slide");
        ResourceManager.star_f.renderPart("Mag");
        ResourceManager.star_f.renderPart("Hammer");
        if(hasSilencer(stack, index)) {
            GlStateManager.translate(0, 2.375, -0.25);
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.uzi_tex);
            ResourceManager.uzi.renderPart("Silencer");
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public void renderEntity(ItemStack stack) {
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        boolean anySilenced = hasSilencer(stack, 0) || hasSilencer(stack, 1);

        if(anySilenced) {
            GlStateManager.scale(0.75, 0.75, 0.75);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(-1, 1, 0);
        renderStandardGun(stack, 1);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(1, 1, 0);
        renderStandardGun(stack, 0);
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        boolean anySilenced = hasSilencer(stack, 0) || hasSilencer(stack, 1);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(225, 0, 0, 1);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.translate(0.5, 0, 0);
        if(anySilenced) {
            double scale = 0.625D;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.translate(0, 0, -4);
        }
        renderStandardGun(stack, 1);
        GlStateManager.popMatrix();

        GlStateManager.translate(0, 0, 5);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(225, 0, 0, 1);
        GlStateManager.rotate(-90, 0, 1, 0);
        GlStateManager.rotate(-90, 1, 0, 0);
        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.translate(-0.5, 0, 0);
        if(anySilenced) {
            double scale = 0.625D;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.translate(0, 0, -4);
        }
        renderStandardGun(stack, 0);
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    public boolean hasSilencer(ItemStack stack, int cfg) {
        return XWeaponModManager.hasUpgrade(stack, cfg, XWeaponModManager.ID_SILENCER);
    }

    public void renderStandardGun(ItemStack stack, int index) {

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.star_f_elite_tex);
        ResourceManager.star_f.renderPart("Gun");
        ResourceManager.star_f.renderPart("Slide");
        ResourceManager.star_f.renderPart("Mag");
        ResourceManager.star_f.renderPart("Hammer");
        boolean silenced = hasSilencer(stack, index);
        if(silenced) {
            GlStateManager.translate(0, 2.375, -0.25);
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.uzi_tex);
            ResourceManager.uzi.renderPart("Silencer");
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }
}
