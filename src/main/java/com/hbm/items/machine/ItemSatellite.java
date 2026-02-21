package com.hbm.items.machine;

import com.hbm.items.ISatChip;
import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public class ItemSatellite extends ItemBakedBase implements ISatChip {

	public ItemSatellite(String s) {
		super(s);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, list, flagIn);
		list.add(I18nUtil.resolveKey("desc.satellitefr", getFreq(stack)));

		if(this == ModItems.sat_foeq)
			list.add(I18nUtil.resolveKey("satchip.foeq"));

		if (this == ModItems.sat_gerald) {
			String[] lines = I18nUtil.resolveKeyArray("satchip.gerald.desc");
			list.addAll(Arrays.asList(lines));
		}

		if(this == ModItems.sat_laser)
			list.add(I18nUtil.resolveKey("satchip.laser"));

		if(this == ModItems.sat_mapper)
			list.add(I18nUtil.resolveKey("satchip.mapper"));

		if(this == ModItems.sat_miner)
			list.add(I18nUtil.resolveKey("satchip.miner"));

		if(this == ModItems.sat_lunar_miner)
			list.add(I18nUtil.resolveKey("satchip.lunar_miner"));

		if(this == ModItems.sat_radar)
			list.add(I18nUtil.resolveKey("satchip.radar"));

		if(this == ModItems.sat_resonator)
			list.add(I18nUtil.resolveKey("satchip.resonator"));

		if(this == ModItems.sat_scanner)
			list.add(I18nUtil.resolveKey("satchip.scanner"));
	}
}
