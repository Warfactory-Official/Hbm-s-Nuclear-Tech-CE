package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerMachineGasCent;
import com.hbm.tileentity.machine.TileEntityMachineGasCent;
import com.hbm.util.I18nUtil;
import com.hbm.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

public class GUIMachineGasCent extends GuiInfoContainer {

  private static final ResourceLocation TEXTURE =
      new ResourceLocation(Tags.MODID + ":textures/gui/processing/gui_centrifuge_gas.png");

  private final TileEntityMachineGasCent gasCent;

  public GUIMachineGasCent(InventoryPlayer invPlayer, TileEntityMachineGasCent teGasCent) {
    super(new ContainerMachineGasCent(invPlayer, teGasCent));
    this.gasCent = teGasCent;
    this.xSize = 206;
    this.ySize = 204;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    drawTankInfo(mouseX, mouseY);
    drawElectricityInfo(
        this, mouseX, mouseY, guiLeft + 182, guiTop + 17, 16, 52, gasCent.power, gasCent.maxPower);
    drawInfoTexts(mouseX, mouseY);
    super.renderHoveredToolTip(mouseX, mouseY);
  }

  private void drawTankInfo(int mouseX, int mouseY) {
    String[] inTankInfo = {
      gasCent.inputTank.getTankType().getName(),
      gasCent.inputTank.getFill() + " / " + gasCent.inputTank.getMaxFill() + " mB"
    };

    if (gasCent.inputTank.getTankType().getIfHighSpeed()) {
      inTankInfo[0] =
          (gasCent.processingSpeed > gasCent.processingSpeed - 70)
              ? TextFormatting.DARK_RED + inTankInfo[0]
              : TextFormatting.GOLD + inTankInfo[0];
    }

    String[] outTankInfo = {
      gasCent.outputTank.getTankType().getName(),
      gasCent.outputTank.getFill() + " / " + gasCent.outputTank.getMaxFill() + " mB"
    };

    if (gasCent.outputTank.getTankType().getIfHighSpeed()) {
      outTankInfo[0] = TextFormatting.GOLD + outTankInfo[0];
    }

    drawCustomInfoStat(
        mouseX, mouseY, guiLeft + 15, guiTop + 15, 24, 55, mouseX, mouseY, inTankInfo);
    drawCustomInfoStat(
        mouseX, mouseY, guiLeft + 137, guiTop + 15, 25, 55, mouseX, mouseY, outTankInfo);
  }

  private void drawInfoTexts(int mouseX, int mouseY) {
    drawCustomInfoStat(
        mouseX,
        mouseY,
        guiLeft - 12,
        guiTop + 16,
        16,
        16,
        guiLeft - 8,
        guiTop + 32,
        I18nUtil.resolveKeyArray("desc.gui.gasCent.enrichment"));
    drawCustomInfoStat(
        mouseX,
        mouseY,
        guiLeft - 12,
        guiTop + 32,
        16,
        16,
        guiLeft - 8,
        guiTop + 48,
        I18nUtil.resolveKeyArray("desc.gui.gasCent.output"));
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    fontRenderer.drawString(I18n.format("container.inventory"), 8, ySize - 96 + 2, 4210752);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    super.drawDefaultBackground();
    GlStateManager.color(1F, 1F, 1F, 1F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

    drawEnergyBar();
    drawProgressBar();
    renderTanks();
    drawInfoPanels();
  }

  private void drawEnergyBar() {
    int powerHeight = (int) gasCent.getPowerRemainingScaled(52);
    drawTexturedModalRect(
        guiLeft + 182, guiTop + 69 - powerHeight, 206, 52 - powerHeight, 16, powerHeight);
  }

  private void drawProgressBar() {
    int progressWidth = gasCent.getCentrifugeProgressScaled(36);
    drawTexturedModalRect(guiLeft + 70, guiTop + 35, 206, 52, progressWidth, 13);
  }

  private void renderTanks() {
    renderTank(
        guiLeft + 16,
        guiTop + 16,
        zLevel,
        6,
        52,
        gasCent.inputTank.getFill(),
        gasCent.inputTank.getMaxFill());
    renderTank(
        guiLeft + 32,
        guiTop + 16,
        zLevel,
        6,
        52,
        gasCent.inputTank.getFill(),
        gasCent.inputTank.getMaxFill());
    renderTank(
        guiLeft + 138,
        guiTop + 16,
        zLevel,
        6,
        52,
        gasCent.outputTank.getFill(),
        gasCent.outputTank.getMaxFill());
    renderTank(
        guiLeft + 154,
        guiTop + 16,
        zLevel,
        6,
        52,
        gasCent.outputTank.getFill(),
        gasCent.outputTank.getMaxFill());
  }

  private void drawInfoPanels() {
    drawInfoPanel(guiLeft - 12, guiTop + 16, 16, 16, 3);
    drawInfoPanel(guiLeft - 12, guiTop + 32, 16, 16, 2);
  }

  public void renderTank(int x, int y, double z, int width, int height, int fluid, int maxFluid) {
    boolean wasBlendEnabled = RenderUtil.isBlendEnabled();
    if (!wasBlendEnabled) GlStateManager.enableBlend();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

    y += height;
    Minecraft.getMinecraft()
        .getTextureManager()
        .bindTexture(gasCent.tank.getTankType().getTexture());

    int scaledHeight = (fluid * height) / maxFluid;
    double minX = x;
    double maxX = x + width;
    double minY = y - height;
    double maxY = y - (height - scaledHeight);
    double minU = 0.0;
    double maxU = width / 16.0;
    double minV = 1.0;
    double maxV = 1.0 - scaledHeight / 16.0;

    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();
    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    buffer.pos(minX, maxY, z).tex(minU, maxV).endVertex();
    buffer.pos(maxX, maxY, z).tex(maxU, maxV).endVertex();
    buffer.pos(maxX, minY, z).tex(maxU, minV).endVertex();
    buffer.pos(minX, minY, z).tex(minU, minV).endVertex();
    tessellator.draw();

    if (!wasBlendEnabled) GlStateManager.disableBlend();
  }
}
