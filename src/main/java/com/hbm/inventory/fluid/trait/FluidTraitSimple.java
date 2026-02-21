package com.hbm.inventory.fluid.trait;

import com.hbm.util.I18nUtil;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class FluidTraitSimple {

	public static class FT_Gaseous extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("trait.gaseous") + "]");
		}
	}

	/** gaseous at room temperature, for cryogenic hydrogen for example */
	public static class FT_Gaseous_ART extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("trait.gaseousroom") + "]");
		}
	}

	public static class FT_Liquid extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("trait.liquid") + "]");
		}
	}

	/** to viscous to be sprayed/turned into a mist */
	public static class FT_Viscous extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("trait.viscous") + "]");
		}
	}

	public static class FT_Plasma extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.LIGHT_PURPLE + "[" + I18nUtil.resolveKey("trait.plasma") + "]");
		}
	}

	public static class FT_Amat extends FluidTrait {
		@Override public void addInfo(List<String> info) {
			info.add(TextFormatting.DARK_RED + "[" + I18nUtil.resolveKey("trait.antimatterliq") + "]");
		}
	}

	public static class FT_LeadContainer extends FluidTrait {
		@Override public void addInfo(List<String> info) {
			info.add(TextFormatting.DARK_RED + "[" + I18nUtil.resolveKey("trait.haztank") + "]");
		}
	}

	public static class FT_Delicious extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.DARK_GREEN +"[" + I18nUtil.resolveKey("trait.delicious") + "]");
		}
	}

	public static class FT_Unsiphonable extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("trait.nosiphon") + "]");
		}
	}

	public static class FT_NoID extends FluidTrait { }
	public static class FT_NoContainer extends FluidTrait { }
}
