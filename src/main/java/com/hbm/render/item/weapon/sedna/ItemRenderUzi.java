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
@AutoRegister(item = "gun_uzi")
public class ItemRenderUzi extends ItemRenderWeaponBase {

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
                -1.75F * offset, -1.5F * offset, 2.5F * offset,
                0, -4.375 / 8D, 1);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        Minecraft.getMinecraft().renderEngine.bindTexture(isSaturnite(stack) ? ResourceManager.uzi_saturnite_tex : ResourceManager.uzi_tex);
        double scale = 0.25D;
        GlStateManager.scale(scale, scale, scale);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] stockFront = HbmAnimationsSedna.getRelevantTransformation("STOCKFRONT");
        double[] stockBack = HbmAnimationsSedna.getRelevantTransformation("STOCKBACK");
        double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
        double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
        double[] mag = HbmAnimationsSedna.getRelevantTransformation("MAG");
        double[] bullet = HbmAnimationsSedna.getRelevantTransformation("BULLET");
        double[] slide = HbmAnimationsSedna.getRelevantTransformation("SLIDE");
        double[] yeet = HbmAnimationsSedna.getRelevantTransformation("YEET");
        double[] speen = HbmAnimationsSedna.getRelevantTransformation("SPEEN");

        GlStateManager.translate(yeet[0], yeet[1], yeet[2]);
        GlStateManager.rotate(speen[0], 0, 0, 1);

        GlStateManager.translate(0, -2, -4);
        GlStateManager.rotate(equip[0], 1, 0, 0);
        GlStateManager.translate(0, 2, 4);

        GlStateManager.translate(0, 0, -6);
        GlStateManager.rotate(lift[0], 1, 0, 0);
        GlStateManager.translate(0, 0, 6);

        GlStateManager.translate(0, 0, recoil[2]);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.uzi.renderPart("Gun");

        boolean silenced = hasSilencer(stack, 0);
        if(silenced) ResourceManager.uzi.renderPart("Silencer");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.3125D, -5.75);
        GlStateManager.rotate(180 - stockFront[0], 1, 0, 0);
        GlStateManager.translate(0, -0.3125D, 5.75);
        ResourceManager.uzi.renderPart("StockFront");

        GlStateManager.translate(0, -0.3125D, -3);
        GlStateManager.rotate(-200 - stockBack[0], 1, 0, 0);
        GlStateManager.translate(0, 0.3125D, 3);
        ResourceManager.uzi.renderPart("StockBack");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, slide[2]);
        ResourceManager.uzi.renderPart("Slide");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(mag[0], mag[1], mag[2]);
        ResourceManager.uzi.renderPart("Magazine");
        if(bullet[0] == 1) ResourceManager.uzi.renderPart("Bullet");
        GlStateManager.popMatrix();
        if(!silenced) {
            double smokeScale = 0.5;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.75, 8.5);
            GlStateManager.rotate(90, 0, 1, 0);
            GlStateManager.scale(smokeScale, smokeScale, smokeScale);
            this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.75D);
            GlStateManager.popMatrix();

            GlStateManager.shadeModel(GL11.GL_FLAT);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.75, 8.5);
            GlStateManager.rotate(90, 0, 1, 0);
            GlStateManager.rotate(90 * gun.shotRand, 1, 0, 0);
            this.renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        GlStateManager.translate(0, 1, 1);

    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 1.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.translate(0, 1, 0);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -6.25D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, 1, -4);
    }

    @Override
    public void renderModTable(ItemStack stack, int index) {
        GlStateManager.enableLighting();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(isSaturnite(stack) ? ResourceManager.uzi_saturnite_tex : ResourceManager.uzi_tex);
        ResourceManager.uzi.renderPart("Gun");
        ResourceManager.uzi.renderPart("StockBack");
        ResourceManager.uzi.renderPart("StockFront");
        ResourceManager.uzi.renderPart("Slide");
        ResourceManager.uzi.renderPart("Magazine");
        if(hasSilencer(stack, index)) ResourceManager.uzi.renderPart("Silencer");
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();

        boolean silenced = hasSilencer(stack, 0);

        if(silenced && type == ItemCameraTransforms.TransformType.GUI) {
            double scale = 0.625D;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.translate(0, 0, -4);
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(isSaturnite(stack) ? ResourceManager.uzi_saturnite_tex : ResourceManager.uzi_tex);
        ResourceManager.uzi.renderPart("Gun");
        ResourceManager.uzi.renderPart("StockBack");
        ResourceManager.uzi.renderPart("StockFront");
        ResourceManager.uzi.renderPart("Slide");
        ResourceManager.uzi.renderPart("Magazine");
        if(silenced) ResourceManager.uzi.renderPart("Silencer");
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    public boolean hasSilencer(ItemStack stack, int cfg) {
        return XWeaponModManager.hasUpgrade(stack, cfg, XWeaponModManager.ID_SILENCER);
    }

    public boolean isSaturnite(ItemStack stack) {
        return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_UZI_SATURN);
    }
}
