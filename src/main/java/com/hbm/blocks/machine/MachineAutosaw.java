package com.hbm.blocks.machine;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineAutosaw;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MachineAutosaw extends BlockContainer
    implements ILookOverlay, ITooltipProvider, IToolable {

  public MachineAutosaw(Material material, String s) {
    super(material);
    this.setTranslationKey(s);
    this.setRegistryName(s);

    ModBlocks.ALL_BLOCKS.add(this);
  }

  @Override
  public TileEntity createNewTileEntity(@NotNull World world, int meta) {
    return new TileEntityMachineAutosaw();
  }

  @Override
  public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
    return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
  }

  @Override
  public boolean isOpaqueCube(@NotNull IBlockState state) {
    return false;
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

      if (!player.getHeldItem(hand).isEmpty()
          && player.getHeldItem(hand).getItem() instanceof IItemFluidIdentifier) {

        TileEntityMachineAutosaw saw = (TileEntityMachineAutosaw) world.getTileEntity(pos);

        FluidType type =
            ((IItemFluidIdentifier) player.getHeldItem(hand).getItem())
                .getType(world, pos.getX(), pos.getY(), pos.getZ(), player.getHeldItem(hand));
        if (TileEntityMachineAutosaw.acceptedFuels.contains(type)) {
          Objects.requireNonNull(saw).tank.setTankType(type);
          saw.markDirty();
          player.sendMessage(
              new TextComponentString("Changed type to ")
                  .appendSibling(new TextComponentTranslation(type.getConditionalName()))
                  .appendSibling(new TextComponentString("!"))
                  .setStyle(new Style().setColor(TextFormatting.YELLOW)));
          return true;
        }
      }

      return false;
    }

    return true;
  }

  @Override
  public boolean onScrew(
      World world,
      EntityPlayer player,
      int x,
      int y,
      int z,
      EnumFacing side,
      float fX,
      float fY,
      float fZ,
      EnumHand hand,
      ToolType tool) {
    if (tool != ToolType.SCREWDRIVER) return false;

    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

    if (!(te instanceof TileEntityMachineAutosaw saw)) return false;

    saw.isSuspended = !saw.isSuspended;
    saw.markDirty();

    return true;
  }

  @Override
  public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {

    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

    if (!(te instanceof TileEntityMachineAutosaw saw)) return;

    List<String> text = new ArrayList<>();
    text.add(
        saw.tank.getTankType().getLocalizedName()
            + ": "
            + saw.tank.getFill()
            + "/"
            + saw.tank.getMaxFill()
            + "mB");

    if (saw.isSuspended) {
      text.add(
          ChatFormatting.RED
              + "! "
              + I18nUtil.resolveKey(getTranslationKey() + ".suspended")
              + " !");
    }

    ILookOverlay.printGeneric(
        event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
  }

  @Override
  public void addInformation(
      @NotNull ItemStack stack,
      World worldIn,
      @NotNull List<String> list,
      @NotNull ITooltipFlag flagIn) {
    this.addStandardInfo(list);
    super.addInformation(stack, worldIn, list, flagIn);
  }
}
