package com.hbm.interfaces;

import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.util.List;

public interface IMixinFMLProxyPacket {
    List<Packet<INetHandlerPlayClient>> hbm$toS3FPackets();
}
