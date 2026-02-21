package com.hbm.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ColorUtil {

    /**
     * Decides whether a color is considered "colorful", i.e. weeds out colors that are too dark or too close to gray.
     * @param hex
     * @return
     */
    public static boolean isColorColorful(int hex) {
        Color color = new Color(hex);

		/*double r = color.getRed();
		double g = color.getBlue();
		double b = color.getGreen();

		if(r < 50 && g < 50 && b < 50)
			return false;

		if(r / g > 1.5) return true;
		if(r / b > 1.5) return true;
		if(g / r > 1.5) return true;
		if(g / b > 1.5) return true;
		if(b / r > 1.5) return true;
		if(b / g > 1.5) return true;*/

        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);

        //     saturation       brightness
        return hsb[1] > 0.25 && hsb[2] > 0.25;
    }

    /**
     * Raises the highest RGB component to the specified limit, scaling the other components with it.
     * @param hex
     * @param limit
     * @return
     */
    public static int amplifyColor(int hex, int limit) {
        Color color = new Color(hex);
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int max = Math.max(Math.max(1, r), Math.max(g, b));

        r = r * limit / max;
        g = g * limit / max;
        b = b * limit / max;

        return new Color(r, g, b).getRGB();
    }

    /**
     * Same as the regular amplifyColor but it uses 255 as the limit.
     * @param hex
     * @return
     */
    public static int amplifyColor(int hex) {
        return amplifyColor(hex, 255);
    }

    /**
     * Amplifies a given color by approaching all components to maximum by a given percentage. A percentage of 1 (100%) should always yield white.
     * @param hex
     * @param percent
     * @return
     */
    public static int lightenColor(int hex, double percent) {
        Color color = new Color(hex);
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        r = (int) (r + (255 - r) * percent);
        g = (int) (g + (255 - g) * percent);
        b = (int) (b + (255 - b) * percent);

        return new Color(r, g, b).getRGB();
    }

    /** Converts a color into HSB and then returns the brightness component [] */
    public static double getColorBrightness(int hex) {
        Color color = new Color(hex);
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
        return hsb[2];
    }

    public static HashMap<String, Integer> nameToColor = new HashMap() {{
        put("black", 1973019);
        put("red", 11743532);
        put("green", 3887386);
        put("brown", 5320730);
        put("blue", 2437522);
        put("purple", 8073150);
        put("cyan", 2651799);
        put("silver", 11250603);
        put("gray", 4408131);
        put("pink", 14188952);
        put("lime", 4312372);
        put("yellow", 14602026);
        put("lightBlue", 6719955);
        put("magenta", 12801229);
        put("orange", 15435844);
        put("white", 15790320);
    }};

    public static int getColorFromDye(ItemStack stack) {
        List<String> oreNames = ItemStackUtil.getOreDictNames(stack);

        for(String dict : oreNames) {
            if(dict.length() > 3 && dict.startsWith("dye")) {
                String color = dict.substring(3).toLowerCase(Locale.US);
                if(nameToColor.containsKey(color)) return nameToColor.get(color);
            }
        }

        return 0;
    }

    @SideOnly(Side.CLIENT)
    public static int getAverageColorFromStack(ItemStack stack) {
        if (stack.isEmpty()) return 0xFFFFFF;
        try {
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack).getParticleTexture();
            if (sprite.getFrameCount() == 0) return 0xFFFFFF;
            int[][] frameData = sprite.getFrameTextureData(0);
            int lowestMipmapLevel = frameData.length - 1;
            if (lowestMipmapLevel < 0) return 0xFFFFFF;
            int[] smallestMipmap = frameData[lowestMipmapLevel];
            if (smallestMipmap.length == 0) return 0xFFFFFF;
            int avgColorARGB = smallestMipmap[0];
            return avgColorARGB & 0x00FFFFFF;
        } catch (Exception ex) {
            return 0xFFFFFF;
        }
    }

    public static int ir(int color) {
        return (color & 0xff0000) >> 16;
    }

    public static int ig(int color) {
        return (color & 0x00ff00) >> 8;
    }

    public static int ib(int color) {
        return (color & 0x0000ff) >> 0;
    }

    public static float fr(int color) {
        return ir(color) / 255F;
    }

    public static float fg(int color) {
        return ig(color) / 255F;
    }

    public static float fb(int color) {
        return ib(color) / 255F;
    }
}
