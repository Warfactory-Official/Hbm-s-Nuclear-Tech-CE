package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MachineRotaryFurnace extends BlockDummyable implements ILookOverlay {

  public MachineRotaryFurnace(Material material, String s) {
    super(material, s);
  }

  @Override
  public TileEntity createNewTileEntity(@NotNull World world, int meta) {
    if (meta >= 12) return new TileEntityMachineRotaryFurnace();
    if (meta >= 6) return new TileEntityProxyCombo(true, false, true);
    return null;
  }

  @Override
  public boolean onBlockActivated(@NotNull World world, BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
    return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
  }

  @Override
  public int[] getDimensions() {
    return new int[] {4, 0, 1, 1, 2, 2};
  }

  @Override
  public int getOffset() {
    return 1;
  }

  @Override
  protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
    super.fillSpace(world, x, y, z, dir, o);
    x += dir.offsetX * o;
    z += dir.offsetZ * o;

    ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

    // back
    for (int i = -2; i <= 2; i++) {
      this.makeExtra(world, x - dir.offsetX + rot.offsetX * i, y, z - dir.offsetZ + rot.offsetZ * i);
    }
    // side fluid
    this.makeExtra(world, x + dir.offsetX + rot.offsetX * 2, y, z + dir.offsetZ + rot.offsetZ * 2);
    // exhaust
    this.makeExtra(world, x + rot.offsetX, y + 4, z + rot.offsetZ);
    // solid fuel
    this.makeExtra(world, x + dir.offsetX + rot.offsetX, y, z + dir.offsetZ + rot.offsetZ);
  }

  @Override
  public void printHook(Pre event, World world, BlockPos pos) {
      BlockPos corePos = this.findCore(world, pos);

      if(corePos == null)
          return;

    TileEntity te = world.getTileEntity(corePos);

    if (!(te instanceof TileEntityMachineRotaryFurnace furnace)) return;

    ForgeDirection dir = ForgeDirection.getOrientation(furnace.getBlockMetadata() - offset);

    List<String> text = new ArrayList<>();

    // steam
    if (hitCheck(dir, corePos.getX(), corePos.getY(), corePos.getZ(), -1, -1, 0, pos.getX(), pos.getY(), pos.getZ()) || hitCheck(dir, corePos.getX(), corePos.getY(), corePos.getZ(), -1, -2, 0, pos.getX(), pos.getY(), pos.getZ())) {
      text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + furnace.tanks[1].getTankType().getLocalizedName());
      text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + furnace.tanks[2].getTankType().getLocalizedName());
    }

    // fluids
    if (hitCheck(dir, corePos.getX(), corePos.getY(), corePos.getZ(), 1, 2, 0, pos.getX(), pos.getY(), pos.getZ()) || hitCheck(dir, corePos.getX(), corePos.getY(), corePos.getZ(), -1, 2, 0, pos.getX(), pos.getY(), pos.getZ())) {
      text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + furnace.tanks[0].getTankType().getLocalizedName());
    }

    if (hitCheck(dir, corePos.getX(), corePos.getY(), corePos.getZ(), 1, 1, 0, pos.getX(), pos.getY(), pos.getZ())) {
      text.add(TextFormatting.YELLOW + "-> " + TextFormatting.RESET + "Fuel");
    }

    if (!text.isEmpty()) {
      ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
  }

  protected boolean hitCheck(ForgeDirection dir, int coreX, int coreY, int coreZ, int exDir, int exRot, int exY, int hitX, int hitY, int hitZ) {

    ForgeDirection turn = dir.getRotation(ForgeDirection.DOWN);

    int iX = coreX + dir.offsetX * exDir + turn.offsetX * exRot;
    int iY = coreY + exY;
    int iZ = coreZ + dir.offsetZ * exDir + turn.offsetZ * exRot;

    return iX == hitX && iZ == hitZ && iY == hitY;
  }
}
