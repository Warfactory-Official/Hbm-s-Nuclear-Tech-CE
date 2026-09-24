package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPileRodMK2 extends ItemEnumMulti<ItemPileRodMK2.EnumPileRod> {

	public static final String KEY_NBT_DEPLETION = "depletion";

	public ItemPileRodMK2(String registryName) {
		super(registryName, EnumPileRod.VALUES, true, true);
	}

	public enum EnumPileRod {
		RA226BE(1D),
		PO210BE(1D),
		ZR(0D, 0D, 0D, 2),
		NU(1D, 25_000D, 0.25D, 4),
		PU239(1D, 500D, 0.5D, 5),
		RGP(1D, 1_000D, 0.5D, 6),
		WASTE(1D, 0D, 1.5D, 6),
		THORIUM(1D, 35_000D, 0.25D, 8),
		THORIUM_FUEL(1D, 2_000D, 0.5D, 6);

		public static final EnumPileRod[] VALUES = values();

		public double reactionMult = 1.0D;
		public double life = 1_000D;
		public double heatMult = 0.0D;
		public double neutronSource = 0D;
		public int turnsInto;

		EnumPileRod(double neutronSource) {
			this.neutronSource = neutronSource;
			this.reactionMult = 0;
			this.life = 0;
			this.heatMult = 0;
		}

		EnumPileRod(double reaction, double life, double heat, int turnsInto) {
			this.reactionMult = reaction;
			this.life = life;
			this.heatMult = heat;
			this.turnsInto = turnsInto;
		}
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
		EnumPileRod rod = EnumUtil.grabEnumSafely(EnumPileRod.VALUES, stack.getItemDamage());

		if(rod.life > 0) {
			list.add("Lifetime: " + (int) Math.round(rod.life));
			double depletion = getDepletionPercent(stack);
			if(depletion > 0) list.add("Depletion: " + (int) Math.round(depletion) + "%");
		}

		for(String loc : I18nUtil.autoBreak(Minecraft.getMinecraft().fontRenderer, I18nUtil.resolveKey(this.getTranslationKey(stack) + ".desc"), 225)) {
			list.add(TextFormatting.YELLOW + loc);
		}
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) > 0D;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		EnumPileRod rod = EnumUtil.grabEnumSafely(EnumPileRod.VALUES, stack.getItemDamage());
		double life = rod.life;
		if(life <= 0) return 0D;
		return getDepletion(stack) / life;
	}

	public static double getDepletionPercent(ItemStack stack) {
		if(stack == null || stack.isEmpty()) return 0D;
		EnumPileRod rod = EnumUtil.grabEnumSafely(EnumPileRod.VALUES, stack.getItemDamage());
		double life = rod.life;
		if(life <= 0) return 0D;
		return (getDepletion(stack) / life) * 100;
	}

	public static double getDepletion(ItemStack stack) {
		if(!stack.hasTagCompound()) return 0D;
		return stack.getTagCompound().getDouble(KEY_NBT_DEPLETION);
	}

	public static void setDepletion(ItemStack stack, double depletion) {
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setDouble(KEY_NBT_DEPLETION, depletion);
	}

	public static double getReactivity(ItemStack stack, double inFlux) {
		EnumPileRod rod = EnumUtil.grabEnumSafely(EnumPileRod.VALUES, stack.getItemDamage());
		double outFlux = rod.neutronSource;
		if(rod.reactionMult > 0) {
			outFlux += BobMathUtil.squirt(inFlux) * rod.reactionMult;
		}
		return outFlux;
	}

	public static double getHeatPerNeutron(ItemStack stack) {
		EnumPileRod rod = EnumUtil.grabEnumSafely(EnumPileRod.VALUES, stack.getItemDamage());
		return rod.heatMult;
	}

	public static ItemStack react(ItemStack stack, double inFlux) {
		EnumPileRod rod = EnumUtil.grabEnumSafely(EnumPileRod.VALUES, stack.getItemDamage());
		if(rod.life <= 0) return stack;
		double dep = getDepletion(stack) + inFlux;

		if(dep < rod.life) {
			setDepletion(stack, dep);
			return stack;
		} else {
			return new ItemStack(stack.getItem(), 1, rod.turnsInto);
		}
	}
}
