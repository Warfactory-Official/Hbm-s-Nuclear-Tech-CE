package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineRotaryFurnace;
import com.hbm.inventory.material.Mats;
import com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace;
import com.hbm.util.I18nUtil;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class GUIMachineRotaryFurnace extends GuiInfoContainer {

  private static final ResourceLocation texture =
      new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_rotary_furnace.png");
  private final TileEntityMachineRotaryFurnace furnace;

  public GUIMachineRotaryFurnace(InventoryPlayer playerInv, TileEntityMachineRotaryFurnace tile) {
    super(new ContainerMachineRotaryFurnace(playerInv, tile));

    this.furnace = tile;
    this.xSize = 176;
    this.ySize = 186;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);

    furnace.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 36, 52, 16);
    furnace.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 18, 16, 52);
    furnace.tanks[2].renderTankInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 18, 16, 52);

    Slot slot = this.inventorySlots.inventorySlots.get(4);
    if (this.isMouseOverSlot(slot, mouseX, mouseY) && !slot.getHasStack()) {
      List<String> bonuses = TileEntityMachineRotaryFurnace.burnModule.getDesc();
      if (!bonuses.isEmpty()) this.drawHoveringText(bonuses, mouseX, mouseY);
    }

    if (furnace.output == null) {
      String[] text = new String[] {TextFormatting.RED + "Empty"};
      this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 98, guiTop + 18, 16, 52, mouseX, mouseY, text);
    } else {
      String[] text =
          new String[] {TextFormatting.YELLOW + I18nUtil.resolveKey(furnace.output.material.getTranslationKey()) + ": " + Mats.formatAmount(furnace.output.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))};
      this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 98, guiTop + 18, 16, 52, mouseX, mouseY, text);
    }

    super.renderHoveredToolTip(mouseX, mouseY);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int i, int j) {
    this.fontRenderer.drawString(
        I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
    super.drawDefaultBackground();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

    int p = (int) Math.ceil(furnace.progress * 33);
    drawTexturedModalRect(guiLeft + 63, guiTop + 30, 176, 0, p, 10);

    if (furnace.maxBurnTime > 0) {
      int b = furnace.burnTime * 14 / furnace.maxBurnTime;
      drawTexturedModalRect(guiLeft + 26, guiTop + 69 - b, 176, 24 - b, 14, b);
    }

    if (furnace.output != null) {

      int hex = furnace.output.material.moltenColor;
      int amount = furnace.output.amount * 52 / TileEntityMachineRotaryFurnace.maxOutput;
      Color color = new Color(hex);
      GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
      drawTexturedModalRect(guiLeft + 98, guiTop + 70 - amount, 176, 76 - amount, 16, amount);
      boolean wasBlendEnabled = RenderUtil.isBlendEnabled();
      if (!wasBlendEnabled) GlStateManager.enableBlend();
      GlStateManager.color(1F, 1F, 1F, 0.3F);
      drawTexturedModalRect(guiLeft + 98, guiTop + 70 - amount, 176, 76 - amount, 16, amount);
      if (!wasBlendEnabled) GlStateManager.disableBlend();

      GlStateManager.color(1.0F, 1.0F, 1.0F);
    }

    furnace.tanks[0].renderTank(guiLeft + 8, guiTop + 52, this.zLevel, 52, 16, 1);
    furnace.tanks[1].renderTank(guiLeft + 134, guiTop + 70, this.zLevel, 16, 52);
    furnace.tanks[2].renderTank(guiLeft + 152, guiTop + 70, this.zLevel, 16, 52);
  }
}
