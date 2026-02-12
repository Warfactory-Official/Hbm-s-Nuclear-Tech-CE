package com.hbm.tileentity.network;

import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.tileentity.IControlReceiverFilter;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.Compat;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityRadioTorchCounter extends TileEntityMachineBase implements IControlReceiverFilter, ITickable {
    public String[] channel;
    public int[] lastCount;
    public boolean polling = false;
    public ModulePatternMatcher matcher;

    public TileEntityRadioTorchCounter() {
        super(3, false, false);
        this.channel = new String[3];
        for(int i = 0; i < 3; i++) this.channel[i] = "";
        this.lastCount = new int[3];
        this.matcher = new ModulePatternMatcher(3);
    }

    @Override
    public String getDefaultName() {
        return "container.rttyCounter";
    }
    @Override
    public void nextMode(int i) {
        this.matcher.nextMode(world, inventory.getStackInSlot(i), i);
    }

    @Override
    public void update() {

        if(!world.isRemote) {
            ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();

            TileEntity tile = Compat.getTileStandard(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ);
            if(tile instanceof IInventory inv) {
                ItemStack[] invSlots = new ItemStack[inv.getSizeInventory()];
                for(int i = 0; i < invSlots.length; i++) invSlots[i] = inv.getStackInSlot(i);

                for(int i = 0; i < 3; i++) {
                    if(channel[i].isEmpty()) continue;
                    if(inventory.getStackInSlot(i) == ItemStack.EMPTY) continue;
                    ItemStack pattern = inventory.getStackInSlot(i);

                    int count = 0;

                    for (ItemStack invSlot : invSlots) {
                        if (invSlot != null && matcher.isValidForFilter(pattern, i, invSlot)) {
                            count += invSlot.getCount();
                        }
                    }

                    if(this.polling || this.lastCount[i] != count) {
                        RTTYSystem.broadcast(world, this.channel[i], count);
                    }

                    this.lastCount[i] = count;
                }
            }

            this.networkPackNT(15);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.polling);
        BufferUtil.writeIntArray(buf, this.lastCount);
        this.matcher.serialize(buf);
        for(int i = 0; i < 3; i++) BufferUtil.writeString(buf, this.channel[i]);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.polling = buf.readBoolean();
        this.lastCount = BufferUtil.readIntArray(buf);
        this.matcher.deserialize(buf);
        for(int i = 0; i < 3; i++) this.channel[i] = BufferUtil.readString(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.polling = nbt.getBoolean("p");
        for(int i = 0; i < 3; i++) {
            this.channel[i] = nbt.getString("c" + i);
            this.lastCount[i] = nbt.getInteger("l" + i);
        }
        this.matcher.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("p", polling);
        for(int i = 0; i < 3; i++) {
            if(channel[i] != null) nbt.setString("c" + i, channel[i]);
            nbt.setInteger("l" + i, lastCount[i]);
        }
        this.matcher.writeToNBT(nbt);
        return nbt;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("polling")) {
            this.polling = !this.polling;
            this.markChanged();
        } else {
            for(int i = 0; i < 3; i++) {
                this.channel[i] = data.getString("c" + i);
            }
            this.markChanged();
        }
        if(data.hasKey("slot")){
            setFilterContents(data);
        }
    }

    @Override
    public int[] getFilterSlots() {
        return new int[]{0, inventory.getSlots()};
    }
}
