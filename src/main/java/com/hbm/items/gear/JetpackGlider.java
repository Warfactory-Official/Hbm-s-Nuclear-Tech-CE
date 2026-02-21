package com.hbm.items.gear;

import com.hbm.api.fluidmk2.IFillableItem;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.JetpackHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.IDynamicModels;
import com.hbm.items.armor.ItemArmorMod;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class JetpackGlider extends ItemArmorMod implements IFillableItem {

	public int capacity;

	public JetpackGlider(ArmorMaterial enumArmorMaterialSteel, int i, EntityEquipmentSlot chest, int capacity, String s) {
		super(ArmorModHandler.plate_only, false, true, false, false, s);
		this.capacity = capacity;
		IDynamicModels.INSTANCES.remove(this);
	}

	public FluidTankNTM getTank(ItemStack stack){
		FluidTankNTM tank = new FluidTankNTM(Fluids.NONE, capacity);
		if(!stack.hasTagCompound() || !stack.getTagCompound().hasKey("fuelTank")){
			return tank;
		}
		tank.readFromNBT(stack.getTagCompound().getCompoundTag("fuelTank"), "0");
		return tank;
	}

	public void setTank(ItemStack stack, FluidTankNTM tank){
		if(!stack.hasTagCompound()){
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound nbt = new NBTTagCompound();
		tank.writeToNBT(nbt, "0");
		stack.getTagCompound().setTag("fuelTank", nbt);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		FluidTankNTM tank = getTank(stack);
		if(tank.getFluid() == null){
			list.add(TextFormatting.RED + "    Fuel Type: None");
			list.add(TextFormatting.RED + "    Fuel Speed: " + JetpackHandler.getSpeed(null));
		} else {
			list.add(TextFormatting.RED + "    Fuel Type: " + I18n.format(tank.getFluid().getUnlocalizedName()));
			list.add(TextFormatting.RED + "    Fuel Speed: " + JetpackHandler.getSpeed(tank.getTankType()));
		}
		int percent = (int)(((float)tank.getFluidAmount()/tank.getCapacity())*100);
		list.add(TextFormatting.RED + "    Fuel Amount: " + tank.getFluidAmount() + "/" + tank.getCapacity() + " (" + percent + "%)");
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		super.addDesc(list, stack, armor);
		addInformation(stack, null, list, null);
	}

	@Override
	public boolean acceptsFluid(FluidType type, ItemStack stack) {
		FluidType currentType = this.getTank(stack).getTankType();
		if (Fluids.NONE.equals(currentType) || this.getTank(stack).getFill() == 0){
			return type.equals(Fluids.KEROSENE) || type.equals(Fluids.KEROSENE_REFORM) || type.equals(Fluids.BALEFIRE) || type.equals(Fluids.NITAN);
		} else return type.equals(currentType);
	}

	@Override
	public int tryFill(FluidType type, int amount, ItemStack stack) {
		if (stack.getCount() > 1 || !acceptsFluid(type, stack)) return amount;
		FluidTankNTM contained = this.getTank(stack);
		int filled = Math.min(capacity - contained.getFill(), amount);
		if (filled <= 0) return amount;
		if (contained.getFill() == 0) {
			contained.setTankType(type);
		}
		contained.setFill(contained.getFill() + filled);
		this.setTank(stack, contained);
		return amount - filled;
	}

	@Override
	public boolean providesFluid(FluidType type, ItemStack stack) {
		FluidTankNTM contained = this.getTank(stack);
		return contained != null && contained.getTankType() == type;
	}

	@Override
	public int tryEmpty(FluidType type, int amount, ItemStack stack) {
		if (stack.getCount() > 1) return 0;
		FluidTankNTM contained = this.getTank(stack);
		if (contained == null || !contained.getTankType().equals(type)) return 0;
		int drained = Math.min(contained.getFill(), amount);
		if (drained <= 0) return 0;
		contained.setFill(contained.getFill() - drained);
		if (contained.getFill() == 0) contained.setTankType(Fluids.NONE);
		this.setTank(stack, contained);
		return drained;
	}

	@Override
	public FluidType getFirstFluidType(ItemStack stack) {
		FluidType type = this.getTank(stack).getTankType();
		return Fluids.NONE.equals(type) ? null : type;
	}

	@Override
	public int getFill(ItemStack stack) {
		return this.getTank(stack).getFill();
	}
}
