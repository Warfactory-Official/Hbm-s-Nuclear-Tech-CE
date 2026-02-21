package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineExposureChamber;
import com.hbm.tileentity.machine.TileEntityMachineExposureChamber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIMachineExposureChamber extends GuiInfoContainer {

    public static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_exposure_chamber.png");
    private final TileEntityMachineExposureChamber chamber;

    public GUIMachineExposureChamber(InventoryPlayer invPlayer, TileEntityMachineExposureChamber chamber) {
        super(new ContainerMachineExposureChamber(invPlayer, chamber));
        this.chamber = chamber;

        this.xSize = 176;
        this.ySize = 186;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 34, chamber.power, TileEntityMachineExposureChamber.maxPower);

        drawCustomInfoStat(mouseX, mouseY, guiLeft + 26, guiTop + 36, 9, 16, mouseX, mouseY,
                new String[]{chamber.savedParticles + " / " + TileEntityMachineExposureChamber.maxParticles});
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.chamber.hasCustomName() ? this.chamber.getName() : I18n.format(this.chamber.getName());
        this.fontRenderer.drawString(name, 70 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int p = chamber.progress * 42 / (chamber.processTime + 1);
        drawTexturedModalRect(guiLeft + 36, guiTop + 39, 192, 0, p, 10);

        int c = chamber.savedParticles * 16 / TileEntityMachineExposureChamber.maxParticles;
        drawTexturedModalRect(guiLeft + 26, guiTop + 52 - c, 192, 26 - c, 9, c);

        int e = (int) (chamber.power * 34 / TileEntityMachineExposureChamber.maxPower);
        drawTexturedModalRect(guiLeft + 152, guiTop + 52 - e, 176, 34 - e, 16, e);

        if (chamber.consumption <= chamber.power) {
            drawTexturedModalRect(guiLeft + 156, guiTop + 4, 176, 34, 9, 12);
        }
    }
}
