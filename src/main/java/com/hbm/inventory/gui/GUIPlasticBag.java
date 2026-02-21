package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerPlasticBag;
import com.hbm.items.tool.ItemPlasticBag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class GUIPlasticBag extends GuiContainer {

    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_plastic_bag.png");
    private ItemStack firstHeld;

    public GUIPlasticBag(InventoryPlayer invPlayer, ItemPlasticBag.InventoryPlasticBag box) {
        super(new ContainerPlasticBag(invPlayer, box));

        this.xSize = 176;
        this.ySize = 216;
    }

    @Override
    public void drawScreen(int x, int y, float interp) {
        if(firstHeld == null) {
            firstHeld = this.mc.player.getHeldItemMainhand();
        }

        super.drawScreen(x, y, interp);
        super.renderHoveredToolTip(x, y);
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
    }
}
