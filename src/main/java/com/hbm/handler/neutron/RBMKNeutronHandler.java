package com.hbm.handler.neutron;

import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.handler.neutron.NeutronNodeWorld.StreamWorld;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.rbmk.*;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
public class RBMKNeutronHandler {

    static double moderatorEfficiency;
    static double reflectorEfficiency;
    static double absorberEfficiency;
    static int columnHeight;
    static int fluxRange;

    public enum RBMKType {
        ROD,
        MODERATOR,
        CONTROL_ROD,
        REFLECTOR,
        ABSORBER,
        OUTGASSER,
        OTHER // why do neutron calculations on them if they won't change anything?
    }

    private static TileEntity blockPosToTE(World worldObj, BlockPos pos) {
        return worldObj.getTileEntity(pos);
    }

    public static RBMKNeutronNode makeNode(StreamWorld streamWorld, TileEntityRBMKBase tile) {
        BlockPos pos = tile.getPos();
        RBMKNeutronNode node = (RBMKNeutronNode) streamWorld.getNode(pos);
        return node != null ? node : new RBMKNeutronNode(tile, tile.getRBMKType(), tile.hasLid());
    }

    public static class RBMKNeutronNode extends NeutronNode {
        public boolean hasLid;
        public RBMKType rbmkType;
        protected BlockPos.MutableBlockPos posInstance;

        public RBMKNeutronNode(TileEntityRBMKBase tile, RBMKType type, boolean hasLid) {
            super(tile, NeutronStream.NeutronType.RBMK);
            this.hasLid = hasLid;
            this.rbmkType = type;
            posInstance = new BlockPos.MutableBlockPos(tile.getPos());
        }

        public void addLid() { this.hasLid = true; }
        public void removeLid() { this.hasLid = false; }


