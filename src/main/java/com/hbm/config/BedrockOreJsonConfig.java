package com.hbm.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.main.MainRegistry;

import java.util.*;

public class BedrockOreJsonConfig {

	public static final String FILENAME = "hbm_bedrock_ores.json";

	public static HashMap<Integer, HashSet<String>> dimOres = new HashMap<>();
	public static HashMap<Integer, Boolean> dimWhiteList = new HashMap<>();
	public static HashMap<Integer, Integer> dimOreRarity = new HashMap<>();

	public static void init(){
		if(!loadFromJson()){
			clear();
			setDefaults();
            if (JsonConfig.isFileNonexistent(FILENAME)) writeToJson();
		}
	}

	public static void clear(){
		dimOres.clear();
		dimWhiteList.clear();
		dimOreRarity.clear();
	}

	public static boolean isOreAllowed(int dimID, String ore) {
		if (!dimWhiteList.containsKey(dimID)) return true;
		return dimOres.get(dimID).contains(ore);
	}

	public static void setDefaults() {
		addEntry(0, 15, Arrays.asList(
			"orePlutonium",
			"oreQuartz",
			"oreInfernalCoal",
			"oreRedPhosphorus",
			"oreSchrabidium",
			"oreNeodymium",
			"oreNitanium",
			"oreDesh",
			"oreCheese",
			"oreMercury",
			"oreCarbon",
			"oreCrystal",
			"oreWhiteGem",
			"oreRedGem",
			"oreBlueGem",
			"oreDarkIron",
			"oreDenseCoal",
			"oreBlueDiamond",
			"oreRedDiamond",
			"oreGreenDiamond",
			"oreYellowDiamond",
			"orePurpleDiamond",
			"oreAdrite",
			"oreSteel"
		), false);
		addEntry(-1, 60, Arrays.asList(
			"orePlutonium", 
			"oreQuartz", 
			"oreInfernalCoal", 
			"oreRedPhosphorus", 
			"oreSchrabidium", 
			"oreNeodymium", 
			"oreTungsten",
			"oreUranium",
			"oreSulfur",   
			"oreNitanium",
			"oreCobalt",
			"oreAdrite"
		), true);
		addEntry(-6, 30, Arrays.asList(//Mining Dim
			"orePlutonium", 
			"oreQuartz", 
			"oreInfernalCoal", 
			"oreRedPhosphorus", 
			"oreSchrabidium", 
			"oreNeodymium", 
			"oreNitanium",
			"oreDesh",
			"oreCheese",
			"oreMercury",
			"oreCarbon", 
			"oreCrystal", 
			"oreRedGem",
			"oreWhiteGem",
			"oreBlueGem", 
			"oreDarkIron", 
			"oreDenseCoal",
			"oreBlueDiamond",
			"oreRedDiamond",
			"oreGreenDiamond",
			"oreYellowDiamond",
			"orePurpleDiamond",
			"oreAdrite",
			"oreSteel"
		), false);
	}

	public static void addEntry(int dimID, int rarity, List<String> ores, Boolean isWhiteList){
		dimOres.put(dimID, new HashSet<>(ores));
		dimOreRarity.put(dimID, rarity);
		dimWhiteList.put(dimID, isWhiteList);
	}

	// foont: who would ever overwrite config if it's wrong. I don't see a reason to have this, in release mod definitely
    // vidarin: this is needed to generate the file the first time minecraft loads
	public static void writeToJson(){
		try {
			JsonWriter writer = JsonConfig.startWriting(FILENAME);
			writer.name("dimConfig").beginArray();
			for(Integer dimID : dimOres.keySet()){
				writer.beginObject();
					writer.name("dimID").value(dimID);
					writer.name("oreRarity").value(dimOreRarity.get(dimID));
					writer.name("isWhiteList").value(dimWhiteList.get(dimID));
					writer.name("bedrockOres").beginArray();
					for(String line : dimOres.get(dimID))
						writer.value(line);
					writer.endArray();
				writer.endObject();
			}
			writer.endArray();
			JsonConfig.stopWriting(writer);
		} catch(Exception ex) {
			MainRegistry.logger.error(ex.getStackTrace());
		}
	}

	public static boolean loadFromJson() {
		try {
			JsonObject reader = JsonConfig.startReading(FILENAME);

			if(reader == null || !reader.has("dimConfig")) return false;

			JsonArray entries = reader.getAsJsonArray("dimConfig");
			for(JsonElement entry : entries){

				if(entry == null || !entry.isJsonObject()) continue;
				JsonObject dimEntry = entry.getAsJsonObject();

				if(!dimEntry.has("dimID")) return false;
				int dimID = dimEntry.get("dimID").getAsInt();

				if(!dimEntry.has("oreRarity")) return false;
				int oreRarity = dimEntry.get("oreRarity").getAsInt();

				if(!dimEntry.has("isWhiteList")) continue;
				boolean isWhiteList = dimEntry.get("isWhiteList").getAsBoolean();

				if(!dimEntry.has("bedrockOres") || !dimEntry.get("bedrockOres").isJsonArray()) continue;
				JsonArray jbedrockOres = dimEntry.get("bedrockOres").getAsJsonArray();
				List<String> bedrockOres = new ArrayList<String>();
				for(JsonElement ore : jbedrockOres){
					bedrockOres.add(ore.getAsString());
				}

				addEntry(dimID, oreRarity, bedrockOres, isWhiteList);
			}

			return true;
		} catch(Exception ex) {
			MainRegistry.logger.error("Loading the bedrock ore config resulted in an error. Falling back to defaults.");
            MainRegistry.logger.error(ex.getStackTrace());
			return false;
		}
	}
}