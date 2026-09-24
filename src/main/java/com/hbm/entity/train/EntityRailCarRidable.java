package com.hbm.entity.train;

import com.hbm.interfaces.AutoRegister;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public abstract class EntityRailCarRidable extends EntityRailCarCargo {

    public double engineSpeed;
    public SeatDummyEntity[] passengerSeats;

    public EntityRailCarRidable(World world) {
        super(world);
        this.passengerSeats = new SeatDummyEntity[this.getPassengerSeats().length];
    }

    public abstract double getPoweredAcceleration();
    public abstract double getPassivBrake();
    public abstract boolean shouldUseEngineBrake(EntityPlayer player);
    public abstract double getMaxPoweredSpeed();
    public abstract boolean canAccelerate();
    public void consumeFuel() { }

    public double getGravitySpeed() {
        return 0D;
    }

    @Override
    public double getCurrentSpeed() {

        Entity controller = this.getControllingPassenger();

        if(controller instanceof EntityPlayer) {

            EntityPlayer player = (EntityPlayer) controller;

            if(this.canAccelerate()) {
                if(player.moveForward > 0) {
                    engineSpeed += this.getPoweredAcceleration();
                    this.consumeFuel();
                } else if(player.moveForward < 0) {
                    engineSpeed -= this.getPoweredAcceleration();
                    this.consumeFuel();
                } else {
                    if(this.shouldUseEngineBrake(player)) {
                        engineSpeed *= this.getPassivBrake();
                    } else {
                        this.consumeFuel();
                    }
                }
            } else {
                engineSpeed *= this.getPassivBrake();
            }

        } else {
            engineSpeed *= this.getPassivBrake();
        }

        double maxSpeed = this.getMaxPoweredSpeed();
        engineSpeed = MathHelper.clamp(engineSpeed, -maxSpeed, maxSpeed);

        return engineSpeed + this.getGravitySpeed();
    }

    @Override
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    @Override
    public boolean canBeRidden(@NotNull Entity entity) {
        return true;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, @NotNull EnumHand hand) {

        if(super.processInitialInteract(player, hand)) return true;
        if(world.isRemote) return true;

        int nearestSeat = this.getNearestSeat(player);

        if(nearestSeat == -1) {
            player.startRiding(this);
        } else if(nearestSeat >= 0) {
            SeatDummyEntity dummySeat = new SeatDummyEntity(world, this, nearestSeat);
            Vec3d passengerSeat = this.getPassengerSeats()[nearestSeat].rotateYaw((float) (-this.rotationYaw * Math.PI / 180));
            dummySeat.setPosition(renderX + passengerSeat.x, renderY + passengerSeat.y - 1, renderZ + passengerSeat.z);
            passengerSeats[nearestSeat] = dummySeat;
            world.spawnEntity(dummySeat);
            player.startRiding(dummySeat);
        }

        return true;
    }

    public int getNearestSeat(EntityPlayer player) {

        if(player == null) return -2;

        double nearestDist = Double.POSITIVE_INFINITY;
        int nearestSeat = -3;

        Vec3d[] seats = getPassengerSeats();
        Vec3d look = player.getPositionEyes(1F).add(player.getLook(1F));

        for(int i = 0; i < seats.length; i++) {

            Vec3d seat = seats[i];
            if(seat == null) continue;
            if(passengerSeats[i] != null) continue;

            Vec3d rot = seat.rotateYaw((float) (-this.rotationYaw * Math.PI / 180));
            Vec3d delta = new Vec3d(look.x - (renderX + rot.x), look.y - (renderY + rot.y), look.z - (renderZ + rot.z));
            double dist = delta.length();

            if(dist < nearestDist) {
                nearestDist = dist;
                nearestSeat = i;
            }
        }

        if(!this.isBeingRidden()) {
            Vec3d rot = getRiderSeatPosition().rotateYaw((float) (-this.rotationYaw * Math.PI / 180));
            Vec3d delta = new Vec3d(look.x - (renderX + rot.x), look.y - (renderY + rot.y), look.z - (renderZ + rot.z));
            double dist = delta.length();

            if(dist < nearestDist) {
                nearestDist = dist;
                nearestSeat = -1;
            }
        }

        if(nearestDist > 180) return -2;

        return nearestSeat;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if(!world.isRemote) {

            Vec3d[] seats = this.getPassengerSeats();
            for(int i = 0; i < passengerSeats.length; i++) {
                SeatDummyEntity seat = passengerSeats[i];

                if(seat != null) {
                    if(!seat.isBeingRidden()) {
                        passengerSeats[i] = null;
                        seat.setDead();
                    } else {
                        Vec3d rot = seats[i]
                                .rotatePitch((float) (this.rotationPitch * Math.PI / 180))
                                .rotateYaw((float) (-this.rotationYaw * Math.PI / 180));
                        seat.setPosition(renderX + rot.x, renderY + rot.y - 1, renderZ + rot.z);
                    }
                }
            }
        }
    }

    @Override
    public void updatePassenger(@NotNull Entity passenger) {

        Vec3d offset = getRiderSeatPosition()
                .rotatePitch((float) (this.rotationPitch * Math.PI / 180))
                .rotateYaw((float) (-this.rotationYaw * Math.PI / 180));

        passenger.setPosition(this.renderX + offset.x, this.renderY + offset.y, this.renderZ + offset.z);
    }

    public abstract Vec3d getRiderSeatPosition();

    public abstract Vec3d[] getPassengerSeats();

    @AutoRegister(name = "entity_ntm_train_seat", trackingRange = 250, sendVelocityUpdates = false)
    public static class SeatDummyEntity extends Entity {

        private static final DataParameter<Integer> TRAIN_ID = EntityDataManager.createKey(SeatDummyEntity.class, DataSerializers.VARINT);
        private static final DataParameter<Integer> SEAT_INDEX = EntityDataManager.createKey(SeatDummyEntity.class, DataSerializers.VARINT);

        private int turnProgress;
        private double trainX;
        private double trainY;
        private double trainZ;
        public EntityRailCarRidable train;

        public SeatDummyEntity(World world) { super(world); this.setSize(0.5F, 0.1F); }
        public SeatDummyEntity(World world, EntityRailCarRidable train, int index) {
            this(world);
            this.train = train;
            if(train != null) this.dataManager.set(TRAIN_ID, train.getEntityId());
            this.dataManager.set(SEAT_INDEX, index);
        }

        @Override protected void entityInit() {
            this.dataManager.register(TRAIN_ID, 0);
            this.dataManager.register(SEAT_INDEX, 0);
        }

        @Override protected void writeEntityToNBT(@NotNull NBTTagCompound nbt) { }
        @Override public boolean writeToNBTOptional(@NotNull NBTTagCompound nbt) { return false; }
        @Override public void readEntityFromNBT(@NotNull NBTTagCompound nbt) { this.setDead(); }

        @Override
        public boolean canBeRidden(@NotNull Entity entity) {
            return true;
        }

        @Override public void onUpdate() {
            if(!world.isRemote) {
                if(this.train == null || this.train.isDead) {
                    this.setDead();
                }
            } else {

                if(this.turnProgress > 0) {
                    this.prevRotationYaw = this.rotationYaw;
                    double x = this.posX + (this.trainX - this.posX) / (double) this.turnProgress;
                    double y = this.posY + (this.trainY - this.posY) / (double) this.turnProgress;
                    double z = this.posZ + (this.trainZ - this.posZ) / (double) this.turnProgress;
                    --this.turnProgress;
                    this.setPosition(x, y, z);
                } else {
                    this.setPosition(this.posX, this.posY, this.posZ);
                }
            }
        }

        @Override @SideOnly(Side.CLIENT) public void setPositionAndRotationDirect(double posX, double posY, double posZ, float yaw, float pitch, int turnProg, boolean teleport) {
            this.trainX = posX;
            this.trainY = posY;
            this.trainZ = posZ;
            this.turnProgress = turnProg + 2;
        }

        @Override
        public void updatePassenger(@NotNull Entity passenger) {

            if(train == null) {
                Entity entity = world.getEntityByID(this.dataManager.get(TRAIN_ID));
                if(entity instanceof EntityRailCarRidable) {
                    train = (EntityRailCarRidable) entity;
                }
            }

            if(train == null) {
                passenger.setPosition(posX, posY + 1, posZ);
                return;
            }

            int index = this.dataManager.get(SEAT_INDEX);
            Vec3d[] seats = this.train.getPassengerSeats();

            if(index < 0 || index >= seats.length) {
                passenger.setPosition(posX, posY + 1, posZ);
                return;
            }

            Vec3d rot = seats[index]
                    .rotatePitch((float) (train.rotationPitch * Math.PI / 180))
                    .rotateYaw((float) (-train.rotationYaw * Math.PI / 180));
            passenger.setPosition(train.renderX + rot.x, train.renderY + rot.y, train.renderZ + rot.z);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
    }
}
