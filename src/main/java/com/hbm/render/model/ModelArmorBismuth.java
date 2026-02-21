package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorBismuth extends ModelArmorBase {

	public ModelArmorBismuth(int type) {
		super(type);

		this.head = new ModelRendererObj(ResourceManager.armor_bismuth, "Head");
		this.body = new ModelRendererObj(ResourceManager.armor_bismuth, "Body");
		this.leftArm = new ModelRendererObj(ResourceManager.armor_bismuth, "LeftArm").setRotationPoint(5.0F, 2.0F, 0.0F);
		this.rightArm = new ModelRendererObj(ResourceManager.armor_bismuth, "RightArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
		this.leftLeg = new ModelRendererObj(ResourceManager.armor_bismuth, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightLeg = new ModelRendererObj(ResourceManager.armor_bismuth, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
		this.leftFoot = new ModelRendererObj(ResourceManager.armor_bismuth, "LeftFoot").setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightFoot = new ModelRendererObj(ResourceManager.armor_bismuth, "RightFoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);

        GL11.glPushMatrix();
        GL11.glShadeModel(GL11.GL_SMOOTH);

        bindTexture(ResourceManager.armor_bismuth_tex);

        switch(type) {
            case 0 -> this.head.render(scaleFactor);
            case 1 -> {
                this.body.render(scaleFactor);

                this.leftArm.render(scaleFactor);
                this.rightArm.render(scaleFactor);
            }
            case 2 -> {
                this.leftLeg.render(scaleFactor);
                this.rightLeg.render(scaleFactor);
            }
            case 3 -> {
                this.leftFoot.render(scaleFactor);
                this.rightFoot.render(scaleFactor);
            }
        }

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glPopMatrix();
    }

	@Override
	public void renderArmor(Entity par1Entity, float par7) {
        bindTexture(ResourceManager.armor_bismuth_tex);

		switch (type) {
			case 0 -> head.render(par7 * 1.001F);
			case 1 -> {
				body.render(par7);
				leftArm.render(par7);
				rightArm.render(par7);
			}
			case 2 -> {
				leftLeg.render(par7);
				rightLeg.render(par7);
			}
			case 3 -> {
				leftFoot.render(par7);
				rightFoot.render(par7);
			}
		}
	}
}
