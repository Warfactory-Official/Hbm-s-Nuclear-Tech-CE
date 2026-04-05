package com.hbm.tileentity.machine;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.ContaminationUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@AutoRegister
public class TileEntityZirnoxDestroyed extends TileEntity implements ITickable {

    private AxisAlignedBB bb;
    public boolean onFire = true;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        onFire = nbt.getBoolean("fire");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("onFire", onFire);
        return super.writeToNBT(nbt);
    }

    @Override
    public void update() {
        if(!world.isRemote) {
            radiate(world, this.pos.getX(), this.pos.getY(), this.pos.getZ());

            if(this.world.rand.nextInt(5000) == 0)
                onFire = false;

            if(onFire && this.world.getTotalWorldTime() % 50 == 0) {
                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "rbmkflame");
                data.setInteger("maxAge", 90);
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + 0.25 + world.rand.nextDouble() * 0.5, pos.getY() + 1.75, pos.getZ() + 0.25 + world.rand.nextDouble() * 0.5), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 1.75, pos.getZ() + 0.5, 75));
                MainRegistry.proxy.effectNT(data);
                world.playSound(null, pos.getX() + 0.5F, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + world.rand.nextFloat(), world.rand.nextFloat() * 0.7F + 0.3F);
            }
        }
    }

    private void radiate(World world, int x, int y, int z) {

        float rads = onFire ? 500000F : 75000F;
        double range = 100D;

        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).expand(range, range, range));

        for(EntityLivingBase e : entities) {

            Vec3 vec = Vec3.createVectorHelper(e.posX - (x + 0.5), (e.posY + e.getEyeHeight()) - (y + 0.5), e.posZ - (z + 0.5));
            double len = vec.length();
            vec = vec.normalize();

            float res = 0;

            for(int i = 1; i < len; i++) {

                int ix = (int)Math.floor(x + 0.5 + vec.xCoord * i);
                int iy = (int)Math.floor(y + 0.5 + vec.yCoord * i);
                int iz = (int)Math.floor(z + 0.5 + vec.zCoord * i);

                res += world.getBlockState(new BlockPos(ix, iy, iz)).getBlockHardness( world, pos); //Norwood: getPlayerRelativeBlockHardness crashes the game when player is null, why would you ever do that?
            }

            if(res < 1)
                res = 1;

            float eRads = rads;
            eRads /= (float)res;
            eRads /= (float)(len * len);

            ContaminationUtil.contaminate(e, ContaminationUtil.HazardType.RADIATION, ContaminationUtil.ContaminationType.CREATIVE, eRads);

            if(onFire && len < 5) {
                e.attackEntityFrom(DamageSource.ON_FIRE, 2);
            }
        }
    }

    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) bb = new AxisAlignedBB(pos.getX() - 3, pos.getY(), pos.getZ() - 3, pos.getX() + 4, pos.getY() + 3, pos.getZ() + 4);
        return bb;
    }

    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
