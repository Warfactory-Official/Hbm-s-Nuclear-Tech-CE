package com.hbm.entity.grenade;

import com.hbm.config.GeneralConfig;
import com.hbm.entity.projectile.EntityThrowableInterp;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.grenade.ItemGrenadeExtra.EnumGrenadeExtra;
import com.hbm.items.weapon.grenade.ItemGrenadeFilling.EnumGrenadeFilling;
import com.hbm.items.weapon.grenade.ItemGrenadeFuze.EnumGrenadeFuze;
import com.hbm.items.weapon.grenade.ItemGrenadeShell.EnumGrenadeShell;
import com.hbm.items.weapon.grenade.ItemGrenadeUniversal;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.util.TrackerUtil;
import com.hbm.util.Vec3NT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

@AutoRegister(name = "entity_grenade_universal", trackingRange = 64)
public class EntityGrenadeUniversal extends EntityThrowableInterp {

    public static final int TRAIL_NONE = 0;
    public static final int TRAIL_TRIPLET = 1;

    private static final DataParameter<ItemStack> DW_GRENADE =
            EntityDataManager.createKey(EntityGrenadeUniversal.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Integer> DW_BOUNCES =
            EntityDataManager.createKey(EntityGrenadeUniversal.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DW_TRAIL =
            EntityDataManager.createKey(EntityGrenadeUniversal.class, DataSerializers.VARINT);

    public double prevSpin;
    public double spin;

    public EntityGrenadeUniversal(World world) {
        super(world);
    }

    public EntityGrenadeUniversal(World world, ItemStack grenade) {
        super(world);
        ItemStack copy = grenade.copy();
        copy.setCount(1);
        this.dataManager.set(DW_GRENADE, copy);
    }

    public EntityGrenadeUniversal(World world, EntityPlayer thrower, ItemStack grenade, EnumHand hand) {
        super(world);
        this.thrower = thrower;
        ItemStack copy = grenade.copy();
        copy.setCount(1);
        this.dataManager.set(DW_GRENADE, copy);

        double offsetLateral = hand == EnumHand.OFF_HAND ? -0.25 : 0.25;
        Vec3NT offset = new Vec3NT(offsetLateral, -0.25, 0).rotateAroundYDeg(-thrower.rotationYaw + 180);

        this.setLocationAndAngles(
                thrower.posX + offset.x,
                thrower.posY + thrower.getEyeHeight() + offset.y,
                thrower.posZ + offset.z,
                thrower.rotationYaw,
                thrower.rotationPitch);

        EnumGrenadeShell shell = ItemGrenadeUniversal.getShell(grenade);
        Vec3NT yeet = new Vec3NT(thrower.getLookVec()).normalizeSelf();
        this.shoot(yeet.x, yeet.y, yeet.z, (float) shell.getYeetForce(), 0);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DW_GRENADE, ItemStack.EMPTY);
        this.dataManager.register(DW_BOUNCES, 0);
        this.dataManager.register(DW_TRAIL, 0);
    }

    public EntityGrenadeUniversal setTrail(int trail) {
        this.dataManager.set(DW_TRAIL, trail);
        return this;
    }

    public ItemStack getGrenadeItem() { return this.dataManager.get(DW_GRENADE); }
    public int getBounces() { return this.dataManager.get(DW_BOUNCES); }
    public int getTrail() { return this.dataManager.get(DW_TRAIL); }

    public EnumGrenadeShell getShell() { return ItemGrenadeUniversal.getShell(getGrenadeItem()); }
    public EnumGrenadeFilling getFilling() { return ItemGrenadeUniversal.getFilling(getGrenadeItem()); }
    public EnumGrenadeFuze getFuze() { return ItemGrenadeUniversal.getFuze(getGrenadeItem()); }
    public EnumGrenadeExtra getExtra() { return ItemGrenadeUniversal.getExtra(getGrenadeItem()); }

    @Override
    public void onUpdate() {
        super.onUpdate();

        EnumGrenadeFuze fuze = this.getFuze();
        EnumGrenadeExtra extra = this.getExtra();

        if (fuze != null && fuze.updateTick != null) fuze.updateTick.accept(this);
        if (extra != null && extra.updateTick != null) extra.updateTick.accept(this);

        if (this.world.isRemote) {
            this.prevSpin = this.spin;

            if (this.getBounces() <= 0) {
                this.spin += 15;
            } else {
                this.spin += Math.min(15, new Vec3NT(lastTickPosX - posX, 0, lastTickPosZ - posZ).length() * 50);
            }

            if (this.spin >= 360) {
                this.prevSpin -= 360;
                this.spin -= 360;
            }

            if (this.getTrail() == TRAIL_TRIPLET) {
                MainRegistry.proxy.effectNT(HbmEffectNT.VanillaExt_Flame, posX, posY, posZ);
            }
        }
    }

    public int getTimer() { return this.ticksInAir + this.ticksInGround; }

    @Override
    protected void onImpact(RayTraceResult mop) {
        EnumGrenadeFuze fuze = this.getFuze();
        EnumGrenadeExtra extra = this.getExtra();

        if (fuze != null && fuze.onImpact != null) fuze.onImpact.accept(this, mop);
        if (extra != null && extra.onImpact != null) extra.onImpact.accept(this, mop);

        if (this.isDead) return; // we assume the grenade has gone off by this point

        if (mop.typeOfHit == RayTraceResult.Type.BLOCK) {
            EnumFacing dir = mop.sideHit == null ? EnumFacing.UP : mop.sideHit;
            this.setPosition(
                    mop.hitVec.x + dir.getXOffset() * 0.05,
                    mop.hitVec.y + dir.getYOffset() * 0.05,
                    mop.hitVec.z + dir.getZOffset() * 0.05);
            EnumGrenadeShell shell = this.getShell();
            Vec3NT vec = new Vec3NT(this.motionX, this.motionY, this.motionZ);
            if (vec.length() > 0.2) {
                this.world.playSound(null, this.posX, this.posY, this.posZ, HBMSoundHandler.grenadeBounce, SoundCategory.HOSTILE, 1F, 1F);
            }
            if (dir.getXOffset() != 0) this.motionX *= -shell.getBounce(); else this.motionX *= 0.8;
            if (dir.getYOffset() != 0) this.motionY *= -shell.getBounce(); else this.motionY *= 0.8;
            if (dir.getZOffset() != 0) this.motionZ *= -shell.getBounce(); else this.motionZ *= 0.8;
            if (this.world instanceof WorldServer) TrackerUtil.sendTeleport((WorldServer) this.world, this);
            this.dataManager.set(DW_BOUNCES, this.getBounces() + 1);
        }
    }

    public void explode() {
        this.setDead();
        EnumGrenadeFilling filling = this.getFilling();
        if (filling != null && filling.explode != null) filling.explode.accept(this);
        EnumGrenadeExtra extra = this.getExtra();
        if (extra != null && extra.onExplode != null) extra.onExplode.accept(this);

        if (GeneralConfig.enableExtendedLogging) {
            String s = "null";
            if (getThrower() instanceof EntityPlayer) s = getThrower().getDisplayName().getUnformattedText();
            MainRegistry.logger.info("[GREN] Set off grenade at {} / {} / {} by {}!", (int) posX, (int) posY,
                    (int) posZ, s);
        }
    }

    @Override
    protected int groundDespawn() { return 0; }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("bounces", this.getBounces());
        nbt.setInteger("trail", this.getTrail());
        ItemStack grenade = this.getGrenadeItem();
        if (!grenade.isEmpty()) {
            nbt.setTag("grenade", grenade.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.dataManager.set(DW_BOUNCES, nbt.getInteger("bounces"));
        this.dataManager.set(DW_TRAIL, nbt.getInteger("trail"));
        if (nbt.hasKey("grenade")) {
            this.dataManager.set(DW_GRENADE, new ItemStack(nbt.getCompoundTag("grenade")));
        }
    }
}
