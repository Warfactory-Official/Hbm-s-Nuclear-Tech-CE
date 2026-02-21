package com.hbm.tileentity.network;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.FluidCounterValve;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.uninos.UniNodespace;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityFluidCounterValve extends TileEntityPipeBaseNT implements ITickable, SimpleComponent, CompatHandler.OCComponent {
    private long counter;

    @Override
    public void update() {
        super.update();

        if(!world.isRemote) {
            if(node != null && node.net != null && getType() != Fluids.NONE) {
                counter += node.net.fluidTracker;
            }

            networkPackNT(25);
        }
    }

    @Override
    public boolean shouldCreateNode() {
        return this.getBlockMetadata() == 1;
    }

    public void updateState() {
        this.blockMetadata = -1; // delete cache

        if(this.getBlockMetadata() == 0 && this.node != null) {
            UniNodespace.destroyNode(world, node);
            this.node = null;
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        counter = nbt.getLong("counter");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("counter", counter);
        return super.writeToNBT(nbt);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeLong(counter);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.counter = Math.max(buf.readLong(), 0);
    }

    private void setState(int state) {
        world.setBlockState(pos, ModBlocks.fluid_counter_valve.getDefaultState().withProperty(FluidCounterValve.META, state), 2);
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.reactorStart, SoundCategory.BLOCKS, 1.0F, 1.0F);
        updateState();
    }

    public long getCounter() {
        return counter;
    }

    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_fluid_counter_valve";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getFluid(Context context, Arguments args) {
        return new Object[] {getType().getName()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getCounter(Context context, Arguments args) {
        return new Object[] {counter};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] resetCounter(Context context, Arguments args) {
        counter = 0;
        markDirty();
        return new Object[] {};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getState(Context context, Arguments args) {
        return new Object[] {getBlockMetadata() == 1 ? 1 : 0};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] setState(Context context, Arguments args) {
        final int state = args.checkInteger(0);
        if(state != 0 && state != 1) {
            throw new IllegalArgumentException();
        }
        setState(state);
        return new Object[] {};
    }
}
