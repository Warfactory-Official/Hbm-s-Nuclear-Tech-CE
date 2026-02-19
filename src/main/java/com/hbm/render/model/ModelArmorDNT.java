package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorDNT extends ModelArmorBase {
	
	public ModelArmorDNT(int type) {
		super(type);

		head = new ModelRendererObj(ResourceManager.armor_dnt, "Head");
		body = new ModelRendererObj(ResourceManager.armor_dnt, "Body");
		leftArm = new ModelRendererObj(ResourceManager.armor_dnt, "LeftArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
		rightArm = new ModelRendererObj(ResourceManager.armor_dnt, "RightArm").setRotationPoint(5.0F, 2.0F, 0.0F);
		leftLeg = new ModelRendererObj(ResourceManager.armor_dnt, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
		rightLeg = new ModelRendererObj(ResourceManager.armor_dnt, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
		leftFoot = new ModelRendererObj(ResourceManager.armor_dnt, "LeftBoot").setRotationPoint(1.9F, 12.0F, 0.0F);
		rightFoot = new ModelRendererObj(ResourceManager.armor_dnt, "RightBoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);

        GlStateManager.pushMatrix();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        switch(type) {
            case 0 -> {
                bindTexture(ResourceManager.dnt_helmet);
                this.head.render(scaleFactor);
            }
            case 1 -> {
                bindTexture(ResourceManager.dnt_chest);
                this.body.render(scaleFactor);
                bindTexture(ResourceManager.dnt_arm);
                this.leftArm.render(scaleFactor);
                this.rightArm.render(scaleFactor);
            }
            case 2 -> {
                bindTexture(ResourceManager.dnt_leg);
                this.leftLeg.render(scaleFactor);
                this.rightLeg.render(scaleFactor);
            }
            case 3 -> {
                bindTexture(ResourceManager.dnt_leg);
                this.leftFoot.render(scaleFactor);
                this.rightFoot.render(scaleFactor);
            }
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

	@Override
    public void renderArmor(Entity par1Entity, float scale) {
        switch (type) {
            case 3 -> {
                bindTexture(ResourceManager.dnt_helmet);
                head.render(scale * 1.001F);
            }
            case 2 -> {
                bindTexture(ResourceManager.dnt_chest);
                body.render(scale);
                bindTexture(ResourceManager.dnt_arm);
                leftArm.render(scale);
                rightArm.render(scale);
            }
            case 1 -> {
                bindTexture(ResourceManager.dnt_leg);
                leftLeg.render(scale);
                rightLeg.render(scale);
            }
            case 0 -> {
                bindTexture(ResourceManager.dnt_leg);
                leftFoot.render(scale);
                rightFoot.render(scale);
            }
        }
	}
}