package com.hbm.entity.mob.glyphid;

import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@AutoRegister(name = "entity_glyphid_brawler", eggColors = {0x273038, 0xD2BB72})
public class EntityGlyphidBrawler extends EntityGlyphid {

    public EntityGlyphidBrawler(World world) {
        super(world);
        this.setSize(2F, 1.125F);
    }

    public int timer = 0;
    protected Entity lastTarget;
    protected double lastX;
    protected double lastY;
    protected double lastZ;

    @Override
    public void onUpdate(){
        super.onUpdate();
        Entity e = this.getAttackTarget();
        if (e != null && this.isEntityAlive()) {

            this.lastX = e.posX;
            this.lastY = e.posY;
            this.lastZ = e.posZ;

            if (--timer <= 0) {
                leap();
                timer = 80 + world.rand.nextInt(30);
            }
        }
    }

    /** Mainly composed of repurposed bombardier code**/
    public void leap() {
        if (!world.isRemote && getAttackTarget() instanceof EntityLivingBase && this.getDistance(getAttackTarget()) < 20) {
            Entity e = this.getAttackTarget();

            double velX = e.posX - lastX;
            double velY = e.posY - lastY;
            double velZ = e.posZ - lastZ;

            if (this.lastTarget != e) {
                velX = velY = velZ = 0;
            }

            int prediction = 60;
            Vec3 delta = Vec3.createVectorHelper(e.posX - posX + velX * prediction, (e.posY + e.height / 2) - (posY + 1) + velY * prediction, e.posZ - posZ + velZ * prediction);
            double len = delta.length();
            if (len < 3) return;
            double targetYaw = -Math.atan2(delta.xCoord, delta.zCoord);

            double x = Math.sqrt(delta.xCoord * delta.xCoord + delta.zCoord * delta.zCoord);
            double y = delta.yCoord;
            double v0 = 1.5;
            double v02 = v0 * v0;
            double g = 0.01;
            double targetPitch = Math.atan((v02 + Math.sqrt(v02 * v02 - g * (g * x * x + 2 * y * v02)) * 1) / (g * x));
            Vec3 fireVec = null;
            if (!Double.isNaN(targetPitch)) {

                fireVec = Vec3.createVectorHelper(v0, 0, 0);
                fireVec.rotateAroundZ((float) (-targetPitch / 3.5));
                fireVec.rotateAroundY((float) -(targetYaw + Math.PI * 0.5));
            }
            if (fireVec != null)
                this.launch(fireVec.xCoord, fireVec.yCoord, fireVec.zCoord, (float) v0, rand.nextFloat());
        }
    }
    //yeag this is now a motherfucking projectile
    public void launch(double motionX, double motionY, double motionZ, float velocity, float inaccuracy) {
        float throwLen = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX /= throwLen;
        motionY /= throwLen;
        motionZ /= throwLen;
        motionX += this.rand.nextGaussian() * 0.0075D * (double) inaccuracy;
        motionY += this.rand.nextGaussian() * 0.0075D * (double) inaccuracy;
        motionZ += this.rand.nextGaussian() * 0.0075D * (double) inaccuracy;
        motionX *= velocity;
        motionY *= velocity;
        motionZ *= velocity;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        float hyp = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(motionY, (double) hyp) * 180.0D / Math.PI);
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.glyphid_brawler_tex;
    }

    @Override
    public double getScale() {
        return 1.25D;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(GlyphidStats.getStats().getBrawler().health());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(GlyphidStats.getStats().getBrawler().speed());
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(GlyphidStats.getStats().getBrawler().damage());
    }

    public StatBundle getStats() {
        return GlyphidStats.getStats().statsBrawler;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {

		/*NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "debug");
		data.setInteger("color", 0x0000ff);
		data.setFloat("scale", 2.5F);
		data.setString("text", "" + (int) amount);
		PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, posX, posY + 2, posZ), new TargetPoint(dimension, posX, posY + 2, posZ, 50));*/
        //allows brawlers to get no damage on short leaps, but still affected by fall damage on big drops
        if(source == DamageSource.FALL && amount <= 10) return false;
        return super.attackEntityFrom(source, amount);
    }
    @Override
    public boolean isArmorBroken(float amount) {
        return this.rand.nextInt(100) <= Math.min(Math.pow(amount * 0.25, 2), 100);
    }
}
