package com.hbm.render.model;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public final class BakedModelMatrixUtil {

    private BakedModelMatrixUtil() {
    }

    public static Matrix4f identity() {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        return matrix;
    }

    public static Matrix4f compose(Matrix4f... operations) {
        Matrix4f result = identity();
        for (Matrix4f operation : operations) {
            result.mul(operation);
        }
        return result;
    }

    public static Matrix4f translate(double x, double y, double z) {
        Matrix4f matrix = identity();
        matrix.m03 = (float) x;
        matrix.m13 = (float) y;
        matrix.m23 = (float) z;
        return matrix;
    }

    public static Matrix4f scale(double xyz) {
        return scale(xyz, xyz, xyz);
    }

    public static Matrix4f scale(double x, double y, double z) {
        Matrix4f matrix = identity();
        matrix.m00 = (float) x;
        matrix.m11 = (float) y;
        matrix.m22 = (float) z;
        return matrix;
    }

    public static Matrix4f rotateX(double degrees) {
        double radians = Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        Matrix4f matrix = identity();
        matrix.m11 = cos;
        matrix.m12 = -sin;
        matrix.m21 = sin;
        matrix.m22 = cos;
        return matrix;
    }

    public static Matrix4f rotateY(double degrees) {
        double radians = Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        Matrix4f matrix = identity();
        matrix.m00 = cos;
        matrix.m02 = sin;
        matrix.m20 = -sin;
        matrix.m22 = cos;
        return matrix;
    }

    public static Matrix4f rotateZ(double degrees) {
        double radians = Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        Matrix4f matrix = identity();
        matrix.m00 = cos;
        matrix.m01 = -sin;
        matrix.m10 = sin;
        matrix.m11 = cos;
        return matrix;
    }

    public static Vector3f transformPosition(Matrix4f matrix, float x, float y, float z) {
        return new Vector3f(
                matrix.m00 * x + matrix.m01 * y + matrix.m02 * z + matrix.m03,
                matrix.m10 * x + matrix.m11 * y + matrix.m12 * z + matrix.m13,
                matrix.m20 * x + matrix.m21 * y + matrix.m22 * z + matrix.m23
        );
    }

    public static Vector3f transformNormal(Matrix4f matrix, float x, float y, float z) {
        Vector3f normal = new Vector3f(
                matrix.m00 * x + matrix.m01 * y + matrix.m02 * z,
                matrix.m10 * x + matrix.m11 * y + matrix.m12 * z,
                matrix.m20 * x + matrix.m21 * y + matrix.m22 * z
        );
        if (normal.lengthSquared() > 0.0F) {
            normal.normalize();
        }
        return normal;
    }
}
