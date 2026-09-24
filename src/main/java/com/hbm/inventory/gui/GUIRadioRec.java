package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityRadioRec;
import com.hbm.util.I18nUtil;
import com.hbm.util.SoundUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GUIRadioRec extends GuiScreenBase {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_radio.png");
    protected TileEntityRadioRec radio;
    protected int xSize;
    protected int ySize;
    protected int guiLeft;
    protected int guiTop;
    protected GuiTextField frequency;

    public GUIRadioRec(TileEntityRadioRec radio) {
        this.radio = radio;

        this.xSize = 220;
        this.ySize = 42;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        Keyboard.enableRepeatEvents(true);

        int oX = 4;
        int oY = 4;

        this.frequency = new GuiTextField(0, this.fontRenderer, guiLeft + 25 + oX, guiTop + 17 + oY, 90 - oX * 2, 14);
        this.frequency.setTextColor(0x00ff00);
        this.frequency.setDisabledTextColour(0x00ff00);
        this.frequency.setEnableBackgroundDrawing(false);
        this.frequency.setMaxStringLength(10);
        this.frequency.setText(radio.channel == null ? "" : radio.channel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer();
        GlStateManager.disableLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.enableLighting();
    }


    private void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = I18nUtil.resolveKey("container.radio");
        this.fontRenderer.drawString(name, this.guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 6, 4210752);

        drawCustomInfo(mouseX, mouseY, guiLeft + 137, guiTop + 17, 18, 18, new String[]{"Save Settings"});
        drawCustomInfo(mouseX, mouseY, guiLeft + 173, guiTop + 17, 18, 18, new String[]{"Toggle"});
    }

    private void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (this.radio.isOn) {
            drawTexturedModalRect(guiLeft + 173, guiTop + 17, 0, 42, 18, 18);
        }

        this.frequency.drawTextBox();
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        this.frequency.mouseClicked(x, y, i);

        if (guiLeft + 137 <= x && guiLeft + 137 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
            SoundUtil.playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setString("channel", this.frequency.getText());
            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, radio.getPos()));
        }

        if (guiLeft + 173 <= x && guiLeft + 173 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
            SoundUtil.playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("isOn", !radio.isOn);
            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, radio.getPos()));
        }
    }

    @Override
    protected void keyTyped(char c, int i) {

        if (this.frequency.textboxKeyTyped(c, i))
            return;

        if (i == 1 || i == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.player.closeScreen();
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
