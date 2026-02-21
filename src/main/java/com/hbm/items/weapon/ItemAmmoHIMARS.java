package com.hbm.items.weapon;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.projectile.EntityArtilleryRocket;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.potion.HbmPotion;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemAmmoHIMARS extends Item implements IMetaItemTesr {
  public enum RocketType {
    SMALL,
    SMALL_HE,
    SMALL_WP,
    SMALL_TB,
    SMALL_LAVA,
    SMALL_MINI_NUKE,
    LARGE,
    LARGE_TB;

    public static final RocketType[] VALUES = values();
  }

  public static HIMARSRocket[] itemTypes = new HIMARSRocket[RocketType.VALUES.length];

  public ItemAmmoHIMARS(String s) {
    this.setTranslationKey(s);
    this.setRegistryName(s);
    this.setHasSubtypes(true);
    this.setCreativeTab(MainRegistry.weaponTab);
    this.setMaxStackSize(1);

    init();
    ModItems.ALL_ITEMS.add(this);
    IMetaItemTesr.INSTANCES.add(this);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items) {
    if (this.isInCreativeTab(tab)) {
      for (int i = 0; i < itemTypes.length; i++) {
        items.add(new ItemStack(this, 1, i));
      }
    }
  }

  @Override
  public String getResourceLocationAsString() {
    return getRegistryName().toString();
  }

  @Override
  public void addInformation(
      ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {

    RocketType type = RocketType.VALUES[stack.getItemDamage()];
    switch (type) {
      case SMALL:
        list.add(TextFormatting.YELLOW + "Strength: 20");
        list.add(TextFormatting.YELLOW + "Damage modifier: 3x");
        list.add(TextFormatting.BLUE + "Does not destroy blocks");
        break;
      case SMALL_HE:
        list.add(TextFormatting.YELLOW + "Strength: 20");
        list.add(TextFormatting.YELLOW + "Damage modifier: 3x");
        list.add(TextFormatting.RED + "Destroys blocks");
        break;
      case SMALL_WP:
        list.add(TextFormatting.YELLOW + "Strength: 20");
        list.add(TextFormatting.YELLOW + "Damage modifier: 3x");
        list.add(TextFormatting.RED + "Phosphorus splash");
        list.add(TextFormatting.BLUE + "Does not destroy blocks");
        break;
      case SMALL_TB:
        list.add(TextFormatting.YELLOW + "Strength: 20");
        list.add(TextFormatting.YELLOW + "Damage modifier: 10x");
        list.add(TextFormatting.RED + "Destroys blocks");
        break;
      case SMALL_MINI_NUKE:
        list.add(TextFormatting.YELLOW + "Strength: 20");
        list.add(TextFormatting.RED + "Deals nuclear damage");
        list.add(TextFormatting.RED + "Destroys blocks");
        break;
      case SMALL_LAVA:
        list.add(TextFormatting.YELLOW + "Strength: 20");
        list.add(TextFormatting.RED + "Creates volcanic lava");
        list.add(TextFormatting.RED + "Destroys blocks");
        break;
      case LARGE:
        list.add(TextFormatting.YELLOW + "Strength: 50");
        list.add(TextFormatting.YELLOW + "Damage modifier: 5x");
        list.add(TextFormatting.RED + "Destroys blocks");
        break;
      case LARGE_TB:
        list.add(TextFormatting.YELLOW + "Strength: 50");
        list.add(TextFormatting.YELLOW + "Damage modifier: 12x");
        list.add(TextFormatting.RED + "Destroys blocks");
        break;
    }
  }

  @Override
  public @NotNull String getTranslationKey(ItemStack stack) {
    return "item." + itemTypes[Math.abs(stack.getItemDamage()) % itemTypes.length].ammo_name;
  }

  @Override
  public int getSubitemCount() {
    return itemTypes.length;
  }

  @Override
  public void redirectModel() {
    IMetaItemTesr.super.redirectModel();
  }

  public abstract static class HIMARSRocket {
    public enum Type {
      Standard,
      Single
    }

    public final String ammo_name;
    public final String name;
    public final ResourceLocation texture;
    public final int amount;
    public final Type modelType;

    public HIMARSRocket(String name, Type type) {
      this.ammo_name = "ammo_himars_" + name;
      this.name = name;
      this.texture =
          new ResourceLocation(
                  Tags.MODID, "textures/models/projectiles/himars_" + name + ".png");
      this.amount = type == Type.Standard ? 6 : 1;
      this.modelType = type;
    }

    public abstract void onImpact(EntityArtilleryRocket rocket, RayTraceResult mop);

    public void onUpdate(EntityArtilleryRocket rocket) {}
  }

  private static void standardExplosion(
      EntityArtilleryRocket entity,
      RayTraceResult mop,
      float size,
      float rangeMod,
      boolean breaksBlocks,
      Block slag,
      int slagMeta) {
    Vec3 vec = Vec3.createVectorHelper(entity.motionX, entity.motionY, entity.motionZ).normalize();
    ExplosionVNT explosionVnt =
        new ExplosionVNT(
            entity.world,
            mop.hitVec.x - vec.xCoord,
            mop.hitVec.y - vec.yCoord,
            mop.hitVec.z - vec.zCoord,
            size);
    if (breaksBlocks) {
      explosionVnt.setBlockAllocator(new BlockAllocatorStandard(48));
      explosionVnt.setBlockProcessor(
          new BlockProcessorStandard()
              .setNoDrop()
              .withBlockEffect(new BlockMutatorDebris(slag, slagMeta)));
    }
    explosionVnt.setEntityProcessor(new EntityProcessorCross(7.5D).withRangeMod(rangeMod));
    explosionVnt.setPlayerProcessor(new PlayerProcessorStandard());
    explosionVnt.setSFX(new ExplosionEffectStandard());
    explosionVnt.explode();
    entity.killAndClear();
  }

  private static void standardMush(EntityArtilleryRocket entity, RayTraceResult mop, float size) {
    NBTTagCompound data = new NBTTagCompound();
    data.setString("type", "rbmkmush");
    data.setFloat("scale", size);
    PacketThreading.createAllAroundThreadedPacket(
        new AuxParticlePacketNT(data, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z),
        new NetworkRegistry.TargetPoint(
            entity.dimension, entity.posX, entity.posY, entity.posZ, 250));
  }

  private void init() {
    itemTypes[RocketType.SMALL.ordinal()] =
        new HIMARSRocket("standard", HIMARSRocket.Type.Standard) {
          public void onImpact(EntityArtilleryRocket rocket, RayTraceResult mop) {
            final Vec3d hitPos = mop.hitVec;

            standardExplosion(rocket, mop, 20F, 3F, false, ModBlocks.block_slag, 1);
            ExplosionCreator.composeEffect(
                rocket.world,
                hitPos.x + 0.5,
                hitPos.y + 0.5,
                hitPos.z + 0.5,
                15,
                5F,
                1F,
                45F,
                10,
                0,
                50,
                1F,
                3F,
                -2F,
                200);
          }
        };

    itemTypes[RocketType.SMALL_HE.ordinal()] =
        new HIMARSRocket("standard_he", HIMARSRocket.Type.Standard) {
          public void onImpact(EntityArtilleryRocket rocket, RayTraceResult mop) {
            final Vec3d hitPos = mop.hitVec;

            standardExplosion(rocket, mop, 20F, 3F, true, ModBlocks.block_slag, 1);
            ExplosionCreator.composeEffectStandard(
                rocket.world, hitPos.x + 0.5, hitPos.y + 0.5, hitPos.z + 0.5);
          }
        };
    itemTypes[RocketType.SMALL_LAVA.ordinal()] =
        new HIMARSRocket("standard_lava", HIMARSRocket.Type.Standard) {
          public void onImpact(EntityArtilleryRocket rocket, RayTraceResult mop) {
            standardExplosion(rocket, mop, 20F, 3F, true, ModBlocks.volcanic_lava_block, 0);
          }
        };
    itemTypes[RocketType.LARGE.ordinal()] =
        new HIMARSRocket("single", HIMARSRocket.Type.Single) {
          public void onImpact(EntityArtilleryRocket rocket, RayTraceResult mop) {
            final Vec3d hitPos = mop.hitVec;

            standardExplosion(rocket, mop, 50F, 5F, true, ModBlocks.block_slag, 1);
            ExplosionCreator.composeEffectLarge(
                rocket.world, hitPos.x + 0.5, hitPos.y + 0.5, hitPos.z + 0.5);
          }
        };

    itemTypes[RocketType.SMALL_MINI_NUKE.ordinal()] =
        new HIMARSRocket("standard_mini_nuke", HIMARSRocket.Type.Standard) {
          public void onImpact(EntityArtilleryRocket rocket, RayTraceResult mop) {
            rocket.killAndClear();
            // I think radius of the explosion is too high!
            rocket.world.spawnEntity(
                EntityNukeExplosionMK5.statFac(
                    rocket.world, 100, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z));
            EntityNukeTorex.statFac(rocket.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 100);
          }
        };

    itemTypes[RocketType.SMALL_WP.ordinal()] =
        new HIMARSRocket("standard_wp", HIMARSRocket.Type.Standard) {
          public void onImpact(EntityArtilleryRocket rocket, RayTraceResult mop) {
            rocket.world.playSound(
                null,
                rocket.posX,
                rocket.posY,
                rocket.posZ,
                HBMSoundHandler.explosion_medium,
                SoundCategory.BLOCKS,
                20.0F,
                0.9F + rocket.world.rand.nextFloat() * 0.2F);
            standardExplosion(rocket, mop, 20F, 3F, false, ModBlocks.block_slag, 1);
            ExplosionLarge.spawnShrapnels(
                rocket.world, (int) mop.hitVec.x, (int) mop.hitVec.y, (int) mop.hitVec.z, 30);
            ExplosionChaos.burn(
                rocket.world,
                    null, new BlockPos((int) mop.hitVec.x, (int) mop.hitVec.y, (int) mop.hitVec.z),
                20);
            int radius = 30;
            List<Entity> hit =
                rocket.world.getEntitiesWithinAABBExcludingEntity(
                    rocket,
                    new AxisAlignedBB(
                        rocket.posX - radius,
                        rocket.posY - radius,
                        rocket.posZ - radius,
                        rocket.posX + radius,
                        rocket.posY + radius,
                        rocket.posZ + radius));
            for (Entity e : hit) {
              e.setFire(5);
              if (e instanceof EntityLivingBase) {
                PotionEffect eff =
                    new PotionEffect(HbmPotion.phosphorus.delegate.get(), 30 * 20, 0, true, false);
                eff.getCurativeItems().clear();
                ((EntityLivingBase) e).addPotionEffect(eff);
              }
            }
            for (int i = 0; i < 10; i++) {
              NBTTagCompound haze = new NBTTagCompound();
              haze.setString("type", "haze");
              PacketThreading.createAllAroundThreadedPacket(
                  new AuxParticlePacketNT(
                      haze,
                      mop.hitVec.x + rocket.world.rand.nextGaussian() * 15,
                      mop.hitVec.y,
                      mop.hitVec.z + rocket.world.rand.nextGaussian() * 15),
                  new NetworkRegistry.TargetPoint(
                      rocket.dimension, rocket.posX, rocket.posY, rocket.posZ, 150));
            }
            standardMush(rocket, mop, 15);
          }
        };

    itemTypes[RocketType.SMALL_TB.ordinal()] =
        new HIMARSRocket("standard_tb", HIMARSRocket.Type.Standard) {
          public void onImpact(EntityArtilleryRocket rocket, RayTraceResult mop) {
            rocket.world.playSound(
                null,
                rocket.posX,
                rocket.posY,
                rocket.posZ,
                HBMSoundHandler.explosion_medium,
                SoundCategory.BLOCKS,
                20.0F,
                0.9F + rocket.world.rand.nextFloat() * 0.2F);
            standardExplosion(rocket, mop, 20F, 10F, true, ModBlocks.block_slag, 1);
            ExplosionLarge.spawnShrapnels(
                rocket.world, (int) mop.hitVec.x, (int) mop.hitVec.y, (int) mop.hitVec.z, 30);
            standardMush(rocket, mop, 20);
          }
        };

    itemTypes[RocketType.LARGE_TB.ordinal()] =
        new HIMARSRocket("single_tb", HIMARSRocket.Type.Single) {
          public void onImpact(EntityArtilleryRocket rocket, RayTraceResult mop) {
            rocket.world.playSound(
                null,
                rocket.posX,
                rocket.posY,
                rocket.posZ,
                HBMSoundHandler.explosion_medium,
                SoundCategory.BLOCKS,
                20.0F,
                0.9F + rocket.world.rand.nextFloat() * 0.2F);
            standardExplosion(rocket, mop, 50F, 12F, true, ModBlocks.block_slag, 1);
            ExplosionLarge.spawnShrapnels(
                rocket.world, (int) mop.hitVec.x, (int) mop.hitVec.y, (int) mop.hitVec.z, 30);
            standardMush(rocket, mop, 35);
          }
        };
  }
}
