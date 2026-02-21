package com.hbm.tileentity.machine.fusion;

import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PlasmaNetwork;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityFusionCollector extends TileEntity implements ITickable {

    protected PlasmaNetwork.PlasmaNode plasmaNode;

    @Override
    public void update() {

        if(!world.isRemote) {

            if(plasmaNode == null || plasmaNode.expired) {
                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite();
                plasmaNode = UniNodespace.getNode(world, pos.add(dir.offsetX * 2, 2, dir.offsetZ * 2), PlasmaNetwork.THE_PROVIDER);

                if(plasmaNode == null) {
                    plasmaNode = (PlasmaNetwork.PlasmaNode) new PlasmaNetwork.PlasmaNode(PlasmaNetwork.THE_PROVIDER,
                            new BlockPos(pos.getX() + dir.offsetX * 2, pos.getY() + 2, pos.getZ() + dir.offsetZ * 2))
                            .setConnections(new DirPos(pos.getX() + dir.offsetX * 3, pos.getY() + 2, pos.getZ() + dir.offsetZ * 3, dir));

                    UniNodespace.createNode(world, plasmaNode);
                }
            }

            if(plasmaNode != null && plasmaNode.hasValidNet()) plasmaNode.net.addReceiver(this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if(!world.isRemote) {
            if(this.plasmaNode != null) UniNodespace.destroyNode(world, plasmaNode);
        }
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 2,
                    pos.getY(),
                    pos.getZ() - 2,
                    pos.getX() + 3,
                    pos.getY() + 4,
                    pos.getZ() + 3
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
