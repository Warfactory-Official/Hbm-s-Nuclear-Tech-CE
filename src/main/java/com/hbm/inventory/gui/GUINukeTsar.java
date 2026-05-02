package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerNukeTsar;
import com.hbm.items.ModItems;
import com.hbm.tileentity.bomb.TileEntityNukeTsar;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

public class GUINukeTsar extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/weapon/tsarBombaSchematic.png");
	private static final ResourceLocation textureMike = new ResourceLocation(Tags.MODID + ":textures/gui/weapon/ivyMikeSchematic.png");
	private final TileEntityNukeTsar tsar;

	public GUINukeTsar(InventoryPlayer invPlayer, TileEntityNukeTsar tedf) {
		super(new ContainerNukeTsar(invPlayer, tedf));
		this.tsar = tedf;

		this.xSize = 256;
		this.ySize = 233;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.tsar.hasCustomInventoryName() ? this.tsar.getInventoryName() : I18n.format(this.tsar.getInventoryName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 48, this.ySize - 96 + 2, 4210752);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);

		String[] descText = I18nUtil.resolveKeyArray("desc.gui.nukeTsar.desc");
		this.drawCustomInfoStat(mouseX, mouseY, guiLeft - 16, guiTop + 16, 16, 16, guiLeft - 8, guiTop + 16 + 16, descText);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		Minecraft.getMinecraft().getTextureManager().bindTexture(textureMike);
        IItemHandler inv = tsar.inventory;

        if(tsar.isFilled()) drawTexturedModalRect(guiLeft + 18, guiTop + 50, 176, 18, 16, 16);
        else if(tsar.isReady()) drawTexturedModalRect(guiLeft + 18, guiTop + 50, 176, 0, 16, 16);

        for(int i = 0; i < 4; i++) {
            if(inv.getStackInSlot(i).getItem() == ModItems.explosive_lenses) switch(i) {
                case 0: drawTexturedModalRect(guiLeft + 24 + 16, guiTop + 20 + 16, 209, 1, 23, 23); break;
                case 2: drawTexturedModalRect(guiLeft + 47 + 16, guiTop + 20 + 16, 232, 1, 23, 23); break;
                case 1: drawTexturedModalRect(guiLeft + 24 + 16, guiTop + 43 + 16, 209, 24, 23, 23); break;
                case 3: drawTexturedModalRect(guiLeft + 47 + 16, guiTop + 43 + 16, 232, 24, 23, 23); break;
            }
        }

        if(inv.getStackInSlot(5).getItem() == ModItems.tsar_core)
            drawTexturedModalRect(guiLeft + 75 + 16, guiTop + 25 + 16, 176, 220, 80, 36);

        this.drawInfoPanel(guiLeft - 16, guiTop + 16, 16, 16, 2);
	}
}
