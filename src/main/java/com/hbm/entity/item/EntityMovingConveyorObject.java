package com.hbm.entity.item;

import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.api.conveyor.IEnterableBlock;
import com.hbm.config.ServerConfig;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.ExplosionEffectTiny;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class EntityMovingConveyorObject extends Entity {
    protected int turnProgress;
    protected double syncPosX;
    protected double syncPosY;
    protected double syncPosZ;
    @SideOnly(Side.CLIENT) protected double velocityX;
    @SideOnly(Side.CLIENT) protected double velocityY;
    @SideOnly(Side.CLIENT) protected double velocityZ;

    public EntityMovingConveyorObject(World p_i1582_1_) {
        super(p_i1582_1_);
        this.noClip = true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }


    public boolean canAttackWithItem() {
        return true;
    }

    @Override
    public boolean hitByEntity(Entity attacker) {

        if(attacker instanceof EntityPlayer) {
            this.setDead();
        }

        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return true;
    }

    @Override
    public void onUpdate() {
        if(world.isRemote) {
            if(this.turnProgress > 0) {
                double interpX = this.posX + (this.syncPosX - this.posX) / (double) this.turnProgress;
                double interpY = this.posY + (this.syncPosY - this.posY) / (double) this.turnProgress;
                double interpZ = this.posZ + (this.syncPosZ - this.posZ) / (double) this.turnProgress;
                --this.turnProgress;
                this.setPosition(interpX, interpY, interpZ);
            } else {
                this.setPosition(this.posX, this.posY, this.posZ);
            }
        }

        if(!world.isRemote) {
            ticksExisted++;

            if(this.ticksExisted <= 5) {
                return;
            }

            // cram check every 20s
            if((ticksExisted + this.getEntityId()) % 400 == 0) {
                List<EntityMovingConveyorObject> objs = world.getEntitiesWithinAABB(EntityMovingConveyorObject.class, this.getEntityBoundingBox().grow(0.125, 0.125, 0.125));
                if(objs.size() >= ServerConfig.CONVEYOR_CRAM_MAX.get()) {
                    for(EntityMovingConveyorObject obj : objs) obj.setDead();
                    ExplosionVNT vnt = new ExplosionVNT(world, posX, posY + 0.125, posZ, 1, this);
                    vnt.setSFX(new ExplosionEffectTiny());
                    vnt.explode();

                    BlockPos cramPos = new BlockPos(Math.floor(posX), Math.floor(posY), Math.floor(posZ));
                    if(world.getBlockState(cramPos).getBlock() instanceof IConveyorBelt && this.ticksExisted > 400 && ServerConfig.CONVEYOR_CRAM_EXPLODE.get())
                        world.destroyBlock(cramPos, false);
                }
            }

            int blockX = (int) Math.floor(posX);
            int blockY = (int) Math.floor(posY);
            int blockZ = (int) Math.floor(posZ);
            BlockPos blockPos = new BlockPos(blockX, blockY, blockZ);
            Block b = world.getBlockState(blockPos).getBlock();
            boolean isOnConveyor = b instanceof IConveyorBelt && ((IConveyorBelt) b).canItemStay(world, blockX, blockY, blockZ, new Vec3d(posX, posY, posZ));

            if(!isOnConveyor) {
                if(onLeaveConveyor()) {
                    return;
                }
            } else {
                Vec3d target = ((IConveyorBelt) b).getTravelLocation(world, blockX, blockY, blockZ, new Vec3d(posX, posY, posZ), getMoveSpeed());
                this.motionX = target.x - this.posX;
                this.motionY = target.y - this.posY;
                this.motionZ = target.z - this.posZ;
            }

            BlockPos lastPos = new BlockPos(posX, posY, posZ);
            this.move(MoverType.SELF, motionX, motionY, motionZ);
            BlockPos newPos = new BlockPos(posX, posY, posZ);

            if(!lastPos.equals(newPos)) {
                Block newBlock = world.getBlockState(newPos).getBlock();

                if(newBlock instanceof IEnterableBlock) {
                    IEnterableBlock enterable = (IEnterableBlock) newBlock;

                    EnumFacing dir = null;

                    if (lastPos.getX() > newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() == newPos.getZ())
                        dir = EnumFacing.EAST;
                    else if (lastPos.getX() < newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() == newPos.getZ())
                        dir = EnumFacing.WEST;
                    else if (lastPos.getX() == newPos.getX() && lastPos.getY() > newPos.getY() && lastPos.getZ() == newPos.getZ())
                        dir = EnumFacing.UP;
                    else if (lastPos.getX() == newPos.getX() && lastPos.getY() < newPos.getY() && lastPos.getZ() == newPos.getZ())
                        dir = EnumFacing.DOWN;
                    else if (lastPos.getX() == newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() > newPos.getZ())
                        dir = EnumFacing.SOUTH;
                    else if (lastPos.getX() == newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() < newPos.getZ())
                        dir = EnumFacing.NORTH;

                    enterBlock(enterable, newPos, dir);

                } else {
                    if(!newBlock.getMaterial(world.getBlockState(newPos)).isSolid()) {
                        newBlock = world.getBlockState(newPos.down()).getBlock();

                        if(newBlock instanceof IEnterableBlock) {
                            IEnterableBlock enterable = (IEnterableBlock) newBlock;
                            enterBlockFalling(enterable, newPos);
                        }
                    }
                }
            }
        }
    }

    public abstract void enterBlock(IEnterableBlock enterable, BlockPos pos, EnumFacing dir);

    public void enterBlockFalling(IEnterableBlock enterable, BlockPos pos) {
        this.enterBlock(enterable, pos.add(0, -1, 0), EnumFacing.UP);
    }

    public abstract boolean onLeaveConveyor();

    public double getMoveSpeed() {
        return 0.0625D;
    }

    @SideOnly(Side.CLIENT)
    public void setVelocity(double motionX, double motionY, double motionZ) {
        this.velocityX = this.motionX = motionX;
        this.velocityY = this.motionY = motionY;
        this.velocityZ = this.motionZ = motionZ;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int theNumberThree, boolean teleport) {
        this.syncPosX = x;
        this.syncPosY = y;
        this.syncPosZ = z;
        this.turnProgress = theNumberThree + 2; //use 4-ply for extra smoothness
        this.motionX = this.velocityX;
        this.motionY = this.velocityY;
        this.motionZ = this.velocityZ;
    }

}
