package com.hbm.handler;

import com.google.common.io.Files;
import net.minecraft.util.math.MathHelper;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Identity {

	public static int value = -1;
	public static final int FALLBACK = 666;
	public static final int MIN = 0;
	public static final int MAX = 65_535;
	public static final String FILE_NAME = "identity";

	public static void init(File dir) {

		File idFile = new File(dir, FILE_NAME);

		if(idFile.exists() && idFile.isFile()) {
			try {
				String line = Files.readFirstLine(idFile, StandardCharsets.US_ASCII);
				value = MathHelper.clamp(Integer.parseInt(line.trim()), MIN, MAX);
			} catch(Throwable _) { }
		}

		if(value == -1) {
			try {
				PrintWriter printer = new PrintWriter(idFile, StandardCharsets.US_ASCII);
				int newValue = new Random().nextInt(MAX + 1);
				printer.write(newValue + "");
				printer.close();
				value = newValue;
			} catch(Throwable _) { }
		}

		if(value == -1) value = FALLBACK;
	}
}
