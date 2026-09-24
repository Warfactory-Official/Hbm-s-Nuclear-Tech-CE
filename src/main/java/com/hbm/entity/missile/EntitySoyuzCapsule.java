package com.hbm.entity.missile;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntitySoyuzCapsule;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

@AutoRegister(name = "entity_soyuz_capsule", trackingRange = 1000)
public class EntitySoyuzCapsule extends Entity {

	public int soyuz;
	public ItemStackHandler payload = new ItemStackHandler(18);

	public EntitySoyuzCapsule(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
	}

	@Override
	protected void entityInit() { }

	@Override
	public void onUpdate() {

		if(this.motionY > -0.2) this.motionY -= 0.02;
		if(posY > 600) posY = 600;

		this.prevPosX = this.lastTickPosX = this.posX;
		this.prevPosY = this.lastTickPosY = this.posY;
		this.prevPosZ = this.lastTickPosZ = this.posZ;

		this.setPosition(posX + this.motionX, posY + this.motionY, posZ + this.motionZ);

		BlockPos impact = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ));

		if(this.world.getBlockState(impact).getBlock() != Blocks.AIR) {

			this.setDead();

			if(!world.isRemote) {

				BlockPos place = impact.up();
				world.setBlockState(place, ModBlocks.soyuz_capsule.getDefaultState());

				if(world.getTileEntity(place) instanceof TileEntitySoyuzCapsule capsule) {

					for(int i = 0; i < payload.getSlots(); i++) {
						capsule.inventory.setStackInSlot(i, payload.getStackInSlot(i));
					}

					capsule.inventory.setStackInSlot(18, new ItemStack(ModItems.missile_soyuz, 1, soyuz));
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance < 500000;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		soyuz = nbt.getInteger("soyuz");
		this.payload = new ItemStackHandler(18);
		if(nbt.hasKey("payload")) this.payload.deserializeNBT(nbt.getCompoundTag("payload"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("soyuz", soyuz);
		nbt.setTag("payload", this.payload.serializeNBT());
	}
}
