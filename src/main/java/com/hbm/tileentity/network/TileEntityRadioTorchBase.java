package com.hbm.tileentity.network;

import com.hbm.blocks.network.RadioTorchBase;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityRadioTorchBase extends TileEntityLoadedBase implements ITickable, IControlReceiver, CompatHandler.OCComponent {
    private static final int MAPPING_SIZE = 16;

    /**
     * channel we're broadcasting on/listening to
     */
    public String channel = "";
    /**
     * previous redstone state for input/output, needed for state change detection
     */
    public int lastState = 0;
    /**
     * last update tick, needed for receivers listening for changes
     */
    public long lastUpdate;
    /**
     * switches state change mode to tick-based polling
     */
    public boolean polling = false;
    /**
     * switches redstone passthrough to custom signal mapping
     */
    public boolean customMap = false;
    /**
     * custom mapping
     */
    public String[] mapping = new String[MAPPING_SIZE];

    @Override
    public boolean shouldRefresh(@NotNull World world, @NotNull BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void update() {

        if (!world.isRemote) {
            networkPackNT(50);
        }
    }

    protected @NotNull EnumFacing getTorchFacing() {
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof RadioTorchBase) {
                return state.getValue(RadioTorchBase.FACING);
            }
        }

        int meta = this.getBlockMetadata();
        if (meta > 5) {
            meta >>= 1;
        }
        return EnumFacing.byIndex(meta);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.polling = nbt.getBoolean("isPolling");
        this.customMap = nbt.getBoolean("hasMapping");
        this.lastState = nbt.getInteger("lastPower");
        this.lastUpdate = nbt.getLong("lastTime");
        this.channel = nbt.getString("channel");
        for (int i = 0; i < MAPPING_SIZE; i++) {
            this.mapping[i] = nbt.getString("mapping" + i);
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("isPolling", polling);
        nbt.setBoolean("hasMapping", customMap);
        nbt.setInteger("lastPower", lastState);
        nbt.setLong("lastTime", lastUpdate);
        if (channel != null) nbt.setString("channel", channel);
        for (int i = 0; i < MAPPING_SIZE; i++) {
            if (mapping[i] != null) {
                nbt.setString("mapping" + i, mapping[i]);
            }
        }
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return new Vec3d(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).length() < 16;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("isPolling")) this.polling = data.getBoolean("isPolling");
        if (data.hasKey("hasMapping")) this.customMap = data.getBoolean("hasMapping");
        if (data.hasKey("channel")) this.channel = data.getString("channel");
        for (int i = 0; i < MAPPING_SIZE; i++) {
            if (data.hasKey("mapping" + i)) {
                this.mapping[i] = data.getString("mapping" + i);
            }
        }

        this.markDirty();
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(polling);
        buf.writeBoolean(customMap);
        buf.writeInt(lastState);
        buf.writeBoolean(channel != null);
        if (channel != null) BufferUtil.writeString(buf, channel);

        for (int i = 0; i < MAPPING_SIZE; i++) {
            buf.writeBoolean(mapping[i] != null);
            if (mapping[i] != null) BufferUtil.writeString(buf, mapping[i]);
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.polling = buf.readBoolean();
        this.customMap = buf.readBoolean();
        this.lastState = buf.readInt();

        if (buf.readBoolean()) {
            this.channel = BufferUtil.readString(buf);
        }

        for (int i = 0; i < MAPPING_SIZE; i++) {
            if (buf.readBoolean()) {
                this.mapping[i] = BufferUtil.readString(buf);
            }
        }
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "radio_torch";
    }

    @Callback(direct = true, limit = 4, doc = "setChannel(channel: string) -- Set the channel the torch is listening/broadcasting to")
    @Optional.Method(modid = "opencomputers")
    public Object[] setChannel(Context context, Arguments args) {
        channel = args.checkString(0);
        return new Object[]{};
    }

    @Callback(direct = true, limit = 4, doc = "setPolling(value: boolean) -- Switches state change mode to tick-based polling")
    @Optional.Method(modid = "opencomputers")
    public Object[] setPolling(Context context, Arguments args) {
        polling = args.checkBoolean(0);
        return new Object[]{};
    }

    @Callback(direct = true, limit = 4, doc = "setCustomMap(value: boolean) -- Switches redstone passthrough to custom signal mapping")
    @Optional.Method(modid = "opencomputers")
    public Object[] setCustomMap(Context context, Arguments args) {
        customMap = args.checkBoolean(0);
        return new Object[]{};
    }

    @Callback(direct = true, limit = 4, doc = "setCustomMapValues(value: table) -- Sets the custom signal mapping values with a table with indices corresponding to the redstone value (1-16)")
    @Optional.Method(modid = "opencomputers")
    public Object[] setCustomMapValues(Context context, Arguments args) {
        Map values = args.checkTable(0);

        for (int i = 1; i <= 16; i++) {
            if (values.containsKey(i) && values.get(i) instanceof String) {
                this.mapping[i - 1] = (String) values.get(i);
            }
        }

        return new Object[]{};
    }
}
