package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCoreEmitter;
import com.hbm.lib.Library;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.machine.TileEntityCoreEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUICoreEmitter extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/dfc/gui_emitter.png");
	private TileEntityCoreEmitter emitter;
    private GuiTextField field;

    protected short saveButtonCoolDown = 0;
	
	public GUICoreEmitter(EntityPlayer invPlayer, TileEntityCoreEmitter tedf) {
		super(new ContainerCoreEmitter(invPlayer, tedf));
		emitter = tedf;
		
		this.xSize = 176;
		this.ySize = 166;
	}
	
	public void initGui() {

		super.initGui();

        Keyboard.enableRepeatEvents(true);
        this.field = new GuiTextField(0, this.fontRenderer, guiLeft + 88, guiTop + 62, 24, 10);
        this.field.setTextColor(0x5BBC00);
        this.field.setDisabledTextColour(0x499500);
        this.field.setEnableBackgroundDrawing(false);
        this.field.setMaxStringLength(3);
        this.field.setText(String.valueOf(emitter.watts));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		String[] output = new String[] { "Output: " + Library.getShortNumber(emitter.prev) + "SPK" };
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 79, guiTop + 14, 8, 39, mouseX, mouseY, output);

		emitter.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 13, guiTop + 17, 16, 52);
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 49, guiTop + 20, 16, 52, emitter.power, TileEntityCoreEmitter.maxPower);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	protected void mouseClicked(int x, int y, int i) throws IOException {
    	super.mouseClicked(x, y, i);
        this.field.mouseClicked(x, y, i);

    	if(guiLeft + 124 <= x && guiLeft + 124 + 18 > x && guiTop + 56 < y && guiTop + 56 + 18 >= y) {
    		
    		if(saveButtonCoolDown == 0 && NumberUtils.isCreatable(field.getText())) {
    			int j = MathHelper.clamp(Integer.parseInt(field.getText()), 1, 100);
    			field.setText(j + "");
				playClickSound();
	    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(emitter.getPos(), j, 0));
	    		saveButtonCoolDown = 20;
    		}
    	}

    	if(guiLeft + 151 <= x && guiLeft + 151 + 18 > x && guiTop + 56 < y && guiTop + 56 + 18 >= y) {
			playClickSound();
    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(emitter.getPos(), 0, 1));
    	}
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = I18n.format(this.emitter.getName());
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		
		String inventory = I18n.format("container.inventory");
		this.fontRenderer.drawString(inventory, this.xSize - 8 - this.fontRenderer.getStringWidth(inventory), this.ySize - 96 + 2, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(field.isFocused())
			drawTexturedModalRect(guiLeft + 81, guiTop + 58, 210, 0, 32, 14);

		if(emitter.isOn){
			drawTexturedModalRect(guiLeft + 151, guiTop + 56, 192, 0, 18, 18);
		}

		int emitterWatts = emitter.getWattsScaled(35);
		drawTexturedModalRect(guiLeft + 81, guiTop + 52 - emitterWatts, 176, 87 - emitterWatts, 4, emitterWatts);
		
		int i = (int) emitter.getPowerScaled(52);
		drawTexturedModalRect(guiLeft + 49, guiTop + 73 - i, 176, 52 - i, 16, i);

		if(emitter.isOn && emitter.power > 500000)
			drawTexturedModalRect(guiLeft + 149, guiTop + 33, 176, 87, 80, 3);

		if(saveButtonCoolDown > 0){
            drawTexturedModalRect(guiLeft + 124, guiTop + 56, 192, 18, 18, 18);
            saveButtonCoolDown--;
        }
		
        this.field.drawTextBox();

		emitter.tank.renderTank(guiLeft+13, guiTop+73, zLevel, 16, 52);
	}
	
    protected void keyTyped(char p_73869_1_, int p_73869_2_) throws IOException
    {
        if (this.field.textboxKeyTyped(p_73869_1_, p_73869_2_)) { }
        else {
            super.keyTyped(p_73869_1_, p_73869_2_);
        }
    }
}