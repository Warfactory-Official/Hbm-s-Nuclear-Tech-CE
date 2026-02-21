package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineStrandCaster;
import com.hbm.inventory.material.Mats;
import com.hbm.tileentity.machine.TileEntityMachineStrandCaster;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GUIMachineStrandCaster extends GuiInfoContainer {

  private static final ResourceLocation texture =
      new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_strand_caster.png");
  private final TileEntityMachineStrandCaster caster;

  public GUIMachineStrandCaster(InventoryPlayer invPlayer, TileEntityMachineStrandCaster tile) {
    super(new ContainerMachineStrandCaster(invPlayer, tile));
    caster = tile;

    this.xSize = 176;
    this.ySize = 214;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);

    drawStackInfo(mouseX, mouseY, 16, 17);

    caster.water.renderTankInfo(this, mouseX, mouseY, guiLeft + 82, guiTop + 14, 16, 24);
    caster.steam.renderTankInfo(this, mouseX, mouseY, guiLeft + 82, guiTop + 65, 16, 24);

    super.renderHoveredToolTip(mouseX, mouseY);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    this.fontRenderer.drawString(
        I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    super.drawDefaultBackground();

    GlStateManager.enableBlend();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

    if (caster.amount != 0) {
      int targetHeight = Math.min((caster.amount) * 79 / caster.getCapacity(), 92);

      int hex = caster.type.moltenColor;
      Color color = new Color(hex);
      GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
      drawTexturedModalRect(
          guiLeft + 17, guiTop + 93 - targetHeight, 176, 89 - targetHeight, 34, targetHeight);
      GlStateManager.enableBlend();
      GlStateManager.color(1F, 1F, 1F, 0.3F);
      drawTexturedModalRect(
          guiLeft + 17, guiTop + 93 - targetHeight, 176, 89 - targetHeight, 34, targetHeight);
      GlStateManager.disableBlend();
    }

    GlStateManager.color(1.0F, 1.0F, 1.0F);

    caster.water.renderTank(guiLeft + 82, guiTop + 38, this.zLevel, 16, 24);
    caster.steam.renderTank(guiLeft + 82, guiTop + 89, this.zLevel, 16, 24);

    GlStateManager.disableBlend();
  }

  protected void drawStackInfo(int mouseX, int mouseY, int x, int y) {

    List<String> list = new ArrayList<>();

    if (caster.type == null) list.add(TextFormatting.RED + "Empty");
    else
      list.add(
          TextFormatting.YELLOW
              + I18nUtil.resolveKey(caster.type.getTranslationKey())
              + ": "
              + Mats.formatAmount(caster.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)));

    this.drawCustomInfoStat(mouseX, mouseY, guiLeft + x, guiTop + y, 36, 81, mouseX, mouseY, list);
  }
}
