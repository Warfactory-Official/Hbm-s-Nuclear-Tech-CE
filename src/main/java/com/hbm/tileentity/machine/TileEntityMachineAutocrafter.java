package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerAutocrafter;
import com.hbm.inventory.gui.GUIAutocrafter;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.ItemStackUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityMachineAutocrafter extends TileEntityMachineBase implements IEnergyReceiverMK2, ITickable, IGUIProvider {

    public static final String MODE_EXACT = "exact";
    public static final String MODE_WILDCARD = "wildcard";
    public String[] modes = new String[9];

    public List<IRecipe> recipes = new ArrayList();
    public int recipeIndex;
    public int recipeCount;

    public TileEntityMachineAutocrafter() {
        super(21);
    }

    public void initPattern(ItemStack stack, int i) {

        if(world.isRemote) return;

        if(stack == null) {
            modes[i] = null;
            return;
        }

        List<String> names = ItemStackUtil.getOreDictNames(stack);

        if(iterateAndCheck(names, i ,"ingot")) return;
        if(iterateAndCheck(names, i ,"block")) return;
        if(iterateAndCheck(names, i ,"dust")) return;
        if(iterateAndCheck(names, i ,"nugget")) return;
        if(iterateAndCheck(names, i ,"plate")) return;

        if(stack.getHasSubtypes()) {
            modes[i] = MODE_EXACT;
        } else {
            modes[i] = MODE_WILDCARD;
        }
    }

    private boolean iterateAndCheck(List<String> names, int i, String prefix) {

        for(String s : names) {
            if(s.startsWith(prefix)) {
                modes[i] = s;
                return true;
            }
        }

        return false;
    }

    public void nextMode(int i) {

        if(world.isRemote) return;

        ItemStack stack = inventory.getStackInSlot(i);

        if(stack.isEmpty()) {
            modes[i] = null;
            return;
        }

        if(modes[i] == null) {
            modes[i] = MODE_EXACT;
        } else if(MODE_EXACT.equals(modes[i])) {
            modes[i] = MODE_WILDCARD;
        } else if(MODE_WILDCARD.equals(modes[i])) {

            List<String> names = ItemStackUtil.getOreDictNames(stack);

            if(names.isEmpty()) {
                modes[i] = MODE_EXACT;
            } else {
                modes[i] = names.get(0);
            }
        } else {

            List<String> names = ItemStackUtil.getOreDictNames(stack);

            if(names.size() < 2 || modes[i].equals(names.get(names.size() - 1))) {
                modes[i] = MODE_EXACT;
            } else {

                for(int j = 0; j < names.size() - 1; j++) {

                    if(modes[i].equals(names.get(j))) {
                        modes[i] = names.get(j + 1);
                        return;
                    }
                }
            }
        }
    }

    public void nextTemplate() {

        if(world.isRemote) return;

        this.recipeIndex++;

        if(this.recipeIndex >= this.recipes.size())
            this.recipeIndex = 0;

        if(!this.recipes.isEmpty()) {
            inventory.setStackInSlot(9, this.recipes.get(this.recipeIndex).getCraftingResult(getTemplateGrid()));
        } else {
            inventory.setStackInSlot(9, ItemStack.EMPTY);
        }
    }

    @Override
    public String getDefaultName() {
        return "container.autocrafter";
    }

    protected InventoryCraftingAuto craftingInventory = new InventoryCraftingAuto(3, 3);
    private NonNullList<ItemStack> slots = NonNullList.withSize(21, ItemStack.EMPTY);;

    @Override
    public void update() {

        if(!world.isRemote) {

            this.power = Library.chargeTEFromItems(inventory, 20, power, maxPower);
            for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);

            if(!this.recipes.isEmpty() && this.power >= this.consumption) {
                IRecipe recipe = this.recipes.get(recipeIndex);

                if(recipe.matches(this.getRecipeGrid(), this.world)) {
                    ItemStack stack = recipe.getCraftingResult(this.getRecipeGrid());

                    if(!stack.isEmpty()) {

                        boolean didCraft = false;

                        if(this.inventory.getStackInSlot(19).isEmpty()) {
                            inventory.setStackInSlot(19, stack.copy());
                            didCraft = true;
                        } else if(this.inventory.getStackInSlot(19).isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, this.inventory.getStackInSlot(19)) && this.inventory.getStackInSlot(19).getCount() + stack.getCount() <= this.inventory.getStackInSlot(19).getMaxStackSize()) {
                            inventory.getStackInSlot(19).setCount(inventory.getStackInSlot(19).getCount() + stack.getCount());
                            didCraft = true;
                        }

                        if(didCraft) {
                            for(int i = 10; i < 19; i++) {

                                ItemStack ingredient = this.inventory.getStackInSlot(i);

                                if(!ingredient.isEmpty()) {
                                    this.inventory.getStackInSlot(i).shrink(1);

                                    if(this.inventory.getStackInSlot(i).isEmpty() && ingredient.getItem().hasContainerItem(ingredient)) {
                                        ItemStack container = ingredient.getItem().getContainerItem(ingredient);

                                        if(container != null && container.isItemStackDamageable() && container.getItemDamage() > container.getMaxDamage()) {
                                            continue;
                                        }

                                        this.inventory.setStackInSlot(i, container);
                                    }
                                }
                            }

                            this.power -= this.consumption;
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
        buf.writeLong(power);
        for(int i = 0; i < 9; i++) {
            if(modes[i] != null) {
                buf.writeBoolean(true);
                BufferUtil.writeString(buf, modes[i]);
            } else
                buf.writeBoolean(false);
        }

        buf.writeInt(recipeCount);
        buf.writeInt(recipeIndex);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        power = buf.readLong();

        modes = new String[9];
        for (int i = 0; i < 9; i++) {
            if (buf.readBoolean()) modes[i] = BufferUtil.readString(buf);
        }

        recipeCount = buf.readInt();
        recipeIndex = buf.readInt();
    }

    public void updateTemplateGrid() {

        this.recipes = getMatchingRecipes(this.getTemplateGrid());
        this.recipeCount = recipes.size();
        this.recipeIndex = 0;

        if(!this.recipes.isEmpty()) {
            this.inventory.setStackInSlot(9, this.recipes.get(this.recipeIndex).getCraftingResult(getTemplateGrid()));
        } else {
            this.inventory.setStackInSlot(9, ItemStack.EMPTY);
        }
    }

    public List<IRecipe> getMatchingRecipes(InventoryCrafting grid) {
        List<IRecipe> recipes = new ArrayList();

        for(IRecipe recipe : ForgeRegistries.RECIPES.getValues()) {

            if(recipe.matches(grid, world)) {
                recipes.add(recipe);
            }
        }

        return recipes;
    }

    public int[] access = new int[] { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return access;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack stack, int j) {
        if(i == 19)
            return true;

        if(i > 9 && i < 19) {
            ItemStack filter = this.inventory.getStackInSlot(i-10);
            String mode = modes[i - 10];

            if(filter == null || mode == null || mode.isEmpty()) return true;

            if(isValidForFilter(filter, mode, stack)) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {

        //automatically prohibit any stacked item with a container
        if(stack.getCount() > 1 && stack.getItem().hasContainerItem(stack))
            return false;

        //only allow insertion for the nine recipe slots
        if(slot < 10 || slot > 18)
            return false;

        //is the filter at this space null? no input.
        if(this.inventory.getStackInSlot(slot - 10).isEmpty())
            return false;

        //let's find all slots that this item could potentially go in
        List<Integer> validSlots = new ArrayList();
        for(int i = 0; i < 9; i++) {
            ItemStack filter = this.inventory.getStackInSlot(i);
            String mode = modes[i];

            if(filter.isEmpty() || mode == null || mode.isEmpty()) continue;

            if(isValidForFilter(filter, mode, stack)) {
                validSlots.add(i + 10);

                //if the current slot is valid and has no item in it, shortcut to true [*]
                if(i + 10 == slot && this.inventory.getStackInSlot(slot).isEmpty()) {
                    return true;
                }
            }
        }

        //if the slot we are looking at isn't valid, skip
        if(!validSlots.contains(slot)) {
            return false;
        }

        //assumption from [*]: the slot has to be valid by now, and it cannot be null
        int size = this.inventory.getStackInSlot(slot).getCount();

        //now we decide based on stacksize, woohoo
        for(Integer i : validSlots) {
            ItemStack valid = this.inventory.getStackInSlot(i);

            if(valid.isEmpty()) return false; //null? since slots[slot] is not null by now, this other slot needs the item more
            if(!(valid.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(valid, stack))) continue; //different item anyway? out with it

            //if there is another slot that actually does need the same item more, cancel
            if(valid.getCount() < size)
                return false;
        }

        //prevent items with containers from stacking
        if(stack.getItem().hasContainerItem(stack))
            return false;

        //by now, we either already have filled the slot (if valid by filter and null) or weeded out all other options, which means it is good to go
        return true;
    }

    private boolean isValidForFilter(ItemStack filter, String mode, ItemStack input) {

        switch(mode) {
            case MODE_EXACT: return input.isItemEqual(filter) && ItemStack.areItemStackTagsEqual(input, filter);
            case MODE_WILDCARD: return input.getItem() == filter.getItem() && ItemStack.areItemStackTagsEqual(input, filter);
            default:
                List<String> keys = ItemStackUtil.getOreDictNames(input);
                return keys.contains(mode);
        }
    }

    public InventoryCrafting getTemplateGrid() {
        this.craftingInventory.loadInventory(inventory, 0);
        return this.craftingInventory;
    }

    public InventoryCrafting getRecipeGrid() {
        this.craftingInventory.loadInventory(inventory, 10);
        return this.craftingInventory;
    }

    public static class InventoryCraftingAuto extends InventoryCrafting {

        public InventoryCraftingAuto(int width, int height) {
            super(new ContainerBlank() /* "can't be null boo hoo" */, width, height);
        }

        public void loadInventory(ItemStackHandler inv, int start) {

            for(int i = 0; i < this.getSizeInventory(); i++) {
                this.setInventorySlotContents(i, inv.getStackInSlot(start + i));
            }
        }

        public void clear() {
            for(int i = 0; i < this.getSizeInventory(); i++) this.setInventorySlotContents(i, null);
        }

        public static class ContainerBlank extends Container {
            @Override public void onCraftMatrixChanged(IInventory inventory) { }
            @Override public boolean canInteractWith(EntityPlayer player) { return false; }
        }
    }

    public static int consumption = 100;
    public static long maxPower = consumption * 100;
    public long power;

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.power = nbt.getLong("power");

        for(int i = 0; i < 9; i++) {
            if(nbt.hasKey("mode" + i)) {
                modes[i] = nbt.getString("mode" + i);
            }
        }

        this.recipes = getMatchingRecipes(this.getTemplateGrid());
        this.recipeCount = recipes.size();
        this.recipeIndex = nbt.getInteger("rec");

        if(!this.recipes.isEmpty()) {
            this.inventory.setStackInSlot(9, this.recipes.get(this.recipeIndex).getCraftingResult(getTemplateGrid()));
        } else {
            this.inventory.setStackInSlot(9, ItemStack.EMPTY);
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("power", power);

        for(int i = 0; i < 9; i++) {
            if(modes[i] != null) {
                nbt.setString("mode" + i, modes[i]);
            }
        }

        nbt.setInteger("rec", this.recipeIndex);
        return super.writeToNBT(nbt);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerAutocrafter(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIAutocrafter(player.inventory, this);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(
                    new NTMEnergyCapabilityWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }
}
