package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.rbmk.RBMKColumn;
import com.hbm.tileentity.machine.rbmk.RBMKColumn.ColumnType;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GUIRBMKConsole extends GuiScreen {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/reactors/gui_rbmk_console.png");
	private final TileEntityRBMKConsole console;
	protected int guiLeft;
	protected int guiTop;
	protected int xSize;
	protected int ySize;

	private boolean[] selection = new boolean[15 * 15];
	private boolean az5Lid = true;
	private long lastPress = 0;

	private GuiTextField field;

	public GUIRBMKConsole(InventoryPlayer invPlayer, TileEntityRBMKConsole console) {
		super();
		this.console = console;

		this.xSize = 244;
		this.ySize = 172;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		Keyboard.enableRepeatEvents(true);

		this.field = new GuiTextField(0, this.fontRenderer, guiLeft + 9, guiTop + 84, 35, 9);
		this.field.setTextColor(0x00ff00);
		this.field.setDisabledTextColour(0x008000);
		this.field.setEnableBackgroundDrawing(false);
		this.field.setMaxStringLength(3);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

		final int bX = 86;
		final int bY = 11;
		final int size = 10;

		if (guiLeft + 86 <= mouseX && guiLeft + 86 + 150 > mouseX && guiTop + 11 < mouseY && guiTop + 11 + 10150 >= mouseY) {
			int index = ((mouseX - bX - guiLeft) / size + (mouseY - bY - guiTop) / size * 15);

			if (index > 0 && index < console.columns.length) {
				RBMKColumn col = console.columns[index];

				if (col != null) {
					List<String> list = new ArrayList<>();
					list.add(col.type.toString());
					list.addAll(col.getFancyStats());
					this.drawHoveringText(list, mouseX, mouseY);
				}
			}
		}

		// helpers
		drawCustomInfoStat(mouseX, mouseY, guiLeft + 61, guiTop + 70, 10, 10, mouseX, mouseY, new String[]{"Select all control rods"});
		drawCustomInfoStat(mouseX, mouseY, guiLeft + 72, guiTop + 70, 10, 10, mouseX, mouseY, new String[]{"Deselect all"});

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				int id = i * 2 + j + 1;
				drawCustomInfoStat(mouseX, mouseY, guiLeft + 6 + 40 * j, guiTop + 8 + 21 * i, 18, 18, mouseX, mouseY,
						new String[]{"§e" + I18nUtil.resolveKey("rbmk.console." + console.screens[id - 1].type.name().toLowerCase(Locale.US), id)});
				drawCustomInfoStat(mouseX, mouseY, guiLeft + 24 + 40 * j, guiTop + 8 + 21 * i, 18, 18, mouseX, mouseY,
						new String[]{I18nUtil.resolveKey("rbmk.console.assign", id)});
			}
		}

		drawCustomInfoStat(mouseX, mouseY, guiLeft + 6, guiTop + 70, 10, 10, mouseX, mouseY, new String[]{"§cLeft click: Select red group", "§cRight click: Assign red group"});
		drawCustomInfoStat(mouseX, mouseY, guiLeft + 17, guiTop + 70, 10, 10, mouseX, mouseY, new String[]{"§eLeft click: Select yellow group", "§eRight click: Assign yellow group"});
		drawCustomInfoStat(mouseX, mouseY, guiLeft + 28, guiTop + 70, 10, 10, mouseX, mouseY, new String[]{"§aLeft click: Select green group", "§aRight click: Assign green group"});
		drawCustomInfoStat(mouseX, mouseY, guiLeft + 39, guiTop + 70, 10, 10, mouseX, mouseY, new String[]{"§9Left click: Select blue group", "§9Right click: Assign blue group"});
		drawCustomInfoStat(mouseX, mouseY, guiLeft + 50, guiTop + 70, 10, 10, mouseX, mouseY, new String[]{"§dLeft click: Select purple group", "§dRight click: Assign purple group"});

		drawCustomInfoStat(mouseX, mouseY, guiLeft + 70, guiTop + 82, 12, 12, mouseX, mouseY, new String[]{"Cycle steam channel compressor setting"});
	}

	public void drawCustomInfoStat(int mouseX, int mouseY, int x, int y, int width, int height, int tPosX, int tPosY, String[] text) {
		if (x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY)
			this.drawHoveringText(Arrays.asList(text), tPosX, tPosY);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
		super.mouseClicked(mouseX, mouseY, button);
		this.field.mouseClicked(mouseX, mouseY, button);

		final int LEFT_CLICK = 0;
		final int RIGHT_CLICK = 1;

		final int bX = 86;
		final int bY = 11;
		final int size = 10;

		// toggle column selection
		if (guiLeft + 86 <= mouseX && guiLeft + 86 + 150 > mouseX && guiTop + 11 < mouseY && guiTop + 11 + 150 >= mouseY) {
			int index = ((mouseX - bX - guiLeft) / size + (mouseY - bY - guiTop) / size * 15);

			if (index >= 0 && index < selection.length && console.columns[index] != null) {
				this.selection[index] = !this.selection[index];

				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.75F + (this.selection[index] ? 0.25F : 0.0F)));
				return;
			}
		}

		// clear selection
		if (guiLeft + 72 <= mouseX && guiLeft + 72 + 10 > mouseX && guiTop + 70 < mouseY && guiTop + 70 + 10 >= mouseY) {
			this.selection = new boolean[15 * 15];
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.5F));
			return;
		}

		// select all control rods
		if (guiLeft + 61 <= mouseX && guiLeft + 61 + 10 > mouseX && guiTop + 70 < mouseY && guiTop + 70 + 10 >= mouseY) {
			this.selection = new boolean[15 * 15];

			for (int j = 0; j < console.columns.length; j++) {
				if (console.columns[j] != null && console.columns[j].type == ColumnType.CONTROL) {
					this.selection[j] = true;
				}
			}
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.5F));
			return;
		}

		// compressor toggle button
		if (guiLeft + 70 <= mouseX && guiLeft + 70 + 12 > mouseX && guiTop + 82 < mouseY && guiTop + 82 + 12 >= mouseY) {
			NBTTagCompound control = new NBTTagCompound();
			control.setBoolean("compressor", true);
			List<Integer> ints = new ArrayList<>();
			for (int j = 0; j < console.columns.length; j++) {
				if (console.columns[j] != null && console.columns[j].type == ColumnType.BOILER && this.selection[j]) {
					ints.add(j);
				}
			}
			int[] cols = new int[ints.size()];
			for (int i = 0; i < cols.length; i++) cols[i] = ints.get(i);
			control.setIntArray("cols", cols);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(control, console.getPos()));
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1F));
			return;
		}

		// color groups
		for (int k = 0; k < 5; k++) {
			if (guiLeft + 6 + k * 11 <= mouseX && guiLeft + 6 + k * 11 + 10 > mouseX && guiTop + 70 < mouseY && guiTop + 70 + 10 >= mouseY) {
				if (button == LEFT_CLICK) {
					this.selection = new boolean[15 * 15];

					for (int j = 0; j < console.columns.length; j++) {
						if (console.columns[j] != null && console.columns[j].type == ColumnType.CONTROL && ((RBMKColumn.ControlColumn) console.columns[j]).color == k) {
							this.selection[j] = true;
						}
					}
				} else if (button == RIGHT_CLICK) {
					NBTTagCompound control = new NBTTagCompound();
					control.setByte("assignColor", (byte) k);
					List<Integer> ints = new ArrayList<>();
					for (int j = 0; j < console.columns.length; j++) {
						if (console.columns[j] != null && console.columns[j].type == ColumnType.CONTROL && this.selection[j]) {
							ints.add(j);
						}
					}
					int[] cols = new int[ints.size()];
					for (int i = 0; i < cols.length; i++) cols[i] = ints.get(i);
					control.setIntArray("cols", cols);
                    PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(control, console.getPos()));
                }

				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.8F + k * 0.1F));
				return;
			}
		}

		// AZ-5
		if (guiLeft + 30 <= mouseX && guiLeft + 30 + 28 > mouseX && guiTop + 138 < mouseY && guiTop + 138 + 28 >= mouseY) {
			if (az5Lid) {
				az5Lid = false;
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(HBMSoundHandler.rbmk_az5_cover, 0.5F));
			} else if (lastPress + 3000 < System.currentTimeMillis()) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(HBMSoundHandler.shutdown, 1));
				lastPress = System.currentTimeMillis();

				NBTTagCompound control = new NBTTagCompound();
				control.setDouble("level", 0);

				for (int j = 0; j < console.columns.length; j++) {
					if (console.columns[j] != null && console.columns[j].type == ColumnType.CONTROL)
						control.setInteger("sel_" + j, j);
				}

                PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(control, console.getPos()));
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1F));
			}
			return;
		}

		// save control rod setting
		if (guiLeft + 48 <= mouseX && guiLeft + 48 + 12 > mouseX && guiTop + 82 < mouseY && guiTop + 82 + 12 >= mouseY) {

			double level;

			if (NumberUtils.isCreatable(field.getText())) {
				int j = (int) MathHelper.clamp(Double.parseDouble(field.getText()), 0, 100);
				field.setText(j + "");
				level = j * 0.01D;
			} else {
				return;
			}

			NBTTagCompound control = new NBTTagCompound();
			control.setDouble("level", level);

			for (int j = 0; j < selection.length; j++) {
				if (selection[j])
					control.setInteger("sel_" + j, j);
			}

            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(control, console.getPos()));
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1F));
			return;
		}

		// submit selection for status screen (toggle / assign)
		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 2; k++) {

				int slot = j * 2 + k; // 0..5

				// toggle type
				if (guiLeft + 6 + 40 * k <= mouseX && guiLeft + 6 + 40 * k + 18 > mouseX && guiTop + 8 + 21 * j < mouseY && guiTop + 8 + 21 * j + 18 >= mouseY) {
					NBTTagCompound control = new NBTTagCompound();
					control.setByte("toggle", (byte) slot);
                    PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(control, console.getPos()));
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.5F));
					return;
				}

				// assign columns to screen
				if (guiLeft + 24 + 40 * k <= mouseX && guiLeft + 24 + 40 * k + 18 > mouseX && guiTop + 8 + 21 * j < mouseY && guiTop + 8 + 21 * j + 18 >= mouseY) {

					NBTTagCompound control = new NBTTagCompound();
					control.setByte("id", (byte) slot);

					for (int s = 0; s < selection.length; s++) {
						if (selection[s]) {
							control.setBoolean("s" + s, true);
						}
					}

                    PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(control, console.getPos()));
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.75F));
					return;
				}
			}
		}
	}

	protected void drawGuiContainerBackgroundLayer(float interp, int mX, int mY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if (az5Lid) {
			drawTexturedModalRect(guiLeft + 30, guiTop + 138, 228, 172, 28, 28);
		} else if(lastPress + 3000 >= System.currentTimeMillis()){
			// mlbv: "pressed" button, 1.12.2 exclusive
			drawTexturedModalRect(guiLeft + 30, guiTop + 136, 228, 228, 28, 28);
		}

		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 2; k++) {
				int slot = j * 2 + k;
				drawTexturedModalRect(guiLeft + 6 + 40 * k, guiTop + 8 + 21 * j, this.console.screens[slot].type.offset, 238, 18, 18);
			}
		}

		final int bX = 86;
		final int bY = 11;
		final int size = 10;

		// draw 15x15 grid
		for (int i = 0; i < console.columns.length; i++) {

			RBMKColumn col = console.columns[i];
			if (col == null) continue;

			int x = bX + size * (i % 15);
			int y = bY + size * (i / 15);

			int tX = col.type.offset;
			int tY = 172;

			drawTexturedModalRect(guiLeft + x, guiTop + y, tX, tY, size, size);


			int h = (int) Math.ceil((col.heat - 20) * 10 / Math.max(col.maxHeat, 1.0D));
			if (h < 0) h = 0;
			if (h > 10) h = 10;
			drawTexturedModalRect(guiLeft + x, guiTop + y + size - h, 0, 192 - h, 10, h);

			switch (col.type) {
				case ABSORBER:
				case BLANK:
				case MODERATOR:
				case REFLECTOR:
				case OUTGASSER:
				case BREEDER:
					break;
				case COOLER:
					RBMKColumn.CoolerColumn cooler = (RBMKColumn.CoolerColumn) col;
					int cryo = (int) Math.ceil(cooler.cryo * 8 / 16000);
					if (cryo > 0)
						drawTexturedModalRect(guiLeft + x + 3, guiTop + y + size - cryo - 1, 123, 191 - cryo, 4, cryo);
					break;
				case CONTROL:
					RBMKColumn.ControlColumn control = (RBMKColumn.ControlColumn) col;
					int color = control.color;
					if (color > -1)
						drawTexturedModalRect(guiLeft + x, guiTop + y, color * size, 202, 10, 10);
				case CONTROL_AUTO:
					RBMKColumn.ControlColumn controlAuto = (RBMKColumn.ControlColumn) col;
					int fr = 8 - (int) Math.ceil((controlAuto.level * 8));
					if (fr < 0) fr = 0;
					if (fr > 8) fr = 8;
					drawTexturedModalRect(guiLeft + x + 4, guiTop + y + 1, 24, 183, 2, fr);
					break;

				case FUEL:
				case FUEL_SIM:
					RBMKColumn.FuelColumn fuel = (RBMKColumn.FuelColumn) col;
					int fh = (int) Math.ceil((fuel.c_heat - 20) * 8 / Math.max(fuel.c_maxHeat, 1.0D));
					if (fh < 0) fh = 0;
					if (fh > 8) fh = 8;
					drawTexturedModalRect(guiLeft + x + 1, guiTop + y + size - fh - 1, 11, 191 - fh, 2, fh);

					int fe = (int) Math.ceil((fuel.enrichment) * 8);
					if (fe < 0) fe = 0;
					if (fe > 8) fe = 8;
					drawTexturedModalRect(guiLeft + x + 4, guiTop + y + size - fe - 1, 14, 191 - fe, 2, fe);

					int fx = (int) Math.ceil((fuel.xenon) * 8 / 100.0D);
					if (fx < 0) fx = 0;
					if (fx > 8) fx = 8;
					drawTexturedModalRect(guiLeft + x + 7, guiTop + y + size - fx - 1, 17, 191 - fx, 2, fx);
					break;

				case BOILER:
					RBMKColumn.BoilerColumn boiler = (RBMKColumn.BoilerColumn) col;
					int fw = (int) Math.ceil((boiler.water) * 8 / Math.max(boiler.maxWater, 1.0D));
					if (fw < 0) fw = 0;
					if (fw > 8) fw = 8;
					drawTexturedModalRect(guiLeft + x + 1, guiTop + y + size - fw - 1, 41, 191 - fw, 3, fw);

					int fs = (int) Math.ceil((boiler.steam) * 8 / Math.max(boiler.maxSteam, 1.0D));
					if (fs < 0) fs = 0;
					if (fs > 8) fs = 8;
					drawTexturedModalRect(guiLeft + x + 6, guiTop + y + size - fs - 1, 46, 191 - fs, 3, fs);

					short type = boiler.steamType;
					if (Fluids.fromID(type) == Fluids.STEAM)
						drawTexturedModalRect(guiLeft + x + 4, guiTop + y + 1, 44, 183, 2, 2);
					if (Fluids.fromID(type) == Fluids.HOTSTEAM)
						drawTexturedModalRect(guiLeft + x + 4, guiTop + y + 3, 44, 185, 2, 2);
					if (Fluids.fromID(type) == Fluids.SUPERHOTSTEAM)
						drawTexturedModalRect(guiLeft + x + 4, guiTop + y + 5, 44, 187, 2, 2);
					if (Fluids.fromID(type) == Fluids.ULTRAHOTSTEAM)
						drawTexturedModalRect(guiLeft + x + 4, guiTop + y + 7, 44, 189, 2, 2);
					break;

				case HEATEX:
					RBMKColumn.HeaterColumn heater = (RBMKColumn.HeaterColumn) col;
					int cc = (int) Math.ceil((heater.water) * 8 / Math.max(heater.maxWater, 1.0D));
					if (cc < 0) cc = 0;
					if (cc > 8) cc = 8;
					drawTexturedModalRect(guiLeft + x + 1, guiTop + y + size - cc - 1, 131, 191 - cc, 3, cc);

					int hc = (int) Math.ceil((heater.steam) * 8 / Math.max(heater.maxSteam, 1.0D));
					if (hc < 0) hc = 0;
					if (hc > 8) hc = 8;
					drawTexturedModalRect(guiLeft + x + 6, guiTop + y + size - hc - 1, 136, 191 - hc, 3, hc);
					break;
			}

			if (this.selection[i])
				drawTexturedModalRect(guiLeft + x, guiTop + y, 0, 192, 10, 10);
		}

		int highest = Integer.MIN_VALUE;
		int lowest = Integer.MAX_VALUE;

		for (int v : console.fluxBuffer) {
			if (v > highest) highest = v;
			if (v < lowest) lowest = v;
		}

		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.color(0f, 1f, 0f, 1f);
		GL11.glLineWidth(2F);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

		int range = highest - lowest;
		for (int k = 0; k < console.fluxBuffer.length; k++) {
			int flux = console.fluxBuffer[k];
			double x = guiLeft + 7 + k * 74D / console.fluxBuffer.length;
			double y = guiTop + 127 - (flux - lowest) * 24D / Math.max(range, 1);
			buf.pos(x, y, this.zLevel + 10).endVertex();
		}

		tess.draw();
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GlStateManager.enableTexture2D();

		GlStateManager.pushMatrix();
		double scale = 0.5D;
		GlStateManager.scale(scale, scale, 1);
		this.fontRenderer.drawString(highest + "", (int) ((guiLeft + 8) / scale), (int) ((guiTop + 98) / scale), 0x00ff00);
		this.fontRenderer.drawString(highest + "", (int) ((guiLeft + 80 - this.fontRenderer.getStringWidth(highest + "") * scale) / scale), (int) ((guiTop + 98) / scale), 0x00ff00);
		this.fontRenderer.drawString(lowest + "", (int) ((guiLeft + 8) / scale), (int) ((guiTop + 133 - this.fontRenderer.FONT_HEIGHT * scale) / scale), 0x00ff00);
		this.fontRenderer.drawString(lowest + "", (int) ((guiLeft + 80 - this.fontRenderer.getStringWidth(lowest + "") * scale) / scale), (int) ((guiTop + 133 - this.fontRenderer.FONT_HEIGHT * scale) / scale), 0x00ff00);
		GlStateManager.popMatrix();

		this.field.drawTextBox();
	}

	@Override
	protected void keyTyped(char c, int keyCode) throws IOException {

		if (this.field.textboxKeyTyped(c, keyCode))
			return;

		if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
			this.mc.player.closeScreen();
			return;
		}

		super.keyTyped(c, keyCode);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}
}
