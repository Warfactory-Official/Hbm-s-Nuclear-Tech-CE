package com.hbm.physics;

import com.hbm.util.Vec3NT;

import javax.vecmath.Matrix3f;

public abstract class Collider {

	public float mass;
	public Matrix3f localInertiaTensor;
	public Vec3NT localCentroid;
	
	public abstract Vec3NT support(Vec3NT direction);
	
	public abstract Collider copy();
	
	public abstract void debugRender();
}
