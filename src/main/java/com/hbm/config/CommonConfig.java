package com.hbm.config;

import com.hbm.main.MainRegistry;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class CommonConfig {
	public static final String CATEGORY_GENERAL = "01_general";
	public static final String CATEGORY_ORES = "02_ores";
	public static final String CATEGORY_NUKES = "03_nukes";
	public static final String CATEGORY_DUNGEONS = "04_dungeons";
	public static final String CATEGORY_METEORS = "05_meteors";
	public static final String CATEGORY_EXPLOSIONS = "06_explosions";
	public static final String CATEGORY_MISSILE = "07_missile_machines";
	public static final String CATEGORY_POTION = "08_potion_effects";
	public static final String CATEGORY_MACHINES = "09_machines";
	public static final String CATEGORY_DROPS = "10_dangerous_drops";
	public static final String CATEGORY_TOOLS = "11_tools";
	public static final String CATEGORY_MOBS = "12_mobs";
	public static final String CATEGORY_RADIATION = "13_radiation";
	public static final String CATEGORY_HAZARD = "14_hazard";
	public static final String CATEGORY_STRUCTURES = "15_structures";
	public static final String CATEGORY_POLLUTION = "16_pollution";
	public static final String CATEGORY_BIOMES = "17_biomes";
	public static final String CATEGORY_WEAPONS = "18_weapons";

	public static final String CATEGORY_528 = "528";
	public static final String CATEGORY_LBSM = "LESS BULLSHIT MODE";

	public static boolean createConfigBool(Configuration config, String category, String name, String comment, boolean def) {
	
	    Property prop = config.get(category, name, def);
	    prop.setComment(comment);
	    return prop.getBoolean();
	}
	
	public static String createConfigString(Configuration config, String category, String name, String comment, String def) {

		Property prop = config.get(category, name, def);
		prop.setComment(comment);
		return prop.getString();
	}

	public static String[] createConfigStringList(Configuration config, String category, String name, String comment, String[] defaultValues) {

		Property prop = config.get(category, name, defaultValues);
		prop.setComment(comment);
		return prop.getStringList();
	}

    @SuppressWarnings("unchecked")
	public static <K, V> HashMap<K, V> createConfigHashMap(Configuration config, String category, String name, String comment, Class<K> keyType, Class<V> valueType, String[] defaultValues, String splitReg) {
		HashMap<K, V> configDictionary = new HashMap<>();
		Property prop = config.get(category, name, defaultValues);
		prop.setComment(comment);
		for(String entry: prop.getStringList()){
			String[] pairs = entry.split(splitReg, 0);
			configDictionary.put((K) parseType(pairs[0], keyType), (V) parseType(pairs[1], valueType));
		}
		return configDictionary;
	}

	public static int[] createConfigIntList(Configuration config, String category, String name, String comment, int[] def){
		Property prop = config.get(category, name, def);
		prop.setComment(comment);
		return prop.getIntList();
	}

    @SuppressWarnings("unchecked")
	public static <T> HashSet<T> createConfigHashSet(Configuration config, String category, String name, String comment, Class<T> valueType, String[] defaultValues) {
		HashSet<T> configSet = new HashSet<>();
		Property prop = config.get(category, name, defaultValues);
		prop.setComment(comment);
		for(String entry: prop.getStringList()){
			configSet.add((T) parseType(entry, valueType));
		}
		return configSet;
	}

	private static Object parseType(String value, Class<?> type) {
        if (type == Float.class) return Float.parseFloat(value);
        if (type == Integer.class) return Integer.parseInt(value);
        if (type == Long.class) return Long.parseLong(value);
        if (type == Double.class) return Double.parseDouble(value);
        if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, value);
        return value;
	}

	public static int createConfigInt(Configuration config, String category, String name, String comment, int def) {
	    Property prop = config.get(category, name, def);
	    prop.setComment(comment);
	    return prop.getInt();
	}

	public static double createConfigDouble(Configuration config, String category, String name, String comment, double def) {
	    Property prop = config.get(category, name, def);
	    prop.setComment(comment);
	    return prop.getDouble();
	}

	public static int setDefZero(int value, int def) {
		if (value < 0) {
			MainRegistry.logger.error("Fatal error config: Randomizer value has been below zero, despite bound having to be positive integer!");
			MainRegistry.logger.error(String.format("Errored value will default back to %d, PLEASE REVIEW CONFIGURATION DESCRIPTION BEFORE MEDDLING WITH VALUES!", def));
			return def;
		}

		return value;
	}
	
	public static int setDef(int value, int def) {
		if(value <= 0) {
			MainRegistry.logger.error("Fatal error config: Randomizer value has been set to zero, despite bound having to be positive integer!");
			MainRegistry.logger.error(String.format("Errored value will default back to %d, PLEASE REVIEW CONFIGURATION DESCRIPTION BEFORE MEDDLING WITH VALUES!", def));
			return def;
		}
	
		return value;
	}

	public static int parseStructureFlag(String flag) {
		if(flag == null) flag = "";

		return switch (flag.toLowerCase(Locale.US)) {
			case "true", "on", "yes" -> 1;
			case "false", "off", "no" -> 0;
			default -> 2;
		};
	}

}
