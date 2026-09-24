package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static com.hbm.render.NTMRenderHelper.bindTexture;

public class ModelArmorWings extends ModelArmorBase {

	ModelRendererObj wingLB;
	ModelRendererObj wingLT;
	ModelRendererObj wingRB;
	ModelRendererObj wingRT;
	
	public ModelArmorWings(int type) {
		super(type);

		wingLB = new ModelRendererObj(ResourceManager.armor_wings, "LeftBase");
		wingLT = new ModelRendererObj(ResourceManager.armor_wings, "LeftTip");
		wingRB = new ModelRendererObj(ResourceManager.armor_wings, "RightBase");
		wingRT = new ModelRendererObj(ResourceManager.armor_wings, "RightTip");

		//i should really stop doing that
		head = new ModelRendererObj(ResourceManager.anvil);
		body = new ModelRendererObj(ResourceManager.anvil);
		leftArm = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(-5.0F, 2.0F, 0.0F);
		rightArm = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(5.0F, 2.0F, 0.0F);
		leftLeg = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(1.9F, 12.0F, 0.0F);
		rightLeg = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(-1.9F, 12.0F, 0.0F);
		leftFoot = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(1.9F, 12.0F, 0.0F);
		rightFoot = new ModelRendererObj(ResourceManager.anvil).setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

	@Override
	protected void renderArmor(Entity entity, float scale) {

		GlStateManager.pushMatrix();

		bindTexture(this.getTexture());

		double px = 0.0625D;

		float rot = (float) (Math.sin((entity.ticksExisted) * 0.2D) * 20);
		float rot2 = (float) (Math.sin((entity.ticksExisted) * 0.2D - Math.PI * 0.5) * 50 + 30);

		int pivotSideOffset = 1;
		int pivotFrontOffset = 5;
		int pivotZOffset = 3;
		int tipSideOffset = 16;
		int tipZOffset = 2;
		float inwardAngle = 10F;

		GlStateManager.pushMatrix();

		body.applyTransform(scale);

		if(this.type != 1 && entity.onGround) {
			rot = 20;
			rot2 = 160;
		}

		if(this.type == 1) {

			if(entity.onGround) {
				rot = 30;
				rot2 = -30;
			} else if(entity.motionY < -0.1) {
				rot = 0;
				rot2 = 10;
			} else {
				rot = 30;
				rot2 = 20;
			}
		}

		GlStateManager.translate(0, -2 * px, 0);

		GlStateManager.enableCull();
		GlStateManager.pushMatrix();

		GlStateManager.rotate(-inwardAngle, 0, 1, 0);

		GlStateManager.translate(pivotSideOffset * px, pivotFrontOffset * px, pivotZOffset * px);
		GlStateManager.rotate(rot * 0.5F, 0, 1, 0);
		GlStateManager.rotate(rot + 5, 0, 0, 1);
		GlStateManager.rotate(45, 1, 0, 0);
		GlStateManager.translate(-pivotSideOffset * px, -pivotFrontOffset * px, -pivotZOffset * px);

		GlStateManager.translate(pivotSideOffset * px, pivotFrontOffset * px, pivotZOffset * px);
		GlStateManager.rotate(rot, 0, 0, 1);
		GlStateManager.translate(-pivotSideOffset * px, -pivotFrontOffset * px, -pivotZOffset * px);
		wingLB.render(scale);

		GlStateManager.translate(tipSideOffset * px, pivotFrontOffset * px, tipZOffset * px);
		GlStateManager.rotate(rot2, 0, 1, 0);
		if(doesRotateZ())
			GlStateManager.rotate(rot2 * 0.25F + 5, 0, 0, 1);
		GlStateManager.translate(-tipSideOffset * px, -pivotFrontOffset * px, -tipZOffset * px);
		wingLT.render(scale);

		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();

		GlStateManager.rotate(inwardAngle, 0, 1, 0);

		GlStateManager.translate(-pivotSideOffset * px, pivotFrontOffset * px, pivotZOffset * px);
		GlStateManager.rotate(-rot * 0.5F, 0, 1, 0);
		GlStateManager.rotate(-rot - 5, 0, 0, 1);
		GlStateManager.rotate(45, 1, 0, 0);
		GlStateManager.translate(pivotSideOffset * px, -pivotFrontOffset * px, -pivotZOffset * px);

		GlStateManager.translate(-pivotSideOffset * px, pivotFrontOffset * px, pivotZOffset * px);
		GlStateManager.rotate(-rot, 0, 0, 1);
		GlStateManager.translate(pivotSideOffset * px, -pivotFrontOffset * px, -pivotZOffset * px);
		wingRB.render(scale);

		GlStateManager.translate(-tipSideOffset * px, pivotFrontOffset * px, tipZOffset * px);
		GlStateManager.rotate(-rot2, 0, 1, 0);
		if(doesRotateZ())
			GlStateManager.rotate(-rot2 * 0.25F - 5, 0, 0, 1);
		GlStateManager.translate(tipSideOffset * px, -pivotFrontOffset * px, -tipZOffset * px);
		wingRT.render(scale);

		GlStateManager.popMatrix();
		GlStateManager.disableCull();

		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}

	protected boolean doesRotateZ() {
		return true;
	}
	
	protected ResourceLocation getTexture() {
		
		if(this.type == 2)
			return ResourceManager.wings_bob;
		
		return ResourceManager.wings_murk;
	}
}