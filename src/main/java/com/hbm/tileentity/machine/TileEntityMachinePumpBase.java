package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashSet;

public abstract class TileEntityMachinePumpBase extends TileEntityLoadedBase implements ITickable, IFluidStandardTransceiver, IConfigurableMachine, IFluidCopiable {

    public static final HashSet<Block> validBlocks = new HashSet();

    static {
        validBlocks.add(Blocks.GRASS);
        validBlocks.add(Blocks.DIRT);
        validBlocks.add(Blocks.SAND);
        validBlocks.add(Blocks.MYCELIUM);
        validBlocks.add(ModBlocks.waste_earth);
        validBlocks.add(ModBlocks.dirt_dead);
        validBlocks.add(ModBlocks.dirt_oily);
        validBlocks.add(ModBlocks.sand_dirty);
        validBlocks.add(ModBlocks.sand_dirty_red);
    }

    public FluidTankNTM water;

    public boolean isOn = false;
    public float rotor;
    public float lastRotor;
    public boolean onGround = false;
    public int groundCheckDelay = 0;

    public static int groundHeight = 70;
    public static int groundDepth = 4;
    public static int steamSpeed = 1_000;
    public static int electricSpeed = 10_000;
    public static int nonWaterDebuff = 100;

    @Override
    public String getConfigName() {
        return "waterpump";
    }

    @Override
    public void readIfPresent(JsonObject obj) {
        groundHeight = IConfigurableMachine.grab(obj, "I:groundHeight", groundHeight);
        groundDepth = IConfigurableMachine.grab(obj, "I:groundDepth", groundDepth);
        steamSpeed = IConfigurableMachine.grab(obj, "I:steamSpeed", steamSpeed);
        electricSpeed = IConfigurableMachine.grab(obj, "I:electricSpeed", electricSpeed);
    }

    @Override
    public void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:groundHeight").value(groundHeight);
        writer.name("I:groundDepth").value(groundDepth);
        writer.name("I:steamSpeed").value(steamSpeed);
        writer.name("I:electricSpeed").value(electricSpeed);
    }
    @Override
    public void update() {
        if(!world.isRemote) {

            for(DirPos pos : getConPos()) {
                if(water.getFill() > 0) this.sendFluid(water, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            if(groundCheckDelay > 0) {
                groundCheckDelay--;
            } else {
                onGround = this.checkGround();
            }

            this.isOn = false;
            if(this.canOperate() && pos.getY() <= groundHeight && onGround) {
                this.isOn = true;
                this.operate();
            }
            
            networkPackNT(150);

        } else {

            this.lastRotor = this.rotor;
            if(this.isOn) this.rotor += 10F;

            if(this.rotor >= 360F) {
                this.rotor -= 360F;
                this.lastRotor -= 360F;

                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.steamEngineOperate, SoundCategory.BLOCKS, 0.5F, 0.75F);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1F, 0.5F);
            }
        }
    }

    protected boolean checkGround() {

        if(!world.provider.hasSkyLight()) return false;

        int validBlocks = 0;
        int invalidBlocks = 0;

        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y >= -groundDepth; y--) {
                for(int z = -1; z <= 1; z++) {
                    IBlockState st = world.getBlockState(new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z));
                    Block b = st.getBlock();

                    if(y == -1 && !b.isNormalCube(st, world, new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z))){
                        return false;
                    } // first layer has to be full solid

                    if(this.validBlocks.contains(b)) validBlocks++;
                    else invalidBlocks ++;
                }
            }
        }

        return validBlocks >= invalidBlocks; // valid block count has to be at least 50%
    }
    
    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(isOn);
        buf.writeBoolean(onGround);
        water.serialize(buf);
    }
    
    @Override
    public void deserialize(ByteBuf buf) {
        this.isOn = buf.readBoolean();
        this.onGround = buf.readBoolean();
        water.deserialize(buf);
    }

    protected abstract boolean canOperate();
    protected abstract void operate();

    protected DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ(), Library.NEG_X),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 2, Library.NEG_Z)
        };
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[] {water};
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[] {water};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[0];
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 5,
                    pos.getZ() + 2
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public FluidTankNTM getTankToPaste() {
        return null;
    }


    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
                    new NTMFluidHandlerWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }
}
