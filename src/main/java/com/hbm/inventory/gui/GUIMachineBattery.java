package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.inventory.container.ContainerMachineBattery;
import com.hbm.lib.Library;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.machine.TileEntityMachineBattery;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIMachineBattery extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_battery.png");
	private TileEntityMachineBattery battery;

	private IEnergyReceiverMK2.ConnectionPriority lastPrio = IEnergyReceiverMK2.ConnectionPriority.LOW;

	public GUIMachineBattery(InventoryPlayer invPlayer, TileEntityMachineBattery tedf) {
		super(new ContainerMachineBattery(invPlayer, tedf));
		battery = tedf;
		
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		String deltaText = Library.getShortNumber(Math.abs(battery.delta)) + "HE/s";
		if(battery.delta > 0)
			deltaText = TextFormatting.GREEN + "+" + deltaText;
		else if(battery.delta < 0)
			deltaText = TextFormatting.RED + "-" + deltaText;
		else 
			deltaText = TextFormatting.YELLOW + "0HE/s";

		String[] info = new String[] { Library.getShortNumber(battery.power)+"HE/"+Library.getShortNumber(battery.getMaxPower())+"HE", deltaText};
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 71, guiTop + 69 - 52, 34, 52, mouseX, mouseY, info);

		String lang = switch (battery.priority) {
            case LOW -> "low";
            case NORMAL -> "normal";
            case HIGH -> "high";
            default -> null;
        };

        List<String> priority = new ArrayList<>();
		priority.add(I18nUtil.resolveKey("battery.priority." + lang));
		priority.add(I18nUtil.resolveKey("battery.priority.recommended"));
		String[] desc = I18nUtil.resolveKeyArray("battery.priority." + lang + ".desc");
        Collections.addAll(priority, desc);
		
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 151, guiTop + 16, 16, 16, mouseX, mouseY, priority.toArray(new String[priority.size()]));

		String[] text = I18nUtil.resolveKeyArray("desc.guimachbattery");
				
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 36, 16, 16, guiLeft - 8, guiTop + 36 + 16, text);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void mouseClicked(int x, int y, int i) throws IOException {
    	super.mouseClicked(x, y, i);
		
    	if(guiLeft + 6 <= x && guiLeft + 6 + 18 > x && guiTop + 33 < y && guiTop + 33 + 18 >= y) {
    		
			playClickSound();
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(battery.getPos(), 0, 0));
    	}
		
    	if(guiLeft + 150 <= x && guiLeft + 150 + 18 > x && guiTop + 33 < y && guiTop + 33 + 18 >= y) {
    		
			playClickSound();
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(battery.getPos(), 0, 1));
    	}

    	if(guiLeft + 151 <= x && guiLeft + 151 + 16 > x && guiTop + 16 < y && guiTop + 17 + 16 >= y) {
    		
			playClickSound();
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(battery.getPos(), 0, 2));
    	}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.battery.hasCustomName() ? this.battery.getName() : I18n.format(this.battery.getName());
		name += (" (" + Library.getShortNumber(battery.power) + " HE)");
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(battery.power > 0) {
			int i = (int)battery.getPowerRemainingScaled(52);
			drawTexturedModalRect(guiLeft + 71, guiTop + 69 - i, 176, 52 - i, 34, i);
		}
		
		int i = battery.redLow;
		drawTexturedModalRect(guiLeft + 7, guiTop + 34, 176, 52 + i * 18, 18, 18);
		
		int j = battery.redHigh;
		drawTexturedModalRect(guiLeft + 151, guiTop + 34, 176, 52 + j * 18, 18, 18);

		drawTexturedModalRect(guiLeft + 152, guiTop + 17, 194, 52 + (battery.priority.ordinal() - 1) * 16, 16, 16);

		this.drawInfoPanel(guiLeft - 16, guiTop + 36, 16, 16, 2);
		
	}
}
