package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.container.ContainerCombustionEngine;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemPistons;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineCombustionEngine;
import com.hbm.util.EnumUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUICombustionEngine extends GuiInfoContainer {

  private static final ResourceLocation texture =
      new ResourceLocation(Tags.MODID + ":textures/gui/generators/gui_combustion.png");
  private final TileEntityMachineCombustionEngine engine;
  private int setting;
  private boolean isMouseLocked = false;

  public GUICombustionEngine(InventoryPlayer invPlayer, TileEntityMachineCombustionEngine tile) {
    super(new ContainerCombustionEngine(invPlayer, tile));
    engine = tile;
    this.setting = engine.setting;

    this.xSize = 176;
    this.ySize = 203;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);

    if (!isMouseLocked) {
      this.drawElectricityInfo(
          this,
          mouseX,
          mouseY,
          guiLeft + 143,
          guiTop + 17,
          16,
          52,
          engine.getPower(),
          TileEntityMachineCombustionEngine.maxPower);
      engine.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 35, guiTop + 17, 16, 52);
    }

    if (isMouseLocked
        || (guiLeft + 80 <= mouseX
            && guiLeft + 80 + 34 > mouseX
            && guiTop + 38 < mouseY
            && guiTop + 38 + 8 >= mouseY)) {
      drawHoveringText(
          ((setting * 2) / 10D) + "mB/t",
          MathHelper.clamp(mouseX, guiLeft + 80, guiLeft + 114),
          MathHelper.clamp(mouseY, guiTop + 38, guiTop + 46));
    }

    ItemStack stack = engine.inventory.getStackInSlot(2);

    if (!stack.isEmpty() && stack.getItem() == ModItems.piston_set) {
      double power = 0;
      if (engine.tank.getTankType().hasTrait(FT_Combustible.class)) {
        FT_Combustible trait = engine.tank.getTankType().getTrait(FT_Combustible.class);
        int i = engine.inventory.getStackInSlot(2).getItemDamage();
        ItemPistons.EnumPistonType piston =
            EnumUtil.grabEnumSafely(ItemPistons.EnumPistonType.VALUES, i);
        power =
            setting
                * 0.2
                * trait.getCombustionEnergy()
                / 1_000D
                * piston.eff[trait.getGrade().ordinal()];
      }

      String c = String.valueOf(TextFormatting.YELLOW);

      String[] text =
          new String[] {
            c + String.format(Locale.US, "%,d", (int) (power)) + " HE/t",
            c + String.format(Locale.US, "%,d", (int) (power * 20)) + " HE/s"
          };

      drawCustomInfoStat(mouseX, mouseY, guiLeft + 79, guiTop + 50, 35, 14, mouseX, mouseY, text);
    }

    drawCustomInfoStat(
        mouseX,
        mouseY,
        guiLeft + 79,
        guiTop + 13,
        35,
        15,
        mouseX,
        mouseY,
        Collections.singletonList("Ignition"));

    if (isMouseLocked) {

      int setting = (mouseX - guiLeft - 81) * 30 / 32;
      setting = MathHelper.clamp(setting, 0, 30);

      if (this.setting != setting) {
        this.setting = setting;
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("setting", setting);
          PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(
                  data, engine.getPos().getX(), engine.getPos().getY(), engine.getPos().getZ()));
      }
    }

    super.renderHoveredToolTip(mouseX, mouseY);
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);

    if (guiLeft + 89 <= mouseX
        && guiLeft + 89 + 16 > mouseX
        && guiTop + 13 < mouseY
        && guiTop + 13 + 14 >= mouseY) {
      playClickSound();
      NBTTagCompound data = new NBTTagCompound();
      data.setBoolean("turnOn", true);
        PacketThreading.createSendToServerThreadedPacket(new NBTControlPacket(
              data, engine.getPos().getX(), engine.getPos().getY(), engine.getPos().getZ()));
    }

    if (guiLeft + 79 <= mouseX
        && guiLeft + 79 + 36 > mouseX
        && guiTop + 38 < mouseY
        && guiTop + 38 + 8 >= mouseY) {
      playClickSound();
      isMouseLocked = true;
    }
  }

  @Override
  protected void mouseReleased(int mouseX, int mouseY, int state) {
    super.mouseReleased(mouseX, mouseY, state);

    if (isMouseLocked) {

      if (state == 0 || state == 1) {
        isMouseLocked = false;
      }
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    this.fontRenderer.drawString(
        I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
    super.drawDefaultBackground();
    
    GlStateManager.enableBlend();
    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

    ItemStack stack = engine.inventory.getStackInSlot(2);

    if (!stack.isEmpty() && stack.getItem() == ModItems.piston_set) {
      int i = stack.getItemDamage();
      drawTexturedModalRect(guiLeft + 80, guiTop + 51, 176, 52 + i * 12, 25, 12);
    }

    drawTexturedModalRect(guiLeft + 79 + (setting * 32 / 30), guiTop + 38, 192, 15, 4, 8);

    if (engine.isOn) {
      drawTexturedModalRect(guiLeft + 79, guiTop + 13, 192, 0, 35, 15);
    }

    int i = (int) (engine.power * 53 / TileEntityMachineCombustionEngine.maxPower);
    drawTexturedModalRect(guiLeft + 143, guiTop + 69 - i, 176, 52 - i, 16, i);

    engine.tank.renderTank(guiLeft + 35, guiTop + 69, this.zLevel, 16, 52);

    GlStateManager.disableBlend();
  }
}
