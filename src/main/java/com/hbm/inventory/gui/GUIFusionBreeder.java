package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerFusionBreeder;
import com.hbm.render.util.GaugeUtil;
import com.hbm.tileentity.machine.fusion.TileEntityFusionBreeder;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GUIFusionBreeder extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/reactors/gui_fusion_breeder.png");
    public TileEntityFusionBreeder breeder;

    public GUIFusionBreeder(InventoryPlayer invPlayer, TileEntityFusionBreeder breeder) {
        super(new ContainerFusionBreeder(invPlayer, breeder));
        this.breeder = breeder;

        this.xSize = 176;
        this.ySize = 200;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float interp) {
        super.drawScreen(mouseX, mouseY, interp);
        super.renderHoveredToolTip(mouseX, mouseY);

        drawCustomInfoStat(mouseX, mouseY, guiLeft + 79, guiTop + 23, 18, 18, mouseX, mouseY, new String[]{TextFormatting.GREEN + "-> " + TextFormatting.RESET + (int) Math.ceil(breeder.neutronEnergy) + " flux/t"});
        drawCustomInfoStat(mouseX, mouseY, guiLeft + 67, guiTop + 46, 42, 14, mouseX, mouseY, new String[]{BobMathUtil.format((int) Math.ceil(breeder.progress)) + " / " + BobMathUtil.format((int) Math.ceil(breeder.capacity)) + " flux"});

        breeder.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 26, guiTop + 18, 16, 52);
        breeder.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 18, 16, 52);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.breeder.hasCustomName() ? this.breeder.getName() : I18n.format(this.breeder.getDefaultName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 35, this.ySize - 93, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
        super.drawDefaultBackground();
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int p = (int) Math.ceil(breeder.progress * 42 / breeder.capacity);
        if(p > 0) drawTexturedModalRect(guiLeft + 67, guiTop + 48, 176, 0, p, 10);

        double gauge = 1D - Math.pow(Math.E, -breeder.neutronEnergy * 10 / breeder.capacity);

        // input flux
        GaugeUtil.drawSmoothGauge(guiLeft + 88, guiTop + 32, this.zLevel, gauge, 5, 2, 1, 0xA00000);

        breeder.tanks[0].renderTank(guiLeft + 26, guiTop + 70, this.zLevel, 16, 52);
        breeder.tanks[1].renderTank(guiLeft + 134, guiTop + 70, this.zLevel, 16, 52);
    }
}
