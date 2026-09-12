package com.hbm.items.machine;

import com.hbm.items.ISatChip;
import com.hbm.items.ItemEnumMulti;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemOrbitalAssembly extends ItemEnumMulti<ItemOrbitalAssembly.EnumOrbitalAssembly> implements ISatChip {

	public ItemOrbitalAssembly(String s) {
		super(s, EnumOrbitalAssembly.VALUES, true, false);
	}

	public enum EnumOrbitalAssembly {
		CRYSTAL_CIRCUIT;

		public static final EnumOrbitalAssembly[] VALUES = values();
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);
		list.add(TextFormatting.AQUA + I18nUtil.resolveKey("satchip.frequency") + ": " + getFreq(stack));
	}
}
