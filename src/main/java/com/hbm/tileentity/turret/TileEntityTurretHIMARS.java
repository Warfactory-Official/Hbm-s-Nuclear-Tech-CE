package com.hbm.tileentity.turret;

import com.hbm.entity.projectile.EntityArtilleryRocket;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.inventory.gui.GUITurretHIMARS;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemAmmoHIMARS;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityTurretHIMARS extends TileEntityTurretBaseArtillery implements IGUIProvider {

  public enum FiringMode {
    AUTO,
    MANUAL;

    public static final FiringMode[] VALUES = values();
  }

  private static final int FIRE_DELAY_TICKS = 40;

  public FiringMode mode = FiringMode.AUTO;

  public int typeLoaded = -1;
  public int ammo = 0;
  public float crane;
  public float lastCrane;
  private int firingTimer;

  @Override
  @SideOnly(Side.CLIENT)
  public List<ItemStack> getAmmoTypesForDisplay() {

    if (ammoStacks != null) return ammoStacks;

    ammoStacks = new ArrayList<>();

    NonNullList<ItemStack> list = NonNullList.create();
    ModItems.ammo_himars.getSubItems(MainRegistry.weaponTab, list);
    this.ammoStacks.addAll(list);

    return ammoStacks;
  }

  @Override
  protected List<Integer> getAmmoList() {
    return new ArrayList<>();
  }

  @Override
  public String getDefaultName() {
    return "container.turretHIMARS";
  }

  @Override
  public long getMaxPower() {
    return 1_000_000;
  }

  @Override
  public double getBarrelLength() {
    return 0.5D;
  }

  @Override
  public double getAcceptableInaccuracy() {
    return 5D;
  }

  @Override
  public double getHeightOffset() {
    return 5D;
  }

  @Override
  public double getDecetorRange() {
    return 5000D;
  }

  @Override
  public double getDecetorGrace() {
    return 250D;
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
  public boolean doLOSCheck() {
    return false;
  }

  @Override
  protected void alignTurret() {

    Vec3d pos = this.getTurretPos();

    Vec3d delta = new Vec3d(tPos.x - pos.x, tPos.y - pos.y, tPos.z - pos.z);
    double targetYaw = -Math.atan2(delta.x, delta.z);
    double targetPitch = Math.PI / 4D;

    this.turnTowardsAngle(targetPitch, targetYaw);
  }

  private ItemStack getSpareRocket() {

    for (int i = 1; i < 10; i++) {
      if (!inventory.getStackInSlot(i).isEmpty()) {
        if (inventory.getStackInSlot(i).getItem() == ModItems.ammo_himars) {
          return inventory.getStackInSlot(i);
        }
      }
    }

    return null;
  }

  @Override
  public void update() {

    if (world.isRemote) {
      this.lastRotationPitch = this.rotationPitch;
      this.lastRotationYaw = this.rotationYaw;
      this.lastCrane = this.crane;
      this.rotationPitch = this.syncRotationPitch;
      this.rotationYaw = this.syncRotationYaw;
    }

    if (!world.isRemote) {

      if (this.mode == FiringMode.MANUAL) {
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

      if (target != null && this.mode != FiringMode.MANUAL) {
        if (!this.entityInLOS(this.target)) {
          this.target = null;
        }
      }

      if (target != null) {
        this.tPos = this.getEntityPos(target);
      } else {
        if (this.mode != FiringMode.MANUAL) {
          this.tPos = null;
        }
      }

      if (isOn() && hasPower()) {

        if (!this.hasAmmo() || this.crane > 0) {

          this.turnTowardsAngle(0, this.rotationYaw);

          if (this.aligned) {

            if (this.hasAmmo()) {
              this.crane -= 0.0125F;
            } else {
              this.crane += 0.0125F;

              if (this.crane >= 1F) {
                ItemStack available = this.getSpareRocket();

                if (available != null) {
                  ItemAmmoHIMARS.HIMARSRocket type =
                      ItemAmmoHIMARS.itemTypes[available.getItemDamage()];
                  this.typeLoaded = available.getItemDamage();
                  this.ammo = type.amount;
                  this.consumeAmmo(new RecipesCommon.ComparableStack(ModItems.ammo_himars, 1, available.getItemDamage()));
                }
              }
            }
          }

          this.crane = MathHelper.clamp(this.crane, 0F, 1F);

        } else {

          if (this.tPos != null) {
            this.alignTurret();
          }
        }

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
        this.searchTimer--;

        this.setPower(this.getPower() - this.getConsumption());

        if (this.searchTimer <= 0) {
          this.searchTimer = this.getDecetorInterval();

          if (this.target == null && this.mode != FiringMode.MANUAL) {
            this.seekNewTarget();
          }
        }
      } else {
        searchTimer = 0;
      }

      if (this.aligned && crane <= 0) {
        this.updateFiringTick();
      }

      this.power = Library.chargeTEFromItems(inventory, 10, this.power, this.getMaxPower());

      networkPackNT(250);

    } else {
      if (Math.abs(this.lastRotationYaw - this.rotationYaw) > Math.PI) {

        if (this.lastRotationYaw < this.rotationYaw) {
          this.lastRotationYaw += Math.PI * 2;
        } else {
          this.lastRotationYaw -= Math.PI * 2;
        }
      }
    }
  }

  @Override
  public void updateFiringTick() {
    if (++this.firingTimer % FIRE_DELAY_TICKS == 0) {

      if (this.hasAmmo() && this.tPos != null) {
        this.spawnShell(this.typeLoaded);
        this.ammo--;
        this.world.playSound(
            null, this.pos, HBMSoundHandler.rocketFlame, SoundCategory.BLOCKS, 25.0F, 1.0F);
      }

      if (this.mode == FiringMode.MANUAL && !this.targetQueue.isEmpty()) {
        this.targetQueue.remove(0);
        this.tPos = null;
      }
    }
  }

  public boolean hasAmmo() {
    return this.typeLoaded >= 0 && this.ammo > 0;
  }

  public void spawnShell(int type) {
    Vec3d pos = this.getTurretPos();
    Vec3 vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
    vec.rotateAroundZ((float) -this.rotationPitch);
    vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));

    EntityArtilleryRocket proj = new EntityArtilleryRocket(world);
    proj.setPositionAndRotation(
        pos.x + vec.xCoord, pos.y + vec.yCoord, pos.z + vec.zCoord, 0.0F, 0.0F);
    proj.shoot(vec.xCoord, vec.yCoord, vec.zCoord, 25F, 0.0F);

    if (this.target != null) proj.setTarget(this.target);
    else proj.setTarget(tPos.x, tPos.y, tPos.z);

    proj.setType(type);

    world.spawnEntity(proj);
  }

  @Override
  public void handleButtonPacket(int value, int meta) {
    if (meta == 5) {
      int nextOrdinal = (mode.ordinal() + 1) % FiringMode.VALUES.length;
      this.mode = FiringMode.VALUES[nextOrdinal];
      this.tPos = null;
      this.targetQueue.clear();

    } else {
      super.handleButtonPacket(value, meta);
    }
  }

  @Override
  public void serialize(ByteBuf buf) {
    super.serialize(buf);
    buf.writeShort(this.mode.ordinal());
    buf.writeShort(this.typeLoaded);
    buf.writeInt(this.ammo);
    buf.writeFloat(this.crane);
  }

  @Override
  public void deserialize(ByteBuf buf) {
    super.deserialize(buf);
    this.mode = FiringMode.VALUES[buf.readShort()];
    this.typeLoaded = buf.readShort();
    this.ammo = buf.readInt();
    this.crane = buf.readFloat();
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.mode = FiringMode.VALUES[nbt.getShort("mode")];
    this.typeLoaded = nbt.getInteger("type");
    this.ammo = nbt.getInteger("ammo");
  }

  @Override
  public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setShort("mode", (short) this.mode.ordinal());
    nbt.setInteger("type", this.typeLoaded);
    nbt.setInteger("ammo", this.ammo);
    return nbt;
  }

  @Override
  public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new ContainerTurretBase(player.inventory, this);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new GUITurretHIMARS(player.inventory, this);
  }
}
