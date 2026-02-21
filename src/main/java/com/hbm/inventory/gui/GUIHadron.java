package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerHadron;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.machine.TileEntityHadron;
import com.hbm.tileentity.machine.TileEntityHadron.EnumHadronState;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIHadron extends GuiInfoContainer {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_hadron.png");
	private TileEntityHadron hadron;

	public GUIHadron(InventoryPlayer invPlayer, TileEntityHadron laser) {
		super(new ContainerHadron(invPlayer, laser));
		this.hadron = laser;

		this.xSize = 176;
		this.ySize = 222;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 62, guiTop + 108, 70, 16, hadron.power, hadron.maxPower);

		if(hadron.hopperMode)
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 142, guiTop + 89, 18, 18, mouseX, mouseY, I18nUtil.resolveKeyArray("hadron.hopper1"));
		else
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 142, guiTop + 89, 18, 18, mouseX, mouseY, I18nUtil.resolveKeyArray("hadron.hopper0"));

		if(hadron.analysisOnly)
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 142, guiTop + 107, 18, 18, mouseX, mouseY, I18nUtil.resolveKeyArray("hadron.modeLine"));
		else
			this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 142, guiTop + 107, 18, 18, mouseX, mouseY, I18nUtil.resolveKeyArray("hadron.modeCircular"));

		List<String> stats = new ArrayList();
		stats.add("§e" + I18nUtil.resolveKey("hadron.stats"));
		stats.add((hadron.stat_success ? "§a" : "§c") + I18n.format("hadron." + this.hadron.stat_state.name().toLowerCase()));
		if(this.hadron.state.showCoord){
			stats.add("§c" + I18nUtil.resolveKey("hadron.stats_coord", hadron.stat_x, hadron.stat_y, hadron.stat_z));
		}
		stats.add("§7" + I18nUtil.resolveKey("hadron.stats_momentum", String.format("%,d", hadron.stat_charge)));
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 41, guiTop + 92, 25, 11, mouseX, mouseY, stats.toArray(new String[0]));

		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 4, guiTop + 36, 16, 16, guiLeft + 4, guiTop + 36 + 16, new String[] {"Initial particle momentum: 750"});
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	protected void mouseClicked(int x, int y, int i) throws IOException {
    	super.mouseClicked(x, y, i);

    	//Toggle hadron
    	if(guiLeft + 19 <= x && guiLeft + 19 + 18 > x && guiTop + 89 < y && guiTop + 89 + 18 >= y) {
			playClickSound();
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(hadron.getPos(), 0, 0));
    	}

    	//Toggle analysis chamber
    	if(guiLeft + 142 <= x && guiLeft + 142 + 18 > x && guiTop + 107 < y && guiTop + 107 + 18 >= y) {
			playClickSound();
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(hadron.getPos(), 0, 1));
    	}
    	//Toggle hopper mode
    	if(guiLeft + 142 <= x && guiLeft + 142 + 18 > x && guiTop + 89 < y && guiTop + 89 + 18 >= y) {
			playClickSound();
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(hadron.getPos(), 0, 2));
    	}
    }

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.hadron.hasCustomName() ? this.hadron.getName() : I18n.format(this.hadron.getName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
		
		String state = I18n.format("hadron." + this.hadron.state.name().toLowerCase());
		this.fontRenderer.drawString(state, this.xSize / 2 - this.fontRenderer.getStringWidth(state) / 2, 76, this.hadron.state.color);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(hadron.isOn)
			drawTexturedModalRect(guiLeft + 16, guiTop + 89, 206, 0, 18, 18);

		if(hadron.analysisOnly)
			drawTexturedModalRect(guiLeft + 142, guiTop + 107, 206, 18, 18, 18);
		
		if(hadron.hopperMode)
			drawTexturedModalRect(guiLeft + 142, guiTop + 89, 206, 36, 18, 18);

		if(hadron.state == EnumHadronState.SUCCESS) {
			drawTexturedModalRect(guiLeft + 73, guiTop + 29, 176, 0, 30, 30);
		}
		if(hadron.state == EnumHadronState.NORESULT) {
			drawTexturedModalRect(guiLeft + 73, guiTop + 29, 176, 30, 30, 30);
		}
		if(hadron.state == EnumHadronState.ERROR_GENERIC) {
			drawTexturedModalRect(guiLeft + 73, guiTop + 29, 176, 106, 30, 30);
		}
		
		int i = hadron.getPowerScaled(70);
		drawTexturedModalRect(guiLeft + 62, guiTop + 108, 176, 60, i, 16);
		
		int color = hadron.state.color;
		float red = (color & 0xff0000) >> 16;
		float green = (color & 0x00ff00) >> 8;
		float blue = (color & 0x0000ff);

		GlStateManager.color(red, green, blue, 1.0F);
		drawTexturedModalRect(guiLeft + 45, guiTop + 73, 0, 222, 86, 14);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
}