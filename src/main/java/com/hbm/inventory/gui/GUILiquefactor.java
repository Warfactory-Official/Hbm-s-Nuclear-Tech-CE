package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerLiquefactor;
import com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUILiquefactor extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_liquefactor.png");
    private final TileEntityMachineLiquefactor liquefactor;

    public GUILiquefactor(InventoryPlayer invPlayer, TileEntityMachineLiquefactor tile) {
        super(new ContainerLiquefactor(invPlayer, tile));
        liquefactor = tile;

        this.xSize = 176;
        this.ySize = 204;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        liquefactor.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 71, guiTop + 36, 16, 52);
        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 18, 16, 52, liquefactor.power, TileEntityMachineLiquefactor.maxPower);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int i = (int)(liquefactor.getPower() * 52 / liquefactor.getMaxPower());
        drawTexturedModalRect(guiLeft + 134, guiTop + 70 - i, 176, 52 - i, 16, i);

        int j = liquefactor.processTime > 0 ? liquefactor.progress * 42 / liquefactor.processTime : 0;
        drawTexturedModalRect(guiLeft + 42, guiTop + 17, 192, 0, j, 35);

        if(i > 0)
            drawTexturedModalRect(guiLeft + 138, guiTop + 4, 176, 52, 9, 12);

        liquefactor.tank.renderTank(guiLeft + 71, guiTop + 88, this.zLevel, 16, 52);
    }
}
