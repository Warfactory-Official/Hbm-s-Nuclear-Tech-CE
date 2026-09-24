package com.hbm.render.item;

import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.hbm.render.model.BakedModelMatrixUtil.*;

public class ItemRenderBase extends TEISRBase {

	// 1.7-exact base frames: each matrix reproduces, on the 1.12.2 engine (RenderItem context chain +
	// its translate(-0.5,-0.5,-0.5)), exactly what 1.7 applied before the verbatim-ported render bodies.
	// Left hands mirror the right chains: X translations and Y/Z rotations are negated, with the
	// leading +1 X shift needed for the corner asymmetry.
	private static final FloatBuffer FIRST_PERSON_RIGHT_HAND_MATRIX = glMatrix(
			// renderItemInFirstPerson rest, ForgeHooksClient else-pose, EQUIPPED_FIRST_PERSON preamble
			translate(0.5, 0.5, 0.5),
			rotateY(45),
			scale(0.4),
			translate(0, -0.3, 0),
			scale(1.5),
			rotateY(50),
			rotateZ(335),
			translate(-0.9375, -0.0625, 0),
			translate(0.5, 0.25, 0),
			scale(0.25),
			rotateY(90)
	);
	private static final FloatBuffer FIRST_PERSON_LEFT_HAND_MATRIX = glMatrix(
			translate(0.5, 0.5, 0.5),
			rotateY(-45),
			scale(0.4),
			translate(0, -0.3, 0),
			scale(1.5),
			rotateY(-50),
			rotateZ(-335),
			translate(0.9375, -0.0625, 0),
			translate(-0.5, 0.25, 0),
			scale(0.25),
			rotateY(-90)
	);
	private static final FloatBuffer THIRD_PERSON_RIGHT_HAND_MATRIX = glMatrix(
			// Undo LayerHeldItem, then apply the 1.7 RenderBiped flat-item arm chain and EQUIPPED preamble.
			translate(0.4375, 0.375, 1.125),
			rotateY(180),
			rotateX(90),
			translate(0.1875, 0.625, -0.125),
			scale(0.375),
			rotateZ(60),
			rotateX(-90),
			rotateZ(20),
			translate(0, -0.3, 0),
			scale(1.5),
			rotateY(50),
			rotateZ(335),
			translate(-0.9375, -0.0625, 0),
			translate(0.5, 0.25, 0),
			scale(0.25)
	);
	private static final FloatBuffer THIRD_PERSON_LEFT_HAND_MATRIX = glMatrix(
			translate(0.5625, 0.375, 1.125),
			rotateY(180),
			rotateX(90),
			translate(-0.1875, 0.625, -0.125),
			scale(0.375),
			rotateZ(-60),
			rotateX(-90),
			rotateZ(-20),
			translate(0, -0.3, 0),
			scale(1.5),
			rotateY(-50),
			rotateZ(-335),
			translate(0.9375, -0.0625, 0),
			translate(-0.5, 0.25, 0),
			scale(0.25)
	);
	private static final FloatBuffer HEAD_MATRIX = glMatrix(
			// Undo LayerCustomHead, then apply the 1.7 non-3D ItemBlock helmet path.
			translate(0.5, 0.5, 0.5),
			scale(1.6, -1.6, -1.6),
			rotateY(180),
			translate(0, 0.25, 0),
			translate(0, -0.3, 0),
			scale(1.5),
			rotateY(50),
			rotateZ(335),
			translate(-0.9375, -0.0625, 0),
			translate(0.5, 0.25, 0),
			scale(0.25)
	);
	private static final FloatBuffer GROUND_MATRIX = glMatrix(
			// 1.7 custom-flat entity scale and ENTITY preamble; Y offsets the 1.12.2 display-scale lift.
			translate(0.5, 0.25, 0.5),
			scale(0.1875),
			rotateY(90)
	);
	private static final FloatBuffer FIXED_MATRIX = glMatrix(
			// 1.7 item-frame depth and Y offset, re-expressed under the 1.12.2 facing convention.
			translate(0.5, 0.34, 0.53125),
			rotateY(-90),
			scale(0.375)
	);
	private static final FloatBuffer GUI_MATRIX = glMatrix(
			// Raw-pixel Y-down slot frame with the 1.7 translate(8,10,0) folded into 1.12.2 units.
			translate(0.5, 0.375, 0),
			rotateX(30),
			rotateY(225),
			scale(0.0625)
	);

