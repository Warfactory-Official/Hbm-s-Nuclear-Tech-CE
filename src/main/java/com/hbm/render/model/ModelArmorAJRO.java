package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorAJRO extends ModelArmorBase {

    public ModelArmorAJRO(int type) {
        super(type);

        head = new ModelRendererObj(ResourceManager.armor_ajr, "Head");
        body = new ModelRendererObj(ResourceManager.armor_ajr, "Body");
        leftArm = new ModelRendererObj(ResourceManager.armor_ajr, "LeftArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
        rightArm = new ModelRendererObj(ResourceManager.armor_ajr, "RightArm").setRotationPoint(5.0F, 2.0F, 0.0F);
        leftLeg = new ModelRendererObj(ResourceManager.armor_ajr, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
        rightLeg = new ModelRendererObj(ResourceManager.armor_ajr, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
        leftFoot = new ModelRendererObj(ResourceManager.armor_ajr, "LeftBoot").setRotationPoint(1.9F, 12.0F, 0.0F);
        rightFoot = new ModelRendererObj(ResourceManager.armor_ajr, "RightBoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    @Override
    public void renderArmor(Entity par1Entity, float par7) {
        switch (type) {
            case 3 -> {
                bindTexture(ResourceManager.ajro_helmet);
                head.render(par7 * 1.001F);
            }
            case 2 -> {
                bindTexture(ResourceManager.ajro_chest);
                body.render(par7);
                bindTexture(ResourceManager.ajro_arm);
                leftArm.render(par7);
                rightArm.render(par7);
            }
            case 1 -> {
                bindTexture(ResourceManager.ajro_leg);
                leftLeg.render(par7);
                rightLeg.render(par7);
            }
            case 0 -> {
                bindTexture(ResourceManager.ajro_leg);
                leftFoot.render(par7);
                rightFoot.render(par7);
            }
        }
    }
}
