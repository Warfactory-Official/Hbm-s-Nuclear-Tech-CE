package com.hbm.entity.logic;

import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.projectile.EntityBombletZeta;
import com.hbm.entity.projectile.EntityBoxcar;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.interfaces.NotableComments;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotableComments
@AutoRegister(name = "entity_bomber", trackingRange = 1000)
public class EntityBomber extends EntityPlaneBase implements IConstantRenderer {

    public static final DataParameter<Byte> STYLE = EntityDataManager.createKey(EntityBomber.class, DataSerializers.BYTE);
    @SideOnly(Side.CLIENT)
    protected AudioWrapper audio;
    /* This was probably the dumbest fucking way that I could have handled this. Not gonna change it now, be glad I made a superclass at all. */ int bombStart = 75;
    int bombStop = 125;
    int bombRate = 3;
    int type = 0;

    public EntityBomber(World world) {
        super(world);
        this.ignoreFrustumCheck = true;
        this.setSize(8.0F, 4.0F);
    }

    public static EntityBomber statFacCarpet(World world, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(world);
        bomber.timer = 200;
        bomber.bombStart = 50;
        bomber.bombStop = 100;
        bomber.bombRate = 2;
        bomber.fac(world, x, y, z);
        bomber.type = 0;
        return bomber;
    }

    public static EntityBomber statFacNapalm(World world, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(world);
        bomber.timer = 200;
        bomber.bombStart = 50;
        bomber.bombStop = 100;
        bomber.bombRate = 5;
        bomber.fac(world, x, y, z);
        bomber.type = 1;
        return bomber;
    }

    public static EntityBomber statFacChlorine(World world, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(world);
        bomber.timer = 200;
        bomber.bombStart = 50;
        bomber.bombStop = 100;
        bomber.bombRate = 4;
        bomber.fac(world, x, y, z);
        bomber.type = 2;
        return bomber;
    }

    public static EntityBomber statFacOrange(World world, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(world);
        bomber.timer = 200;
        bomber.bombStart = 75;
        bomber.bombStop = 125;
        bomber.bombRate = 1;
        bomber.fac(world, x, y, z);
        bomber.type = 3;
        return bomber;
    }

    public static EntityBomber statFacABomb(World world, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(world);
        bomber.timer = 200;
        bomber.bombStart = 60;
        bomber.bombStop = 70;
        bomber.bombRate = 65;
        bomber.fac(world, x, y, z);

        int i = 1;
        int rand = world.rand.nextInt(3);

        switch (rand) {
            case 0:
                i = 5;
                break;
            case 1:
                i = 6;
                break;
            case 2:
                i = 7;
                break;
        }

        if (world.rand.nextInt(100) == 0) {
            i = 8;
        }

        bomber.getDataManager().set(STYLE, (byte) i);
        bomber.type = 4;
        return bomber;
    }

    public static EntityBomber statFacStinger(World world, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(world);
        bomber.timer = 200;
        bomber.bombStart = 50;
        bomber.bombStop = 150;
        bomber.bombRate = 10;
        bomber.fac(world, x, y, z);
        bomber.getDataManager().set(STYLE, (byte) 4);
        bomber.type = 5;
        return bomber;
    }

    public static EntityBomber statFacBoxcar(World world, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(world);
        bomber.timer = 200;
        bomber.bombStart = 50;
        bomber.bombStop = 150;
        bomber.bombRate = 10;
        bomber.fac(world, x, y, z);
        bomber.getDataManager().set(STYLE, (byte) 6);
        bomber.type = 6;
        return bomber;
    }

    public static EntityBomber statFacPC(World world, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(world);
        bomber.timer = 200;
        bomber.bombStart = 75;
        bomber.bombStop = 125;
        bomber.bombRate = 1;
        bomber.fac(world, x, y, z);
        bomber.getDataManager().set(STYLE, (byte) 6);
        bomber.type = 7;
        return bomber;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(STYLE, (byte) 0);
    }