    protected ItemCameraTransforms getBindingTransforms(Item item) {
        return ItemCameraTransforms.DEFAULT;
    }

	protected FloatBuffer getTransformMatrix(TransformType type) {
		return switch (type) {
			case FIRST_PERSON_RIGHT_HAND -> FIRST_PERSON_RIGHT_HAND_MATRIX;
			case FIRST_PERSON_LEFT_HAND -> FIRST_PERSON_LEFT_HAND_MATRIX;
			case THIRD_PERSON_RIGHT_HAND -> THIRD_PERSON_RIGHT_HAND_MATRIX;
			case THIRD_PERSON_LEFT_HAND -> THIRD_PERSON_LEFT_HAND_MATRIX;
			case HEAD -> HEAD_MATRIX;
			case GROUND -> GROUND_MATRIX;
			case FIXED -> FIXED_MATRIX;
			case GUI -> GUI_MATRIX;
			case NONE -> throw new IllegalArgumentException("NONE has no transform matrix");
		};
	}

    @Override
    public ModelBinding createModelBinding(Item item) {
        if (item instanceof ItemArmor) {
            return ModelBinding.of(new ModelResourceLocation(item.getRegistryName(), "inventory"), ItemCameraTransforms.DEFAULT, false);
        }
        return ModelBinding.inventory(item, getBindingTransforms(item));
    }

	@Override
	//Norwood: nowhere in the source of MC in context of rendering, itemstack is considered non-null
	public void renderByItem(@Nullable ItemStack itemStackIn) {
		if (this.type == null || itemStackIn == null) {
			this.type = TransformType.NONE;
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableCull();

		switch (type) {
			case FIRST_PERSON_RIGHT_HAND -> {
				GlStateManager.multMatrix(getTransformMatrix(type));
				renderNonInv(itemStackIn);
                renderFirstPersonRightHand();
			}
			case FIRST_PERSON_LEFT_HAND -> {
				GlStateManager.multMatrix(getTransformMatrix(type));
				renderNonInv(itemStackIn);
			}
			case THIRD_PERSON_RIGHT_HAND -> {
				GlStateManager.multMatrix(getTransformMatrix(type));
				renderNonInv(itemStackIn);
			}
			case THIRD_PERSON_LEFT_HAND -> {
				GlStateManager.multMatrix(getTransformMatrix(type));
				renderNonInv(itemStackIn);
			}
			case HEAD -> {
				GlStateManager.multMatrix(getTransformMatrix(type));
				renderNonInv(itemStackIn);
			}
			case GROUND -> {
				GlStateManager.multMatrix(getTransformMatrix(type));
				renderGround();
			}
			case FIXED -> {
				GlStateManager.multMatrix(getTransformMatrix(type));
				renderNonInv(itemStackIn);
			}
			case GUI -> {
				GlStateManager.enableLighting();
				GlStateManager.multMatrix(getTransformMatrix(type));
				renderInventory(itemStackIn);
			}
			case NONE -> {}
		}

		renderCommon(itemStackIn);
		GlStateManager.popMatrix();
		this.type = null;
	}

	public void renderNonInv(ItemStack stack) { renderNonInv(); }
	public void renderInventory(ItemStack stack) { renderInventory(); }
	public void renderCommon(ItemStack stack) { renderCommon(); }
	public void renderNonInv() { }
	public void renderInventory() { }
	public void renderCommon() { }
    // GROUND runs the 1.7 ENTITY body (== the non-inventory body) by default; a subclass needing a
    // distinct dropped-item pose (e.g. RenderExplosiveCharge) overrides this.
    public void renderGround() { renderNonInv(); }
    public void renderFirstPersonRightHand() { }
}
