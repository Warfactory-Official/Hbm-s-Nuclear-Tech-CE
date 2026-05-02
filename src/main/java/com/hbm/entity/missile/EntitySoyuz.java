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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
@AutoRegister(name = "entity_soyuz", trackingRange = 1000)
public class EntitySoyuz extends Entity {

	public static final DataParameter<Integer> SKIN = EntityDataManager.createKey(EntitySoyuz.class, DataSerializers.VARINT);
	
	double acceleration = 0.00D;
	public int mode;
	public int targetX;
	public int targetZ;
	boolean memed = false;

	private ItemStack[] payload;
	
	public EntitySoyuz(World worldIn) {
		super(worldIn);
		this.ignoreFrustumCheck = true;
        this.setSize(5.0F, 50.0F);
        payload = new ItemStack[18];
	}
	
	@Override
	public void onUpdate() {
		if(motionY < 2.0D) {
			acceleration += 0.00025D;
			motionY += acceleration;
		}
		
		this.setLocationAndAngles(posX + this.motionX, posY + this.motionY, posZ + this.motionZ, 0, 0);
		
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
		
		if(this.posY > 600) {
			deployPayload();
		}
	}
	
	private void spawnExhaust(double x, double y, double z) {
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("count", 1);
		data.setDouble("width", world.rand.nextDouble() * 0.25 - 0.5);
		
		MainRegistry.proxy.effectNT(HbmEffectNT.Exhaust_Soyuz, x, y, z, data);
	}
	
	private void deployPayload() {
		if(mode == 0 && payload != null) {
			if(payload[0] != null) {
				
				ItemStack load = payload[0];
				
				if(load.getItem() == ModItems.flame_pony) {
					ExplosionLarge.spawnTracers(world, posX, posY, posZ, 25);
					for(EntityPlayer p : world.playerEntities)
						AdvancementManager.grantAchievement(p, AdvancementManager.achSpace);
				}
				
				if(load.getItem() == ModItems.sat_foeq) {
					for(EntityPlayer p : world.playerEntities)
						AdvancementManager.grantAchievement(p, AdvancementManager.achFOEQ);
				}
				
				if(load.getItem() instanceof ISatChip) {
				    int freq = ISatChip.getFreqS(load);
			    	Satellite.orbit(world, Satellite.getIDFromItem(load.getItem()), freq, posX, posY, posZ);
				}
			}
		}
		
		if(mode == 1) {
			
			EntitySoyuzCapsule capsule = new EntitySoyuzCapsule(world);
			capsule.payload = this.payload;
			capsule.soyuz = this.getSkin();
			capsule.setPosition(targetX + 0.5, 600, targetZ + 0.5);
			
			IChunkProvider provider = world.getChunkProvider();
			provider.provideChunk(targetX >> 4, targetZ >> 4);
			
			world.spawnEntity(capsule);
		}
		
		this.setDead();
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(SKIN, 0);
	}
	
	public void setSat(ItemStack stack) {
		this.payload[0] = stack;
	}
	
	public void setPayload(List<ItemStack> payload) {
		
		for(int i = 0; i < payload.size(); i++) {
			this.payload[i] = payload.get(i);
		}
	}
	
	public void setSkin(int i) {
		this.getDataManager().set(SKIN, i);
	}
	
	public int getSkin() {
		return this.getDataManager().get(SKIN);
	}
	
    @Override
	@SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 500000;
    }

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("items", 10);
		
		this.setSkin(nbt.getInteger("skin"));
		targetX = nbt.getInteger("targetX");
		targetZ = nbt.getInteger("targetZ");
		mode = nbt.getInteger("mode");

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if (b0 >= 0 && b0 < payload.length) {
				payload[b0] = new ItemStack(nbt1);
			}
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();

		nbt.setInteger("skin", this.getSkin());
		nbt.setInteger("targetX", targetX);
		nbt.setInteger("targetZ", targetZ);
		nbt.setInteger("mode", mode);

		for (int i = 0; i < payload.length; i++) {
			if (payload[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte) i);
				payload[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);
	}

}
