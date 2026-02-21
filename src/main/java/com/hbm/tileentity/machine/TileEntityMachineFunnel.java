package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.container.ContainerFunnel;
import com.hbm.inventory.gui.GUIFunnel;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@AutoRegister
public class TileEntityMachineFunnel extends TileEntityMachineBase
    implements IGUIProvider, IControlReceiver, ITickable {
  public int mode = 0;
  public static final int MODE_3x3 = 1;
  public static final int MODE_2x2 = 2;

  private final int[] topAccess = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
  private final int[] bottomAccess = new int[] {9, 10, 11, 12, 13, 14, 15, 16, 17};

  public TileEntityMachineFunnel() {
    super(18, false, false);
  }

  @Override
  public String getDefaultName() {
    return "container.machineFunnel";
  }

  @Override
  public void update() {

    if (!world.isRemote) {

      for (int i = 0; i < 9; i++) {

        if (inventory.getStackInSlot(i) != ItemStack.EMPTY) {
          int count = 9;
          ItemStack compressed =
              (mode == MODE_2x2 || inventory.getStackInSlot(i).getCount() < 9)
                  ? ItemStack.EMPTY
                  : this.getFrom9(inventory.getStackInSlot(i));
          if (compressed == ItemStack.EMPTY) {
            compressed =
                (mode == MODE_3x3 || inventory.getStackInSlot(i).getCount() < 4)
                    ? ItemStack.EMPTY
                    : this.getFrom4(inventory.getStackInSlot(i));
            count = 4;
          }

          if (compressed != ItemStack.EMPTY && inventory.getStackInSlot(i).getCount() >= count) {
            if (inventory.getStackInSlot(i + 9) == ItemStack.EMPTY) {
              inventory.setStackInSlot(i + 9, compressed.copy());
              inventory.getStackInSlot(i).shrink(count);
            } else if (inventory.getStackInSlot(i + 9).getItem() == compressed.getItem()
                && inventory.getStackInSlot(i + 9).getItemDamage() == compressed.getItemDamage()
                && inventory.getStackInSlot(i + 9).getCount() + compressed.getCount()
                    <= compressed.getMaxStackSize()) {
              inventory.getStackInSlot(i + 9).grow(compressed.getCount());
              inventory.getStackInSlot(i).shrink(count);
            }
          }
        }
      }

      this.networkPackNT(15);
    }
  }

  @Override
  public void serialize(ByteBuf buf) {
    super.serialize(buf);
    buf.writeInt(this.mode);
  }

  @Override
  public void deserialize(ByteBuf buf) {
    super.deserialize(buf);
    this.mode = buf.readInt();
  }

  @Override
  public int[] getAccessibleSlotsFromSide(EnumFacing facing) {
    return facing == EnumFacing.DOWN ? bottomAccess : topAccess;
  }

  @Override
  public boolean canExtractItem(int i, ItemStack stack, int j) {
    if (j == 0) return i > 8;
    return j != 1 && i < 9;
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    if (slot > 8) return false;
    if (inventory.getStackInSlot(slot) != ItemStack.EMPTY)
      return true; // if the slot is already occupied, return true because then the same type
    // merging skips the validity check
    return this.getFrom9(stack) != ItemStack.EMPTY || this.getFrom4(stack) != ItemStack.EMPTY;
  }

  protected TileEntityMachineAutocrafter.InventoryCraftingAuto craftingInventory =
      new TileEntityMachineAutocrafter.InventoryCraftingAuto(3, 3);

  // hashmap lookups are way faster than iterating over the entire ass crafting list all the fucking
  // time
  public static final HashMap<RecipesCommon.ComparableStack, ItemStack> from4Cache =
      new HashMap<>();
  public static final HashMap<RecipesCommon.ComparableStack, ItemStack> from9Cache =
      new HashMap<>();

  public ItemStack getFrom4(ItemStack ingredient) {
    RecipesCommon.ComparableStack singular =
        new RecipesCommon.ComparableStack(ingredient).makeSingular();
    if (from4Cache.containsKey(singular)) return from4Cache.get(singular);
    this.craftingInventory.clear();
    this.craftingInventory.setInventorySlotContents(0, ingredient.copy());
    this.craftingInventory.setInventorySlotContents(1, ingredient.copy());
    this.craftingInventory.setInventorySlotContents(3, ingredient.copy());
    this.craftingInventory.setInventorySlotContents(4, ingredient.copy());
    ItemStack match = getMatch(this.craftingInventory);
    from4Cache.put(singular, match != ItemStack.EMPTY ? match.copy() : ItemStack.EMPTY);
    return match;
  }

  public ItemStack getFrom9(ItemStack ingredient) {
    RecipesCommon.ComparableStack singular =
        new RecipesCommon.ComparableStack(ingredient).makeSingular();
    if (from9Cache.containsKey(singular)) return from9Cache.get(singular);
    this.craftingInventory.clear();
    for (int i = 0; i < 9; i++)
      this.craftingInventory.setInventorySlotContents(i, ingredient.copy());
    ItemStack match = getMatch(this.craftingInventory);
    from9Cache.put(singular, match != ItemStack.EMPTY ? match.copy() : ItemStack.EMPTY);
    return match;
  }

  public ItemStack getMatch(InventoryCrafting grid) {
    for (IRecipe recipe : CraftingManager.REGISTRY) {

      if (recipe.matches(grid, world)) {
        return recipe.getCraftingResult(grid);
      }
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.mode = nbt.getInteger("mode");
  }

  @Override
  public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setInteger("mode", mode);
    return nbt;
  }

  @Override
  public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new ContainerFunnel(player.inventory, this);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new GUIFunnel(player.inventory, this);
  }

  @Override
  public boolean hasPermission(EntityPlayer player) {
    return this.isUseableByPlayer(player);
  }

  @Override
  public void receiveControl(NBTTagCompound data) {
    this.mode++;
    if (mode > 2) mode = 0;
    this.markDirty();
  }
}
