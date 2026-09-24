package com.hbm.tileentity.machine;

import java.util.List;
import java.util.Random;

import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.container.ContainerBlastFurnace;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.gui.GUIBlastFurnace;
import com.hbm.inventory.recipes.BlastFurnaceRecipesNT;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes.IOutput;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.modules.ModuleBurnTime;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityMachineBlastFurnace extends TileEntityMachineBase implements ITickable, IFluidStandardTransceiverMK2, IGUIProvider, IFluidCopiable {

	public FluidTankNTM[] tanks;

	public boolean isProgressing;
	public float progress;
	public float speed;
	public int fuel;
	public static final int FUEL_COAL = 200 * 8;
	public static final int FUEL_RATE = 200 * 4; // half coal per operation
	public static final int MAX_FUEL = FUEL_COAL * 24; // 24 pieces of coal
	public static final int FLUE_GAS = 8; // per tick

	public ModuleBurnTime burnModule = new ModuleBurnTime()
			.setWoodHeatMod(0D);

	public TileEntityMachineBlastFurnace() {
		super(5);
		this.tanks = new FluidTankNTM[2];
		this.tanks[0] = new FluidTankNTM(Fluids.AIRBLAST, 4_000).withOwner(this);
		this.tanks[1] = new FluidTankNTM(Fluids.FLUE, 1_000).withOwner(this); // TEMP
	}

	@Override
	public String getDefaultName() {
		return "container.blastFurnace";
	}

	@Override
	public void update() {

		if(!world.isRemote) {
			this.checkTilt(TiltType.CONFIG, false);

			for(DirPos pos : this.getConPos()) {
				this.trySubscribe(tanks[0].getTankType(), world, pos);
				if(this.tanks[1].getFill() > 0) this.tryProvide(tanks[1], world, pos);
			}

			if(!inventory.getStackInSlot(0).isEmpty()) {
				int capacity = MAX_FUEL - fuel;
				int burnValue = getBurnTime(inventory.getStackInSlot(0));
				if(burnValue > 0 && burnValue <= capacity) {
					this.fuel += burnValue;
					inventory.getStackInSlot(0).shrink(1);
				}
			}

			this.speed = 0F;
			GenericRecipe recipe = BlastFurnaceRecipesNT.INSTANCE.getRecipe(inventory.getStackInSlot(1), inventory.getStackInSlot(2));

			if(!this.tilted && recipe != null && this.fuel >= FUEL_RATE && this.hasQuantities(recipe) && this.canOutput(recipe)) {

				this.speed = MathHelper.clamp(0.5F + this.tanks[0].getFill() * 8F / this.tanks[0].getMaxFill(), 0.5F, 5F);

				this.isProgressing = true;
				this.progress += speed / recipe.duration;

				if(this.progress >= 1F) {
					this.process(recipe);
					this.progress = 0F;
					this.fuel -= FUEL_RATE;
					this.tanks[1].setFill((int) (tanks[1].getFill() + FLUE_GAS * (recipe.duration / speed)));
					if(this.tanks[1].getFill() > this.tanks[1].getMaxFill()) {
						int spill = this.tanks[1].getFill() - this.tanks[1].getMaxFill();
						this.tanks[1].getTankType().onFluidRelease(world, pos.getX(), pos.getY() + 7, pos.getZ(), tanks[1], spill);
						FT_Polluting.pollute(world, pos.getX(), pos.getY(), pos.getZ(), tanks[1].getTankType(), FluidReleaseType.SPILL, spill);
						this.tanks[1].setFill(this.tanks[1].getMaxFill());
					}
				}

				if(world.rand.nextInt(10) == 0 && !this.muffled) {
					world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F, 0.5F + world.rand.nextFloat() * 0.25F);
				}

			} else {
				this.isProgressing = false;
				this.progress = 0F;
			}

			if(this.tanks[0].getFill() > 0) this.tanks[0].setFill((int) (this.tanks[0].getFill() * 0.95));

			this.networkPackNT(100);
		} else {

			if(world.isAirBlock(pos.up(7))) {
				if(isProgressing && world.getTotalWorldTime() % 2 == 0) {
					Random rand = world.rand;
					this.world.spawnParticle(EnumParticleTypes.LAVA, pos.getX() + 0.25 + rand.nextDouble() * 0.5, pos.getY() + 7.25, pos.getZ() + 0.25 + rand.nextDouble() * 0.5, 0, 0, 0);

					if(tanks[1].getFill() >= 100 && MainRegistry.proxy.me().getDistanceSq(pos.getX() + 0.5, pos.getY() + 7, pos.getZ() + 0.5) < 100 * 100) {
						if(world.getTotalWorldTime() % 2 == 0) {
							NBTTagCompound fx = new NBTTagCompound();
							fx.setString("type", "tower");
							fx.setFloat("lift", 10F);
							fx.setFloat("base", 0.25F);
							fx.setFloat("max", 2.5F);
							fx.setInteger("life", 100 + world.rand.nextInt(20));
							fx.setInteger("color", 0x202020);
							fx.setDouble("posX", pos.getX() + 0.5);
							fx.setDouble("posY", pos.getY() + 7);
							fx.setDouble("posZ", pos.getZ() + 0.5);
							MainRegistry.proxy.effectNT(fx);
						}
					}
				}
			}
		}
	}

	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		return new DirPos[] {
				new DirPos(pos.getX() + 2, pos.getY(), pos.getZ(), Library.POS_X),
				new DirPos(pos.getX() - 2, pos.getY(), pos.getZ(), Library.NEG_X),
				new DirPos(pos.getX(), pos.getY(), pos.getZ() + 2, Library.POS_Z),
				new DirPos(pos.getX() + dir.offsetX * 2, pos.getY() + 3, pos.getZ() + dir.offsetZ * 2, dir),
				new DirPos(pos.getX() + dir.offsetX * 2, pos.getY() + 5, pos.getZ() + dir.offsetZ * 2, dir),
				new DirPos(pos.getX(), pos.getY() + 7, pos.getZ(), Library.POS_Y)
		};
	}

	public boolean hasQuantities(GenericRecipe recipe) {
		if(recipe.inputItem[0].matchesRecipe(inventory.getStackInSlot(1), false) && recipe.inputItem[1].matchesRecipe(inventory.getStackInSlot(2), false)) return true;
		if(recipe.inputItem[0].matchesRecipe(inventory.getStackInSlot(2), false) && recipe.inputItem[1].matchesRecipe(inventory.getStackInSlot(1), false)) return true;
		return false;
	}

	public boolean canOutput(GenericRecipe recipe) {

		for(int i = 0; i < recipe.outputItem.length; i++) {
			ItemStack slot = inventory.getStackInSlot(3 + i);
			if(slot.isEmpty()) continue;
			IOutput out = recipe.outputItem[i];
			if(out.possibleMultiOutput()) return false;
			ItemStack stack = out.collapse();
			if(stack.getItem() != slot.getItem()) return false;
			if(stack.getItemDamage() != slot.getItemDamage()) return false;
			if(stack.getCount() + slot.getCount() > stack.getMaxStackSize()) return false;
		}

		return true;
	}

	public void process(GenericRecipe recipe) {

		for(int i = 0; i < recipe.outputItem.length; i++) {
			IOutput out = recipe.outputItem[i];
			ItemStack stack = out.collapse();
			if(!inventory.getStackInSlot(3 + i).isEmpty()) inventory.getStackInSlot(3 + i).grow(stack.getCount());
			else inventory.setStackInSlot(3 + i, stack);
		}

		if(recipe.inputItem.length == 1) {
			if(recipe.inputItem[0].matchesRecipe(inventory.getStackInSlot(1), false)) inventory.getStackInSlot(1).shrink(recipe.inputItem[0].stacksize);
			else if(recipe.inputItem[0].matchesRecipe(inventory.getStackInSlot(2), false)) inventory.getStackInSlot(2).shrink(recipe.inputItem[0].stacksize);

		} else if(recipe.inputItem.length == 2) {
			if(recipe.inputItem[0].matchesRecipe(inventory.getStackInSlot(1), false) && recipe.inputItem[1].matchesRecipe(inventory.getStackInSlot(2), false)) {
				inventory.getStackInSlot(1).shrink(recipe.inputItem[0].stacksize);
				inventory.getStackInSlot(2).shrink(recipe.inputItem[1].stacksize);
			} else if(recipe.inputItem[0].matchesRecipe(inventory.getStackInSlot(2), false) && recipe.inputItem[1].matchesRecipe(inventory.getStackInSlot(1), false)) {
				inventory.getStackInSlot(2).shrink(recipe.inputItem[0].stacksize);
				inventory.getStackInSlot(1).shrink(recipe.inputItem[1].stacksize);
			}
		}
	}

	public int getBurnTime(ItemStack stack) {
		if(stack.getItem().hasContainerItem(stack)) return 0;
		return burnModule.getBurnHeat(burnModule.getBurnTime(stack, 0D), stack, 0D);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0 && getBurnTime(stack) > 0) return true;
		if(slot == 1 || slot == 2) return true;
		return false;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack) {

		if(slot == 1 || slot == 2) {

			// repetition prevention
			if(slot == 1 && !inventory.getStackInSlot(2).isEmpty() && stack.isItemEqual(inventory.getStackInSlot(2))) return false;
			if(slot == 2 && !inventory.getStackInSlot(1).isEmpty() && stack.isItemEqual(inventory.getStackInSlot(1))) return false;

			// needs to match at least one recipe
			for(GenericRecipe recipe : BlastFurnaceRecipesNT.INSTANCE.recipeOrderedList) {
				for(AStack input : recipe.inputItem) {
					if(input.matchesRecipe(stack, true)) return true;
				}
			}

			return false;
		}

		return this.isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return slot >= 3;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing side) {
		return new int[] { 1, 2, 0, 3, 4 };
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeBoolean(isProgressing);
		buf.writeFloat(progress);
		buf.writeFloat(speed);
		buf.writeInt(fuel);

		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		this.isProgressing = buf.readBoolean();
		this.progress = buf.readFloat();
		this.speed = buf.readFloat();
		this.fuel = buf.readInt();

		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.progress = nbt.getFloat("progress");
		this.fuel = nbt.getInteger("fuel");
		tanks[0].readFromNBT(nbt, "t0");
		tanks[1].readFromNBT(nbt, "t1");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setFloat("progress", progress);
		nbt.setInteger("fuel", fuel);
		tanks[0].writeToNBT(nbt, "t0");
		tanks[1].writeToNBT(nbt, "t1");
		return nbt;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 7, pos.getZ() + 2);
		}
		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerBlastFurnace(player.inventory, this); }
	@Override @SideOnly(Side.CLIENT) public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIBlastFurnace(player.inventory, this); }

	@Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] { tanks[0] }; }
	@Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] { tanks[1] }; }
	@Override public FluidTankNTM[] getAllTanks() { return tanks; }

	@Override public long getProviderSpeed(FluidType type, int pressure) { return Math.max(tanks[1].getFill() * 50 / tanks[1].getMaxFill(), 1); }

	@Override public FluidTankNTM getTankToPaste() { return null; }

	@Override public int getFloorCount() { return 2 * 2; }
	@Override public BlockPos getFloorPosFromIndex(int index) { return this.standardFloor3x3(index); }
}
