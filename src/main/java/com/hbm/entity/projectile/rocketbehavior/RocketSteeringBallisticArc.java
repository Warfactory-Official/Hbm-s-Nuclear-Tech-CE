package com.hbm.entity.projectile.rocketbehavior;

import com.hbm.entity.projectile.EntityArtilleryRocket;
import com.hbm.util.Vec3NT;
import net.minecraft.util.math.Vec3d;

public class RocketSteeringBallisticArc implements IRocketSteeringBehavior {

  @Override
  public void adjustCourse(EntityArtilleryRocket rocket, double speed, double maxTurn) {

    double turnSpeed = 45;

    Vec3d direction =
        new Vec3d(rocket.motionX, rocket.motionY, rocket.motionZ).normalize();
    double horizontalMomentum =
        Math.sqrt(rocket.motionX * rocket.motionX + rocket.motionZ * rocket.motionZ);
    Vec3d targetPos = rocket.getLastTarget();
    double deltaX = targetPos.x - rocket.posX;
    double deltaZ = targetPos.z - rocket.posZ;
    double horizontalDelta = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    double stepsRequired = horizontalDelta / horizontalMomentum;
    Vec3d target =
        new Vec3d (
                targetPos.x - rocket.posX,
                targetPos.y - rocket.posY,
                targetPos.z - rocket.posZ)
            .normalize();

    /* the entity's angles lack precision and i lack the nerve to figure out how they're oriented */
    double rocketYaw = yaw(direction);
    double rocketPitch = pitch(direction);
    double targetYaw = yaw(target);
    double targetPitch = pitch(target);

    boolean debug = false;

    if (debug) {
      System.out.println("=== INITIAL ===");
      System.out.println("Rocket Yaw: " + rocketYaw);
      System.out.println("Rocket Pitch: " + rocketPitch);
      System.out.println("Target Yaw: " + targetYaw);
      System.out.println("Target Pitch: " + targetPitch);
    }

    turnSpeed = Math.min(maxTurn, turnSpeed / stepsRequired);

    /* ...and then we just cheat */
    if (stepsRequired <= 1) {
      turnSpeed = 180D;
    }

    /*if(stepsRequired > 1) {
    	targetPitch = rocketPitch + ((targetPitch - rocketPitch) / stepsRequired);
    }*/

    if (debug) {
      System.out.println("=== ADJUSTED ===");
      System.out.println("Target Pitch: " + targetPitch);
    }

    /* shortest delta of α < 180° */
    double deltaYaw = ((targetYaw - rocketYaw) + 180D) % 360D - 180D;
    double deltaPitch = ((targetPitch - rocketPitch) + 180D) % 360D - 180D;

    double turnYaw = Math.min(Math.abs(deltaYaw), turnSpeed) * Math.signum(deltaYaw);
    double turnPitch = Math.min(Math.abs(deltaPitch), turnSpeed) * Math.signum(deltaPitch);

    if (debug) {
      System.out.println("=== RESULTS ===");
      System.out.println("Delta Yaw: " + deltaYaw);
      System.out.println("Delta Pitch: " + deltaPitch);
      System.out.println("Turn Yaw: " + turnYaw);
      System.out.println("Turn Pitch: " + turnPitch);
    }

    Vec3NT velocity = Vec3NT.createVectorHelper(speed, 0, 0);
    velocity.rotateRollSelf((float) -Math.toRadians(rocketPitch + turnPitch));
    velocity.rotateYawSelf((float) Math.toRadians(rocketYaw + turnYaw + 90));

    rocket.motionX = velocity.x;
    rocket.motionY = velocity.y;
    rocket.motionZ = velocity.z;

    rocket.rotationPitch += (float) turnPitch;
  }

  private static double yaw(Vec3d vec) {
    boolean pos = vec.z >= 0;
    return Math.toDegrees(Math.atan(vec.x / vec.z)) + (pos ? 180 : 0);
  }

  private static double pitch(Vec3d vec) {
    return Math.toDegrees(
        Math.atan(vec.y / Math.sqrt(vec.x * vec.x + vec.z * vec.z)));
  }
}
