package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityRadioTorchReader;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hbm.tileentity.network.TileEntityRadioTorchReader.MAPPING_SIZE;

public class GUIScreenRadioTorchReader extends GuiScreen {

    protected static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_rtty_reader.png");
    public TileEntityRadioTorchReader reader;
    protected int xSize = 256;
    protected int ySize = 204;
    protected int guiLeft;
    protected int guiTop;

    protected GuiTextField[] frequencies;
    protected GuiTextField[] names;

    public GUIScreenRadioTorchReader(TileEntityRadioTorchReader reader) {
        this.reader = reader;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        Keyboard.enableRepeatEvents(true);

        int oX = 4;
        int oY = 4;
        this.frequencies = new GuiTextField[MAPPING_SIZE];
        this.names = new GuiTextField[MAPPING_SIZE];

        for (int i = 0; i < MAPPING_SIZE; i++) {
            this.frequencies[i] = new GuiTextField(i, this.fontRenderer, guiLeft + 25 + oX, guiTop + 53 + i * 18 + oY, 72 - oX * 2, 14);
            this.frequencies[i].setTextColor(0x00ff00);
            this.frequencies[i].setDisabledTextColour(0x00ff00);
            this.frequencies[i].setEnableBackgroundDrawing(false);
            this.frequencies[i].setMaxStringLength(15);
            this.frequencies[i].setText(reader.channels[i] == null ? "" : reader.channels[i]);

            this.names[i] = new GuiTextField(i + 8, this.fontRenderer, guiLeft + 119 + oX, guiTop + 53 + i * 18 + oY, 126 - oX * 2, 14);
            this.names[i].setTextColor(0x00ff00);
            this.names[i].setDisabledTextColour(0x00ff00);
            this.names[i].setEnableBackgroundDrawing(false);
            this.names[i].setMaxStringLength(25);
            this.names[i].setText(reader.names[i] == null ? "" : reader.names[i]);
        }
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

    private void drawGuiContainerForegroundLayer(int x, int y) {
        String name = I18nUtil.resolveKey("container.rttyReader");
        this.fontRenderer.drawString(name, this.guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 6, 4210752);

        drawCustomInfoStat(x, y, guiLeft + 173, guiTop + 17, 18, 18, x, y, new String[]{reader.polling ? "Polling" : "State Change"});
        drawCustomInfoStat(x, y, guiLeft + 209, guiTop + 17, 18, 18, x, y, new String[]{"Save Settings"});

        if (guiLeft + 29 <= x && guiLeft + 29 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
            EnumFacing dir = reader.getTorchFacing().getOpposite();
            TileEntity tile = Compat.getTileStandard(reader.getWorld(), reader.getPos().getX() + dir.getXOffset(), reader.getPos().getY() + dir.getYOffset(), reader.getPos().getZ() + dir.getZOffset());
            if (tile instanceof IRORValueProvider prov) {
                String[] info = prov.getFunctionInfo();
                List<String> lines = new ArrayList<>();
                lines.add("Readable values:");
                for (String s : info) {
                    if (s.startsWith(IRORValueProvider.PREFIX_VALUE))
                        lines.add(ChatFormatting.LIGHT_PURPLE + s.substring(4));
                }
                drawCustomInfoStat(x, y, guiLeft + 29, guiTop + 17, 18, 18, x, y, lines.toArray(new String[0]));
            }
        }
    }

    private void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (reader.polling) drawTexturedModalRect(guiLeft + 173, guiTop + 17, 0, 204, 18, 18);

        for (GuiTextField field : frequencies) field.drawTextBox();
        for (GuiTextField field : names) field.drawTextBox();
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        for (GuiTextField field : frequencies) field.mouseClicked(x, y, i);
        for (GuiTextField field : names) field.mouseClicked(x, y, i);

        if (guiLeft + 173 <= x && guiLeft + 173 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
            SoundUtil.playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("polling", !reader.polling);
            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, reader.getPos()));
        }

        if (guiLeft + 209 <= x && guiLeft + 209 + 18 > x && guiTop + 17 < y && guiTop + 17 + 18 >= y) {
            SoundUtil.playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            for (int j = 0; j < 8; j++) data.setString("channels" + j, this.frequencies[j].getText());
            for (int j = 0; j < 8; j++) data.setString("names" + j, this.names[j].getText());
            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, reader.getPos()));
        }
    }

    @Override
    protected void keyTyped(char c, int i) {

        for (GuiTextField field : frequencies) if (field.textboxKeyTyped(c, i)) return;
        for (GuiTextField field : names) if (field.textboxKeyTyped(c, i)) return;

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
