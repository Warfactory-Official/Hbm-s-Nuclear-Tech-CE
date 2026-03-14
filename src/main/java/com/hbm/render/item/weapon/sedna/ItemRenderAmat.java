package com.hbm.render.item.weapon.sedna;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ItemRenderAmat extends ItemRenderWeaponBase {

    public ResourceLocation texture;

    public ItemRenderAmat(ResourceLocation texture) {
        this.texture = texture;
        this.offsets = offsets.get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND).setScale(1).setPosition(-0.65 , 0.15, -0.9).getHelper();
    }

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F; }

    @Override
    public float getViewFOV(ItemStack stack, float fov) {
        float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
        return  fov * (1 - aimingProgress * (isScoped(stack) ? 0.8F : 0.33F));
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0, 0, 0.875);

        float offset = 0.8F;

        standardAimingTransform(stack,
                -1F * offset, -1F * offset, 3.25F * offset,
                0, -4.875 / 8D, 1.875);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {
        boolean isScoped = isScoped(stack);
        if(isScoped && ItemGunBaseNT.prevAimingProgress == 1 && ItemGunBaseNT.aimingProgress == 1) return;

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        double scale = 0.375D;
        GlStateManager.scale(scale, scale, scale);

        boolean deployed = HbmAnimationsSedna.getRelevantAnim(0) == null || HbmAnimationsSedna.getRelevantAnim(0).animation.getBus("BIPOD") == null;
        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] bipod = HbmAnimationsSedna.getRelevantTransformation("BIPOD");
        double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
        double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
        double[] boltTurn = HbmAnimationsSedna.getRelevantTransformation("BOLT_TURN");
        double[] boltPull = HbmAnimationsSedna.getRelevantTransformation("BOLT_PULL");
        double[] mag = HbmAnimationsSedna.getRelevantTransformation("MAG");
        double[] scopeThrow = HbmAnimationsSedna.getRelevantTransformation("SCOPE_THROW");
        double[] scopeSpin = HbmAnimationsSedna.getRelevantTransformation("SCOPE_SPIN");

        GlStateManager.translate(0, 0, recoil[2]);

        GlStateManager.translate(0, -3, -8);
        GlStateManager.rotate(equip[0], 1, 0, 0);
        GlStateManager.rotate(lift[0], 1, 0, 0);
        GlStateManager.translate(0, 3, 8);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.amat.renderPart("Gun");

        if(isScoped(stack)) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(scopeThrow[0], scopeThrow[1], scopeThrow[2]);
            GlStateManager.translate(0, 1.5, -4.5);
            GlStateManager.rotate(scopeSpin[0], 1, 0, 0);
            GlStateManager.translate(0, -1.5, 4.5);
            ResourceManager.amat.renderPart("Scope");
            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.625, 0);
        GlStateManager.rotate(boltTurn[2], 0, 0, 1);
        GlStateManager.translate(0, -0.625, 0);
        GlStateManager.translate(0, 0, boltPull[2]);
        ResourceManager.amat.renderPart("Bolt");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(mag[0], mag[1], mag[2]);
        ResourceManager.amat.renderPart("Magazine");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.3125, -0.625, -1);
        GlStateManager.rotate(deployed ? 25 : bipod[1], 0, 0, 1);
        GlStateManager.translate(-0.3125, 0.625, 1);
        ResourceManager.amat.renderPart("BipodHingeLeft");
        GlStateManager.translate(0.3125, -0.625, -1);
        GlStateManager.rotate(deployed ? 80 : bipod[0], 1, 0, 0);
        GlStateManager.translate(-0.3125, 0.625, 1);
        ResourceManager.amat.renderPart("BipodLeft");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.3125, -0.625, -1);
        GlStateManager.rotate(deployed ? -25 : -bipod[1], 0, 0, 1);
        GlStateManager.translate(0.3125, 0.625, 1);
        ResourceManager.amat.renderPart("BipodHingeRight");
        GlStateManager.translate(-0.3125, -0.625, -1);
        GlStateManager.rotate(deployed ? 80 : bipod[0], 1, 0, 0);
        GlStateManager.translate(0.3125, 0.625, 1);
        ResourceManager.amat.renderPart("BipodRight");
        GlStateManager.popMatrix();

        if(isSilenced(stack)) {
            GlStateManager.translate(0, 0.625, -4.3125);
            GlStateManager.scale(1.25, 1.25, 1.25);
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.g3_attachments);
            ResourceManager.g3.renderPart("Silencer");

            GlStateManager.shadeModel(GL11.GL_FLAT);
        } else {
            ResourceManager.amat.renderPart("MuzzleBrake");

            double smokeScale = 0.5;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.625, 12);
            GlStateManager.rotate(90, 0, 1, 0);
            GlStateManager.scale(smokeScale, smokeScale, smokeScale);
            this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 1D);
            GlStateManager.popMatrix();

            GlStateManager.shadeModel(GL11.GL_FLAT);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.5, 11);
            GlStateManager.rotate(90, 0, 1, 0);
            GlStateManager.scale(0.75, 0.75, 0.75);
            this.renderGapFlash(gun.lastShot[0]);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        double scale = 1.25D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0, 0.5, 6.75);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        if(isSilenced(stack)) {
            double scale = 0.8175D;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.rotate(25, 1, 0, 0);
            GlStateManager.rotate(45, 0, 1, 0);
            GlStateManager.translate(-0.5, 0.5, -1);
        } else {
            double scale = 0.9375D;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.rotate(25, 1, 0, 0);
            GlStateManager.rotate(45, 0, 1, 0);
            GlStateManager.translate(-0.5, 0.5, 0);
        }
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -5.75D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, -0.25, -1.5);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        ResourceManager.amat.renderPart("Gun");
        ResourceManager.amat.renderPart("Bolt");
        ResourceManager.amat.renderPart("Magazine");
        ResourceManager.amat.renderPart("BipodLeft");
        ResourceManager.amat.renderPart("BipodHingeLeft");
        ResourceManager.amat.renderPart("BipodRight");
        ResourceManager.amat.renderPart("BipodHingeRight");
        if(isScoped(stack)) ResourceManager.amat.renderPart("Scope");
        if(isSilenced(stack)) {
            GlStateManager.translate(0, 0.625, -4.3125);
            GlStateManager.scale(1.25, 1.25, 1.25);
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.g3_attachments);
            ResourceManager.g3.renderPart("Silencer");
        } else {
            ResourceManager.amat.renderPart("MuzzleBrake");
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    public boolean isScoped(ItemStack stack) {
        return true;
    }

    public boolean isSilenced(ItemStack stack) {
        return stack.getItem() == ModItems.gun_amat_penance || XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SILENCER);
    }
}
