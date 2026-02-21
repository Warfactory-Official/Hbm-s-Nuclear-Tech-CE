package com.hbm.tileentity.network;

import com.hbm.api.fluidmk2.FluidNode;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.uninos.UniNodespace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// copy pasted crap class
public abstract class TileEntityPipelineBase extends TileEntityPipeBaseNT {

    protected List<int[]> connected = new ArrayList<>();

    @Override
    public FluidNode createNode(FluidType type) {
        FluidNode node = new FluidNode(type.getNetworkProvider(), new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ())).setConnections(new DirPos(pos.getX(), pos.getY(), pos.getZ(), ForgeDirection.UNKNOWN));
        for(int[] pos : this.connected) node.addConnection(new DirPos(pos[0], pos[1], pos[2], ForgeDirection.UNKNOWN));
        return node;
    }

    public void addConnection(int x, int y, int z) {

        connected.add(new int[] {x, y, z});

        FluidNode node = UniNodespace.getNode(world, pos, this.type.getNetworkProvider());
        node.recentlyChanged = true;
        node.addConnection(new DirPos(x, y, z, ForgeDirection.UNKNOWN));

        this.markDirty();

        if (world instanceof WorldServer) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    public void disconnectAll() {

        for(int[] pos : connected) {
            BlockPos idfkPos = new BlockPos(pos[0], pos[1], pos[2]);
            TileEntity te = world.getTileEntity(idfkPos);
            if(te == this) continue;

            if(te instanceof TileEntityPipelineBase pipeline) {
                UniNodespace.destroyNode(world, idfkPos, this.type.getNetworkProvider());

                for(int i = 0; i < pipeline.connected.size(); i++) {
                    int[] conPos = pipeline.connected.get(i);

                    if(conPos[0] == getPos().getX() && conPos[1] == getPos().getY() && conPos[2] == getPos().getZ()) {
                        pipeline.connected.remove(i);
                        i--;
                    }
                }

                pipeline.markDirty();

                if (world instanceof WorldServer) {
                    IBlockState state = world.getBlockState(pipeline.getPos());
                    world.notifyBlockUpdate(pipeline.getPos(), state, state, 3);
                    world.markBlockRangeForRenderUpdate(pipeline.getPos(), pipeline.getPos());
                }
            }
        }

        UniNodespace.destroyNode(world, pos, this.type.getNetworkProvider());
    }

    @Override
    public void invalidate() {
        super.invalidate();
        disconnectAll();
    }

    /**
     * Returns a status code based on the operation.<br>
     * 0: Connected<br>
     * 1: Connections are incompatible<br>
     * 2: Both parties are the same block<br>
     * 3: Connection length exceeds maximum
     * 4: Pipeline fluid types do not match
     */
    public static int canConnect(TileEntityPipelineBase first, TileEntityPipelineBase second) {

        if(first.getConnectionType() != second.getConnectionType()) return 1;
        if(first == second) return 2;
        if(first.type != second.type) return 4;

        double len = Math.min(first.getMaxPipeLength(), second.getMaxPipeLength());

        Vec3d firstPos = first.getConnectionPoint();
        Vec3d secondPos = second.getConnectionPoint();

        Vec3d delta = new Vec3d(
                (secondPos.x) - (firstPos.x),
                (secondPos.y) - (firstPos.y),
                (secondPos.z) - (firstPos.z)
        );

        return len >= delta.length() ? 0 : 3;
    }

    public abstract ConnectionType getConnectionType();
    public abstract Vec3d getMountPos();
    public abstract double getMaxPipeLength();

    public Vec3d getConnectionPoint() {
        Vec3d mount = this.getMountPos();
        return mount.add(pos.getX(), pos.getY(), pos.getZ());
    }

    public List<int[]> getConnected() {
        return connected;
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("conCount", connected.size());

        for(int i = 0; i < connected.size(); i++) {
            nbt.setIntArray("con" + i, connected.get(i));
        }
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        int count = nbt.getInteger("conCount");

        this.connected.clear();

        for(int i = 0; i < count; i++) {
            connected.add(nbt.getIntArray("con" + i));
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new SPacketUpdateTileEntity(pos, 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
        if (world != null) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
        this.lastType = this.type;
    }

    public enum ConnectionType {
        SMALL
    }

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB; // not great!
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
