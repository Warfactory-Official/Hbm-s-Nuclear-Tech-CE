package com.hbm.uninos.networkproviders;

import com.hbm.uninos.GenNode;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.NodeNet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class KlystronNetwork extends NodeNet<TileEntity, TileEntity, KlystronNetwork.KlystronNode, KlystronNetwork> {

    public static INetworkProvider<KlystronNetwork> THE_PROVIDER = KlystronNetwork::new;

    @Override
    public void update() { }

    public static class KlystronNode extends GenNode<KlystronNetwork> {

        public KlystronNode(INetworkProvider<KlystronNetwork> provider, BlockPos... positions) {
            super(provider, positions);
        }
    }
}
