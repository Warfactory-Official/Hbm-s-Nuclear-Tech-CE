package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerCounterTorch;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.network.TileEntityRadioTorchCounter;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

import static com.hbm.render.NTMRenderHelper.bindTexture;
import static com.hbm.util.SoundUtil.playClickSound;

public class GUICounterTorch extends GuiInfoContainer {

  protected static final ResourceLocation texture =
      new ResourceLocation(Tags.MODID + ":textures/gui/machine/gui_rtty_counter.png");

  protected TileEntityRadioTorchCounter counter;
  protected GuiTextField[] frequency;

  public GUICounterTorch(InventoryPlayer invPlayer, TileEntityRadioTorchCounter counter) {
    super(new ContainerCounterTorch(invPlayer, counter));
    this.counter = counter;

    this.xSize = 218;
    this.ySize = 238;
  }

  @Override
  public void initGui() {
    super.initGui();

    Keyboard.enableRepeatEvents(true);

    this.frequency = new GuiTextField[3];

    for (int i = 0; i < 3; i++) {

      this.frequency[i] =
          new GuiTextField(0, this.fontRenderer, guiLeft + 29, guiTop + 21 + 44 * i, 86, 14);
      this.frequency[i].setTextColor(0x00ff00);
      this.frequency[i].setDisabledTextColour(0x00ff00);
      this.frequency[i].setEnableBackgroundDrawing(false);
      this.frequency[i].setMaxStringLength(10);
      this.frequency[i].setText(counter.channel[i] == null ? "" : counter.channel[i]);
    }
  }

  @Override
  public void drawScreen(int x, int y, float partialTicks) {
    this.drawDefaultBackground();
    super.drawScreen(x, y, partialTicks);
    this.renderHoveredToolTip(x, y);

    if (guiLeft + 193 <= x && guiLeft + 193 + 18 > x && guiTop + 8 < y && guiTop + 8 + 18 >= y) {
      drawHoveringText(List.of(new String[] {counter.polling ? "Polling" : "State Change"}), x, y);
    }
    if (guiLeft + 193 <= x && guiLeft + 193 + 18 > x && guiTop + 30 < y && guiTop + 30 + 18 >= y) {
      drawHoveringText(List.of(new String[] {"Save Settings"}), x, y);
    }

    if (this.mc.player.inventory.getItemStack() == ItemStack.EMPTY) {
      for (int i = 0; i < 3; ++i) {
        Slot slot = this.inventorySlots.inventorySlots.get(i);

        if (this.isMouseOverSlot(slot, x, y) && counter.matcher.modes[i] != null) {
          this.drawHoveringText(
              List.of(
                  new String[] {
                    ChatFormatting.RED + "Right click to change",
                    ModulePatternMatcher.getLabel(counter.matcher.modes[i])
                  }),
              x,
              y - 30);
        }
      }
    }
  }

  @Override
  protected void mouseClicked(int x, int y, int i) throws IOException {
    super.mouseClicked(x, y, i);

    for (int j = 0; j < 3; j++) this.frequency[j].mouseClicked(x, y, i);

    if (guiLeft + 193 <= x && guiLeft + 193 + 18 > x && guiTop + 8 < y && guiTop + 8 + 18 >= y) {

      playClickSound();
      NBTTagCompound data = new NBTTagCompound();
      data.setBoolean("polling", true);
      PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, counter.getPos()));
    }

    if (guiLeft + 193 <= x && guiLeft + 193 + 18 > x && guiTop + 30 < y && guiTop + 30 + 18 >= y) {
      playClickSound();
      NBTTagCompound data = new NBTTagCompound();

      for (int j = 0; j < 3; j++) data.setString("c" + j, this.frequency[j].getText());
      PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, counter.getPos()));
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int x, int y) {
    String name = I18nUtil.resolveKey(this.counter.getName());
    this.fontRenderer.drawString(
        name, 184 / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
    this.fontRenderer.drawString(
        I18n.format("container.inventory"), 16, this.ySize - 96 + 2, 4210752);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    bindTexture(texture);
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

    if (counter.polling) {
      drawTexturedModalRect(guiLeft + 193, guiTop + 8, 218, 0, 18, 18);
    }

    for (int i = 0; i < 3; i++) this.frequency[i].drawTextBox();
  }

  @Override
  protected void keyTyped(char c, int i) throws IOException {

    for (int j = 0; j < 3; j++) if (this.frequency[j].textboxKeyTyped(c, i)) return;

    super.keyTyped(c, i);
  }

  @Override
  public void onGuiClosed() {
    super.onGuiClosed();
    Keyboard.enableRepeatEvents(false);
  }
}
