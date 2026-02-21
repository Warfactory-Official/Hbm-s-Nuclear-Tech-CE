package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerReactorControl;
import com.hbm.modules.NumberDisplay;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityReactorControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIReactorControl extends GuiInfoContainer {

    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_reactor_control.png");
    private TileEntityReactorControl control;

    private final NumberDisplay[] displays = new NumberDisplay[3];
    private GuiTextField[] fields;

    public GUIReactorControl(InventoryPlayer invPlayer, TileEntityReactorControl tedf) {
        super(new ContainerReactorControl(invPlayer, tedf));
        control = tedf;
        displays[0] = new NumberDisplay(this, 6, 20, 0x08FF00).setDigitLength(3);
        displays[1] = new NumberDisplay(this, 66, 20, 0x08FF00).setDigitLength(4);
        displays[2] = new NumberDisplay(this, 126, 20, 0x08FF00).setDigitLength(3);

        fields = new GuiTextField[4];

        this.xSize = 176;
        this.ySize = 166;
    }

    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        // rod extraction fields
        for(byte i = 0; i < 2; i++) {
            this.fields[i] = new GuiTextField(i, this.fontRenderer, guiLeft + 35 + 30 * i, guiTop + 38, 26, 7);
            this.fields[i].setTextColor(0x08FF00);
            this.fields[i].setDisabledTextColour(-1);
            this.fields[i].setEnableBackgroundDrawing(false);

            this.fields[i].setMaxStringLength(3);
        }

        // heat fields
        for(byte i = 0; i < 2; i++) {
            this.fields[i + 2] = new GuiTextField(i + 2, this.fontRenderer, guiLeft + 35 + 30 * i, guiTop + 49, 26, 7);
            this.fields[i + 2].setTextColor(0x08FF00);
            this.fields[i + 2].setDisabledTextColour(-1);
            this.fields[i + 2].setEnableBackgroundDrawing(false);

            this.fields[i + 2].setMaxStringLength(4);
        }

        this.fields[0].setText(String.valueOf((int) control.levelUpper));
        this.fields[1].setText(String.valueOf((int) control.levelLower));
        this.fields[2].setText(String.valueOf((int) control.heatUpper / 50));
        this.fields[3].setText(String.valueOf((int) control.heatLower / 50));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        for(int j = 0; j < 4; j++) {
            this.fields[j].mouseClicked(x, y, i);
        }

        if(guiLeft + 33 <= x && guiLeft + 33 + 58 > x && guiTop + 59 < y && guiTop + 59 + 10 >= y) {

            playClickSound();
            NBTTagCompound data = new NBTTagCompound();

            double[] vals = new double[] { 0D, 0D, 0D, 0D };

            for(int k = 0; k < 4; k++) {

                double clamp = k < 2 ? 100 : 1000;
                int mod = k < 2 ? 1 : 50;

                if(NumberUtils.isDigits(fields[k].getText())) {
                    int j = (int) MathHelper.clamp(Double.parseDouble(fields[k].getText()), 0, clamp);
                    fields[k].setText(j + "");
                    vals[k] = j * mod;
                } else {
                    fields[k].setText("0");
                }
            }

            data.setDouble("levelUpper", vals[0]);
            data.setDouble("levelLower", vals[1]);
            data.setDouble("heatUpper", vals[2]);
            data.setDouble("heatLower", vals[3]);

            PacketThreading.createSendToServerThreadedPacket(
                    new NBTControlPacket(data, control.getPos().getX(), control.getPos().getY(), control.getPos().getZ()));
        }

        for(int k = 0; k < 3; k++) {
            if(guiLeft + 7 <= x && guiLeft + 7 + 22 > x && guiTop + 37 + k * 11 < y && guiTop + 37 + 10 + k * 11 >= y) {

                playClickSound();
                NBTTagCompound data = new NBTTagCompound();
                data.setInteger("function", k);
                PacketThreading.createSendToServerThreadedPacket(
                        new NBTControlPacket(data, control.getPos().getX(), control.getPos().getY(), control.getPos().getZ()));
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.control.hasCustomName() ? this.control.getName() : I18n.format(this.control.getName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        org.lwjgl.opengl.GL11.glLineWidth(3F);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(org.lwjgl.opengl.GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        int color = 0x08FF00;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        for (int i = 0; i < 40; i++) {
            double yVal = guiTop + 39 + net.minecraft.util.math.MathHelper.clamp(control.getTargetLevel(control.function, i * 1250) / 100D * 28D, 0D, 28D);
            buffer.pos(guiLeft + 128 + i, yVal, this.zLevel).color(r, g, b, 255).endVertex();
        }

        tess.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

        for (byte i = 0; i < 3; i++)
            displays[i].drawNumber(control.getDisplayData()[i]);

        for (int i = 0; i < 4; i++)
            this.fields[i].drawTextBox();
    }

    @Override
    protected void keyTyped(char c, int i) throws IOException {

        for(byte j = 0; j < 4; j++) {
            if(this.fields[j].textboxKeyTyped(c, i))
                return;
        }

        if(i == 1 || i == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.player.closeScreen();
            return;
        }

        super.keyTyped(c, i);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
}
