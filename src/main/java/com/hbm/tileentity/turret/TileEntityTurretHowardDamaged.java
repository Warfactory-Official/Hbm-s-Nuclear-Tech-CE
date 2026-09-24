package com.hbm.tileentity.turret;

import com.hbm.config.WeaponConfig;
import com.hbm.handler.guncfg.GunDGKFactory;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.util.Vec3NT;
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
					
				Vec3NT pos = new Vec3NT(this.getTurretPos());
				Vec3NT vec = Vec3NT.createVectorHelper(this.getBarrelLength(), 0, 0);
				vec.rotateRollSelf((float) -this.rotationPitch);
				vec.rotateYawSelf((float) -(this.rotationYaw + Math.PI * 0.5));
				
				Vec3NT hOff = Vec3NT.createVectorHelper(0, 0.25, 0);
				hOff.rotateRollSelf((float) -this.rotationPitch);
				hOff.rotateYawSelf((float) -(this.rotationYaw + Math.PI * 0.5));
					
				NBTTagCompound data = new NBTTagCompound();
				data.setFloat("size", 1.5F);
				data.setByte("count", (byte)1);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.VanillaExt_LargeExplode, data, pos.x + vec.x + hOff.x, pos.y + vec.y + hOff.y, pos.z + vec.z + hOff.z), new TargetPoint(world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 50));
			}
		}
	}
}
