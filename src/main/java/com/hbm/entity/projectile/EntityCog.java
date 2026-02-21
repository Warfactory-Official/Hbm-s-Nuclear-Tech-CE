package com.hbm.entity.projectile;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@AutoRegister(name = "entity_cog", trackingRange = 1000)
public class EntityCog extends EntityThrowableInterp {
    private static final DataParameter<Integer> ROT = EntityDataManager.createKey(EntityCog.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> META = EntityDataManager.createKey(EntityCog.class, DataSerializers.VARINT);

    public EntityCog(World worldIn) {
        super(worldIn);
        this.setSize(1F, 1F);
    }

    public EntityCog(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
        this.setSize(1F, 1F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ROT, 0);
        this.dataManager.register(META, 0);
    }

    public EntityCog setOrientation(int rot) {
        this.dataManager.set(ROT, rot);
        return this;
    }

    public EntityCog setMeta(int meta) {
        this.dataManager.set(META, meta);
        return this;
    }

    public int getOrientation() {
        return this.dataManager.get(ROT);
    }

    public int getMeta() {
        return this.dataManager.get(META);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            if (player.inventory.addItemStackToInventory(new ItemStack(ModItems.gear_large, 1, this.getMeta()))) {
                this.setDead();
            }
            player.inventoryContainer.detectAndSendChanges();
        }
        return true;
    }


    @Override
    protected void onImpact(RayTraceResult result) {
        if (world != null && result != null && result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit.isEntityAlive()) {
            Entity e = result.entityHit;
            e.attackEntityFrom(ModDamageSource.rubble, 1000);
            if (!e.isEntityAlive() && e instanceof EntityLivingBase) {
                NBTTagCompound vdat = new NBTTagCompound();
                vdat.setString("type", "giblets");
                vdat.setInteger("ent", e.getEntityId());
                vdat.setInteger("cDiv", 5);
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(vdat, e.posX, e.posY + e.height * 0.5, e.posZ), new NetworkRegistry.TargetPoint(e.dimension, e.posX, e.posY + e.height * 0.5, e.posZ, 150));

                world.playSound(null, e.posX, e.posY, e.posZ, SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.HOSTILE, 2.0F, 0.95F + world.rand.nextFloat() * 0.2F);
            }
        }

        if (this.ticksExisted > 1 && world != null && result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
            int orientation = this.getOrientation();

            if (orientation < 6) {
                if (new Vec3d(motionX, motionY, motionZ).length() < 0.75) {
                    this.setOrientation(orientation + 6);
                    orientation += 6;
                } else {
                    EnumFacing side = result.sideHit;
                    this.motionX *= 1 - (Math.abs(side.getXOffset()) * 2);
                    this.motionY *= 1 - (Math.abs(side.getYOffset()) * 2);
                    this.motionZ *= 1 - (Math.abs(side.getZOffset()) * 2);
                    world.createExplosion(this, posX, posY, posZ, 3F, false);

                    BlockPos hitPos = result.getBlockPos();
                    IBlockState state = world.getBlockState(hitPos);
                    if (state.getBlock().getExplosionResistance(world, hitPos, this, null) < 50) {
                        world.destroyBlock(hitPos, false);
                    }
                }
            }

            if (orientation >= 6) {
                this.motionX = 0;
                this.motionY = 0;
                this.motionZ = 0;
                this.onGround = true;
                this.setCollisionDisabled(true); //Makes you not push it into the ground
            }
        }
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            int orientation = this.getOrientation();
            if (orientation >= 6 && !this.onGround) {
                this.setOrientation(orientation - 6);
            }
        }
        super.onUpdate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return true;
    }

    @Override
    public float getGravityVelocity() {
        return inGround ? 0F : 0.03F;
    }

    @Override
    protected int groundDespawn() {
        return 0;
    }
    @Override
    public boolean canBePushed() {
        return !collisionDisabled;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !collisionDisabled;
    }
    private boolean collisionDisabled = false;

    public void setCollisionDisabled(boolean disabled) {
        this.collisionDisabled = disabled;
    }
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("rot", this.getOrientation());
        compound.setInteger("meta", this.getMeta());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setOrientation(compound.getInteger("rot"));
        this.setMeta(compound.getInteger("meta"));
    }
}

