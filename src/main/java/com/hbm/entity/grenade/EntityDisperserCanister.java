package com.hbm.entity.grenade;

import com.hbm.entity.effect.EntityMist;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.render.entity.RenderMetaSensitiveItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

@AutoRegister(name = "entity_disperser")
public class EntityDisperserCanister extends EntityGrenadeBase implements RenderMetaSensitiveItem.IHasMetaSensitiveRenderer<EntityDisperserCanister> {
    private static final DataParameter<Integer> FLUID_ID = EntityDataManager.createKey(EntityDisperserCanister.class, DataSerializers.VARINT);
    // mlbv: item ids are variable, switch to String of the item's ResourceLocation if it turns out to be problem later (1.7 used id so i copied it)
    private static final DataParameter<Integer> TYPE_ID = EntityDataManager.createKey(EntityDisperserCanister.class, DataSerializers.VARINT);

    public EntityDisperserCanister(World world) {
        super(world);
    }

    public EntityDisperserCanister(World world, EntityLivingBase living, EnumHand hand) {
        super(world, living, hand);
    }

    public FluidType getFluid() {
        return Fluids.fromID(this.dataManager.get(FLUID_ID));
    }

    public EntityDisperserCanister setFluid(int id) {
        this.dataManager.set(FLUID_ID, id);
        return this;
    }

    public Item getType() {
        return Item.getItemById(this.dataManager.get(TYPE_ID));
    }

    public EntityDisperserCanister setType(int id) {
        this.dataManager.set(TYPE_ID, id);
        return this;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLUID_ID, 0);
        this.dataManager.register(TYPE_ID, 0);
    }

    @Override
    public void explode() {
        if (!world.isRemote) {
            EntityMist mist = new EntityMist(world);
            mist.setType(getFluid());
            mist.setPosition(posX, posY, posZ);
            mist.setArea(10, 5);
            mist.setDuration(80);
            world.spawnEntity(mist);
            this.setDead();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("fluid", this.dataManager.get(FLUID_ID));
        nbt.setInteger("item", this.dataManager.get(TYPE_ID));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.dataManager.set(FLUID_ID, nbt.getInteger("fluid"));
        this.dataManager.set(TYPE_ID, nbt.getInteger("item"));
    }

    private ItemStack stackToRender;
    
    @Override
    public ItemStack getStackToRender(EntityDisperserCanister entityIn) {
        if(stackToRender == null) {
            stackToRender = new ItemStack(getType(), 1, dataManager.get(FLUID_ID));
        }
        return stackToRender;
    }
}
