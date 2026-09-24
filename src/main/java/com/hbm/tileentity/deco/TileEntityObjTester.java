package com.hbm.tileentity.deco;

import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.Library;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PacketSpecialDeath;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityObjTester extends TileEntity implements ITickable {
    public int fireAge = -1;
    private AxisAlignedBB bb;

    @Override
    public void update() {
        RayTraceResult r = Library.rayTraceIncludeEntities(world, new Vec3d(this.pos).add(0, 2, 0.5), new Vec3d(this.pos).add(12, 2, 0.5), null);
        if (world.isRemote) {
            if (fireAge >= 0) {
                fireAge++;
            }
            //MainRegistry.proxy.spawnParticle(pos.getX(), pos.getY(), pos.getZ(), "bfg_fire", new float[]{fireAge}); this is the only thing the old shader manager is still used for
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null)
            bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 13, pos.getY() + 4, pos.getZ() + 2);
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
