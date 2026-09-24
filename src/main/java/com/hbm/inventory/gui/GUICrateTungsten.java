package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityCrateTungsten;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUICrateTungsten extends GUICrateBase {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_tungsten.png");
    private static final ResourceLocation texture_hot = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_tungsten_hot.png");
    private final TileEntityCrateTungsten tungsten;

    public GUICrateTungsten(InventoryPlayer invPlayer, TileEntityCrateTungsten tedf) {
        super(invPlayer, tedf);
        this.tungsten = tedf;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String title = this.tungsten.hasCustomName() ? this.tungsten.getName() : I18n.format(this.tungsten.getName());
        this.fontRenderer.drawString(title, this.xSize / 2 - this.fontRenderer.getStringWidth(title) / 2, 6,
                tungsten.heatTimer == 0 ? 0xA0A0A0 : 0xFFCA53);
        this.fontRenderer.drawString(I18n.format("container.inventory"), tungsten.getInventoryLabelX(), this.ySize - 96 + 2, tungsten.heatTimer == 0 ? 0xA0A0A0 : 0xFFCA53);
        String sparks = Library.getShortNumber(tungsten.joules) + "SPK";
        this.fontRenderer.drawString(sparks, this.xSize - 8 - this.fontRenderer.getStringWidth(sparks), this.ySize - 96 + 2,
                tungsten.heatTimer == 0 ? 0xA0A0A0 : 0xFFCA53);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (tungsten.heatTimer == 0)
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        else
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture_hot);

        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
