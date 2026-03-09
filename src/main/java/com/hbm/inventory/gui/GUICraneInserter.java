package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerCraneInserter;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityCraneInserter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.Arrays;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUICraneInserter extends GuiInfoContainer {
    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crane_inserter.png");
    private TileEntityCraneInserter inserter;

    public GUICraneInserter(InventoryPlayer invPlayer, TileEntityCraneInserter tedf) {
        super(new ContainerCraneInserter(invPlayer, tedf));
        inserter = tedf;

        this.xSize = 176;
        this.ySize = 185;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.inserter.hasCustomName() ? this.inserter.getName() : I18n.format(this.inserter.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2 - 18, 5, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    public void drawScreen(int x, int y, float interp) {
        super.drawScreen(x, y, interp);

        if(guiLeft + 151 <= x && guiLeft + 151 + 18 > x && guiTop + 34 < y && guiTop + 34 + 18 >= y) {
            this.drawHoveringText(
                    Arrays.asList("Destroy overflow: " + (inserter.destroyer ? TextFormatting.GREEN + "ON" : TextFormatting.RED + "OFF")),
                    x,
                    y
            );
        }

        this.renderHoveredToolTip(x, y);
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        if(guiLeft + 151 <= x && guiLeft + 151 + 18 > x && guiTop + 34 < y && guiTop + 34 + 18 >= y) {
            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("destroyer", true);
            PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(data, inserter.getPos()));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if(inserter.destroyer) {
            drawTexturedModalRect(guiLeft + 151, guiTop + 34, 176, 0, 18, 18);
        }
    }
}
