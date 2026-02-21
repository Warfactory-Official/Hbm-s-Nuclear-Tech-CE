package com.hbm.uninos.networkproviders;

import com.hbm.uninos.GenNode;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.NodeNet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

// potentially already FIXME idfk which arguments should be here for NodeNet
public class PlasmaNetwork extends NodeNet<TileEntity, TileEntity, PlasmaNetwork.PlasmaNode, PlasmaNetwork> {

    public static INetworkProvider<PlasmaNetwork> THE_PROVIDER = PlasmaNetwork::new;

    @Override
    public void update() { }

    public static class PlasmaNode extends GenNode<PlasmaNetwork> {

        public PlasmaNode(INetworkProvider<PlasmaNetwork> provider, BlockPos... positions) {
            super(provider, positions);
        }
    }
}
