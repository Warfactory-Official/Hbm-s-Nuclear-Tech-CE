package com.hbm.packet.toclient;

import com.hbm.interfaces.IKeypadHandler;
import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.PrecompiledPacket;
import com.hbm.util.KeypadClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KeypadClientPacket extends PrecompiledPacket {

    private int x, y, z;
    private byte[] data;
    private ByteBuf payload;

    public KeypadClientPacket() {
    }

    public KeypadClientPacket(BlockPos pos, byte[] data) {
        if (data == null || data.length != 21) throw new IllegalArgumentException();
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.data = data;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeBytes(data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.payload = buf.retain();
    }

    public static class Handler implements IMessageHandler<KeypadClientPacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(KeypadClientPacket m, MessageContext ctx) {
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
        private void handle(KeypadClientPacket m) {
            int x = m.payload.readInt();
            int y = m.payload.readInt();
            int z = m.payload.readInt();

            TileEntity te = Minecraft.getMinecraft().world.getTileEntity(new BlockPos(x, y, z));

            if (te instanceof IKeypadHandler) {
                KeypadClient pad = ((IKeypadHandler) te).getKeypad().client();
                for (int i = 0; i < 12; i++) {
                    pad.buttons[i].cooldown = m.payload.readByte();
                }
                pad.isSettingCode = m.payload.readByte() == 1;
                for (int i = 0; i < 6; i++) {
                    pad.code[i] = m.payload.readByte();
                }
                pad.successColorTicks = m.payload.readByte();
                pad.failColorTicks = m.payload.readByte();
            }
        }
    }
}
