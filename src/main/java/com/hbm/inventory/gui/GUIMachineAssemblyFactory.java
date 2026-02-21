package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineAssemblyFactory;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

public class GUIMachineAssemblyFactory extends GuiInfoContainer {

    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_assembly_factory.png");
    private TileEntityMachineAssemblyFactory assembler;

    public GUIMachineAssemblyFactory(InventoryPlayer invPlayer, TileEntityMachineAssemblyFactory tedf) {
        super(new ContainerMachineAssemblyFactory(invPlayer, tedf.inventory));
        assembler = tedf;

        this.xSize = 256;
        this.ySize = 240;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        super.renderHoveredToolTip(mouseX, mouseY);
        for(int j = 0; j < 4; j++) {
            assembler.inputTanks[j].renderTankInfo(this, mouseX, mouseY, guiLeft + 105 + (j % 2) * 109, guiTop + 20 + (j / 2) * 56, 3, 16);
            assembler.outputTanks[j].renderTankInfo(this, mouseX, mouseY, guiLeft + 105 + (j % 2) * 109, guiTop + 54 + (j / 2) * 56, 3, 16);
        }

        assembler.water.renderTankInfo(this, mouseX, mouseY, guiLeft + 232, guiTop + 149, 7, 52);
        assembler.lps.renderTankInfo(this, mouseX, mouseY, guiLeft + 241, guiTop + 149, 7, 52);

        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 234, guiTop + 18, 16, 92, assembler.power, assembler.maxPower);

        for(int i = 0; i < 4; i++) if(guiLeft + 6 + (i % 2) * 109 <= mouseX && guiLeft + 6 + (i % 2) * 109 + 18 > mouseX && guiTop + 53 + (i / 2) * 56 < mouseY && guiTop + 53 + (i / 2) * 56 + 18 >= mouseY) {
            if(this.assembler.assemblerModule[i].recipe != null && AssemblyMachineRecipes.INSTANCE.recipeNameMap.containsKey(this.assembler.assemblerModule[i].recipe)) {
                GenericRecipe recipe = AssemblyMachineRecipes.INSTANCE.recipeNameMap.get(this.assembler.assemblerModule[i].recipe);
                this.drawHoveringText(recipe.print(), mouseX, mouseY);
            } else {
                this.drawHoveringText(TextFormatting.YELLOW + I18nUtil.resolveKey("gui.recipe.setRecipe"), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);

        for(int i = 0; i < 4; i++) if(this.checkClick(x, y, 6 + (i % 2) * 109, 53 + (i / 2) * 56, 18, 18)) GUIScreenRecipeSelector.openSelector(AssemblyMachineRecipes.INSTANCE, assembler, assembler.assemblerModule[i].recipe, i, ItemBlueprints.grabPool(assembler.inventory.getStackInSlot(4 + i * 14)), this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.assembler.hasCustomName() ? this.assembler.getName() : I18n.format(this.assembler.getName());

        this.fontRenderer.drawString(name, 113 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 33, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 256, 140);
        drawTexturedModalRect(guiLeft + 25, guiTop + 140, 25, 140, 231, 100);

        int p = (int) (assembler.power * 92 / assembler.maxPower);
        drawTexturedModalRect(guiLeft + 234, guiTop + 110 - p, 0, 232 - p, 16, p);

        for(int i = 0; i < 4; i++) if(assembler.assemblerModule[i].progress > 0) {
            int j = (int) Math.ceil(37 * assembler.assemblerModule[i].progress);
            drawTexturedModalRect(guiLeft + 45 + (i % 2) * 109, guiTop + 63 + (i / 2) * 56, 0, 240, j, 6);
        }

        for(int g = 0; g < 4; g++) {
            GenericRecipe recipe = AssemblyMachineRecipes.INSTANCE.recipeNameMap.get(assembler.assemblerModule[g].recipe);

            /// LEFT LED
            if(assembler.didProcess[g]) {
                drawTexturedModalRect(guiLeft + 45 + (g % 2) * 109, guiTop + 55 + (g / 2) * 56, 4, 236, 4, 4);
            } else if(recipe != null) {
                drawTexturedModalRect(guiLeft + 45 + (g % 2) * 109, guiTop + 55 + (g / 2) * 56, 0, 236, 4, 4);
            }

            /// RIGHT LED
            if(assembler.didProcess[g]) {
                drawTexturedModalRect(guiLeft + 53 + (g % 2) * 109, guiTop + 55 + (g / 2) * 56, 4, 236, 4, 4);
            } else if(recipe != null && assembler.power >= recipe.power && assembler.canCool()) {
                drawTexturedModalRect(guiLeft + 53 + (g % 2) * 109, guiTop + 55 + (g / 2) * 56, 0, 236, 4, 4);
            }
        }

        for(int g = 0; g < 4; g++) {
            GenericRecipe recipe = AssemblyMachineRecipes.INSTANCE.recipeNameMap.get(assembler.assemblerModule[g].recipe);

            this.renderItem(recipe != null ? recipe.getIcon() : TEMPLATE_FOLDER, 7 + (g % 2) * 109, 54 + (g / 2) * 56);

            if(recipe != null && recipe.inputItem != null) {
                for(int i = 0; i < recipe.inputItem.length; i++) {
                    Slot slot = this.inventorySlots.inventorySlots.get(assembler.assemblerModule[g].inputSlots[i]);
                    if(!slot.getHasStack()) this.renderItem(recipe.inputItem[i].extractForCyclingDisplay(20), slot.xPos, slot.yPos, 10F);
                }

                Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.color(1F, 1F, 1F, 0.5F);
                GlStateManager.enableBlend();
                this.zLevel = 300F;
                for(int i = 0; i < recipe.inputItem.length; i++) {
                    Slot slot = this.inventorySlots.inventorySlots.get(assembler.assemblerModule[g].inputSlots[i]);
                    if(!slot.getHasStack()) drawTexturedModalRect(guiLeft + slot.xPos, guiTop + slot.yPos, slot.xPos, slot.yPos, 16, 16);
                }
                this.zLevel = 0F;
                GlStateManager.color(1F, 1F, 1F, 1F);
                GlStateManager.disableBlend();
            }
        }

        for(int j = 0; j < 4; j++) {
            assembler.inputTanks[j].renderTank(guiLeft + 105 + (j % 2) * 109, guiTop + 52 + (j / 2) * 56, this.zLevel, 5, 32);
            assembler.outputTanks[j].renderTank(guiLeft + 105 + (j % 2) * 109, guiTop + 70 + (j / 2) * 56, this.zLevel, 5, 16);
        }

        assembler.water.renderTank(guiLeft + 232, guiTop + 201, this.zLevel, 7, 52);
        assembler.lps.renderTank(guiLeft + 241, guiTop + 201, this.zLevel, 7, 52);
    }
}
