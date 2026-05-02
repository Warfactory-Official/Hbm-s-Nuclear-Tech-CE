package com.hbm.tileentity.network;

import com.hbm.api.fluidmk2.FluidNode;
import com.hbm.api.fluidmk2.IFluidPipeMK2;
import com.hbm.blocks.network.IBlockFluidDuct;
import com.hbm.capability.HbmCapability;
import com.hbm.handler.HbmKeybinds;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

@AutoRegister
public class TileEntityPipeBaseNT extends TileEntityLoadedBase implements IFluidPipeMK2, IFluidCopiable, ITickable, ICachedPipeConnections {

    protected FluidNode node;
    protected FluidType type = Fluids.NONE;
    protected FluidType lastType = Fluids.NONE;

    private byte cachedConnectionMask;
    private boolean cachedConnectionMaskValid;

    public byte getCachedConnectionMask(IBlockAccess access) {
        if (access instanceof World && ((World) access).isRemote) {
            return computeConnectionMask(access);
        }
        if (!this.cachedConnectionMaskValid) {
            this.cachedConnectionMask = computeConnectionMask(access);
            this.cachedConnectionMaskValid = true;
        }
        return this.cachedConnectionMask;
    }

    public void invalidateConnectionCache() {
        this.cachedConnectionMaskValid = false;
        markConnectionRenderUpdate();
    }

    private void markConnectionRenderUpdate() {
        if (world != null && world.isRemote) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    private byte computeConnectionMask(IBlockAccess access) {
        byte mask = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
            ForgeDirection dir = ForgeDirection.getOrientation(facing);
            BlockPos adj = pos.offset(facing);
            if (access instanceof World && !((World) access).isBlockLoaded(adj)) {
                continue;
            }
            if (Library.canConnectFluid(access, adj, dir, this.type)) {
                mask |= (byte) (1 << facing.getIndex());
            }
        }
        return mask;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world.isRemote) {
            invalidateConnectionCache();
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos neighborPos = pos.offset(facing);
                if (!world.isBlockLoaded(neighborPos)) continue;
                TileEntity te = world.getTileEntity(neighborPos);
                if (te instanceof ICachedPipeConnections cached) {
                    cached.invalidateConnectionCache();
                }
            }
        }
    }

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
        if (this.type == type) return;
        this.type = type;
        invalidateConnectionCache();
        this.markDirty();

        if (world instanceof WorldServer) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            world.markBlockRangeForRenderUpdate(pos, pos);
            IConnectionAnchors.notifyAnchors(this);
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
    @Override
    public void serializeInitial(ByteBuf buf) {
        super.serializeInitial(buf);
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        ByteBufUtils.writeTag(buf, nbt);
    }

    @Override
    public void deserializeInitial(ByteBuf buf) {
        super.deserializeInitial(buf);
        NBTTagCompound nbt = ByteBufUtils.readTag(buf);
        if (nbt != null) readFromNBT(nbt);
        this.lastType = this.type;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.type = Fluids.fromID(nbt.getInteger("type"));
        invalidateConnectionCache();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("type", this.type.getID());
        return nbt;
    }

    @Override
    public int[] getFluidIDToCopy() {
        return new int[]{ type.getID() };
    }

    @Override
    public FluidTankNTM getTankToPaste() {
        return null;
    }

    @Override
    public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
        int[] ids = nbt.getIntArray("fluidID");
        if(ids.length == 0) return;

        FluidType fluid = Fluids.fromID(index < ids.length ? ids[index] : 0);

        if(HbmCapability.getData(player).getKeyPressed(HbmKeybinds.EnumKeybind.TOOL_CTRL)
                && world.getBlockState(pos).getBlock() instanceof IBlockFluidDuct duct) {
            duct.changeTypeRecursively(world, pos, getType(), fluid, 64);
        } else {
            this.setType(fluid);
        }
    }
}
