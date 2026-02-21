package com.hbm.items.machine;

import com.hbm.items.ModItems;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemFELCrystal extends Item {

	public EnumWavelengths wavelength = EnumWavelengths.NULL;

	public ItemFELCrystal(EnumWavelengths wavelength, String s) {
		this.wavelength = wavelength;
		this.setMaxStackSize(1);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		String desc = (stack.getItem() == ModItems.laser_crystal_digamma) ? (TextFormatting.OBFUSCATED + "THERADIANCEOFATHOUSANDSUNS") : (this.getUnlocalizedNameInefficiently(stack) + ".desc");
		list.add(I18nUtil.resolveKey(desc));
		list.add(wavelength.textColor + I18nUtil.resolveKey(wavelength.name) + " - " + wavelength.textColor + I18nUtil.resolveKey(this.wavelength.wavelengthRange));
	}

	public enum EnumWavelengths{
		NULL("la creatura", "6 dollar", 0x010101, 0x010101, TextFormatting.WHITE), //why do you exist?
		IR("wavelengths.name.ir", "wavelengths.waveRange.ir", 0xBB1010, 0xCC4040, TextFormatting.RED),
		VISIBLE("wavelengths.name.visible", "wavelengths.waveRange.visible", 0, 0, TextFormatting.GREEN),
		UV("wavelengths.name.uv", "wavelengths.waveRange.uv", 0x0A1FC4, 0x00EFFF, TextFormatting.AQUA),
		GAMMA("wavelengths.name.gamma", "wavelengths.waveRange.gamma", 0x150560, 0xEF00FF, TextFormatting.LIGHT_PURPLE),
		DRX("wavelengths.name.drx", "wavelengths.waveRange.drx", 0xFF0000, 0xFF0000, TextFormatting.DARK_RED);

		public final String name;
		public final String wavelengthRange;
		public final int renderedBeamColor;
		public final int guiColor;
		public final TextFormatting textColor;

		EnumWavelengths(String name, String wavelength, int color, int guiColor, TextFormatting textColor) {
			this.name = name;
			this.wavelengthRange = wavelength;
			this.renderedBeamColor = color;
			this.guiColor = guiColor;
			this.textColor = textColor;
		}
	}
}