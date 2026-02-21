package com.hbm.tileentity.network;

import com.hbm.api.fluidmk2.FluidNode;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
@AutoRegister
public class TileEntityPipeAnchor extends TileEntityPipelineBase {

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.SMALL;
    }

    @Override
    public Vec3d getMountPos() {
        return new Vec3d(0.5, 0.5, 0.5);
    }

    @Override
    public double getMaxPipeLength() {
        return 10;
    }

    @Override
    public FluidNode createNode(FluidType type) {
        TileEntity tile = this;
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
        FluidNode node = new FluidNode(type.getNetworkProvider(), tile.getPos()).setConnections(
                new DirPos(pos.getX(), pos.getY(), pos.getZ(), ForgeDirection.UNKNOWN),
                new DirPos(pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir));
        for(int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
        return node;
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite() == dir && type == this.type;
    }
}
