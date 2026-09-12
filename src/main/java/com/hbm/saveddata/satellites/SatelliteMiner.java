package com.hbm.saveddata.satellites;

import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsSatellite;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import com.hbm.util.WeightedRandomObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;

public class SatelliteMiner extends Satellite {

	/**
	 * {@link WeightedRandomObject} array with loot the satellite will deliver.
	 */
	private static final HashMap<Class<? extends SatelliteMiner>, String> CARGO = new HashMap<>();

	public double progress;
	public static final double SPEED = 1D / (15 * 60 * 20); // 15 minutes

	public SatelliteMiner() {
		this.satIface = Interfaces.NONE;
	}

	@Override public String getType() { return "ASTEROID_MINER"; }

	@Override
	public ITextComponent[] getInfo(World world) {
		return new ITextComponent[] {
				new TextComponentTranslation(ItemSatellite.make(EnumSatType.MINER_ASTRO).getTranslationKey() + ".name"),
				new TextComponentTranslation("satellite.minerprogress", (int) Math.round(this.progress * 100) + "%")
		};
	}

	@Override
	public void onUpdateTick(World world) {

		if(this.requestableSlots.getSlots() <= 0) {
			this.progress += SPEED;

			if(this.progress >= 1D) {
				this.progress = 0D;

				WeightedRandomChestContentFrom1710[] pool = ItemPool.getPool(getCargo());

				int itemAmount = 10 + world.rand.nextInt(6); // 10-15
				this.requestableSlots = new ItemStackHandler(itemAmount);

				for(int i = 0; i < itemAmount; i++) {
					ItemStack stack = ItemPool.getStack(pool, world.rand);
					if(stack != null && !stack.isEmpty()) this.requestableSlots.setStackInSlot(i, stack);
				}

				this.markDirty();
			}

			if(world.getTotalWorldTime() % 1200 == 0) this.markDirty();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setDouble("progress", progress);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.progress = nbt.getDouble("progress");
	}

	/**
	 * Replaces cargo of the satellite.
	 * @param cargo - Array of {@link WeightedRandomObject} representing the loot that will be delivered.
	 */
	public static void registerCargo(Class<? extends SatelliteMiner> minerSatelliteClass, String cargo) {
		CARGO.put(minerSatelliteClass, cargo);
	}

	/**
	 * Gets items the satellite can deliver.
	 * @return - Array of {@link WeightedRandomObject} of satellite loot.
	 */
	public String getCargo() {
		return CARGO.get(getClass());
	}

	static {
		registerCargo(SatelliteMiner.class, ItemPoolsSatellite.POOL_SAT_MINER);
	}

	@Override
	public float[] getColor() {
		return new float[] { 0.0F, 0.0F, 0.0F };
	}
}