    /**
     * This sucks balls. Too bad!
     */
    @Override
    public void onUpdate() {
        // Preserve the 1.12 war-dimension gate: if shot down outside war dim, kill immediately.
        if (!this.world.isRemote && this.health <= 0.0F && !CompatibilityConfig.isWarDim(this.world)) {
            this.setDead();
            return;
        }

        super.onUpdate();

        if (this.world.isRemote) {
            if (this.getDataManager().get(HEALTH) > 0.0F) {
                if (audio == null || !audio.isPlaying()) {
                    int bomberType = this.getDataManager().get(STYLE);
                    audio = MainRegistry.proxy.getLoopedSound(bomberType <= 4 ? HBMSoundHandler.bomberSmallLoop : HBMSoundHandler.bomberLoop, SoundCategory.HOSTILE, (float) posX, (float) posY, (float) posZ, 2F, 250F, 1F, 20);
                    audio.startSound();
                }
                audio.keepAlive();
                audio.updatePosition((float) posX, (float) posY, (float) posZ);
            } else {
                if (audio != null && audio.isPlaying()) {
                    audio.stopSound();
                    audio = null;
                }
            }
        }

        if (!world.isRemote && this.health > 0.0F && this.ticksExisted > bombStart && this.ticksExisted < bombStop && this.ticksExisted % bombRate == 0) {

            if (!CompatibilityConfig.isWarDim(world)) {
                return;
            }

            if (type == 3) {
                world.playSound(null, posX + 0.5D, posY + 0.5D, posZ + 0.5D, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 5.0F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F);
                ExplosionChaos.spawnChlorine(world, this.posX, this.posY - 1F, this.posZ, 10, 0.5, 3);

            } else if (type == 5) {

            } else if (type == 6) {
                world.playSound(null, posX + 0.5D, posY + 0.5D, posZ + 0.5D, HBMSoundHandler.missileTakeoff, SoundCategory.HOSTILE, 10.0F, 0.9F + rand.nextFloat() * 0.2F);

                EntityBoxcar rocket = new EntityBoxcar(world);
                rocket.posX = posX + rand.nextDouble() - 0.5;
                rocket.posY = posY - rand.nextDouble();
                rocket.posZ = posZ + rand.nextDouble() - 0.5;
                world.spawnEntity(rocket);

            } else if (type == 7) {
                world.playSound(null, posX + 0.5D, posY + 0.5D, posZ + 0.5D, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 5.0F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F);

                ExplosionChaos.spawnChlorine(world, this.posX, world.getHeight((int) this.posX, (int) this.posZ) + 2, this.posZ, 10, 1, 2);

            } else {
                world.playSound(null, posX + 0.5D, posY + 0.5D, posZ + 0.5D, HBMSoundHandler.bombWhistle, SoundCategory.HOSTILE, 10.0F, 0.9F + rand.nextFloat() * 0.2F);

                EntityBombletZeta zeta = new EntityBombletZeta(world);
                zeta.rotation();
                zeta.type = type;

                zeta.posX = posX + rand.nextDouble() - 0.5;
                zeta.posY = posY - rand.nextDouble();
                zeta.posZ = posZ + rand.nextDouble() - 0.5;

                if (type == 0) {
                    zeta.motionX = motionX + rand.nextGaussian() * 0.15;
                    zeta.motionZ = motionZ + rand.nextGaussian() * 0.15;
                } else {
                    zeta.motionX = motionX;
                    zeta.motionZ = motionZ;
                }

                world.spawnEntity(zeta);
            }
        }
    }

    @Override
    public void setDead() {
        super.setDead();

        if (world != null && world.isRemote) {
            if (audio != null && audio.isPlaying()) {
                audio.stopSound();
            }
            audio = null;
        }
    }

    public void fac(World world, double x, double y, double z) {
        Vec3d vector = new Vec3d(world.rand.nextDouble() - 0.5, 0, world.rand.nextDouble() - 0.5).normalize();
        double scale = GeneralConfig.enableBomberShortMode ? 1.0D : 2.0D;
        vector = new Vec3d(vector.x * scale, 0.0D, vector.z * scale);

        this.setLocationAndAngles(x - vector.x * 100, y + 50, z - vector.z * 100, 0.0F, 0.0F);
        this.loadNeighboringChunks((int) (x / 16), (int) (z / 16));

        this.motionX = vector.x;
        this.motionZ = vector.z;
        this.motionY = 0.0D;

        this.rotation();

        int i = 1;
        int rand = world.rand.nextInt(7);

        switch (rand) {
            case 0:
            case 1:
                i = 1;
                break;
            case 2:
            case 3:
                i = 2;
                break;
            case 4:
                i = 5;
                break;
            case 5:
                i = 6;
                break;
            case 6:
                i = 7;
                break;
        }

        if (world.rand.nextInt(100) == 0) {
            rand = world.rand.nextInt(4);
            switch (rand) {
                case 0:
                    i = 0;
                    break;
                case 1:
                    i = 3;
                    break;
                case 2:
                    i = 4;
                    break;
                case 3:
                    i = 8;
                    break;
            }
        }

        this.getDataManager().set(STYLE, (byte) i);
        this.setSize(8.0F, 4.0F);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        bombStart = nbt.getInteger("bombStart");
        bombStop = nbt.getInteger("bombStop");
        bombRate = nbt.getInteger("bombRate");
        type = nbt.getInteger("type");
        this.getDataManager().set(STYLE, nbt.getByte("style"));
        this.setSize(8.0F, 4.0F);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("bombStart", bombStart);
        nbt.setInteger("bombStop", bombStop);
        nbt.setInteger("bombRate", bombRate);
        nbt.setInteger("type", type);
        nbt.setByte("style", this.getDataManager().get(STYLE));
    }
}
