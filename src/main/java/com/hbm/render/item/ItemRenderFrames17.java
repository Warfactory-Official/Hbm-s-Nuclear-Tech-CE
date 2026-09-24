package com.hbm.render.item;

import java.nio.FloatBuffer;

import static com.hbm.render.model.BakedModelMatrixUtil.*;

/**
 * 1.7-exact base frames for raw TEISR item renderers
 */
public final class ItemRenderFrames17 {

	private ItemRenderFrames17() {}

	public static final FloatBuffer GUI = glMatrix(
			// machine GUI frame · inv(M17 gui T(8,10,0)·Rx(-30)·Ry(45)·S(-1))
			translate(0.5, 0.375, 0), rotateX(30), rotateY(225), scale(1.0 / 16),
			scale(-1, -1, -1), rotateY(-45), rotateX(30), translate(-8, -10, 0));

	public static final FloatBuffer THIRD_PERSON = glMatrix(
			// machine TP frame, FORGE_ELSE kept, machine M17 T(.5,.25,0)·S(.25) dropped
			translate(0.4375, 0.375, 1.125), rotateY(180), rotateX(90),
			translate(0.1875, 0.625, -0.125), scale(0.375),
			rotateZ(60), rotateX(-90), rotateZ(20),
			translate(0, -0.3, 0), scale(1.5), rotateY(50), rotateZ(335), translate(-0.9375, -0.0625, 0));

	public static final FloatBuffer FIRST_PERSON = glMatrix(
			// machine FP frame, FORGE_ELSE kept, machine M17 T(.5,.25,0)·S(.25)·Ry(90) dropped
			translate(0.5, 0.5, 0.5), rotateY(45), scale(0.4),
			translate(0, -0.3, 0), scale(1.5), rotateY(50), rotateZ(335), translate(-0.9375, -0.0625, 0));

	public static final FloatBuffer HEAD = glMatrix(
			// machine HEAD frame with the ItemRenderBase equipped preamble removed
			translate(0.5, 0.5, 0.5), scale(1.6, -1.6, -1.6), rotateY(180), translate(0, 0.25, 0),
			translate(0, -0.3, 0), scale(1.5), rotateY(50), rotateZ(335), translate(-0.9375, -0.0625, 0));

	public static final FloatBuffer GROUND = glMatrix(
			// machine GROUND frame · inv(M17 ENTITY S(0.375)·Ry(90)) collapses to this
			translate(0.5, 0.25, 0.5), scale(0.5));

	// FIXED (item frame): the 1.7 ENTITY body rendered in a frame. ItemRenderBase FIXED minus the ENTITY
	// preamble — S(0.375)·Ry(-90)·S(1/0.375) collapses to Ry(-90), so Ry(-90)·Ry(-90) = Ry(180).
	public static final FloatBuffer FIXED = glMatrix(
			translate(0.5, 0.34, 0.53125), rotateY(180));

	// Left hands: T(1,0,0)·op-mirror(right) — negate x-translations and Y/Z rotations (X rotations and
	// uniform scales are mirror-invariant). These equal the ItemRenderBase left-hand matrices minus the
	// mirrored render-body preamble.
	public static final FloatBuffer FIRST_PERSON_LEFT = glMatrix(
			translate(0.5, 0.5, 0.5), rotateY(-45), scale(0.4),
			translate(0, -0.3, 0), scale(1.5), rotateY(-50), rotateZ(-335), translate(0.9375, -0.0625, 0));

	public static final FloatBuffer THIRD_PERSON_LEFT = glMatrix(
			translate(0.5625, 0.375, 1.125), rotateY(180), rotateX(90),
			translate(-0.1875, 0.625, -0.125), scale(0.375),
			rotateZ(-60), rotateX(-90), rotateZ(-20),
			translate(0, -0.3, 0), scale(1.5), rotateY(-50), rotateZ(-335), translate(0.9375, -0.0625, 0));

	// FULL-3D third-person branch (RenderBiped:287 / RenderPlayer:354): items whose 1.7 instance was
	// Item.setFull3D() (e.g. detonator_laser via ModItems) take THIS branch, not the flat one above.
	// Same inv(E12·T-½) prefix + FORGE_ELSE tail; the middle is the 1.7 full-3D held-item pose
	// T(0,.1875,0)·S(.625,-.625,.625)·Rx(-100)·Ry(45). THIRD_PERSON (flat) stays for non-full-3D items.
	public static final FloatBuffer THIRD_PERSON_FULL3D = glMatrix(
			translate(0.4375, 0.375, 1.125), rotateY(180), rotateX(90),
			translate(-0.0625, 0.625, 0.0625), scale(0.625, -0.625, 0.625), rotateX(-100), rotateY(45),
			translate(0, -0.3, 0), scale(1.5), rotateY(50), rotateZ(335), translate(-0.9375, -0.0625, 0));

	public static final FloatBuffer THIRD_PERSON_FULL3D_LEFT = glMatrix(
			translate(0.5625, 0.375, 1.125), rotateY(180), rotateX(90),
			translate(0.0625, 0.625, 0.0625), scale(0.625, -0.625, 0.625), rotateX(-100), rotateY(-45),
			translate(0, -0.3, 0), scale(1.5), rotateY(-50), rotateZ(-335), translate(0.9375, -0.0625, 0));

}
