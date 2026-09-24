package com.hbm.entity.missile;

import com.hbm.items.ISatChip;
import com.hbm.saveddata.satellites.Satellite;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public abstract class EntityRocketBase extends Entity {

	protected ItemStackHandler payload;
	protected double acceleration = 0.00D;

	public EntityRocketBase(World world, int payloadCapacity) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.payload = new ItemStackHandler(payloadCapacity);
	}

	@Override
	protected void entityInit() { }

	@Override
	public void onUpdate() {

		if(motionY < 2.0D) {
			acceleration += 0.00025D;
			motionY += acceleration;
		}

		this.prevPosX = this.lastTickPosX = this.posX;
		this.prevPosY = this.lastTickPosY = this.posY;
		this.prevPosZ = this.lastTickPosZ = this.posZ;

		this.setPosition(posX + this.motionX, posY + this.motionY, posZ + this.motionZ);

		if(this.posY > 600) {
			deployPayload();
			this.setDead();
		}
	}

	public void setSat(ItemStack stack) {
		if(this.payload.getSlots() > 0) this.payload.setStackInSlot(0, stack == null ? ItemStack.EMPTY : stack);
	}

	public void setPayload(List<ItemStack> payload) {

		for(int i = 0; i < payload.size() && i < this.payload.getSlots(); i++) {
			ItemStack stack = payload.get(i);
			this.payload.setStackInSlot(i, stack == null ? ItemStack.EMPTY : stack);
		}
	}

	protected void deployPayload() {

		ItemStack load = this.payload.getSlots() > 0 ? this.payload.getStackInSlot(0) : ItemStack.EMPTY;

		if(load.getItem() instanceof ISatChip) {
			int freq = ISatChip.getFreqS(load);
			Satellite.orbit(world, Satellite.getIDFromStack(load), load, freq, posX, posY, posZ);
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		int slots = this.payload.getSlots();
		this.payload = new ItemStackHandler(slots);
		if(nbt.hasKey("payload")) this.payload.deserializeNBT(nbt.getCompoundTag("payload"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setTag("payload", this.payload.serializeNBT());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 500000;
	}
}
