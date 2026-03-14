package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import org.lwjgl.opengl.GL11;

import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.impl.ItemGunNI4NI;
import com.hbm.main.ResourceManager;
import com.hbm.render.tileentity.RenderArcFurnace;
import com.hbm.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
@AutoRegister(item = "gun_n_i_4_n_i")
public class ItemRenderNI4NI extends ItemRenderWeaponBase {

    @Override
    protected float getTurnMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F; }

    @Override
    public float getViewFOV(ItemStack stack, float fov) {
        float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
        return  fov * (1 - aimingProgress * 0.33F);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0.0D, 0.0D, 1.0D);

        float offset = 0.8F;
        standardAimingTransform(stack,
                -1.0F * offset, -1F * offset, offset,
                0, -5 / 8D, 0.125);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();

        int[] color = ItemGunNI4NI.getColors(stack);
        int dark = 0xffffff;
        int light = 0xffffff;
        int grip = 0xffffff;
        if(color != null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.n_i_4_n_i_greyscale_tex);
            dark = color[0];
            light = color[1];
            grip = color[2];
        } else {
            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.n_i_4_n_i_tex);
        }

        double scale = 0.3125D;
        GlStateManager.scale(scale, scale, scale);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
        double[] drum = HbmAnimationsSedna.getRelevantTransformation("DRUM");

        GlStateManager.translate(0.0D, 0.0D, -2.25D);
        GlStateManager.rotate((float) equip[0], 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0D, 0.0D, 2.25D);

        GlStateManager.translate(0.0D, -1.0D, -6.0D);
        GlStateManager.rotate((float) recoil[0], 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0D, 1.0D, 6.0D);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.pushMatrix();

        GlStateManager.color(ColorUtil.fr(dark), ColorUtil.fg(dark), ColorUtil.fb(dark));
        ResourceManager.n_i_4_n_i.renderPart("FrameDark");

        GlStateManager.color(ColorUtil.fr(grip), ColorUtil.fg(grip), ColorUtil.fb(grip));
        ResourceManager.n_i_4_n_i.renderPart("Grip");

        GlStateManager.color(ColorUtil.fr(light), ColorUtil.fg(light), ColorUtil.fb(light));
        ResourceManager.n_i_4_n_i.renderPart("FrameLight");

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 1.1875D, 0.0D);
        GlStateManager.rotate((float) drum[2], 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.0D, -1.1875D, 0.0D);
        ResourceManager.n_i_4_n_i.renderPart("Cylinder");
        RenderArcFurnace.fullbright(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        ResourceManager.n_i_4_n_i.renderPart("CylinderHighlights");
        RenderArcFurnace.fullbright(false);
        GlStateManager.popMatrix();

        RenderArcFurnace.fullbright(true);
        ResourceManager.n_i_4_n_i.renderPart("Barrel");

        GlStateManager.disableTexture2D();
        int coinCount = ItemGunNI4NI.getCoinCount(stack);
        if(coinCount > 3) { GlStateManager.color(coinCount > 7 ? 1.0F : 0.0F, 1.0F, 0.0F); ResourceManager.n_i_4_n_i.renderPart("Coin1"); }
        if(coinCount > 2) { GlStateManager.color(coinCount > 6 ? 1.0F : 0.0F, 1.0F, 0.0F); ResourceManager.n_i_4_n_i.renderPart("Coin2"); }
        if(coinCount > 1) { GlStateManager.color(coinCount > 5 ? 1.0F : 0.0F, 1.0F, 0.0F); ResourceManager.n_i_4_n_i.renderPart("Coin3"); }
        if(coinCount > 0) { GlStateManager.color(coinCount > 4 ? 1.0F : 0.0F, 1.0F, 0.0F); ResourceManager.n_i_4_n_i.renderPart("Coin4"); }

        GlStateManager.enableTexture2D();

        RenderArcFurnace.fullbright(false);

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.75D, 4.0D);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F * (float) gun.shotRand, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.125D, 0.125D, 0.125D);
        renderLaserFlash(gun.lastShot[0], 75, 7.5, 0xFFFFFF);
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        GlStateManager.translate(0.0D, 0.25D, 3.0D);
        double scale = 1.5D;
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 2.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(25.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0D, 0.0D, 0.0D);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -15.0D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0D, -0.5D, 0.0D);
    }

    @Override
    public void renderOther(ItemStack stack, Object data) {

        GlStateManager.enableLighting();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GlStateManager.enableAlpha();

        int[] color = ItemGunNI4NI.getColors(stack);
        int dark = 0xffffff;
        int light = 0xffffff;
        int grip = 0xffffff;
        if(color != null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.n_i_4_n_i_greyscale_tex);
            dark = color[0];
            light = color[1];
            grip = color[2];
        } else {
            Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.n_i_4_n_i_tex);
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(ColorUtil.fr(light), ColorUtil.fg(light), ColorUtil.fb(light));
        ResourceManager.n_i_4_n_i.renderPart("FrameLight");
        ResourceManager.n_i_4_n_i.renderPart("Cylinder");
        GlStateManager.color(ColorUtil.fr(grip), ColorUtil.fg(grip), ColorUtil.fb(grip));
        ResourceManager.n_i_4_n_i.renderPart("Grip");
        GlStateManager.color(ColorUtil.fr(dark), ColorUtil.fg(dark), ColorUtil.fb(dark));
        ResourceManager.n_i_4_n_i.renderPart("FrameDark");
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        RenderArcFurnace.fullbright(true);
        ResourceManager.n_i_4_n_i.renderPart("CylinderHighlights");
        ResourceManager.n_i_4_n_i.renderPart("Barrel");
        GlStateManager.disableTexture2D();
        GlStateManager.color(0.0F, 1.0F, 0.0F);
        ResourceManager.n_i_4_n_i.renderPart("Coin1");
        ResourceManager.n_i_4_n_i.renderPart("Coin2");
        ResourceManager.n_i_4_n_i.renderPart("Coin3");
        ResourceManager.n_i_4_n_i.renderPart("Coin4");
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        RenderArcFurnace.fullbright(false);
        GlStateManager.shadeModel(GL11.GL_FLAT);

        if(type == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
            if(!(data instanceof EntityLivingBase ent)) return;
            long shot;
            double shotRand = 0;
            if(ent == Minecraft.getMinecraft().player) {
                ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
                shot = gun.lastShot[0];
                shotRand = gun.shotRand;
            } else {
                shot = ItemRenderWeaponBase.flashMap.getOrDefault(ent, (long) -1);
                if(shot < 0) return;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0D, 0.75D, 4.0D);
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(90.0F * (float) shotRand, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(0.125D, 0.125D, 0.125D);
            renderLaserFlash(shot, 75, 7.5, 0xFFFFFF);
            GlStateManager.popMatrix();
        }
    }
}
