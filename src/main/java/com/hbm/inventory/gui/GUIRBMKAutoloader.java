package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerRBMKAutoloader;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKAutoloader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GUIRBMKAutoloader extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_autoloader.png");
    private final TileEntityRBMKAutoloader loader;

    public GUIRBMKAutoloader(InventoryPlayer invPlayer, TileEntityRBMKAutoloader tile) {
        super(new ContainerRBMKAutoloader(invPlayer, tile));
        loader = tile;

        this.xSize = 176;
        this.ySize = 182;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        if(guiLeft + 74 <= x && guiLeft + 74 + 12 > x && guiTop + 36 < y && guiTop + 36 + 12 >= y) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1F));
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("minus", true);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, loader.getPos()));
        }

        if(guiLeft + 90 <= x && guiLeft + 90 + 12 > x && guiTop + 36 < y && guiTop + 36 + 12 >= y) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1F));
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("plus", true);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, loader.getPos()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.loader.hasCustomName() ? this.loader.getName() : I18n.format(this.loader.getName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xFFFFFF);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);

        String percent = loader.cycle + "%";
        this.fontRenderer.drawString(percent, this.xSize / 2 - this.fontRenderer.getStringWidth(percent) / 2, 23, 0x00FF00);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
