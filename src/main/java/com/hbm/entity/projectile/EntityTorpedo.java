package com.hbm.entity.projectile;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.ExplosionCreator;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister(name = "entity_torpedo", trackingRange = 1000)
public class EntityTorpedo extends EntityThrowable {
    public EntityTorpedo(World world) {
        super(world);
        this.ignoreFrustumCheck = true;
        this.isImmuneToFire = true;
    }

    @Override
    public void onUpdate() {

        if(!world.isRemote && this.ticksExisted == 1) {
            for(int i = 0; i < 15; i++) {
                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "bf");
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data,
                                posX + (rand.nextDouble() - 0.5) * 2,
                                posY + (rand.nextDouble() - 0.5) * 1,
                                posZ + (rand.nextDouble() - 0.5) * 2),
                        new NetworkRegistry.TargetPoint(dimension, posX, posY, posZ, 150));
            }
        }

        this.lastTickPosX = this.prevPosX = posX;
        this.lastTickPosY = this.prevPosY = posY;
        this.lastTickPosZ = this.prevPosZ = posZ;

        this.setPosition(posX + this.motionX, posY + this.motionY, posZ + this.motionZ);

        this.motionY -= 0.04;
        if(motionY < -2.5) motionY = -2.5;

        if(this.world.getBlockState(this.getPosition()) != Blocks.AIR.getDefaultState()) {
            if(!world.isRemote) {
                this.setDead();
                ExplosionCreator.composeEffectStandard(world, posX, posY + 1, posZ);
                ExplosionVNT vnt = new ExplosionVNT(world, posX, posY, posZ, 20F);
                vnt.makeStandard();
                vnt.explode();
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult p_70184_1_) { }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 25000;
    }
}
