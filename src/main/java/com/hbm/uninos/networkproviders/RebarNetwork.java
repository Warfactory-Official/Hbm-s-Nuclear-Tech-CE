package com.hbm.uninos.networkproviders;

import com.hbm.lib.DirPos;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.NodeNet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class RebarNetwork extends NodeNet<TileEntity, TileEntity, RebarNetwork.RebarNode, RebarNetwork> {

    public static final INetworkProvider<RebarNetwork> THE_PROVIDER = RebarNetwork::new;

    @Override
    public void update() { }

    public static class RebarNode extends GenNode<RebarNetwork> {

        public RebarNode(INetworkProvider<RebarNetwork> provider, BlockPos... positions) {
            super(provider, positions);
        }

        @Override
        public RebarNode setConnections(DirPos... connections) {
            return (RebarNode) super.setConnections(connections);
        }
    }
}