        public void checkNode(StreamWorld streamWorld, List<Long> toRemove) {
            BlockPos pos = this.tile.getPos();
            ForgeDirection[] fluxDirs = TileEntityRBMKRod.fluxDirs;

            // Check if the rod should uncache nodes.
            if(tile instanceof TileEntityRBMKRod rod && !(tile instanceof TileEntityRBMKRodReaSim)) {
                if(!rod.hasRod || rod.lastFluxQuantity == 0) {
                    BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
                    for(int i = 0; i < fluxDirs.length; i++) {
                        ForgeDirection dir = fluxDirs[i];
                        for(int j = 1; j <= fluxRange; j++) {
                            mut.setPos(pos.getX() + dir.offsetX * j, pos.getY(), pos.getZ() + dir.offsetZ * j);
                            long lPos = mut.toLong();
                            if(streamWorld.nodeCache.containsKey(lPos)) toRemove.add(lPos);
                        }
                    }
                    return;
                }
            }

            if(tile instanceof TileEntityRBMKRodReaSim) {
                TileEntityRBMKRodReaSim rod = (TileEntityRBMKRodReaSim) tile;
                if(!rod.hasRod || rod.lastFluxQuantity == 0) {
                    BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
                    int r2 = fluxRange * fluxRange;
                    for(int dx = -fluxRange; dx <= fluxRange; dx++) {
                        for(int dz = -fluxRange; dz <= fluxRange; dz++) {
                            if(dx * dx + dz * dz <= r2) {
                                mut.setPos(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                                toRemove.add(mut.toLong());
                            }
                        }
                    }
                    return;
                }
            }

            /* Optimized presence check */
            boolean hasRod = false;
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
            int r2 = fluxRange * fluxRange;
            search:
            for(int dx = -fluxRange; dx <= fluxRange; dx++) {
                for(int dz = -fluxRange; dz <= fluxRange; dz++) {
                    if(dx * dx + dz * dz <= r2) {
                        mut.setPos(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                        NeutronNode node = streamWorld.getNode(mut.toLong());
                        if(node != null && node.tile instanceof TileEntityRBMKRod) {
                            TileEntityRBMKRod rod = (TileEntityRBMKRod) node.tile;
                            if(rod.hasRod && rod.lastFluxQuantity > 0) {
                                hasRod = true;
                                break search;
                            }
                        }
                    }
                }
            }

            if(!hasRod) {
                toRemove.add(pos.toLong());
                return;
            }

            // Fallback check for streams
            for(int i = 0; i < fluxDirs.length; i++) {
                ForgeDirection dir = fluxDirs[i];
                for(int j = 1; j <= fluxRange; j++) {
                    mut.setPos(pos.getX() + dir.offsetX * j, pos.getY(), pos.getZ() + dir.offsetZ * j);
                    NeutronNode node = streamWorld.getNode(mut.toLong());
                    if(node != null && node.tile instanceof TileEntityRBMKRod) return;
                }
            }

            toRemove.add(pos.toLong());
        }
    }


    public static class RBMKNeutronStream extends NeutronStream {

        public RBMKNeutronStream(NeutronNode origin, Vec3d vector) {
            super(origin, vector);
        }

        public RBMKNeutronStream(NeutronNode origin, Vec3d vector, double flux, double ratio) {
            super(origin, vector, flux, ratio, NeutronType.RBMK);
        }

        // Does NOT include the origin node
        // USES THE CACHE!!!
        public NeutronNode[] getNodes(StreamWorld streamWorld, boolean addNode) {
            NeutronNode[] positions = new RBMKNeutronNode[fluxRange];
            BlockPos.MutableBlockPos posMut = new BlockPos.MutableBlockPos(origin.tile.getPos());
            World world = origin.tile.getWorld();

            for(int i = 1; i <= fluxRange; i++) {
                int xOffset = (int) (vector.x * i);
                int zOffset = (int) (vector.z * i);
                posMut.setPos(origin.tile.getPos().getX() + xOffset, origin.tile.getPos().getY(), origin.tile.getPos().getZ() + zOffset);

                NeutronNode node = streamWorld.getNode(posMut.toLong());
                if(node instanceof RBMKNeutronNode) {
                    positions[i - 1] = node;
                } else if(this.origin.tile.getBlockType() instanceof RBMKBase) {
                    TileEntity te = world.getTileEntity(posMut);
                    if(te instanceof TileEntityRBMKBase) {
                        node = makeNode(streamWorld, (TileEntityRBMKBase) te);
                        positions[i - 1] = node;
                        if(addNode) streamWorld.addNode(node);
                    }
                }
            }
            return positions;
        }

        // The... small one? whatever it's still pretty big, runs the interaction for the stream.
        public void runStreamInteraction(World worldObj, StreamWorld streamWorld) {
            if(fluxQuantity <= 0D) return;

            BlockPos originPos = origin.tile.getPos();
            RBMKNeutronNode originNode = (RBMKNeutronNode) streamWorld.getNode(originPos.toLong());
            if(originNode == null) {
                TileEntityRBMKBase originTE = (TileEntityRBMKBase) worldObj.getTileEntity(originPos);
                if(originTE == null) return;
                originNode = new RBMKNeutronNode(originTE, originTE.getRBMKType(), originTE.hasLid());
                streamWorld.addNode(originNode);
            }

            TileEntityRBMKBase originTE = (TileEntityRBMKBase) originNode.tile;
            int moderatedCount = 0;
            BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();

            for(int i = 1; i <= fluxRange; i++) {
                if(fluxQuantity <= 0D) return;

                int dx = (int) (vector.x * i);
                int dz = (int) (vector.z * i);
                targetPos.setPos(originPos.getX() + dx, originPos.getY(), originPos.getZ() + dz);

                NeutronNode targetNode = streamWorld.getNode(targetPos.toLong());
                if(targetNode == null) {
                    TileEntity te = worldObj.getTileEntity(targetPos);
                    if(te instanceof TileEntityRBMKBase) {
                        targetNode = makeNode(streamWorld, (TileEntityRBMKBase) te);
                        streamWorld.addNode(targetNode);
                    } else {
                        int hits = getHits(worldObj, targetPos);
                        if(hits >= columnHeight) return;
                        if(hits > 0) {
                            irradiateFromFlux(worldObj, originPos, hits);
                            fluxQuantity *= 1.0 - ((double) hits / columnHeight);
                            continue;
                        } else {
                            irradiateFromFlux(worldObj, originPos, 0);
                            continue;
                        }
                    }
                }

                RBMKNeutronNode rNode = (RBMKNeutronNode) targetNode;
                RBMKType type = rNode.rbmkType;

                if(type == RBMKType.OTHER || type == null) continue;

                TileEntityRBMKBase nodeTE = (TileEntityRBMKBase) rNode.tile;

                if(!rNode.hasLid)
                    ChunkRadiationManager.proxy.incrementRad(worldObj, targetPos, (float) (this.fluxQuantity * 0.05F));

                if(type == RBMKType.MODERATOR || nodeTE.isModerated()) {
                    moderatedCount++;
                    moderateStream();
                }

                if(nodeTE instanceof IRBMKFluxReceiver) {
                    IRBMKFluxReceiver receiver = (IRBMKFluxReceiver) nodeTE;
                    if(type == RBMKType.ROD) {
                        TileEntityRBMKRod rod = (TileEntityRBMKRod) receiver;
                        if(rod.hasRod) {
                            rod.receiveFlux(this);
                            return;
                        }
                    } else if(type == RBMKType.OUTGASSER) {
                        TileEntityRBMKOutgasser outgasser = ((TileEntityRBMKOutgasser) receiver);
                        if(outgasser.canProcess()) {
                            receiver.receiveFlux(this);
                            return;
                        }
                    }
                } else if(type == RBMKType.CONTROL_ROD) {
                    TileEntityRBMKControl rod = (TileEntityRBMKControl) nodeTE;
                    if(rod.level > 0.0D) {
                        this.fluxQuantity *= rod.getMult();
                        continue;
                    }
                    return;
                } else if(type == RBMKType.REFLECTOR) {
                    if(originTE.isModerated()) moderatedCount++;
                    if(this.fluxRatio > 0 && moderatedCount > 0) {
                        for(int j = 0; j < moderatedCount; j++) moderateStream();
                    }
                    if(reflectorEfficiency != 1.0D) {
                        this.fluxQuantity *= reflectorEfficiency;
                        continue;
                    }
                    ((TileEntityRBMKRod) originTE).receiveFlux(this);
                    return;
                } else if(type == RBMKType.ABSORBER) {
                    if(absorberEfficiency == 1) return;
                    this.fluxQuantity *= absorberEfficiency;
                }
            }

            // End of stream logic (GitHub issue #1933 fix)
            int dx = (int) Math.floor(0.5 + vector.x * fluxRange);
            int dz = (int) Math.floor(0.5 + vector.z * fluxRange);
            targetPos.setPos(originPos.getX() + dx, originPos.getY(), originPos.getZ() + dz);

            NeutronNode lastNode = streamWorld.getNode(targetPos.toLong());
            if(lastNode == null) {
                irradiateFromFlux(worldObj, targetPos);
                return;
            }

            if(((RBMKNeutronNode)lastNode).rbmkType == RBMKType.CONTROL_ROD) {
                TileEntityRBMKControl rod = (TileEntityRBMKControl) lastNode.tile;
                if(rod.getMult() > 0.0D) {
                    this.fluxQuantity *= rod.getMult();
                    targetPos.setPos(targetPos.getX() + vector.x, targetPos.getY(), targetPos.getZ() + vector.z);

                    if(streamWorld.getNode(targetPos.toLong()) == null) {
                        TileEntity te = worldObj.getTileEntity(targetPos);
                        if (te instanceof TileEntityRBMKBase) {
                            RBMKNeutronNode nodeAfter = makeNode(streamWorld, (TileEntityRBMKBase) te);
                            streamWorld.addNode(nodeAfter);
                        } else {
                            irradiateFromFlux(worldObj, targetPos);
                        }
                    }
                }
            }
        }

        public int getHits(World world, BlockPos pos) {
            int hits = 0;
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos(pos);
            for(int h = 0; h < columnHeight; h++) {
                mut.setY(pos.getY() + h);
                if(world.getBlockState(mut).isOpaqueCube()) hits++;
            }
            return hits;
        }

        public void irradiateFromFlux(World world, BlockPos pos) {
            ChunkRadiationManager.proxy.incrementRad(world, pos, (float) (fluxQuantity * 0.05F * (1 - (double) getHits(world, pos) / columnHeight)));
        }

        public void irradiateFromFlux(World world, BlockPos pos, int hits) {
            ChunkRadiationManager.proxy.incrementRad(world, pos, (float) (fluxQuantity * 0.05F * (1 - (double) hits / columnHeight)));
        }

        public void moderateStream() {
            fluxRatio *= (1 - moderatorEfficiency);
        }

    }
}
