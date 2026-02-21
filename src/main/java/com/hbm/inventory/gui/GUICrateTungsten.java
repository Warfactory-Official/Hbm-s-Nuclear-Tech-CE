package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCrateTungsten;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityCrateTungsten;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUICrateTungsten extends GUICrateBase<TileEntityCrateTungsten, ContainerCrateTungsten> {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_tungsten.png");
    private static final ResourceLocation texture_hot = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_crate_tungsten_hot.png");

    public GUICrateTungsten(InventoryPlayer invPlayer, TileEntityCrateTungsten tedf) {
        super(tedf, new ContainerCrateTungsten(invPlayer, tedf), 176, 168, texture);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String title = I18n.format("container.crateTungsten");
        float percent = this.diFurnace.fillPercentage;
        String combinedTitle = combineTitle(title, percent);
        this.fontRenderer.drawString(combinedTitle, this.xSize / 2 - this.fontRenderer.getStringWidth(combinedTitle) / 2, 6,
				diFurnace.heatTimer == 0 ? 0xA0A0A0 : 0xFFCA53);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, diFurnace.heatTimer == 0 ? 0xA0A0A0 : 0xFFCA53);
        String sparks = Library.getShortNumber(diFurnace.joules) + "SPK";
        this.fontRenderer.drawString(sparks, this.xSize - 8 - this.fontRenderer.getStringWidth(sparks), this.ySize - 96 + 2,
				diFurnace.heatTimer == 0 ? 0xA0A0A0 : 0xFFCA53);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (diFurnace.heatTimer == 0)
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        else
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture_hot);

        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
