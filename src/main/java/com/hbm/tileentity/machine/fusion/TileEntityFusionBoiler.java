package com.hbm.tileentity.machine.fusion;

import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PlasmaNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityFusionBoiler extends TileEntityLoadedBase implements ITickable, IFluidStandardTransceiverMK2, IFusionPowerReceiver {

    protected PlasmaNetwork.PlasmaNode plasmaNode;

    public long plasmaEnergy;
    public long plasmaEnergySync;
    public FluidTankNTM[] tanks;

    public TileEntityFusionBoiler() {
        this.tanks = new FluidTankNTM[2];
        this.tanks[0] = new FluidTankNTM(Fluids.WATER, 32_000);
        this.tanks[1] = new FluidTankNTM(Fluids.SUPERHOTSTEAM, 32_000);
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            this.plasmaEnergySync = this.plasmaEnergy;
            this.plasmaEnergy = 0;

            for(DirPos pos : getConPos()) {
                if(tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(tanks[0].getTankType(), world, pos);
                if(tanks[1].getFill() > 0) this.tryProvide(tanks[1], world, pos);
            }

            if(plasmaNode == null || plasmaNode.expired) {
                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite();
                plasmaNode = UniNodespace.getNode(world, pos.add(dir.offsetX * 4, 2, dir.offsetZ * 4), PlasmaNetwork.THE_PROVIDER);

                if(plasmaNode == null) {
                    plasmaNode = (PlasmaNetwork.PlasmaNode) new PlasmaNetwork.PlasmaNode(PlasmaNetwork.THE_PROVIDER,
                            new BlockPos(pos.getX() + dir.offsetX * 4, pos.getY() + 2, pos.getZ() + dir.offsetZ * 4))
                            .setConnections(new DirPos(pos.getX() + dir.offsetX * 5, pos.getY() + 2, pos.getZ() + dir.offsetZ * 5, dir));

                    UniNodespace.createNode(world, plasmaNode);
                }
            }

            if(plasmaNode != null && plasmaNode.hasValidNet()) plasmaNode.net.addReceiver(this);

            this.networkPackNT(50);
        }
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                //new DirPos(pos.getX() + dir.offsetX * 5, pos.getY() + 2, pos.getZ() + dir.offsetZ * 5, dir),
                new DirPos(pos.getX() - dir.offsetX + rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() - dir.offsetX - rot.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ - rot.offsetZ * 2, rot.getOpposite()),
                new DirPos(pos.getX() + dir.offsetX * 2 + rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() + dir.offsetX * 2 - rot.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 2 - rot.offsetZ * 2, rot.getOpposite())
        };
    }

    @Override public boolean receivesFusionPower() { return true; }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower) {
        this.plasmaEnergy = fusionPower;

        int waterCycles = Math.min(tanks[0].getFill(), tanks[1].getMaxFill() - tanks[1].getFill());
        int steamCycles = (int) (Math.min(fusionPower / tanks[0].getTankType().getTrait(FT_Heatable.class).getFirstStep().heatReq, waterCycles));
        // the Math.min call was mushed into the steam cycles call instead of doing it afterwards as usual
        // in order to prevent issues when casting, should the fusion reactor output truly absurd amounts of power
        // due to the water cycles being effectively capped via the buffer size

        if(steamCycles > 0) {
            tanks[0].setFill(tanks[0].getFill() - steamCycles);
            tanks[1].setFill(tanks[1].getFill() + steamCycles);

            if(world.rand.nextInt(200) == 0) {
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, HBMSoundHandler.boilerGroanSounds[world.rand.nextInt(3)], SoundCategory.BLOCKS, 2.5F, 1.0F);
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(plasmaEnergySync);

        this.tanks[0].serialize(buf);
        this.tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.plasmaEnergy = buf.readLong();

        this.tanks[0].deserialize(buf);
        this.tanks[1].deserialize(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.tanks[0].readFromNBT(nbt, "t0");
        this.tanks[1].readFromNBT(nbt, "t1");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.tanks[0].writeToNBT(nbt, "t0");
        this.tanks[1].writeToNBT(nbt, "t1");
        return super.writeToNBT(nbt);
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if(!world.isRemote) {
            if(this.plasmaNode != null) UniNodespace.destroyNode(world, plasmaNode);
        }
    }

    @Override public FluidTankNTM[] getAllTanks() { return tanks; }
    @Override public FluidTankNTM[] getReceivingTanks() { return new FluidTankNTM[] {tanks[0]}; }
    @Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {tanks[1]}; }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 4,
                    pos.getY(),
                    pos.getZ() - 4,
                    pos.getX() + 5,
                    pos.getY() + 4,
                    pos.getZ() + 5
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}

