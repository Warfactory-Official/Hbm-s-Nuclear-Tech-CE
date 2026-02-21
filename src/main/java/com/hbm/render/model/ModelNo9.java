package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class ModelNo9 extends ModelArmorBase {

    public ModelRendererObj lamp;
    public ModelRendererObj insig;

    public ModelNo9(int type) {
        super(type);

        this.head = new ModelRendererObj(ResourceManager.armor_no9, "Helmet");
        this.insig = new ModelRendererObj(ResourceManager.armor_no9, "Insignia");
        this.lamp = new ModelRendererObj(ResourceManager.armor_no9, "Flame");
        this.body = new ModelRendererObj(null);
        this.leftArm = new ModelRendererObj(null).setRotationPoint(5.0F, 2.0F, 0.0F);
        this.rightArm = new ModelRendererObj(null).setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.leftLeg = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
        this.rightLeg = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.leftFoot = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
        this.rightFoot = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    @Override
    public void renderArmor(Entity entity, float scaleFactor) {
        this.head.copyTo(this.insig);
        this.head.copyTo(this.lamp);

        GlStateManager.pushMatrix();

        if(this.type == 0) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.no9);
            this.head.render(scaleFactor);
            Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.no9_insignia);
            this.insig.render(scaleFactor);

            if(entity instanceof EntityPlayer player) {
                ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

                if(!helmet.isEmpty() && helmet.hasTagCompound() && helmet.getTagCompound().getBoolean("isOn")) {
                    GlStateManager.color(1F, 1F, 0.8F);
                    GlStateManager.disableTexture2D();
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
                    boolean prevLighting = RenderUtil.isLightingEnabled();
                    GlStateManager.disableLighting();
                    this.lamp.render(scaleFactor);
                    if (prevLighting) GlStateManager.enableLighting();
                    GlStateManager.enableTexture2D();
                    GlStateManager.color(1F, 1F, 1F);
                    GlStateManager.shadeModel(GL11.GL_FLAT);
                }
            }
        }

        GlStateManager.popMatrix();
    }
}
