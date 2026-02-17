package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerCraneBoxer;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityCraneBoxer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUICraneBoxer extends GuiInfoContainer {
    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crane_boxer.png");
    private TileEntityCraneBoxer boxer;

    private String[] modeDesc = new String[] { 
        "desc.conveyor.boxer.mode1",
        "desc.conveyor.boxer.mode2",
        "desc.conveyor.boxer.mode4",
        "desc.conveyor.boxer.mode8",
        "desc.conveyor.boxer.mode16",
        "desc.conveyor.boxer.modeR"
    };

    public GUICraneBoxer(InventoryPlayer invPlayer, TileEntityCraneBoxer tedf) {
        super(new ContainerCraneBoxer(invPlayer, tedf));
        boxer = tedf;

        this.xSize = 176;
        this.ySize = 185;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.boxer.hasCustomName() ? this.boxer.getName() : I18n.format(this.boxer.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        if(guiLeft + 151 <= x && guiLeft + 151 + 18 > x && guiTop + 34 < y && guiTop + 34 + 18 >= y) {

            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("toggle", true);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, boxer.getPos()));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 148, guiTop + 31, 22, 22, mouseX, mouseY, new String[] { I18n.format(modeDesc[boxer.mode]) });
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        drawTexturedModalRect(guiLeft + 151, guiTop + 34, 176, boxer.mode * 18, 18, 18);
    }
}
