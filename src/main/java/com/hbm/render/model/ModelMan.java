package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.entity.Entity;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelMan extends ModelArmorBase {

    public ModelMan() {
        super(0);

        head = new ModelRendererObj(ResourceManager.player_manly_af, "Head");
        body = new ModelRendererObj(ResourceManager.player_manly_af, "Body");
        leftArm = new ModelRendererObj(ResourceManager.player_manly_af, "LeftArm").setRotationPoint(5.0F, 2.0F, 0.0F);
        rightArm = new ModelRendererObj(ResourceManager.player_manly_af, "RightArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
        leftLeg = new ModelRendererObj(ResourceManager.player_manly_af, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
        rightLeg = new ModelRendererObj(ResourceManager.player_manly_af, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
    }

    @Override
    protected void renderArmor(Entity entity, float scale) {
        bindTexture(ResourceManager.player_manly_tex);
        head.render(scale);
        body.render(scale);
        leftArm.render(scale);
        rightArm.render(scale);
        leftLeg.render(scale);
        rightLeg.render(scale);
    }

    public void renderRightArm(Entity entity, float scale) {
        this.swingProgress = 0.0F;
        this.isSneak = false;
        this.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, scale, entity);
        this.rightArm.rotateAngleX = 0.0F;
        bindTexture(ResourceManager.player_manly_tex);
        this.rightArm.render(scale);
    }

    public void renderLeftArm(Entity entity, float scale) {
        this.swingProgress = 0.0F;
        this.isSneak = false;
        this.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, scale, entity);
        this.leftArm.rotateAngleX = 0.0F;
        bindTexture(ResourceManager.player_manly_tex);
        this.leftArm.render(scale);
    }
}
