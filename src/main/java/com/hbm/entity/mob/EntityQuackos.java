package com.hbm.entity.mob;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 *  BOW
 */
@AutoRegister(name = "entity_elder_one", trackingRange = 1000, eggColors = {0xd0d0d0, 0xFFBF00})
public class EntityQuackos extends EntityDuck {

	/**
     *  BOW
     */
	private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS));
	
	/**
     *  BOW
     */
	public EntityQuackos(World worldIn) {
		super(worldIn);
		this.setSize(0.3F * 25, 0.7F * 25);
		this.ignoreFrustumCheck = true;
	}
	
	/**
     *  BOW
     */
	@Override
	protected SoundEvent getAmbientSound() {
		return HBMSoundHandler.megaquacc;
	}
	
	/**
     *  BOW
     */
	@Override
	protected SoundEvent getDeathSound() {
		return HBMSoundHandler.megaquacc;
	}
	
	/**
     *  BOW
     */
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return HBMSoundHandler.megaquacc;
	}
	
	/**
     *  BOW
     */
	@Override
	protected SoundEvent getFallSound(int heightIn) {
		return HBMSoundHandler.megaquacc;
	}
	
	/**
     *  BOW
     */
	@Override
	public boolean getIsInvulnerable() {
		return true;
	}
	
	/**
     *  BOW
     */
	@Override
	public void setHealth(float health) {
		if(health < this.getHealth()){
			return;
		}
		super.setHealth(health);
	}
	
	/**
     *  BOW
     */
	@Override
	public void setDead() {
		if(world.isRemote){
			//Fix an issue where Quackos wouldn't get removed from the client world, causing ghost entities
			//Shouldn't affect the logic though, he still can't be removed from the server by this method
			super.setDead();
		}
	}
	
	/**
     *  BOW
     */
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if(super.processInteract(player, hand)){
			return true;
		} else if(!this.world.isRemote && this.getPassengers().size() == 0) {
			player.startRiding(this);
			return true;

		} else {
			return false;
		}
	}
	
	/**
     *  BOW
     */
	@Override
	public void addTrackingPlayer(EntityPlayerMP player) {
		super.addTrackingPlayer(player);
		bossInfo.addPlayer(player);
	}
	
	/**
     *  BOW
     */
	@Override
	public void removeTrackingPlayer(EntityPlayerMP player) {
		super.removeTrackingPlayer(player);
		bossInfo.removePlayer(player);
	}
	
	/**
	 * BOW
	 */
	public void despawn() {
		if(!world.isRemote) {
			for(int i = 0; i < 150; i++) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "bf");
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data,
								posX + rand.nextDouble() * 20 - 10,
								posY + rand.nextDouble() * 25,
								posZ + rand.nextDouble() * 20 - 10),
						new NetworkRegistry.TargetPoint(dimension, posX, posY, posZ, 150));
			}
		}
		this.isDead = true;
	}
	
	/**
	 * BOW
	 */
	@Override
	protected boolean canDespawn() {
		return false;
	}
	
	/**
     *  BOW
     */
	@Override
	public void updatePassenger(Entity passenger) {
		super.updatePassenger(passenger);
		float f = MathHelper.sin(this.renderYawOffset * (float)Math.PI / 180.0F);
        float f1 = MathHelper.cos(this.renderYawOffset * (float)Math.PI / 180.0F);
        float f2 = 0.1F;
        float f3 = 0.0F;
        passenger.setPosition(this.posX + (double)(f2 * f), this.posY + (double)(this.height - 0.125F) + passenger.getYOffset() + (double)f3, this.posZ - (double)(f2 * f1));

        if (passenger instanceof EntityLivingBase) {
            ((EntityLivingBase)passenger).renderYawOffset = this.renderYawOffset;
        }
	}
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if(!world.isRemote && this.posY < -30) {
			this.setPosition(this.posX + rand.nextGaussian() * 30, 256, this.posZ + rand.nextGaussian() * 30);
		}
	}
}
