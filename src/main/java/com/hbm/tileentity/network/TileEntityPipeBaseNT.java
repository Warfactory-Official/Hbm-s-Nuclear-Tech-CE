package com.hbm.tileentity.network;

import com.hbm.api.fluidmk2.FluidNode;
import com.hbm.api.fluidmk2.IFluidPipeMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;

@AutoRegister
public class TileEntityPipeBaseNT extends TileEntityLoadedBase implements IFluidPipeMK2, IFluidCopiable, ITickable {

    protected FluidNode node;
    protected FluidType type = Fluids.NONE;
    protected FluidType lastType = Fluids.NONE;

    @Override
    public void update() {
        if(!world.isRemote && canUpdate()) {
            if(this.node == null || this.node.expired) {

                if(this.shouldCreateNode()) {
                    this.node = UniNodespace.getNode(world, pos, type.getNetworkProvider());

                    if(this.node == null || this.node.expired) {
                        this.node = this.createNode(type);
                        UniNodespace.createNode(world, this.node);
                    }
                }
            }
        }
    }

    public boolean shouldCreateNode() {
        return true;
    }

    public FluidType getType() {
        return this.type;
    }

    public void setType(FluidType type) {
        FluidType prev = this.type;
        this.type = type;
        this.markDirty();

        if (world instanceof WorldServer) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }

        if(this.node != null) {
            UniNodespace.destroyNode(world, node);
            this.node = null;
        }
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && type == this.type;
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if(!world.isRemote) {
            if(this.node != null) {
                UniNodespace.destroyNode(world, node);
            }
        }
    }

    /**
     * Only update until a power net is formed, in >99% of the cases it should be the first tick. Everything else is handled by neighbors and the net itself.
     */
    public boolean canUpdate() {
        return (this.node == null || this.node.net == null || !this.node.net.isValid()) && !this.isInvalid();
    }
    // is that redundant? probably
    // do I want visual shit to be gone? yes x10
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

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = super.getUpdateTag();
        writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        readFromNBT(tag);
        if (world != null) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
        this.lastType = this.type;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.type = Fluids.fromID(nbt.getInteger("type"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("type", this.type.getID());
        return nbt;
    }
}
