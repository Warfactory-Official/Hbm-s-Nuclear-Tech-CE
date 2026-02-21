package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_drill")
public class ItemRenderDrill extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 0F : -0.5F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0, 0, 0.875);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.25F * offset, -1.75F * offset, 1.75F * offset,
                -1F * offset, -1.75F * offset, 1.25F * offset);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.drill_tex);
        double scale = 0.375D;
        GlStateManager.scale(scale, scale, scale);

        IMagazine mag = gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);
        double gauge = (double) mag.getAmount(stack, null) / (double) mag.getCapacity(stack);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] deploy = HbmAnimationsSedna.getRelevantTransformation("DEPLOY");
        double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
        double[] spin = HbmAnimationsSedna.getRelevantTransformation("SPIN");

        GlStateManager.rotate(15 * (1 - deploy[0] * 0.5), 0, 1, 0);
        GlStateManager.rotate(-10 * (1 - deploy[0] * 0.5), 1, 0, 0);

        GlStateManager.translate(0, 2, -6);
        GlStateManager.rotate(equip[0] * -45, 0, 1, 0);
        GlStateManager.rotate(equip[0] * -20, 1, 0, 0);
        GlStateManager.translate(0, -2, 6);

        GlStateManager.rotate(lift[0], 1, 0, 0);

        GlStateManager.translate(0, 0, deploy[0]);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        ResourceManager.drill.renderPart("Base");

        GlStateManager.pushMatrix();
        GlStateManager.translate(1, 2.0625, -1.75);
        GlStateManager.rotate(45, 1, 0, 0);
        GlStateManager.rotate(-135 + gauge * 270, 0, 0, 1);
        GlStateManager.rotate(-45, 1, 0, 0);
        GlStateManager.translate(-1, -2.0625, 1.75);
        ResourceManager.drill.renderPart("Gauge");
        GlStateManager.popMatrix();

        double rot = spin[0];
        double rot2 = rot * 5;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, Math.sin(rot2 * Math.PI / 180) * 0.125 - 0.125, 0);
        ResourceManager.drill.renderPart("Piston1");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, Math.sin(rot2 * Math.PI / 180 + Math.PI * 2D / 3D) * 0.125 - 0.125, 0);
        ResourceManager.drill.renderPart("Piston2");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, Math.sin(rot2 * Math.PI / 180 + Math.PI * 4D / 3D) * 0.125 - 0.125, 0);
        ResourceManager.drill.renderPart("Piston3");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(rot, 0, 0, -1);
        ResourceManager.drill.renderPart("DrillBack");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.rotate(rot, 0, 0, 1);
        ResourceManager.drill.renderPart("DrillFront");
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        double scale = 2.25D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(1, -2, 6);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 1.25D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.translate(-0.5, 0, 0);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -8.75D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, 0, 0);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        int prevShade = RenderUtil.getShadeModel();
        GlStateManager.enableLighting();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.drill_tex);
        ResourceManager.drill.renderAll();
        GlStateManager.shadeModel(prevShade);
    }
}

