package com.hbm.entity.projectile;

import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.ParticleBurstPacket;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
@AutoRegister(name = "entity_rubble", trackingRange = 1000)
public class EntityRubble extends EntityThrowableNT {
	
	public static final DataParameter<Integer> BLOCKID = EntityDataManager.createKey(EntityRubble.class, DataSerializers.VARINT);
	public static final DataParameter<Integer> BLOCKMETA = EntityDataManager.createKey(EntityRubble.class, DataSerializers.VARINT);
	
	public EntityRubble(World world)
    {
        super(world);
    }

    public EntityRubble(World world, EntityLivingBase entity)
    {
        super(world, entity);
    }

    @Override
	public void entityInit() {
        this.dataManager.register(BLOCKID, 0);
        this.dataManager.register(BLOCKMETA, 0);
    }

    public EntityRubble(World world, double x, double y, double z)
    {
        super(world, x, y, z);
    }

    @Override
	protected void onImpact(RayTraceResult result)
    {
        if (result.entityHit != null)
        {
            byte b0 = 15;

            result.entityHit.attackEntityFrom(ModDamageSource.rubble, b0);
        }

        if(this.ticksExisted > 2) {
        	this.setDead();
        	
    		world.playSound(this.posX, this.posY, this.posZ, HBMSoundHandler.blockDebris, SoundCategory.BLOCKS, 1.5F, 1.0F, true);
            //worldObj.playAuxSFX(2001, (int)posX, (int)posY, (int)posZ, this.dataWatcher.getWatchableObjectInt(16) + (this.dataWatcher.getWatchableObjectInt(17) << 12));
    		if(!world.isRemote)
    			PacketDispatcher.wrapper.sendToAll(new ParticleBurstPacket((int)posX - 1, (int)posY, (int)posZ - 1, this.dataManager.get(BLOCKID), this.dataManager.get(BLOCKMETA)));
        }
    }

    @Override
    protected float getAirDrag() {
        return 1F;
    }

    public void setMetaBasedOnBlock(Block b, int i) {
    	this.dataManager.set(BLOCKID, Block.getIdFromBlock(b));
    	this.dataManager.set(BLOCKMETA, i);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.dataManager.set(BLOCKID, nbt.getInteger("block"));
        this.dataManager.set(BLOCKMETA, nbt.getInteger("meta"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("block", this.dataManager.get(BLOCKID));
        nbt.setInteger("meta", this.dataManager.get(BLOCKMETA));
    }
}
