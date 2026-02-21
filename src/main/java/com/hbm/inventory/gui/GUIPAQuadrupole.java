package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerPAQuadrupole;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.albion.TileEntityPAQuadrupole;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GUIPAQuadrupole extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/particleaccelerator/gui_quadrupole.png");
    private final TileEntityPAQuadrupole quadrupole;

    public GUIPAQuadrupole(InventoryPlayer player, TileEntityPAQuadrupole slopper) {
        super(new ContainerPAQuadrupole(player, slopper));
        this.quadrupole = slopper;

        this.xSize = 176;
        this.ySize = 204;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        quadrupole.coolantTanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 116, guiTop + 36, 16, 52);
        quadrupole.coolantTanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 36, 16, 52);
        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 26, guiTop + 18, 16, 52, quadrupole.power, quadrupole.getMaxPower());
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = this.quadrupole.hasCustomName() ? this.quadrupole.getName() : I18n.format(this.quadrupole.getName());
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2 - 9, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);

        this.fontRenderer.drawString(TextFormatting.AQUA + "/123K", 118, 22, 4210752);
        int heat = (int) Math.ceil(quadrupole.temperature);
        String label = (heat > 123 ? TextFormatting.RED : TextFormatting.AQUA) + "" + heat + "K";
        this.fontRenderer.drawString(label, 148 - this.fontRenderer.getStringWidth(label), 12, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int j = (int) (quadrupole.power * 52 / quadrupole.getMaxPower());
        drawTexturedModalRect(guiLeft + 26, guiTop + 70 - j, 184, 52 - j, 16, j);

        int heat = (int) Math.ceil(quadrupole.temperature);
        if (heat <= 123) drawTexturedModalRect(guiLeft + 75, guiTop + 64, 176, 8, 8, 8);

        ItemStack coilStack = quadrupole.inventory.getStackInSlot(1);
        if (!coilStack.isEmpty() && coilStack.getItem() == ModItems.pa_coil) {
            drawTexturedModalRect(guiLeft + 85, guiTop + 64, 176, 8, 8, 8);
        }

        if (quadrupole.power >= TileEntityPAQuadrupole.usage) drawTexturedModalRect(guiLeft + 65, guiTop + 64, 176, 8, 8, 8);

        quadrupole.coolantTanks[0].renderTank(guiLeft + 116, guiTop + 88, this.zLevel, 16, 52);
        quadrupole.coolantTanks[1].renderTank(guiLeft + 134, guiTop + 88, this.zLevel, 16, 52);
    }
}