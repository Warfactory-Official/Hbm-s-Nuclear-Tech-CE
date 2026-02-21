package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCore;
import com.hbm.tileentity.machine.TileEntityCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUICore extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/dfc/gui_core.png");
    private final TileEntityCore core;

    public GUICore(InventoryPlayer invPlayer, TileEntityCore tedf) {
        super(new ContainerCore(invPlayer, tedf));
        core = tedf;

        this.xSize = 176;
        this.ySize = 204;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        core.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 7, 16, 80);
        core.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 152 , guiTop + 7, 16, 80);

        String[] heat = new String[]{"Heat Saturation: " + core.heat + "%"};
        String[] field = new String[]{"Restriction Field: " + core.field + "%"};
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 53, guiTop + 97, 70, 4, mouseX, mouseY, heat);
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 53, guiTop + 101, 70, 4, mouseX, mouseY, field);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {

        String name = this.core.hasCustomName() ? this.core.getName() : I18n.format(this.core.getName()).trim();
        this.fontRenderer.drawString(name, this.xSize - 8 - this.fontRenderer.getStringWidth(name), this.ySize - 96 + 2, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.colorMask(true, true, true, false);

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        int heat = Math.min(core.getHeatScaled(70), 70);
        drawTexturedModalRect(guiLeft + 53, guiTop + 98, 0, 204, heat, 4);
        int field = Math.min(core.getFieldScaled(70), 70);
        drawTexturedModalRect(guiLeft + 53, guiTop + 102, 0, 208, field, 4);
        if (core.hasCore())
            drawTexturedModalRect(guiLeft + 70, guiTop + 29, 220, 0, 36, 36);
        core.tanks[0].renderTank(guiLeft + 8, guiTop + 88, this.zLevel, 16, 80);
        core.tanks[1].renderTank(guiLeft + 152, guiTop + 88, this.zLevel, 16, 80);


        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}