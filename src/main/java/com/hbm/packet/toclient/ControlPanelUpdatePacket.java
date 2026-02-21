package com.hbm.packet.toclient;

import com.hbm.inventory.control_panel.ControlPanel;
import com.hbm.inventory.control_panel.DataValue;
import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.PrecompiledPacket;
import com.hbm.tileentity.machine.TileEntityControlPanel;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ControlPanelUpdatePacket extends PrecompiledPacket {
    private int x, y, z;
    private List<VarUpdate> updates;
    private NBTTagCompound fullNbt;
    private ByteBuf payload;

    public ControlPanelUpdatePacket() {
    }

    public ControlPanelUpdatePacket(BlockPos pos, List<VarUpdate> toUpdate) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.updates = toUpdate;
    }

    public ControlPanelUpdatePacket(BlockPos pos, NBTTagCompound tag) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.fullNbt = tag;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        PacketBuffer wrapper = new PacketBuffer(buf);
        if (updates == null) {
            buf.writeInt(-1);
            wrapper.writeCompoundTag(fullNbt);
        } else {
            buf.writeInt(updates.size());
            for (VarUpdate u : updates) {
                if (u != null) {
                    buf.writeInt(u.varListIdx);
                    wrapper.writeString(u.varName);
                } else {
                    buf.writeInt(0);
                    wrapper.writeString("");
                }
            }

            NBTTagCompound tag = new NBTTagCompound();
            int i = 0;
            for (VarUpdate u : updates) {
                if (u != null && u.data != null) {
                    tag.setTag(String.valueOf(i), u.data.writeToNBT());
                }
                i++;
            }
            wrapper.writeCompoundTag(tag);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.payload = buf.retain();
    }

    public static class Handler implements IMessageHandler<ControlPanelUpdatePacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(ControlPanelUpdatePacket m, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                try {
                    handle(m);
                } catch (Exception e) {
                    MainRegistry.logger.catching(e);
                } finally {
                    m.payload.release();
                }
            });
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void handle(ControlPanelUpdatePacket m) {
            PacketBuffer buffer = new PacketBuffer(m.payload);

            int x = buffer.readInt();
            int y = buffer.readInt();
            int z = buffer.readInt();
            int size = buffer.readInt();

            TileEntity te = Minecraft.getMinecraft().world.getTileEntity(new BlockPos(x, y, z));
            if (!(te instanceof TileEntityControlPanel)) return;

            ControlPanel control = ((TileEntityControlPanel) te).panel;

            if (size == -1) {
                try {
                    NBTTagCompound tag = buffer.readCompoundTag();
                    control.readFromNBT(tag);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                VarUpdate[] receivedUpdates = new VarUpdate[size];

                for (int i = 0; i < size; i++) {
                    int idx = buffer.readInt();
                    String name = buffer.readString(32);
                    receivedUpdates[i] = new VarUpdate(idx, name, null);
                }

                try {
                    NBTTagCompound dataTag = buffer.readCompoundTag();
                    if (dataTag != null) {
                        for (int i = 0; i < size; i++) {
                            if (dataTag.hasKey(String.valueOf(i))) {
                                receivedUpdates[i].data = DataValue.newFromNBT(dataTag.getTag(String.valueOf(i)));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (VarUpdate u : receivedUpdates) {
                    if (u.varName == null) continue;

                    if (u.varListIdx == -1) {
                        if (u.data == null) {
                            control.globalVars.remove(u.varName);
                        } else {
                            control.globalVars.put(u.varName, u.data);
                        }
                    } else {
                        if (u.varListIdx < control.controls.size()) {
                            if (u.data == null) {
                                control.controls.get(u.varListIdx).vars.remove(u.varName);
                            } else {
                                control.controls.get(u.varListIdx).vars.put(u.varName, u.data);
                            }
                        }
                    }
                }
            }
        }
    }

    public static class VarUpdate {
        final int varListIdx;
        final String varName;
        public DataValue data;

        public VarUpdate(int varListIdx, String varName, DataValue data) {
            this.varListIdx = varListIdx;
            this.varName = varName;
            this.data = data;
        }
    }
}
