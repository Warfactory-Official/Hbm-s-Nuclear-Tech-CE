package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.network.TileEntityRadioTorchBase;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public abstract class RadioTorchRWBase extends RadioTorchBase {

  @Override
  @SideOnly(Side.CLIENT)
  public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
    TileEntity te = world.getTileEntity(pos);

    if (te instanceof TileEntityRadioTorchBase radio) {
      List<String> text = new ArrayList<>();
      if (radio.channel != null && !radio.channel.isEmpty())
        text.add(ChatFormatting.AQUA + "Freq: " + radio.channel);
      text.add(ChatFormatting.RED + "Signal: " + radio.lastState);
      ILookOverlay.printGeneric(
          event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
  }
}
