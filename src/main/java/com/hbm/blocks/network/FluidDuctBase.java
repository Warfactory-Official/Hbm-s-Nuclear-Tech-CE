package com.hbm.blocks.network;

import com.hbm.api.fluidmk2.FluidNetMK2;
import com.hbm.api.fluidmk2.FluidNode;
import com.hbm.blocks.IAnalyzable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.uninos.UniNodespace;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

// Th3_Sl1ze: do I want to replace ItemFluidIDMulti with setting fluid here? well uhhhh.. not yet
public abstract class FluidDuctBase extends BlockContainer implements IAnalyzable {

    public FluidDuctBase(Material mat) {
        super(mat);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityPipeBaseNT();
    }

    @Override
    public List<String> getDebugInfo(World world, BlockPos pos) {

        TileEntity te = world.getTileEntity(pos);

        if(te instanceof TileEntityPipeBaseNT pipe) {
            FluidType type = pipe.getType();

            if(type != null) {

                FluidNode node = UniNodespace.getNode(world, pos, type.getNetworkProvider());

                if(node != null && node.net != null) {
                    FluidNetMK2 net = node.net;

                    List<String> debug = new ArrayList<>();
                    debug.add("Links: " + net.links.size());
                    debug.add("Subscribers: " + net.receiverEntries.size());
                    debug.add("Providers: " + net.providerEntries.size());
                    debug.add("Transfer: " + net.fluidTracker);
                    return debug;
                }
            }
        }

        return null;
    }
}
