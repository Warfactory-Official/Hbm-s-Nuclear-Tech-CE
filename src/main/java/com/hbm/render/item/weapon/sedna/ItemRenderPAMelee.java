package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.armor.IPAMelee;
import com.hbm.items.armor.IPAWeaponsProvider;
import com.hbm.main.ResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister(item = "gun_pa_melee")
public class ItemRenderPAMelee extends ItemRenderWeaponBase {

    public ItemRenderPAMelee() { offsets = offsets.get(ItemCameraTransforms.TransformType.GUI).setPosition(0.0, 13.75, 1.0).setRotation(126, -152, 15).getHelper(); }
    @Override public boolean isAkimbo() { return true; }

    @Override protected float getSwayMagnitude(ItemStack stack) { return 2F; }
    @Override protected float getSwayPeriod(ItemStack stack) { return 0.5F; }

    @Override
    public void setupFirstPerson(ItemStack stack) {
        IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
        if(component != null) component.setupFirstPerson(stack);
    }

    @Override
    public void renderFirstPerson(ItemStack stack) {
        IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
        if(component != null) component.renderFirstPerson(stack);
    }

    @Override public void setupThirdPerson(ItemStack stack) { }
    @Override public void setupThirdPersonAkimbo(ItemStack stack) { }

    @Override
    public void setupInv(ItemStack stack) {
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
        GlStateManager.enableAlpha();
        GlStateManager.scale(1.0D, 1.0D, -1.0D);
        GlStateManager.translate(8.0D, 8.0D, 0.0D);
        double scale = 2.5D;
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    public void setupModTable(ItemStack stack) {
        double scale = -12.5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0D, -0.5D, 1.0D);
    }

    @Override
    public void renderInv(ItemStack stack) {

        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ncrpa_arm);

        GlStateManager.pushMatrix();
        double scale = 0.3125D;
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.rotate(135.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0D, -5.5D, 0.0D);
        GlStateManager.translate(-3.5D, 0.0D, 0.0D);
        ResourceManager.armor_ncr.renderPart("Leftarm");
        GlStateManager.translate(7.0D, 1.0D, -1.0D);
        ResourceManager.armor_ncr.renderPart("RightArm");
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    public void renderOther(ItemStack stack, Object... data) {
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ncrpa_arm);

        GlStateManager.pushMatrix();
        double scale = 0.3125D;
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0D, -5.5D, 0.0D);
        GlStateManager.translate(-2.0D, 0.0D, 0.0D);
        ResourceManager.armor_ncr.renderPart("Leftarm");
        GlStateManager.translate(4.0D, 0.0D, 0.0D);
        ResourceManager.armor_ncr.renderPart("RightArm");
        GlStateManager.popMatrix();

        GlStateManager.shadeModel(GL11.GL_FLAT);
    }
}
