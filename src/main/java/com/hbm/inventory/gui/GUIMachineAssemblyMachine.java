package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineAssemblyMachine;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.modules.machine.ModuleMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIMachineAssemblyMachine extends GuiInfoContainerProcessor {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_assembler.png");
    private final TileEntityMachineAssemblyMachine assembler;

    public GUIMachineAssemblyMachine(InventoryPlayer invPlayer, TileEntityMachineAssemblyMachine tedf) {
        super(new ContainerMachineAssemblyMachine(invPlayer, tedf.getCheckedInventory()));
        assembler = tedf;

        this.processorModule = new ModuleMachineBase[] { assembler.assemblerModule };

        this.xSize = 176;
        this.ySize = 256;
    }

    @Override public int[][] getSelectorPositions() { return new int[][] {{7, 125, 1}}; }
    @Override public IControlReceiver getControlReceiver() { return this.assembler; }
    @Override public ResourceLocation getTexture() { return texture; }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        assembler.inputTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 99, 52, 16);
        assembler.outputTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 80, guiTop + 99, 52, 16);

        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 61, assembler.power, assembler.maxPower);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.assembler.hasCustomName() ? this.assembler.getName() : I18n.format(this.assembler.getName());

        this.fontRenderer.drawString(name, 70 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int p = (int) (assembler.power * 61 / assembler.maxPower);
        drawTexturedModalRect(guiLeft + 152, guiTop + 79 - p, 176, 61 - p, 16, p);

        if(assembler.assemblerModule.progress > 0) {
            int j = (int) Math.ceil(70 * assembler.assemblerModule.progress);
            drawTexturedModalRect(guiLeft + 62, guiTop + 126, 176, 61 + (assembler.assemblerModule.restrictedMode ? 16 : 0), j, 16);
        }

        GenericRecipe recipe = assembler.assemblerModule.getRecipe();
        this.renderStandardLEDs(assembler.didProcess, recipe, assembler.power, 51, 121, 195, 0);
        this.renderRecipeIcons();

        assembler.inputTank.renderTank(guiLeft + 8, guiTop + 115, this.zLevel, 52, 16, 1);
        assembler.outputTank.renderTank(guiLeft + 80, guiTop + 115, this.zLevel, 52, 16, 1);
    }
}
