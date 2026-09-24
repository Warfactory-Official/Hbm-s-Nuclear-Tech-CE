package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineAmmoPress;
import com.hbm.inventory.gui.GUIMachineAmmoPress;
import com.hbm.inventory.gui.GuiInfoContainer;
import com.hbm.inventory.recipes.AmmoPressRecipes;
import com.hbm.inventory.recipes.AmmoPressRecipes.AmmoPressRecipe;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@AutoRegister
public class TileEntityMachineAmmoPress extends TileEntityMachineBase
    implements IControlReceiver, IGUIProvider, ITickable {

  public int selectedRecipe = -1;

  public AnimationState animState = AnimationState.LIFTING;

  public int playAnimation = 0;
  public float prevLift = 0F;
  public float lift = 0F;
  public float prevPress = 0F;
  public float press = 0F;

  private static final int invSize = 10;

  public enum AnimationState {
    LIFTING,
    PRESSING,
    RETRACTING,
    LOWERING
  }

  public TileEntityMachineAmmoPress() {
    super(invSize);

    inventory =
        new ItemStackHandler(invSize) {
          @Override
          protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirty();
          }

          @Override
          public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            super.setStackInSlot(slot, stack);
            /*if(this.worldObj.isRemote) return;

            //while this allowed one shift click to process absolutely everything, it also caused a fuckton of issues
            if(!recipeLock) {
                recipeLock = true;
                if(slot < 10) this.performRecipe();
                recipeLock = false;
            }*/
          }
        };
  }

  @Override
  public String getDefaultName() {
    return "container.machineAmmoPress";
  }

  @Override
  public void update() {
    if (!world.isRemote) {
      if (this.playAnimation > 0) this.playAnimation--;
      this.performRecipe();
      this.networkPackNT(25);
    } else {

      this.prevLift = this.lift;
      this.prevPress = this.press;

      if (playAnimation > 0 || lift > 0)
        switch (animState) {
          case LIFTING:
            this.lift += 1F / 40F;
            if (this.lift >= 1F) {
              this.lift = 1F;
              this.animState = AnimationState.PRESSING;
            }
            break;
          case PRESSING:
            this.press += 1F / 20F;
            if (this.press >= 1F) {
              this.press = 1F;
              this.animState = AnimationState.RETRACTING;
            }
            break;
          case RETRACTING:
            this.press -= 1F / 20F;
            if (this.press <= 0F) {
              this.press = 0F;
              this.animState = AnimationState.LOWERING;
            }
            break;
          case LOWERING:
            this.lift -= 1F / 40F;
            if (this.lift <= 0F) {
              this.lift = 0F;
              this.animState = AnimationState.LIFTING;
            }
            break;
        }
    }
  }

  // we want to update the output every time the grid changes, but producing output changes the grid
  // again, so we just put a recursion brake on this fucker
  // public static boolean recipeLock = false;

  public void performRecipe() {
    if (selectedRecipe < 0 || selectedRecipe >= AmmoPressRecipes.recipes.size()) return;

    AmmoPressRecipe recipe = AmmoPressRecipes.recipes.get(selectedRecipe);

    ItemStack stack = inventory.getStackInSlot(9);

    if (!stack.isEmpty()) {
      if (stack.getItem() != recipe.output.getItem()) return;
      if (stack.getItemDamage() != recipe.output.getItemDamage()) return;
      if (stack.getCount() + recipe.output.getCount() > stack.getMaxStackSize()) return;
    }

    if (this.hasIngredients(recipe)) {
      this.produceAmmo(recipe);
      performRecipe();
    }
  }

  public boolean hasIngredients(AmmoPressRecipe recipe) {

    for (int i = 0; i < 9; i++) {
      ItemStack stack = inventory.getStackInSlot(i);
      if (recipe.input[i] == null && stack.isEmpty()) continue;
      if (recipe.input[i] != null && stack.isEmpty()) return false;
      if (recipe.input[i] == null && !stack.isEmpty()) return false;
      if (!recipe.input[i].matchesRecipe(stack, false)) return false;
    }

    return true;
  }

  // implies hasIngredients returns true, will violently explode otherwise
  protected void produceAmmo(AmmoPressRecipe recipe) {

    for (int i = 0; i < 9; i++) {
      if (recipe.input[i] != null) inventory.getStackInSlot(i).shrink(recipe.input[i].stacksize);
    }

    if (inventory.getStackInSlot(9).isEmpty()) {
      inventory.setStackInSlot(9, recipe.output.copy());
    } else {
      inventory.getStackInSlot(9).grow(recipe.output.getCount());
    }

    this.playAnimation = 40;
  }

  public int[] access = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

  @Override
  public int[] getAccessibleSlotsFromSide(EnumFacing side) {
    return access;
  }

  @Override
  public boolean canExtractItem(int i, ItemStack stack, int j) {
    return i == 9;
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    if (slot > 8) return false;
    if (selectedRecipe < 0 || selectedRecipe >= AmmoPressRecipes.recipes.size()) return false;

    AmmoPressRecipe recipe = AmmoPressRecipes.recipes.get(selectedRecipe);
    if (recipe.input[slot] == null) return false;
    return recipe.input[slot].matchesRecipe(stack, true);
  }

  @Override
  public void serialize(ByteBuf buf) {
    super.serialize(buf);
    buf.writeInt(this.selectedRecipe);
    buf.writeInt(this.playAnimation);
  }

  @Override
  public void deserialize(ByteBuf buf) {
    super.deserialize(buf);
    this.selectedRecipe = buf.readInt();
    this.playAnimation = buf.readInt();
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.selectedRecipe = nbt.getInteger("recipe");
  }

  @Override
  public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setInteger("recipe", selectedRecipe);
    return nbt;
  }

  @Override
  public boolean hasPermission(EntityPlayer player) {
    return this.isUseableByPlayer(player);
  }

  @Override
  public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {
    int newRecipe = data.getInteger("selection");
    if (newRecipe == selectedRecipe) this.selectedRecipe = -1;
    else this.selectedRecipe = newRecipe;
    this.markDirty();
  }

  AxisAlignedBB bb = null;

  @Override
  public @NotNull AxisAlignedBB getRenderBoundingBox() {

    if (bb == null) {
      bb =
          new AxisAlignedBB(
              pos.getX() - 1,
              pos.getY(),
              pos.getZ() - 1,
              pos.getX() + 2,
              pos.getY() + 2,
              pos.getZ() + 2);
    }

    return bb;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public double getMaxRenderDistanceSquared() {
    return 65536.0D;
  }

  @Override
  public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new ContainerMachineAmmoPress(player.inventory, this);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public GuiInfoContainer provideGUI(
      int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new GUIMachineAmmoPress(player.inventory, this);
  }
}
