package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerFunnel;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineFunnel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Collections;

import static com.hbm.util.GuiUtil.playClickSound;

public class GUIFunnel extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_funnel.png");
    private final TileEntityMachineFunnel funnel;

    public GUIFunnel(InventoryPlayer invPlayer, TileEntityMachineFunnel tile) {
        super(new ContainerFunnel(invPlayer, tile));
        funnel = tile;

        this.xSize = 176;
        this.ySize = 168;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        this.renderHoveredToolTip(mouseX, mouseY);

        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 159, guiTop + 73, 10, 10, mouseX, mouseY, Collections.singletonList("Mode: " + (funnel.mode == TileEntityMachineFunnel.MODE_3x3 ? "3x3 only" : funnel.mode == TileEntityMachineFunnel.MODE_2x2 ? "2x2 only" : "3x3 then 2x2")));
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        if(this.checkClick(x, y, 159, 73, 10, 10)) {
            playClickSound();
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("toggle", true);
            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, funnel.getPos().getX(), funnel.getPos().getY(), funnel.getPos().getZ()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.funnel.hasCustomName() ? this.funnel.getName() : I18n.format(this.funnel.getName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        drawTexturedModalRect(guiLeft + 159, guiTop + 73, 176, funnel.mode * 10, 10, 10);
    }
}
