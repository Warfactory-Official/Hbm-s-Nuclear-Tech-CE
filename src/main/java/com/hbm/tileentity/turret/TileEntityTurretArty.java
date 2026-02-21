package com.hbm.tileentity.turret;

import com.hbm.entity.projectile.EntityArtilleryShell;
import com.hbm.handler.CasingEjector;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.inventory.gui.GUITurretArty;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemAmmoArty;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.CasingCreator;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.Vec3dUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityTurretArty extends TileEntityTurretBaseArtillery implements IGUIProvider {

    public static final short MODE_ARTILLERY = 0;
    public static final short MODE_CANNON = 1;
    public static final short MODE_MANUAL = 2;
    protected static CasingEjector ejector = new CasingEjector().setMotion(0, 0.6, -1).setAngleRange(0.1F, 0.1F);
    public short mode = 0;
    public double barrelPos = 0;
    public double lastBarrelPos = 0;
    int timer;
    private boolean didJustShoot = false;
    private boolean retracting = false;

    @Override
    @SideOnly(Side.CLIENT)
    public List<ItemStack> getAmmoTypesForDisplay() {

        if (ammoStacks != null)
            return ammoStacks;

        ammoStacks = new ArrayList<>();

        NonNullList<ItemStack> list = NonNullList.create();
        ModItems.ammo_arty.getSubItems(MainRegistry.weaponTab, list);
        this.ammoStacks.addAll(list);

        return ammoStacks;
    }

    @Override
    protected List<Integer> getAmmoList() {
        return new ArrayList();
    }

    @Override
    public String getDefaultName() {
        return "container.turretArty";
    }

    @Override
    public long getMaxPower() {
        return 100000;
    }

    @Override
    public double getBarrelLength() {
        return 9D;
    }

    @Override
    public double getAcceptableInaccuracy() {
        return 0;
    }

    @Override
    public double getHeightOffset() {
        return 3D;
    }

    @Override
    public double getDecetorRange() {
        return this.mode == MODE_CANNON ? 250D : 3000D;
    }

    @Override
    public double getDecetorGrace() {
        return this.mode == MODE_CANNON ? 32D : 250D;
    }

    @Override
    public double getTurretYawSpeed() {
        return 1D;
    }

    @Override
    public double getTurretPitchSpeed() {
        return 0.5D;
    }

    @Override
    public double getTurretDepression() {
        return 30D;
    }

    @Override
    public double getTurretElevation() {
        return 90D;
    }

    @Override
    public int getDecetorInterval() {
        return mode == MODE_CANNON ? 20 : 200;
    }

    @Override
    public boolean doLOSCheck() {
        return this.mode == MODE_CANNON;
    }

    @Override
    protected void alignTurret() {
        Vec3d pos = this.getTurretPos();
        Vec3d barrel = new Vec3d(this.getBarrelLength(), 0, 0);
        Vec3dUtil.rotateRoll(barrel, (float) this.rotationPitch);
        barrel.rotateYaw((float) -(this.rotationYaw + Math.PI * 0.5));
        /*
         * This is done to compensate for the barrel length, as this small deviation has a huge impact in both modes at longer ranges.
         * The consequence of this is that using the >before< angle of the barrel as an approximation can lead to problems at closer range,
         * as the math tries to properly calculate the >after< angle. This should not be a problem due to the detector grace distance being
         * rather high, but it is still important to note.
         */
        double newX = pos.x + barrel.x;
        double newY = pos.y + barrel.y;
        double newZ = pos.z + barrel.z;

        Vec3d delta = new Vec3d(tPos.x - newX, tPos.y - newY, tPos.z - newZ);
        double targetYaw = -Math.atan2(delta.x, delta.z);

        double x = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double y = delta.y;
        double v0 = getV0();
        double v02 = v0 * v0;
        double g = 9.81 * 0.05;
        double upperLower = mode == MODE_CANNON ? -1 : 1;
        double targetPitch = Math.atan((v02 + Math.sqrt(v02 * v02 - g * (g * x * x + 2 * y * v02)) * upperLower) / (g * x));


        MainRegistry.logger.info("TurretPos: " + pos + "\nBarrel: " + barrel + "\nTarget: " + tPos + "\nDelta: " + delta + "\nTargetYaw: " + targetYaw + "\nTargetPitch: " + targetPitch);
        this.turnTowardsAngle(targetPitch, targetYaw);
    }

    public double getV0() {
        return mode == MODE_CANNON ? 20D : 50D;
    }

    public ItemStack getShellLoaded() {

        for (int i = 1; i < 10; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                if (inventory.getStackInSlot(i).getItem() == ModItems.ammo_arty) {
                    return inventory.getStackInSlot(i);
                }
            }
        }

        return null;
    }

    public void consumeAmmo(Item ammo) {

        for (int i = 1; i < 10; i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ammo) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    this.inventory.setStackInSlot(i, ItemStack.EMPTY);
                }
                this.markDirty();
                return;
            }
        }
    }

    public void spawnShell(ItemStack type) {

        Vec3 pos = new Vec3(this.getTurretPos());
        Vec3 vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
        vec.rotateAroundZ((float) -this.rotationPitch);
        vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));

        EntityArtilleryShell proj = new EntityArtilleryShell(world);
        proj.setPositionAndRotation(pos.xCoord + vec.xCoord, pos.yCoord + vec.yCoord, pos.zCoord + vec.zCoord, 0.0F, 0.0F);
        proj.shoot(vec.xCoord, vec.yCoord, vec.zCoord, (float) getV0(), 0.0F);
        proj.setTarget((int) tPos.x, (int) tPos.y, (int) tPos.z);
        proj.setType(type.getItemDamage());

        if (type.getItemDamage() == 8 && type.hasTagCompound()) {
            assert type.getTagCompound() != null;
            NBTTagCompound cargo = type.getTagCompound().getCompoundTag("cargo");
            proj.setCargo(new ItemStack(cargo));
        }

        if (this.mode != MODE_CANNON)
            proj.setWhistle(true);

        world.spawnEntity(proj);

        casingDelay = this.casingDelay();
    }

    @Override
    public int casingDelay() {
        return 7;
    }

    @Override
    public void update() {

        if (world.isRemote) {
            this.lastBarrelPos = this.barrelPos;

            if (this.retracting) {
                this.barrelPos += 0.5;

                if (this.barrelPos >= 1) {
                    this.retracting = false;
                }

            } else {
                this.barrelPos -= 0.05;
                if (this.barrelPos < 0) {
                    this.barrelPos = 0;
                }
            }
            this.lastRotationPitch = this.rotationPitch;
            this.lastRotationYaw = this.rotationYaw;
            this.rotationPitch = this.syncRotationPitch;
            this.rotationYaw = this.syncRotationYaw;
        }

        if (!world.isRemote) {
            // mlbv: this is NOT how 1.7 deals with it, but I had to put it here to make the retraction work
            // I frankly don't know why
            this.didJustShoot = false;
            if (this.mode == MODE_MANUAL) {
                if (!this.targetQueue.isEmpty()) {
                    this.tPos = this.targetQueue.get(0);
                }
            } else {
                this.targetQueue.clear();
            }

            this.aligned = false;

            this.updateConnections();

            if (this.target != null && !target.isEntityAlive()) {
                this.target = null;
                this.stattrak++;
            }

            if (target != null && this.mode != MODE_MANUAL) {
                if (!this.entityInLOS(this.target)) {
                    this.target = null;
                }
            }

            if (target != null) {
                this.tPos = this.getEntityPos(target);
            } else {
                if (this.mode != MODE_MANUAL) {
                    this.tPos = null;
                }
            }

            if (isOn() && hasPower()) {

                if (tPos != null)
                    this.alignTurret();
            } else {
                this.target = null;
                this.tPos = null;
            }

            if (!isOn()) this.targetQueue.clear();

            if (this.target != null && !target.isEntityAlive()) {
                this.target = null;
                this.tPos = null;
                this.stattrak++;
            }

            if (isOn() && hasPower()) {
                searchTimer--;

                this.setPower(this.getPower() - this.getConsumption());

                if (searchTimer <= 0) {
                    searchTimer = this.getDecetorInterval();

                    if (this.target == null && this.mode != MODE_MANUAL)
                        this.seekNewTarget();
                }
            } else {
                searchTimer = 0;
            }

            if (this.aligned) {
                this.updateFiringTick();
            }

            this.power = Library.chargeTEFromItems(inventory, 10, this.power, this.getMaxPower());

            networkPackNT(250);

            if (casingDelay > 0) {
                casingDelay--;
            } else {
                spawnCasing();
            }

        } else {

            //this will fix the interpolation error when the turret crosses the 360° point
            if (Math.abs(this.lastRotationYaw - this.rotationYaw) > Math.PI) {

                if (this.lastRotationYaw < this.rotationYaw)
                    this.lastRotationYaw += Math.PI * 2;
                else
                    this.lastRotationYaw -= Math.PI * 2;
            }
        }
    }

    @Override
    public void updateFiringTick() {

        timer++;

        int delay = mode == MODE_ARTILLERY ? 300 : 40;

        if (timer % delay == 0) {

            ItemStack conf = this.getShellLoaded();

            if (conf != null) {
                cachedCasingConfig = ItemAmmoArty.itemTypes[conf.getItemDamage()].casing;
                this.spawnShell(conf);
                this.consumeAmmo(ModItems.ammo_arty);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.jeremy_fire, SoundCategory.BLOCKS, 25.0F, 1.0F);
                Vec3 pos = new Vec3(this.getTurretPos());
                Vec3 vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
                vec.rotateAroundZ((float) -this.rotationPitch);
                vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));
                this.didJustShoot = true;

                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "vanillaExt");
                data.setString("mode", "largeexplode");
                data.setFloat("size", 0F);
                data.setByte("count", (byte) 5);
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.xCoord + vec.xCoord, pos.yCoord + vec.yCoord, pos.zCoord + vec.zCoord), new TargetPoint(world.provider.getDimension(), pos.xCoord, pos.yCoord, pos.zCoord, 150));
            }

            if (this.mode == MODE_MANUAL && !this.targetQueue.isEmpty()) {
                this.targetQueue.remove(0);
                this.tPos = null;
            }
        }
    }

    @Override
    protected CasingEjector getEjector() {
        return ejector;
    }

    @Override
    protected Vec3d getCasingSpawnPos() {
        return this.getTurretPos();
    }

    @Override
    public void handleButtonPacket(int value, int meta) {
        if (meta == 5) {
            this.mode++;
            if (this.mode > 2)
                this.mode = 0;

            this.tPos = null;
            this.targetQueue.clear();

        } else {
            super.handleButtonPacket(value, meta);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeShort(mode);
        buf.writeBoolean(didJustShoot);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.mode = buf.readShort();
        this.retracting = buf.readBoolean();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.mode = nbt.getShort("mode");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setShort("mode", this.mode);
        return nbt;
    }

    @Override
    protected void spawnCasing() {

        if (cachedCasingConfig == null) return;

        Vec3d spawn = this.getCasingSpawnPos();
        float yaw = (float) Math.toDegrees(rotationYaw);
        float pitch = (float) -Math.toDegrees(this.rotationPitch);

        CasingCreator.composeEffect(world,
                spawn,
                yaw, pitch,
                -0.6, 0.3, 0,
                0.01, world.rand.nextFloat() * 20F - 10F, 0,
                cachedCasingConfig.getName(),
                true, 200, 1, 20);

        cachedCasingConfig = null;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerTurretBase(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUITurretArty(player.inventory, this);
    }

}
