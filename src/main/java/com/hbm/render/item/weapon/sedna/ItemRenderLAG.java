package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_lag")
public class ItemRenderLAG extends ItemRenderWeaponBase {

    public ItemRenderLAG() {}

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
                -1.5F * offset, -1F * offset, 1.5F * offset,
                0, -3.375 / 8D, 0.5);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.mike_hawk_tex);
        double scale = 0.25D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] addTrans = HbmAnimationsSedna.getRelevantTransformation("ADD_TRANS");
        double[] addRot = HbmAnimationsSedna.getRelevantTransformation("ADD_ROT");

        GlStateManager.translate(4, -4, 0);
        GlStateManager.rotate((float)-equip[0], 0, 0, 1);
        GlStateManager.translate(-4, 4, 0);

        GlStateManager.translate(addTrans[0], addTrans[1], addTrans[2]);
        GlStateManager.rotate((float) addRot[2], 0, 0, 1);
        GlStateManager.rotate((float)addRot[1], 0, 1, 0);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.pushMatrix();
        HbmAnimationsSedna.applyRelevantTransformation("Grip");
        ResourceManager.mike_hawk.renderPart("Grip");

        GlStateManager.pushMatrix();
        HbmAnimationsSedna.applyRelevantTransformation("Slide");
		
		/*if(anim != null) {
			BusAnimationSequence slideSeq = anim.animation.getBus("Hammer");
			if(slideSeq != null) GlStateManager.translate(0, 0.75, 0);
		}*/

        ResourceManager.mike_hawk.renderPart("Slide");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(3.125, 0.125, 0);
        GlStateManager.rotate(-25, 0, 0, 1);
        GlStateManager.translate(-3.125, -0.125, 0);
        HbmAnimationsSedna.applyRelevantTransformation("Hammer");
        ResourceManager.mike_hawk.renderPart("Hammer");
        GlStateManager.popMatrix();

        if(gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack).getAmount(stack, null) > 0) {
            GlStateManager.pushMatrix();
            HbmAnimationsSedna.applyRelevantTransformation("Bullet");
            ResourceManager.mike_hawk.renderPart("Bullet");
            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();
        HbmAnimationsSedna.applyRelevantTransformation("Magazine");
        ResourceManager.mike_hawk.renderPart("Magazine");
        GlStateManager.popMatrix();

        double smokeScale = 0.5;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-10.25, 1, 0);
        GlStateManager.scale(smokeScale, smokeScale, smokeScale);
        this.renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.5D);
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-10.25, 1, 0);
        GlStateManager.rotate((float)(90 * gun.shotRand), 1, 0, 0);
        this.renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
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
        GlStateManager.translate(2.5, 1, 0);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -7.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, 0.5, -2);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();
        GlStateManager.rotate(90, 0, 1, 0);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.mike_hawk_tex);
        ResourceManager.mike_hawk.renderPart("Grip");
        ResourceManager.mike_hawk.renderPart("Slide");
        ResourceManager.mike_hawk.renderPart("Hammer");
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }
}
