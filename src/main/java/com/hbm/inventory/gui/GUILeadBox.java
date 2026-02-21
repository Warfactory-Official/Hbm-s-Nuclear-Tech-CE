package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerLeadBox;
import com.hbm.items.tool.ItemLeadBox;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GUILeadBox extends GuiContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_containment.png");
    private final ItemLeadBox.InventoryLeadBox inventory;
    private ItemStack firstHeld;

    public GUILeadBox(InventoryPlayer invPlayer, ItemLeadBox.InventoryLeadBox box) {
        super(new ContainerLeadBox(invPlayer, box));
        this.inventory = box;

        this.xSize = 176;
        this.ySize = 186;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        if (firstHeld == null) firstHeld = this.inventory.box;

        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = net.minecraft.client.resources.I18n.format("container.leadBox");

        if (inventory.box.hasDisplayName()) {
            name = inventory.box.getDisplayName();
        }
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 4, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
