package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTraitSimple;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.tileentity.IConnectionAnchors;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@AutoRegister
public class TileEntityMachineDrain extends TileEntityLoadedBase implements IFluidStandardReceiver, IBufPacketReceiver, IFluidCopiable, ITickable, IConnectionAnchors {

    public FluidTankNTM tank;
    AxisAlignedBB bb = null;

    public TileEntityMachineDrain() {
        this.tank = new FluidTankNTM(Fluids.NONE, 2_000).withOwner(this);
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            if (world.getTotalWorldTime() % 20 == 0) {
                for (DirPos pos : getConPos())
                    this.trySubscribe(tank.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }


            networkPackNT(50);

            if (tank.getFill() > 0) {
                if (tank.getTankType().hasTrait(FluidTraitSimple.FT_Amat.class)) {
                    world.newExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10F, true, true);
                    return;
                }
                int toSpill = Math.max(tank.getFill() / 2, 1);
                tank.setFill(tank.getFill() - toSpill);
                FT_Polluting.pollute(world, pos.getX(), pos.getY(), pos.getZ(), tank.getTankType(), FluidTrait.FluidReleaseType.SPILL, toSpill);

                if (toSpill >= 100 && world.rand.nextInt(20) == 0 && tank.getTankType().hasTrait(FluidTraitSimple.FT_Liquid.class) && tank.getTankType().hasTrait(FluidTraitSimple.FT_Viscous.class) && tank.getTankType().hasTrait(FT_Flammable.class)) {
                    ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
                    Vec3d start = new Vec3d(pos.getX() + 0.5 - dir.offsetX * 3, pos.getY() + 0.5, pos.getZ() + 0.5 - dir.offsetZ * 3);
                    Vec3d end = start.add(world.rand.nextGaussian() * 5, -25, world.rand.nextGaussian() * 5);
                    RayTraceResult mop = world.rayTraceBlocks(start, end, false, true, false);

                    if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK && mop.sideHit == EnumFacing.UP) {
                        BlockPos blockPos = mop.getBlockPos().up();
                        Block block = world.getBlockState(blockPos).getBlock();
                        if (!block.getMaterial(block.getDefaultState()).isLiquid() && block.isReplaceable(world, blockPos) && ModBlocks.oil_spill.canPlaceBlockAt(world, blockPos)) {
                            world.setBlockState(blockPos, ModBlocks.oil_spill.getDefaultState());
                        }
                    }
                }
            }

        } else {

            if (tank.getFill() > 0 && MainRegistry.proxy.me().getDistance(pos.getX(), pos.getY(), pos.getZ()) < 100) {
                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);

                NBTTagCompound data = new NBTTagCompound();
                HbmEffectNT effect;
                if (tank.getTankType().hasTrait(FluidTraitSimple.FT_Gaseous.class)) {
                    effect = HbmEffectNT.Tower;
                    data.setFloat("lift", 0.5F);
                    data.setFloat("base", 0.375F);
                    data.setFloat("max", 3F);
                    data.setInteger("life", 100 + world.rand.nextInt(50));
                } else {
                    effect = HbmEffectNT.Splash;
                }

                data.setInteger("color", tank.getTankType().getColor());

                MainRegistry.proxy.effectNT(effect, pos.getX() + 0.5 - dir.offsetX * 2.5, pos.getY() + 0.5, pos.getZ() + 0.5 - dir.offsetZ * 2.5, data);
            }
        }
    }

    public DirPos[] getConPos() {
        ForgeDirection dir0 = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection dir1 = dir0.getRotation(ForgeDirection.UP);
        ForgeDirection dir2 = dir0.getRotation(ForgeDirection.DOWN);
        return new DirPos[]{
                new DirPos(pos.getX() + dir0.offsetX, pos.getY(), pos.getZ() + dir0.offsetZ, dir0),
                new DirPos(pos.getX() + dir1.offsetX, pos.getY(), pos.getZ() + dir1.offsetZ, dir1),
                new DirPos(pos.getX() + dir2.offsetX, pos.getY(), pos.getZ() + dir2.offsetZ, dir2)
        };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.tank.readFromNBT(nbt, "t");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.tank.writeToNBT(nbt, "t");
        return super.writeToNBT(nbt);
    }

    @Override
    public void serialize(ByteBuf buf) {
        tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        tank.deserialize(buf);
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != ForgeDirection.UP && dir != ForgeDirection.DOWN;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 2,
                    pos.getY(),
                    pos.getZ() - 2,
                    pos.getX() + 3,
                    pos.getY() + 1,
                    pos.getZ() + 3
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
        return tank;
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
