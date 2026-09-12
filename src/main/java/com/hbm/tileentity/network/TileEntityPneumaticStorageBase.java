package com.hbm.tileentity.network;

import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.api.ntl.IPneumaticConnector;
import com.hbm.api.ntl.ISlotMonitorProvider;
import com.hbm.api.ntl.SlotMonitor;
import com.hbm.api.ntl.StackCache;
import com.hbm.api.ntl.StackCache.CacheSlot;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PneumaticNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityPneumaticStorageBase extends TileEntityMachineBase
		implements ITickable, IPneumaticConnector, IFluidStandardReceiverMK2, ISlotMonitorProvider, IControlReceiver, IGUIProvider {

	public FluidTankNTM compair;
	public SlotMonitor[] monitors;

	protected TileEntityPneumoTube.PneumaticNode node;
	protected boolean wasAvailable = false;

	public TileEntityPneumaticStorageBase(int slots) {
		super(slots);
		this.compair = new FluidTankNTM(Fluids.AIR, 4_000).withOwner(this).withPressure(1);
		this.monitors = new SlotMonitor[slots];

		for(int i = 0; i < monitors.length; i++) this.monitors[i] = new SlotMonitor(i, this);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return player.getDistanceSq(pos) < 100;
	}

	@Override
	public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {

		if(data.hasKey("pressure")) {
			int pressure = this.compair.getPressure() + 1;
			if(pressure > 5) pressure = 1;
			this.compair.setTankType(Fluids.AIR);
			this.compair.withPressure(pressure);
			for(SlotMonitor monitor : this.monitors) monitor.availabilityHasChanged();
		}
	}

	@Override
	public void update() {

		if(!world.isRemote) {

			boolean isAvailable = this.isAvailable();

			if(isAvailable != wasAvailable) {
				this.wasAvailable = isAvailable;
				for(SlotMonitor monitor : monitors) monitor.availabilityHasChanged();
			}

			if(this.node == null || this.node.expired) {
				this.node = UniNodespace.getNode(world, pos, PneumaticNetwork.THE_PNEUMATIC_PROVIDER);

				if(this.node == null || this.node.expired) {
					this.node = new TileEntityPneumoTube.PneumaticNode(new BlockPos(pos.getX(), pos.getY(), pos.getZ())).setConnections(
							new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
							new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
							new DirPos(pos.getX(), pos.getY() + 1, pos.getZ(), Library.POS_Y),
							new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
							new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
							new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)
					);
					UniNodespace.createNode(world, this.node);
				}
			}

			if(node != null && !node.expired && node.hasValidNet()) {
				this.node.net.storages.put(this, System.currentTimeMillis());
			}

			if(world.getTotalWorldTime() % 10 == 0) for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				this.trySubscribe(compair.getTankType(), world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
			}

			if(this.compair.getFill() > 0) {
				int consumption = (int) Math.ceil(this.compair.getFill() * 9D / this.compair.getMaxFill()) + 1;
				this.compair.setFill(Math.max(this.compair.getFill() - consumption, 0));
			}

			this.updateMonitors();
			this.networkPackNT(15);
		}
	}

	public boolean isAvailable() {
		return this.isLoaded && !this.isInvalid() && this.compair.getFill() > 0;
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(world != null && !world.isRemote) {

			for(SlotMonitor monitor : this.monitors) {
				for(CacheSlot cache : monitor.viewedBy) cache.removeMonitor(monitor);
			}

			if(this.node != null) {

				if(node.hasValidNet()) {
					this.node.net.storages.remove(this);
				}

				UniNodespace.destroyNode(world, pos, PneumaticNetwork.THE_PNEUMATIC_PROVIDER);
			}
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		for(SlotMonitor monitor : this.monitors) {
			for(CacheSlot cache : monitor.viewedBy) cache.removeMonitor(monitor);
		}

		if(node != null && node.hasValidNet()) {
			this.node.net.storages.remove(this);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		compair.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		compair.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.compair.readFromNBT(nbt, "tank");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		this.compair.writeToNBT(nbt, "tank");
		return super.writeToNBT(nbt);
	}

	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }

	@Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {compair}; }
	@Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {compair}; }

	@Override public SlotMonitor[] getMonitors() { return monitors; }
	@Override public ItemStack getSlotAt(int index) { return this.inventory.getStackInSlot(index); }

	@Override
	public PneumaticNetwork getRelevantNetwork() {
		if(this.node == null || this.node.expired || !this.node.hasValidNet()) return null;
		return this.node.net;
	}

	@Override
	public boolean isAvailableToCache(StackCache cache) {
		if(!isAvailable()) return false;
		int range = TileEntityPneumoTube.getRangeFromPressure(this.compair.getPressure());
		int dX = pos.getX() - cache.x;
		int dY = pos.getY() - cache.y;
		int dZ = pos.getZ() - cache.z;
		return dX * dX + dY * dY + dZ * dZ <= range * range;
	}
}
