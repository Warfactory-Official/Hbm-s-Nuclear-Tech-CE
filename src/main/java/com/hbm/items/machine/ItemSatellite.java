package com.hbm.items.machine;

import com.hbm.items.ISatChip;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public class ItemSatellite extends ItemEnumMulti<ItemSatellite.EnumSatType> implements ISatChip {

	public ItemSatellite(String s) {
		super(s, EnumSatType.VALUES, true, true);
	}

	public enum EnumSatType {
		SPY("satchip.mapper"),
		SCANNER("satchip.scanner"),
		RADAR("satchip.radar"),
		MINER_ASTRO("satchip.miner"),
		MINER_LUNAR("satchip.lunar_miner"),
		PRECISION_LASER("satchip.precision_laser"),
		DEATH_RAY("satchip.laser"),
		XENIUM_RESONATOR("satchip.resonator"),
		RELAY("satchip.foeq"),
		DETECTOR("satchip.detector"),
		RAY_SCAN("satchip.ray_scanner"),
		SCIENCE("satchip.science"),
		SCIENCE_ASSEMBLER("satchip.science_assembler"),
		SCIENCE_SENSOR("satchip.science_sensor");

		public final String descKey;

		EnumSatType(String descKey) {
			this.descKey = descKey;
		}

		public static final EnumSatType[] VALUES = values();
	}

	public static ItemStack make(EnumSatType type) {
		return new ItemStack(ModItems.satellite, 1, type.ordinal());
	}

	public static EnumSatType getType(ItemStack stack) {
		return EnumUtil.grabEnumSafely(EnumSatType.VALUES, stack.getItemDamage());
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);
		list.add(TextFormatting.AQUA + I18nUtil.resolveKey("satchip.frequency") + ": " + getFreq(stack));
		list.addAll(Arrays.asList(I18nUtil.resolveKeyArray(getType(stack).descKey)));
	}
}
