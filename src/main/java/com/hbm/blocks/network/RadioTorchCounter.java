package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.container.ContainerCounterTorch;
import com.hbm.inventory.gui.GUICounterTorch;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.network.TileEntityRadioTorchCounter;
import com.hbm.util.Compat;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RadioTorchCounter extends RadioTorchBase implements IGUIProvider {

  public RadioTorchCounter(String regName) {
    super();
    this.setTranslationKey(regName);
    this.setRegistryName(regName);

    ModBlocks.ALL_BLOCKS.add(this);
  }

  @Override
  public boolean onBlockActivated(
      World world,
      @NotNull BlockPos pos,
      @NotNull IBlockState state,
      @NotNull EntityPlayer player,
      @NotNull EnumHand hand,
      @NotNull EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ) {
    if (!world.isRemote && !player.isSneaking()) {
      FMLNetworkHandler.openGui(
          player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
      return true;
    } else {
      return !player.isSneaking();
    }
  }

  @Override
  public TileEntity createNewTileEntity(World world, int meta) {
    return new TileEntityRadioTorchCounter();
  }

  @Override
  public boolean canPlaceBlockOnSide(
      @NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing side) {
    if (!super.canPlaceBlockOnSide(worldIn, pos, side)) return false;
    BlockPos checkPos = pos.offset(side.getOpposite());
    IBlockState checkState = worldIn.getBlockState(checkPos);

    return canBlockStay(worldIn, side, checkState.getBlock(), checkPos, checkState);
  }

  public boolean canBlockStay(
      World world, EnumFacing dir, Block b, BlockPos checkPos, IBlockState checkState) {
    if (b.isSideSolid(checkState, world, checkPos, dir)
        || (b.isFullCube(checkState) && !b.isAir(checkState, world, checkPos))) return true;
    TileEntity te =
        Compat.getTileStandard(world, checkPos.getX(), checkPos.getY(), checkPos.getZ());
    return te instanceof IInventory;
  }

  @Override
  public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new ContainerCounterTorch(
        player.inventory, (TileEntityRadioTorchCounter) world.getTileEntity(new BlockPos(x, y, z)));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new GUICounterTorch(
        player.inventory, (TileEntityRadioTorchCounter) world.getTileEntity(new BlockPos(x, y, z)));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
    TileEntity te = world.getTileEntity(pos);

    if (te instanceof TileEntityRadioTorchCounter radio) {
      List<String> text = new ArrayList<>();

      for (int i = 0; i < 3; i++) {
        if (!radio.channel[i].isEmpty()) {
          text.add(ChatFormatting.AQUA + "Freq " + (i + 1) + ": " + radio.channel[i]);
          text.add(ChatFormatting.RED + "Signal " + (i + 1) + ": " + radio.lastCount[i]);
        }
      }

      ILookOverlay.printGeneric(
          event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
  }
}
