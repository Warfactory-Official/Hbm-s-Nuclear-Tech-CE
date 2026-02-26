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
@AutoRegister(item = "gun_minigun_dual")
public class ItemRenderMinigunDual extends ItemRenderWeaponBase {

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
        GlStateManager.translate(0.0F, 0.0F, 0.875F);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();

        float offset = 0.8F;

        for (int i = -1; i <= 1; i += 2) {
            int index = i == -1 ? 0 : 1;
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.minigun_dual_tex);

            GlStateManager.pushMatrix();
            standardAimingTransform(stack, -2.75F * offset * i, -1.75F * offset, 2.5F * offset, 0, 0, 0);

            double scale = 0.375D;
            GlStateManager.scale((float) scale, (float) scale, (float) scale);

            double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP", index);
            double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL", index);
            double[] rotate = HbmAnimationsSedna.getRelevantTransformation("ROTATE", index);

            GlStateManager.translate(0.0F, 3.0F, -6.0F);
            GlStateManager.rotate((float) equip[0], 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, -3.0F, 6.0F);

            GlStateManager.translate(0.0F, 0.0F, (float) recoil[2]);

            GlStateManager.shadeModel(GL11.GL_SMOOTH);

            ResourceManager.minigun.renderPart(index == 0 ? "GunDual" : "Gun");

            GlStateManager.pushMatrix();
            GlStateManager.rotate((float) (rotate[2] * i), 0.0F, 0.0F, 1.0F);
            ResourceManager.minigun.renderPart("Barrels");
            GlStateManager.popMatrix();

            GlStateManager.shadeModel(GL11.GL_FLAT);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 12.0F);
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);

            GlStateManager.rotate(gun.shotRand * 90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(1.5F, 1.5F, 1.5F);
            this.renderMuzzleFlash(gun.lastShot[index], 75, 5);
            GlStateManager.popMatrix();

            GlStateManager.popMatrix();
        }
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        double scale = 1.75D;
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
        GlStateManager.translate(-1.0F, -3.5F, 8.0F);
    }

    @Override
    public void setupThirdPersonAkimbo(ItemStack stack) {
        super.setupThirdPerson(stack);
        double scale = 1.75D;
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
        GlStateManager.translate(2.0F, -3.5F, 8.0F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        GlStateManager.scale(1.0F, 1.0F, -1.0F);
        GlStateManager.translate(8.0F, 8.0F, 0.0F);
        double scale = 0.875D;
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -6.25D;
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public void renderEquipped(ItemStack stack) {

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.minigun_dual_tex);
        ResourceManager.minigun.renderPart("Gun");
        ResourceManager.minigun.renderPart("Barrels");
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public void renderEquippedAkimbo(ItemStack stack) {

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.minigun_dual_tex);
        ResourceManager.minigun.renderPart("GunDual");
        ResourceManager.minigun.renderPart("Barrels");
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public void renderModTable(ItemStack stack, int index) {
        GlStateManager.enableLighting();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.minigun_dual_tex);
        ResourceManager.minigun.renderPart(index == 0 ? "GunDual" : "Gun");
        ResourceManager.minigun.renderPart("Barrels");
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public void renderInv(ItemStack stack) {

        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.minigun_dual_tex);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(225.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        ResourceManager.minigun.renderPart("GunDual");
        ResourceManager.minigun.renderPart("Barrels");
        GlStateManager.popMatrix();

        GlStateManager.translate(0.0F, 0.0F, 8.0F);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(225.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-45.0F, 0.0F, 1.0F, 0.0F);
        ResourceManager.minigun.renderPart("Gun");
        ResourceManager.minigun.renderPart("Barrels");
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.minigun_dual_tex);
        ResourceManager.minigun.renderPart("Gun");
        ResourceManager.minigun.renderPart("Barrels");
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    public boolean hasSilencer(ItemStack stack, int cfg) {
        return XWeaponModManager.hasUpgrade(stack, cfg, XWeaponModManager.ID_SILENCER);
    }

    public boolean isSaturnite(ItemStack stack, int cfg) {
        return XWeaponModManager.hasUpgrade(stack, cfg, XWeaponModManager.ID_UZI_SATURN);
    }
}
