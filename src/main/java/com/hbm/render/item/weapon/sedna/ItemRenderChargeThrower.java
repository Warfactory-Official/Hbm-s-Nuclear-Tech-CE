package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.factory.XFactoryTool;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.items.weapon.sedna.mods.XWeaponModManager;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_charge_thrower")
public class ItemRenderChargeThrower extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 0F : -0.5F; }

    @Override
    public float getViewFOV(ItemStack stack, float fov) {
        float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
        return  fov * (1 - aimingProgress * (isScoped(stack) ? 0.66F : 0.33F));
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0F, 0F, 0.875F);

        float offset = 0.8F;
        float zoom = 0.5F;

        if(isScoped(stack)) standardAimingTransform(stack,
                -1.5F * offset, -1.25F * offset, 3.5F * offset,
                -0.15625, -6.5 / 8D, 1.6875);
        else standardAimingTransform(stack,
                -1.5F * offset, -1.25F * offset, 3.5F * offset,
                -1.5F * zoom, -1.25F * zoom, 3.5F * zoom);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {
        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        boolean usingScope = this.isScoped(stack) && gun.aimingProgress == 1 && gun.prevAimingProgress == 1;
        MagazineFullReload mag = (MagazineFullReload) gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);

        if(usingScope) {
            double scale = 3.5D;
            GlStateManager.scale((float) scale, (float) scale, (float) scale);
            GlStateManager.translate(-0.5F, -1.5F, -4F);
        } else {
            double scale = 0.5D;
            GlStateManager.scale((float) scale, (float) scale, (float) scale);
        }

        boolean reloading = HbmAnimationsSedna.getRelevantAnim(0) != null && HbmAnimationsSedna.getRelevantAnim(0).animation.getBus("AMMO") != null;
        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
        double[] raise = HbmAnimationsSedna.getRelevantTransformation("RAISE");
        double[] ammo = HbmAnimationsSedna.getRelevantTransformation("AMMO");
        double[] twist = HbmAnimationsSedna.getRelevantTransformation("TWIST");
        double[] turn = HbmAnimationsSedna.getRelevantTransformation("TURN");
        double[] roll = HbmAnimationsSedna.getRelevantTransformation("ROLL");

        GlStateManager.translate(0F, 0F, -7F);
        GlStateManager.rotate((float) equip[0], -1F, 0F, 0F);
        GlStateManager.translate(0F, 0F, 7F);

        GlStateManager.translate(0F, -7F, 4F);
        GlStateManager.rotate((float) raise[0], 1F, 0F, 0F);
        GlStateManager.translate(0F, 7F, -4F);

        GlStateManager.translate((float) recoil[0], (float) recoil[1], (float) recoil[2]);

        GlStateManager.translate(0F, 0F, -2F);
        GlStateManager.rotate((float) turn[1], 0F, 1F, 0F);
        GlStateManager.translate(0F, 0F, 2F);
        GlStateManager.translate(0F, -1F, 0F);
        GlStateManager.rotate((float) roll[2], 0F, 0F, 1F);
        GlStateManager.translate(0F, 1F, 0F);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.charge_thrower_tex);
        ResourceManager.charge_thrower.renderPart("Gun");
        if(isScoped(stack) && !usingScope) ResourceManager.charge_thrower.renderPart("Scope");

        if(mag.getAmount(stack, null) > 0 || reloading) {

            GlStateManager.translate((float) ammo[0], (float) ammo[1], (float) ammo[2]);
            GlStateManager.rotate((float) twist[2], 0F, 0F, 1F);

            if(mag.getType(stack, null) == XFactoryTool.ct_hook) {
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.charge_thrower_hook_tex);
                ResourceManager.charge_thrower.renderPart("Hook");
            }
            if(mag.getType(stack, null) == XFactoryTool.ct_mortar) {
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.charge_thrower_mortar_tex);
                ResourceManager.charge_thrower.renderPart("Mortar");
            }
            if(mag.getType(stack, null) == XFactoryTool.ct_mortar_charge) {
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.charge_thrower_mortar_tex);
                ResourceManager.charge_thrower.renderPart("Mortar");
                ResourceManager.charge_thrower.renderPart("Oomph");
            }
            //Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.charge_thrower_rocket_tex);
            //ResourceManager.charge_thrower.renderPart("Rocket");
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        double scale = 1.5D;
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
        GlStateManager.translate(0.75F, 1F, 4F);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 1.25D;
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
        GlStateManager.rotate(25F, 1F, 0F, 0F);
        GlStateManager.rotate(45F, 0F, 1F, 0F);
        GlStateManager.translate(0F, 0F, -0.625F);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -8.5D;
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
        GlStateManager.rotate(90F, 0F, 1F, 0F);
        GlStateManager.translate(0F, 0F, -1F);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        GlStateManager.enableLighting();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.charge_thrower_tex);
        ResourceManager.charge_thrower.renderPart("Gun");
        if(isScoped(stack)) ResourceManager.charge_thrower.renderPart("Scope");

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        MagazineFullReload mag = (MagazineFullReload) gun.getConfig(stack, 0).getReceivers(stack)[0].getMagazine(stack);

        if(mag.getAmount(stack, null) > 0) {

            if(mag.getType(stack, null) == XFactoryTool.ct_hook) {
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.charge_thrower_hook_tex);
                ResourceManager.charge_thrower.renderPart("Hook");
            }
            if(mag.getType(stack, null) == XFactoryTool.ct_mortar) {
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.charge_thrower_mortar_tex);
                ResourceManager.charge_thrower.renderPart("Mortar");
            }
            if(mag.getType(stack, null) == XFactoryTool.ct_mortar_charge) {
                Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.charge_thrower_mortar_tex);
                ResourceManager.charge_thrower.renderPart("Mortar");
                ResourceManager.charge_thrower.renderPart("Oomph");
            }
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    public boolean isScoped(ItemStack stack) {
        return XWeaponModManager.hasUpgrade(stack, 0, XWeaponModManager.ID_SCOPE);
    }
}
