package com.hbm.saveddata.satellites;

import com.hbm.inventory.recipes.GenericRecipeNoPower;
import com.hbm.inventory.recipes.SpaceAssemblerRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemDrive.EnumDriveType;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import com.hbm.util.BobMathUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class SatelliteScience extends Satellite {

	public static final int COOLDOWN = 15 * 60 * 20;
	public long lastScience;

	public static final int SENSOR_DURATION = 100 * 60 * 60 * 20;
	public int sensorProgress;
	public int sensorCount;

	public int assemblerCount;
	public double assemblerProgress;

	// FIFO
	public List<AssemblerTask> assemblerTasks = new ArrayList<>();

	@Override
	public String getType() {
		return "SCIENCE_PROBE";
	}

	@Override
	public boolean hasData(World world) {
		if(super.hasData(world)) return true;

		if(world.getTotalWorldTime() > this.lastScience + COOLDOWN) {
			this.produceData(EnumDriveType.DISK_EMPTY, EnumDriveType.DISK_FLIGHTDATA);
			this.lastScience = world.getTotalWorldTime();
			this.markDirty();
		}

		return super.hasData(world);
	}

	@Override
	public void onPartDelivered(World world, ItemStack part) {

		if(part.isEmpty()) return;

		if(part.getItem() == ModItems.satellite) {

			if(part.getItemDamage() == EnumSatType.SCIENCE_SENSOR.ordinal()) this.sensorCount++;
			if(part.getItemDamage() == EnumSatType.SCIENCE_ASSEMBLER.ordinal()) this.assemblerCount++;
			this.markDirty();
			return;
		}

		GenericRecipeNoPower recipe = SpaceAssemblerRecipes.INSTANCE.getRecipe(part);

		if(recipe != null) {
			this.assemblerTasks.add(new AssemblerTask(recipe));
			this.markDirty();
		}
	}

	@Override
	public void onUpdateTick(World world) {

		if(this.sensorProgress < SENSOR_DURATION) {
			if(this.sensorCount > 0) {
				this.sensorProgress += this.sensorCount;
				this.markDirty();
			}
		} else {
			this.sensorProgress = 0;
			this.produceData(EnumDriveType.DISK_EMPTY, EnumDriveType.DISK_ORBITDATA);
			this.markDirty();
		}

		if(this.assemblerCount > 0 && this.requestableSlots.getSlots() <= 0 && !this.assemblerTasks.isEmpty()) {

			AssemblerTask task = this.assemblerTasks.get(0);
			this.assemblerProgress += (double) this.assemblerCount / task.duration;

			if(this.assemblerProgress >= 1) {

				GenericRecipeNoPower recipe = SpaceAssemblerRecipes.INSTANCE.recipeNameMap.get(task.recipe);

				if(recipe != null) {
					this.requestableSlots = new ItemStackHandler(recipe.outputItem.length);
					for(int i = 0; i < recipe.outputItem.length; i++) {
						ItemStack stack = recipe.outputItem[i].collapse();
						if(stack != null && !stack.isEmpty()) this.requestableSlots.setStackInSlot(i, stack);
					}
				}

				this.assemblerTasks.remove(0);
				this.assemblerProgress = 0;
				this.markDirty();
			}
		}
	}

	@Override
	public ITextComponent[] getInfo(World world) {

		int cooldown = (int) ((lastScience + COOLDOWN) - world.getTotalWorldTime());
		int seconds = cooldown / 20;

		List<ITextComponent> info = new ArrayList<>();
		info.add(new TextComponentTranslation(ItemSatellite.make(EnumSatType.SCIENCE).getTranslationKey() + ".name"));
		info.add(cooldown <= 0 ? new TextComponentTranslation("satellite.ready") : new TextComponentTranslation("satellite.cooldown", (seconds / 60) + "m" + (seconds % 60) + "s"));
		if(this.sensorCount > 0) {
			info.add(new TextComponentTranslation("satellite.sensors", this.sensorCount));
			info.add(new TextComponentTranslation("satellite.pending", BobMathUtil.getShortNumber(SENSOR_DURATION - sensorProgress)));
		}
		if(this.driveOutput == EnumDriveType.DISK_ORBITDATA) info.add(new TextComponentTranslation("satellite.data"));
		if(this.assemblerCount > 0) {
			info.add(new TextComponentTranslation("satellite.assemblers", this.assemblerCount));
			info.add(new TextComponentTranslation("satellite.progress", (int) Math.round(this.assemblerProgress * 100) + "%"));
			info.add(new TextComponentTranslation("satellite.queue", this.assemblerTasks.size()));
		}

		return info.toArray(new ITextComponent[0]);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("lastScience", lastScience);
		nbt.setInteger("sensorProgress", sensorProgress);
		nbt.setInteger("sensorCount", sensorCount);
		nbt.setInteger("assemblerCount", assemblerCount);
		nbt.setDouble("assemblerProgress", assemblerProgress);

		nbt.setInteger("taskCount", this.assemblerTasks.size());
		for(int i = 0; i < this.assemblerTasks.size(); i++) {
			nbt.setString("task" + i, this.assemblerTasks.get(i).recipe);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		lastScience = nbt.getLong("lastScience");
		sensorProgress = nbt.getInteger("sensorProgress");
		sensorCount = nbt.getInteger("sensorCount");
		assemblerCount = nbt.getInteger("assemblerCount");
		assemblerProgress = nbt.getDouble("assemblerProgress");

		this.assemblerTasks.clear();
		int taskCount = nbt.getInteger("taskCount");

		for(int i = 0; i < taskCount; i++) {
			this.assemblerTasks.add(new AssemblerTask(nbt.getString("task" + i)));
		}
	}

	public static class AssemblerTask {

		public String recipe;
		public int duration;
		public ItemStack icon;

		public AssemblerTask(GenericRecipeNoPower recipe) {

			if(recipe != null) {
				this.recipe = recipe.getInternalName();
				this.duration = recipe.duration;
				this.icon = recipe.getIcon();
			} else {
				this.recipe = "null";
				this.duration = 1;
				this.icon = ItemStack.EMPTY;
			}
		}

		public AssemblerTask(String name) {
			this(SpaceAssemblerRecipes.INSTANCE.recipeNameMap.get(name));
		}
	}

	@Override
	public float[] getColor() {
		return new float[] { 1.0F, 1.0F, 0.4F };
	}
}
