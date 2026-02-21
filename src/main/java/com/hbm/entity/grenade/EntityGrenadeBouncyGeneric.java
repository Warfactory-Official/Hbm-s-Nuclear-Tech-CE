package com.hbm.entity.grenade;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemGenericGrenade;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

@AutoRegister(name = "entity_grenade_bouncy_generic")
public class EntityGrenadeBouncyGeneric extends EntityGrenadeBouncyBase implements IGenericGrenade {

    private static final DataParameter<Integer> GRENADE_TYPE = EntityDataManager.createKey(EntityGrenadeBouncyGeneric.class, DataSerializers.VARINT);

    public EntityGrenadeBouncyGeneric(World worldIn) {
        super(worldIn);
    }

    public EntityGrenadeBouncyGeneric(World worldIn, EntityLivingBase throwerIn, EnumHand hand) {
        super(worldIn, throwerIn, hand);
    }

    public EntityGrenadeBouncyGeneric setType(ItemGenericGrenade grenade) {
        this.dataManager.set(GRENADE_TYPE, Item.getIdFromItem(grenade));
        return this;
    }

    @Override
    public ItemGenericGrenade getGrenade() {
        ItemGenericGrenade grenade = (ItemGenericGrenade) Item.getItemById(this.dataManager.get(GRENADE_TYPE));
        return grenade != null ? grenade : (ItemGenericGrenade) ModItems.grenade_kyiv;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(GRENADE_TYPE, 0);
    }

    @Override
    public void explode() {
        getGrenade().explode(this, this.getThrower(), this.world, this.posX, this.posY, this.posZ);
        this.setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("grenade", this.dataManager.get(GRENADE_TYPE));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(GRENADE_TYPE, compound.getInteger("grenade"));
    }

    @Override
    protected int getMaxTimer() {
        return getGrenade().getMaxTimer();
    }

    @Override
    protected double getBounceMod() {
        return getGrenade().getBounceMod();
    }
}
