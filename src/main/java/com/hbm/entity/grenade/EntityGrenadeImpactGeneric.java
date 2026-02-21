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
@AutoRegister(name = "entity_grenade_impact_generic")
public class EntityGrenadeImpactGeneric extends EntityGrenadeBase implements IGenericGrenade {

    private static final DataParameter<Integer> GRENADE_TYPE = EntityDataManager.createKey(EntityGrenadeImpactGeneric.class, DataSerializers.VARINT);

    public EntityGrenadeImpactGeneric(World worldIn) {
        super(worldIn);
    }

    public EntityGrenadeImpactGeneric(World worldIn, EntityLivingBase throwerIn, EnumHand hand) {
        super(worldIn, throwerIn, hand);
    }

    public EntityGrenadeImpactGeneric setType(ItemGenericGrenade grenade) {
        this.dataManager.set(GRENADE_TYPE, Item.getIdFromItem(grenade));
        return this;
    }

    @Override
    public ItemGenericGrenade getGrenade() {
        ItemGenericGrenade grenade = (ItemGenericGrenade) Item.getItemById(this.dataManager.get(GRENADE_TYPE));
        return grenade != null ? grenade : (ItemGenericGrenade) ModItems.stick_dynamite_fishing;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(GRENADE_TYPE, 0);
    }

    @Override
    public void explode() {
        if (!this.world.isRemote && getGrenade() != null) {
            getGrenade().explode(this, this.getThrower(), this.world, this.posX, this.posY, this.posZ);
            this.setDead();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("grenade", this.dataManager.get(GRENADE_TYPE));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(GRENADE_TYPE, compound.getInteger("grenade"));
    }
}
