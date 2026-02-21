package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineSteamEngine;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MachineSteamEngine extends BlockDummyable implements ITooltipProvider, ILookOverlay {
  public MachineSteamEngine(Material materialIn, String s) {
    super(materialIn, s);
  }

  @Override
  public TileEntity createNewTileEntity(@NotNull World world, int meta) {
    if (meta >= 12) return new TileEntityMachineSteamEngine();
    if (meta >= extra) return new TileEntityProxyCombo().power().fluid();
    return null;
  }

  @Override
  public int[] getDimensions() {
    return new int[] {1, 0, 5, 1, 1, 1};
  }

  @Override
  public int getOffset() {
    return 1;
  }

  @Override
  public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
    super.fillSpace(world, x, y, z, dir, o);

    x = x + dir.offsetX * o;
    z = z + dir.offsetZ * o;

    ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

    this.makeExtra(world, x + rot.offsetX, y + 1, z + rot.offsetZ);
    this.makeExtra(world, x + rot.offsetX + dir.offsetX, y + 1, z + rot.offsetZ + dir.offsetZ);
    this.makeExtra(world, x + rot.offsetX - dir.offsetX, y + 1, z + rot.offsetZ - dir.offsetZ);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void addInformation(
      @NotNull ItemStack stack,
      World player,
      @NotNull List<String> tooltip,
      @NotNull ITooltipFlag advanced) {
    this.addStandardInfo(tooltip);
    super.addInformation(stack, player, tooltip, advanced);
  }

  @Override
  public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
    BlockPos corePos = this.findCore(world, pos);

    if(corePos == null)
        return;

    TileEntity te = world.getTileEntity(corePos);

    if (!(te instanceof TileEntityMachineSteamEngine engine)) return;

    List<String> text = new ArrayList<>();
    text.add(
        TextFormatting.GREEN
            + "-> "
            + TextFormatting.RESET
            + engine.tanks[0].getTankType().getLocalizedName()
            + ": "
            + String.format(Locale.US, "%,d", engine.tanks[0].getFill())
            + " / "
            + String.format(Locale.US, "%,d", engine.tanks[0].getMaxFill())
            + "mB");
    text.add(
        TextFormatting.RED
            + "<- "
            + TextFormatting.RESET
            + engine.tanks[1].getTankType().getLocalizedName()
            + ": "
            + String.format(Locale.US, "%,d", engine.tanks[1].getFill())
            + " / "
            + String.format(Locale.US, "%,d", engine.tanks[1].getMaxFill())
            + "mB");

    ILookOverlay.printGeneric(
        event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
  }
}
