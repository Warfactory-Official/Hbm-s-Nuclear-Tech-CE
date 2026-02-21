package com.hbm.tileentity.bomb;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerNukeBalefire;
import com.hbm.inventory.gui.GUINukeBalefire;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.ModContext;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@AutoRegister
public class TileEntityNukeBalefire extends TileEntityMachineBase implements ITickable, IGUIProvider {

	public boolean loaded;
	public boolean started;
	public int timer;
	public UUID placerID = null;

	public TileEntityNukeBalefire() {
		super(2);
		timer = 18000;
	}

	@Override
	public String getDefaultName() {
		return "container.nukeFstbmb";
	}

	@Override
	public void update() {
		if(!world.isRemote) {

			if(!this.isLoaded()) {
				started = false;
			}

			if(started) {
				timer--;

				if(timer % 20 == 0)
					world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.fstbmbPing, SoundCategory.BLOCKS, 5.0F, 1.0F);
			}

			if(timer <= 0) {
				explode();
			}

			networkPackNT(250);
		}
	}
	
	public void handleButtonPacket(int value, int meta) {

		if(meta == 0 && this.isLoaded()) {
			world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.fstbmbStart, SoundCategory.BLOCKS, 5.0F, 1.0F);
			started = true;
		}

		if(meta == 1)
			timer = value * 20;
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(timer);
		buf.writeBoolean(this.isLoaded());
		buf.writeBoolean(started);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		timer = buf.readInt();
		loaded = buf.readBoolean();
		started = buf.readBoolean();
	}

	@Override
	public boolean isLoaded() {
		return super.isLoaded() && hasEgg() && hasBattery();
	}

	public boolean hasEgg() {
        return inventory.getStackInSlot(0).getItem() == ModItems.egg_balefire;
    }

	public boolean hasBattery() {

		return getBattery() > 0;
	}

	public int getBattery() {
		ItemStack stackInSlot = inventory.getStackInSlot(1);
		if(stackInSlot.getItem() == ModItems.battery_spark) return 1;
		if(stackInSlot.getItem() == ModItems.battery_trixite) return 2;

		return 0;
	}

	public void explode() {
		for(int i = 0; i < inventory.getSlots(); i++)
			inventory.setStackInSlot(i, ItemStack.EMPTY);

		world.destroyBlock(pos, false);

		EntityBalefire bf = new EntityBalefire(world);
		bf.posX = pos.getX() + 0.5;
		bf.posY = pos.getY() + 0.5;
		bf.posZ = pos.getZ() + 0.5;
		bf.destructionRange = (int) 250;
		if (ModContext.DETONATOR_CONTEXT.get() == null){
			bf.detonator = placerID;
		} else bf.setDetonator(ModContext.DETONATOR_CONTEXT.get());
		world.spawnEntity(bf);
		if(BombConfig.enableNukeClouds) {
			EntityNukeTorex.statFacBale(world, pos.getX() + 0.5, pos.getY() + 5, pos.getZ() + 0.5, 250F);
		}
	}

	public String getMinutes() {

		String mins = "" + (timer / 1200);

		if(mins.length() == 1)
			mins = "0" + mins;

		return mins;
	}

	public String getSeconds() {

		String mins = "" + ((timer / 20) % 60);

		if(mins.length() == 1)
			mins = "0" + mins;

		return mins;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		started = compound.getBoolean("started");
		timer = compound.getInteger("timer");
		if (compound.hasKey("placer"))
			placerID = compound.getUniqueId("placer");
		super.readFromNBT(compound);
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("started", started);
		compound.setInteger("timer", timer);
		if (placerID != null)
			compound.setUniqueId("placer", placerID);
		return super.writeToNBT(compound);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerNukeBalefire(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUINukeBalefire(player.inventory, this);
	}
}