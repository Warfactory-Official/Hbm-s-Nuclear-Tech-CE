package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerDroneRequester;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.tileentity.network.TileEntityDroneRequester;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public class GUIDroneRequester extends GuiInfoContainer {

    private static ResourceLocation texture = new ResourceLocation(Tags.MODID, "textures/gui/storage/gui_drone_requester.png");
    private TileEntityDroneRequester requester;

    public GUIDroneRequester(InventoryPlayer invPlayer, TileEntityDroneRequester tedf) {
        super(new ContainerDroneRequester(invPlayer, tedf));
        requester = tedf;

        this.xSize = 176;
        this.ySize = 186;
    }

    @Override
    public void drawScreen(int x, int y, float interp) {
        super.drawScreen(x, y, interp);

        if(this.mc.player.inventory.getItemStack().isEmpty()) {
            for(int i = 0; i < 9; ++i) {
                Slot slot = this.inventorySlots.inventorySlots.get(i);

                if(this.isMouseOverSlot(slot, x, y) && requester.matcher.modes[i] != null) {
                    this.drawHoveringText(Arrays.asList(TextFormatting.RED + "Right click to change", ModulePatternMatcher.getLabel(requester.matcher.modes[i])), x, y - 30);
                }
            }
        }

        this.renderHoveredToolTip(x, y);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.requester.hasCustomName() ? this.requester.getName() : I18n.format(this.requester.getName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}