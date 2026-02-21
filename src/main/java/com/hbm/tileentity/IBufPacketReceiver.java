package com.hbm.tileentity;

import io.netty.buffer.ByteBuf;

public interface IBufPacketReceiver {

    void serialize(ByteBuf buf);

    void deserialize(ByteBuf buf);

    void networkPackNT(int range);
}
