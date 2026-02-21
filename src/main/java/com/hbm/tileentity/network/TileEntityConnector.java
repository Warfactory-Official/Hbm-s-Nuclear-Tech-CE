package com.hbm.tileentity.network;

import com.hbm.api.energymk2.Nodespace;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.network.energy.TileEntityPylonBase;
import net.minecraft.util.math.Vec3d;

@AutoRegister
public class TileEntityConnector extends TileEntityPylonBase {

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.SINGLE;
    }

    @Override
    public Vec3d[] getMountPos() {
        return new Vec3d[]{new Vec3d(0.5, 0.5, 0.5)};
    }

    @Override
    public double getMaxWireLength() {
        return 10;
    }

    @Override
    public Nodespace.PowerNode createNode() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
        Nodespace.PowerNode node = new Nodespace.PowerNode(getPos()).setConnections(
                new DirPos(getPos(), ForgeDirection.UNKNOWN),
                new DirPos(getPos().getX() + dir.offsetX, getPos().getY() + dir.offsetY, getPos().getZ() + dir.offsetZ, dir));
        for (int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
        return node;
    }

    @Override
    public boolean canConnect(ForgeDirection dir) {
        return ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite() == dir;
    }
}
