package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCoreInjector;
import com.hbm.tileentity.machine.TileEntityCoreInjector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUICoreInjector extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/dfc/gui_injector.png");
    private final TileEntityCoreInjector injector;

    public GUICoreInjector(InventoryPlayer invPlayer, TileEntityCoreInjector tedf) {
        super(new ContainerCoreInjector(invPlayer, tedf));
        injector = tedf;

        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        injector.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 35, guiTop + 16, 16, 52);
        injector.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 125, guiTop + 16, 16, 52);

        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {

        String name = this.injector.hasCustomName() ? this.injector.getName() : I18n.format(this.injector.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        injector.tanks[0].renderTank(guiLeft + 35, guiTop + 69, this.zLevel, 16, 52);
        injector.tanks[1].renderTank(guiLeft + 125, guiTop + 69, this.zLevel, 16, 52);
        GlStateManager.disableBlend();
    }
}
