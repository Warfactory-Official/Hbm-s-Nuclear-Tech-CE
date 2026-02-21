package com.hbm.util;

import com.hbm.lib.internal.UnsafeHolder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * A mutable version of {@link Vec3d}, with some extra utilities and supports double precision rotation with intrinsics.
 *
 * @author mlbv
 */
public class MutableVec3d extends Vec3d implements Cloneable {
    private static final long X_OFF = UnsafeHolder.fieldOffset(Vec3d.class, "x", "field_72450_a");
    private static final long Y_OFF = UnsafeHolder.fieldOffset(Vec3d.class, "y", "field_72448_b");
    private static final long Z_OFF = UnsafeHolder.fieldOffset(Vec3d.class, "z", "field_72449_c");
    private static final double DEG2RAD = Math.PI / 180.0;

    public MutableVec3d() {
        super(0.0D, 0.0D, 0.0D);
    }

    public MutableVec3d(double x, double y, double z) {
        super(x, y, z);
    }

    public MutableVec3d(@NotNull Vec3d other) {
        super(other.x, other.y, other.z);
    }

    @Contract("_ -> new")
    public static @NotNull MutableVec3d fromPitchYawMutable(@NotNull Vec2f vec) {
        return fromPitchYawMutable(vec.x, vec.y);
    }

    @Contract("_, _ -> new")
    public static @NotNull MutableVec3d fromPitchYawMutable(float pitchDeg, float yawDeg) {
        final float yaw = (float) (-yawDeg * DEG2RAD - Math.PI);
        final float pitch = (float) (-pitchDeg * DEG2RAD);
        final float cy = MathHelper.cos(yaw);
        final float sy = MathHelper.sin(yaw);
        final float cp = MathHelper.cos(pitch);
        final float sp = MathHelper.sin(pitch);
        return new MutableVec3d(sy * cp, sp, cy * cp);
    }

    @Contract(mutates = "this")
    public MutableVec3d set(double x, double y, double z) {
        U.putDouble(this, X_OFF, x + 0.0D);
        U.putDouble(this, Y_OFF, y + 0.0D);
        U.putDouble(this, Z_OFF, z + 0.0D);
        return this;
    }

    @Contract(mutates = "this")
    public MutableVec3d set(@NotNull Vec3d v) {
        return set(v.x, v.y, v.z);
    }

    @Contract(mutates = "this")
    public MutableVec3d setX(double x) {
        U.putDouble(this, X_OFF, x + 0.0D);
        return this;
    }

    @Contract(mutates = "this")
    public MutableVec3d setY(double y) {
        U.putDouble(this, Y_OFF, y + 0.0D);
        return this;
    }

    @Contract(mutates = "this")
    public MutableVec3d setZ(double z) {
        U.putDouble(this, Z_OFF, z + 0.0D);
        return this;
    }

    @Contract(mutates = "this")
    public MutableVec3d zero() {
        return set(0.0D, 0.0D, 0.0D);
    }

    @Contract(mutates = "this")
    public MutableVec3d addSelf(double dx, double dy, double dz) {
        return set(this.x + dx, this.y + dy, this.z + dz);
    }

    @Contract(mutates = "this")
    public MutableVec3d addSelf(@NotNull Vec3d v) {
        return addSelf(v.x, v.y, v.z);
    }

    @Contract(mutates = "this")
    public MutableVec3d subSelf(double dx, double dy, double dz) {
        return set(this.x - dx, this.y - dy, this.z - dz);
    }

    @Contract(mutates = "this")
    public MutableVec3d subSelf(@NotNull Vec3d v) {
        return subSelf(v.x, v.y, v.z);
    }

    @Contract(mutates = "this")
    public MutableVec3d scaleSelf(double s) {
        return set(this.x * s, this.y * s, this.z * s);
    }

    @Contract(mutates = "this")
    public MutableVec3d mulAddSelf(double s, @NotNull Vec3d v) {
        return set(this.x + s * v.x, this.y + s * v.y, this.z + s * v.z);
    }

    @Contract(mutates = "this")
    public MutableVec3d normalizeSelf() {
        double len = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        if (len < 1.0E-4D) return set(0.0D, 0.0D, 0.0D);
        double inv = 1.0D / len;
        return set(this.x * inv, this.y * inv, this.z * inv);
    }

    @Contract(mutates = "this")
    public MutableVec3d rotateYawSelf(float yaw) {
        double c = MathHelper.cos(yaw);
        double s = MathHelper.sin(yaw);
        double nx = this.x * c + this.z * s;
        double nz = this.z * c - this.x * s;
        return set(nx, this.y, nz);
    }

    @Contract(mutates = "this")
    public MutableVec3d rotatePitchSelf(float pitch) {
        double c = MathHelper.cos(pitch);
        double s = MathHelper.sin(pitch);
        double ny = this.y * c + this.z * s;
        double nz = this.z * c - this.y * s;
        return set(this.x, ny, nz);
    }

    @Contract(mutates = "this")
    public MutableVec3d rotateRollSelf(float roll) {
        double c = MathHelper.cos(roll);
        double s = MathHelper.sin(roll);
        double nx = this.x * c + this.y * s;
        double ny = this.y * c - this.x * s;
        return set(nx, ny, this.z);
    }

