package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCrucible;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.tileentity.machine.TileEntityCrucible;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GUICrucible extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_crucible.png");
    private final TileEntityCrucible crucible;

    public GUICrucible(InventoryPlayer invPlayer, TileEntityCrucible tile) {
        super(new ContainerCrucible(invPlayer, tile));
        crucible = tile;

        this.xSize = 176;
        this.ySize = 214;
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        super.drawScreen(x, y, partialTicks);
        super.renderHoveredToolTip(x, y);

        drawStackInfo(crucible.wasteStack, x, y, 16, 17);
        drawStackInfo(crucible.recipeStack, x, y, 61, 17);

        this.drawCustomInfoStat(x, y, guiLeft + 125, guiTop + 81, 34, 7, x, y, new String[] { String.format(Locale.US, "%,d", crucible.progress) + " / " + String.format(Locale.US, "%,d", crucible.processTime) + "TU" });
        this.drawCustomInfoStat(x, y, guiLeft + 125, guiTop + 90, 34, 7, x, y, new String[] { String.format(Locale.US, "%,d", crucible.heat) + " / " + String.format(Locale.US, "%,d", crucible.maxHeat) + "TU" });
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int pGauge = crucible.progress * 33 / TileEntityCrucible.processTime;
        if(pGauge > 0) drawTexturedModalRect(guiLeft + 126, guiTop + 82, 176, 0, pGauge, 5);
        int hGauge = crucible.heat * 33 / TileEntityCrucible.maxHeat;
        if(hGauge > 0) drawTexturedModalRect(guiLeft + 126, guiTop + 91, 176, 5, hGauge, 5);

        if(!crucible.recipeStack.isEmpty()) drawStack(crucible.recipeStack, TileEntityCrucible.recipeZCapacity, 62, 97);
        if(!crucible.wasteStack.isEmpty()) drawStack(crucible.wasteStack, TileEntityCrucible.wasteZCapacity, 17, 97);
    }

    protected void drawStackInfo(List<Mats.MaterialStack> stack, int mouseX, int mouseY, int x, int y) {
        List<String> tempList = new ArrayList<>();

        if (stack.isEmpty())
            tempList.add(TextFormatting.RED + "Empty");

        for (Mats.MaterialStack sta : stack) {
            tempList.add(TextFormatting.YELLOW + I18nUtil.resolveKey(sta.material.getTranslationKey()) + ": " + Mats.formatAmount(sta.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)));
        }

        String[] list = tempList.toArray(new String[0]);
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + x, guiTop + y, 36, 81, mouseX, mouseY, list);
    }

    protected void drawStack(List<Mats.MaterialStack> stack, int capacity, int x, int y) {

        if(stack.isEmpty()) return;

        int lastHeight = 0;
        int lastQuant = 0;

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        for(Mats.MaterialStack sta : stack) {

            int targetHeight = (lastQuant + sta.amount) * 79 / capacity;

            if(lastHeight == targetHeight) continue; //skip draw calls that would be 0 pixels high

            int offset = sta.material.smeltable == NTMMaterial.SmeltingBehavior.ADDITIVE ? 34 : 0; //additives use a differnt texture

            int hex = sta.material.moltenColor;
            //hex = 0xC18336;
            Color color = new Color(hex);
            GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            drawTexturedModalRect(guiLeft + x, guiTop + y - targetHeight, 176 + offset, 89 - targetHeight, 34, targetHeight - lastHeight);
            GlStateManager.enableBlend();
            GlStateManager.color(1F, 1F, 1F, 0.3F);
            drawTexturedModalRect(guiLeft + x, guiTop + y - targetHeight, 176 + offset, 89 - targetHeight, 34, targetHeight - lastHeight);
            GlStateManager.disableBlend();

            lastQuant += sta.amount;
            lastHeight = targetHeight;
        }

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
    }
}
