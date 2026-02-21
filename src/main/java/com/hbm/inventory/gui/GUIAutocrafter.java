package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerAutocrafter;
import com.hbm.tileentity.machine.TileEntityMachineAutocrafter;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public class GUIAutocrafter extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_autocrafter.png");
    private final TileEntityMachineAutocrafter diFurnace;

    public GUIAutocrafter(InventoryPlayer invPlayer, TileEntityMachineAutocrafter tedf) {
        super(new ContainerAutocrafter(invPlayer, tedf));
        diFurnace = tedf;

        this.xSize = 176;
        this.ySize = 240;
    }

    @Override
    public void drawScreen(int x, int y, float interp) {
        super.drawScreen(x, y, interp);
        super.renderHoveredToolTip(x, y);

        this.drawElectricityInfo(this, x, y, guiLeft + 17, guiTop + 45, 16, 52, diFurnace.getPower(), diFurnace.getMaxPower());

        if(this.mc.player.getHeldItemMainhand().isEmpty()) {
            for(int i = 0; i < 9; ++i) {
                Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i);

                if(this.isMouseOverSlot(slot, x, y) && diFurnace.modes[i] != null) {

                    String label = TextFormatting.YELLOW + "";

                    switch(diFurnace.modes[i]) {
                        case "exact": label += I18nUtil.resolveKey("desc.exact"); break;
                        case "wildcard": label += I18nUtil.resolveKey("desc.wildcard"); break;
                        default: label += I18nUtil.resolveKey("desc.oredictmatch")+" "+ diFurnace.modes[i]; break;
                    }

                    this.drawHoveringText(Arrays.asList(new String[] { TextFormatting.RED + I18nUtil.resolveKey("desc.rcchange"), label }), x, y - 30);
                }
            }

            Slot slot = (Slot) this.inventorySlots.inventorySlots.get(9);

            if(this.isMouseOverSlot(slot, x, y) && !diFurnace.inventory.getStackInSlot(9).isEmpty()) {
                this.drawHoveringText(Arrays.asList(new String[] { TextFormatting.RED + I18nUtil.resolveKey("desc.rcchange"), TextFormatting.YELLOW + "" + (diFurnace.recipeIndex + 1) + " / " + diFurnace.recipeCount }), x, y - 30);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.diFurnace.hasCustomName() ? this.diFurnace.getName() : I18n.format(this.diFurnace.getName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int i = (int)(diFurnace.getPower() * 52 / diFurnace.getMaxPower());
        drawTexturedModalRect(guiLeft + 17, guiTop + 97 - i, 176, 52 - i, 16, i);

    }
}
