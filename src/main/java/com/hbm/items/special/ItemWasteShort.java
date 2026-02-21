package com.hbm.items.special;

import com.hbm.items.ItemBase;
import com.hbm.main.MainRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemWasteShort extends ItemBase {

	public ItemWasteShort(String s){
		super(s);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setCreativeTab(MainRegistry.controlTab);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items){
		if(tab == CreativeTabs.SEARCH || tab == this.getCreativeTab())
			for(int i = 0; i < WasteClass.VALUES.length; ++i) {
				items.add(new ItemStack(this, 1, i));
			}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn){
		list.add(TextFormatting.ITALIC + WasteClass.VALUES[rectify(stack.getItemDamage())].name);
		super.addInformation(stack, world, list, flagIn);
	}

	public static int rectify(int meta){
		return Math.abs(meta) % WasteClass.VALUES.length;
	}

	public enum WasteClass {

		//all decayed versions include lead-types and classic nuclear waste
		URANIUM233("Uranium-233", 50, 100), //fresh recycling makes iodine, caesium and technetium, depleted turns into u235
		URANIUM235("Uranium-235", 0, 100), //fresh recycling makes iodine, caesium and technetium, depleted turns into neptunium
		NEPTUNIUM("Neptunium-237", 150, 500), //funny fission fragments + polonium and pu238 and 239 / u235
		PLUTONIUM239("Plutonium-239", 250, 1000), //funny fission fragments + pu240 and 241 / u238 (actually u236 but fuck you)
		PLUTONIUM240("Plutonium-240", 350, 1000), //funny fission fragments + pu241 / u238  + lead
		PLUTONIUM241("Plutonium-241", 500, 1000), //funny fission fragments + am241 / 242 / np237 + bismuth
		AMERICIUM242("Americium-242", 750, 1000), //funny fission fragments + californium / np237 + pu241
		SCHRABIDIUM("Schrabidium-326", 1000, 1000); //funniest fission fragments

        public static final WasteClass[] VALUES = values();

		public String name;
		public int liquid;
		public int gas;

		private WasteClass(String name, int liquid, int gas){
			this.name = name;
			this.liquid = liquid;
			this.gas = gas;
		}
	}
}