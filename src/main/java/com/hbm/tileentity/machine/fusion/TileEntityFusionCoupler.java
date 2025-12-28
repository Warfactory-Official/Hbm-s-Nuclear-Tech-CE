package com.hbm.tileentity.machine.fusion;

import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.KlystronNetwork;
import com.hbm.uninos.networkproviders.PlasmaNetwork;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
@AutoRegister
public class TileEntityFusionCoupler extends TileEntityLoadedBase implements ITickable, IFusionPowerReceiver {

    protected KlystronNetwork.KlystronNode klystronNode;
    protected PlasmaNetwork.PlasmaNode plasmaNode;

    @Override
    public void update() {

        if(!world.isRemote) {

            ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite();
            ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

            // christ on the cross why didn't i just make a baseclass to shove this crap into
            if(klystronNode == null || klystronNode.expired) {
                klystronNode = UniNodespace.getNode(world, pos.add(rot.offsetX, 2, rot.offsetZ), KlystronNetwork.THE_PROVIDER);

                if(klystronNode == null) {
                    klystronNode = (KlystronNetwork.KlystronNode) new KlystronNetwork.KlystronNode(KlystronNetwork.THE_PROVIDER,
                            new BlockPos(pos.getX() + rot.offsetX, pos.getY() + 2, pos.getZ() + rot.offsetZ))
                            .setConnections(new DirPos(pos.getX() + rot.offsetX * 2, pos.getY() + 2, pos.getZ() + rot.offsetZ * 2, rot));

                    UniNodespace.createNode(world, klystronNode);
                }
            }

            if(plasmaNode == null || plasmaNode.expired) {
                plasmaNode = UniNodespace.getNode(world, pos.add(-rot.offsetX, 2, -rot.offsetZ), PlasmaNetwork.THE_PROVIDER);

                if(plasmaNode == null) {
                    plasmaNode = (PlasmaNetwork.PlasmaNode) new PlasmaNetwork.PlasmaNode(PlasmaNetwork.THE_PROVIDER,
                            new BlockPos(pos.getX() - rot.offsetX, pos.getY() + 2, pos.getZ() - rot.offsetZ))
                            .setConnections(new DirPos(pos.getX() - rot.offsetX * 2, pos.getY() + 2, pos.getZ() - rot.offsetZ * 2, rot.getOpposite()));

                    UniNodespace.createNode(world, plasmaNode);
                }
            }

            if(klystronNode.net != null) klystronNode.net.addProvider(this);
            if(plasmaNode.net != null) plasmaNode.net.addReceiver(this);
        }
    }

    @Override public boolean receivesFusionPower() { return true; }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower) {

        // more copy pasted crap code ! ! !
        if(klystronNode != null && klystronNode.net != null) {
            KlystronNetwork net = klystronNode.net;

            for(Map.Entry <TileEntity, Long> o : net.receiverEntries.entrySet()) {
                if(o.getKey() instanceof TileEntityFusionTorus torus) {
                    if(torus.isLoaded() && !torus.isInvalid()) {
                        torus.klystronEnergy += fusionPower;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if(!world.isRemote) {
            if(this.klystronNode != null) UniNodespace.destroyNode(world, klystronNode);
            if(this.plasmaNode != null) UniNodespace.destroyNode(world, klystronNode);
        }
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 4,
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
}
