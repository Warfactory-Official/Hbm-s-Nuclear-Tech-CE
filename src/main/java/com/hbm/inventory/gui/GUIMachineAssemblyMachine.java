package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineAssemblyMachine;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

public class GUIMachineAssemblyMachine extends GuiInfoContainer {

    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_assembler.png");
    private TileEntityMachineAssemblyMachine assembler;

    public GUIMachineAssemblyMachine(InventoryPlayer invPlayer, TileEntityMachineAssemblyMachine tedf) {
        super(new ContainerMachineAssemblyMachine(invPlayer, tedf.getCheckedInventory()));
        assembler = tedf;

        this.xSize = 176;
        this.ySize = 256;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        super.renderHoveredToolTip(mouseX, mouseY);

        assembler.inputTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 99, 52, 16);
        assembler.outputTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 80, guiTop + 99, 52, 16);

        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 61, assembler.power, assembler.maxPower);

        if(guiLeft + 7 <= mouseX && guiLeft + 7 + 18 > mouseX && guiTop + 125 < mouseY && guiTop + 125 + 18 >= mouseY) {
            if(this.assembler.assemblerModule.recipe != null && AssemblyMachineRecipes.INSTANCE.recipeNameMap.containsKey(this.assembler.assemblerModule.recipe)) {
                GenericRecipe recipe =AssemblyMachineRecipes.INSTANCE.recipeNameMap.get(this.assembler.assemblerModule.recipe);
                this.drawHoveringText(recipe.print(), mouseX, mouseY);
            } else {
                this.drawHoveringText(TextFormatting.YELLOW + I18nUtil.resolveKey("gui.recipe.setRecipe"), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);

        if(this.checkClick(x, y, 7, 125, 18, 18)) GUIScreenRecipeSelector.openSelector(AssemblyMachineRecipes.INSTANCE, assembler, assembler.assemblerModule.recipe, 0, ItemBlueprints.grabPool(assembler.inventory.getStackInSlot(1)), this);
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
            drawTexturedModalRect(guiLeft + 62, guiTop + 126, 176, 61, j, 16);
        }

        GenericRecipe recipe = AssemblyMachineRecipes.INSTANCE.recipeNameMap.get(assembler.assemblerModule.recipe);

        /// LEFT LED
        if(assembler.didProcess) {
            drawTexturedModalRect(guiLeft + 51, guiTop + 121, 195, 0, 3, 6);
        } else if(recipe != null) {
            drawTexturedModalRect(guiLeft + 51, guiTop + 121, 192, 0, 3, 6);
        }

        /// RIGHT LED
        if(assembler.didProcess) {
            drawTexturedModalRect(guiLeft + 56, guiTop + 121, 195, 0, 3, 6);
        } else if(recipe != null && assembler.power >= recipe.power) {
            drawTexturedModalRect(guiLeft + 56, guiTop + 121, 192, 0, 3, 6);
        }

        this.renderItem(recipe != null ? recipe.getIcon() : TEMPLATE_FOLDER, 8, 126);

        if(recipe != null && recipe.inputItem != null) {
            for(int i = 0; i < recipe.inputItem.length; i++) {
                Slot slot = this.inventorySlots.inventorySlots.get(assembler.assemblerModule.inputSlots[i]);
                if(!slot.getHasStack()) this.renderItem(recipe.inputItem[i].extractForCyclingDisplay(20), slot.xPos, slot.yPos, 10F);
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1F, 1F, 1F, 0.5F);
            GlStateManager.enableBlend();
            this.zLevel = 300F;
            for(int i = 0; i < recipe.inputItem.length; i++) {
                Slot slot = this.inventorySlots.inventorySlots.get(assembler.assemblerModule.inputSlots[i]);
                if(!slot.getHasStack()) drawTexturedModalRect(guiLeft + slot.xPos, guiTop + slot.yPos, slot.xPos, slot.yPos, 16, 16);
            }
            this.zLevel = 0F;
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.disableBlend();
        }

        assembler.inputTank.renderTank(guiLeft + 8, guiTop + 115, this.zLevel, 52, 16, 1);
        assembler.outputTank.renderTank(guiLeft + 80, guiTop + 115, this.zLevel, 52, 16, 1);
    }
}
