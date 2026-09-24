package com.hbm.tileentity.network;

import net.minecraft.world.IBlockAccess;

public interface ICachedPipeConnections {

    byte getCachedConnectionMask(IBlockAccess access);

    void invalidateConnectionCache();
}
