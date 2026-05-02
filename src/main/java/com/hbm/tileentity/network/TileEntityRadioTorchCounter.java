package com.hbm.tileentity.network;

import com.hbm.blocks.network.RadioTorchBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.tileentity.IControlReceiverFilter;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.Compat;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityRadioTorchCounter extends TileEntityMachineBase implements IControlReceiverFilter, ITickable {
    public String[] channel;
    public int[] lastCount;
    public boolean polling = false;
    public ModulePatternMatcher matcher;

    public static final int MAPPING_SIZE = 3;

    public TileEntityRadioTorchCounter() {
        super(3, false, false);
        this.channel = new String[MAPPING_SIZE];
        for (int i = 0; i < MAPPING_SIZE; i++) this.channel[i] = "";
        this.lastCount = new int[MAPPING_SIZE];
        this.matcher = new ModulePatternMatcher(MAPPING_SIZE);
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

        if (!world.isRemote) {
            EnumFacing dir = getTorchFacing().getOpposite();

            TileEntity tile = Compat.getTileStandard(world, pos.getX() + dir.getXOffset(), pos.getY() + dir.getYOffset(), pos.getZ() + dir.getZOffset());
            if (tile instanceof IInventory inv) {
                ItemStack[] invSlots = new ItemStack[inv.getSizeInventory()];
                for (int i = 0; i < invSlots.length; i++) invSlots[i] = inv.getStackInSlot(i);

                for (int i = 0; i < MAPPING_SIZE; i++) {
                    if (channel[i].isEmpty()) continue;
                    if (inventory.getStackInSlot(i).isEmpty()) continue;
                    ItemStack pattern = inventory.getStackInSlot(i);

                    int count = 0;

                    for (ItemStack invSlot : invSlots) {
                        if (invSlot != null && matcher.isValidForFilter(pattern, i, invSlot)) {
                            count += invSlot.getCount();
                        }
                    }

                    if (this.polling || this.lastCount[i] != count) {
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
        for (int i = 0; i < MAPPING_SIZE; i++) BufferUtil.writeString(buf, this.channel[i]);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.polling = buf.readBoolean();
        this.lastCount = BufferUtil.readIntArray(buf);
        this.matcher.deserialize(buf);
        for (int i = 0; i < MAPPING_SIZE; i++) this.channel[i] = BufferUtil.readString(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.polling = nbt.getBoolean("polling");
        for (int i = 0; i < MAPPING_SIZE; i++) {
            this.channel[i] = nbt.getString("channel" + i);
            this.lastCount[i] = nbt.getInteger("lastCount" + i);
        }
        this.matcher.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("polling", polling);
        for (int i = 0; i < MAPPING_SIZE; i++) {
            if (channel[i] != null) nbt.setString("channel" + i, channel[i]);
            nbt.setInteger("lastCount" + i, lastCount[i]);
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
        if (data.hasKey("polling")) {
            this.polling = !this.polling;
            this.markChanged();
        } else {
            for (int i = 0; i < MAPPING_SIZE; i++) {
                this.channel[i] = data.getString("channel" + i);
            }
            this.markChanged();
        }
        if (data.hasKey("slot")) {
            setFilterContents(data);
        }
    }

    @Override
    public int[] getFilterSlots() {
        return new int[]{0, inventory.getSlots()};
    }

    private @NotNull EnumFacing getTorchFacing() {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof RadioTorchBase) {
            return state.getValue(RadioTorchBase.FACING);
        }

        int meta = this.getBlockMetadata();
        if (meta > 5) {
            meta >>= 1;
        }
        return EnumFacing.byIndex(meta);
    }
}
