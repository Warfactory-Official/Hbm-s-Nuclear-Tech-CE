package com.hbm.items.armor;

import com.hbm.api.fluidmk2.IFillableItem;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArmorFSBFueled extends ArmorFSB implements IFillableItem {


	FluidType fuelType;
	public int maxFuel = 1;
	public int fillRate;
	public int consumption;
	public int drain;

	public ArmorFSBFueled(ArmorMaterial material, int layer, EntityEquipmentSlot slot, String texture, FluidType fuelType, int maxFuel, int fillRate, int consumption, int drain, String s) {
		super(material, layer, slot, texture, s);
		this.fuelType = fuelType;
		this.fillRate = fillRate;
		this.consumption = consumption;
		this.drain = drain;
		this.maxFuel = maxFuel;
	}

	@Override
	public int getFill(ItemStack stack) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			setFill(stack, maxFuel);
			return maxFuel;
		}
		
		return stack.getTagCompound().getInteger("fuel");
	}

	public void setFill(ItemStack stack, int fill) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		
		stack.getTagCompound().setInteger("fuel", fill);
	}

	public int getMaxFill(ItemStack stack) {
		return this.maxFuel;
	}

	public int getLoadSpeed(ItemStack stack) {
		return this.fillRate;
	}

	public int getUnloadSpeed(ItemStack stack) {
		return 0;
	}

	@Override
	public void setDamage(ItemStack stack, int damage) {
		this.setFill(stack, Math.max(this.getFill(stack) - (damage * consumption), 0));
	}

	@Override
	public boolean isArmorEnabled(ItemStack stack) {
		return getFill(stack) > 0;
	}

	@Override
	public void onArmorTick(@NotNull World world, @NotNull EntityPlayer player, @NotNull ItemStack stack) {

		super.onArmorTick(world, player, stack);

		if(this.drain > 0 && ArmorFSB.hasFSBArmor(player) && !player.capabilities.isCreativeMode && world.getTotalWorldTime() % 10 == 0) {
			this.setFill(stack, Math.max(this.getFill(stack) - this.drain, 0));
		}
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {
		list.add(this.fuelType.getLocalizedName() + ": " + BobMathUtil.getShortNumber(getFill(stack)) + " / " + BobMathUtil.getShortNumber(getMaxFill(stack)));
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getFill(stack) < getMaxFill(stack);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1 - (double) getFill(stack) / (double) getMaxFill(stack);
	}

	@Override
	public boolean acceptsFluid(FluidType type, ItemStack stack) {
		return type == this.fuelType;
	}

	@Override
	public int tryFill(FluidType type, int amount, ItemStack stack) {
		
		if(!acceptsFluid(type, stack))
			return amount;
		
		int toFill = Math.min(amount, this.fillRate);
		toFill = Math.min(toFill, this.maxFuel - this.getFill(stack));
		this.setFill(stack, this.getFill(stack) + toFill);
		
		return amount - toFill;
	}

	@Override
	public boolean providesFluid(FluidType type, ItemStack stack) {
		return false;
	}

	@Override
	public int tryEmpty(FluidType type, int amount, ItemStack stack) {
		return 0;
	}

	@Override
	public FluidType getFirstFluidType(ItemStack stack) {
		return null;
	}

	@Override
	public int getItemEnchantability() {
		return 0;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}
}
