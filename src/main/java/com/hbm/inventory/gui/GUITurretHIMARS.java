package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.packet.toserver.AuxButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import com.hbm.tileentity.turret.TileEntityTurretHIMARS;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUITurretHIMARS extends GUITurretBase {
  private static final ResourceLocation texture =
      new ResourceLocation(Tags.MODID + ":textures/gui/weapon/gui_turret_himars.png");

  public GUITurretHIMARS(InventoryPlayer invPlayer, TileEntityTurretBaseNT turretBaseNT) {
    super(invPlayer, turretBaseNT);
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float f) {
    super.drawScreen(mouseX, mouseY, f);

    TileEntityTurretHIMARS himars = (TileEntityTurretHIMARS) turret;
    String mode = himars.mode == TileEntityTurretHIMARS.FiringMode.AUTO ? "artillery" : "manual";
    this.drawCustomInfoStat(
        mouseX,
        mouseY,
        guiLeft + 151,
        guiTop + 16,
        18,
        18,
        mouseX,
        mouseY,
        I18nUtil.resolveKeyArray("turret.arty." + mode));
  }

  @Override
  protected void mouseClicked(int x, int y, int i) throws IOException {
    super.mouseClicked(x, y, i);

    if (guiLeft + 151 <= x && guiLeft + 151 + 18 > x && guiTop + 16 < y && guiTop + 16 + 18 >= y) {

      playClickSound();
      PacketDispatcher.wrapper.sendToServer(
          new AuxButtonPacket(
              turret.getPos().getX(), turret.getPos().getY(), turret.getPos().getZ(), 0, 5));
    }
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

    TileEntityTurretHIMARS.FiringMode mode = ((TileEntityTurretHIMARS) turret).mode;
    if (mode == TileEntityTurretHIMARS.FiringMode.MANUAL)
      drawTexturedModalRect(guiLeft + 151, guiTop + 16, 210, 0, 18, 18);
  }

  @Override
  protected ResourceLocation getTexture() {
    return texture;
  }
}
