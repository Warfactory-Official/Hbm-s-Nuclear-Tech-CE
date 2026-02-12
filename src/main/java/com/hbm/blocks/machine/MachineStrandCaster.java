package com.hbm.blocks.machine;

import com.hbm.api.block.ICrucibleAcceptor;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineStrandCaster;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MachineStrandCaster extends BlockDummyable implements ICrucibleAcceptor, ILookOverlay, IToolable {

  public MachineStrandCaster(Material material, String name) {
    super(material, name);
  }

  // reminder, if the machine is a solid brick, get dimensions will already
  // handle it without the need to use fillSpace
  // the order is up, down, forward, backward, left, right
  // x is for left(-)/right(+), z is for forward(+)/backward(-), y you already
  // know
  @Override
  public int[] getDimensions() {
    return new int[] {0, 0, 6, 0, 1, 0};
  }

  @Override
  public int getOffset() {
    return 0;
  }

  @Override
  public TileEntity createNewTileEntity(@NotNull World world, int meta) {
    if (meta >= 12) return new TileEntityMachineStrandCaster();
    if (meta >= 6) return new TileEntityProxyCombo(true, false, true).moltenMetal();
    return null;
  }

  @Override
  public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
    super.fillSpace(world, x, y, z, dir, o);

    x += dir.offsetX * o;
    z += dir.offsetZ * o;

    ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

    // up,down;forward,backward;left,right
    MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {2, 0, 1, 0, 1, 0}, this, dir);
    // Fluid ports
    this.makeExtra(world, x + rot.offsetX - dir.offsetX, y, z + rot.offsetZ - dir.offsetZ);
    this.makeExtra(world, x - dir.offsetX, y, z - dir.offsetZ);
    this.makeExtra(world, x - dir.offsetX * 5, y, z - dir.offsetZ * 5);
    this.makeExtra(world, x + rot.offsetX - dir.offsetX * 5, y, z + rot.offsetZ - dir.offsetZ * 5);
    // Molten slop ports
    this.makeExtra(world, x + rot.offsetX - dir.offsetX, y + 2, z + rot.offsetZ - dir.offsetZ);
    this.makeExtra(world, x - dir.offsetX, y + 2, z - dir.offsetZ);
    this.makeExtra(world, x + rot.offsetX, y + 2, z + rot.offsetZ);
    this.makeExtra(world, x, y + 2, z);
  }

  @Override
  public boolean canAcceptPartialPour(World world, BlockPos pos, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {

    TileEntity poured = world.getTileEntity(pos);
    if (!(poured instanceof TileEntityProxyCombo && ((TileEntityProxyCombo) poured).moltenMetal)) return false;

    BlockPos corePos = this.findCore(world, pos);
    if (corePos == null) return false;
    TileEntity tile = world.getTileEntity(corePos);
    if (!(tile instanceof TileEntityMachineStrandCaster caster)) return false;

    return caster.canAcceptPartialPour(world, new BlockPos(pos.getX(), pos.getY(), pos.getZ()), dX, dY, dZ, side, stack);
  }

  @Override
  public Mats.MaterialStack pour(World world, BlockPos pos, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {

    TileEntity poured = world.getTileEntity(pos);
    if (!(poured instanceof TileEntityProxyCombo && ((TileEntityProxyCombo) poured).moltenMetal)) return stack;

    int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
    if(posC == null) return stack;
    TileEntity tile = world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));
    if (!(tile instanceof TileEntityMachineStrandCaster caster)) return stack;

    return caster.pour(world, pos, dX, dY, dZ, side, stack);
  }

  @Override
  public boolean canAcceptPartialFlow(World world, BlockPos pos, ForgeDirection side, Mats.MaterialStack stack) {
    return false;
  }

  @Override
  public Mats.MaterialStack flow(World world, BlockPos pos, ForgeDirection side, Mats.MaterialStack stack) {
    return null;
  }

  @Override
  public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
    if (world.isRemote) {
      return true;
    }

    int[] coords = findCore(world, pos.getX(), pos.getY(), pos.getZ());
    TileEntityMachineStrandCaster caster = (TileEntityMachineStrandCaster) world.getTileEntity(new BlockPos(coords[0], coords[1], coords[2]));
    if (caster != null) {
      // insert mold
      ItemStack heldItem = player.getHeldItem(hand);
      if (!heldItem.isEmpty()
          && heldItem.getItem() == ModItems.mold
          && caster.inventory.getStackInSlot(0).isEmpty()) {
        caster.inventory.setStackInSlot(0, heldItem.copy());
        caster.inventory.getStackInSlot(0).setCount(1);
        player.getHeldItem(hand).shrink(1);
        world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
        caster.markDirty();
        return true;
      }

      if (!heldItem.isEmpty()
          && heldItem.getItem() instanceof ItemTool
          && heldItem.getItem().getToolClasses(heldItem).contains("shovel")) {
        if (caster.amount > 0) {
          ItemStack scrap = ItemScraps.create(new Mats.MaterialStack(caster.type, caster.amount));
          if (!player.inventory.addItemStackToInventory(scrap)) {
            EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, scrap);
            world.spawnEntity(item);
          } else {
            player.inventoryContainer.detectAndSendChanges();
          }
          caster.amount = 0;
          caster.type = null;
          caster.markDirty();
        }
        return true;
      }
    }
    return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
  }

  @Override
  public void breakBlock(@NotNull World world, @NotNull BlockPos pos, IBlockState state) {

    TileEntity te = world.getTileEntity(pos);
    if (te instanceof TileEntityMachineStrandCaster caster) {

      if (caster.amount > 0) {
        ItemStack scrap = ItemScraps.create(new Mats.MaterialStack(caster.type, caster.amount));
        EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, scrap);
        world.spawnEntity(item);
        caster.amount = 0; // just for safety
      }
    }
    super.breakBlock(world, pos, state);
  }

  public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
    BlockPos corePos = findCore(world, pos);
    if (corePos == null) return;

    TileEntityMachineStrandCaster caster = (TileEntityMachineStrandCaster) world.getTileEntity(corePos);

    List<String> text = new ArrayList<>();
    if (caster != null) {
      if (caster.inventory.getStackInSlot(0).isEmpty()) {
        text.add(TextFormatting.RED + I18nUtil.resolveKey("foundry.noCast"));
      } else if (caster.inventory.getStackInSlot(0).getItem() == ModItems.mold) {
        text.add(TextFormatting.BLUE + caster.getInstalledMold().getTitle());
      }
    }
    ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xFF4000, 0x401000, text);
  }

  @Override
  public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
    x += dir.offsetX * o;
    z += dir.offsetZ * o;

    if (!MultiblockHandlerXR.checkSpace(world, x, y, z, getDimensions(), x, y, z, dir))
      return false;
    return MultiblockHandlerXR.checkSpace(
        world, x, y, z, new int[] {2, 0, 1, 0, 1, 0}, x, y, z, dir);
  }

  @Override
  public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
    if (tool != ToolType.SCREWDRIVER) return false;

    BlockPos corePos = findCore(world, new BlockPos(x, y, z));
    TileEntityMachineStrandCaster caster = (TileEntityMachineStrandCaster) world.getTileEntity(corePos);

    if (Objects.requireNonNull(caster).inventory.getStackInSlot(0).isEmpty()) return false;

    if (!player.inventory.addItemStackToInventory(caster.inventory.getStackInSlot(0).copy())) {
      EntityItem item = new EntityItem(world, x + 0.5, y + 1, z + 0.5, caster.inventory.getStackInSlot(0).copy());
      world.spawnEntity(item);
    } else {
      player.inventoryContainer.detectAndSendChanges();
    }

    caster.inventory.setStackInSlot(0, ItemStack.EMPTY);
    caster.markDirty();

    return true;
  }
}
