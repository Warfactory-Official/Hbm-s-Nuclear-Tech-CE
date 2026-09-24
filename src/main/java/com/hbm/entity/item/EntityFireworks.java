package com.hbm.entity.item;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
@AutoRegister(name = "entity_firework_ball", trackingRange = 1000)
public class EntityFireworks extends Entity {

	int color;
	int character;
	
	public EntityFireworks(World worldIn) {
		super(worldIn);
	}
	
	public EntityFireworks(World world, double x, double y, double z, int color, int character) {
		super(world);
		this.setPositionAndRotation(x, y, z, 0.0F, 0.0F);
		this.color = color;
		this.character = character;
	}
	
	@Override
	public void onUpdate() {
		this.move(MoverType.SELF, 0.0, 3.0D, 0.0);
		this.world.spawnParticle(EnumParticleTypes.FLAME, posX, posY, posZ, 0.0, -0.3, 0.0);
		this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, 0.0, -0.2, 0.0);

		if(!world.isRemote) {

			ticksExisted++;

			if(this.ticksExisted > 30) {

				this.world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_FIREWORK_BLAST, SoundCategory.NEUTRAL, 20, 1F + this.rand.nextFloat() * 0.2F);

				this.setDead();
				NBTTagCompound data = new NBTTagCompound();
				data.setInteger("color", color);
				data.setInteger("char", character);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.Fireworks, data, posX, posY, posZ), new TargetPoint(this.world.provider.getDimension(), posX, posY, posZ, 300));
			}
		}
	}

	@Override
	protected void entityInit() {
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.character = compound.getInteger("char");
		this.color = compound.getInteger("color");
		this.ticksExisted = compound.getInteger("ticksExisted");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("char", character);
		compound.setInteger("color", color);
		compound.setInteger("ticksExisted", ticksExisted);
	}

}
