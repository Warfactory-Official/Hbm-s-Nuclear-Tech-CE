package com.hbm.render.tileentity;

import com.hbm.blocks.generic.BlockLoot.TileEntityLoot;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.items.armor.ArmorNCRPA;
import com.hbm.items.armor.ArmorTrenchmaster;
import com.hbm.items.weapon.sedna.factory.GunFactory.EnumAmmo;
import com.hbm.main.ResourceManager;
import com.hbm.util.RenderUtil;
import com.hbm.util.Tuple.Quartet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@AutoRegister
public class RenderLoot extends TileEntitySpecialRenderer<TileEntityLoot> {

    @Override
    public void render(TileEntityLoot loot, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();

        for (Quartet<ItemStack, Double, Double, Double> item : loot.items) {
            ItemStack stack = item.getW();
            if (stack == null || stack.isEmpty()) continue;

            GlStateManager.pushMatrix();
            GlStateManager.translate(item.getX(), item.getY(), item.getZ());

            if (stack.getItem() == ModItems.ammo_standard && stack.getItemDamage() >= EnumAmmo.NUKE_STANDARD.ordinal() && stack.getItemDamage() <= EnumAmmo.NUKE_HIVE.ordinal()) {
                renderNuke();
            } else if (stack.getItem() == ModItems.gun_maresleg) {
                renderShotgun();
            } else if (stack.getItem() instanceof ArmorTrenchmaster) {
                renderTrenchmaster(stack);
            } else if(stack.getItem() instanceof ArmorNCRPA) {
                renderNCR(stack);
            } else {
                renderStandardItem(stack);
            }

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    private void renderTrenchmaster(ItemStack stack) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5, 1.5, 0.5);
        GlStateManager.scale(0.0625, 0.0625, 0.0625);
        GlStateManager.rotate(180, 1, 0, 0);
        GlStateManager.enableRescaleNormal();
        if (stack.getItem() == ModItems.trenchmaster_helmet) {
            bindTexture(ResourceManager.trenchmaster_helmet);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            ResourceManager.armor_trenchmaster.renderPart("Helmet");
            GlStateManager.disableBlend();
            float lastX = OpenGlHelper.lastBrightnessX;
            float lastY = OpenGlHelper.lastBrightnessY;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            GlStateManager.disableLighting();
            ResourceManager.armor_trenchmaster.renderPart("Light");
            GlStateManager.enableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
        }
        if (stack.getItem() == ModItems.trenchmaster_plate) {
            bindTexture(ResourceManager.trenchmaster_chest);
            ResourceManager.armor_trenchmaster.renderPart("Chest");
            bindTexture(ResourceManager.trenchmaster_arm);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-3, 1, 0, 0);
            ResourceManager.armor_trenchmaster.renderPart("LeftArm");
            ResourceManager.armor_trenchmaster.renderPart("RightArm");
            GlStateManager.popMatrix();
        }
        if (stack.getItem() == ModItems.trenchmaster_legs) {
            bindTexture(ResourceManager.trenchmaster_leg);
            ResourceManager.armor_trenchmaster.renderPart("LeftLeg");
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-0.1F, 1, 0, 0);
            ResourceManager.armor_trenchmaster.renderPart("RightLeg");
            GlStateManager.popMatrix();
        }
        if (stack.getItem() == ModItems.trenchmaster_boots) {
            bindTexture(ResourceManager.trenchmaster_leg);
            ResourceManager.armor_trenchmaster.renderPart("LeftBoot");
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-0.1F, 1, 0, 0);
            ResourceManager.armor_trenchmaster.renderPart("RightBoot");
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    private void renderNCR(ItemStack stack) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5, 1.5, 0.5);
        GlStateManager.scale(0.0625, 0.0625, 0.0625);
        GlStateManager.rotate(180, 1, 0, 0);
        GlStateManager.enableRescaleNormal();
        if(stack.getItem() == ModItems.ncrpa_helmet) {
            bindTexture(ResourceManager.ncrpa_helmet);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            ResourceManager.armor_ncr.renderPart("Helmet");
            GlStateManager.disableBlend();
            float lastX = OpenGlHelper.lastBrightnessX;
            float lastY = OpenGlHelper.lastBrightnessY;
            RenderUtil.pushAttrib(GL11.GL_LIGHTING_BIT);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            GlStateManager.disableLighting();
            ResourceManager.armor_ncr.renderPart("Eyes");
            GlStateManager.enableLighting();
            RenderUtil.popAttrib();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
        }
        if(stack.getItem() == ModItems.ncrpa_plate) {
            bindTexture(ResourceManager.ncrpa_chest);
            ResourceManager.armor_ncr.renderPart("Chest");
            bindTexture(ResourceManager.ncrpa_arm);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-3, 1, 0, 0);
            ResourceManager.armor_ncr.renderPart("LeftArm");
            ResourceManager.armor_ncr.renderPart("RightArm");
            GlStateManager.popMatrix();
        }
        if(stack.getItem() == ModItems.ncrpa_legs) {
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.ncrpa_leg);
            ResourceManager.armor_ncr.renderPart("LeftLeg");
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-0.1, 1, 0, 0);
            ResourceManager.armor_ncr.renderPart("RightLeg");
            GlStateManager.popMatrix();
        }
        if(stack.getItem() == ModItems.ncrpa_boots) {
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.ncrpa_leg);
            ResourceManager.armor_ncr.renderPart("LeftBoot");
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-0.1, 1, 0, 0);
            ResourceManager.armor_ncr.renderPart("RightBoot");
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    private void renderNuke() {
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.translate(1, 0.5, 1);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.mini_nuke_tex);
        ResourceManager.projectiles.renderPart("MiniNuke");
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    private void renderShotgun() {
        GlStateManager.scale(0.125, 0.125, 0.125);
        GlStateManager.translate(3, 0, 0);
        GlStateManager.rotate(25, 0, 1, 0);
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.rotate(90, 0, 1, 0);

        GlStateManager.enableRescaleNormal();
        bindTexture(ResourceManager.maresleg_tex);
        ResourceManager.maresleg.renderAll();
        GlStateManager.disableRescaleNormal();
    }

    private static void renderStandardItem(ItemStack stack) {
        GlStateManager.translate(0.25, 0.0, 0.25);
        GlStateManager.scale(1,1,1);
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1F, 1F, 1F, 1F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.disableRescaleNormal();
    }
}
