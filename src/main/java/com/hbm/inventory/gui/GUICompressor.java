package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerCompressor;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineCompressorBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GUICompressor extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_compressor.png");
    private final TileEntityMachineCompressorBase compressor;

    public GUICompressor(InventoryPlayer invPlayer, TileEntityMachineCompressorBase tedf) {
        super(new ContainerCompressor(invPlayer, tedf));
        compressor = tedf;

        this.xSize = 176;
        this.ySize = 204;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        compressor.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 17, guiTop + 18, 16, 52);
        compressor.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 107, guiTop + 18, 16, 52);
        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 52, compressor.power, compressor.maxPower);

        for (int j = 0; j < 5; j++) drawCustomInfoStat(mouseX, mouseY, guiLeft + 43 + j * 11, guiTop + 46, 8, 14, mouseX, mouseY, new String[]{j + " PU -> " + (j + 1) + " PU"});

        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for(int j = 0; j < 5; j++) {
            if(guiLeft + 43 + j * 11 <= mouseX && guiLeft + 43 + 8 + j * 11 > mouseX && guiTop + 46 < mouseY && guiTop + 46 + 14 >= mouseY) {
                mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F));
                NBTTagCompound data = new NBTTagCompound();
                data.setInteger("compression", j);
                PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, compressor.getPos()));
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {

        String name = this.compressor.hasCustomName() ? this.compressor.getName() : I18n.format(this.compressor.getName());

        this.fontRenderer.drawString(name, 70 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xC7C1A3);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if(compressor.power >= compressor.powerRequirement) {
            drawTexturedModalRect(guiLeft + 156, guiTop + 4, 176, 52, 9, 12);
        }

        drawTexturedModalRect(guiLeft + 43 + compressor.tanks[0].getPressure() * 11, guiTop + 46, 193, 18, 8, 124);

        int i = compressor.progress * 55 / compressor.processTime;
        drawTexturedModalRect(guiLeft + 42, guiTop + 26, 192, 0, i, 17);

        int j = (int) (compressor.power * 52 / compressor.maxPower);
        drawTexturedModalRect(guiLeft + 152, guiTop + 70 - j, 176, 52 - j, 16, j);

        compressor.tanks[0].renderTank(guiLeft + 17, guiTop + 70, this.zLevel, 16, 52);
        compressor.tanks[1].renderTank(guiLeft + 107, guiTop + 70, this.zLevel, 16, 52);
    }
}