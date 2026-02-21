package com.hbm.handler;

import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmLivingCapability.EntityHbmProps;
import com.hbm.capability.HbmLivingCapability.IEntityHbmProps;
import com.hbm.capability.HbmLivingProps;
import com.hbm.capability.HbmLivingProps.ContaminationEffect;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.config.WorldConfig;
import com.hbm.entity.mob.EntityCreeperNuclear;
import com.hbm.entity.mob.EntityDuck;
import com.hbm.entity.mob.EntityQuackos;
import com.hbm.entity.mob.EntityRADBeast;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IArmorModDash;
import com.hbm.interfaces.Untested;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.items.weapon.sedna.factory.ConfettiUtil;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.packet.toclient.ExtPropPacket;
import com.hbm.packet.toclient.HbmCapabilityPacket;
import com.hbm.particle.helper.FlameCreator;
import com.hbm.potion.HbmPotion;
import com.hbm.saveddata.AuxSavedData;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.world.biome.BiomeGenCraterBase;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EntityEffectHandler {
	public static void onUpdate(EntityLivingBase entity) {
        //mlbv: placed at top to prevent getting transient radiation from code below
        handleRadiationEffect(entity);

        if(!entity.world.isRemote) {
			
			if(entity.ticksExisted % 20 == 0) {
				HbmLivingProps.setRadBuf(entity, HbmLivingProps.getRadEnv(entity));
				HbmLivingProps.setRadEnv(entity, 0);
			}

            Biome biome = entity.world.getBiome(entity.getPosition());
            float radiation = 0;
            if(biome == BiomeGenCraterBase.craterOuterBiome) radiation = WorldConfig.craterBiomeOuterRad;
            if(biome == BiomeGenCraterBase.craterBiome) radiation = WorldConfig.craterBiomeRad;
            if(biome == BiomeGenCraterBase.craterInnerBiome) radiation = WorldConfig.craterBiomeInnerRad;
            if(entity.isWet()) radiation *= WorldConfig.craterBiomeWaterMult;

            if(radiation > 0) {
                ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, (double) radiation / 20D);
            }
			
			if(entity instanceof EntityPlayerMP) {
				NBTTagCompound data = new NBTTagCompound();
				IEntityHbmProps props = HbmLivingProps.getData(entity);
				props.saveNBTData(data);
				PacketThreading.createSendToThreadedPacket(new ExtPropPacket(data), (EntityPlayerMP) entity);
			}

			if(entity instanceof EntityPlayerMP playerMP) {
				HbmCapability.IHBMData cap = HbmCapability.getData(entity);

				if(cap.getShield() < cap.getEffectiveMaxShield(playerMP) && entity.ticksExisted > cap.getLastDamage() + 60) {
					int tsd = entity.ticksExisted - (cap.getLastDamage() + 60);
					cap.setShield(cap.getShield() + Math.min(cap.getEffectiveMaxShield(playerMP) - cap.getShield(), 0.005F * tsd));
				}

				if(cap.getShield() > cap.getEffectiveMaxShield(playerMP))
					cap.setShield(cap.getEffectiveMaxShield(playerMP));
				PacketThreading.createSendToThreadedPacket(new HbmCapabilityPacket(cap), playerMP);
			}
		} else {
            if(entity == MainRegistry.proxy.me()) {
                EntityPlayer player = MainRegistry.proxy.me();
                if(player != null) {
                    Biome biome = player.world.getBiome(player.getPosition());
                    if(biome == BiomeGenCraterBase.craterBiome || biome == BiomeGenCraterBase.craterInnerBiome) {
                        Random rand = player.getRNG();
                        for(int i = 0; i < 3; i++) player.world.spawnParticle(EnumParticleTypes.TOWN_AURA, player.posX + rand.nextGaussian() * 3, player.posY + rand.nextGaussian() * 2, player.posZ + rand.nextGaussian() * 3, 0, 0, 0);
                    }
                }
            }
        }

		handleContamination(entity);
		handleContagion(entity);
		handleRadiation(entity);
		handleDigamma(entity);
		handleLungDisease(entity);
		handleOil(entity);
		handlePollution(entity);
		handleTemperature(entity);

		handleDashing(entity);
		handlePlinking(entity);
	}
	
	private static void handleContamination(EntityLivingBase entity) {
		
		if(entity.world.isRemote)
			return;

        Iterator<ContaminationEffect> iterator = HbmLivingProps.getCont(entity).iterator();
        while (iterator.hasNext()) {
            ContaminationEffect con = iterator.next();
            ContaminationUtil.contaminate(entity, HazardType.RADIATION, con.ignoreArmor ? ContaminationType.RAD_BYPASS : ContaminationType.CREATIVE, con.getRad());

            con.time--;

            if (con.time <= 0)
                iterator.remove();
        }
	}
	
	private static void handleRadiation(EntityLivingBase entity) {
		
		if(ContaminationUtil.isRadImmune(entity))
			return;
		
		World world = entity.world;

		if(!world.isRemote) {
            int ix = MathHelper.floor(entity.posX);
			int iy = MathHelper.floor(entity.posY);
			int iz = MathHelper.floor(entity.posZ);

            BlockPos pos = new BlockPos(ix, iy, iz);
            double offset = ChunkRadiationManager.proxy.getRadiation(world, pos);

			Object v = CompatibilityConfig.dimensionRad.get(world.provider.getDimension());
            float background = (v instanceof Number) ? ((Number) v).floatValue() : 0f;
            double radD = Math.max(0D, offset + background);
            if (radD > 0D) {
                ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, (float) (radD / 20D));
            }

			if(entity.world.isRaining() && RadiationConfig.cont > 0 && AuxSavedData.getThunder(entity.world) > 0 && entity.world.canBlockSeeSky(pos)) {
				ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, RadiationConfig.cont * 0.0005F);
			}

			if(entity instanceof EntityPlayer player && (player.capabilities.isCreativeMode || player.isSpectator()))
				return;

			Random rand = new Random(entity.getEntityId());
			int r600 = rand.nextInt(600);
			int r1200 = rand.nextInt(1200);

			if(HbmLivingProps.getRadiation(entity) > 600 && (world.getTotalWorldTime() + r600) % 600 < 20 && canVomit(entity)) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setString("mode", "blood");
				nbt.setInteger("count", 25);
				nbt.setInteger("entity", entity.getEntityId());
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));

				if((world.getTotalWorldTime() + r600) % 600 == 1) {
					world.playSound(null, ix, iy, iz, HBMSoundHandler.vomit, SoundCategory.NEUTRAL, 1.0F, 1.0F);
					entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 60, 19));
				}

			} else if(HbmLivingProps.getRadiation(entity) > 200 && (world.getTotalWorldTime() + r1200) % 1200 < 20 && canVomit(entity)) {

				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setString("mode", "normal");
				nbt.setInteger("count", 15);
				nbt.setInteger("entity", entity.getEntityId());
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));

				if((world.getTotalWorldTime() + r1200) % 1200 == 1) {
					world.playSound(null, ix, iy, iz, HBMSoundHandler.vomit, SoundCategory.NEUTRAL, 1.0F, 1.0F);
					entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 60, 19));
				}
			}

			if(HbmLivingProps.getRadiation(entity) > 900 && (world.getTotalWorldTime() + rand.nextInt(10)) % 10 == 0) {

				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "sweat");
				nbt.setInteger("count", 1);
				nbt.setInteger("block", Block.getIdFromBlock(Blocks.REDSTONE_BLOCK));
				nbt.setInteger("entity", entity.getEntityId());
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));

			}
		} else {
            double radiation = HbmLivingProps.getRadiation(entity);

			if(entity instanceof EntityPlayer && radiation > 600) {

				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "radiation");
				nbt.setInteger("count", radiation > 900 ? 4 : radiation > 800 ? 2 : 1);
				MainRegistry.proxy.effectNT(nbt);
			}
		}
	}

    private static void handleRadiationEffect(EntityLivingBase entity) {
        World world = entity.world;
        if (world.isRemote) return;
        if (!GeneralConfig.enableRads || entity.isEntityInvulnerable(ModDamageSource.radiation) || (entity instanceof EntityPlayerMP player && player.isSpectator()))
            return;

        double eRad = HbmLivingProps.getRadiation(entity);
        if (eRad < 50) return;
        int rng = world.rand.nextInt(21000);

        if (eRad >= 200 && entity.getHealth() > 0 && entity instanceof EntityCreeper) {
            if (rng % 3 == 0) {
                EntityCreeperNuclear creep = new EntityCreeperNuclear(world);
                creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
                if (!entity.isDead && world.spawnEntity(creep)) entity.setDead();
            } else {
                entity.attackEntityFrom(ModDamageSource.radiation, 100F);
            }
            return;
        } else if (eRad >= 50 && entity instanceof EntityCow && !(entity instanceof EntityMooshroom)) {
            EntityMooshroom creep = new EntityMooshroom(world);
            creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            if (!entity.isDead && world.spawnEntity(creep)) entity.setDead();
            return;
        } else if (eRad >= 500 && entity instanceof EntityVillager vil) {
            EntityZombieVillager creep = new EntityZombieVillager(world);
            creep.setForgeProfession(vil.getProfessionForge());
            creep.setChild(vil.isChild());
            creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            if (!entity.isDead && world.spawnEntity(creep)) entity.setDead();
            return;
        } else if (eRad >= 700 && entity instanceof EntityBlaze) {
            EntityRADBeast creep = new EntityRADBeast(world);
            creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            if (!entity.isDead && world.spawnEntity(creep)) entity.setDead();
            return;
        } else if (eRad >= 800 && entity instanceof EntityHorse horsie) {
            EntityZombieHorse zomhorsie = new EntityZombieHorse(world);
            zomhorsie.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            zomhorsie.setGrowingAge(horsie.getGrowingAge());
            zomhorsie.setTemper(horsie.getTemper());
            zomhorsie.setHorseSaddled(horsie.isHorseSaddled());
            zomhorsie.setHorseTamed(horsie.isTame());
            zomhorsie.setOwnerUniqueId(horsie.getOwnerUniqueId());
            zomhorsie.makeMad();
            if (!entity.isDead && world.spawnEntity(zomhorsie)) entity.setDead();
            return;
        } else if (eRad >= 200 && entity instanceof EntityDuck) {
            EntityQuackos quacc = new EntityQuackos(world);
            quacc.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            if (!entity.isDead && world.spawnEntity(quacc)) entity.setDead();
            return;
        }

        if (eRad < 200) return;
        if (eRad > 2500000) HbmLivingProps.setRadiation(entity, 2500000);

        if (eRad >= 1000) {
            entity.attackEntityFrom(ModDamageSource.radiation, 1000F);
            HbmLivingProps.setRadiation(entity, 0);

            if (entity.getHealth() > 0) {
                entity.setHealth(0);
                entity.onDeath(ModDamageSource.radiation);
            }

            if (entity instanceof EntityPlayerMP) AdvancementManager.grantAchievement((EntityPlayerMP) entity, AdvancementManager.achRadDeath);
        } else if (eRad >= 800) {
            if (rng % 300 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 30, 0));
            if (rng % 300 == 50) entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10 * 20, 2));
            if (rng % 300 == 100) entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 10 * 20, 2));
            if (rng % 500 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 3 * 20, 2));
            if (rng % 700 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.WITHER, 3 * 20, 1));
            if (rng % 300 == 150) entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 5 * 20, 3));
            if (rng % 300 == 200) entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 5 * 20, 3));
        } else if (eRad >= 600) {
            if (rng % 300 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 30, 0));
            if (rng % 300 == 50) entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10 * 20, 2));
            if (rng % 300 == 100) entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 10 * 20, 2));
            if (rng % 500 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 3 * 20, 1));
            if (rng % 300 == 150) entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 20, 3));
            if (rng % 400 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 6 * 20, 2));
        } else if (eRad >= 400) {
            if (rng % 300 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 30, 0));
            if (rng % 500 == 50) entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 5 * 20, 0));
            if (rng % 300 == 100) entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 5 * 20, 1));
            if (rng % 500 == 150) entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 20, 2));
            if (rng % 600 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 4 * 20, 1));
        } else if (eRad >= 200) {
            if (rng % 300 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));
            if (rng % 500 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 5 * 20, 0));
            if (rng % 700 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 20, 2));
            if (rng % 800 == 0) entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 4 * 20, 0));
            if (entity instanceof EntityPlayerMP) {
                AdvancementManager.grantAchievement((EntityPlayerMP) entity, AdvancementManager.achRadPoison);
            }
        }
    }

    private static void handleDigamma(EntityLivingBase entity) {
		
		if(!entity.world.isRemote) {

            double digamma = HbmLivingProps.getDigamma(entity);
			
			if(digamma < 0.01F)
				return;

            int chance = Math.max(10 - (int) (digamma), 1);
			
			if(chance == 1 || entity.getRNG().nextInt(chance) == 0) {
				
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "sweat");
				data.setInteger("count", 1);
				data.setInteger("block", Block.getIdFromBlock(Blocks.SOUL_SAND));
				data.setInteger("entity", entity.getEntityId());
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
			}
		}
	}
	
	private static void handleContagion(EntityLivingBase entity) {
		
		World world = entity.world;
		
		if(!entity.world.isRemote) {
			
			Random rand = entity.getRNG();
			int minute = 60 * 20;
			int hour = 60 * minute;
			
			int contagion = HbmLivingProps.getContagion(entity);
			
			if(entity instanceof EntityPlayer player) {

                int randSlot = rand.nextInt(player.inventory.mainInventory.size());
				ItemStack stack = player.inventory.getStackInSlot(randSlot);
				
				if(rand.nextInt(100) == 0) {
					stack = player.inventory.armorInventory.get(rand.nextInt(4));
				}
				
				if(stack != null && !ArmorUtil.checkForHazmatOnly(player) && !ArmorRegistry.hasProtection(player, EntityEquipmentSlot.HEAD, ArmorRegistry.HazardClass.BACTERIA)) {
					
					if(contagion > 0) {
						
						if(!stack.hasTagCompound())
							stack.setTagCompound(new NBTTagCompound());
						if(!stack.getTagCompound().getBoolean("ntmContagion"))
							stack.getTagCompound().setBoolean("ntmContagion", true);
						
					} else {
						
						if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("ntmContagion")) {
							HbmLivingProps.setContagion(player, 3 * hour);
						}
					}
				}
			}
			
			if(contagion > 0) {
				HbmLivingProps.setContagion(entity, contagion - 1);
				
				//aerial transmission only happens once a second 5 minutes into the contagion
				if(contagion < (2 * hour + 55 * minute) && contagion % 20 == 0) {
					
					double range = entity.isWet() ? 16D : 2D; //avoid rain, just avoid it
					
					List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(entity, entity.getEntityBoundingBox().grow(range, range, range));
					
					for(Entity ent : list) {
						
						if(ent instanceof EntityLivingBase living) {
                            if(HbmLivingProps.getContagion(living) <= 0 && !ArmorUtil.checkForHazmatOnly(living) && !ArmorRegistry.hasProtection(living, EntityEquipmentSlot.HEAD, ArmorRegistry.HazardClass.BACTERIA)) {
								HbmLivingProps.setContagion(living, 3 * hour);
							}
						}
						
						if(ent instanceof EntityItem) {
							ItemStack stack = ((EntityItem)ent).getItem();
							
							if(!stack.hasTagCompound())
								stack.setTagCompound(new NBTTagCompound());
							
							stack.getTagCompound().setBoolean("ntmContagion", true);
						}
					}
				}
				
				//one hour in, add rare and subtle screen fuckery
				if(contagion < 2 * hour && rand.nextInt(1000) == 0) {
					entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 0));
				}
				
				//two hours in, give 'em the full blast
				if(contagion < 1 * hour && rand.nextInt(100) == 0) {
					entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 0));
					entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 300, 4));
				}
				
				//T-30 minutes, take damage every 20 seconds
				if(contagion < 30 * minute && rand.nextInt(400) == 0) {
					entity.attackEntityFrom(ModDamageSource.mku, 1F);
				}

				//T-30 minutes, start vomiting
				if(contagion < 30 * minute && (contagion + entity.getEntityId()) % 200 < 20 && canVomit(entity)) {
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("type", "vomit");
					nbt.setString("mode", "blood");
					nbt.setInteger("count", 25);
					nbt.setInteger("entity", entity.getEntityId());
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
					
					if((contagion + entity.getEntityId()) % 200 == 19)
						world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.vomit, SoundCategory.PLAYERS, 1.0F, 1.0F);
				}
				
				//T-5 minutes, take damage every 5 seconds
				if(contagion < 5 * minute && rand.nextInt(100) == 0) {
					entity.attackEntityFrom(ModDamageSource.mku, 2F);
				}

				//end of contagion, drop dead
				if(contagion == 0) {
					entity.attackEntityFrom(ModDamageSource.mku, 100000F);
				}
			}
		}
	}
	
	private static void handleLungDisease(EntityLivingBase entity) {
		
		if(entity.world.isRemote)
			return;
		
		if(entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) {
			HbmLivingProps.setBlackLung(entity, 0);
			HbmLivingProps.setAsbestos(entity, 0);
			
			return;
		} else {
			
			int bl = HbmLivingProps.getBlackLung(entity);
			
			if(bl > 0 && bl < EntityHbmProps.maxBlacklung * 0.25)
				HbmLivingProps.setBlackLung(entity, HbmLivingProps.getBlackLung(entity) - 1);
		}

		double blacklung = Math.min(HbmLivingProps.getBlackLung(entity), EntityHbmProps.maxBlacklung);
		double asbestos = Math.min(HbmLivingProps.getAsbestos(entity), EntityHbmProps.maxAsbestos);
		
		boolean coughs = blacklung / EntityHbmProps.maxBlacklung > 0.25D || asbestos / EntityHbmProps.maxAsbestos > 0.25D;
		
		if(!coughs)
			return;

		boolean coughsCoal = blacklung / EntityHbmProps.maxBlacklung > 0.5D;
		boolean coughsALotOfCoal = blacklung / EntityHbmProps.maxBlacklung > 0.8D;
		boolean coughsBlood = asbestos / EntityHbmProps.maxAsbestos > 0.75D || blacklung / EntityHbmProps.maxBlacklung > 0.75D;

		double blacklungDelta = 1D - (blacklung / (double)EntityHbmProps.maxBlacklung);
		double asbestosDelta = 1D - (asbestos / (double)EntityHbmProps.maxAsbestos);
		
		double total = 1 - (blacklungDelta * asbestosDelta);
		
		int freq = Math.max((int) (1000 - 950 * total), 20);
		
		World world = entity.world;
		Random rand = new Random(entity.getEntityId());

		if(total > 0.8D) {
			entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 6));
			entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 0));
			entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 3));
			if(rand.nextInt(250) == 0)
				entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 2));
		}
		else if(total > 0.65D) {
			entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 2));
			entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 2));
			if(rand.nextInt(500) == 0)
				entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 0));
		} 
		else if(total > 0.45D) {
			entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 1));
			entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 1));
		}
		else if(total > 0.25D) {
			entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 0));
		}
		
		if(world.getTotalWorldTime() % freq == entity.getEntityId() % freq) {
			world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.cough, SoundCategory.PLAYERS, 1.0F, 1.0F);
			
			if(coughsBlood) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setString("mode", "blood");
				nbt.setInteger("count", 5);
				nbt.setInteger("entity", entity.getEntityId());
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
			}
			
			if(coughsCoal) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setString("mode", "smoke");
				nbt.setInteger("count", coughsALotOfCoal ? 50 : 10);
				nbt.setInteger("entity", entity.getEntityId());
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
			}
		}
	}
	@Untested
	private static void handleOil(EntityLivingBase entity) {

		if(entity.world.isRemote)
			return;

		int oil = HbmLivingProps.getOil(entity);

		if(oil > 0) {

			if(entity.isBurning()) {
				HbmLivingProps.setOil(entity, 0);
				entity.world.newExplosion(null, entity.posX, entity.posY + entity.height / 2, entity.posZ, 3F, false, true);
			} else {
				HbmLivingProps.setOil(entity, oil - 1);
			}

			if(entity.ticksExisted % 5 == 0) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "sweat");
				nbt.setInteger("count", 1);
				nbt.setInteger("block", Block.getIdFromBlock(Blocks.COAL_BLOCK));
				nbt.setInteger("entity", entity.getEntityId());
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0), new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
			}
		}
	}

	private static void handlePollution(EntityLivingBase entity) {

		if(!RadiationConfig.enablePollution) return;

		if(RadiationConfig.enablePoison && !ArmorRegistry.hasProtection(entity, EntityEquipmentSlot.HEAD, ArmorRegistry.HazardClass.GAS_BLISTERING) && entity.ticksExisted % 60 == 0) {

			float poison = PollutionHandler.getPollution(entity.world, new BlockPos((int) Math.floor(entity.posX), (int) Math.floor(entity.posY + entity.getEyeHeight()), (int) Math.floor(entity.posZ)), PollutionHandler.PollutionType.POISON);

			if(poison > 10) {

				if(poison < 25) {
					entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 0));
				} else if(poison < 50) {
					entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1));
				} else {
					entity.addPotionEffect(new PotionEffect(MobEffects.WITHER, 100, 2));
				}
			}
		}

		if(RadiationConfig.enableLeadPoisoning && !ArmorRegistry.hasProtection(entity, EntityEquipmentSlot.HEAD, ArmorRegistry.HazardClass.PARTICLE_FINE) && entity.ticksExisted % 60 == 0) {

			float poison = PollutionHandler.getPollution(entity.world, new BlockPos((int) Math.floor(entity.posX), (int) Math.floor(entity.posY + entity.getEyeHeight()), (int) Math.floor(entity.posZ)), PollutionHandler.PollutionType.HEAVYMETAL);

			if(poison > 25) {

				if(poison < 50) {
					entity.addPotionEffect(new PotionEffect(HbmPotion.lead, 100, 0));
				} else if(poison < 75) {
					entity.addPotionEffect(new PotionEffect(HbmPotion.lead, 100, 2));
				} else {
					entity.addPotionEffect(new PotionEffect(HbmPotion.lead, 100, 2));
				}
			}
		}
	}

	private static void handleTemperature(Entity entity) {

		if(!(entity instanceof EntityLivingBase living)) return;
		if(entity.world.isRemote) return;

        IEntityHbmProps props = HbmLivingProps.getData(living);
		Random rand = living.getRNG();

		if(!entity.isEntityAlive()) return;

		if(living.isImmuneToFire()) {
			props.setFire(0);
			props.setPhosphorus(0);
		}

		double x = living.posX;
		double y = living.posY;
		double z = living.posZ;

		if(living.isInWater() || living.isWet()) props.setFire(0);

		if(props.getFire() > 0) {
			props.setFire(props.getFire() - 1);
			if((living.ticksExisted + living.getEntityId()) % 15 == 0) living.world.playSound(null, living.posX, living.posY + living.height / 2, living.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 1F, 1.5F + rand.nextFloat() * 0.5F);
			if((living.ticksExisted + living.getEntityId()) % 40 == 0) living.attackEntityFrom(DamageSource.ON_FIRE, 2F);
			FlameCreator.composeEffect(entity.world, x - living.width / 2 + living.width * rand.nextDouble(), y + rand.nextDouble() * living.height, z - living.width / 2 + living.width * rand.nextDouble(), FlameCreator.META_FIRE);
		}

		if(props.getPhosphorus() > 0) {
			props.setPhosphorus(props.getPhosphorus() - 1);
			if((living.ticksExisted + living.getEntityId()) % 15 == 0) living.world.playSound(null, living.posX, living.posY + living.height / 2, living.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.NEUTRAL, 1F, 1.5F + rand.nextFloat() * 0.5F);
			if((living.ticksExisted + living.getEntityId()) % 40 == 0) living.attackEntityFrom(DamageSource.ON_FIRE, 5F);
			FlameCreator.composeEffect(entity.world, x - living.width / 2 + living.width * rand.nextDouble(), y + rand.nextDouble() * living.height, z - living.width / 2 + living.width * rand.nextDouble(), FlameCreator.META_FIRE);
		}

		if(props.getBalefire() > 0) {
			props.setBalefire(props.getBalefire() - 1);
			if((living.ticksExisted + living.getEntityId()) % 15 == 0) living.world.playSound(null, living.posX, living.posY + living.height / 2, living.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.NEUTRAL, 1F, 1.5F + rand.nextFloat() * 0.5F);
			ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, 5F);
			if((living.ticksExisted + living.getEntityId()) % 20 == 0) living.attackEntityFrom(DamageSource.ON_FIRE, 5F);
			FlameCreator.composeEffect(entity.world, x - living.width / 2 + living.width * rand.nextDouble(), y + rand.nextDouble() * living.height, z - living.width / 2 + living.width * rand.nextDouble(), FlameCreator.META_BALEFIRE);
		}

		if(props.getFire() > 0 || props.getBalefire() > 0 || props.getPhosphorus() > 0) if(!entity.isEntityAlive()) ConfettiUtil.decideConfetti(living, DamageSource.ON_FIRE);
	}

	private static void handleDashing(EntityLivingBase entity) {

		//AAAAAAAAAAAAAAAAAAAAEEEEEEEEEEEEEEEEEEEE
		if(entity instanceof EntityPlayer player) {

            HbmCapability.IHBMData props = HbmCapability.getData(player);

			props.setDashCount(0);

			ArmorFSB chestplate = null;

			int armorDashCount = 0;
			int armorModDashCount = 0;

			if(ArmorFSB.hasFSBArmor(player)) {
				ItemStack plate = player.inventory.armorInventory.get(2);

				chestplate = (ArmorFSB)plate.getItem();
			}

			if(chestplate != null)
				armorDashCount = chestplate.dashCount;

			for(int armorSlot = 0; armorSlot < 4; armorSlot++) {
				ItemStack armorStack = player.inventory.armorInventory.get(armorSlot);

				if(!armorStack.isEmpty() && armorStack.getItem() instanceof ItemArmor) {

					for(int modSlot = 0; modSlot < 8; modSlot++) {
						ItemStack mod = ArmorModHandler.pryMods(armorStack)[modSlot];

						if(mod != null && mod.getItem() instanceof IArmorModDash) {
							int count = ((IArmorModDash)mod.getItem()).getDashes();
							armorModDashCount += count;
						}
					}
				}
			}

			int dashCount = armorDashCount + armorModDashCount;
			boolean dashActivated = props.getKeyPressed(EnumKeybind.DASH);

			if(dashCount * 30 < props.getStamina()) props.setStamina(dashCount * 30);

			if(dashCount > 0) {

				int perDash = 30;
				int stamina = props.getStamina();

				props.setDashCount(dashCount);

				if(props.getDashCooldown() <= 0) {

					if(dashActivated && stamina >= perDash) {

						Vec3d lookingIn = player.getLookVec();
						Vec3d strafeVec = lookingIn.rotateYaw((float)Math.PI * 0.5F);

						int forward = (int) Math.signum(player.moveForward);
						int strafe = (int) Math.signum(player.moveStrafing);

						if(forward == 0 && strafe == 0) forward = 1;

						player.addVelocity(lookingIn.x * forward + strafeVec.x * strafe, 0, lookingIn.z * forward + strafeVec.z * strafe);
						player.motionY = 0;
						player.fallDistance = 0F;
						player.playSound(HBMSoundHandler.rocketFlame, 1.0F, 1.0F);

						props.setDashCooldown(HbmCapability.dashCooldownLength);
						stamina -= perDash;
					}
				} else {
					props.setDashCooldown(props.getDashCooldown() - 1);
					props.setKeyPressed(EnumKeybind.DASH, false);
				}

				if(stamina < props.getDashCount() * perDash) {
					stamina++;

					if(stamina % perDash == perDash-1) {
						player.playSound(HBMSoundHandler.techBoop, 1.0F, (1.0F + ((1F/12F)*(stamina/perDash))));
						stamina++;
					}
				}

				props.setStamina(stamina);
			}
		}
	}

	private static void handlePlinking(Entity entity) {

		if(entity instanceof EntityPlayer player) {
            HbmCapability.IHBMData  props = HbmCapability.getData(player);

			if(props.getPlinkCooldown() > 0)
				props.setPlinkCooldown(props.getPlinkCooldown() - 1);
		}

	}

	private static boolean canVomit(Entity e) {
        return !e.isCreatureType(EnumCreatureType.WATER_CREATURE, false);
    }
}
