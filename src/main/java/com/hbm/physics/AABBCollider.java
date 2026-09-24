package com.hbm.physics;

import com.hbm.util.Vec3NT;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.AxisAlignedBB;

import javax.vecmath.Matrix3f;

public class AABBCollider extends Collider {

	public AxisAlignedBB box;
	public float density = -1;
	
	//Only use if this is a static collider.
	public AABBCollider(AxisAlignedBB box) {
		this.box = box;
		this.localCentroid = new Vec3NT(box.getCenter());
	}
	
	public AABBCollider(AxisAlignedBB box, float density) {
		this.box = box;
		float w = (float) (box.maxX-box.minX);
		float h = (float) (box.maxY-box.minY);
		float d = (float) (box.maxZ-box.minZ);
		float vol = w*h*d;
		this.mass = density*vol;
		this.localCentroid = new Vec3NT(box.getCenter());
		//https://en.wikipedia.org/wiki/List_of_moments_of_inertia
		float i_mass = mass/12F;
		this.localInertiaTensor = new Matrix3f(
				i_mass*(h*h+d*d), 0, 0,
				0, i_mass*(w*w+d*d), 0,
				0, 0, i_mass*(w*w+h*h));
	}

	@Override
	public Vec3NT support(Vec3NT direction) {
		return new Vec3NT(
				direction.x > 0 ? box.maxX : box.minX,
				direction.y > 0 ? box.maxY : box.minY,
				direction.z > 0 ? box.maxZ : box.minZ);
	}

	@Override
	public Collider copy() {
		if(density == -1){
			return new AABBCollider(box);
		} else {
			return new AABBCollider(box, density);
		}
	}

	@Override
	public void debugRender() {
		RenderGlobal.drawSelectionBoundingBox(box, 1, 0, 0, 1);
	}
}
