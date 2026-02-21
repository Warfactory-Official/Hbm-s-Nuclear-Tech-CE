package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@AutoRegister
public class TileEntityCharger extends TileEntityLoadedBase implements IBufPacketReceiver, ITickable, IEnergyReceiverMK2 {

	private List<EntityPlayer> players = new ArrayList<>();
	private long charge = 0;
	private int lastOp = 0;

	boolean particles = false;

	public int usingTicks;
	public int lastUsingTicks;
	public static final int delay = 20;

	@Override
	public void update() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();

		if (!world.isRemote) {
			this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ, dir);

			this.players = world.getEntitiesWithinAABB(
					EntityPlayer.class,
					new AxisAlignedBB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
							.grow(0.5, 0.0, 0.5)
			);

			charge = 0; // required

			for(EntityPlayer player : players) {
				InventoryPlayer inv = player.inventory;
				for(int i = 0; i < inv.getSizeInventory(); i ++){

					ItemStack stack = inv.getStackInSlot(i);

					if(Library.isBattery(stack)) {
						if (stack.getItem() instanceof IBatteryItem battery) {
							charge += Math.min(battery.getMaxCharge(stack) - battery.getCharge(stack), battery.getChargeRate(stack));
						} else {
							IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
							if (cap != null && GeneralConfig.conversionRateHeToRF > 0) {
								long maxHe = (long) (cap.getMaxEnergyStored() / GeneralConfig.conversionRateHeToRF);
								long currentHe = (long) (cap.getEnergyStored() / GeneralConfig.conversionRateHeToRF);
								charge += maxHe - currentHe;
							}
						}
					}
				}
			}

			particles = lastOp > 0;

			if (particles) {

				lastOp--;

				if (world.getTotalWorldTime() % 20 == 0)
					world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.2F, 0.5F);
			}

			networkPackNT(50);
		}

		lastUsingTicks = usingTicks;

		if ((charge > 0 || particles) && usingTicks < delay) {
			usingTicks++;
			if (usingTicks == 2)
				world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, 0.5F);
		}
		if ((charge <= 0 && !particles) && usingTicks > 0) {
			usingTicks--;
			if (usingTicks == 4)
				world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, 0.5F);
		}

		if (world.isRemote && particles) {
			Random rand = world.rand;
			world.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
					pos.getX() + 0.5 + rand.nextDouble() * 0.0625 + dir.offsetX * 0.75,
					pos.getY() + 0.1,
					pos.getZ() + 0.5 + rand.nextDouble() * 0.0625 + dir.offsetZ * 0.75,
					-dir.offsetX + rand.nextGaussian() * 0.1,
					0,
					-dir.offsetZ + rand.nextGaussian() * 0.1);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(this.charge);
		buf.writeBoolean(this.particles);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.charge = buf.readLong();
		this.particles = buf.readBoolean();
	}

	@Override
	public long getPower() {
		return 0;
	}

	@Override
	public long getMaxPower() {
		return charge;
	}

	@Override
	public void setPower(long power) { }

	@Override
	public long transferPower(long power, boolean simulate) {
		if(power == 0) return 0;
		long powerBudget = power;
		for(EntityPlayer player : players) {
			InventoryPlayer inv = player.inventory;
			for(int i = 0; i < inv.getSizeInventory(); i ++){
				if(powerBudget <= 0) break;
				ItemStack stack = inv.getStackInSlot(i);
				if(Library.isChargeableBattery(stack)) {
					long powerToOffer = powerBudget;
					long chargedAmount = Library.chargeBatteryIfValid(stack, powerToOffer, false);
					if (chargedAmount > 0) {
						powerBudget -= chargedAmount;
						lastOp = 4;
					}
				}
			}
			if(powerBudget <= 0) {
				break;
			}
		}
		return powerBudget;
	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}
}