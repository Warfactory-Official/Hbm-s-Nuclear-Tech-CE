package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCasingBag;
import com.hbm.items.tool.ItemCasingBag;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

public class GUICasingBag extends GuiContainer {

    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_casing_bag.png");
    private final ItemCasingBag.InventoryCasingBag inventory;

    public GUICasingBag(InventoryPlayer invPlayer, ItemCasingBag.InventoryCasingBag bag) {
        super(new ContainerCasingBag(invPlayer, bag, EnumHand.MAIN_HAND));
        this.inventory = bag;

        this.xSize = 176;
        this.ySize = 186;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = net.minecraft.client.resources.I18n.format("container.casingBag");

        if(inventory.box.hasDisplayName()) {
            name = inventory.box.getDisplayName();
        }

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xffffff);
        this.fontRenderer.drawString(net.minecraft.client.resources.I18n.format("container.inventory"), 8, this.ySize - 98, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
