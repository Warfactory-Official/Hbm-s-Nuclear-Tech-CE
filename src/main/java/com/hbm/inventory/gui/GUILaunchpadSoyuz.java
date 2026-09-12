package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerLaunchpadSoyuz;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityLaunchpadSoyuz;
import com.hbm.tileentity.machine.TileEntityLaunchpadSoyuz.SoyuzStatus;
import com.hbm.util.I18nUtil;
import com.hbm.util.SoundUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GUILaunchpadSoyuz extends GuiInfoContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_launchpad_soyuz.png");

	private final TileEntityLaunchpadSoyuz pad;

	public GUILaunchpadSoyuz(InventoryPlayer invPlayer, TileEntityLaunchpadSoyuz pad) {
		super(new ContainerLaunchpadSoyuz(invPlayer, pad));
		this.pad = pad;

		this.xSize = 194;
		this.ySize = 244;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.renderHoveredToolTip(mouseX, mouseY);

		pad.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 44, 16, 52);
		pad.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 170, guiTop + 44, 16, 52);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 44, 16, 52, pad.power, TileEntityLaunchpadSoyuz.maxPower);

		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 53, 16, 16, guiLeft - 8, guiTop + 53 + 16, I18nUtil.resolveKeyArray("desc.gui.soyuz.desc"));
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 79, guiTop + 52, 18, 18, mouseX, mouseY, I18nUtil.resolveKeyArray("desc.gui.soyuz.cargo"));
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 97, guiTop + 52, 18, 18, mouseX, mouseY, I18nUtil.resolveKeyArray("desc.gui.soyuz.satellite"));
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		super.mouseClicked(x, y, button);

		NBTTagCompound data = null;

		if(this.checkClick(x, y, 79, 52, 18, 18)) {
			data = new NBTTagCompound();
			data.setBoolean("cargo", true);
		}

		if(this.checkClick(x, y, 97, 52, 18, 18)) {
			data = new NBTTagCompound();
			data.setBoolean("cargo", false);
		}

		if(this.checkClick(x, y, 88, 97, 18, 18) && pad.soyuzStatus == SoyuzStatus.READY) {
			data = new NBTTagCompound();
			data.setBoolean("launch", true);
		}

		if(data != null) {
			SoundUtil.playClickSound();
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, pad.getPos()));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.pad.getDisplayName().getUnformattedText();

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 17, this.ySize - 96 + 2, 4210752);

		if(pad.soyuzStatus == SoyuzStatus.LAUNCHING) {

			int countdown = pad.countdown;

			String secs = "" + countdown / 20;
			String cents = "" + (countdown % 20) * 5;
			if(secs.length() == 1) secs = "0" + secs;
			if(cents.length() == 1) cents += "0";

			this.fontRenderer.drawString(secs + ":" + cents, 85, 121, 0xff0000);

		} else if(pad.soyuzStatus == SoyuzStatus.ABSENT) {
			drawConstrainedLabel(I18nUtil.resolveKey("desc.gui.soyuz.idle"), 97, 125, 0xff0000, 1F, 22F);
		} else if(pad.soyuzStatus == SoyuzStatus.LOADING) {
			drawConstrainedLabel(I18nUtil.resolveKey("desc.gui.soyuz.loading"), 97, 125, 0xff8000, 1F, 22F);
		} else if(pad.soyuzStatus == SoyuzStatus.FUELING) {
			drawConstrainedLabel(I18nUtil.resolveKey("desc.gui.soyuz.fueling"), 97, 125, 0xffff00, 1F, 22F);
		} else if(pad.soyuzStatus == SoyuzStatus.READY) {
			drawConstrainedLabel(I18nUtil.resolveKey("desc.gui.soyuz.ready"), 97, 125, 0x00ff00, 1F, 22F);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int power = (int) (pad.power * 52 / TileEntityLaunchpadSoyuz.maxPower);
		drawTexturedModalRect(guiLeft + 134, guiTop + 96 - power, 194, 52 - power, 16, power);

		drawTexturedModalRect(guiLeft + 97 - (pad.cargoMode ? 18 : 0), guiTop + 52, 228 - (pad.cargoMode ? 18 : 0), 26, 18, 18);

		drawTexturedModalRect(guiLeft + 157, guiTop + 31, pad.hasJetFuel() ? 210 : 216, 0, 6, 8);
		drawTexturedModalRect(guiLeft + 175, guiTop + 31, pad.hasOxidizer() ? 210 : 216, 0, 6, 8);
		drawTexturedModalRect(guiLeft + 139, guiTop + 31, pad.power >= TileEntityLaunchpadSoyuz.CONSUMPTION ? 210 : 216, 0, 6, 8);

		int orbital = pad.orbital();
		if(orbital > 0) drawTexturedModalRect(guiLeft + 79, guiTop + 25, 210 + (orbital - 1) * 18, 8, 18, 18);

		if(pad.soyuzStatus == SoyuzStatus.LAUNCHING)
			drawTexturedModalRect(guiLeft + 88, guiTop + 97, 210, 44, 18, 18);

		pad.tanks[0].renderTank(guiLeft + 152, guiTop + 96, this.zLevel, 16, 52);
		pad.tanks[1].renderTank(guiLeft + 170, guiTop + 96, this.zLevel, 16, 52);

		this.drawInfoPanel(guiLeft - 16, guiTop + 53, 16, 16, 2);
	}
}
