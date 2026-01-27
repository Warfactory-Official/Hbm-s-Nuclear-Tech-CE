package com.hbm.explosion;

import com.hbm.config.CompatibilityConfig;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ParticleUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.List;
import java.util.Random;

public class ExplosionLarge {

    static Random rand = new Random();

    public static void spawnParticlesRadial(World world, double x, double y, double z, int count) {

        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "smoke");
        data.setString("mode", "radial");
        data.setInteger("count", count);
        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(world.provider.getDimension(), x, y, z, 250));
    }

    public static void spawnFoam(World world, double x, double y, double z, int count) {

        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "smoke");
        data.setString("mode", "foamSplash");
        data.setInteger("count", count);
        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(world.provider.getDimension(), x, y, z, 250));
    }

    public static void spawnParticles(World world, double x, double y, double z, int count) {
        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "smoke");
        data.setString("mode", "cloud");
        data.setInteger("count", count);
        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(world.provider.getDimension(), x, y, z, 250));
    }

    public static void spawnBurst(World world, double x, double y, double z, int count, double strength) {

        Vec3d vec = new Vec3d(strength, 0, 0);
        vec = vec.rotateYaw(rand.nextInt(360));

        for (int i = 0; i < count; i++) {
            ParticleUtil.spawnGasFlame(world, x, y, z, vec.x, 0.0, vec.z);

            vec = vec.rotateYaw(360 / count);
        }
    }

    public static void spawnShock(World world, double x, double y, double z, int count, double strength) {

        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "smoke");
        data.setString("mode", "shock");
        data.setInteger("count", count);
        data.setDouble("strength", strength);
        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y + 0.5, z), new TargetPoint(world.provider.getDimension(), x, y, z, 250));
    }

    public static void spawnRubble(World world, double x, double y, double z, int count) {

        for (int i = 0; i < count; i++) {
            EntityRubble rubble = new EntityRubble(world);
            rubble.posX = x;
            rubble.posY = y;
            rubble.posZ = z;
            rubble.motionY = 0.75 * (1 + ((count + rand.nextInt(count * 5))) / 25);
            rubble.motionX = rand.nextGaussian() * 0.75 * (1 + (count / 50));
            rubble.motionZ = rand.nextGaussian() * 0.75 * (1 + (count / 50));
            rubble.setMetaBasedOnBlock(Blocks.STONE, 0);
            world.spawnEntity(rubble);
        }
    }

    public static void spawnShrapnels(World world, double x, double y, double z, int count) {

        for (int i = 0; i < count; i++) {
            EntityShrapnel shrapnel = new EntityShrapnel(world);
            shrapnel.posX = x;
            shrapnel.posY = y;
            shrapnel.posZ = z;
            shrapnel.motionY = ((rand.nextFloat() * 0.5) + 0.5) * (1 + (count / (15 + rand.nextInt(21)))) + (rand.nextFloat() / 50 * count);
            shrapnel.motionX = rand.nextGaussian() * 1 * (1 + (count / 50));
            shrapnel.motionZ = rand.nextGaussian() * 1 * (1 + (count / 50));
            shrapnel.setTrail(rand.nextInt(3) == 0);
            world.spawnEntity(shrapnel);
        }
    }

    @SuppressWarnings("deprecation")
    public static void jolt(World world, Entity detonator, double posX, double posY, double posZ, double strength, int count, double vel) {
        if (!CompatibilityConfig.isWarDim(world)) {
            return;
        }
        for (int j = 0; j < count; j++) {

            double phi = rand.nextDouble() * (Math.PI * 2);
            double costheta = rand.nextDouble() * 2 - 1;
            double theta = Math.acos(costheta);
            double x = Math.sin(theta) * Math.cos(phi);
            double y = Math.sin(theta) * Math.sin(phi);
            double z = Math.cos(theta);

            Vec3d vec = new Vec3d(x, y, z);
            MutableBlockPos pos = new BlockPos.MutableBlockPos();

            for (int i = 0; i < strength; i++) {
                double x0 = posX + (vec.x * i);
                double y0 = posY + (vec.y * i);
                double z0 = posZ + (vec.z * i);
                pos.setPos((int) x0, (int) y0, (int) z0);

                if (!world.isRemote) {
                    IBlockState blockstate = world.getBlockState(pos);
                    Block block = blockstate.getBlock();
                    if (blockstate.getMaterial().isLiquid()) {
                        world.setBlockToAir(pos);
                    }

                    if (block != Blocks.AIR) {

                        if (block.getExplosionResistance(null) > 70)
                            continue;

                        EntityRubble rubble = new EntityRubble(world);
                        rubble.posX = x0 + 0.5F;
                        rubble.posY = y0 + 0.5F;
                        rubble.posZ = z0 + 0.5F;
                        rubble.setMetaBasedOnBlock(block, block.getMetaFromState(blockstate));

                        Vec3d vec4 = new Vec3d(posX - rubble.posX, posY - rubble.posY, posZ - rubble.posZ);
                        vec4.normalize();

                        rubble.motionX = vec4.x * vel;
                        rubble.motionY = vec4.y * vel;
                        rubble.motionZ = vec4.z * vel;

                        world.spawnEntity(rubble);

                        world.setBlockToAir(pos);
                        break;
                    }
                }
            }
        }
    }

    public static void spawnTracers(World world, double x, double y, double z, int count) {

        for (int i = 0; i < count; i++) {
            EntityShrapnel shrapnel = new EntityShrapnel(world);
            shrapnel.posX = x;
            shrapnel.posY = y;
            shrapnel.posZ = z;
            shrapnel.motionY = ((rand.nextFloat() * 0.5) + 0.5) * (1 + (count / (15 + rand.nextInt(21)))) + (rand.nextFloat() / 50 * count) * 0.25F;
            shrapnel.motionX = rand.nextGaussian() * 1 * (1 + (count / 50)) * 0.25F;
            shrapnel.motionZ = rand.nextGaussian() * 1 * (1 + (count / 50)) * 0.25F;
            shrapnel.setTrail(true);
            world.spawnEntity(shrapnel);
        }
    }

    public static void spawnShrapnelShower(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int count, double deviation) {

        for (int i = 0; i < count; i++) {
            EntityShrapnel shrapnel = new EntityShrapnel(world);
            shrapnel.posX = x;
            shrapnel.posY = y;
            shrapnel.posZ = z;
            shrapnel.motionX = motionX + rand.nextGaussian() * deviation;
            shrapnel.motionY = motionY + rand.nextGaussian() * deviation;
            shrapnel.motionZ = motionZ + rand.nextGaussian() * deviation;
            shrapnel.setTrail(rand.nextInt(3) == 0);
            world.spawnEntity(shrapnel);
        }
    }

    public static void spawnMissileDebris(World world, double x, double y, double z, double motionX, double motionY, double motionZ, double deviation, List<ItemStack> debris, ItemStack rareDrop) {

        if (debris != null) {
            for (ItemStack itemStack : debris) {
                if (itemStack != null && !itemStack.isEmpty()) {
                    int k = rand.nextInt(itemStack.getCount() + 1);
                    for (int j = 0; j < k; j++) {
                        ItemStack copy = itemStack.copy();
                        copy.setCount(1);
                        //mlbv: 1.7 uses debris.get(i).copy() directly here, I think it's a bug
                        EntityItem item = new EntityItem(world, x, y, z, copy);
                        item.motionX = (motionX + rand.nextGaussian() * deviation) * 0.85;
                        item.motionY = (motionY + rand.nextGaussian() * deviation) * 0.85;
                        item.motionZ = (motionZ + rand.nextGaussian() * deviation) * 0.85;
                        item.posX = item.posX + item.motionX * 2;
                        item.posY = item.posY + item.motionY * 2;
                        item.posZ = item.posZ + item.motionZ * 2;

                        world.spawnEntity(item);
                    }
                }
            }
        }

        if(rareDrop != null && rand.nextInt(10) == 0) {
            EntityItem item = new EntityItem(world, x, y, z, rareDrop.copy());
            item.motionX = motionX + rand.nextGaussian() * deviation * 0.1;
            item.motionY = motionY + rand.nextGaussian() * deviation * 0.1;
            item.motionZ = motionZ + rand.nextGaussian() * deviation * 0.1;
            world.spawnEntity(item);
        }
    }

    public static void explode(World world, Entity detonator, double x, double y, double z, float strength, boolean cloud, boolean rubble, boolean shrapnel) {
        world.createExplosion(detonator, x, y, z, strength, true);
        if (cloud)
            spawnParticles(world, x, y + 2, z, cloudFunction((int) strength));
        if (rubble)
            spawnRubble(world, x, y + 2, z, rubbleFunction((int) strength));
        if (shrapnel)
            spawnShrapnels(world, x, y + 2, z, shrapnelFunction((int) strength));
    }


    public static int cloudFunction(int i) {
        // return (int)(345 * (1 - Math.pow(Math.E, -i/15)) + 15);
        return (int) (545 * (1 - Math.pow(Math.E, -i / 15)) + 15);
    }

    public static int rubbleFunction(int i) {
        return i / 10;
    }

    public static int shrapnelFunction(int i) {
        return i / 3;
    }

    public static void explodeFire(World world, Entity detonator, double x, double y, double z, float strength, boolean cloud, boolean rubble, boolean shrapnel) {
        if (CompatibilityConfig.isWarDim(world)) {
            world.spawnEntity(EntityNukeExplosionMK5.statFacNoRadFire(world, (int) strength, x, y, z).setDetonator(detonator));

            ContaminationUtil.radiate(world, x, y, z, strength, 0, 0, strength * 20F, strength * 5F);
        }
        if (cloud)
            spawnParticles(world, x, y + 2, z, cloudFunction((int) strength));
        if (rubble)
            spawnRubble(world, x, y + 2, z, rubbleFunction((int) strength));
        if (shrapnel)
            spawnShrapnels(world, x, y + 2, z, shrapnelFunction((int) strength));
    }

    public static void buster(World world, Entity detonator, double x, double y, double z, Vec3d vector, float strength, float depth) {

        vector = vector.normalize();
        if (CompatibilityConfig.isWarDim(world)) {
            for (int i = 0; i <= depth; i += 3) {

                ContaminationUtil.radiate(world, x + vector.x * i, y + vector.y * i, z + vector.z * i, strength, 0, 0, 0, strength * 10F);
                world.spawnEntity(EntityNukeExplosionMK5.statFacNoRad(world, (int) strength, x + vector.x * i, y + vector.y * i, z + vector.z * i).setDetonator(detonator));
            }
        }
        spawnParticles(world, x, y + 2, z, cloudFunction((int) strength));
        spawnRubble(world, x, y + 2, z, rubbleFunction((int) strength));
        spawnShrapnels(world, x, y + 2, z, shrapnelFunction((int) strength));
    }
}
