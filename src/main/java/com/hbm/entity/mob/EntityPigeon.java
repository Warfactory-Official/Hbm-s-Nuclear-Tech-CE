package com.hbm.entity.mob;

import com.hbm.entity.mob.ai.*;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.tool.ItemFertilizer;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.network.NetworkRegistry;
@AutoRegister(name = "entity_pigeon", trackingRange = 80, updateFrequency = 3, eggColors = {0xC8C9CD, 0x858894})
public class EntityPigeon extends EntityCreature implements IFlyingCreature, IAnimals {

    private static final DataParameter<Byte> FLYING_STATE = EntityDataManager.createKey(EntityPigeon.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> FAT_STATE = EntityDataManager.createKey(EntityPigeon.class, DataSerializers.BYTE);

    public float fallTime;
    public float dest;
    public float prevDest;
    public float prevFallTime;
    public float offGroundTimer = 1.0F;

    public EntityPigeon(World worldIn) {
        super(worldIn);
        this.tasks.addTask(0, new EntityAIStartFlying(this, this));
        this.tasks.addTask(0, new EntityAIStopFlying(this, this));
        this.tasks.addTask(1, new EntityAISwimmingConditional(this, entity -> ((EntityPigeon) entity).getFlyingState() == IFlyingCreature.STATE_WALKING));
        this.tasks.addTask(2, new EntityAIEatBread(this, 0.4D));
        this.tasks.addTask(5, new EntityAIConditionalWander<>(this, 0.2D, entity -> entity.getFlyingState() == IFlyingCreature.STATE_WALKING));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.setSize(0.5F, 1.0F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLYING_STATE, (byte) 0);
        this.dataManager.register(FAT_STATE, (byte) 0);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (amount >= this.getMaxHealth() * 2 && !this.world.isRemote) {
            this.setDead();

            for (int i = 0; i < 10; i++) {
                Vec3d vec = new Vec3d(this.rand.nextGaussian(), this.rand.nextGaussian(), this.rand.nextGaussian()).normalize();
                EntityItem feather = new EntityItem(this.world, this.posX + vec.x, this.posY + this.height / 2.0D + vec.y, this.posZ + vec.z, new ItemStack(Items.FEATHER));
                feather.motionX = vec.x * 0.5D;
                feather.motionY = vec.y * 0.5D;
                feather.motionZ = vec.z * 0.5D;
                this.world.spawnEntity(feather);
            }

            return true;
        }

        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected Item getDropItem() {
        return Items.FEATHER;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
        int featherCount = this.rand.nextInt(3) + this.rand.nextInt(1 + lootingModifier);

        for (int i = 0; i < featherCount; ++i) {
            this.dropItem(Items.FEATHER, 1);
        }

        if (this.isBurning()) {
            this.dropItem(Items.COOKED_CHICKEN, this.isFat() ? 3 : 1);
        } else {
            this.dropItem(Items.CHICKEN, this.isFat() ? 3 : 1);
        }
    }

    @Override
    public int getFlyingState() {
        return this.dataManager.get(FLYING_STATE);
    }

    @Override
    public void setFlyingState(int state) {
        this.dataManager.set(FLYING_STATE, (byte) state);
    }

    public boolean isFat() {
        return this.dataManager.get(FAT_STATE) == 1;
    }

    public void setFat(boolean fat) {
        this.dataManager.set(FAT_STATE, (byte) (fat ? 1 : 0));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();

        if (this.getFlyingState() == IFlyingCreature.STATE_FLYING) {
            BlockPos heightPos = this.world.getHeight(new BlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ)));
            int height = heightPos.getY();

            boolean ceil = this.posY - height > 10;
            this.motionY = this.rand.nextGaussian() * 0.05D + (ceil ? 0.0D : 0.04D) + (this.isInWater() ? 0.2D : 0.0D);

            if (this.onGround) {
                this.motionY = Math.abs(this.motionY) + 0.1D;
            }

            this.moveForward = 1.5F;

            if (this.rand.nextInt(20) == 0) {
                this.rotationYaw += this.rand.nextGaussian() * 30.0D;
            }

            if (this.isFat() && this.rand.nextInt(50) == 0) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("type", "sweat");
                nbt.setInteger("count", 3);
                nbt.setInteger("block", Block.getIdFromBlock(Blocks.WOOL));
                nbt.setInteger("entity", this.getEntityId());
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0), new NetworkRegistry.TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 50.0D));

                int x = MathHelper.floor(this.posX);
                int y = MathHelper.floor(this.posY) - 1;
                int z = MathHelper.floor(this.posZ);
                EntityPlayer player = FakePlayerFactory.getMinecraft((WorldServer) this.world);

                for (int i = 0; i < 25; i++) {
                    if (ItemFertilizer.fertilize(this.world, x, y - i, z, player, EnumHand.MAIN_HAND, player.getHeldItemMainhand(), true)) {
                        this.world.playEvent(2005, new BlockPos(x, y - i, z), 0);
                        break;
                    }
                }

                if (this.rand.nextInt(10) == 0) {
                    this.setFat(false);
                }
            }
        } else if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.8D;
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevFallTime = this.fallTime;
        this.prevDest = this.dest;
        this.dest = (float) ((double) this.dest + (double) (this.onGround ? -1 : 4) * 0.3D);

        if (this.dest < 0.0F) {
            this.dest = 0.0F;
        }

        if (this.dest > 1.0F) {
            this.dest = 1.0F;
        }

        if (!this.onGround && this.offGroundTimer < 1.0F) {
            this.offGroundTimer = 1.0F;
        }

        this.offGroundTimer = (float) ((double) this.offGroundTimer * 0.9D);

        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.6D;
        }

        this.fallTime += this.offGroundTimer * 2.0F;
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }
}