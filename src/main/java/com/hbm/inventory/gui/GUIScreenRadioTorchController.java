package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.api.redstoneoverradio.IRORInfo;
import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.lib.ForgeDirection;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityRadioTorchController;
import com.hbm.util.Compat;
import com.hbm.util.I18nUtil;
import com.hbm.util.SoundUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIScreenRadioTorchController extends GuiScreen {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_rtty_controller.png");
    protected TileEntityRadioTorchController controller;
    protected int xSize;
    protected int ySize;
    protected int guiLeft;
    protected int guiTop;
    protected GuiTextField frequency;

    public GUIScreenRadioTorchController(TileEntityRadioTorchController controller) {
        this.controller = controller;

        this.xSize = 256;
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
        this.frequency.setText(controller.channel == null ? "" : controller.channel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
        GlStateManager.disableLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.enableLighting();
    }

    private void drawCustomInfoStat(int mouseX, int mouseY, int x, int y, int width, int height, int tPosX, int tPosY, String[] text) {
        if (x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY) {
            this.drawHoveringText(Arrays.asList(text), tPosX, tPosY);
        }
    }

    protected void drawGuiContainerForegroundLayer(int x, int y) {
        String name = I18nUtil.resolveKey("container.rttyController");
        this.fontRenderer.drawString(name, this.guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 6, 4210752);

        drawCustomInfoStat(x, y, guiLeft + 173, guiTop + 17, 18, 18, x, y, new String[]{controller.polling ? "Polling" : "State Change"});
        drawCustomInfoStat(x, y, guiLeft + 209, guiTop + 17, 18, 18, x, y, new String[]{"Save Settings"});

        if (guiLeft + 137 <= x && guiLeft + 137 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
            ForgeDirection dir = ForgeDirection.getOrientation(controller.getBlockMetadata()).getOpposite();
            TileEntity tile = Compat.getTileStandard(controller.getWorld(), controller.getPos().getX() + dir.offsetX, controller.getPos().getY() + dir.offsetY, controller.getPos().getZ() + dir.offsetZ);
            if (tile instanceof IRORInfo prov) {
                String[] info = prov.getFunctionInfo();
                List<String> lines = new ArrayList<>();
                lines.add("Usable functions:");
                for (String s : info) {
                    if (s.startsWith(IRORValueProvider.PREFIX_FUNCTION))
                        lines.add(ChatFormatting.AQUA + s.substring(4));
                }
                drawCustomInfoStat(x, y, guiLeft + 137, guiTop + 17, 18, 18, x, y, lines.toArray(new String[0]));
            }
        }
    }

    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (controller.polling) {
            drawTexturedModalRect(guiLeft + 173, guiTop + 17, 0, 42, 18, 18);
        }

        this.frequency.drawTextBox();
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        this.frequency.mouseClicked(x, y, i);

        if (guiLeft + 173 <= x && guiLeft + 173 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
            SoundUtil.playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("polling", !controller.polling);
            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, controller.getPos()));
        }

        if (guiLeft + 209 <= x && guiLeft + 209 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
            SoundUtil.playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setString("channel", this.frequency.getText());
            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, controller.getPos()));
        }
    }

    @Override
    protected void keyTyped(char c, int i) {
        if (this.frequency.textboxKeyTyped(c, i)) return;

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
