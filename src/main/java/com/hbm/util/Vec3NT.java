package com.hbm.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Vec3NT extends MutableVec3d {

    public Vec3NT() {
        super(0.0D, 0.0D, 0.0D);
    }
    public static Vec3NT ZERO(){
        return new Vec3NT(0,0,0);
    }

    public Vec3NT(double x, double y, double z) {
        super(x, y, z);
    }

    public Vec3NT(@NotNull Vec3d vec) {
        super(vec.x, vec.y, vec.z);
    }

    public Vec3NT(@NotNull Vec3NT vec) {
        super(vec.x, vec.y, vec.z);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Vec3NT of(double x, double y, double z) {
        return new Vec3NT(x, y, z);
    }

    @Contract("_ -> new")
    public static @NotNull Vec3NT from(@NotNull Vec3d v) {
        return new Vec3NT(v.x, v.y, v.z);
    }

    @Contract("_ -> new")
    public static @NotNull Vec3NT fromPitchYaw(@NotNull Vec2f vec) {
        return fromPitchYaw(vec.x, vec.y);
    }

    @Contract("_, _ -> new")
    public static @NotNull Vec3NT fromPitchYaw(float pitchDeg, float yawDeg) {
        final float yaw = (float) (-yawDeg * (Math.PI / 180.0) - Math.PI);
        final float pitch = (float) (-pitchDeg * (Math.PI / 180.0));
        final float cy = MathHelper.cos(yaw);
        final float sy = MathHelper.sin(yaw);
        final float cp = MathHelper.cos(pitch);
        final float sp = MathHelper.sin(pitch);
        return new Vec3NT(sy * cp, sp, cy * cp);
    }

    public static double getMinX(@NotNull Vec3d... vecs) {
        double min = Double.POSITIVE_INFINITY;
        for (Vec3d v : vecs) if (v.x < min) min = v.x;
        return min;
    }

    public static double getMinY(@NotNull Vec3d... vecs) {
        double min = Double.POSITIVE_INFINITY;
        for (Vec3d v : vecs) if (v.y < min) min = v.y;
        return min;
    }

    public static double getMinZ(@NotNull Vec3d... vecs) {
        double min = Double.POSITIVE_INFINITY;
        for (Vec3d v : vecs) if (v.z < min) min = v.z;
        return min;
    }

    public static double getMaxX(@NotNull Vec3d... vecs) {
        double max = Double.NEGATIVE_INFINITY;
        for (Vec3d v : vecs) if (v.x > max) max = v.x;
        return max;
    }

    public static double getMaxY(@NotNull Vec3d... vecs) {
        double max = Double.NEGATIVE_INFINITY;
        for (Vec3d v : vecs) if (v.y > max) max = v.y;
        return max;
    }

    public static double getMaxZ(@NotNull Vec3d... vecs) {
        double max = Double.NEGATIVE_INFINITY;
        for (Vec3d v : vecs) if (v.z > max) max = v.z;
        return max;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT set(double x, double y, double z) {
        super.set(x, y, z);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT set(@NotNull Vec3d v) {
        super.set(v);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT setX(double x) {
        super.setX(x);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT setY(double y) {
        super.setY(y);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT setZ(double z) {
        super.setZ(z);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT zero() {
        super.zero();
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT addSelf(double dx, double dy, double dz) {
        super.addSelf(dx, dy, dz);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT addSelf(@NotNull Vec3d v) {
        super.addSelf(v);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT subSelf(double dx, double dy, double dz) {
        super.subSelf(dx, dy, dz);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT subSelf(@NotNull Vec3d v) {
        super.subSelf(v);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT scaleSelf(double s) {
        super.scaleSelf(s);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT mulAddSelf(double s, @NotNull Vec3d v) {
        super.mulAddSelf(s, v);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT normalizeSelf() {
        super.normalizeSelf();
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT rotateYawSelf(float yaw) {
        super.rotateYawSelf(yaw);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT rotatePitchSelf(float pitch) {
        super.rotatePitchSelf(pitch);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT rotateRollSelf(float roll) {
        super.rotateRollSelf(roll);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT rotateYawSelf(double yaw) {
        super.rotateYawSelf(yaw);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT rotatePitchSelf(double pitch) {
        super.rotatePitchSelf(pitch);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT rotateRollSelf(double roll) {
        super.rotateRollSelf(roll);
        return this;
    }

    @Override
    @Contract(mutates = "this")
    public @NotNull Vec3NT lerpSelf(@NotNull Vec3d other, double t) {
        super.lerpSelf(other, t);
        return this;
    }

    @Override
    @Contract("_, _, _ -> new")
    public @NotNull Vec3NT add(double x, double y, double z) {
        return new Vec3NT(this.x + x, this.y + y, this.z + z);
    }

    @Override
    @Contract("_ -> new")
    public @NotNull Vec3NT add(@NotNull Vec3d vec) {
        return new Vec3NT(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }

    @Override
    @Contract("_, _, _ -> new")
    public @NotNull Vec3NT subtract(double x, double y, double z) {
        return new Vec3NT(this.x - x, this.y - y, this.z - z);
    }

    @Override
    @Contract("_ -> new")
    public @NotNull Vec3NT subtract(@NotNull Vec3d vec) {
        return new Vec3NT(this.x - vec.x, this.y - vec.y, this.z - vec.z);
    }

    @Override
    @Contract("_ -> new")
    public @NotNull Vec3NT scale(double factor) {
        return new Vec3NT(this.x * factor, this.y * factor, this.z * factor);
    }

    @Override
    @Contract("-> new")
    public @NotNull Vec3NT normalize() {
        double len = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        return (len < 1.0E-4D) ? new Vec3NT(0.0, 0.0, 0.0) : new Vec3NT(this.x / len, this.y / len, this.z / len);
    }

    @Override
    @Contract("_ -> new")
    public @NotNull Vec3NT crossProduct(@NotNull Vec3d vec) {
        return new Vec3NT(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
    }

    @Override
    @Contract("_ -> new")
    public @NotNull Vec3NT rotateYaw(float yaw) {
        double c = MathHelper.cos(yaw), s = MathHelper.sin(yaw);
        return new Vec3NT(this.x * c + this.z * s, this.y, this.z * c - this.x * s);
    }

    @Override
    @Contract("_ -> new")
    public @NotNull Vec3NT rotatePitch(float pitch) {
        double c = MathHelper.cos(pitch), s = MathHelper.sin(pitch);
        return new Vec3NT(this.x, this.y * c + this.z * s, this.z * c - this.y * s);
    }

    @Contract("_ -> new")
    public @NotNull Vec3NT rotateRoll(float roll) {
        double c = MathHelper.cos(roll), s = MathHelper.sin(roll);
        return new Vec3NT(this.x * c + this.y * s, this.y * c - this.x * s, this.z);
    }

    @Override
    @Contract("_ -> new")
    public @NotNull Vec3NT rotateYaw(double yaw) {
        double c = Math.cos(yaw), s = Math.sin(yaw);
        return new Vec3NT(this.x * c + this.z * s, this.y, this.z * c - this.x * s);
    }

    @Override
    @Contract("_ -> new")
    public @NotNull Vec3NT rotatePitch(double pitch) {
        double c = Math.cos(pitch), s = Math.sin(pitch);
        return new Vec3NT(this.x, this.y * c + this.z * s, this.z * c - this.y * s);
    }

    @Contract("_ -> new")
    public @NotNull Vec3NT rotateRoll(double roll) {
        double c = Math.cos(roll), s = Math.sin(roll);
        return new Vec3NT(this.x * c + this.y * s, this.y * c - this.x * s, this.z);
    }

    @Override
    @Contract("_, _ -> new")
    public @NotNull Vec3NT lerp(@NotNull Vec3d other, double t) {
        return new Vec3NT(this.x + (other.x - this.x) * t, this.y + (other.y - this.y) * t, this.z + (other.z - this.z) * t);
    }

    @Contract(mutates = "this")
    public @NotNull Vec3NT multiply(double m) {
        set(this.x * m, this.y * m, this.z * m);
        return this;
    }

    @Contract(mutates = "this")
    public @NotNull Vec3NT multiply(double mx, double my, double mz) {
        set(this.x * mx, this.y * my, this.z * mz);
        return this;
    }

    @Contract(mutates = "this")
    public @NotNull Vec3NT addi(@NotNull Vec3d v) {
        addSelf(v);
        return this;
    }

    @Contract(mutates = "this")
    public @NotNull Vec3NT addi(double dx, double dy, double dz) {
        addSelf(dx, dy, dz);
        return this;
    }

    public double distanceTo(double x, double y, double z) {
        double dx = x - this.x, dy = y - this.y, dz = z - this.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Contract(mutates = "this")
    public @NotNull Vec3NT rotateAroundXRad(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        double ny = this.y * c - this.z * s, nz = this.y * s + this.z * c;
        set(this.x, ny, nz);
        return this;
    }

    @Contract(mutates = "this")
    public @NotNull Vec3NT rotateAroundYRad(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        double nx = this.x * c + this.z * s, nz = -this.x * s + this.z * c;
        set(nx, this.y, nz);
        return this;
    }

    @Contract(mutates = "this")
    public @NotNull Vec3NT rotateAroundZRad(double a) {
        double c = Math.cos(a), s = Math.sin(a);
        double nx = this.x * c - this.y * s, ny = this.x * s + this.y * c;
        set(nx, ny, this.z);
        return this;
    }

    @Contract(mutates = "this")
    public @NotNull Vec3NT rotateAroundXDeg(double d) {
        return rotateAroundXRad(Math.toRadians(d));
    }

    @Contract(mutates = "this")
    public @NotNull Vec3NT rotateAroundYDeg(double d) {
        return rotateAroundYRad(Math.toRadians(d));
    }

    @Contract(mutates = "this")
    public @NotNull Vec3NT rotateAroundZDeg(double d) {
        return rotateAroundZRad(Math.toRadians(d));
    }

    @Contract("-> new")
    public @NotNull Vec3d toVec3d() {
        return toImmutable();
    }

    @Override
    @Contract("-> new")
    public @NotNull Vec3NT clone() {
        return (Vec3NT) super.clone();
    }

    @Override
    public String toString() {
        return "Vec3NT[" + this.x + ", " + this.y + ", " + this.z + "]";
    }
}
