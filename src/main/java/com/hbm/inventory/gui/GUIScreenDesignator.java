package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toserver.ItemDesignatorPacket;
import com.hbm.packet.PacketDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GUIScreenDesignator extends GuiScreen {

	protected static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_designator.png");
    protected int xSize = 176;
    protected int ySize = 126;
    protected int guiLeft;
    protected int guiTop;

    protected short hereButtonCoolDown = 0;
    protected short saveButtonCoolDown = 0;

    private final EntityPlayer player;

    private GuiTextField xField;
    private GuiTextField zField;

    
    public GUIScreenDesignator(EntityPlayer player) {
    	this.player = player;
    }
    
    public void drawScreen(int mouseX, int mouseY, float f) {
        this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
        GlStateManager.enableLighting();
        GlStateManager.disableLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.enableLighting();
        if(isOnXButton(mouseX, mouseY))
            this.drawHoveringText("Click to Flip X to -X", mouseX, mouseY);
        if(isOnZButton(mouseX, mouseY))
            this.drawHoveringText("Click to Flip Z to -Z", mouseX, mouseY);
        if(isOnHereButton(mouseX, mouseY))
            this.drawHoveringText("Set coordinates to player position", mouseX, mouseY);
        if(isOnSaveButton(mouseX, mouseY))
            this.drawHoveringText("Save coordinates", mouseX, mouseY);
        if(isOnDistanceField(mouseX, mouseY))
            this.drawHoveringText("Distance from player to coordinates", mouseX, mouseY);
    }

    protected void loadSavedCoords(){
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
                
        if(stack == null || stack.getItem() != ModItems.designator_manual) {
            stack = player.getHeldItem(EnumHand.OFF_HAND);
            if(stack == null || stack.getItem() != ModItems.designator_manual)
                return;
        }
        if(stack.hasTagCompound()){
            this.xField.setText(Integer.toString(stack.getTagCompound().getInteger("xCoord")));
            this.zField.setText(Integer.toString(stack.getTagCompound().getInteger("zCoord")));
        }else{
            this.xField.setText("0");
            this.zField.setText("0");
        }
    }
    
    public void initGui() {

        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        Keyboard.enableRepeatEvents(true);
        this.xField = new GuiTextField(0, this.fontRenderer, guiLeft + 38, guiTop + 38, 115, 10);
        this.xField.setTextColor(0xCC24B5);
        this.xField.setDisabledTextColour(0x991B88);
        this.xField.setEnableBackgroundDrawing(false);
        this.xField.setMaxStringLength(10);

        this.zField = new GuiTextField(1, this.fontRenderer, guiLeft + 38, guiTop + 60, 115, 10);
        this.zField.setTextColor(0xCC24B5);
        this.zField.setDisabledTextColour(0x991B88);
        this.zField.setEnableBackgroundDrawing(false);
        this.zField.setMaxStringLength(10);
        loadSavedCoords();
    }

    protected boolean isOnHereButton(int i, int j){
        return this.guiLeft+112 < i && this.guiLeft+112+18 > i && this.guiTop+87 < j && this.guiTop+87+18 > j;
    }

    protected boolean isOnSaveButton(int i, int j){
        return this.guiLeft+139 < i && this.guiLeft+139+18 > i && this.guiTop+87 < j && this.guiTop+87+18 > j;
    }

    protected boolean isOnXButton(int i, int j){
        return this.guiLeft+14 < i && this.guiLeft+14+18 > i && this.guiTop+33 < j && this.guiTop+33+18 > j;
    }

    protected boolean isOnZButton(int i, int j){
        return this.guiLeft+14 < i && this.guiLeft+14+18 > i && this.guiTop+55 < j && this.guiTop+55+18 > j;
    }

    protected boolean isOnDistanceField(int i, int j){
        return this.guiLeft+34 < i && this.guiLeft+34+67 > i && this.guiTop+89 < j && this.guiTop+89+14 > j;
    }

    protected void mouseClicked(int i, int j, int k) throws IOException {
        super.mouseClicked(i, j, k);
    	
        this.xField.mouseClicked(i, j, k);
        this.zField.mouseClicked(i, j, k);

    	if(player != null){
            if(this.isOnXButton(i, j)){
                this.xField.setText(Integer.toString(-1 * NumberUtils.toInt(this.xField.getText())));
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(HBMSoundHandler.buttonYes, 1.0F));
            }

            if(this.isOnZButton(i, j)){
                this.zField.setText(Integer.toString(-1 * NumberUtils.toInt(this.zField.getText())));
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(HBMSoundHandler.buttonYes, 1.0F));
            }

            if(this.isOnHereButton(i, j)) {
                hereButtonCoolDown = 20;
                this.xField.setText(Integer.toString((int) player.posX));
                this.zField.setText(Integer.toString((int) player.posZ));
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(HBMSoundHandler.buttonYes, 1.0F));
            }

            if(this.isOnSaveButton(i, j) && saveButtonCoolDown == 0) {
                saveButtonCoolDown = 20;
                this.formatInput();
	    		mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(HBMSoundHandler.techBleep, 1.0F));
	    		PacketDispatcher.wrapper.sendToServer(new ItemDesignatorPacket(Integer.parseInt(xField.getText()), Integer.parseInt(zField.getText())));
            }
    	}
    }

    protected void formatInput(){
        this.xField.setText(Integer.toString(NumberUtils.toInt(this.xField.getText())));
        this.zField.setText(Integer.toString(NumberUtils.toInt(this.zField.getText())));
    }
	
	protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = "Manual Designator";
        this.fontRenderer.drawString(name, this.guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 12, 0xCCCCCC);

        this.formatInput();

        this.xField.drawTextBox();
        this.zField.drawTextBox();

        int posx = NumberUtils.toInt(this.xField.getText());
        int posz = NumberUtils.toInt(this.zField.getText());

        String distance = Math.min(((int) Math.sqrt(Math.pow(posx-player.posX, 2)+Math.pow(posz-player.posZ, 2))), 99999999) + " m";

        this.fontRenderer.drawString(distance, this.guiLeft + 97 - this.fontRenderer.getStringWidth(distance), this.guiTop + 93, 0x0091FF);
	}

	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(xField.isFocused())
			drawTexturedModalRect(guiLeft + 32, guiTop + 33, 0, 126, 125, 18);

		if(zField.isFocused())
			drawTexturedModalRect(guiLeft + 32, guiTop + 55, 0, 126, 125, 18);

        if(hereButtonCoolDown > 0){
            drawTexturedModalRect(guiLeft + 112, guiTop + 87, 176, 0, 18, 18);
            hereButtonCoolDown--;
        }

        if(saveButtonCoolDown > 0){
            drawTexturedModalRect(guiLeft + 139, guiTop + 87, 194, 0, 18, 18);
            saveButtonCoolDown--;
        }
	}
	
    protected void keyTyped(char p_73869_1_, int p_73869_2_) throws IOException {
    	
        if (!(this.xField.textboxKeyTyped(p_73869_1_, p_73869_2_) || this.zField.textboxKeyTyped(p_73869_1_, p_73869_2_))) {
            super.keyTyped(p_73869_1_, p_73869_2_);
        }
    	
        if (p_73869_2_ == 1 || p_73869_2_ == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.player.closeScreen();
        }
        
    }
    
    @Override
    public boolean doesGuiPauseGame() {
    	return false;
    }
}
