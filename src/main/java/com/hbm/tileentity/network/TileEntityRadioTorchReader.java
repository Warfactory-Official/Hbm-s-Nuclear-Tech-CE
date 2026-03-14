package com.hbm.tileentity.network;

import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.blocks.network.RadioTorchBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.Compat;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@AutoRegister
public class TileEntityRadioTorchReader extends TileEntityLoadedBase implements IControlReceiver, ITickable {

    public static final int MAPPING_SIZE = 8;

    public String[] channels = new String[MAPPING_SIZE];
    public String[] names = new String[MAPPING_SIZE];
    public String[] prev = new String[MAPPING_SIZE];
    public boolean polling = false;

    public TileEntityRadioTorchReader() {
        Arrays.fill(channels, "");
        Arrays.fill(names, "");
        Arrays.fill(prev, "");
    }

    @Override
    public void update() {

        if (!world.isRemote) {
            EnumFacing dir = getTorchFacing().getOpposite();

            TileEntity tile = Compat.getTileStandard(world, pos.getX() + dir.getXOffset(), pos.getY() + dir.getYOffset(), pos.getZ() + dir.getZOffset());

            if (tile instanceof IRORValueProvider prov) {

                for (int i = 0; i < MAPPING_SIZE; i++) {
                    String channel = channels[i];
                    String name = names[i];
                    String previous = prev[i];

                    if (channel == null || channel.isEmpty()) continue;
                    if (name == null || name.isEmpty()) continue;

                    String value = prov.provideRORValue(IRORValueProvider.PREFIX_VALUE + name);
                    if (value == null) continue;

                    if (polling || !value.equals(previous)) {
                        RTTYSystem.broadcast(world, channel, value);
                        this.prev[i] = value;
                    }
                }
            }

            networkPackNT(50);
        }
    }

    @Override
    public @NotNull NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = super.getUpdateTag();
        writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void handleUpdateTag(@NotNull NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        readFromNBT(tag);
    }

    @Override
    public @Nullable SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new SPacketUpdateTileEntity(this.pos, 0, nbt);
    }

    @Override
    public void onDataPacket(@NotNull NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(this.polling);
        for (String channel : channels) BufferUtil.writeString(buf, channel);
        for (String name : names) BufferUtil.writeString(buf, name);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.polling = buf.readBoolean();
        for (int i = 0; i < channels.length; i++) channels[i] = BufferUtil.readString(buf);
        for (int i = 0; i < names.length; i++) names[i] = BufferUtil.readString(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.polling = nbt.getBoolean("polling");
        for (int i = 0; i < channels.length; i++) channels[i] = nbt.getString("channels" + i);
        for (int i = 0; i < names.length; i++) names[i] = nbt.getString("names" + i);
        for (int i = 0; i < prev.length; i++) prev[i] = nbt.getString("prev" + i);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("polling", polling);
        for (int i = 0; i < channels.length; i++) nbt.setString("channels" + i, channels[i]);
        for (int i = 0; i < names.length; i++) nbt.setString("names" + i, names[i]);
        for (int i = 0; i < prev.length; i++) nbt.setString("prev" + i, prev[i]);
        return nbt;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("polling")) this.polling = data.getBoolean("polling");
        for (int i = 0; i < channels.length; i++)
            if (data.hasKey("channels" + i)) channels[i] = data.getString("channels" + i);
        for (int i = 0; i < names.length; i++) if (data.hasKey("names" + i)) names[i] = data.getString("names" + i);

        this.markDirty();
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return player.getDistance(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5) < 16D;
    }

    public @NotNull EnumFacing getTorchFacing() {
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
