package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.util.I18nUtil;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.List;

public class FT_Pheromone extends  FluidTrait{

	public int type;

	public FT_Pheromone(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	@Override
	public void addInfo(List<String> info) {

		if(type == 1) {
			info.add(TextFormatting.AQUA + "[" + I18nUtil.resolveKey("trait.pherg") + "]");
		} else {
			info.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("trait.pherm") + "]");
		}
	}

	@Override
	public void serializeJSON(JsonWriter writer) throws IOException {
		writer.name("type").value(type);
	}

	@Override
	public void deserializeJSON(JsonObject obj) {
		this.type = obj.get("type").getAsInt();
	}
}
