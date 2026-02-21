package com.hbm.tileentity.turret;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.inventory.gui.GUITurretMaxwell;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.I18nUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityTurretMaxwell extends TileEntityTurretBaseNT
    implements IGUIProvider, IUpgradeInfoProvider {

  public int beam;
  public double lastDist;
  private boolean shot;

  @Override
  public String getDefaultName() {
    return "container.turretMaxwell";
  }

  @Override
  protected List<Integer> getAmmoList() {
    return null;
  }

  @SideOnly(Side.CLIENT)
  public List<ItemStack> getAmmoTypesForDisplay() {

    if (ammoStacks != null) return ammoStacks;

    ammoStacks = new ArrayList<>();

    ammoStacks.add(new ItemStack(ModItems.upgrade_speed_1));
    ammoStacks.add(new ItemStack(ModItems.upgrade_speed_2));
    ammoStacks.add(new ItemStack(ModItems.upgrade_speed_3));
    ammoStacks.add(new ItemStack(ModItems.upgrade_effect_1));
    ammoStacks.add(new ItemStack(ModItems.upgrade_effect_2));
    ammoStacks.add(new ItemStack(ModItems.upgrade_effect_3));
    ammoStacks.add(new ItemStack(ModItems.upgrade_power_1));
    ammoStacks.add(new ItemStack(ModItems.upgrade_power_2));
    ammoStacks.add(new ItemStack(ModItems.upgrade_power_3));
    ammoStacks.add(new ItemStack(ModItems.upgrade_afterburn_1));
    ammoStacks.add(new ItemStack(ModItems.upgrade_afterburn_2));
    ammoStacks.add(new ItemStack(ModItems.upgrade_afterburn_3));
    ammoStacks.add(new ItemStack(ModItems.upgrade_overdrive_1));
    ammoStacks.add(new ItemStack(ModItems.upgrade_overdrive_2));
    ammoStacks.add(new ItemStack(ModItems.upgrade_overdrive_3));
    ammoStacks.add(new ItemStack(ModItems.upgrade_5g));
    ammoStacks.add(new ItemStack(ModItems.upgrade_screm));

    return ammoStacks;
  }

  @Override
  public boolean canProvideInfo(
      ItemMachineUpgrade.UpgradeType type, int level, boolean extendedInfo) {
    return type == ItemMachineUpgrade.UpgradeType.SPEED
        || type == ItemMachineUpgrade.UpgradeType.EFFECT
        || type == ItemMachineUpgrade.UpgradeType.POWER
        || type == ItemMachineUpgrade.UpgradeType.AFTERBURN
        || type == ItemMachineUpgrade.UpgradeType.OVERDRIVE;
  }

  @Override
  public void provideInfo(
      ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
    info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.turret_maxwell));
    if (type == ItemMachineUpgrade.UpgradeType.SPEED) {
      info.add(TextFormatting.GREEN + "Damage +0." + (level * 25) + "/t");
    }
    if (type == ItemMachineUpgrade.UpgradeType.POWER) {
      info.add(
          TextFormatting.GREEN
              + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_CONSUMPTION, "-" + (level * 3) + "%"));
    }
    if (type == ItemMachineUpgrade.UpgradeType.EFFECT) {
      info.add(
          TextFormatting.GREEN
              + I18nUtil.resolveKey(IUpgradeInfoProvider.KEY_RANGE, "+" + (level * 3) + "m"));
    }
    if (type == ItemMachineUpgrade.UpgradeType.AFTERBURN) {
      info.add(TextFormatting.GREEN + "Afterburn +3s");
    }
    if (type == ItemMachineUpgrade.UpgradeType.OVERDRIVE) {
      info.add((BobMathUtil.getBlink() ? TextFormatting.RED : TextFormatting.DARK_GRAY) + "YES");
    }
  }

  @Override
  public HashMap<ItemMachineUpgrade.UpgradeType, Integer> getValidUpgrades() {
    HashMap<ItemMachineUpgrade.UpgradeType, Integer> upgrades = new HashMap<>();
    upgrades.put(ItemMachineUpgrade.UpgradeType.SPEED, 27);
    upgrades.put(ItemMachineUpgrade.UpgradeType.POWER, 27);
    upgrades.put(ItemMachineUpgrade.UpgradeType.EFFECT, 27);
    upgrades.put(ItemMachineUpgrade.UpgradeType.AFTERBURN, 27);
    upgrades.put(ItemMachineUpgrade.UpgradeType.OVERDRIVE, 27);
    return upgrades;
  }

  @Override
  public double getAcceptableInaccuracy() {
    return 2;
  }

  @Override
  public double getDecetorGrace() {
    return 5D;
  }

  @Override
  public double getTurretYawSpeed() {
    return 9D;
  }

  @Override
  public double getTurretPitchSpeed() {
    return 6D;
  }

  @Override
  public double getTurretElevation() {
    return 40D;
  }

  @Override
  public double getTurretDepression() {
    return 35D;
  }

  @Override
  public double getDecetorRange() {
    return 64D + this.greenLevel * 3;
  }

  @Override
  public long getMaxPower() {
    return 10_000_000;
  }

  @Override
  public long getConsumption() {
    return _5g ? 10 : 10000 - this.blueLevel * 300L;
  }

  @Override
  public double getBarrelLength() {
    return 2.125D;
  }

  @Override
  public double getHeightOffset() {
    return 2D;
  }

  @Override
  public void update() {
    if (world.isRemote) {

      if (this.tPos != null) {
        Vec3d pos = this.getTurretPos();
        this.lastDist = new Vec3d(tPos.x - pos.x, tPos.y - pos.y, tPos.z - pos.z).length();
      }

      if (beam > 0) beam--;
    } else {

      if (checkDelay <= 0) {
        checkDelay = 20;

        this.redLevel = 0;
        this.greenLevel = 0;
        this.blueLevel = 0;
        this.blackLevel = 0;
        this.pinkLevel = 0;
        this.screm = false;

        for (int i = 1; i < 10; i++) {
          if (!inventory.getStackInSlot(i).isEmpty()) {
            Item item = inventory.getStackInSlot(i).getItem();

            if (item == ModItems.upgrade_speed_1) redLevel += 1;
            if (item == ModItems.upgrade_speed_2) redLevel += 2;
            if (item == ModItems.upgrade_speed_3) redLevel += 3;
            if (item == ModItems.upgrade_effect_1) greenLevel += 1;
            if (item == ModItems.upgrade_effect_2) greenLevel += 2;
            if (item == ModItems.upgrade_effect_3) greenLevel += 3;
            if (item == ModItems.upgrade_power_1) blueLevel += 1;
            if (item == ModItems.upgrade_power_2) blueLevel += 2;
            if (item == ModItems.upgrade_power_3) blueLevel += 3;
            if (item == ModItems.upgrade_afterburn_1) pinkLevel += 1;
            if (item == ModItems.upgrade_afterburn_2) pinkLevel += 2;
            if (item == ModItems.upgrade_afterburn_3) pinkLevel += 3;
            if (item == ModItems.upgrade_overdrive_1) blackLevel += 1;
            if (item == ModItems.upgrade_overdrive_2) blackLevel += 2;
            if (item == ModItems.upgrade_overdrive_3) blackLevel += 3;
            if (item == ModItems.upgrade_5g) _5g = true;
            if (item == ModItems.upgrade_screm) screm = true;
          }
        }
      }

      checkDelay--;
    }
    super.update();
  }

  int redLevel;
  int greenLevel;
  int blueLevel;
  int blackLevel;
  int pinkLevel;
  boolean _5g;
  boolean screm;

  int checkDelay;

  @Override
  public void updateFiringTick() {

    long demand = this.getConsumption() * 10;

    if (this.target != null && this.getPower() >= demand) {
      if (this.target instanceof EntityPlayer
          && (((EntityPlayer) this.target).capabilities.isCreativeMode
              || ((EntityPlayer) this.target).isSpectator())) return;

      if (_5g && target instanceof EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(HbmPotion.death, 30 * 60 * 20, 0, true, false));
      } else {
        EntityDamageUtil.attackEntityFromIgnoreIFrame(
            this.target,
            ModDamageSource.microwave,
            (this.blackLevel * 10 + this.redLevel + 1F) * 0.25F);
      }

      if (pinkLevel > 0) this.target.setFire(this.pinkLevel * 3);

      if (!this.target.isEntityAlive() && this.target instanceof EntityLivingBase) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("type", "giblets");
        nbt.setInteger("ent", this.target.getEntityId());
        PacketThreading.createAllAroundThreadedPacket(
            new AuxParticlePacketNT(
                nbt,
                this.target.posX,
                this.target.posY + this.target.height * 0.5,
                this.target.posZ),
            new TargetPoint(
                this.target.dimension,
                this.target.posX,
                this.target.posY + this.target.height * 0.5,
                this.target.posZ,
                150));
        if (this.screm)
          world.playSound(
              null,
              this.target.posX,
              this.target.posY,
              this.target.posZ,
              HBMSoundHandler.screm,
              SoundCategory.BLOCKS,
              2.0F,
              0.95F + world.rand.nextFloat() * 0.2F);
        else
          world.playSound(
              null,
              this.target.posX,
              this.target.posY,
              this.target.posZ,
              SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD,
              SoundCategory.BLOCKS,
              2.0F,
              0.95F + world.rand.nextFloat() * 0.2F);
      }

      this.power -= demand;

      shot = true;
      networkPackNT(250);
      shot = false;
    }
  }

  @Override
  public void serialize(ByteBuf buf) {
    super.serialize(buf);
    buf.writeBoolean(this.shot);
  }

  @Override
  public void deserialize(ByteBuf buf) {
    super.deserialize(buf);
    if (buf.readBoolean()) {
      this.beam = 5;
    }
  }

  @Override
  public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new ContainerTurretBase(player.inventory, this);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
    return new GUITurretMaxwell(player.inventory, this);
  }
}
