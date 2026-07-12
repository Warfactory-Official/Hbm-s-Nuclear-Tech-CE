package com.hbm.render.item;

import java.nio.FloatBuffer;

import com.hbm.render.model.BakedModelTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;

import static com.hbm.render.model.BakedModelMatrixUtil.*;

// mlbv: reserved for items that does not have a 1.7 counterpart to copy transform from
public class ItemRenderBaseLegacy extends ItemRenderBase {

	private static final FloatBuffer FIRST_PERSON_RIGHT_HAND_MATRIX = glMatrix(
			translate(0.5, 0, 0.5),
			translate(0, 0.3, 0),
			scale(0.2),
			rotateY(135)
	);
	private static final FloatBuffer FIRST_PERSON_LEFT_HAND_MATRIX = glMatrix(
			translate(0.5, 0, 0.5),
			translate(0, 0.3, 0),
			scale(0.2),
			rotateY(45)
	);
	private static final FloatBuffer THIRD_PERSON_RIGHT_HAND_MATRIX = glMatrix(
			translate(0.5, 0, 0.5),
			translate(0, 0.25, 0),
			scale(0.1875),
			rotateY(180)
	);
	private static final FloatBuffer THIRD_PERSON_LEFT_HAND_MATRIX = glMatrix(
			translate(0.5, 0, 0.5),
			translate(0, 0.25, 0),
			scale(0.1875)
	);
	private static final FloatBuffer GROUND_MATRIX = glMatrix(
			translate(0.5, 0, 0.5),
			translate(0, 0.3, 0),
			scale(0.125),
			rotateY(90)
	);
	private static final FloatBuffer FIXED_MATRIX = glMatrix(
			translate(0.5, 0, 0.5),
			translate(0, 0.3, 0),
			scale(0.25),
			rotateY(90)
	);
	private static final FloatBuffer GUI_MATRIX = glMatrix(
			rotateX(30),
			rotateY(225),
			scale(0.062),
			translate(0, 11.3, -11.3)
	);

	@Override
	protected ItemCameraTransforms getBindingTransforms(Item item) {
		if (!(item instanceof ItemBlock) && !(item instanceof ItemArmor)) {
			return BakedModelTransforms.defaultItemTransforms();
		}
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	protected FloatBuffer getTransformMatrix(TransformType type) {
		return switch (type) {
			case FIRST_PERSON_RIGHT_HAND -> FIRST_PERSON_RIGHT_HAND_MATRIX;
			case FIRST_PERSON_LEFT_HAND -> FIRST_PERSON_LEFT_HAND_MATRIX;
			case THIRD_PERSON_RIGHT_HAND, HEAD -> THIRD_PERSON_RIGHT_HAND_MATRIX;
			case THIRD_PERSON_LEFT_HAND -> THIRD_PERSON_LEFT_HAND_MATRIX;
			case GROUND -> GROUND_MATRIX;
			case FIXED -> FIXED_MATRIX;
			case GUI -> GUI_MATRIX;
			case NONE -> throw new IllegalArgumentException("NONE has no transform matrix");
		};
	}
}
