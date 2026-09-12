package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerLaunchpadLambda;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityLaunchpadLambda;
import com.hbm.util.I18nUtil;
import com.hbm.util.SoundUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GUILaunchpadLambda extends GuiInfoContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_launchpad_lambda.png");

	private final TileEntityLaunchpadLambda launcher;

	public GUILaunchpadLambda(InventoryPlayer invPlayer, TileEntityLaunchpadLambda tedf) {
		super(new ContainerLaunchpadLambda(invPlayer, tedf));
		this.launcher = tedf;

		this.xSize = 176;
		this.ySize = 226;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		launcher.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 107, guiTop + 44, 16, 52);
		launcher.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 125, guiTop + 44, 16, 52);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 89, guiTop + 26, 16, 52, launcher.power, TileEntityLaunchpadLambda.maxPower);
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		super.mouseClicked(x, y, button);

		NBTTagCompound data = null;

		if(this.checkClick(x, y, 34, 43, 18, 18)) {
			data = new NBTTagCompound();
			data.setBoolean("auto", false);
		}

		if(this.checkClick(x, y, 52, 43, 18, 18)) {
			data = new NBTTagCompound();
			data.setBoolean("auto", true);
		}

		if(this.checkClick(x, y, 43, 70, 18, 18) && launcher.erected) {
			data = new NBTTagCompound();
			data.setBoolean("launch", true);
		}

		if(data != null) {
			SoundUtil.playClickSound();
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, launcher.getPos()));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 8, this.ySize - 96 + 2, 4210752);

		if(launcher.erected && launcher.countdown > 0) {

			int countdown = launcher.countdown;

			String secs = "" + countdown / 20;
			String cents = "" + (countdown % 20) * 5;
			if(secs.length() == 1) secs = "0" + secs;
			if(cents.length() == 1) cents += "0";

			this.fontRenderer.drawString(secs + ":" + cents, 40, 103, 0xff0000);

		} else if(!launcher.hasRocketLoaded()) {
			drawConstrainedLabel(I18nUtil.resolveKey("desc.gui.soyuz.idle"), 53, 107, 0xff0000, 1F, 22F);
		} else if(!launcher.erected) {
			drawConstrainedLabel(I18nUtil.resolveKey("desc.gui.soyuz.loading"), 53, 107, 0xff8000, 1F, 22F);
		} else {
			drawConstrainedLabel(I18nUtil.resolveKey("desc.gui.soyuz.ready"), 53, 107, 0x00ff00, 1F, 22F);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int power = (int) (launcher.power * 52 / TileEntityLaunchpadLambda.maxPower);
		drawTexturedModalRect(guiLeft + 89, guiTop + 78 - power, 194, 52 - power, 16, power);

		drawTexturedModalRect(guiLeft + 34 + (launcher.autolaunch ? 18 : 0), guiTop + 43, 210 + (launcher.autolaunch ? 18 : 0), 26, 18, 18);

		drawTexturedModalRect(guiLeft + 112, guiTop + 13, launcher.hasJetFuel() ? 210 : 216, 0, 6, 8);
		drawTexturedModalRect(guiLeft + 130, guiTop + 13, launcher.hasOxidizer() ? 210 : 216, 0, 6, 8);
		drawTexturedModalRect(guiLeft + 94, guiTop + 13, launcher.power >= TileEntityLaunchpadLambda.CONSUMPTION ? 210 : 216, 0, 6, 8);

		if(launcher.countdown > 0)
			drawTexturedModalRect(guiLeft + 43, guiTop + 70, 210, 44, 18, 18);

		launcher.tanks[0].renderTank(guiLeft + 107, guiTop + 78, this.zLevel, 16, 52);
		launcher.tanks[1].renderTank(guiLeft + 125, guiTop + 78, this.zLevel, 16, 52);
	}
}
