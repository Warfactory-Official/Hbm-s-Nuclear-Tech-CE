package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachinePlasmaForge;
import com.hbm.inventory.recipes.PlasmaForgeRecipe;
import com.hbm.inventory.recipes.PlasmaForgeRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.render.util.GaugeUtil;
import com.hbm.tileentity.machine.fusion.TileEntityFusionPlasmaForge;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GUIMachinePlasmaForge extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID, "textures/gui/reactors/gui_fusion_plasmaforge.png");
    private final TileEntityFusionPlasmaForge forge;

    public GUIMachinePlasmaForge(InventoryPlayer invPlayer, TileEntityFusionPlasmaForge forge) {
        super(new ContainerMachinePlasmaForge(invPlayer, forge.inventory));
        this.forge = forge;
        this.xSize = 176;
        this.ySize = 244;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);

        forge.inputTank.renderTankInfo(this, mouseX, mouseY, guiLeft + 80, guiTop + 18, 16, 52);
        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 62, forge.power, forge.maxPower);

        if(guiLeft + 7 <= mouseX && guiLeft + 25 > mouseX && guiTop + 80 < mouseY && guiTop + 98 >= mouseY) {
            if(this.forge.plasmaModule.recipe != null && PlasmaForgeRecipes.INSTANCE.recipeNameMap.containsKey(this.forge.plasmaModule.recipe)) {
                GenericRecipe recipe = PlasmaForgeRecipes.INSTANCE.recipeNameMap.get(this.forge.plasmaModule.recipe);
                this.drawHoveringText(recipe.print(), mouseX, mouseY);
            } else {
                this.drawHoveringText(TextFormatting.YELLOW + I18nUtil.resolveKey("gui.recipe.setRecipe"), mouseX, mouseY);
            }
        }

        PlasmaForgeRecipe recipe = PlasmaForgeRecipes.INSTANCE.recipeNameMap.get(forge.plasmaModule.recipe);
        if(recipe != null) {
            drawCustomInfoStat(mouseX, mouseY, guiLeft + 25, guiTop + 115, 18, 18, mouseX, mouseY, new String[] {TextFormatting.GREEN + "-> " + TextFormatting.RESET + BobMathUtil.getShortNumber(forge.plasmaEnergySync) + "TU / " + BobMathUtil.getShortNumber(recipe.ignitionTemp) + "TU"});
        } else {
            drawCustomInfoStat(mouseX, mouseY, guiLeft + 25, guiTop + 115, 18, 18, mouseX, mouseY, new String[] {"0TU / 0TU"});
        }

        if(this.isMouseOverSlot(this.inventorySlots.inventorySlots.get(2), mouseX, mouseY) && forge.inventory.getStackInSlot(2).isEmpty() && this.mc.player.inventory.getItemStack().isEmpty()) {
            List<ItemStack> list = new ArrayList<>();
            forge.boosters.forEach(pair -> list.addAll(pair.getKey().extractForJEI()));
            if(!list.isEmpty()) {
                List<Object[]> lines = new ArrayList<>();
                ItemStack selected = list.get(0).copy();

                if(list.size() > 1) {
                    int cycle = (int) ((System.currentTimeMillis() % (1000L * list.size())) / 1000L);
                    selected = list.get(cycle).copy();
                    ItemStack highlight = selected.copy();
                    highlight.setCount(0);
                    list.set(cycle, highlight);
                }

                lines.add(new Object[] {"Booster Isotope:"});
                if(list.size() < 10) {
                    lines.add(list.toArray());
                } else if(list.size() < 24) {
                    lines.add(list.subList(0, list.size() / 2).toArray());
                    lines.add(list.subList(list.size() / 2, list.size()).toArray());
                } else {
                    int bound0 = (int) Math.ceil(list.size() / 3D);
                    int bound1 = (int) Math.ceil(list.size() / 3D * 2D);
                    lines.add(list.subList(0, bound0).toArray());
                    lines.add(list.subList(bound0, bound1).toArray());
                    lines.add(list.subList(bound1, list.size()).toArray());
                }

                lines.add(new Object[] {selected.getDisplayName()});
                this.drawStackText(lines, mouseX, mouseY, this.fontRenderer, -1);
            }
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        if(this.checkClick(x, y, 7, 80, 18, 18)) {
            GUIScreenRecipeSelector.openSelector(PlasmaForgeRecipes.INSTANCE, forge, forge.plasmaModule.recipe, 0, ItemBlueprints.grabPool(forge.inventory.getStackInSlot(1)), this);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = this.forge.hasCustomName() ? this.forge.getName() : I18n.format(this.forge.getDefaultName());
        this.fontRenderer.drawString(name, 70 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int p = (int) (forge.power * 62 / forge.maxPower);
        drawTexturedModalRect(guiLeft + 152, guiTop + 80 - p, 176, 62 - p, 16, p);

        if(forge.plasmaModule.progress > 0) {
            int progress = (int) Math.ceil(70 * forge.plasmaModule.progress);
            drawTexturedModalRect(guiLeft + 62, guiTop + 81, 176, 62, progress, 16);
        }

        PlasmaForgeRecipe recipe = PlasmaForgeRecipes.INSTANCE.recipeNameMap.get(forge.plasmaModule.recipe);

        if(forge.didProcess) {
            drawTexturedModalRect(guiLeft + 51, guiTop + 76, 195, 0, 3, 6);
        } else if(recipe != null) {
            drawTexturedModalRect(guiLeft + 51, guiTop + 76, 192, 0, 3, 6);
        }

        if(forge.didProcess) {
            drawTexturedModalRect(guiLeft + 56, guiTop + 76, 195, 0, 3, 6);
        } else if(recipe != null && forge.power >= recipe.power) {
            drawTexturedModalRect(guiLeft + 56, guiTop + 76, 192, 0, 3, 6);
        }

        double inputGauge = recipe == null ? 0 : Math.min((double) forge.plasmaEnergySync / (double) recipe.ignitionTemp, 1.5D) / 1.5D;
        double boosterGauge = forge.maxBooster <= 0 ? 0 : (double) forge.booster / (double) forge.maxBooster;

        GaugeUtil.drawSmoothGauge(guiLeft + 34, guiTop + 124, this.zLevel, inputGauge, 5, 2, 1, 0xA00000);
        GaugeUtil.drawSmoothGauge(guiLeft + 70, guiTop + 124, this.zLevel, boosterGauge, 5, 2, 1, 0xA00000);

        this.renderItem(recipe != null ? recipe.getIcon() : TEMPLATE_FOLDER, 8, 81);

        if(recipe != null && recipe.inputItem != null) {
            for(int i = 0; i < recipe.inputItem.length; i++) {
                Slot slot = this.inventorySlots.inventorySlots.get(forge.plasmaModule.inputSlots[i]);
                if(!slot.getHasStack()) this.renderItem(recipe.inputItem[i].extractForCyclingDisplay(20), slot.xPos, slot.yPos, 10F);
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1F, 1F, 1F, 0.5F);
            GlStateManager.enableBlend();
            this.zLevel = 300F;
            for(int i = 0; i < recipe.inputItem.length; i++) {
                Slot slot = this.inventorySlots.inventorySlots.get(forge.plasmaModule.inputSlots[i]);
                if(!slot.getHasStack()) drawTexturedModalRect(guiLeft + slot.xPos, guiTop + slot.yPos, slot.xPos, slot.yPos, 16, 16);
            }
            this.zLevel = 0F;
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.disableBlend();
        }

        forge.inputTank.renderTank(guiLeft + 80, guiTop + 70, this.zLevel, 16, 52);
    }
}
