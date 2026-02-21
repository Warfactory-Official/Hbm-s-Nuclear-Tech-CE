package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerAmmoBag;
import com.hbm.items.tool.ItemAmmoBag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIAmmoBag extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID, "textures/gui/gui_ammo_bag.png");
    private final ItemAmmoBag.InventoryAmmoBag inventory;

    public GUIAmmoBag(InventoryPlayer invPlayer, ItemAmmoBag.InventoryAmmoBag bag) {
        super(new ContainerAmmoBag(invPlayer, bag));
        this.inventory = bag;

        this.xSize = 176;
        this.ySize = 168;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = I18n.format("container.ammoBag");

        if (inventory.box.hasDisplayName()) {
            name = inventory.box.getDisplayName();
        }

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xFFFFFF);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 98, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
