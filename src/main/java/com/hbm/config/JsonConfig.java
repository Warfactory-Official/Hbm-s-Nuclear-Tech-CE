package com.hbm.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.main.MainRegistry;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class JsonConfig {
	public static final Gson gson = new Gson();

	private static File getFile(String filename){
		return new File(MainRegistry.proxy.getDataDir().getPath() + "/config/hbm" + File.separatorChar + filename);
	}

    public static boolean isFileNonexistent(String filename) {
        return !getFile(filename).exists();
    }

	public static JsonWriter startWriting(String filename){
		try{
			JsonWriter writer = new JsonWriter(new FileWriter(getFile(filename)));
			writer.setIndent("  ");
			writer.beginObject();
			return writer;
		} catch(Exception _) { }
		return null;
	}

	public static void stopWriting(JsonWriter writer){
		try{
			writer.endObject();
			writer.close();
		} catch(Exception _) {	}
	}

	public static JsonObject startReading(String filename){
		try{
			File file = getFile(filename);
			if(file.exists())
				return gson.fromJson(new FileReader(file), JsonObject.class);
			return null;
		} catch(Exception _) { }
		return null;
	}
}
