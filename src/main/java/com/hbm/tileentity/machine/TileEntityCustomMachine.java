package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.api.tile.IHeatSource;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockCustomMachine;
import com.hbm.blocks.machine.ReactorResearch;
import com.hbm.config.CustomMachineConfigJSON;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration.ComponentDefinition;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMachineCustom;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineCustom;
import com.hbm.inventory.recipes.CustomMachineRecipes;
import com.hbm.inventory.recipes.CustomMachineRecipes.CustomMachineRecipe;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityProxyBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.Compat;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityCustomMachine extends TileEntityMachinePolluting implements IFluidStandardTransceiverMK2, IEnergyProviderMK2, IEnergyReceiverMK2, IGUIProvider, ITickable {

	public String machineType;
	public MachineConfiguration config;

	public long power;
	public int flux;
	public int heat;
	public int maxHeat;
	public int progress;
	public int maxProgress = 1;
	public FluidTankNTM[] inputTanks;
	public FluidTankNTM[] outputTanks;
	public ModulePatternMatcher matcher;
	public int structureCheckDelay;
	public boolean structureOK = false;
	public CustomMachineRecipe cachedRecipe;

	public List<DirPos> connectionPos = new ArrayList<>();
	public List<DirPos> fluxPos = new ArrayList<>();
	public List<DirPos> heatPos = new ArrayList<>();

	public TileEntityCustomMachine() {
		/*
		 * 0: Battery
		 * 1-3: Fluid IDs
		 * 4-9: Inputs
		 * 10-15: Template
		 * 16-21: Output
		 */
		super(22, 100, true, true);
	}

	public void init() {
		MachineConfiguration config = CustomMachineConfigJSON.customMachines.get(this.machineType);

		if (config != null) {
			this.config = config;

			inputTanks = new FluidTankNTM[config.fluidInCount];
			for (int i = 0; i < inputTanks.length; i++) inputTanks[i] = new FluidTankNTM(Fluids.NONE, config.fluidInCap);
			outputTanks = new FluidTankNTM[config.fluidOutCount];
			for (int i = 0; i < outputTanks.length; i++)
				outputTanks[i] = new FluidTankNTM(Fluids.NONE, config.fluidOutCap);
			maxHeat = config.maxHeat;
			matcher = new ModulePatternMatcher(config.itemInCount);
			smoke.changeTankSize(config.maxPollutionCap);
			smoke_leaded.changeTankSize(config.maxPollutionCap);
			smoke_poison.changeTankSize(config.maxPollutionCap);

		} else if (world != null) {
			world.destroyBlock(pos, false);
		}
	}

	@Override
	public String getName() {
		return config != null ? config.localizedName : "INVALID";
	}

	@Override
	public String getDefaultName() {
		return config != null ? config.localizedName : "INVALID";
	}

	protected ForgeDirection getDir() {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != ModBlocks.custom_machine) return ForgeDirection.NORTH;
		return ForgeDirection.getOrientation(state.getValue(BlockCustomMachine.FACING).getIndex());
	}

	@Override
	public void update() {

		if (!world.isRemote) {

			if (config == null) {
				world.destroyBlock(pos, false);
				return;
			}

			this.power = Library.chargeTEFromItems(inventory, 0, power, this.config.maxPower);

			if (this.inputTanks.length > 0) this.inputTanks[0].setType(1, inventory);
			if (this.inputTanks.length > 1) this.inputTanks[1].setType(2, inventory);
			if (this.inputTanks.length > 2) this.inputTanks[2].setType(3, inventory);

			this.structureCheckDelay--;
			if (this.structureCheckDelay <= 0) this.checkStructure();

			if (this.world.getTotalWorldTime() % 20 == 0) {
				for (DirPos pos : this.connectionPos) {
					for (FluidTankNTM tank : this.inputTanks) {
						this.trySubscribe(tank.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
					}
					if (!config.generatorMode)
						this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				}
				for (byte d = 2; d < 6; d++) {
					ForgeDirection dir = ForgeDirection.getOrientation(d);
					for (DirPos pos : this.fluxPos) {
						Block b = world.getBlockState(new BlockPos(pos.getPos().getX() + dir.offsetX, pos.getPos().getY(), pos.getPos().getZ() + dir.offsetZ)).getBlock();
						if (b == ModBlocks.reactor_research) {
							int[] source = ((ReactorResearch) ModBlocks.reactor_research).findCore(world, pos.getPos().getX() + dir.offsetX, pos.getPos().getY(), pos.getPos().getZ() + dir.offsetZ);
							if (source != null) {

								TileEntity tile = world.getTileEntity(new BlockPos(source[0], source[1], source[2]));

								if (tile instanceof TileEntityReactorResearch reactor) {
									this.flux = reactor.totalFlux;
								}
							}
						}
					}
					if(config.maxHeat > 0){
						for (DirPos pos : this.heatPos){
							this.tryPullHeat(pos.getPos().getX() + dir.offsetX, pos.getPos().getY() - 1, pos.getPos().getZ() + dir.offsetZ);
						}
					}
				}
			}

			for (DirPos pos : this.connectionPos) {
				if (config.generatorMode && power > 0)
					this.tryProvide(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				for (FluidTankNTM tank : this.outputTanks)
					if (tank.getFill() > 0)
						this.tryProvide(tank, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				this.sendSmoke(pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			}

			if (this.structureOK) {

				if (config.generatorMode) {
					if (this.cachedRecipe == null) {
						CustomMachineRecipe recipe = this.getMatchingRecipe();
						if (recipe != null && this.hasRequiredQuantities(recipe) && this.hasSpace(recipe)) {
							this.cachedRecipe = recipe;
							this.useUpInput(recipe);
						}
					}

					if (this.cachedRecipe != null) {
						this.maxProgress = (int) Math.max(cachedRecipe.duration / this.config.recipeSpeedMult, 1);
						int powerReq = (int) Math.max(cachedRecipe.consumptionPerTick * this.config.recipeConsumptionMult, 1);

						this.progress++;
						this.power += powerReq;
						this.heat -= cachedRecipe.heat;
						if (power > config.maxPower) power = config.maxPower;
						if (world.getTotalWorldTime() % 20 == 0) {
							pollution(cachedRecipe);
							radiation(cachedRecipe);
						}
						if (progress >= this.maxProgress) {
							this.progress = 0;
							this.processRecipe(cachedRecipe);
							this.cachedRecipe = null;
						}
					}

				} else {
					CustomMachineRecipe recipe = this.getMatchingRecipe();

					if (recipe != null) {
						this.maxProgress = (int) Math.max(recipe.duration / this.config.recipeSpeedMult, 1);
						int powerReq = (int) Math.max(recipe.consumptionPerTick * this.config.recipeConsumptionMult, 1);

						if (this.power >= powerReq && this.hasRequiredQuantities(recipe) && this.hasSpace(recipe)) {
							this.progress++;
							this.power -= powerReq;
							this.heat -= recipe.heat;
							if (world.getTotalWorldTime() % 20 == 0) {
								pollution(recipe);
								radiation(recipe);
							}
							if (progress >= this.maxProgress) {
								this.progress = 0;
								this.useUpInput(recipe);
								this.processRecipe(recipe);
							}
						}
					} else {
						this.progress = 0;
					}
				}
			} else {
				this.progress = 0;
			}
			this.networkPackNT(50);
		}

	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		BufferUtil.writeString(buf, this.machineType);

		buf.writeLong(power);
		buf.writeInt(progress);
		buf.writeInt(flux);
		buf.writeInt(heat);
		buf.writeBoolean(structureOK);
		buf.writeInt(maxProgress);
		for (FluidTankNTM inputTank : inputTanks) inputTank.serialize(buf);
		for (FluidTankNTM outputTank : outputTanks) outputTank.serialize(buf);
		this.matcher.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		this.machineType = BufferUtil.readString(buf);
		if(this.config == null) this.init();

		this.power = buf.readLong();
		this.progress = buf.readInt();
		this.flux = buf.readInt();
		this.heat = buf.readInt();
		this.structureOK = buf.readBoolean();
		this.maxProgress = buf.readInt();
		for (FluidTankNTM inputTank : inputTanks) inputTank.deserialize(buf);
		for (FluidTankNTM outputTank : outputTanks) outputTank.deserialize(buf);
		this.matcher.deserialize(buf);
	}

	/** Only accepts inputs in a fixed order, saves a ton of performance because there's no permutations to check for */
	public CustomMachineRecipe getMatchingRecipe() {
		List<CustomMachineRecipe> recipes = CustomMachineRecipes.recipes.get(this.config.recipeKey);
		if(recipes == null || recipes.isEmpty()) return null;

		outer:
		for(CustomMachineRecipe recipe : recipes) {
			for(int i = 0; i < recipe.inputFluids.length; i++) {
				if(this.inputTanks[i].getTankType() != recipe.inputFluids[i].type || this.inputTanks[i].getPressure() != recipe.inputFluids[i].pressure) continue outer;
			}

			for(int i = 0; i < recipe.inputItems.length; i++) {
				if(recipe.inputItems[i] != null && inventory.getStackInSlot(i + 4).isEmpty()) continue outer;
				if(!recipe.inputItems[i].matchesRecipe(inventory.getStackInSlot(i + 4), true)) continue outer;
			}

			return recipe;
		}

		return null;
	}

	public void pollution(CustomMachineRecipe recipe) {
		if(recipe.pollutionAmount > 0) {
			this.pollute(PollutionHandler.PollutionType.valueOf(recipe.pollutionType), recipe.pollutionAmount);
		} else if(recipe.pollutionAmount < 0 && PollutionHandler.getPollution(world, pos, PollutionHandler.PollutionType.valueOf(recipe.pollutionType)) >= -recipe.pollutionAmount) {
			PollutionHandler.decrementPollution(world, pos, PollutionHandler.PollutionType.valueOf(recipe.pollutionType), -recipe.pollutionAmount);
		}
	}

	public void radiation(CustomMachineRecipe recipe){
		if(recipe.radiationAmount > 0) {
			ChunkRadiationManager.proxy.incrementRad(world, pos, recipe.radiationAmount);
		} else if (recipe.radiationAmount < 0) {
			ChunkRadiationManager.proxy.decrementRad(world, pos, -recipe.radiationAmount);
		}
	}

	protected void tryPullHeat(int x, int y, int z) {
		TileEntity con = world.getTileEntity(new BlockPos(x, y, z));

		if(con instanceof IHeatSource source) {
			int diff = source.getHeatStored() - this.heat;

			if(diff == 0) {
				return;
			}

			if(diff > 0) {
				source.useUpHeat(diff);
				this.heat += diff;
				if(this.heat > this.maxHeat)
					this.heat = this.maxHeat;
			}
		}
	}

	public boolean hasRequiredQuantities(CustomMachineRecipe recipe) {

		for(int i = 0; i < recipe.inputFluids.length; i++) {
			if(this.inputTanks[i].getFill() < recipe.inputFluids[i].fill) return false;
		}

		for(int i = 0; i < recipe.inputItems.length; i++) {
			ItemStack stack = inventory.getStackInSlot(i + 4);
			if(!stack.isEmpty() && stack.getCount() < recipe.inputItems[i].stacksize) return false;
		}
		if(config.fluxMode && this.flux < recipe.flux) return false;
		if(config.maxHeat > 0 && recipe.heat > 0 && this.heat < recipe.heat) return false;
		return true;
	}

	public boolean hasSpace(CustomMachineRecipe recipe) {

		for(int i = 0; i < recipe.outputFluids.length; i++) {
			if(this.outputTanks[i].getTankType() == recipe.outputFluids[i].type && this.outputTanks[i].getFill() + recipe.outputFluids[i].fill > this.outputTanks[i].getMaxFill()) return false;
		}

		for(int i = 0; i < recipe.outputItems.length; i++) {
			ItemStack stack = inventory.getStackInSlot(i + 16);
			if(!stack.isEmpty() && (stack.getItem() != recipe.outputItems[i].getKey().getItem() || stack.getItemDamage() != recipe.outputItems[i].getKey().getItemDamage())) return false;
			if(!stack.isEmpty() && stack.getCount() + recipe.outputItems[i].getKey().getCount() > stack.getMaxStackSize()) return false;
		}

		return true;
	}

	public void useUpInput(CustomMachineRecipe recipe) {

		for(int i = 0; i < recipe.inputFluids.length; i++) {
			this.inputTanks[i].setFill(this.inputTanks[i].getFill() - recipe.inputFluids[i].fill);
		}

		for(int i = 0; i < recipe.inputItems.length; i++) {
			inventory.getStackInSlot(i + 4).shrink(recipe.inputItems[i].stacksize);
		}
		this.markDirty();
	}

	public void processRecipe(CustomMachineRecipe recipe) {

		for(int i = 0; i < recipe.outputFluids.length; i++) {
			if(this.outputTanks[i].getTankType() != recipe.outputFluids[i].type) this.outputTanks[i].setTankType(recipe.outputFluids[i].type);
			this.outputTanks[i].setFill(this.outputTanks[i].getFill() + recipe.outputFluids[i].fill);
		}

		for(int i = 0; i < recipe.outputItems.length; i++) {

			if(world.rand.nextFloat() < recipe.outputItems[i].getValue()) {
				if(inventory.getStackInSlot(i + 16).isEmpty()) {
					inventory.setStackInSlot(i + 16, recipe.outputItems[i].getKey().copy());
				} else {
					inventory.getStackInSlot(i + 16).grow(recipe.outputItems[i].getKey().getCount());
				}
			}
		}
		this.markDirty();
	}

	public boolean checkStructure() {

		this.connectionPos.clear();
		this.fluxPos.clear();
		this.heatPos.clear();
		this.structureCheckDelay = 300;
		this.structureOK = false;
		if(this.config == null) return false;

		ForgeDirection dir = this.getDir();
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		for(ComponentDefinition comp : config.components) {

			/* vvv precisely the same method used for defining ports vvv */
			int x = pos.getX() - dir.offsetX * comp.x + rot.offsetX * comp.x;
			int y = pos.getY() + comp.y;
			int z = pos.getZ() - dir.offsetZ * comp.z + rot.offsetZ * comp.z;
			/* but for EW directions it just stops working entirely */
			/* there is absolutely zero reason why this should be required */
			if(dir == ForgeDirection.EAST || dir == ForgeDirection.WEST) {
				x = pos.getX() + dir.offsetZ * comp.z - rot.offsetZ * comp.z;
				z = pos.getZ() + dir.offsetX * comp.x - rot.offsetX * comp.x;
			}
			/* i wholeheartedly believe it is the computer who is wrong here */

			IBlockState state = world.getBlockState(new BlockPos(x, y, z));
			if(state.getBlock() != comp.block) return false;

			int meta = comp.block.getMetaFromState(state);
			if(!comp.allowedMetas.contains(meta)) return false;

			TileEntity tile = Compat.getTileStandard(world, x, y, z);
			if(tile instanceof TileEntityProxyBase proxy) {
				proxy.cachedPosition = new BlockPos(pos);
				proxy.markDirty();

				for(ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
					this.connectionPos.add(new DirPos(x + facing.offsetX, y + facing.offsetY, z + facing.offsetZ, facing));
				}
			}
			if(state.getBlock() == ModBlocks.cm_flux){
				for(ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
					this.fluxPos.add(new DirPos(x + facing.offsetX, y + facing.offsetY, z + facing.offsetZ, facing));
				}
			}
			else if(state.getBlock() == ModBlocks.cm_heat){
				for(ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
					this.heatPos.add(new DirPos(x + facing.offsetX, y + facing.offsetY, z + facing.offsetZ, facing));
				}
			}

		}
		for(ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
			this.connectionPos.add(new DirPos(pos.getX() + facing.offsetX, pos.getY() + facing.offsetY, pos.getZ() + facing.offsetZ, facing));
		}

		this.structureOK = true;
		return true;
	}

	public void buildStructure() {

		if(this.config == null) return;

		ForgeDirection dir = this.getDir();
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		for(ComponentDefinition comp : config.components) {

			int x = pos.getX() - dir.offsetX * comp.x + rot.offsetX * comp.x;
			int y = pos.getY() + comp.y;
			int z = pos.getZ() - dir.offsetZ * comp.z + rot.offsetZ * comp.z;
			if(dir == ForgeDirection.EAST || dir == ForgeDirection.WEST) {
				x = pos.getX() + dir.offsetZ * comp.z - rot.offsetZ * comp.z;
				z = pos.getZ() + dir.offsetX * comp.x - rot.offsetX * comp.x;
			}

			world.setBlockState(new BlockPos(x, y, z), comp.block.getStateFromMeta((Integer) comp.allowedMetas.toArray()[0]), 3);
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing side) {
		if(this.config == null) return new int[] { };
		if(this.config.itemInCount > 5) return new int[] { 4, 5, 6, 7, 8, 9, 16, 17, 18, 19, 20, 21 };
		if(this.config.itemInCount > 4) return new int[] { 4, 5, 6, 7, 8, 16, 17, 18, 19, 20, 21 };
		if(this.config.itemInCount > 3) return new int[] { 4, 5, 6, 7, 16, 17, 18, 19, 20, 21 };
		if(this.config.itemInCount > 2) return new int[] { 4, 5, 6, 16, 17, 18, 19, 20, 21 };
		if(this.config.itemInCount > 1) return new int[] { 4, 5, 16, 17, 18, 19, 20, 21 };
		if(this.config.itemInCount > 0) return new int[] { 4, 16, 17, 18, 19, 20, 21 };
		return new int[] { 16, 17, 18, 19, 20, 21 };
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, int amount) {
		return i >= 16 && i <= 21;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot < 4 || slot > 9) return false;

		int index = slot - 4;
		int filterSlot = slot + 6;

		if(inventory.getStackInSlot(filterSlot).isEmpty()) return true;

		return matcher.isValidForFilter(inventory.getStackInSlot(filterSlot), index, stack);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		this.machineType = nbt.getString("machineType");
		this.init();

		super.readFromNBT(nbt);

		if(this.config != null) {

			for(int i = 0; i < inputTanks.length; i++) inputTanks[i].readFromNBT(nbt, "i" + i);
			for(int i = 0; i < outputTanks.length; i++) outputTanks[i].readFromNBT(nbt, "o" + i);

			this.matcher.readFromNBT(nbt);

			int index = nbt.getInteger("cachedIndex");
			if(index != -1) {
				List<CustomMachineRecipe> recipes = CustomMachineRecipes.recipes.get(this.config.recipeKey);
				if(recipes != null && index < recipes.size()) this.cachedRecipe = recipes.get(index);
			}
		}
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		if(machineType == null || this.config == null) {
			return super.writeToNBT(nbt);
		}

		nbt.setString("machineType", machineType);

		super.writeToNBT(nbt);

		for(int i = 0; i < inputTanks.length; i++) inputTanks[i].writeToNBT(nbt, "i" + i);
		for(int i = 0; i < outputTanks.length; i++) outputTanks[i].writeToNBT(nbt, "o" + i);

		this.matcher.writeToNBT(nbt);

		if(this.cachedRecipe != null) {
			int index = CustomMachineRecipes.recipes.get(this.config.recipeKey).indexOf(this.cachedRecipe);
			nbt.setInteger("cachedIndex", index);
		} else {
			nbt.setInteger("cachedIndex", -1);
		}
		return nbt;
	}

	@Override
	public FluidTankNTM[] getAllTanks() {

		FluidTankNTM[] all = new FluidTankNTM[inputTanks.length + outputTanks.length];

		for(int i = 0; i < inputTanks.length; i++) all[i] = inputTanks[i];
		for(int i = 0; i < outputTanks.length; i++) all[inputTanks.length + i] = outputTanks[i];

		return all;
	}

	@Override
	public @NotNull FluidTankNTM[] getSendingTanks() {
		FluidTankNTM[] all = new FluidTankNTM[outputTanks.length + this.getSmokeTanks().length];
		for(int i = 0; i < outputTanks.length; i++) all[i] = outputTanks[i];
		for(int i = 0; i < this.getSmokeTanks().length; i++) all[outputTanks.length + i] = this.getSmokeTanks()[i];
		return all;
	}

	@Override
	public @NotNull FluidTankNTM[] getReceivingTanks() {
		return inputTanks != null ? inputTanks : new FluidTankNTM[0];
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(this.config == null) return null;
		return new ContainerMachineCustom(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(this.config == null) return null;
		return new GUIMachineCustom(player.inventory, this);
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public long getMaxPower() {
		return this.config != null ? this.config.maxPower : 1;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long transferPower(long power, boolean simulate) {
		if(this.config != null && this.config.generatorMode) return power;
		return IEnergyReceiverMK2.super.transferPower(power, simulate);
	}

	@Override
	public long getReceiverSpeed() {
		if(this.config != null && !this.config.generatorMode) return this.getMaxPower();
		return 0;
	}

	@Override
	public long getProviderSpeed() {
		if(this.config != null && this.config.generatorMode) return this.getMaxPower();
		return 0;
	}
}
