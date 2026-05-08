package com.hbm.inventory.gui;

import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GUIScreenRBMKTerminal extends GuiScreen {

    protected GuiTextField line;
    protected final TileEntityRBMKTerminal terminal;

    public GUIScreenRBMKTerminal(TileEntityRBMKTerminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        this.line = new GuiTextField(0, this.fontRenderer, 0, 0, 0, 0);
        this.line.setMaxStringLength(50);
        this.line.setFocused(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (terminal.isInvalid()) {
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
            return;
        }

        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 1.0D);
        this.fontRenderer.drawString("[Esc] - Quit", 2, 2, 0xffffff);
        this.fontRenderer.drawString("chan <channel> - Set selected channel", 2, 12, 0xffffff);
        this.fontRenderer.drawString("send <cmd> - Send single signal over selected channel", 2, 22, 0xffffff);
        this.fontRenderer.drawString("start <cmd> - Continuously send signal over selected channel", 2, 32, 0xffffff);
        this.fontRenderer.drawString("stop - Stop continuous sending", 2, 42, 0xffffff);
        this.fontRenderer.drawString("clear - Delete command history", 2, 52, 0xffffff);
        GL11.glPopMatrix();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
            return;
        }

        if (keyCode == Keyboard.KEY_RETURN) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("cmd", this.line.getText());
            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, terminal.getPos().getX(), terminal.getPos().getY(), terminal.getPos().getZ()));
            this.line.setText("");
            return;
        }

        if (keyCode == Keyboard.KEY_HOME || keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT || keyCode == Keyboard.KEY_END) {
            return;
        }

        this.line.setCursorPositionEnd();
        this.line.textboxKeyTyped(typedChar, keyCode);
    }

    public static String getWorkingLine() {
        if (Minecraft.getMinecraft().currentScreen instanceof GUIScreenRBMKTerminal gui) {
            return gui.line.getText();
        }
        return "";
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
