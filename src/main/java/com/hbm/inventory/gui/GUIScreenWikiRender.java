package com.hbm.inventory.gui;

import com.hbm.main.MainRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.ItemStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.util.function.Function;

public class GUIScreenWikiRender extends GuiScreen {

	// Basically the same thing as GUIScreenPreview, but will iterate through all provided preview stacks
	// taking a screenshot of each, as fast as the game can render them

	protected ItemStack[] preview;
	protected int index = 0;
	protected int scale = 1;
	protected String saveLocation = "wiki-screenshots";
	protected String prefix = "";

	protected Function<ItemStack, String> getStackName = ItemStack::getDisplayName;

	public GUIScreenWikiRender(ItemStack[] stacks, String prefix, String directory, int scale) {
		this.preview = stacks;
		this.prefix = prefix;
		this.saveLocation = directory;
		this.scale = scale;
	}

	public GUIScreenWikiRender(ItemStack[] stacks, String prefix, String directory, int scale, Function<ItemStack, String> getStackName) {
		this(stacks, prefix, directory, scale);
		this.getStackName = getStackName;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		GuiScreen.drawRect(0, 0, this.width, this.height, 0xFFFF00FF);

		// Once we've reached the end of the array, immedaitely close this GUI
		if(index >= preview.length) {
			this.mc.player.closeScreen();
			return;
		}

		GlStateManager.disableLighting();
		this.drawGuiContainerForegroundLayer(preview[index]);
		GlStateManager.enableLighting();

		ScaledResolution res = new ScaledResolution(this.mc);
		int zoom = scale * res.getScaleFactor();

		try {
			String slotName = getStackName.apply(preview[index]).replaceAll("§.", "").replaceAll("[^\\w ().-]+", "");
			if(!slotName.endsWith(".name")) {
				saveScreenshot(Minecraft.getMinecraft().gameDir, saveLocation, prefix + slotName + ".png", zoom, zoom, zoom * 16, zoom * 16, 0xFFFF00FF);
			}
		} catch (Exception ex) {
			// Just skip any failures caused by display name or rendering
		}

		index++;
	}

	protected void drawGuiContainerForegroundLayer(ItemStack preview) {
		if(preview == null || preview.isEmpty()) return;

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.enableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.enableDepth();

		GlStateManager.scale(scale, scale, scale);

		ScaledResolution res = new ScaledResolution(this.mc);
		GlStateManager.translate(9D, res.getScaledHeight_double() / scale - 9D, -200D);

		this.zLevel = 200.0F;
		itemRender.zLevel = 200.0F;

		GlStateManager.enableDepth();
		itemRender.renderItemAndEffectIntoGUI(preview, -8, -8);
		itemRender.renderItemOverlayIntoGUI(this.fontRenderer, preview, -8, -8, null);

		itemRender.zLevel = 0.0F;
		this.zLevel = 0.0F;

		GlStateManager.popMatrix();
	}

	private static IntBuffer pixelBuffer;
	private static int[] pixelValues;

	// This implementation is based directly on ScreenShotHelper.saveScreenshot()
	// But allows for defining a rect where you want to sample pixels from
	public static void saveScreenshot(File dataDir, String ssDir, String fileName, int x, int y, int width, int height, int transparentColor) {
		try {
			File screenshotDirectory = new File(dataDir, ssDir);
			screenshotDirectory.mkdir();

			int bufferSize = width * height;
			if(pixelBuffer == null || pixelBuffer.capacity() < bufferSize) {
				pixelBuffer = BufferUtils.createIntBuffer(bufferSize);
				pixelValues = new int[bufferSize];
			}

			GlStateManager.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
			GlStateManager.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
			pixelBuffer.clear();
			GlStateManager.glReadPixels(x, y, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);

			pixelBuffer.get(pixelValues);
			TextureUtil.processPixelValues(pixelValues, width, height);
			BufferedImage imageBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			imageBuffer.setRGB(0, 0, width, height, pixelValues, 0, width);

			// This is the only proper custom part, setting the background of an inventory slot to be transparent
			if(transparentColor != 0) {
				for(int iy = 0; iy < imageBuffer.getHeight(); ++iy) {
					for(int ix = 0; ix < imageBuffer.getWidth(); ++ix) {
						if(imageBuffer.getRGB(ix, iy) == transparentColor) {
							imageBuffer.setRGB(ix, iy, 0);
						}
					}
				}
			}

			if(fileName == null) {
				throw new IllegalArgumentException("fileName must not be null");
			}

			ImageIO.write(imageBuffer, "png", new File(screenshotDirectory, fileName));
		} catch (Exception ex) {
			MainRegistry.logger.warn("Failed to save NTM screenshot", ex);
		}
	}
}
