package com.hbm.tileentity.turret;

import com.hbm.config.WeaponConfig;
import com.hbm.handler.guncfg.GunDGKFactory;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.EntityDamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

@AutoRegister
public class TileEntityTurretHowardDamaged extends TileEntityTurretHoward {

	@Override
	public boolean hasPower() { //does not need power
		return true;
	}

	@Override
	public boolean isOn() { //is always on
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
	public double getDecetorRange() {
		return 16D;
	}

	@Override
	public double getDecetorGrace() {
		return 5D;
	}

	@Override
	public boolean hasThermalVision() {
		return false;
	}
	
	@Override
	public boolean entityAcceptableTarget(Entity e) { //will fire at any living entity
		
		if(e instanceof EntityPlayer && ((EntityPlayer)e).capabilities.isCreativeMode)
			return false;
		
		return e instanceof EntityLivingBase;
	}
	
	@Override
	public void updateFiringTick(){
		timer++;
		
		if(this.tPos != null) {
			
			if(timer % 4 == 0) {
				
				this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.howard_fire, SoundCategory.BLOCKS, 4.0F, 0.7F + world.rand.nextFloat() * 0.3F);

				this.cachedCasingConfig = GunDGKFactory.CASINGDGK;
				this.spawnCasing();

				if(world.rand.nextInt(100) + 1 <= WeaponConfig.ciwsHitrate * 0.5)
					EntityDamageUtil.attackEntityFromIgnoreIFrame(this.target, ModDamageSource.shrapnel, 2F + world.rand.nextInt(2));
					
				Vec3 pos = new Vec3(this.getTurretPos());
				Vec3 vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
				vec.rotateAroundZ((float) -this.rotationPitch);
				vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));
				
				Vec3 hOff = Vec3.createVectorHelper(0, 0.25, 0);
				hOff.rotateAroundZ((float) -this.rotationPitch);
				hOff.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));
					
				NBTTagCompound data = new NBTTagCompound();
				data.setFloat("size", 1.5F);
				data.setByte("count", (byte)1);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.VanillaExt_LargeExplode, data, pos.xCoord + vec.xCoord + hOff.xCoord, pos.yCoord + vec.yCoord + hOff.yCoord, pos.zCoord + vec.zCoord + hOff.zCoord), new TargetPoint(world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 50));
			}
		}
	}
}
