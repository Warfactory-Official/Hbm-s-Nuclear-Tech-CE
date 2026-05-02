package com.hbm.tileentity.turret;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactory9mm;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.util.Vec3dUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@AutoRegister
public class TileEntityTurretSentryDamaged extends TileEntityTurretSentry {

  @Override
  public boolean hasPower() {
    return true;
  }

  @Override
  public boolean isOn() {
    return true;
  }

  @Override
  public double getTurretYawSpeed() {
    return 3D;
  }

  @Override
  public double getTurretPitchSpeed() {
    return 2D;
  }

  @Override
  public boolean entityAcceptableTarget(Entity e) {

    if (e instanceof EntityPlayer && ((EntityPlayer) e).capabilities.isCreativeMode) return false;

    return e instanceof EntityLivingBase;
  }

  @Override
  public void updateFiringTick() {

    timer++;

    if (timer % 10 == 0) {

      BulletConfig conf = XFactory9mm.p9_fmj;

      if (conf != null) {

      Vec3d pos = this.getTurretPos();
      Vec3d vec = new Vec3d(0, 0, 0);
      Vec3d side = new Vec3d(0, 0, 0);
        this.cachedCasingConfig = conf.casing;

      if (shotSide) {
        this.world.playSound(
            null, this.pos, HBMSoundHandler.sentryFire, SoundCategory.BLOCKS, 2.0F, 1.0F);
        this.spawnBullet(conf, 5F);

        vec = new Vec3d(this.getBarrelLength(), 0, 0);
        vec = Vec3dUtil.rotateRoll(vec, (float) -this.rotationPitch);
        vec = vec.rotateYaw((float) -(this.rotationYaw + Math.PI * 0.5));

        side = new Vec3d(0.125 * (shotSide ? 1 : -1), 0, 0);
        side = side.rotateYaw((float) -(this.rotationYaw));

      } else {
        this.world.playSound(
            null, this.pos, HBMSoundHandler.sentryFire, SoundCategory.BLOCKS, 2.0F, 0.75F);
        if (usesCasings()) {
          if (this.casingDelay() == 0) {
            spawnCasing();
          } else {
            casingDelay = this.casingDelay();
          }
        }
      }

      NBTTagCompound data = new NBTTagCompound();
      data.setFloat("size", 1F);
      data.setByte("count", (byte) 1);
      PacketThreading.createAllAroundThreadedPacket(
          new AuxParticlePacketNT(
              HbmEffectNT.VanillaExt_LargeExplode, data, pos.x + vec.x + side.x, pos.y + vec.y, pos.z + vec.z + side.z),
          new NetworkRegistry.TargetPoint(
              world.provider.getDimension(),
              this.pos.getX(),
              this.pos.getY(),
              this.pos.getZ(),
              50));

      if (shotSide) {
        this.didJustShootLeft = true;
      } else {
        this.didJustShootRight = true;
      }
      shotSide = !shotSide;
    }
    }
  }
}
