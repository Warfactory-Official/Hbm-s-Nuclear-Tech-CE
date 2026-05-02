package com.hbm.entity.particle;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockCloudResidue;
import com.hbm.blocks.machine.PinkCloudBroadcaster;
import com.hbm.blocks.machine.RadioRec;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@AutoRegister(name = "entity_mod_fx_shadow", trackingRange = 0, sendVelocityUpdates = false)
public class EntityModFXShadow extends Entity {

    public enum Type {
        CHLORINE,
        CLOUD,
        ORANGE,
        PINK_CLOUD
    }

    private Type type = Type.CLOUD;
    private int particleAge;
    private int particleMaxAge = 1200;

    public EntityModFXShadow(World world) {
        super(world);
        setSize(0.2F, 0.2F);
        noClip = true;
        isImmuneToFire = true;
    }

    public static void spawn(World world, Type type, double x, double y, double z, double mx, double my, double mz) {
        if (world.isRemote) return;
        EntityModFXShadow e = new EntityModFXShadow(world);
        e.type = type;
        e.setPosition(x, y, z);
        e.motionX = mx;
        e.motionY = my;
        e.motionZ = mz;
        e.particleMaxAge = switch (type) {
            case CHLORINE -> 700 + world.rand.nextInt(101);
            case CLOUD, PINK_CLOUD, ORANGE -> 900 + world.rand.nextInt(301);
        };
        world.spawnEntity(e);
    }

    @Override
    protected void entityInit() {}

    @Override
    public void onUpdate() {
        if (world.isRemote) {
            setDead();
            return;
        }

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        particleAge++;
        if (particleAge >= particleMaxAge) {
            setDead();
            return;
        }

        if (type == Type.ORANGE) {
            motionX *= 0.86D;
            motionY *= 0.86D;
            motionZ *= 0.86D;
            motionY -= 0.1D;
        } else {
            if (world.isRaining() && world.canBlockSeeSky(new BlockPos(posX, posY, posZ))) {
                motionY -= 0.01D;
            }
            motionX *= 0.76D;
            motionY *= 0.76D;
            motionZ *= 0.76D;
        }

        if (rand.nextInt(50) == 0) {
            int ix = (int) posX;
            int iy = (int) posY;
            int iz = (int) posZ;
            switch (type) {
                case CHLORINE, ORANGE -> ExplosionChaos.poison(world, ix, iy, iz, 2);
                case CLOUD -> ExplosionChaos.c(world, ix, iy, iz, 2);
                case PINK_CLOUD -> ExplosionChaos.pc(world, ix, iy, iz, 2);
            }
        }

        double subdivisions = 4;
        for (int i = 0; i < subdivisions; i++) {
            posX += motionX / subdivisions;
            posY += motionY / subdivisions;
            posZ += motionZ / subdivisions;

            BlockPos pos = new BlockPos((int) posX, (int) posY, (int) posZ);

            if (type == Type.PINK_CLOUD) {
                IBlockState st = world.getBlockState(pos);
                if (st.getBlock() == ModBlocks.radiorec) {
                    EnumFacing facing = st.getValue(RadioRec.FACING);
                    world.setBlockState(pos, ModBlocks.broadcaster_pc.getDefaultState().withProperty(PinkCloudBroadcaster.FACING, facing), 2);
                    setDead();
                    return;
                }
            }

            if (type == Type.ORANGE) {
                if (world.getBlockState(pos).getMaterial() != Material.AIR) {
                    for (int a = -1; a < 2; a++) {
                        for (int b = -1; b < 2; b++) {
                            for (int c = -1; c < 2; c++) {
                                ExplosionNukeGeneric.solinium(world, pos.add(a, b, c));
                            }
                        }
                    }
                    setDead();
                    return;
                }
            } else {
                if (world.getBlockState(pos).isNormalCube()) {
                    boolean expire = rand.nextInt(5) != 0;
                    if (expire) {
                        if (type == Type.CLOUD) {
                            BlockPos prev = new BlockPos((int) prevPosX, (int) prevPosY, (int) prevPosZ);
                            if (BlockCloudResidue.hasPosNeightbour(world, prev)) {
                                IBlockState pst = world.getBlockState(prev);
                                if (pst.getBlock().isReplaceable(world, prev)) {
                                    if (world.rand.nextInt(5) != 0) {
                                        world.setBlockState(prev, ModBlocks.residue.getDefaultState());
                                    }
                                }
                            }
                        }
                        setDead();
                        return;
                    }
                    posX -= motionX / subdivisions;
                    posY -= motionY / subdivisions;
                    posZ -= motionZ / subdivisions;
                    motionX = 0;
                    motionY = 0;
                    motionZ = 0;
                }
            }
        }

        setPosition(posX, posY, posZ);
    }

    @Override
    public boolean writeToNBTOptional(NBTTagCompound compound) {
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {}
}
