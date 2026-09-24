package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineChemicalPlant;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.modules.machine.ModuleMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineChemicalPlant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIMachineChemicalPlant extends GuiInfoContainerProcessor {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_chemplant.png");
    private final TileEntityMachineChemicalPlant chemplant;

    public GUIMachineChemicalPlant(InventoryPlayer invPlayer, TileEntityMachineChemicalPlant tedf) {
        super(new ContainerMachineChemicalPlant(invPlayer, tedf.getCheckedInventory(), tedf));
        chemplant = tedf;

        this.processorModule = new ModuleMachineBase[] { chemplant.chemplantModule };

        this.xSize = 176;
        this.ySize = 256;
    }

    @Override public int[][] getSelectorPositions() { return new int[][] {{7, 125, 1}}; }
    @Override public IControlReceiver getControlReceiver() { return this.chemplant; }
    @Override public ResourceLocation getTexture() { return texture; }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        for(int i = 0; i < 3; i++) {
            chemplant.inputTanks[i].renderTankInfo(this, mouseX, mouseY, guiLeft + 8 + i * 18, guiTop + 18, 16, 34);
            chemplant.outputTanks[i].renderTankInfo(this, mouseX, mouseY, guiLeft + 80 + i * 18, guiTop + 18, 16, 34);
        }

        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 61, chemplant.power, chemplant.maxPower);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.chemplant.hasCustomName() ? this.chemplant.getName() : I18n.format(this.chemplant.getName());

        this.fontRenderer.drawString(name, 70 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int p = (int) (chemplant.power * 61 / chemplant.maxPower);
        drawTexturedModalRect(guiLeft + 152, guiTop + 79 - p, 176, 61 - p, 16, p);

        if(chemplant.chemplantModule.progress > 0) {
            int j = (int) Math.ceil(70 * chemplant.chemplantModule.progress);
            drawTexturedModalRect(guiLeft + 62, guiTop + 126, 176, 61 + (chemplant.chemplantModule.restrictedMode ? 16 : 0), j, 16);
        }

        GenericRecipe recipe = chemplant.chemplantModule.getRecipe();
        this.renderStandardLEDs(chemplant.didProcess, recipe, chemplant.power, 51, 121, 195, 0);
        this.renderRecipeIcons();

        for(int i = 0; i < 3; i++) {
            chemplant.inputTanks[i].renderTank(guiLeft + 8 + i * 18, guiTop + 52, this.zLevel, 16, 34);
            chemplant.outputTanks[i].renderTank(guiLeft + 80 + i * 18, guiTop + 52, this.zLevel, 16, 34);
        }
    }
}
