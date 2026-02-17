package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerMachineExcavator;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineExcavator;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIMachineExcavator extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_mining_drill.png");
	private TileEntityMachineExcavator drill;

	public GUIMachineExcavator(InventoryPlayer inventory, TileEntityMachineExcavator tile) {
		super(new ContainerMachineExcavator(inventory, tile));
		
		this.drill = tile;
		
		this.xSize = 242;
		this.ySize = 204;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		String[] text1 = I18nUtil.resolveKeyArray("desc.excavator1");
		String[] text2 = I18nUtil.resolveKeyArray("desc.excavator2");
		String[] text3 = I18nUtil.resolveKeyArray("desc.excavator3");
		String[] text4 = I18nUtil.resolveKeyArray("desc.excavator4");
		String[] text5 = I18nUtil.resolveKeyArray("desc.excavator5");
		
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 7, guiTop + 16, 18, 18, mouseX, mouseY, text1);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 31, guiTop + 16, 18, 18, mouseX, mouseY, text2);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 55, guiTop + 16, 18, 18, mouseX, mouseY, text3);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 79, guiTop + 16, 18, 18, mouseX, mouseY, text4);
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 103, guiTop + 16, 18, 18, mouseX, mouseY, text5);
		
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 220, guiTop + 17, 16, 52, drill.getPower(), drill.maxPower);
		this.drill.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 202, guiTop + 18, 16, 52);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	public boolean isInBox(int mouseX, int mouseY, int x1, int x2, int y1, int y2){
		return guiLeft + x1 <= mouseX && guiLeft + x2 > mouseX && guiTop + y1 < mouseY && guiTop + y2 >= mouseY;
	}

	@Override
	protected void mouseClicked(int x, int y, int mouseButton) throws IOException {
		super.mouseClicked(x, y, mouseButton);

		String toggle = null;
		
		if(isInBox(x, y, 6, 6 + 20, 42, 42 + 40)) toggle = "drill";
		if(isInBox(x, y, 30, 30 + 20, 42, 42 + 40)) toggle = "silktouch";
		if(isInBox(x, y, 54, 54 + 20, 42, 42 + 40)) toggle = "crusher";
		if(isInBox(x, y, 78, 78 + 20, 42, 42 + 40)) toggle = "veinminer";
		if(isInBox(x, y, 102, 102 + 20, 42, 42 + 40)) toggle = "walling";

		if(toggle != null) {
			// "hbm:block.leverLarge"
			playClickSound();
			NBTTagCompound data = new NBTTagCompound();
			data.setBoolean(toggle, true);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, drill.getPos()));
        }
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8 + 33, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
		super.drawDefaultBackground();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, 242, 96);
		drawTexturedModalRect(guiLeft + 33, guiTop + 104, 33, 104, 176, 100);
		
		int i = (int) (drill.getPower() * 52 / drill.getMaxPower());
		drawTexturedModalRect(guiLeft + 220, guiTop + 70 - i, 229, 156 - i, 16, i);
		
		if(drill.getPower() > drill.getPowerConsumption()) {
			drawTexturedModalRect(guiLeft + 224, guiTop + 4, 239, 156, 9, 12);
		}
		
		if(drill.getInstalledDrill() == null && System.currentTimeMillis() % 1000 < 500) {
			drawTexturedModalRect(guiLeft + 171, guiTop + 74, 209, 154, 18, 18);
		}
		
		if(drill.enableDrill) {
			drawTexturedModalRect(guiLeft + 6, guiTop + 42, 209, 114, 20, 40);
			if(drill.getInstalledDrill() != null && drill.getPower() >= drill.getPowerConsumption())
				drawTexturedModalRect(guiLeft + 11, guiTop + 5, 209, 104, 10, 10);
			else if(System.currentTimeMillis() % 1000 < 500)
				drawTexturedModalRect(guiLeft + 11, guiTop + 5, 219, 104, 10, 10);
		}

		if(drill.enableSilkTouch) {
			drawTexturedModalRect(guiLeft + 30, guiTop + 42, 209, 114, 20, 40);
			if(drill.canSilkTouch())
				drawTexturedModalRect(guiLeft + 35, guiTop + 5, 209, 104, 10, 10);
			else if(System.currentTimeMillis() % 1000 < 500)
				drawTexturedModalRect(guiLeft + 35, guiTop + 5, 219, 104, 10, 10);
		}
		
		if(drill.enableCrusher) {
			drawTexturedModalRect(guiLeft + 54, guiTop + 42, 209, 114, 20, 40);
			drawTexturedModalRect(guiLeft + 59, guiTop + 5, 209, 104, 10, 10);
		}
		
		if(drill.enableVeinMiner) {
			drawTexturedModalRect(guiLeft + 78, guiTop + 42, 209, 114, 20, 40);
			if(drill.canVeinMine())
				drawTexturedModalRect(guiLeft + 83, guiTop + 5, 209, 104, 10, 10);
			else if(System.currentTimeMillis() % 1000 < 500)
				drawTexturedModalRect(guiLeft + 83, guiTop + 5, 219, 104, 10, 10);
			
		}

		if(drill.enableWalling) {
			drawTexturedModalRect(guiLeft + 102, guiTop + 42, 209, 114, 20, 40);
			drawTexturedModalRect(guiLeft + 107, guiTop + 5, 209, 104, 10, 10);
		}

		drill.tank.renderTank(guiLeft + 202, guiTop + 70, this.zLevel, 16, 52);
	}
}
