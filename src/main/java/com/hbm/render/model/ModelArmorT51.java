package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorT51 extends ModelArmorBase {

    public ModelArmorT51(int type) {
        super(type);

        this.head = new ModelRendererObj(ResourceManager.armor_t51, "Helmet");
        this.body = new ModelRendererObj(ResourceManager.armor_t51, "Chest");
        this.leftArm = new ModelRendererObj(ResourceManager.armor_t51, "LeftArm").setRotationPoint(5.0F, 2.0F, 0.0F);
        this.rightArm = new ModelRendererObj(ResourceManager.armor_t51, "RightArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.leftLeg = new ModelRendererObj(ResourceManager.armor_t51, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
        this.rightLeg = new ModelRendererObj(ResourceManager.armor_t51, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.leftFoot = new ModelRendererObj(ResourceManager.armor_t51, "LeftBoot").setRotationPoint(1.9F, 12.0F, 0.0F);
        this.rightFoot = new ModelRendererObj(ResourceManager.armor_t51, "RightBoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);

        GL11.glPushMatrix();
        GL11.glShadeModel(GL11.GL_SMOOTH);

        if(this.type == 0) {
            bindTexture(ResourceManager.t51_helmet);
            this.head.render(scaleFactor);
        }
        if(this.type == 1) {
            bindTexture(ResourceManager.t51_chest);
            this.body.render(scaleFactor);

            bindTexture(ResourceManager.t51_arm);
            this.leftArm.render(scaleFactor);
            this.rightArm.render(scaleFactor);
        }
        if(this.type == 2) {
            bindTexture(ResourceManager.t51_leg);
            this.leftLeg.render(scaleFactor);
            this.rightLeg.render(scaleFactor);
        }
        if(this.type == 3) {
            bindTexture(ResourceManager.t51_leg);
            this.leftFoot.render(scaleFactor);
            this.rightFoot.render(scaleFactor);
        }

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glPopMatrix();
    }

    @Override
    public void renderArmor(Entity par1Entity, float scale) {
        switch (type) {
            case 3 -> {
                bindTexture(ResourceManager.t51_helmet);
                head.render(scale * 1.001F);
            }
            case 2 -> {
                bindTexture(ResourceManager.t51_chest);
                body.render(scale);
                bindTexture(ResourceManager.t51_arm);
                leftArm.render(scale);
                rightArm.render(scale);
            }
            case 1 -> {
                bindTexture(ResourceManager.t51_leg);
                leftLeg.render(scale);
                rightLeg.render(scale);
            }
            case 0 -> {
                bindTexture(ResourceManager.t51_leg);
                leftFoot.render(scale);
                rightFoot.render(scale);
            }
        }
    }
}


