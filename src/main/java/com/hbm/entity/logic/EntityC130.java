package com.hbm.entity.logic;

import com.hbm.entity.item.EntityParachuteCrate;
import com.hbm.interfaces.AutoRegister;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsC130;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.EnumUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
@AutoRegister(name = "entity_c130", trackingRange = 1000)
public class EntityC130 extends EntityPlaneBase {

    protected AudioWrapper audio;
    public C130PayloadType payload = C130PayloadType.SUPPLIES;

    public EntityC130(World world) {
        super(world);
        this.ignoreFrustumCheck = true;
        this.setSize(8.0F, 4.0F);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if(world.isRemote) {
            if(this.getDataManager().get(HEALTH) > 0) {
                if(audio == null || !audio.isPlaying()) {
                    audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.bomberLoop, SoundCategory.NEUTRAL, (float) posX, (float) posY, (float) posZ, 2F, 250F, 1F, 20);
                    audio.startSound();
                }
                audio.keepAlive();
                audio.updatePosition((float) posX, (float) posY, (float) posZ);
            } else {
                if(audio != null && audio.isPlaying()) {
                    audio.stopSound();
                    audio = null;
                }
            }
        }

        if(!world.isRemote && this.ticksExisted == this.getLifetime() / 2 && this.health > 0) {
            EntityParachuteCrate crate = new EntityParachuteCrate(world);
            crate.setPosition(posX - motionX * 7, posY - 10, posZ - motionZ * 7);

            if(this.payload == C130PayloadType.SUPPLIES) {
                for(int i = 0; i < 5; i++) crate.items.add(ItemPool.getStack(ItemPoolsC130.POOL_SUPPLIES, this.rand));
            }
            if(this.payload == C130PayloadType.WEAPONS) {
                int amount = 1 + rand.nextInt(2);
                for(int i = 0; i < amount; i++) crate.items.add(ItemPool.getStack(ItemPoolsC130.POOL_WEAPONS, this.rand));
                for(int i = 0; i < 6; i++) crate.items.add(ItemPool.getStack(ItemPoolsC130.POOL_AMMO, this.rand));
            }

            world.spawnEntity(crate);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.payload = EnumUtil.grabEnumSafely(C130PayloadType.VALUES, nbt.getInteger("payload"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("payload", this.payload.ordinal());
    }

    public void fac(World world, double x, double y, double z, C130PayloadType payload) {

        Vec3d vector = new Vec3d(world.rand.nextDouble() - 0.5, 0, world.rand.nextDouble() - 0.5);
        vector = vector.normalize();
        vector.scale(2);

        this.payload = payload;

        this.setLocationAndAngles(x - vector.x * 100, y + 100, z - vector.z * 100, 0.0F, 0.0F);
        this.loadNeighboringChunks((int) (x / 16), (int) (z / 16));

        this.motionX = vector.x;
        this.motionZ = vector.z;
        this.motionY = 0.0D;

        this.rotation();
    }

    public enum C130PayloadType {
        SUPPLIES,
        WEAPONS,
        A_FUCKING_FUEL_TRUCK;

        public static final C130PayloadType[] VALUES = values();
    }
}
