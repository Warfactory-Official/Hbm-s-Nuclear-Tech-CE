package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachinePUREX;
import com.hbm.inventory.recipes.PUREXRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.tileentity.machine.TileEntityMachinePUREX;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

public class GUIMachinePUREX extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_purex.png");
    private final TileEntityMachinePUREX purex;

    public GUIMachinePUREX(InventoryPlayer invPlayer, TileEntityMachinePUREX tedf) {
        super(new ContainerMachinePUREX(invPlayer, tedf.inventory));
        purex = tedf;

        this.xSize = 176;
        this.ySize = 256;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        for (int i = 0; i < 3; i++) {
            purex.inputTanks[i].renderTankInfo(this, mouseX, mouseY, guiLeft + 8 + i * 18, guiTop + 18, 16, 52);
        }
        purex.outputTanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 116, guiTop + 36, 16, 52);

        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 61, purex.power, purex.maxPower);

        if (guiLeft + 7 <= mouseX && guiLeft + 7 + 18 > mouseX && guiTop + 125 < mouseY && guiTop + 125 + 18 >= mouseY) {
            if (this.purex.purexModule.recipe != null && PUREXRecipes.INSTANCE.recipeNameMap.containsKey(this.purex.purexModule.recipe)) {
                GenericRecipe recipe = PUREXRecipes.INSTANCE.recipeNameMap.get(this.purex.purexModule.recipe);
                this.drawHoveringText(recipe.print(), mouseX, mouseY);
            } else {
                this.drawHoveringText(TextFormatting.YELLOW + I18nUtil.resolveKey("gui.recipe.setRecipe"), mouseX, mouseY);
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);

        if (this.checkClick(x, y, 7, 125, 18, 18)) GUIScreenRecipeSelector.openSelector(PUREXRecipes.INSTANCE, purex, purex.purexModule.recipe, 0,
                ItemBlueprints.grabPool(purex.inventory.getStackInSlot(1)), this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.purex.hasCustomName() ? this.purex.getName() : I18n.format(this.purex.getName());

        this.fontRenderer.drawString(name, 70 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int p = (int) (purex.power * 61 / purex.maxPower);
        drawTexturedModalRect(guiLeft + 152, guiTop + 79 - p, 176, 61 - p, 16, p);

        if (purex.purexModule.progress > 0) {
            int j = (int) Math.ceil(70 * purex.purexModule.progress);
            drawTexturedModalRect(guiLeft + 62, guiTop + 126, 176, 61, j, 16);
        }

        GenericRecipe recipe = PUREXRecipes.INSTANCE.recipeNameMap.get(purex.purexModule.recipe);

        /// LEFT LED
        if (purex.didProcess) {
            drawTexturedModalRect(guiLeft + 51, guiTop + 121, 195, 0, 3, 6);
        } else if (recipe != null) {
            drawTexturedModalRect(guiLeft + 51, guiTop + 121, 192, 0, 3, 6);
        }

        /// RIGHT LED
        if (purex.didProcess) {
            drawTexturedModalRect(guiLeft + 56, guiTop + 121, 195, 0, 3, 6);
        } else if (recipe != null && purex.power >= recipe.power) {
            drawTexturedModalRect(guiLeft + 56, guiTop + 121, 192, 0, 3, 6);
        }

        this.renderItem(recipe != null ? recipe.getIcon() : TEMPLATE_FOLDER, 8, 126);

        if (recipe != null && recipe.inputItem != null) {
            for (int i = 0; i < recipe.inputItem.length; i++) {
                Slot slot = this.inventorySlots.inventorySlots.get(purex.purexModule.inputSlots[i]);
                if (!slot.getHasStack()) this.renderItem(recipe.inputItem[i].extractForCyclingDisplay(20), slot.xPos, slot.yPos, 10F);
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1F, 1F, 1F, 0.5F);
            GlStateManager.enableBlend();
            this.zLevel = 300F;
            for (int i = 0; i < recipe.inputItem.length; i++) {
                Slot slot = this.inventorySlots.inventorySlots.get(purex.purexModule.inputSlots[i]);
                if (!slot.getHasStack()) drawTexturedModalRect(guiLeft + slot.xPos, guiTop + slot.yPos, slot.xPos, slot.yPos, 16, 16);
            }
            this.zLevel = 0F;
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.disableBlend();
        }

        for (int i = 0; i < 3; i++) {
            purex.inputTanks[i].renderTank(guiLeft + 8 + i * 18, guiTop + 70, this.zLevel, 16, 52);
        }
        purex.outputTanks[0].renderTank(guiLeft + 116, guiTop + 88, this.zLevel, 16, 52);
    }
}