    @Contract(mutates = "this")
    public MutableVec3d rotateYawSelf(double yaw) {
        double c = Math.cos(yaw);
        double s = Math.sin(yaw);
        double nx = this.x * c + this.z * s;
        double nz = this.z * c - this.x * s;
        return set(nx, this.y, nz);
    }

    @Contract(mutates = "this")
    public MutableVec3d rotatePitchSelf(double pitch) {
        double c = Math.cos(pitch);
        double s = Math.sin(pitch);
        double ny = this.y * c + this.z * s;
        double nz = this.z * c - this.y * s;
        return set(this.x, ny, nz);
    }

    @Contract(mutates = "this")
    public MutableVec3d rotateRollSelf(double roll) {
        double c = Math.cos(roll);
        double s = Math.sin(roll);
        double nx = this.x * c + this.y * s;
        double ny = this.y * c - this.x * s;
        return set(nx, ny, this.z);
    }

    @Contract(mutates = "this")
    public MutableVec3d lerpSelf(Vec3d other, double t) {
        double x = this.x + (other.x - this.x) * t;
        double y = this.y + (other.y - this.y) * t;
        double z = this.z + (other.z - this.z) * t;
        return set(x, y, z);
    }

    @Override
    @Contract("_, _, _ -> new")
    public MutableVec3d add(double x, double y, double z) {
        return new MutableVec3d(this.x + x, this.y + y, this.z + z);
    }

    @Override
    @Contract("_ -> new")
    public MutableVec3d add(@NotNull Vec3d vec) {
        return new MutableVec3d(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }

    @Override
    @Contract("_, _, _ -> new")
    public MutableVec3d subtract(double x, double y, double z) {
        return new MutableVec3d(this.x - x, this.y - y, this.z - z);
    }

    @Override
    @Contract("_ -> new")
    public MutableVec3d subtract(@NotNull Vec3d vec) {
        return new MutableVec3d(this.x - vec.x, this.y - vec.y, this.z - vec.z);
    }

    @Override
    @Contract("_ -> new")
    public MutableVec3d scale(double factor) {
        return new MutableVec3d(this.x * factor, this.y * factor, this.z * factor);
    }

    @Override
    @Contract("-> new")
    public MutableVec3d normalize() {
        double len = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        return (len < 1.0E-4D) ? new MutableVec3d(0.0, 0.0, 0.0) : new MutableVec3d(this.x / len, this.y / len, this.z / len);
    }

    @Override
    @Contract("_ -> new")
    public MutableVec3d crossProduct(@NotNull Vec3d vec) {
        return new MutableVec3d(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
    }

    @Override
    @Contract("_ -> new")
    public MutableVec3d rotateYaw(float yaw) {
        double c = MathHelper.cos(yaw);
        double s = MathHelper.sin(yaw);
        double nx = this.x * c + this.z * s;
        double nz = this.z * c - this.x * s;
        return new MutableVec3d(nx, this.y, nz);
    }

    @Override
    @Contract("_ -> new")
    public MutableVec3d rotatePitch(float pitch) {
        double c = MathHelper.cos(pitch);
        double s = MathHelper.sin(pitch);
        double ny = this.y * c + this.z * s;
        double nz = this.z * c - this.y * s;
        return new MutableVec3d(this.x, ny, nz);
    }

    @Contract("_ -> new")
    public MutableVec3d rotateRoll(float roll) {
        double c = MathHelper.cos(roll);
        double s = MathHelper.sin(roll);
        double nx = this.x * c + this.y * s;
        double ny = this.y * c - this.x * s;
        return new MutableVec3d(nx, ny, this.z);
    }

    @Contract("_ -> new")
    public MutableVec3d rotateYaw(double yaw) {
        double c = Math.cos(yaw);
        double s = Math.sin(yaw);
        double nx = this.x * c + this.z * s;
        double nz = this.z * c - this.x * s;
        return new MutableVec3d(nx, this.y, nz);
    }

    @Contract("_ -> new")
    public MutableVec3d rotatePitch(double pitch) {
        double c = Math.cos(pitch);
        double s = Math.sin(pitch);
        double ny = this.y * c + this.z * s;
        double nz = this.z * c - this.y * s;
        return new MutableVec3d(this.x, ny, nz);
    }

    @Contract("_ -> new")
    public MutableVec3d rotateRoll(double roll) {
        double c = Math.cos(roll);
        double s = Math.sin(roll);
        double nx = this.x * c + this.y * s;
        double ny = this.y * c - this.x * s;
        return new MutableVec3d(nx, ny, this.z);
    }

    @Contract("_, _ -> new")
    public MutableVec3d lerp(@NotNull Vec3d other, double t) {
        double x = this.x + (other.x - this.x) * t;
        double y = this.y + (other.y - this.y) * t;
        double z = this.z + (other.z - this.z) * t;
        return new MutableVec3d(x, y, z);
    }

    @Contract("-> new")
    public Vec3i toVec3i() {
        return new Vec3i(MathHelper.floor(this.x), MathHelper.floor(this.y), MathHelper.floor(this.z));
    }

    @Contract("-> new")
    public Vec3d toImmutable() {
        return new Vec3d(this.x, this.y, this.z);
    }

    @Override
    @Contract("-> new")
    public @NotNull MutableVec3d clone() {
        try {
            return (MutableVec3d) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
