package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerNukeBalefire;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.bomb.TileEntityNukeBalefire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

public class GUINukeBalefire extends GuiInfoContainer {

	public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/weapon/fstbmbSchematic.png");
	private TileEntityNukeBalefire bomb;
	private GuiTextField timer;

	public GUINukeBalefire(InventoryPlayer invPlayer, TileEntityNukeBalefire bomb) {
		super(new ContainerNukeBalefire(invPlayer, bomb));
		this.bomb = bomb;

		this.xSize = 176;
		this.ySize = 222;
	}

	@Override
	public void initGui() {
		super.initGui();

		Keyboard.enableRepeatEvents(true);
		this.timer = new GuiTextField(0, this.fontRenderer, guiLeft + 94, guiTop + 40, 29, 12);
		this.timer.setTextColor(0xff0000);
		this.timer.setDisabledTextColour(0x800000);
		this.timer.setEnableBackgroundDrawing(false);
		this.timer.setMaxStringLength(3);
		this.timer.setText(String.valueOf(bomb.timer / 20));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int x, int y, int i) throws IOException {
		super.mouseClicked(x, y, i);
		this.timer.mouseClicked(x, y, i);

		if(!bomb.started) {
			if(guiLeft + 142 <= x && guiLeft + 142 + 18 > x && guiTop + 35 < y && guiTop + 35 + 18 >= y) {
				PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(bomb.getPos(), 0, 0));
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {

		String name = this.bomb.hasCustomName() ? this.bomb.getName() : I18n.format(this.bomb.getName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);

		if(bomb.hasBattery()) {
			String timer = bomb.getMinutes() + ":" + bomb.getSeconds();
			double scale = 0.75;
			GL11.glScaled(scale, scale, scale);
			this.fontRenderer.drawString(timer, (int) ((69 - this.fontRenderer.getStringWidth(timer) / 2) * (1 / scale)), (int) (95.5 * (1 / scale)), 0xff0000);

			GL11.glScaled(1 / scale, 1 / scale, 1 / scale);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(bomb.hasEgg())
			drawTexturedModalRect(guiLeft + 19, guiTop + 90, 176, 0, 30, 16);

		int battery = bomb.getBattery();

		if(battery == 1)
			drawTexturedModalRect(guiLeft + 88, guiTop + 93, 176, 16, 18, 10);
		else if(battery == 2)
			drawTexturedModalRect(guiLeft + 88, guiTop + 93, 194, 16, 18, 10);

		if(bomb.started)
			drawTexturedModalRect(guiLeft + 142, guiTop + 35, 176, 26, 18, 18);

		this.timer.drawTextBox();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(this.timer.textboxKeyTyped(typedChar, keyCode)) {

			if(NumberUtils.isCreatable(timer.getText())) {
				int j = MathHelper.clamp(Integer.parseInt(timer.getText()), 1, 999);
				PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(bomb.getPos(), j, 1));
			}

		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}
}