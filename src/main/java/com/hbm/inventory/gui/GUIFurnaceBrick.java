package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerFurnaceBrick;
import com.hbm.tileentity.machine.TileEntityFurnaceBrick;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIFurnaceBrick extends GuiInfoContainer {

    private static ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_furnace_brick.png");
    private TileEntityFurnaceBrick furnace;

    public GUIFurnaceBrick(InventoryPlayer invPlayer, TileEntityFurnaceBrick tile) {
        super(new ContainerFurnaceBrick(invPlayer, tile));
        this.furnace = tile;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        String name = this.furnace.hasCustomName() ? this.furnace.getName() : I18n.format(this.furnace.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xffffff);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0xffffff);
    }

    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);

        if(furnace.isInvalid() && furnace.getWorld().getTileEntity(furnace.getPos()) instanceof TileEntityFurnaceBrick)
            furnace = (TileEntityFurnaceBrick) furnace.getWorld().getTileEntity(furnace.getPos());

        if(this.furnace.burnTime > 0) {
            int b = furnace.burnTime * 13 / furnace.maxBurnTime;
            this.drawTexturedModalRect(guiLeft + 62, guiTop + 54 + 12 - b, 176, 12 - b, 14, b + 1);
            int p = this.furnace.progress * 24 / 200;
            this.drawTexturedModalRect(guiLeft + 85, guiTop + 34, 176, 14, p + 1, 16);
        }
    }
}
