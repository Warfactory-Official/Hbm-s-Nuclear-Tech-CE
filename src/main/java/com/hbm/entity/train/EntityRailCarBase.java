package com.hbm.entity.train;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.rail.IRailNTM;
import com.hbm.blocks.rail.IRailNTM.MoveContext;
import com.hbm.blocks.rail.IRailNTM.RailCheckType;
import com.hbm.blocks.rail.IRailNTM.RailContext;
import com.hbm.blocks.rail.IRailNTM.TrackGauge;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public abstract class EntityRailCarBase extends Entity implements ILookOverlay {

    public LogicalTrainUnit ltu;
    public int ltuIndex = 0;
    public boolean isOnRail = true;
    private int turnProgress;
    private double trainX;
    private double trainY;
    private double trainZ;
    private double trainYaw;
    private double trainPitch;
    private float movementYaw;
    private float movementPitch;
    @SideOnly(Side.CLIENT) private double velocityX;
    @SideOnly(Side.CLIENT) private double velocityY;
    @SideOnly(Side.CLIENT) private double velocityZ;
    public double lastRenderX;
    public double lastRenderY;
    public double lastRenderZ;
    public double renderX;
    public double renderY;
    public double renderZ;
    public double cachedSpeed;

    public EntityRailCarBase coupledFront;
    public EntityRailCarBase coupledBack;

    public boolean initDummies = false;
    public BoundingBoxDummyEntity[] dummies = new BoundingBoxDummyEntity[0];

    public EntityRailCarBase(World world) {
        super(world);
    }

    @Override protected void entityInit() { }
    @Override protected void readEntityFromNBT(@NotNull NBTTagCompound nbt) { }
    @Override protected void writeEntityToNBT(@NotNull NBTTagCompound nbt) { }

    @Override
    public boolean processInitialInteract(EntityPlayer player, @NotNull EnumHand hand) {

        if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == ModItems.coupling_tool) {

            List<EntityRailCarBase> intersecting = world.getEntitiesWithinAABB(EntityRailCarBase.class, this.getEntityBoundingBox().grow(2D, 0D, 2D));

            for(EntityRailCarBase neighbor : intersecting) {
                if(neighbor == this) continue;
                if(neighbor.getGauge() != this.getGauge()) continue;

                TrainCoupling closestOwnCoupling = null;
                TrainCoupling closestNeighborCoupling = null;
                double closestDist = Double.POSITIVE_INFINITY;

                for(TrainCoupling ownCoupling : TrainCoupling.values()) {
                    for(TrainCoupling neighborCoupling : TrainCoupling.values()) {
                        Vec3d ownPos = this.getCouplingPos(ownCoupling);
                        Vec3d neighborPos = neighbor.getCouplingPos(neighborCoupling);
                        if(ownPos != null && neighborPos != null) {
                            Vec3d delta = new Vec3d(ownPos.x - neighborPos.x, ownPos.y - neighborPos.y, ownPos.z - neighborPos.z);
                            double length = delta.length();

                            if(length < 1 && length < closestDist) {
                                closestDist = length;
                                closestOwnCoupling = ownCoupling;
                                closestNeighborCoupling = neighborCoupling;
                            }
                        }
                    }
                }

                if(closestOwnCoupling != null && closestNeighborCoupling != null) {
                    if(this.getCoupledTo(closestOwnCoupling) != null) continue;
                    if(neighbor.getCoupledTo(closestNeighborCoupling) != null) continue;
                    this.couple(closestOwnCoupling, neighbor);
                    neighbor.couple(closestNeighborCoupling, this);
                    if(this.ltu != null) this.ltu.dissolveTrain();
                    if(neighbor.ltu != null) neighbor.ltu.dissolveTrain();
                    player.swingArm(hand);

                    player.sendMessage(new TextComponentString("Coupled " + this.hashCode() + " (" + closestOwnCoupling.name() + ") to " + neighbor.hashCode() + " (" + closestNeighborCoupling.name() + ")"));

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onUpdate() {

        if(this.world.isRemote) {

            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if(this.turnProgress > 0) {
                this.prevRotationYaw = this.rotationYaw;
                double x = this.posX + (this.trainX - this.posX) / (double) this.turnProgress;
                double y = this.posY + (this.trainY - this.posY) / (double) this.turnProgress;
                double z = this.posZ + (this.trainZ - this.posZ) / (double) this.turnProgress;
                double yaw = MathHelper.wrapDegrees(this.trainYaw - (double) this.rotationYaw);
                this.rotationYaw = (float) ((double) this.rotationYaw + yaw / (double) this.turnProgress);
                this.rotationPitch = (float) ((double) this.rotationPitch + (this.trainPitch - (double) this.rotationPitch) / (double) this.turnProgress);
                --this.turnProgress;
                this.setPosition(x, y, z);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            } else {
                this.setPosition(this.posX, this.posY, this.posZ);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            }

            BlockPos anchor = this.getCurrentAnchorPos();
            Vec3d frontPos = getRelPosAlongRail(anchor, this.getLengthSpan(), new MoveContext(RailCheckType.FRONT, this.getCollisionSpan() - this.getLengthSpan()));
            Vec3d backPos = getRelPosAlongRail(anchor, -this.getLengthSpan(), new MoveContext(RailCheckType.BACK, this.getCollisionSpan() - this.getLengthSpan()));

            this.lastRenderX = this.renderX;
            this.lastRenderY = this.renderY;
            this.lastRenderZ = this.renderZ;

            if(frontPos != null && backPos != null) {
                this.renderX = (frontPos.x + backPos.x) / 2D;
                this.renderY = (frontPos.y + backPos.y) / 2D;
                this.renderZ = (frontPos.z + backPos.z) / 2D;
            } else {
                this.renderX = posX;
                this.renderY = posY;
                this.renderZ = posZ;
            }

        } else {

            if(!this.isOnRail) {
                if(this.coupledFront != null) this.coupledFront.couple(this.coupledFront.getCouplingFrom(this), null);
                if(this.coupledBack != null) this.coupledBack.couple(this.coupledBack.getCouplingFrom(this), null);
                this.coupledFront = null;
                this.coupledBack = null;
            }

            if(this.coupledFront != null && this.coupledFront.isDead) {
                this.coupledFront = null;
                if(this.ltu != null) this.ltu.dissolveTrain();
            }
            if(this.coupledBack != null && this.coupledBack.isDead) {
                this.coupledBack = null;
                if(this.ltu != null) this.ltu.dissolveTrain();
            }

            if(this.ltu == null && (this.coupledFront == null || this.coupledBack == null) && this.isOnRail) {
                LogicalTrainUnit.generateTrain(this);
            }

            if(!this.isOnRail) {
                Vec3d motion = new Vec3d(0, 0, this.cachedSpeed).rotateYaw((float) (-this.rotationYaw * Math.PI / 180D));
                this.move(MoverType.SELF, motion.x, motion.y - 0.04, motion.z);
                this.renderX = posX;
                this.renderY = posY;
                this.renderZ = posZ;
                this.cachedSpeed *= 0.95D;
            }

            DummyConfig[] definitions = this.getDummies();

            if(!this.initDummies) {
                this.dummies = new BoundingBoxDummyEntity[definitions.length];

                for(int i = 0; i < definitions.length; i++) {
                    DummyConfig def = definitions[i];
                    BoundingBoxDummyEntity dummy = new BoundingBoxDummyEntity(world, this, def.width, def.height);
                    Vec3d rot = def.offset.rotateYaw((float) (-this.rotationYaw * Math.PI / 180));
                    dummy.setPosition(posX + rot.x, posY + rot.y, posZ + rot.z);
                    dummy.setSize(def.width, def.height);
                    world.spawnEntity(dummy);
                    this.dummies[i] = dummy;
                }

                this.initDummies = true;
            }

            if(renderY != 0) {
                for(int i = 0; i < definitions.length; i++) {
                    DummyConfig def = definitions[i];
                    BoundingBoxDummyEntity dummy = dummies[i];
                    Vec3d rot = def.offset
                            .rotatePitch((float) (this.rotationPitch * Math.PI / 180D))
                            .rotateYaw((float) (-this.rotationYaw * Math.PI / 180));
                    dummy.setPosition(renderX + rot.x, renderY + rot.y, renderZ + rot.z);
                }
            }
        }
    }

    public Vec3d getRelPosAlongRail(BlockPos anchor, double distanceToCover, MoveContext context) {
        return getRelPosAlongRail(anchor, distanceToCover, this.getGauge(), this.world, new Vec3d(posX, posY, posZ), this.rotationYaw, context);
    }

    public static Vec3d getRelPosAlongRail(BlockPos anchor, double distanceToCover, TrackGauge gauge, World world, Vec3d next, float yaw, MoveContext context) {

        if(distanceToCover < 0) {
            distanceToCover *= -1;
            yaw += 180;
        }

        int it = 0;

        do {

            it++;

            if(it > 30) {
                return null;
            }

            int x = anchor.getX();
            int y = anchor.getY();
            int z = anchor.getZ();
            Block block = world.getBlockState(anchor).getBlock();

            Vec3d rot = new Vec3d(0, 0, 1).rotateYaw((float) (-yaw * Math.PI / 180D));

            if(block instanceof IRailNTM) {
                IRailNTM rail = (IRailNTM) block;

                if(it == 1) {
                    next = rail.getTravelLocation(world, x, y, z, next.x, next.y, next.z, rot.x, rot.y, rot.z, 0, new RailContext(), context);
                }

                boolean flip = distanceToCover < 0;

                if(rail.getGauge(world, x, y, z) == gauge) {
                    RailContext info = new RailContext();
                    Vec3d prev = next;
                    next = rail.getTravelLocation(world, x, y, z, prev.x, prev.y, prev.z, rot.x, rot.y, rot.z, distanceToCover, info, context);
                    distanceToCover = info.overshoot;
                    anchor = info.pos;

                    yaw = generateYaw(next, prev) * (flip ? -1 : 1);

                } else {
                    return null;
                }
            } else {
                return null;
            }

        } while(distanceToCover != 0);

        return next;
    }

    public static float generateYaw(Vec3d front, Vec3d back) {
        double deltaX = front.x - back.x;
        double deltaZ = front.z - back.z;
        double radians = -Math.atan2(deltaX, deltaZ);
        return (float) MathHelper.wrapDegrees(radians * 180D / Math.PI);
    }

    public static void updateMotion(World world) {
        Set<LogicalTrainUnit> ltus = new HashSet<>();

        for(Entity o : world.loadedEntityList) {
            if(o instanceof EntityRailCarBase) {
                EntityRailCarBase train = (EntityRailCarBase) o;
                if(train.ltu != null) ltus.add(train.ltu);
            }
        }

        for(LogicalTrainUnit ltu : ltus) {

            double speed = ltu.getTotalSpeed() + ltu.pushForce;

            if(Math.abs(speed) < 0.001) speed = 0;

            for(EntityRailCarBase car : ltu.trains) car.cachedSpeed = speed;

            if(ltu.trains.length == 1) {

                EntityRailCarBase train = ltu.trains[0];

                BlockPos anchor = new BlockPos(train.posX, train.posY, train.posZ);
                Vec3d newPos = train.getRelPosAlongRail(anchor, speed, new MoveContext(RailCheckType.CORE, 0));
                if(newPos == null) {
                    train.derail();
                    ltu.dissolveTrain();
                    continue;
                }
                train.setPosition(newPos.x, newPos.y, newPos.z);
                anchor = train.getCurrentAnchorPos();
                Vec3d frontPos = train.getRelPosAlongRail(anchor, train.getLengthSpan(), new MoveContext(RailCheckType.FRONT, train.getCollisionSpan() - train.getLengthSpan()));
                Vec3d backPos = train.getRelPosAlongRail(anchor, -train.getLengthSpan(), new MoveContext(RailCheckType.BACK, train.getCollisionSpan() - train.getLengthSpan()));

                if(frontPos == null || backPos == null) {
                    train.derail();
                    ltu.dissolveTrain();
                    continue;
                } else {
                    ltu.setRenderPos(train, frontPos, backPos);
                }

                ltu.pushForce = 0;
                ltu.collideTrain(speed);

                continue;
            }

            if(speed == 0) {
                ltu.combineWagons();
            } else {
                ltu.moveTrainByApproach(speed);
            }

            ltu.pushForce = 0;
            ltu.collideTrain(speed);
        }
    }

    public abstract double getCurrentSpeed();
    public abstract double getMaxRailSpeed();
    public abstract TrackGauge getGauge();
    public abstract double getLengthSpan();
    public abstract double getCollisionSpan();

    public BlockPos getCurrentAnchorPos() {
        return new BlockPos(posX, posY + 0.25, posZ);
    }

    public void derail() {
        isOnRail = false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double posX, double posY, double posZ, float yaw, float pitch, int turnProg, boolean teleport) {
        this.trainX = posX;
        this.trainY = posY;
        this.trainZ = posZ;
        this.trainPitch = pitch;
        this.turnProgress = turnProg + 2;
        this.motionX = this.velocityX;
        this.motionY = this.velocityY;
        this.motionZ = this.velocityZ;
        this.trainYaw = this.movementYaw;
        this.trainPitch = this.movementPitch;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double mX, double mY, double mZ) {
        this.movementYaw = (float) this.motionX * 360F;
        this.movementPitch = (float) this.motionY * 360F;
        this.velocityX = this.motionX = mX;
        this.velocityY = this.motionY = mY;
        this.velocityZ = this.motionZ = mZ;
    }

    @AutoRegister(name = "entity_ntm_bounding_dummy", trackingRange = 250, sendVelocityUpdates = false)
    public static class BoundingBoxDummyEntity extends Entity implements ILookOverlay {

        private static final DataParameter<Integer> TRAIN_ID = EntityDataManager.createKey(BoundingBoxDummyEntity.class, DataSerializers.VARINT);
        private static final DataParameter<Float> DUMMY_WIDTH = EntityDataManager.createKey(BoundingBoxDummyEntity.class, DataSerializers.FLOAT);
        private static final DataParameter<Float> DUMMY_HEIGHT = EntityDataManager.createKey(BoundingBoxDummyEntity.class, DataSerializers.FLOAT);

        private int turnProgress;
        private double trainX;
        private double trainY;
        private double trainZ;
        public EntityRailCarBase train;

        public BoundingBoxDummyEntity(World world) { this(world, null, 1F, 1F); }
        public BoundingBoxDummyEntity(World world, EntityRailCarBase train, float width, float height) {
            super(world);
            this.setSize(width, height);
            this.train = train;
            if(train != null) this.dataManager.set(TRAIN_ID, train.getEntityId());
        }

        @Override
        public void setSize(float width, float height) {
            super.setSize(width, height);
            this.dataManager.set(DUMMY_WIDTH, width);
            this.dataManager.set(DUMMY_HEIGHT, height);
        }

        @Override protected void entityInit() {
            this.dataManager.register(TRAIN_ID, 0);
            this.dataManager.register(DUMMY_WIDTH, 1F);
            this.dataManager.register(DUMMY_HEIGHT, 1F);
        }

        @Override protected void writeEntityToNBT(@NotNull NBTTagCompound nbt) { }
        @Override public boolean writeToNBTOptional(@NotNull NBTTagCompound nbt) { return false; }
        @Override public void readEntityFromNBT(@NotNull NBTTagCompound nbt) { this.setDead(); }
        @Override public boolean canBePushed() { return true; }
        @Override public boolean canBeCollidedWith() { return !this.isDead; }

        @Override public boolean attackEntityFrom(@NotNull DamageSource source, float amount) { if(train != null) return train.attackEntityFrom(source, amount); return super.attackEntityFrom(source, amount); }
        @Override public boolean processInitialInteract(EntityPlayer player, @NotNull EnumHand hand) { if(train != null) return train.processInitialInteract(player, hand); return super.processInitialInteract(player, hand); }

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

                this.setSize(this.dataManager.get(DUMMY_WIDTH), this.dataManager.get(DUMMY_HEIGHT));
            }
        }

        @Override @SideOnly(Side.CLIENT) public void setPositionAndRotationDirect(double posX, double posY, double posZ, float yaw, float pitch, int turnProg, boolean teleport) {
            this.trainX = posX;
            this.trainY = posY;
            this.trainZ = posZ;
            this.turnProgress = turnProg + 2;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
            Entity e = this.world.getEntityByID(this.dataManager.get(TRAIN_ID));
            if(e instanceof EntityRailCarBase) {
                ((EntityRailCarBase) e).printHook(event, world, pos);
            }
        }
    }

    public DummyConfig[] getDummies() {
        return new DummyConfig[0];
    }

    public static class DummyConfig {
        public Vec3d offset;
        public float width;
        public float height;

        public DummyConfig(float width, float height, Vec3d offset) {
            this.width = width;
            this.height = height;
            this.offset = offset;
        }
    }

    public enum TrainCoupling {
        FRONT,
        BACK
    }

    public double getCouplingDist(TrainCoupling coupling) {
        return 0D;
    }

    public Vec3d getCouplingPos(TrainCoupling coupling) {
        double dist = this.getCouplingDist(coupling);

        if(dist <= 0) return null;

        if(coupling == TrainCoupling.BACK) dist *= -1;

        Vec3d rot = new Vec3d(0, 0, dist).rotateYaw((float) (-this.rotationYaw * Math.PI / 180D));
        return rot.add(this.renderX, this.renderY, this.renderZ);
    }

    public EntityRailCarBase getCoupledTo(TrainCoupling coupling) {
        return coupling == TrainCoupling.FRONT ? this.coupledFront : coupling == TrainCoupling.BACK ? this.coupledBack : null;
    }

    public TrainCoupling getCouplingFrom(EntityRailCarBase coupledTo) {
        return coupledTo == this.coupledFront ? TrainCoupling.FRONT : coupledTo == this.coupledBack ? TrainCoupling.BACK : null;
    }

    public void couple(TrainCoupling coupling, EntityRailCarBase to) {
        if(coupling == TrainCoupling.FRONT) this.coupledFront = to;
        if(coupling == TrainCoupling.BACK) this.coupledBack = to;
    }

    public static class LogicalTrainUnit {

        protected double pushForce;
        protected EntityRailCarBase[] trains;

        public static LogicalTrainUnit generateTrain(EntityRailCarBase train) {
            List<EntityRailCarBase> links = new ArrayList<>();
            Set<EntityRailCarBase> brake = new HashSet<>();
            LogicalTrainUnit ltu = new LogicalTrainUnit();

            if(train.coupledFront == null && train.coupledBack == null) {
                ltu.trains = new EntityRailCarBase[] {train};
                train.ltu = ltu;
                train.ltuIndex = 0;
                return ltu;
            }

            EntityRailCarBase current = train;
            EntityRailCarBase next;

            do {
                next = null;

                if(current.coupledFront != null && !brake.contains(current.coupledFront)) next = current.coupledFront;
                if(current.coupledBack != null && !brake.contains(current.coupledBack)) next = current.coupledBack;

                links.add(current);
                brake.add(current);

                current = next;

            } while(next != null);

            ltu.trains = new EntityRailCarBase[links.size()];
            for(int i = 0; i < ltu.trains.length; i++) {
                ltu.trains[i] = links.get(i);
                ltu.trains[i].ltu = ltu;
                ltu.trains[i].ltuIndex = i;
            }

            return ltu;
        }

        public void dissolveTrain() {
            for(EntityRailCarBase train : trains) {
                train.ltu = null;
                train.ltuIndex = 0;
            }
        }

        public void combineWagons() {

            if(trains.length <= 1) return;

            boolean odd = trains.length % 2 == 1;
            int centerIndex = odd ? trains.length / 2 : trains.length / 2 - 1;
            EntityRailCarBase center = trains[centerIndex];
            EntityRailCarBase prev = center;

            for(int i = centerIndex - 1; i >= 0; i--) {
                EntityRailCarBase next = trains[i];
                moveWagonTo(prev, next);
                prev = next;
            }

            prev = center;
            for(int i = centerIndex + 1; i < trains.length; i++) {
                EntityRailCarBase next = trains[i];
                moveWagonTo(prev, next);
                prev = next;
            }
        }

        public void moveWagonTo(EntityRailCarBase moveTo, EntityRailCarBase moving) {
            TrainCoupling prevCouple = moveTo.getCouplingFrom(moving);
            TrainCoupling nextCouple = moving.getCouplingFrom(moveTo);
            Vec3d prevLoc = moveTo.getCouplingPos(prevCouple);
            Vec3d nextLoc = moving.getCouplingPos(nextCouple);
            Vec3d delta = new Vec3d(prevLoc.x - nextLoc.x, 0, prevLoc.z - nextLoc.z);
            double len = delta.length();
            len = (len / (0.5D / (len * len) + 1D));
            BlockPos anchor = new BlockPos(moving.posX, moving.posY, moving.posZ);
            Vec3d trainPos = new Vec3d(moving.posX, moving.posY, moving.posZ);
            float yaw = EntityRailCarBase.generateYaw(prevLoc, nextLoc);
            Vec3d newPos = EntityRailCarBase.getRelPosAlongRail(anchor, len, moving.getGauge(), moving.world, trainPos, yaw, new MoveContext(RailCheckType.CORE, 0));

            if(newPos == null) {
                moving.derail();
                this.dissolveTrain();
                return;
            }

            moving.setPosition(newPos.x, newPos.y, newPos.z);
            anchor = moving.getCurrentAnchorPos();
            Vec3d frontPos = moving.getRelPosAlongRail(anchor, moving.getLengthSpan(), new MoveContext(RailCheckType.FRONT, moving.getCollisionSpan() - moving.getLengthSpan()));
            Vec3d backPos = moving.getRelPosAlongRail(anchor, -moving.getLengthSpan(), new MoveContext(RailCheckType.BACK, moving.getCollisionSpan() - moving.getLengthSpan()));

            if(frontPos == null || backPos == null) {
                moving.derail();
                this.dissolveTrain();
            } else {
                setRenderPos(moving, frontPos, backPos);
            }
        }

        public double getTotalSpeed() {

            EntityRailCarBase prev = trains[0];
            double totalSpeed = 0;
            double maxSpeed = Double.POSITIVE_INFINITY;
            boolean reverseTheReverse = prev.getCouplingFrom(null) == TrainCoupling.BACK;

            if(trains.length == 1) {
                return prev.getCurrentSpeed();
            }

            for(EntityRailCarBase train : this.trains) {
                boolean reverse = false;

                EntityRailCarBase conFront = train.getCoupledTo(TrainCoupling.FRONT);
                EntityRailCarBase conBack = train.getCoupledTo(TrainCoupling.BACK);

                if(conFront != null && conFront.ltuIndex > train.ltuIndex) reverse = true;
                if(conBack != null && conBack.ltuIndex < train.ltuIndex) reverse = true;

                reverse ^= reverseTheReverse;

                double speed = train.getCurrentSpeed();
                if(reverse) speed *= -1;
                totalSpeed += speed;
                maxSpeed = Math.min(maxSpeed, train.getMaxRailSpeed());
            }

            if(Math.abs(totalSpeed) > maxSpeed) {
                totalSpeed = maxSpeed * Math.signum(totalSpeed);
            }

            return totalSpeed;
        }

        public void moveTrainByApproach(double speed) {
            EntityRailCarBase previous = null;
            EntityRailCarBase first = this.trains[0];
            boolean forward = speed > 0;
            boolean order = forward ^ first.getCouplingFrom(null) == TrainCoupling.BACK;

            for(int i = order ? 0 : this.trains.length - 1; order ? i < this.trains.length : i >= 0; i += order ? 1 : -1) {
                EntityRailCarBase current = this.trains[i];

                if(previous == null) {

                    if(first == current) speed *= -1;

                    boolean inReverse = first.getCouplingFrom(null) == current.getCouplingFrom(null);
                    int sigNum = inReverse ? 1 : -1;
                    BlockPos anchor = current.getCurrentAnchorPos();

                    Vec3d frontPos = current.getRelPosAlongRail(anchor, (speed + current.getLengthSpan()) * -sigNum, new MoveContext(RailCheckType.FRONT, current.getCollisionSpan() - current.getLengthSpan()));

                    if(frontPos == null) {
                        current.derail();
                        this.dissolveTrain();
                        return;
                    } else {
                        anchor = current.getCurrentAnchorPos();
                        Vec3d corePos = current.getRelPosAlongRail(anchor, speed * -sigNum, new MoveContext(RailCheckType.CORE, 0));

                        if(corePos == null) {
                            current.derail();
                            this.dissolveTrain();
                            return;
                        }

                        current.setPosition(corePos.x, corePos.y, corePos.z);
                        Vec3d backPos = current.getRelPosAlongRail(anchor, (speed - current.getLengthSpan()) * -sigNum, new MoveContext(RailCheckType.BACK, current.getCollisionSpan() - current.getLengthSpan()));

                        if(backPos == null) {
                            current.derail();
                            this.dissolveTrain();
                            return;
                        } else {
                            setRenderPos(current, inReverse ? backPos : frontPos, inReverse ? frontPos : backPos);
                        }
                    }

                } else {
                    this.moveWagonTo(previous, current);
                }

                previous = current;
            }
        }

        public void setRenderPos(EntityRailCarBase current, Vec3d frontPos, Vec3d backPos) {
            current.renderX = (frontPos.x + backPos.x) / 2D;
            current.renderY = (frontPos.y + backPos.y) / 2D;
            current.renderZ = (frontPos.z + backPos.z) / 2D;
            current.prevRotationYaw = current.rotationYaw;
            current.rotationYaw = current.movementYaw = generateYaw(frontPos, backPos);
            Vec3d delta = new Vec3d(frontPos.x - backPos.x, frontPos.y - backPos.y, frontPos.z - backPos.z);
            current.rotationPitch = current.movementPitch = (float) (Math.asin(delta.y / delta.length()) * 180D / Math.PI);
            current.motionX = current.rotationYaw / 360D;
            current.motionY = current.rotationPitch / 360D;
            current.velocityChanged = true;
        }

        public void collideTrain(double speed) {
            EntityRailCarBase collidingTrain = speed > 0 ? trains[0] : trains[trains.length - 1];
            List<EntityRailCarBase> intersect = collidingTrain.world.getEntitiesWithinAABB(EntityRailCarBase.class, collidingTrain.getEntityBoundingBox().grow(1, 1, 1));
            EntityRailCarBase collidesWith = null;

            for(EntityRailCarBase train : intersect) {
                if(train.ltu != null && train.ltu != this) {
                    collidesWith = train;
                    break;
                }
            }

            if(collidesWith == null) return;

            Vec3d delta = new Vec3d(collidingTrain.posX - collidesWith.posX, 0, collidingTrain.posZ - collidesWith.posZ);
            double totalSpan = collidingTrain.getCollisionSpan() + collidesWith.getCollisionSpan();
            double diff = delta.length();
            if(diff > totalSpan) return;
            double push = (totalSpan - diff);

            EntityRailCarBase[][] whatever = new EntityRailCarBase[][] {{collidingTrain, collidesWith}, {collidesWith, collidingTrain}};
            for(EntityRailCarBase[] array : whatever) {
                LogicalTrainUnit ltu = array[0].ltu;
                if(ltu.trains.length == 1) {
                    Vec3d rot = new Vec3d(0, 0, array[0].getCollisionSpan())
                            .rotatePitch((float) (array[0].rotationPitch * Math.PI / 180D))
                            .rotateYaw((float) (-array[0].rotationYaw * Math.PI / 180));
                    Vec3d forward = new Vec3d(array[1].posX - (array[0].posX + rot.x), 0, array[1].posZ - (array[0].posZ + rot.z));
                    Vec3d backward = new Vec3d(array[1].posX - (array[0].posX - rot.x), 0, array[1].posZ - (array[0].posZ - rot.z));

                    if(forward.length() > backward.length()) {
                        ltu.pushForce += push;
                    } else {
                        ltu.pushForce -= push;
                    }
                } else {

                    if(array[0].ltuIndex < ltu.trains.length / 2) {
                        ltu.pushForce -= push;
                    } else {
                        ltu.pushForce += push;
                    }
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
    }
}
