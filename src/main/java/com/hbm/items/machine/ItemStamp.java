package com.hbm.items.machine;

import com.hbm.items.ItemBakedBase;
import com.hbm.main.MainRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemStamp extends ItemBakedBase {

	protected StampType type;
	public static final HashMap<StampType, List<ItemStack>> stamps = new HashMap<>();

	public ItemStamp(String s, int dura, StampType type) {
		super(s);
		this.setMaxDamage(dura);
		this.setCreativeTab(MainRegistry.controlTab);
		this.setMaxStackSize(1);
		this.type = type;
		if(type != null) {
			this.addStampToList(this, 0, type);
		}
	}

	protected void addStampToList(Item item, int meta, StampType type) {
		List<ItemStack> list = stamps.get(type);

		if(list == null)
			list = new ArrayList<>();

		ItemStack stack = new ItemStack(item, 1, meta);

		list.add(stack);
		stamps.put(type, list);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(stack.getMaxDamage() > 0 && stack.getItemDamage() == 0) tooltip.add("Durability: "+ stack.getMaxDamage() + " / " + stack.getMaxDamage());
	}

	/** Params can't take an ItemStack, for some reason it crashes during init */
	public StampType getStampType(Item item, int meta) {
		return type;
	}

	public static enum StampType {
		FLAT,
		PLATE,
		WIRE,
		CIRCUIT,
		C357,
		C44,
		C50,
		C9,
		PRINTING1,
		PRINTING2,
		PRINTING3,
		PRINTING4,
		PRINTING5,
		PRINTING6,
		PRINTING7,
		PRINTING8;
	}
}
