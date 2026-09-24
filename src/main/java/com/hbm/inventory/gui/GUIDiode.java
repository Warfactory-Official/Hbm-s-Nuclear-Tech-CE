package com.hbm.inventory.gui;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.network.energy.CableDiode;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.util.EnumUtil;
import com.hbm.util.SoundUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GUIDiode extends GuiScreen {

    protected final CableDiode.TileEntityDiode diode;

    private GuiTextField textThroughput;
    private GuiButton buttonPriority;
    private int priority;

    public GUIDiode(CableDiode.TileEntityDiode diode) {
        this.diode = diode;
        this.priority = diode.priority.ordinal();
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        textThroughput = new GuiTextField(1, fontRenderer, this.width / 2 - 150, 100, 90, 20);
        textThroughput.setText("" + diode.limit);
        textThroughput.setMaxStringLength(11);

        buttonPriority = new GuiButton(0, this.width / 2 + 20, 100, 90, 20, diode.priority.name());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawString(fontRenderer, "Throughput:", this.width / 2 - 150, 80, 0xA0A0A0);
        drawString(fontRenderer, "(max. 10,000,000,000 HE)", this.width / 2 - 150, 90, 0xA0A0A0);
        textThroughput.drawTextBox();

        drawString(fontRenderer, "Priority:", this.width / 2 + 20, 80, 0xA0A0A0);
        buttonPriority.drawButton(mc, mouseX, mouseY, partialTicks);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        NBTTagCompound data = new NBTTagCompound();
        data.setByte("priority", (byte) priority);

        try { data.setLong("limit", Long.parseLong(textThroughput.getText())); } catch(Exception ignored) {}

        PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, diode.getPos()));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if(textThroughput.textboxKeyTyped(typedChar, keyCode)) return;

        if(keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.player.closeScreen();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        textThroughput.mouseClicked(mouseX, mouseY, mouseButton);

        if(buttonPriority.mousePressed(mc, mouseX, mouseY)) {
            this.priority++;
            if(priority >= IEnergyReceiverMK2.ConnectionPriority.values().length) priority = 0;
            buttonPriority.displayString = EnumUtil.grabEnumSafely(IEnergyReceiverMK2.ConnectionPriority.class, priority).name();
            SoundUtil.playClickSound();
        }
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
