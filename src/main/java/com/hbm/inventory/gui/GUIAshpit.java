package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerAshpit;
import com.hbm.tileentity.machine.TileEntityAshpit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIAshpit extends GuiInfoContainer {

    private TileEntityAshpit firebox;
    private final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_ashpit.png");

    public GUIAshpit(InventoryPlayer invPlayer, TileEntityAshpit tedf) {
        super(new ContainerAshpit(invPlayer, tedf));
        firebox = tedf;

        this.xSize = 176;
        this.ySize = 168;
    }
    public void initGui() {
        super.initGui();
        if(!firebox.getWorld().isRemote) firebox.playersUsing++;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if(!firebox.getWorld().isRemote) firebox.playersUsing--;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.firebox.hasCustomName() ? this.firebox.getName() : I18n.format(this.firebox.getName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
