package com.hbm.handler;

import com.hbm.particle.ParticleSpentCasing;
import com.hbm.particle.SpentCasing;
import com.hbm.util.Vec3NT;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Random;

public class CasingEjector implements Cloneable {
    public static final Random rand = new Random();
    public static HashMap<Integer, CasingEjector> mappings = new HashMap<>();
    private static int nextId = 0;
    private final int id;
    private Vec3NT posOffset = Vec3NT.ZERO();
    private Vec3NT initialMotion = Vec3NT.ZERO();

    @Deprecated
    private int casingAmount = 1;
    @Deprecated
    private boolean afterReload = false;
    @Deprecated
    private int delay = 0;

    private float randomYaw = 0F;
    private float randomPitch = 0F;

    public CasingEjector() {
        this.id = nextId++;
        mappings.put(id, this);
    }


    /**
     * Translates the particle position from the player's center to the ejection port.
     */
    @SideOnly(Side.CLIENT)
    private static void offsetCasing(ParticleSpentCasing casing, Vec3NT offset, float pitch, float yaw, boolean crouched) {
        Vec3NT result = new Vec3NT(crouched ? 0 : offset.x, offset.y, offset.z);

        result.rotateAroundXRad(pitch);
        result.rotateAroundYRad(-yaw);

        casing.setPosition(casing.getPosX() + result.x, casing.getPosY() + result.y, casing.getPosZ() + result.z);
    }

    /**
     * Rotates the base motion vector and applies gaussian jitter.
     */
    private static Vec3NT rotateVector(Vec3NT vector, float pitch, float yaw, float pitchFactor, float yawFactor) {
        Vec3NT result = vector.clone();

        result.addi(
                rand.nextGaussian() * yawFactor * 0.1D,
                rand.nextGaussian() * pitchFactor * 0.1D,
                rand.nextGaussian() * yawFactor * 0.1D
        );

        result.rotateAroundXRad(pitch);
        result.rotateAroundYRad(-yaw);

        return result;
    }

    public static CasingEjector fromId(int id) {
        return mappings.get(id);
    }

    public CasingEjector setOffset(double x, double y, double z) {
        this.posOffset.set(x, y, z);
        return this;
    }

    public CasingEjector setMotion(double x, double y, double z) {
        this.initialMotion.set(x, y, z);
        return this;
    }

    public CasingEjector setAngleRange(float yaw, float pitch) {
        this.randomYaw = yaw;
        this.randomPitch = pitch;
        return this;
    }

    @Deprecated
    public CasingEjector setAfterReload() {
        this.afterReload = true;
        return this;
    }

    public int getId() {
        return this.id;
    }

    public Vec3NT getOffset() {
        return this.posOffset;
    }

    public CasingEjector setOffset(Vec3NT vec) {
        this.posOffset.set(vec);
        return this;
    }

    public Vec3NT getMotion() {
        return this.initialMotion;
    }

    public CasingEjector setMotion(Vec3NT vec) {
        this.initialMotion.set(vec);
        return this;
    }

    public int getAmount() {
        return this.casingAmount;
    }

    @Deprecated
    public CasingEjector setAmount(int am) {
        this.casingAmount = am;
        return this;
    }

    public boolean getAfterReload() {
        return this.afterReload;
    }

    public int getDelay() {
        return this.delay;
    }

    @Deprecated
    public CasingEjector setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    public float getYawFactor() {
        return this.randomYaw;
    }

    public float getPitchFactor() {
        return this.randomPitch;
    }

    @SideOnly(Side.CLIENT)
    public void spawnCasing(SpentCasing config, World world, double x, double y, double z, float pitch, float yaw, boolean crouched) {
        Vec3NT rotatedMotionVec = rotateVector(getMotion(), pitch, yaw, getPitchFactor(), getYawFactor());

        float momentumP = (float) (getPitchFactor() * rand.nextGaussian() * 5.0F);
        float momentumY = (float) (getYawFactor() * rand.nextGaussian() * 5.0F);

        ParticleSpentCasing casing = new ParticleSpentCasing(
                world,
                x, y, z,
                rotatedMotionVec.x, rotatedMotionVec.y, rotatedMotionVec.z,
                momentumP, momentumY,
                config,
                true, // isSmoking
                60,   // smokeLife
                0.5D, // smokeLift
                30    // nodeLife
        );

        offsetCasing(casing, getOffset(), pitch, yaw, crouched);

        casing.rotationPitch = (float) Math.toDegrees(pitch);
        casing.rotationYaw = (float) Math.toDegrees(yaw);

        Minecraft.getMinecraft().effectRenderer.addEffect(casing);
    }

    @Override
    public CasingEjector clone() {
        try {
            CasingEjector cloned = (CasingEjector) super.clone();
            cloned.posOffset = this.posOffset.clone();
            cloned.initialMotion = this.initialMotion.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            return new CasingEjector()
                    .setOffset(this.posOffset.clone())
                    .setMotion(this.initialMotion.clone())
                    .setAngleRange(this.randomYaw, this.randomPitch);
        }
    }
}
