package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityHeatBoilerIndustrial;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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

public class MachineHeatBoilerIndustrial extends BlockDummyable
    implements ILookOverlay, ITooltipProvider {

  public MachineHeatBoilerIndustrial(Material material, String name) {
    super(material, name);
  }

  @Override
  public TileEntity createNewTileEntity(@NotNull World world, int meta) {

    if (meta >= 12) return new TileEntityHeatBoilerIndustrial();
    if (meta >= extra) return new TileEntityProxyCombo(false, false, true);
    return null;
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

      ItemStack heldItem = player.getHeldItem(hand);

      if (!heldItem.isEmpty() && heldItem.getItem() instanceof IItemFluidIdentifier) {
        BlockPos corePos = this.findCore(world, pos);

        if (corePos == null) return false;

        TileEntity te = world.getTileEntity(corePos);

        if (!(te instanceof TileEntityHeatBoilerIndustrial boiler)) return false;

        FluidType type =
            ((IItemFluidIdentifier) heldItem.getItem())
                .getType(world, corePos.getX(), corePos.getY(), corePos.getZ(), heldItem);

        if (type.hasTrait(FT_Heatable.class)
            && type.getTrait(FT_Heatable.class).getEfficiency(FT_Heatable.HeatingType.BOILER) > 0) {
          boiler.tanks[0].setTankType(type);
          boiler.markDirty();
          player.sendMessage(
              new TextComponentString("Changed type to ")
                  .appendSibling(new TextComponentTranslation(type.getConditionalName()))
                  .appendSibling(new TextComponentString("!"))
                  .setStyle(new Style().setColor(TextFormatting.YELLOW)));
        }
        return true;
      }
      return false;

    } else {
      return true;
    }
  }

  @Override
  public int[] getDimensions() {
    return new int[] {4, 0, 1, 1, 1, 1};
  }

  @Override
  public int getOffset() {
    return 1;
  }

  @Override
  public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
    super.fillSpace(world, x, y, z, dir, o);

    this.makeExtra(world, x - dir.offsetX + 1, y, z - dir.offsetZ);
    this.makeExtra(world, x - dir.offsetX - 1, y, z - dir.offsetZ);
    this.makeExtra(world, x - dir.offsetX, y, z - dir.offsetZ + 1);
    this.makeExtra(world, x - dir.offsetX, y, z - dir.offsetZ - 1);
    this.makeExtra(world, x - dir.offsetX, y + 4, z - dir.offsetZ);
  }

  @Override
  public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {

    BlockPos corePos = this.findCore(world, new BlockPos(x, y, z));

    if (corePos == null) return;

    TileEntity te = world.getTileEntity(corePos);

    if (!(te instanceof TileEntityHeatBoilerIndustrial boiler)) return;

    List<String> text = new ArrayList<>();
    text.add(String.format(Locale.US, "%,d", boiler.heat) + "TU");
    text.add(
        ChatFormatting.GREEN
            + "-> "
            + ChatFormatting.RESET
            + boiler.tanks[0].getTankType().getLocalizedName()
            + ": "
            + String.format(Locale.US, "%,d", boiler.tanks[0].getFill())
            + " / "
            + String.format(Locale.US, "%,d", boiler.tanks[0].getMaxFill())
            + "mB");
    text.add(
        ChatFormatting.RED
            + "<- "
            + ChatFormatting.RESET
            + boiler.tanks[1].getTankType().getLocalizedName()
            + ": "
            + String.format(Locale.US, "%,d", boiler.tanks[1].getFill())
            + " / "
            + String.format(Locale.US, "%,d", boiler.tanks[1].getMaxFill())
            + "mB");

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
