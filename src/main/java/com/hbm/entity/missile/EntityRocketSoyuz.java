package com.hbm.entity.missile;

import com.hbm.explosion.ExplosionLarge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.saveddata.satellites.Satellite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.List;

@AutoRegister(name = "entity_soyuz", trackingRange = 1000)
public class EntityRocketSoyuz extends EntityRocketBase {

	public static final DataParameter<Integer> SKIN = EntityDataManager.createKey(EntityRocketSoyuz.class, DataSerializers.VARINT);

	public int mode;
	public int targetX;
	public int targetZ;
	boolean memed = false;

	public EntityRocketSoyuz(World worldIn) {
		super(worldIn, 18);
		this.setSize(5.0F, 50.0F);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!world.isRemote) {

			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(posX - 5, posY - 15, posZ - 5, posX + 5, posY, posZ + 5));

			for(Entity e : list) {
				e.setFire(15);
				e.attackEntityFrom(ModDamageSource.exhaust, 100.0F);

				if(e instanceof EntityPlayer) {
					if(!memed) {
						memed = true;
						world.playSound(null, posX, posY, posZ, HBMSoundHandler.soyuzed, SoundCategory.NEUTRAL, 100, 1.0F);
					}

					AdvancementManager.grantAchievement(((EntityPlayer)e), AdvancementManager.achSoyuz);
				}
			}
		}

		if(world.isRemote) {
			spawnExhaust(posX, posY, posZ);
			spawnExhaust(posX + 2.75, posY, posZ);
			spawnExhaust(posX - 2.75, posY, posZ);
			spawnExhaust(posX, posY, posZ + 2.75);
			spawnExhaust(posX, posY, posZ - 2.75);
		}
	}

	private void spawnExhaust(double x, double y, double z) {
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("count", 1);
		data.setDouble("width", world.rand.nextDouble() * 0.25 - 0.5);

		MainRegistry.proxy.effectNT(HbmEffectNT.Exhaust_Soyuz, x, y, z, data);
	}

	@Override
	protected void deployPayload() {

		if(mode == 0) {

			ItemStack load = payload.getStackInSlot(0);

			if(load.getItem() == ModItems.flame_pony) {
				ExplosionLarge.spawnTracers(world, posX, posY, posZ, 25);
				for(EntityPlayer p : world.playerEntities)
					AdvancementManager.grantAchievement(p, AdvancementManager.achSpace);
			}

			if(load.getItem() instanceof ISatChip) {
				int freq = ISatChip.getFreqS(load);
				Satellite.orbit(world, Satellite.getIDFromStack(load), load, freq, posX, posY, posZ);
			}
		}

		if(mode == 1) {

			EntitySoyuzCapsule capsule = new EntitySoyuzCapsule(world);
			for(int i = 0; i < capsule.payload.getSlots() && i < this.payload.getSlots(); i++) {
				capsule.payload.setStackInSlot(i, this.payload.getStackInSlot(i));
			}
			capsule.soyuz = this.getSkin();
			capsule.setPosition(targetX + 0.5, 600, targetZ + 0.5);

			IChunkProvider provider = world.getChunkProvider();
			provider.provideChunk(targetX >> 4, targetZ >> 4);

			world.spawnEntity(capsule);
		}
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(SKIN, 0);
	}

	public void setSkin(int i) {
		this.getDataManager().set(SKIN, i);
	}

	public int getSkin() {
		return this.getDataManager().get(SKIN);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		this.setSkin(nbt.getInteger("skin"));
		targetX = nbt.getInteger("targetX");
		targetZ = nbt.getInteger("targetZ");
		mode = nbt.getInteger("mode");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		nbt.setInteger("skin", this.getSkin());
		nbt.setInteger("targetX", targetX);
		nbt.setInteger("targetZ", targetZ);
		nbt.setInteger("mode", mode);
	}
}
