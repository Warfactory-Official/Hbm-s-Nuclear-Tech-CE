package com.hbm.tileentity.machine.storage;

import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerMassStorage;
import com.hbm.inventory.gui.GUIMassStorage;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.IControlReceiverFilter;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityMassStorage extends TileEntityCrateBase implements ITickable, IControlReceiverFilter, IGUIProvider, IRORValueProvider, IRORInteractive, SimpleComponent, CompatHandler.OCComponent {

    private int stack = 0;
    public boolean output = false;
    private int capacity;
    public int redstone = 0;

    @SideOnly(Side.CLIENT)
    public ItemStack type;

    public TileEntityMassStorage() {
        super(3);
    }

    public TileEntityMassStorage(int capacity) {
        this();
        this.capacity = capacity;
    }

    @Override
    public @NotNull String getName() {
        return this.hasCustomName() ? this.customName : "container.massStorage";
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            int newRed = this.getStockpile() * 15 / this.capacity;

            if (newRed != this.redstone) {
                this.redstone = newRed;
                this.markDirty();
            }

            if (!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() == ModItems.fluid_barrel_infinite) {
                this.stack = this.getCapacity();
            }

            if (this.getType() == null) this.stack = 0;

            if(canInsert(inventory.getStackInSlot(0))) {
                int remaining = getCapacity() - getStockpile();
                int toRemove = Math.min(remaining, inventory.getStackInSlot(0).getCount());
                this.inventory.getStackInSlot(0).shrink(toRemove);
                this.stack += toRemove;
                this.world.markChunkDirty(pos, this);
            }

            if (output && getType() != null) {

                if (!inventory.getStackInSlot(2).isEmpty() && !(inventory.getStackInSlot(2).isItemEqual(getType()) && ItemStack.areItemStackTagsEqual(inventory.getStackInSlot(2), getType()))) {
                    return;
                }

                int amount = Math.min(getStockpile(), getType().getMaxStackSize());

                if (amount > 0) {
                    if (inventory.getStackInSlot(2).isEmpty()) {
                        inventory.setStackInSlot(2, inventory.getStackInSlot(1).copy());
                        inventory.getStackInSlot(2).setCount(amount);
                        this.stack -= amount;
                    } else {
                        amount = Math.min(amount, inventory.getStackInSlot(2).getMaxStackSize() - inventory.getStackInSlot(2).getCount());
                        inventory.getStackInSlot(2).grow(amount);
                        this.stack -= amount;
                    }
                }
            }

            networkPackNT(32);
        }
    }

    public boolean canInsert(ItemStack stack) {
        if (stack.isEmpty() || this.isLocked()) return false;
        ItemStack type = getType();
        if (type.isEmpty()) return false;
        return type.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, type);
    }

    public boolean quickInsert(ItemStack stack) {
        if (!canInsert(stack)) return false;

        int remaining = getCapacity() - getStockpile();
        if (remaining < stack.getCount()) return false;

        this.stack += stack.getCount();
        stack.setCount(0);
        this.markDirty();

        return true;
    }

    public ItemStack quickExtract() {
        if (!output) return ItemStack.EMPTY;

        ItemStack type = getType();
        if (type.isEmpty()) return ItemStack.EMPTY;

        int amount = type.getMaxStackSize();

        if (getStockpile() < amount) return ItemStack.EMPTY;

        ItemStack result = type.copy();
        result.setCount(amount);
        this.stack -= amount;
        this.markDirty();

        return result;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(getStockpile());
        buf.writeBoolean(output);
        ByteBufUtils.writeItemStack(buf, inventory.getStackInSlot(1));
        buf.writeInt(this.capacity);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.stack = buf.readInt();
        this.output = buf.readBoolean();
        this.type = ByteBufUtils.readItemStack(buf);
        this.capacity = buf.readInt();
    }

    public int getCapacity() {
        return capacity;
    }

    public ItemStack getType() {
        return inventory.getStackInSlot(1).isEmpty() ? ItemStack.EMPTY : inventory.getStackInSlot(1).copy();
    }

    public int getStockpile() {
        return stack;
    }

    public void setStockpile(int stack) {
        this.stack = stack;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return new Vec3d(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).length() < 20;
    }

    public void openInventory(EntityPlayer player) {
        player.world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.storageOpen, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
    }

    public void closeInventory(EntityPlayer player) {
        player.world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.storageClose, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.stack = nbt.getInteger("stack");
        this.output = nbt.getBoolean("output");
        this.capacity = nbt.getInteger("capacity");
        this.redstone = nbt.getByte("redstone");

        if (this.capacity <= 0) {
            this.capacity = 10_000;
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("stack", stack);
        nbt.setBoolean("output", output);
        nbt.setInteger("capacity", capacity);
        nbt.setByte("redstone", (byte) redstone);
        return super.writeToNBT(nbt);
    }

    @Override
    public void nextMode(int i) {
    }

    @Override
    public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {

        if (data.hasKey("provide") && !inventory.getStackInSlot(1).isEmpty()) {

            if (this.getStockpile() == 0) {
                return;
            }

            int amount = data.getBoolean("provide") ? inventory.getStackInSlot(1).getMaxStackSize() : 1;
            amount = Math.min(amount, getStockpile());

            if (!inventory.getStackInSlot(2).isEmpty() && !(inventory.getStackInSlot(2).isItemEqual(getType()) && ItemStack.areItemStackTagsEqual(inventory.getStackInSlot(2), getType()))) {
                return;
            }

            if (inventory.getStackInSlot(2).isEmpty()) {
                inventory.setStackInSlot(2, inventory.getStackInSlot(1).copy());
                inventory.getStackInSlot(2).setCount(amount);
                this.stack -= amount;
            } else {
                amount = Math.min(amount, inventory.getStackInSlot(2).getMaxStackSize() - inventory.getStackInSlot(2).getCount());
                inventory.getStackInSlot(2).grow(amount);
                this.stack -= amount;
            }
        }

        if (data.hasKey("toggle")) {
            this.output = !output;
        }
        if (data.hasKey("slot") && this.getStockpile() <= 0) {
            setFilterContents(data);
            if (!inventory.getStackInSlot(1).isEmpty()) inventory.getStackInSlot(1).setCount(1);
        }
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemStackIn, int amount) {
        return !this.isLocked() && i == 0 && (this.getType() == null || (getType().isItemEqual(itemStackIn) && ItemStack.areItemStackTagsEqual(itemStackIn, getType())));
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int amount) {
        return !this.isLocked() && i == 2;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[]{0, 2};
    }

    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 1024.0D; // only render mass storage info 32 blocks away, for performance
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMassStorage(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMassStorage(player.inventory, this);
    }

    @Override
    public int[] getFilterSlots() {
        return new int[]{1, 2};
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[]{PREFIX_VALUE + "type", PREFIX_VALUE + "fill", PREFIX_VALUE + "fillpercent", PREFIX_FUNCTION + "toggleoutput",};
    }

    @Override
    public String provideRORValue(String name) {
        if ((PREFIX_VALUE + "fill").equals(name)) return "" + this.stack;
        if ((PREFIX_VALUE + "fillpercent").equals(name)) return "" + this.stack * 100 / this.capacity;
        if ((PREFIX_VALUE + "type").equals(name)) {
            if (inventory.getStackInSlot(1).isEmpty()) return "None";
            return inventory.getStackInSlot(1).getDisplayName();
        }

        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if ((PREFIX_FUNCTION + "toggleoutput").equals(name)) {
            this.output = !this.output;
            this.markDirty();
        }

        return null;
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_mass_storage";
    }

    @Callback(direct = true, doc = "function():number -- Returns item amount")
    @Optional.Method(modid = "opencomputers")
    public Object[] getFill(Context context, Arguments args) {
        return new Object[] {this.stack};
    }

    @Callback(direct = true, doc = "function():number -- Returns item capacity")
    @Optional.Method(modid = "opencomputers")
    public Object[] getCapacity(Context context, Arguments args) {
        return new Object[] {this.capacity};
    }

    @Callback(direct = true, doc = "function():string -- Returns item type")
    @Optional.Method(modid = "opencomputers")
    public Object[] getType(Context context, Arguments args) {
        ItemStack slot = inventory.getStackInSlot(1);
        if(slot.isEmpty()) return new Object[] {"None"};
        return new Object[] {slot.getDisplayName()};
    }

    @Callback(direct = true, doc = "function():boolean -- Returns output mode")
    @Optional.Method(modid = "opencomputers")
    public Object[] getOutputMode(Context context, Arguments args) {
        return new Object[] {this.output};
    }

    @Callback(direct = true, limit = 4, doc = "function(mode: boolean) -- Sets output mode")
    @Optional.Method(modid = "opencomputers")
    public Object[] setOutputMode(Context context, Arguments args) {
        this.output = args.checkBoolean(0);
        return new Object[] {};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[] {
            "getFill",
            "getCapacity",
            "getType",
            "getOutputMode",
            "setOutputMode"
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        switch (method) {
            case "getFill": return getFill(context, args);
            case "getCapacity": return getCapacity(context, args);
            case "getType": return getType(context, args);
            case "getOutputMode": return getOutputMode(context, args);
            case "setOutputMode": return setOutputMode(context, args);
        }
        throw new NoSuchMethodException();
    }
}
