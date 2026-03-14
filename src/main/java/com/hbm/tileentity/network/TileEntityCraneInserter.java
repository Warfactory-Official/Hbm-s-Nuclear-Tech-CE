package com.hbm.tileentity.network;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerCraneInserter;
import com.hbm.inventory.gui.GUICraneInserter;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityCraneInserter extends TileEntityCraneBase implements IGUIProvider, IControlReceiver {
    public boolean destroyer = true;

    public TileEntityCraneInserter() {
        super(21);
    }

    @Override
    public String getDefaultName() {
        return "container.craneInserter";
    }

    @Override
    public void update() {
        super.update();
        if(!world.isRemote) {

            tryFillTe();

        }
    }

    public void tryFillTe(){
        EnumFacing outputSide = getOutputSide();
        EnumFacing accessSide = outputSide.getOpposite();
        TileEntity te = world.getTileEntity(pos.offset(outputSide));

        if(te != null){
            if(te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide)) {
                IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessSide);
            
                for(int i = 0; i < inventory.getSlots(); i++) {
                    tryFillContainerCap(cap, i);
                }
            }
        }
    }

    public boolean tryFillTeDirect(ItemStack stack){
        return tryInsertItemCap(inventory, stack);
    }

    //Unloads output into chests. Capability version.
    public boolean tryFillContainerCap(IItemHandler target, int invSlot) {
        ItemStack stack = inventory.getStackInSlot(invSlot);
        if(stack.isEmpty()) return false;
        return tryInsertItemCap(target, stack);
    }

    //Unloads output into chests. Capability version.
    public boolean tryInsertItemCap(IItemHandler target, ItemStack stack) {
        if(stack.isEmpty())
            return false;

        boolean movedAny = false;

        for(int i = 0; i < target.getSlots() && !stack.isEmpty(); i++) {
            ItemStack probe = stack.copy();
            probe.setCount(1);
            ItemStack simOne = target.insertItem(i, probe, true);
            if(!simOne.isEmpty()) {
                continue;
            }

            int maxTry = Math.min(stack.getCount(), target.getSlotLimit(i));
            int accepted = findMaxInsertable(target, i, stack, maxTry);

            if(accepted > 0) {
                ItemStack toInsert = stack.copy();
                toInsert.setCount(accepted);
                ItemStack rest = target.insertItem(i, toInsert, false);

                int actuallyInserted = accepted - (!rest.isEmpty() ? rest.getCount() : 0);
                if(actuallyInserted > 0) {
                    stack.shrink(actuallyInserted);
                    movedAny = true;
                }
            }
        }

        return movedAny;
    }

    private int findMaxInsertable(IItemHandler target, int slot, ItemStack stack, int upperBound) {
        int lo = 0;
        int hi = upperBound;

        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;

            ItemStack test = stack.copy();
            test.setCount(mid);
            ItemStack res = target.insertItem(slot, test, true);

            if (res.isEmpty()) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }

        return lo;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCraneInserter(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICraneInserter(player.inventory, this);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(destroyer);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.destroyer = buf.readBoolean();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.destroyer = nbt.getBoolean("destroyer");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("destroyer", this.destroyer);
        return nbt;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        int xCoord = pos.getX();
        int yCoord = pos.getY();
        int zCoord = pos.getZ();
        return new Vec3d(xCoord - player.posX, yCoord - player.posY, zCoord - player.posZ).length() < 20;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("destroyer")) {
            this.destroyer = !this.destroyer;
            this.markDirty();
        }
    }

}
