package com.hbm.tileentity.machine;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerForceField;
import com.hbm.inventory.gui.GUIForceField;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.TEFFPacket;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityForceField extends TileEntityLoadedBase implements ITickable, IEnergyReceiverMK2, IGUIProvider, IConfigurableMachine {

    public static int baseCon = 1000;
    public static int radCon = 500;
    public static int shCon = 250;
    public static long maxPower = 1000000;
    public static int baseRadius = 16;
    public static int radUpgrade = 16;
    public static int shUpgrade = 50;
    public static double cooldownModif = 1;
    public static double healthRegenModif = 1;
    public ItemStackHandler inventory;
    public int health = 100;
    public int maxHealth = 100;
    public long power;
    public int powerCons;

    //private static final int[] slots_top = new int[] {0};
    //private static final int[] slots_bottom = new int[] {0};
    //private static final int[] slots_side = new int[] {0};
    public int cooldown = 0;
    public int blink = 0;
    public float radius = 16;
    public boolean isOn = false;
    public int color = 0x0000FF;
    List<Entity> outside = new ArrayList<Entity>();
    List<Entity> inside = new ArrayList<Entity>();
    private String customName;

    public String getConfigName() { return "forcefield";}

    public TileEntityForceField() {
        inventory = new ItemStackHandler(3) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
                super.onContentsChanged(slot);
            }
        };
    }

    public String getInventoryName() {
        return this.hasCustomInventoryName() ? this.customName : "container.forceField";
    }

    public boolean hasCustomInventoryName() {
        return this.customName != null && this.customName.length() > 0;
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.power = nbt.getLong("powerTime");
        this.health = nbt.getInteger("health");
        this.maxHealth = nbt.getInteger("maxHealth");
        this.cooldown = nbt.getInteger("cooldown");
        this.blink = nbt.getInteger("blink");
        this.radius = nbt.getFloat("radius");
        this.isOn = nbt.getBoolean("isOn");
        if (nbt.hasKey("inventory"))
            inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
        super.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("powerTime", power);
        nbt.setInteger("health", health);
        nbt.setInteger("maxHealth", maxHealth);
        nbt.setInteger("cooldown", cooldown);
        nbt.setInteger("blink", blink);
        nbt.setFloat("radius", radius);
        nbt.setBoolean("isOn", isOn);
        nbt.setTag("inventory", inventory.serializeNBT());
        return super.writeToNBT(nbt);
    }

    public int getHealthScaled(int i) {
        return (health * i) / Math.max(1, maxHealth);
    }

    public long getPowerScaled(long i) {
        return (power * i) / Math.max(1, maxPower);
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            updateConnections();
            int rStack = 0;
            int hStack = 0;
            radius = 16;
            maxHealth = 100;

            if (inventory.getStackInSlot(1).getItem() == ModItems.upgrade_radius) {
                rStack = inventory.getStackInSlot(1).getCount();
                radius += rStack * 16;
            }

            if (inventory.getStackInSlot(2).getItem() == ModItems.upgrade_health) {
                hStack = inventory.getStackInSlot(2).getCount();
                maxHealth += hStack * 50;
            }

            this.powerCons = this.baseCon + rStack * this.radCon + hStack * this.shCon;

            power = Library.chargeTEFromItems(inventory, 0, power, maxPower);

            if (blink > 0) {
                blink--;
                color = 0xFF0000;
            } else {
                color = 0x00FF00;
            }
        }

        if (cooldown > 0) {
            cooldown--;
        } else {
            if (health < maxHealth)
                health += (int) (((double) maxHealth / 100) * healthRegenModif);

            if (health > maxHealth)
                health = maxHealth;
        }

        if (isOn && cooldown == 0 && health > 0 && power >= powerCons) {
            doField(radius);

            if (!world.isRemote) {
                power -= powerCons;
            }
        } else {
            this.outside.clear();
            this.inside.clear();
        }

        if (!world.isRemote) {
            if (power < powerCons)
                power = 0;
        }

        if (!world.isRemote) {
            PacketDispatcher.wrapper.sendToAllTracking(new TEFFPacket(pos, radius, health, maxHealth, (int) power, isOn, color, cooldown), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 100));
        }
    }

    private int impact(Entity e) {

        double mass = e.height * e.width * e.width;
        double speed = getMotionWithFallback(e);
        return (int) (mass * speed * 50);
    }

    private void damage(int ouch) {
        health -= ouch;

        if (ouch >= (this.maxHealth / 250))
            blink = 5;

        if (health <= 0) {
            health = 0;
            cooldown = (int) (100 + radius);
        }
    }

    private void updateConnections() {
        this.trySubscribe(world, pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X);
        this.trySubscribe(world, pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X);
        this.trySubscribe(world, pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z);
        this.trySubscribe(world, pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z);
        this.trySubscribe(world, pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y);
    }

    private void doField(float rad) {

        List<Entity> oLegacy = new ArrayList<Entity>(outside);
        List<Entity> iLegacy = new ArrayList<Entity>(inside);

        outside.clear();
        inside.clear();

        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX() + 0.5 - (rad + 25), pos.getY() + 0.5 - (rad + 25), pos.getZ() + 0.5 - (rad + 25), pos.getX() + 0.5 + (rad + 25), pos.getY() + 0.5 + (rad + 25), pos.getZ() + 0.5 + (rad + 25)));

        for (Entity entity : list) {

            if (!(entity instanceof EntityPlayer) && !(entity instanceof EntityItem)) {

                double dist = Math.sqrt(Math.pow(pos.getX() + 0.5 - entity.posX, 2) + Math.pow(pos.getY() + 0.5 - entity.posY, 2) + Math.pow(pos.getZ() + 0.5 - entity.posZ, 2));

                boolean out = dist > rad;

                //if the entity has not been registered yet
                if (!oLegacy.contains(entity) && !iLegacy.contains(entity)) {
                    if (out) {
                        outside.add(entity);
                    } else {
                        inside.add(entity);
                    }

                    //if the entity has been detected before
                } else {

                    //if the entity has crossed inwards
                    if (oLegacy.contains(entity) && !out) {
                        Vec3 vec = Vec3.createVectorHelper(pos.getX() + 0.5 - entity.posX, pos.getY() + 0.5 - entity.posY, pos.getZ() + 0.5 - entity.posZ);
                        vec = vec.normalize();

                        double mx = -vec.xCoord * (rad + 1);
                        double my = -vec.yCoord * (rad + 1);
                        double mz = -vec.zCoord * (rad + 1);

                        entity.setLocationAndAngles(pos.getX() + 0.5 + mx, pos.getY() + 0.5 + my, pos.getZ() + 0.5 + mz, 0, 0);

                        double mo = Math.sqrt(Math.pow(entity.motionX, 2) + Math.pow(entity.motionY, 2) + Math.pow(entity.motionZ, 2));

                        entity.motionX = vec.xCoord * -mo;
                        entity.motionY = vec.yCoord * -mo;
                        entity.motionZ = vec.zCoord * -mo;

                        entity.posX -= entity.motionX;
                        entity.posY -= entity.motionY;
                        entity.posZ -= entity.motionZ;

                        world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.sparkShoot, SoundCategory.BLOCKS, 2.5F, 1.0F);
                        outside.add(entity);

                        if (!world.isRemote) {
                            this.damage(this.impact(entity));
                        }

                    } else

                        //if the entity has crossed outwards
                        if (iLegacy.contains(entity) && out) {
                            Vec3 vec = Vec3.createVectorHelper(pos.getX() + 0.5 - entity.posX, pos.getY() + 0.5 - entity.posY, pos.getZ() + 0.5 - entity.posZ);
                            vec = vec.normalize();

                            double mx = -vec.xCoord * (rad - 1);
                            double my = -vec.yCoord * (rad - 1);
                            double mz = -vec.zCoord * (rad - 1);

                            entity.setLocationAndAngles(pos.getX() + 0.5 + mx, pos.getY() + 0.5 + my, pos.getZ() + 0.5 + mz, 0, 0);

                            double mo = Math.sqrt(Math.pow(entity.motionX, 2) + Math.pow(entity.motionY, 2) + Math.pow(entity.motionZ, 2));

                            entity.motionX = vec.xCoord * mo;
                            entity.motionY = vec.yCoord * mo;
                            entity.motionZ = vec.zCoord * mo;

                            entity.posX -= entity.motionX;
                            entity.posY -= entity.motionY;
                            entity.posZ -= entity.motionZ;

                            world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.sparkShoot, SoundCategory.BLOCKS, 2.5F, 1.0F);
                            inside.add(entity);

                            if (!world.isRemote) {
                                this.damage(this.impact(entity));
                            }

                        } else {

                            if (out) {
                                outside.add(entity);
                            } else {
                                inside.add(entity);
                            }
                        }
                }
            }
        }
    }

    private double getMotionWithFallback(Entity e) {

        Vec3 v1 = Vec3.createVectorHelper(e.motionX, e.motionY, e.motionZ);
        Vec3 v2 = Vec3.createVectorHelper(e.posX - e.prevPosY, e.posY - e.prevPosY, e.posZ - e.prevPosZ);

        double s1 = v1.length();
        double s2 = v2.length();

        if (s1 == 0)
            return s2;

        if (s2 == 0)
            return s1;

        return Math.min(s1, s2);
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long i) {
        power = i;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        double r = radius;
        return new AxisAlignedBB(pos.getX() - r, pos.getY() - r, pos.getZ() - r, pos.getX() + 1 + r, pos.getY() + 1 + r, pos.getZ() + 1 + r);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(
                    new NTMEnergyCapabilityWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerForceField(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIForceField(player.inventory, this);
    }

    @Override
    public void readIfPresent(JsonObject obj) {
        maxPower = IConfigurableMachine.grab(obj, "L:powerCap", maxPower);
        baseCon = IConfigurableMachine.grab(obj, "I:baseConsumption", baseCon);
        radCon = IConfigurableMachine.grab(obj, "I:radiusConsumption", radCon);
        shCon = IConfigurableMachine.grab(obj, "I:shieldConsumption", shCon);
        baseRadius = IConfigurableMachine.grab(obj, "I:baseRadius", baseRadius);
        radUpgrade = IConfigurableMachine.grab(obj, "I:radiusUpgrade", radUpgrade);
        shUpgrade = IConfigurableMachine.grab(obj, "I:shieldUpgrade", shUpgrade);
        cooldownModif = IConfigurableMachine.grab(obj, "D:cooldownModifier", cooldownModif);
        healthRegenModif = IConfigurableMachine.grab(obj, "D:healthRegenModifier", healthRegenModif);
    }

    @Override
    public void writeConfig(JsonWriter writer) throws IOException {
        writer.name("L:powerCap").value(maxPower);
        writer.name("I:baseConsumption").value(baseCon);
        writer.name("I:radiusConsumption").value(radCon);
        writer.name("I:shieldConsumption").value(shCon);
        writer.name("I:baseRadius").value(baseRadius);
        writer.name("I:radiusUpgrade").value(radUpgrade);
        writer.name("I:shieldUpgrade").value(shUpgrade);
        writer.name("D:cooldownModifier").value(cooldownModif);
        writer.name("D:healthRegenModifier").value(healthRegenModif);
    }
}
