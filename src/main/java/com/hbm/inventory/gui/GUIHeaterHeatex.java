package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerHeaterHeatex;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityHeaterHeatex;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;

public class GUIHeaterHeatex extends GuiInfoContainer {
    private final static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_heatex.png");
    private final TileEntityHeaterHeatex heater;
    private GuiTextField fieldCycles;
    private GuiTextField fieldDelay;

    public GUIHeaterHeatex(InventoryPlayer invPlayer, TileEntityHeaterHeatex tedf) {
        super(new ContainerHeaterHeatex(invPlayer, tedf));
        heater = tedf;

        this.xSize = 176;
        this.ySize = 204;
    }

    @Override
    public void initGui() {
        super.initGui();

        Keyboard.enableRepeatEvents(true);
        this.fieldCycles = new GuiTextField(0, this.fontRenderer, guiLeft + 73, guiTop + 31, 30, 10);
        initText(this.fieldCycles);
        this.fieldCycles.setText(String.valueOf(heater.amountToCool));

        this.fieldDelay = new GuiTextField(1, this.fontRenderer, guiLeft + 73, guiTop + 49, 30, 10);
        initText(this.fieldDelay);
        this.fieldDelay.setText(String.valueOf(heater.tickDelay));
    }

    protected void initText(GuiTextField field) {
        field.setTextColor(0x00ff00);
        field.setDisabledTextColour(0x00ff00);
        field.setEnableBackgroundDrawing(false);
        field.setMaxStringLength(5);
    }

    @Override
    public void drawScreen(int x, int y, float f) {
        super.drawScreen(x, y, f);

        heater.tanksNew[0].renderTankInfo(this, x, y, guiLeft + 44, guiTop + 36, 16, 52);
        heater.tanksNew[1].renderTankInfo(this, x, y, guiLeft + 116, guiTop + 36, 16, 52);

        if (guiLeft + 70 <= x && guiLeft + 70 + 36 > x && guiTop + 26 < y && guiTop + 26 + 18 >= y) {
            drawHoveringText(Arrays.asList(I18nUtil.resolveKey("gui.heatex.amount")), x, y);
        }
        if (guiLeft + 70 <= x && guiLeft + 70 + 36 > x && guiTop + 44 < y && guiTop + 44 + 18 >= y) {
            drawHoveringText(Arrays.asList(I18nUtil.resolveKey("gui.heatex.cycle")), x, y);
        }

        super.renderHoveredToolTip(x, y);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = I18n.format(this.heater.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        heater.tanksNew[0].renderTank(guiLeft + 44, guiTop + 88, this.zLevel, 16, 52);
        heater.tanksNew[1].renderTank(guiLeft + 116, guiTop + 88, this.zLevel, 16, 52);

        this.fieldCycles.drawTextBox();
        this.fieldDelay.drawTextBox();
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        this.fieldCycles.mouseClicked(x, y, i);
        this.fieldDelay.mouseClicked(x, y, i);
    }

    @Override
    protected void keyTyped(char c, int i) throws IOException {
        BlockPos heaterPos = heater.getPos();

        if (this.fieldCycles.textboxKeyTyped(c, i)) {
            int cyc = Math.max(NumberUtils.toInt(this.fieldCycles.getText()), 1);
            NBTTagCompound data = new NBTTagCompound();
            data.setInteger("toCool", cyc);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, heaterPos.getX(), heaterPos.getY(), heaterPos.getZ()));
            return;
        }
        if (this.fieldDelay.textboxKeyTyped(c, i)) {
            int delay = Math.max(NumberUtils.toInt(this.fieldDelay.getText()), 1);
            NBTTagCompound data = new NBTTagCompound();
            data.setInteger("delay", delay);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, heaterPos.getX(), heaterPos.getY(), heaterPos.getZ()));
            return;
        }

        super.keyTyped(c, i);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
}
