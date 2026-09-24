package com.hbm.items.special;

import com.hbm.items.ItemEnumMulti;

public class ItemHoloTape<E extends Enum<E>> extends ItemEnumMulti<E> {

	public ItemHoloTape(String registryName, E[] theEnum, boolean multiName, String texture) {
		super(registryName, theEnum, multiName, texture);
		this.setMaxStackSize(1);
	}
}
