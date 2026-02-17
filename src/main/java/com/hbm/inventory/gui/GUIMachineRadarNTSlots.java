package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineRadarNT;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineRadarNT;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;
import java.util.Arrays;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIMachineRadarNTSlots extends GuiInfoContainer {

    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_radar_link.png");
    private TileEntityMachineRadarNT radar;

    public GUIMachineRadarNTSlots(InventoryPlayer invPlayer, TileEntityMachineRadarNT tedf) {
        super(new ContainerMachineRadarNT(invPlayer, tedf));
        radar = tedf;

        this.xSize = 176;
        this.ySize = 184;
    }

    @Override
    protected void mouseClicked(int x, int y, int i) throws IOException {
        super.mouseClicked(x, y, i);

        if(checkClick(x, y, 5, 5, 8, 8)) {
            playClickSound();
            this.mc.player.connection.handleCloseWindow(new SPacketCloseWindow(this.mc.player.openContainer.windowId)); // closes the server-side GUI component without resetting the client's cursor position
            FMLNetworkHandler.openGui(this.mc.player, MainRegistry.instance, 0, radar.getWorld(), radar.getPos().getX(), radar.getPos().getY(), radar.getPos().getZ());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        if(checkClick(mouseX, mouseY, 5, 5, 8, 8)) this.drawHoveringText(Arrays.asList(I18nUtil.resolveKeyArray("radar.toggleGui")), mouseX, mouseY);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.radar.hasCustomName() ? this.radar.getName() : I18n.format(this.radar.getName());
        if(MainRegistry.polaroidID == 11) name = "Reda";
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if(radar.power > 0) {
            int i = (int) (radar.power * 160 / radar.maxPower);
            drawTexturedModalRect(guiLeft + 8, guiTop + 64, 0, 185, i, 16);
        }
    }

}
