package com.hbm.inventory.gui;

import com.hbm.blocks.machine.BlockPWR;
import com.hbm.blocks.machine.BlockPWR.TileEntityBlockPWR;
import com.hbm.main.MainRegistry;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

public class GUIScreenSlicePrinter extends GuiScreen {

	private final int x1, y1, z1;
	private final int x2, y2, z2;
	private final int sizeX, sizeY, sizeZ;
	private final EnumFacing dir;

	private HashSet<Block> whitelist;

	private int yIndex;

	private BlockRendererDispatcher renderer;

	private final String dirname;
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

	public GUIScreenSlicePrinter(int x1, int y1, int z1, int x2, int y2, int z2, EnumFacing dir) {
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.z1 = Math.min(z1, z2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
		this.z2 = Math.max(z1, z2);

		this.dir = dir;

		this.sizeX = this.x2 - this.x1 + 1;
		this.sizeY = this.y2 - this.y1 + 1;
		this.sizeZ = this.z2 - this.z1 + 1;

		this.dirname = dateFormat.format(new Date());
	}

	public GUIScreenSlicePrinter(int x1, int y1, int z1, int x2, int y2, int z2, EnumFacing dir, HashSet<Block> whitelist) {
		this(x1, y1, z1, x2, y2, z2, dir);
		this.whitelist = whitelist;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		if(renderer == null) {
			this.renderer = mc.getBlockRendererDispatcher();
		}

		GuiScreen.drawRect(0, 0, width, height, 0xFFFF00FF);

		if(yIndex >= sizeY) {
			mc.player.sendMessage(new TextComponentString("Slices saved to: .minecraft/printer/" + dirname));
			mc.player.closeScreen();
			return;
		}

		GlStateManager.pushMatrix();
		{
			setupRotation();

			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			BufferBuilder buffer = NTMImmediate.INSTANCE.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

			for(int x = 0; x < sizeX; x++) {
				for(int z = 0; z < sizeZ; z++) {
					BlockPos pos = new BlockPos(x1 + x, y1 + yIndex, z1 + z);
					IBlockState state = mc.world.getBlockState(pos);
					if(whitelist != null && !whitelist.contains(state.getBlock())) continue;

					if(state.getBlock() instanceof BlockPWR) {
						TileEntity tile = mc.world.getTileEntity(pos);
						if(tile instanceof TileEntityBlockPWR) {
							TileEntityBlockPWR pwr = (TileEntityBlockPWR) tile;
							if(pwr.originalBlockState != null) {
								state = pwr.originalBlockState;
							}
						}
					}

					if(state.getRenderType() != EnumBlockRenderType.MODEL) continue;

					int dx = x;
					int dz = z;

					if(dir == EnumFacing.WEST) {
						dx = sizeZ - 1 - z;
						dz = x;
					} else if(dir == EnumFacing.SOUTH) {
						dx = sizeX - 1 - x;
						dz = sizeZ - 1 - z;
					} else if(dir == EnumFacing.EAST) {
						dx = z;
						dz = sizeX - 1 - x;
					}

					buffer.setTranslation(dx - pos.getX(), -pos.getY(), dz - pos.getZ());
					renderer.renderBlock(state, pos, mc.world, buffer);
				}
			}

			buffer.setTranslation(0, 0, 0);
			NTMImmediate.INSTANCE.draw();
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}
		GlStateManager.popMatrix();

		File printerDir = new File(mc.gameDir, "printer");
		printerDir.mkdir();

		saveScreenshot(printerDir, dirname, "slice_" + yIndex + ".png", 0, 0, mc.displayWidth, mc.displayHeight, 0xFFFF00FF);

		yIndex++;
	}

	private void setupRotation() {
		double scale = -24;

		GlStateManager.translate(width / 2, height / 2 - 36, 400);
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.scale(1, 1, 0.5);

		GlStateManager.rotate(-30, 1, 0, 0);
		GlStateManager.rotate(225, 0, 1, 0);

		if(dir == EnumFacing.WEST || dir == EnumFacing.EAST) {
			GlStateManager.translate(sizeX / -2D, -sizeY / 2D, sizeZ / -2D);
		} else {
			GlStateManager.translate(sizeZ / -2D, -sizeY / 2D, sizeX / -2D);
		}
	}

	private static IntBuffer pixelBuffer;
	private static int[] pixelValues;

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
		} catch(Exception ex) {
			MainRegistry.logger.warn("Failed to save NTM screenshot", ex);
		}
	}
}
