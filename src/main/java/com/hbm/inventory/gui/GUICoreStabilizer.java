package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCoreStabilizer;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.machine.TileEntityCoreStabilizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUICoreStabilizer extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/dfc/gui_stabilizer.png");
	private TileEntityCoreStabilizer stabilizer;
    private GuiTextField field;

    protected short saveButtonCoolDown = 0;
	
	public GUICoreStabilizer(EntityPlayer invPlayer, TileEntityCoreStabilizer tedf) {
		super(new ContainerCoreStabilizer(invPlayer, tedf));
		stabilizer = tedf;
		
		this.xSize = 176;
		this.ySize = 170;
	}
	
	public void initGui() {

		super.initGui();

        Keyboard.enableRepeatEvents(true);
        this.field = new GuiTextField(0, this.fontRenderer, guiLeft + 60, guiTop + 62, 24, 10);
        this.field.setTextColor(0x148EC2);
        this.field.setDisabledTextColour(0x0A6C94);
        this.field.setEnableBackgroundDrawing(false);
        this.field.setMaxStringLength(3);
        this.field.setText(String.valueOf(stabilizer.watts));
	}
	
	public void syncTextField(int watts){
		this.field.setText(String.valueOf(watts));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 13, guiTop + 20, 16, 52, stabilizer.power, TileEntityCoreStabilizer.maxPower);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	protected void mouseClicked(int x, int y, int i) throws IOException {
    	super.mouseClicked(x, y, i);
        this.field.mouseClicked(x, y, i);

    	if(guiLeft + 115 <= x && guiLeft + 115 + 18 > x && guiTop + 55 < y && guiTop + 55 + 18 >= y) {
    		
    		if(saveButtonCoolDown == 0 && NumberUtils.isCreatable(field.getText())) {
    			int j = MathHelper.clamp(Integer.parseInt(field.getText()), 1, 100);
    			field.setText(j + "");
				playClickSound();
	    		PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(stabilizer.getPos(), j, 0));
	    		saveButtonCoolDown = 20;
    		}
    	}
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int i, int j) {
		String name = this.stabilizer.hasCustomName() ? this.stabilizer.getName() : I18n.format(this.stabilizer.getName());
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
			drawTexturedModalRect(guiLeft + 53, guiTop + 58, 192, 0, 32, 14);

		int stabilizerWatts = (int)(stabilizer.watts * 35 / 100);
		drawTexturedModalRect(guiLeft + 81, guiTop + 52 - stabilizerWatts, 176, 87 - stabilizerWatts, 4, stabilizerWatts);
		
		int i = (int) stabilizer.getPowerScaled(52);
		drawTexturedModalRect(guiLeft + 13, guiTop + 73 - i, 176, 52 - i, 16, i);

		if(stabilizer.isOn){
			drawTexturedModalRect(guiLeft + 43, guiTop + 33, 0, 170, 25, 2);
			drawTexturedModalRect(guiLeft + 149, guiTop + 33, 176, 87, 80, 3);
		}

		if(saveButtonCoolDown > 0){
            drawTexturedModalRect(guiLeft + 115, guiTop + 56, 192, 14, 18, 18);
            saveButtonCoolDown--;
        }
		
        this.field.drawTextBox();
	}
	
    protected void keyTyped(char p_73869_1_, int p_73869_2_) throws IOException
    {
        if (this.field.textboxKeyTyped(p_73869_1_, p_73869_2_)) { }
        else {
            super.keyTyped(p_73869_1_, p_73869_2_);
        }
    }
}
