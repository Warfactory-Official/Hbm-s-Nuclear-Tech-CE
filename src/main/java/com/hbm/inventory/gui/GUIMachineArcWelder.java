package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineArcWelder;
import com.hbm.tileentity.machine.TileEntityMachineArcWelder;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIMachineArcWelder extends GuiInfoContainer {
    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_arc_welder.png");
    private final TileEntityMachineArcWelder welder;

    public GUIMachineArcWelder(InventoryPlayer playerInv, TileEntityMachineArcWelder tile) {
        super(new ContainerMachineArcWelder(playerInv, tile));

        this.welder = tile;
        this.xSize = 176;
        this.ySize = 204;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        welder.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 35, guiTop + 63, 34, 16);
        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 52, welder.getPower(), welder.getMaxPower());

        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 78, guiTop + 67, 8, 8, guiLeft + 78, guiTop + 67, this.getUpgradeInfo(welder));
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = this.welder.hasCustomName() ? this.welder.getName() : I18n.format(this.welder.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2 - 18, 6, 4210752);
        this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int p = (int) (welder.power * 52 / Math.max(welder.maxPower, 1));
        drawTexturedModalRect(guiLeft + 152, guiTop + 70 - p, 176, 52 - p, 16, p);

        int i = welder.progress * 33 / Math.max(welder.processTime, 1);
        drawTexturedModalRect(guiLeft + 72, guiTop + 37, 192, 0, i, 14);

        if (welder.power >= welder.consumption) {
            drawTexturedModalRect(guiLeft + 156, guiTop + 4, 176, 52, 9, 12);
        }

        this.drawInfoPanel(guiLeft + 78, guiTop + 67, 8, 8, 8);
        welder.tank.renderTank(guiLeft + 35, guiTop + 79, this.zLevel, 34, 16, 1);
    }
}
