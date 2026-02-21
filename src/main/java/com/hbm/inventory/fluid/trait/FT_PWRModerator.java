package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.util.I18nUtil;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.List;

public class FT_PWRModerator extends FluidTrait {

	private double multiplier;
	
	public FT_PWRModerator(double mulitplier) {
		this.multiplier = mulitplier;
	}
	
	public double getMultiplier() {
		return multiplier;
	}
	
	@Override
	public void addInfo(List<String> info) {
		info.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("trait.pwrflux") + "]");
	}

	@Override
	public void addInfoHidden(List<String> info) {
		int mult = (int) (multiplier * 100 - 100);
		info.add(TextFormatting.BLUE + I18nUtil.resolveKey("trait.pwrflux.desc") + " " + (mult >= 0 ? "+" : "") + mult + "%");
	}

	@Override
	public void serializeJSON(JsonWriter writer) throws IOException {
		writer.name("multiplier").value(multiplier);
	}
	
	@Override
	public void deserializeJSON(JsonObject obj) {
		this.multiplier = obj.get("multiplier").getAsDouble();
	}
}
