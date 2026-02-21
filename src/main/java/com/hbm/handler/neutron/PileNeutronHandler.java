package com.hbm.handler.neutron;

import com.hbm.api.block.IPileNeutronReceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.pile.TileEntityPileBase;
import com.hbm.util.ContaminationUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class PileNeutronHandler {

    public static int range = 5;

    public static class PileNeutronNode extends NeutronNode {

        public PileNeutronNode(TileEntityPileBase tile) {
            super(tile, NeutronStream.NeutronType.PILE);
        }

    }

    public static PileNeutronNode makeNode(NeutronNodeWorld.StreamWorld streamWorld, TileEntityPileBase tile) {
        BlockPos pos = tile.getPos();
        PileNeutronNode node = (PileNeutronNode) streamWorld.getNode(pos);
        return node != null ? node : new PileNeutronNode(tile);
    }

    private static TileEntity blockPosToTE(World worldObj, BlockPos pos) {
        return worldObj.getTileEntity(pos);
    }

    public static class PileNeutronStream extends NeutronStream {

        public PileNeutronStream(NeutronNode origin, Vec3d vector, double flux) {
            super(origin, vector, flux, 0D, NeutronType.PILE);
        }

        @Override
        public void runStreamInteraction(World worldObj, NeutronNodeWorld.StreamWorld streamWorld) {

            TileEntityPileBase originTE = (TileEntityPileBase) origin.tile;
            BlockPos pos = originTE.getPos();

            for(float i = 1; i <= range; i += 0.5F) {

                BlockPos nodePos = new BlockPos(
                        (int)Math.floor(pos.getX() + 0.5 + vector.x * i),
                        (int)Math.floor(pos.getY() + 0.5 + vector.y * i),
                        (int)Math.floor(pos.getZ() + 0.5 + vector.z * i)
                );

                if(nodePos.equals(pos))
                    continue; // don't interact with itself!

                pos = new BlockPos(nodePos.getX(), nodePos.getY(), nodePos.getZ());

                TileEntity tile;

                NeutronNode node = streamWorld.getNode(nodePos);
                if(node instanceof PileNeutronNode) {
                    tile = node.tile;
                } else {
                    tile = blockPosToTE(worldObj, nodePos);
                    if(tile == null) return;

                    if(tile instanceof TileEntityPileBase) {
                        streamWorld.addNode(new PileNeutronNode((TileEntityPileBase) tile));
                    }
                }

                Block block = tile.getBlockType();
                int meta = tile.getBlockMetadata();
                if(!(tile instanceof TileEntityPileBase)) {

                    // Return when a boron block is hit
                    if(block == ModBlocks.block_boron)
                        return;

                    else if(block == ModBlocks.concrete ||
                            block == ModBlocks.concrete_smooth ||
                            block == ModBlocks.concrete_asbestos ||
                            block == ModBlocks.concrete_colored ||
                            block == ModBlocks.brick_concrete)
                        fluxQuantity *= 0.25;

                    if(block == ModBlocks.block_graphite_rod && (meta & 8) == 0)
                        return;
                }

                if(tile instanceof IPileNeutronReceiver rec) {

                    rec.receiveNeutrons((int) Math.floor(fluxQuantity));

                    if(block != ModBlocks.block_graphite_detector || (meta & 8) == 0) return;
                }

                int x = (int) (nodePos.getX() + 0.5);
                int y = (int) (nodePos.getY() + 0.5);
                int z = (int) (nodePos.getZ() + 0.5);
                List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(x, y, z, x, y, z));

                for(EntityLivingBase e : entities)
                    ContaminationUtil.contaminate(e, ContaminationUtil.HazardType.RADIATION, ContaminationUtil.ContaminationType.CREATIVE, (float) (fluxQuantity / 4D));
            }
        }
    }
}
