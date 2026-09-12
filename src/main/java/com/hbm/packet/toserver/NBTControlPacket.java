package com.hbm.packet.toserver;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.packet.threading.PrecompiledPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class NBTControlPacket extends PrecompiledPacket {

    private int x, y, z;
    private NBTTagCompound nbt;
    private ByteBuf payload;

    public NBTControlPacket() { }

    public NBTControlPacket(NBTTagCompound nbt, BlockPos pos) {
        this(nbt, pos.getX(), pos.getY(), pos.getZ());
    }

    public NBTControlPacket(NBTTagCompound nbt, int x, int y, int z) {
        this.nbt = nbt;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        new PacketBuffer(buf).writeCompoundTag(nbt);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.payload = buf.retain();
    }

    public static class Handler implements IMessageHandler<NBTControlPacket, IMessage> {

        @Override
        public IMessage onMessage(NBTControlPacket m, MessageContext ctx) {
            ctx.getServerHandler().player.server.addScheduledTask(() -> {
                try {
                    EntityPlayerMP p = ctx.getServerHandler().player;
                    if (p == null || p.world == null) return;
                    PacketBuffer buffer = new PacketBuffer(m.payload);
                    int x = buffer.readInt();
                    int y = buffer.readInt();
                    int z = buffer.readInt();
                    BlockPos pos = new BlockPos(x, y, z);
                    TileEntity te = p.world.getTileEntity(pos);
                    NBTTagCompound nbt = buffer.readCompoundTag();
                    if (nbt != null && te instanceof IControlReceiver tile) {
                        if (tile.hasPermission(p)) {
                            PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(p, EnumHand.MAIN_HAND, pos, EnumFacing.UP,
                                    new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
                            if (!event.isCanceled()) tile.receiveControl(p, nbt);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    m.payload.release();
                }
            });

            return null;
        }
    }
}
