package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerAssemfac;
import com.hbm.tileentity.machine.TileEntityMachineAssemfac;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GUIAssemfac extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_assemfac.png");
    private static final ResourceLocation chemfac = new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_chemfac.png");
    private final TileEntityMachineAssemfac assemfac;

    public GUIAssemfac(InventoryPlayer invPlayer, TileEntityMachineAssemfac tedf) {
        super(new ContainerAssemfac(invPlayer, tedf));
        assemfac = tedf;

        this.xSize = 256;
        this.ySize = 256;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        super.renderHoveredToolTip(mouseX, mouseY);

        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 234, guiTop + 164, 16, 52, assemfac.power, assemfac.getMaxPower());

        assemfac.water.renderTankInfo(this, mouseX, mouseY, guiLeft + 209, guiTop + 181, 9, 54);
        assemfac.steam.renderTankInfo(this, mouseX, mouseY, guiLeft + 218, guiTop + 181, 9, 54);

        for (int i = 0; i < 8; i++) {

            if (assemfac.maxProgress[i] > 0) {
                int progress = assemfac.progress[i] * 16 / assemfac.maxProgress[i];

                if (progress > 0) {
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.colorMask(true, true, true, false);

                    int x = guiLeft + 234;
                    int y = guiTop + 13 + 16 * i;
                    this.drawGradientRect(x, y, x + progress + 1, y + 16, -2130706433, -2130706433);

                    GlStateManager.colorMask(true, true, true, true);
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();

                }
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float interp, int mX, int mY) {
        super.drawDefaultBackground();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        Minecraft.getMinecraft().getTextureManager().bindTexture(chemfac);

        int p = (int) (assemfac.power * 52 / assemfac.getMaxPower());
        drawTexturedModalRect(guiLeft + 234, guiTop + 216 - p, 0, 219 - p, 16, p);

        if (assemfac.power > 0)
            drawTexturedModalRect(guiLeft + 238, guiTop + 150, 0, 219, 9, 12);

        assemfac.water.renderTank(guiLeft + 210, guiTop + 234, this.zLevel, 7, 52);
        assemfac.steam.renderTank(guiLeft + 219, guiTop + 234, this.zLevel, 7, 52);

        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
            for (int i = 0; i < this.inventorySlots.inventorySlots.size(); i++) {
                Slot s = this.inventorySlots.getSlot(i);

                this.fontRenderer.drawStringWithShadow(i + "", guiLeft + s.xPos + 2, guiTop + s.yPos, 0xffffff);
                this.fontRenderer.drawStringWithShadow(s.getSlotIndex() + "", guiLeft + s.xPos + 2, guiTop + s.yPos + 8, 0xff8080);
            }

    }
    public ItemStackHandler getInventory() {
        return assemfac.inventory;
    }
}
