package com.hbm.render.item.weapon.sedna;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ItemRenderShredder extends ItemRenderWeaponBase {

    protected static String label = "[> <]";
    protected ResourceLocation texture;

    public ItemRenderShredder(ResourceLocation texture) {
        this.texture = texture;
        offsets = offsets.get(ItemCameraTransforms.TransformType.GUI).setScale(0.055).setPosition(-6.55, 16.5, -16).setRotation(6, 98, 100).getHelper().get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND).setScale(0.8).setPosition(-0.95, -0.05, -1.2).getHelper();
    }

    @Override
    protected float getTurnMagnitude(ItemStack stack) {
        return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.5F;
    }

    @Override
    public float getViewFOV(ItemStack stack, float fov) {
        float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
        return fov * (1 - aimingProgress * 0.33F);
    }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0, 0, 0.875);

        float offset = 0.8F;
        standardAimingTransform(stack, -1.5F * offset, -1.25F * offset, 1.5F * offset, 0, -6.25 / 8D, 0.5);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        EntityPlayer player = Minecraft.getMinecraft().player;
        double scale = 0.25D;
        GlStateManager.scale(scale, scale, scale);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double[] lift = HbmAnimationsSedna.getRelevantTransformation("LIFT");
        double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
        double[] mag = HbmAnimationsSedna.getRelevantTransformation("MAG");
        double[] speen = HbmAnimationsSedna.getRelevantTransformation("SPEEN");
        double[] cycle = HbmAnimationsSedna.getRelevantTransformation("CYCLE");

        GlStateManager.translate(0, -2, -6);
        GlStateManager.rotate((float) equip[0], 1, 0, 0);
        GlStateManager.translate(0, 2, 6);

        GlStateManager.translate(0, 0, -4);
        GlStateManager.rotate((float) lift[0], 1, 0, 0);
        GlStateManager.translate(0, 0, 4);

        GlStateManager.translate(0, 0, recoil[2]);
        final int prevShade = RenderUtil.getShadeModel();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(GL11.GL_SMOOTH);

        boolean sexy = stack.getItem() == ModItems.gun_autoshotgun_sexy;

        if (sexy || (ItemGunBaseNT.prevAimingProgress >= 1F && ItemGunBaseNT.aimingProgress >= 1F)) {
            GlStateManager.pushMatrix();

            final boolean prevLighting = RenderUtil.isLightingEnabled();
            final boolean prevCull = RenderUtil.isCullEnabled();
            final boolean prevBlend = RenderUtil.isBlendEnabled();
            final int prevSrc = RenderUtil.getBlendSrcFactor();
            final int prevDst = RenderUtil.getBlendDstFactor();
            final int prevSrcA = RenderUtil.getBlendSrcAlphaFactor();
            final int prevDstA = RenderUtil.getBlendDstAlphaFactor();

            if (prevLighting) GlStateManager.disableLighting();
            if (prevCull) GlStateManager.disableCull();
            if (!prevBlend) GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0); // SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            float f3 = 0.04F;
            GlStateManager.translate((font.getStringWidth(label) / 2F) * f3, 3.25F, -1.75F);
            GlStateManager.scale(f3, -f3, f3);
            GlStateManager.rotate(180F, 0, 1, 0);
            GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F * f3);
            float variance = 0.9F + player.getRNG().nextFloat() * 0.1F;
            font.drawString(label, 0, 0, new Color(sexy ? variance : 0F, sexy ? 0F : variance, 0F).getRGB());
            GlStateManager.color(1F, 1F, 1F);

            GlStateManager.tryBlendFuncSeparate(prevSrc, prevDst, prevSrcA, prevDstA);
            if (!prevBlend) GlStateManager.disableBlend();
            if (prevCull) GlStateManager.enableCull();
            if (prevLighting) GlStateManager.enableLighting();

            GlStateManager.popMatrix();

            int brightness = player.world.getCombinedLight(new BlockPos(player.posX, player.posY, player.posZ), 0);
            int j = brightness % 65536;
            int k = brightness / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        ResourceManager.shredder.renderPart("Gun");

        GlStateManager.pushMatrix();
        GlStateManager.translate(mag[0], mag[1], mag[2]);
        GlStateManager.translate(0, -1, -0.5);
        GlStateManager.rotate((float) speen[0], 1, 0, 0);
        GlStateManager.translate(0, 1, 0.5);
        ResourceManager.shredder.renderPart("Magazine");
        GlStateManager.translate(0, -1, -0.5);
        GlStateManager.rotate((float) cycle[2], 0, 0, 1);
        GlStateManager.translate(0, 1, 0.5);
        ResourceManager.shredder.renderPart("Shells");
        GlStateManager.popMatrix();

        double smokeScale = 0.75;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1, 7.5);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.scale(smokeScale, smokeScale, smokeScale);
        renderSmokeNodes(gun.getConfig(stack, 0).smokeNodes, 0.5D);
        GlStateManager.popMatrix();

        // Temporarily switch to flat for muzzle flash, then restore to previous shade
        final boolean needFlat = (prevShade != GL11.GL_FLAT);
        if (needFlat) GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1, 7.5);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.rotate(gun.shotRand * 90, 1, 0, 0);
        GlStateManager.scale(0.75, 0.75, 0.75);
        renderMuzzleFlash(gun.lastShot[0], 75, 7.5);
        GlStateManager.popMatrix();

        // Restore original shade model
        if (prevShade != RenderUtil.getShadeModel()) GlStateManager.shadeModel(prevShade);
    }

    @Override
    public void setupThirdPerson(ItemStack stack) {
        super.setupThirdPerson(stack);
        double scale = 1.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0, 0.5, 4);
    }

    @Override
    public void setupInv(ItemStack stack) {
        super.setupInv(stack);
        double scale = 1.25D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.translate(-1.5, 0, 0);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -7.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.translate(0, 0, 1.5);
    }

    @Override
    public void renderOther(ItemStack stack, Object type) {
        final int prevShade = RenderUtil.getShadeModel();

        GlStateManager.enableLighting();
        if (prevShade != GL11.GL_SMOOTH) GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        ResourceManager.shredder.renderAll();
        if (RenderUtil.getShadeModel() != prevShade) GlStateManager.shadeModel(prevShade);
    }
}
