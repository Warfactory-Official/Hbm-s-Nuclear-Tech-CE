package com.hbm.tileentity.network;

import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.network.RTTYSystem.RTTYChannel;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.hbm.blocks.network.RadioTorchBase.LIT;

@AutoRegister
public class TileEntityRadioTorchLogic extends TileEntityLoadedBase implements IControlReceiver, ITickable {

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
     * switches evaluation of conditions from ascending to descending
     */
    public boolean descending = false;
    /**
     * mapping for constants to compare against
     */
    public String[] mapping;
    /**
     * mapping for conditions through [1, 10], being (<, <=, >=, >, ==, !=, equals, !equals, contains, !contains)
     */
    public int[] conditions;

    public static final int MAPPING_SIZE = 16;

    public TileEntityRadioTorchLogic() {
        this.mapping = new String[MAPPING_SIZE];
        for (int i = 0; i < MAPPING_SIZE; i++) this.mapping[i] = "";
        this.conditions = new int[MAPPING_SIZE];
        for (int i = 0; i < MAPPING_SIZE; i++) this.conditions[i] = 0;
    }

    @Override
    public boolean shouldRefresh(@NotNull World world, @NotNull BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            if (!this.channel.isEmpty()) {

                RTTYChannel chan = RTTYSystem.listen(world, this.channel);

                if (chan != null && (this.polling || (chan.timeStamp > this.lastUpdate - 1 && chan.timeStamp != -1))) { // if we're either polling or a new message has come in
                    String msg = "" + chan.signal;
                    this.lastUpdate = world.getTotalWorldTime();
                    int nextState = 0; //if no remap apply, default to 0

                    if (chan.timeStamp < this.lastUpdate - 2 && this.polling) {
                        /* the vast majority use-case for this is going to be inequalities, NOT parsing, and the input is undefined - not the output
                         * if no signal => 0 for polling, advanced users parsing strings can easily accommodate this fact instead of breaking numerical torches */
                        msg = "0";
                    }

                    if (descending) {
                        for (int i = MAPPING_SIZE - 1; i >= 0; i--) {
                            if (!mapping[i].isEmpty() && parseSignal(msg, i)) {
                                nextState = i;
                                break;
                            }
                        }
                    } else {
                        for (int i = 0; i <= MAPPING_SIZE - 1; i++) {
                            if (!mapping[i].isEmpty() && parseSignal(msg, i)) {
                                nextState = i;
                                break;
                            }
                        }
                    }

                    if (this.lastState != nextState) {
                        this.lastState = nextState;

                        IBlockState state = world.getBlockState(pos);

                        boolean oldLit = state.getValue(LIT);
                        boolean newLit = this.lastState > 0;

                        if (oldLit != newLit) {
                            world.setBlockState(pos, state.withProperty(LIT, newLit), 3);
                        }

                        this.markDirty();
                    }
                }
            }

            networkPackNT(50);
        }
    }

    public boolean parseSignal(String signal, int index) {
        if (conditions[index] <= 5) { //if a non-string operator
            int sig;
            int map;
            try {
                sig = Integer.parseInt(signal);
                map = Integer.parseInt(mapping[index]);
            } catch (Exception x) {
                return false; //not a valid input; skip! slightly annoying about the mapping but we'll restrict input anyway
            }

            return switch (conditions[index]) {
                case 1 -> sig <= map;
                case 2 -> sig >= map;
                case 3 -> sig > map;
                case 4 -> sig == map;
                case 5 -> sig != map;
                default -> sig < map;
            };
        }

        return switch (conditions[index]) {
            case 7 -> !signal.equals(mapping[index]);
            case 8 -> signal.contains(mapping[index]);
            case 9 -> !signal.contains(mapping[index]);
            default -> signal.equals(mapping[index]);
        };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.polling = nbt.getBoolean("polling");
        this.descending = nbt.getBoolean("descending");
        this.lastState = nbt.getInteger("lastState");
        this.lastUpdate = nbt.getLong("lastUpdate");
        this.channel = nbt.getString("channel");
        for (int i = 0; i < MAPPING_SIZE; i++) this.mapping[i] = nbt.getString("mapping" + i);
        for (int i = 0; i < MAPPING_SIZE; i++) this.conditions[i] = nbt.getInteger("conditions" + i);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("polling", polling);
        nbt.setBoolean("descending", descending);
        nbt.setInteger("lastState", lastState);
        nbt.setLong("lastUpdate", lastUpdate);
        if (channel != null) nbt.setString("channel", channel);
        for (int i = 0; i < MAPPING_SIZE; i++) if (!mapping[i].isEmpty()) nbt.setString("mapping" + i, mapping[i]);
        for (int i = 0; i < MAPPING_SIZE; i++) if (conditions[i] > 0) nbt.setInteger("conditions" + i, conditions[i]);
        return nbt;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(this.polling);
        BufferUtil.writeString(buf, this.channel);
        buf.writeBoolean(this.descending);
        for (int i = 0; i < MAPPING_SIZE; i++) BufferUtil.writeString(buf, this.mapping[i]);
        for (int i = 0; i < MAPPING_SIZE; i++) buf.writeInt(this.conditions[i]);
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
    public void deserialize(ByteBuf buf) {
        this.polling = buf.readBoolean();
        this.channel = BufferUtil.readString(buf);
        this.descending = buf.readBoolean();
        for (int i = 0; i < MAPPING_SIZE; i++) this.mapping[i] = BufferUtil.readString(buf);
        for (int i = 0; i < MAPPING_SIZE; i++) this.conditions[i] = buf.readInt();
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("lastState", (byte) this.lastState);
        return new SPacketUpdateTileEntity(this.pos, 0, nbt);
    }

    @Override
    public void onDataPacket(@NotNull NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.lastState = pkt.getNbtCompound().getByte("lastState");
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return player.getDistanceSq(pos) < 16;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("polling")) this.polling = data.getBoolean("polling");
        if (data.hasKey("channel")) this.channel = data.getString("channel");
        if (data.hasKey("descending")) this.descending = data.getBoolean("descending");
        for (int i = 0; i < MAPPING_SIZE; i++)
            if (data.hasKey("mapping" + i)) this.mapping[i] = data.getString("mapping" + i);
        for (int i = 0; i < MAPPING_SIZE; i++)
            if (data.hasKey("conditions" + i)) this.conditions[i] = data.getInteger("conditions" + i);

        this.markDirty();
    }
}
