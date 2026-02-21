package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ItemEnums;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemDrillbit extends ItemEnumMulti<ItemEnums.EnumDrillType> {

	public ItemDrillbit(String s) {
		super(s, ItemEnums.EnumDrillType.VALUES, true, true);
	}
	
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		ItemEnums.EnumDrillType type = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		if(type == null) return;
		list.add("§e"+I18nUtil.resolveKey("desc.speed")+" " + ((int) (type.speed * 100)) + "%");
		list.add("§e"+I18nUtil.resolveKey("desc.tier", type.tier));
		if(type.fortune > 0) list.add("§d"+I18nUtil.resolveKey("desc.fortune")+" " + type.fortune);
		if(type.vein) list.add("§a"+I18nUtil.resolveKey("desc.veinminer"));
		if(type.silk) list.add("§a"+I18nUtil.resolveKey("desc.silktouch"));
	}
}
