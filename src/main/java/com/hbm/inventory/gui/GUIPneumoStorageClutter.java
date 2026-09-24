package com.hbm.inventory.gui;

import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import net.minecraft.client.renderer.GlStateManager;
import com.hbm.Tags;
import com.hbm.inventory.container.ContainerPneumoStorageClutter;
import com.hbm.tileentity.network.TileEntityPneumoStorageClutter;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GUIPneumoStorageClutter extends GuiInfoContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_pneumatic_clutter.png");

    protected final TileEntityPneumoStorageClutter storage;

    public GUIPneumoStorageClutter(InventoryPlayer invPlayer, TileEntityPneumoStorageClutter storage) {
        super(new ContainerPneumoStorageClutter(invPlayer, storage));
        this.storage = storage;
        this.xSize = 200;
        this.ySize = 235;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 174, guiTop + 36, 20, 8, mouseX, mouseY, new String[] {"Compressor: " + storage.compair.getPressure() + " PU", "Max range: " + TileEntityPneumoTube.getRangeFromPressure(storage.compair.getPressure()) + "m"});
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        clickSendFlag(storage, mouseX, mouseY, 174, 36, 20, 8, "pressure");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = I18nUtil.resolveKey(this.storage.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 5, 4210752);
        this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        drawTexturedModalRect(guiLeft + 174 + 4 * (storage.compair.getPressure() - 1), guiTop + 36, 200, 0, 4, 8);
        GUIElements.drawSmoothGauge(guiLeft + 184, guiTop + 25, this.zLevel, (double) storage.compair.getFill() / (double) storage.compair.getMaxFill(), 5, 2, 1, 0xCA6C43, 0xAB4223);
    }
}
