package com.hbm.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class I18nUtil {

	public static String resolveKey(String s, Object... args) {
		return FMLCommonHandler.instance().getSide().isClient() ? ClientOnly.format(s, args) : s;
	}

	@SideOnly(Side.CLIENT)
	private static class ClientOnly {
		private static String format(String s, Object... args) {
			return I18n.format(s, args);
		}
	}

	public static String[] resolveKeyArray(String s, Object... args) {
		return resolveKey(s, args).split("\\$");
	}

	@SideOnly(Side.CLIENT)
	public static List<String> autoBreak(FontRenderer fontRenderer, String text, int width) {

		List<String> lines = new ArrayList();
		//split the text by all spaces
		String[] words = text.split(" ");

		//add the first word to the first line, no matter what
		lines.add(words[0]);
		//starting indent is the width of the first word
		int indent = fontRenderer.getStringWidth(words[0]);

		for(int w = 1; w < words.length; w++) {

			//increment the indent by the width of the next word + leading space
			indent += fontRenderer.getStringWidth(" " + words[w]);

			//if the indent is within bounds
			if(indent <= width) {
				//add the next word to the last line (i.e. the one in question)
				String last = lines.get(lines.size() - 1);
				lines.set(lines.size() - 1, last += (" " + words[w]));
			} else {
				//otherwise, start a new line and reset the indent
				lines.add(words[w]);
				indent = fontRenderer.getStringWidth(words[w]);
			}
		}

		return lines;
	}
}
