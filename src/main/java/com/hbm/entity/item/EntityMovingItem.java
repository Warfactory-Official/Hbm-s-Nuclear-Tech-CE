package com.hbm.entity.item;

import com.hbm.api.conveyor.IConveyorItem;
import com.hbm.api.conveyor.IEnterableBlock;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
@AutoRegister(name = "entity_c_item", trackingRange = 1000)
public class EntityMovingItem extends EntityMovingConveyorObject implements IConveyorItem {

	public static final DataParameter<ItemStack> STACK = EntityDataManager.createKey(EntityMovingItem.class, DataSerializers.ITEM_STACK);

	public EntityMovingItem(World p_i1582_1_) {
		super(p_i1582_1_);
		this.setSize(0.375F, 0.375F);
	}

    public void setItemStack(ItemStack stack) {
        this.getDataManager().set(STACK, stack);
    }

    public ItemStack getItemStack() {

        ItemStack stack = this.getDataManager().get(STACK);
        return stack == null ? new ItemStack(Blocks.STONE) : stack;
    }

    public boolean canBeCollidedWith() {
        return true;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {

    	if(!world.isRemote) {
			world.spawnEntity(new EntityItem(world, posX, posY, posZ, this.getItemStack()));
	    	this.setDead();
    	}
    	return true;
    }

    private int schedule = 0;

	@Override
    protected void entityInit() {
        this.getDataManager().register(STACK, ItemStack.EMPTY);
    }

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {

        NBTTagCompound compound = nbt.getCompoundTag("Item");
        this.setItemStack(new ItemStack(compound));

        ItemStack stack = this.getDataManager().get(STACK);

        schedule = nbt.getInteger("schedule");

        if (stack == null || stack.isEmpty())
            this.setDead();
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {

        if (!this.getItemStack().isEmpty())
        	nbt.setTag("Item", this.getItemStack().writeToNBT(new NBTTagCompound()));

        nbt.setInteger("schedule", schedule);
	}

	@Override
	public void enterBlock(IEnterableBlock enterable, BlockPos pos, EnumFacing dir) {

		if(enterable.canItemEnter(world, pos.getX(), pos.getY(), pos.getZ(), dir, this)) {
			enterable.onItemEnter(world, pos.getX(), pos.getY(), pos.getZ(), dir, this);
			this.setDead();
		}
	}

	@Override
	public boolean onLeaveConveyor() {

		this.setDead();
		EntityItem item = new EntityItem(world, posX + motionX * 2, posY + motionY * 2, posZ + motionZ * 2, this.getItemStack());
		item.motionX = this.motionX * 2;
		item.motionY = 0.1;
		item.motionZ = this.motionZ * 2;
		item.velocityChanged = true;
		world.spawnEntity(item);

		return true;
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target){
		if(target.entityHit != null && target.entityHit instanceof EntityMovingItem)
			return ((EntityMovingItem)target.entityHit).getItemStack();
		return null;
	}
}