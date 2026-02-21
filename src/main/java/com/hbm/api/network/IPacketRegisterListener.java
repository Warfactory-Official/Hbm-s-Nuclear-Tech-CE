package com.hbm.api.network;

import com.hbm.packet.PacketDispatcher;

public interface IPacketRegisterListener {

    /**
     * Called at the end of {@link PacketDispatcher#registerPackets()}
     * @return next id available
     */
    int registerPackets(int nextId);
}
