package com.hbm.main;

import com.google.common.collect.Multimap;
import com.hbm.Tags;
import com.hbm.blocks.IStepTickReceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmCapability.IHBMData;
import com.hbm.capability.HbmLivingCapability;
import com.hbm.capability.HbmLivingProps;
import com.hbm.config.*;
import com.hbm.core.BlockMetaAir;
import com.hbm.util.CompatBlockReplacer;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.entity.mob.EntityCreeperTainted;
import com.hbm.entity.mob.EntityCyberCrab;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.entity.projectile.EntityBurningFOEQ;
import com.hbm.events.CheckLadderEvent;
import com.hbm.events.InventoryChangedEvent;
import com.hbm.handler.*;
import com.hbm.handler.neutron.NeutronHandler;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.hazard.HazardSystem;
import com.hbm.integration.groovy.HbmGroovyPropertyContainer;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.IContainerOpenEventListener;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.IEquipReceiver;
import com.hbm.items.ModItems;
import com.hbm.items.armor.*;
import com.hbm.items.food.ItemConserve;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.items.special.ItemHot;
import com.hbm.items.tool.ItemDigammaDiagnostic;
import com.hbm.items.tool.ItemGuideBook;
import com.hbm.items.weapon.ItemGunBase;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.threading.ThreadedPacket;
import com.hbm.packet.toclient.*;
import com.hbm.particle.bullet_hit.EntityHitDataHandler;
import com.hbm.particle.helper.BlackPowderCreator;
import com.hbm.potion.HbmDetox;
import com.hbm.potion.HbmPotion;
import com.hbm.tileentity.machine.TileEntityMachineRadarNT;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.tileentity.network.RequestNetwork;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.*;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.world.biome.BiomeGenCraterBase;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.BlockStatePaletteHashMap;
import net.minecraft.world.chunk.BlockStatePaletteLinear;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionApplicableEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.registries.DataSerializerEntry;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class ModEventHandler {

    public static final ResourceLocation ENT_HBM_PROP_ID = new ResourceLocation(Tags.MODID, "HBMLIVINGPROPS");
    public static final ResourceLocation DATA_LOC = new ResourceLocation(Tags.MODID, "HBMDATA");
    private static final Set<String> hashes = new HashSet();
    public static final Int2IntOpenHashMap RBMK_COL_HEIGHT_MAP = new Int2IntOpenHashMap(); // server only, to avoid sending redundant packets
    public static boolean showMessage = true;
    public static Random rand = new Random();
    private static final ForkJoinPool THREAD_POOL = ForkJoinPool.commonPool();

    static {
        RBMK_COL_HEIGHT_MAP.defaultReturnValue((int) RBMKDials.RBMKKeys.KEY_COLUMN_HEIGHT.defValue);
        hashes.add("41de5c372b0589bbdb80571e87efa95ea9e34b0d74c6005b8eab495b7afd9994");
        hashes.add("31da6223a100ed348ceb3254ceab67c9cc102cb2a04ac24de0df3ef3479b1036");
    }

    public static boolean doesArrayContain(Object[] array, Object objectCheck) {
        System.out.println("On Recipe Register");
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void replaceBlocks(ChunkEvent.Load evt) {
        // TODO: add config option
        Chunk chunk = evt.getChunk();
        for (ExtendedBlockStorage storage : chunk.getBlockStorageArray())
            replacePalette(storage);
    }

    private static void replacePalette(ExtendedBlockStorage storage) {
        if (storage == null) return;
        BlockStateContainer data = storage.data;
        if (data == null) return;
        IBlockStatePalette palette = data.palette;
        if (palette == null) return;
        if (palette instanceof BlockStatePaletteHashMap map) {
            IntIdentityHashBiMap<IBlockState> statePaletteMap = map.statePaletteMap;
            if (statePaletteMap == null) return;
            int size = statePaletteMap.size();
            for (int id = 0; id < size; id++) {
                IBlockState state = statePaletteMap.get(id);
                if (state == null) continue;
                // inheritance: BlockDummyAir extends BlockMetaAir extends BlockAir
                if (state.getBlock() instanceof BlockMetaAir) {
                    IBlockState repl = CompatBlockReplacer.replaceBlock(state);
                    statePaletteMap.put(repl, id);
                }
            }
        } else if (palette instanceof BlockStatePaletteLinear linear) {
            IBlockState[] states = linear.states;
            if (states == null) return;
            for (int i = 0; i < states.length; i++) {
                IBlockState state = states[i];
                if (state == null) continue;
                if (state.getBlock() instanceof BlockMetaAir) {
                    IBlockState repl = CompatBlockReplacer.replaceBlock(state);
                    states[i] = repl;
                }
            }
        } else if (palette == BlockStateContainer.REGISTRY_BASED_PALETTE) {
            BitArray bits = data.storage;
            if (bits == null) return;
            for (int i = 0; i < 4096; i++) {
                int rawId = bits.getAt(i);
                if (rawId == 0) continue; // AIR
                IBlockState state = Block.BLOCK_STATE_IDS.getByValue(rawId);
                if (state == null) continue;
                if (state.getBlock() instanceof BlockMetaAir) {
                    IBlockState repl = CompatBlockReplacer.replaceBlock(state);
                    int newId = Block.BLOCK_STATE_IDS.get(repl);
                    if (newId < 0) newId = 0;
                    bits.setAt(i, newId);
                }
            }
        } else {
            throw new IllegalStateException("Unknown palette format: " + palette.getClass().getName());
        }
    }

    @SubscribeEvent
    public void soundRegistering(RegistryEvent.Register<SoundEvent> evt) {
        for (SoundEvent e : HBMSoundHandler.ALL_SOUNDS) {
            evt.getRegistry().register(e);
        }
    }

    @SubscribeEvent
    public void attachRadCap(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof EntityLivingBase)
            e.addCapability(ENT_HBM_PROP_ID, new HbmLivingCapability.EntityHbmPropsProvider());
        if (e.getObject() instanceof EntityPlayer) {
            e.addCapability(DATA_LOC, new HbmCapability.HBMDataProvider());
        }
    }

    @SubscribeEvent
    public void worldUnload(WorldEvent.Unload e) {
        ClimbableRegistry.clearDimension(e.getWorld());
    }

    @SubscribeEvent
    public void potionCheck(PotionApplicableEvent e) {
        if (HbmDetox.isBlacklisted(e.getPotionEffect().getPotion()) && ArmorUtil.checkForHazmat(e.getEntityLiving()) && ArmorRegistry.hasProtection(e.getEntityLiving(), EntityEquipmentSlot.HEAD, HazardClass.BACTERIA)) {
            e.setResult(Result.DENY);
            ArmorUtil.damageGasMaskFilter(e.getEntityLiving(), 10);
        }
    }

    @SubscribeEvent
    public void enteringChunk(EnteringChunk evt) {
        if (evt.getEntity() instanceof IChunkLoader) {
            ((IChunkLoader) evt.getEntity()).loadNeighboringChunks(evt.getNewChunkX(), evt.getNewChunkZ());
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        ItemStack yeet = event.getEntityItem().getItem();

        if (yeet.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(yeet)) {

            ItemStack[] mods = ArmorModHandler.pryMods(yeet);
            ItemStack cladding = mods[ArmorModHandler.cladding];

            if (cladding != null && cladding.getItem() == ModItems.cladding_obsidian) {
                event.getEntity().setEntityInvulnerable(true);
            }
        }

        if (yeet.getItem() == ModItems.bismuth_tool) {
            event.getEntity().setEntityInvulnerable(true);
        }
    }

    @SubscribeEvent
    public void lootTableLoad(LootTableLoadEvent e) {
        //Drillgon200: Yeah we're doing this in code. Screw minecraft json.
        if (CompatibilityConfig.modLoot) {
            addWeightedRandomToLootTable(e, LootTableList.CHESTS_VILLAGE_BLACKSMITH, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.armor_polish), 1, 1, 3));
            addWeightedRandomToLootTable(e, LootTableList.CHESTS_VILLAGE_BLACKSMITH, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.bathwater), 1, 1, 1));
            addWeightedRandomToLootTable(e, LootTableList.CHESTS_ABANDONED_MINESHAFT, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.bathwater), 1, 1, 1));
            addWeightedRandomToLootTable(e, LootTableList.CHESTS_ABANDONED_MINESHAFT, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.serum), 1, 1, 5));
            addWeightedRandomToLootTable(e, LootTableList.CHESTS_SIMPLE_DUNGEON, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.heart_piece), 1, 1, 1));
            addWeightedRandomToLootTable(e, LootTableList.CHESTS_DESERT_PYRAMID, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.heart_piece), 1, 1, 1));
            addWeightedRandomToLootTable(e, LootTableList.CHESTS_JUNGLE_TEMPLE, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.heart_piece), 1, 1, 1));
            addWeightedRandomToLootTable(e, LootTableList.CHESTS_SIMPLE_DUNGEON, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.scrumpy), 1, 1, 1));
            addWeightedRandomToLootTable(e, LootTableList.CHESTS_DESERT_PYRAMID, new WeightedRandomChestContentFrom1710(new ItemStack(ModItems.scrumpy), 1, 1, 1));
        }
    }

    private void addWeightedRandomToLootTable(LootTableLoadEvent e, ResourceLocation loc, WeightedRandomChestContentFrom1710 content) {
        if (e.getName().equals(loc)) {
            LootCondition[] conds = new LootCondition[0];
            LootFunction[] funcs = new LootFunction[1];
            funcs[0] = new LootFunction(conds) {
                @Override
                public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
                    ItemStack sta = content.theItemId.copy();
                    sta.setCount(content.theMinimumChanceToGenerateItem + rand.nextInt(content.theMaximumChanceToGenerateItem - content.theMinimumChanceToGenerateItem + 1));
                    return sta;
                }
            };
            LootEntry entry = new LootEntryItem(content.theItemId.getItem(), content.itemWeight, 1, funcs, conds, content.theItemId.getTranslationKey() + "_loot");
            LootPool pool = new LootPool(new LootEntry[]{entry}, new LootCondition[]{new RandomChanceWithLooting(0.25F, 0.1F)}, new RandomValueRange(1), new RandomValueRange(0), content.theItemId.getTranslationKey() + "_loot");
            e.getTable().addPool(pool);
        }
    }

    @SubscribeEvent
    public void itemSmelted(PlayerEvent.ItemSmeltedEvent e) {

        if (!e.player.world.isRemote && e.smelting.getItem() == Items.IRON_INGOT && e.player.getRNG().nextInt(64) == 0) {

            if (!e.player.inventory.addItemStackToInventory(new ItemStack(ModItems.lodestone)))
                e.player.dropItem(new ItemStack(ModItems.lodestone), false);
            else
                e.player.inventoryContainer.detectAndSendChanges();
        }

        if (!e.player.world.isRemote && e.smelting.getItem() == ModItems.ingot_uranium && e.player.getRNG().nextInt(64) == 0) {

            if (!e.player.inventory.addItemStackToInventory(new ItemStack(ModItems.quartz_plutonium)))
                e.player.dropItem(new ItemStack(ModItems.quartz_plutonium), false);
            else
                e.player.inventoryContainer.detectAndSendChanges();
        }
    }
    @SubscribeEvent
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if(event.getStack().getItem() == ModItems.canned_conserve && EnumUtil.grabEnumSafely(
                ItemConserve.EnumFoodType.VALUES, event.getStack().getItemDamage()) == ItemConserve.EnumFoodType.JIZZ)
            AdvancementManager.grantAchievement(event.player, AdvancementManager.achC20_5);
        if(event.getStack().getItem() == Items.SLIME_BALL)
            AdvancementManager.grantAchievement(event.player, AdvancementManager.achSlimeball);
    }

    public boolean canWear(Entity entity) {
        return entity instanceof EntityZombie || entity instanceof EntitySkeleton || entity instanceof EntityVillager || entity instanceof EntityIronGolem;
    }
    // Th3_Sl1ze: maybe we should go and just fucking delete this slop?..
    @SubscribeEvent
    public void mobSpawn(LivingSpawnEvent.SpecialSpawn event) {
        if (CompatibilityConfig.mobGear) {
            EntityLivingBase entity = event.getEntityLiving();
            World world = event.getWorld();

            if (entity instanceof EntityLiving && canWear(entity)) {
                int randomArmorNumber = rand.nextInt(2 << 16);
                int randomHandNumber = rand.nextInt(256);
                EntityLiving mob = (EntityLiving) entity;
                boolean hasMainHand = !mob.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty();
                boolean hasOffHand = !mob.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).isEmpty();
                boolean hasHat = !mob.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty();
                boolean hasChest = !mob.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty();
                boolean hasLegs = !mob.getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty();
                boolean hasFeet = !mob.getItemStackFromSlot(EntityEquipmentSlot.FEET).isEmpty();

                if (!hasHat) {
                    if (rand.nextInt(64) == 0)
                        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.gas_mask_m65, 1, world.rand.nextInt(100)));
                    if (rand.nextInt(128) == 0)
                        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.gas_mask, 1, world.rand.nextInt(100)));
                    if (rand.nextInt(256) == 0)
                        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.mask_of_infamy, 1, world.rand.nextInt(100)));
                }
                if (!(hasHat || hasChest || hasLegs || hasFeet)) {
/*                    if (randomArmorNumber < 2) { //1:32768
                        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.dns_helmet, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.dns_plate, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.dns_legs, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.dns_boots, 1));
                    } else if (randomArmorNumber < 2 << 6) { //1:1024
                        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.rpa_helmet, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.rpa_plate, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.rpa_legs, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.rpa_boots, 1));
                    } else if (randomArmorNumber < 2 << 8) { //1:256                                            // MetalloloM: Man that's fucking unbalanced, people just gonna farm these instead of normal crafting. Can we have a config option for this? Like the chances of drop
                        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.ajr_helmet, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.ajr_plate, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.ajr_legs, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.ajr_boots, 1));
                    }*/ if (randomArmorNumber < 2 << 8) { //1:256
                        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.t51_helmet, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.t51_plate, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.t51_legs, 1));
                        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.t51_boots, 1));
                    } else  if (randomArmorNumber < 2 << 10) { //1:64
                        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.security_helmet, 1, world.rand.nextInt(ModItems.titanium_helmet.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.security_plate, 1, world.rand.nextInt(ModItems.titanium_plate.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.security_legs, 1, world.rand.nextInt(ModItems.titanium_legs.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.security_boots, 1, world.rand.nextInt(ModItems.titanium_boots.getMaxDamage(ItemStack.EMPTY))));
                    } else if (randomArmorNumber < 2 << 11) { //1:32
                        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.hazmat_helmet, 1, world.rand.nextInt(ModItems.hazmat_helmet.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.hazmat_plate, 1, world.rand.nextInt(ModItems.hazmat_helmet.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.hazmat_legs, 1, world.rand.nextInt(ModItems.hazmat_helmet.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.hazmat_boots, 1, world.rand.nextInt(ModItems.hazmat_helmet.getMaxDamage(ItemStack.EMPTY))));
                    } else if (randomArmorNumber < 2 << 12) { //1:16
                        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.titanium_helmet, 1, world.rand.nextInt(ModItems.titanium_helmet.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.titanium_plate, 1, world.rand.nextInt(ModItems.titanium_plate.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.titanium_legs, 1, world.rand.nextInt(ModItems.titanium_legs.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.titanium_boots, 1, world.rand.nextInt(ModItems.titanium_boots.getMaxDamage(ItemStack.EMPTY))));
                    } else if (randomArmorNumber < 2 << 13) { //1:8
                        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ModItems.steel_helmet, 1, world.rand.nextInt(ModItems.steel_helmet.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ModItems.steel_plate, 1, world.rand.nextInt(ModItems.steel_plate.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ModItems.steel_legs, 1, world.rand.nextInt(ModItems.steel_legs.getMaxDamage(ItemStack.EMPTY))));
                        entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ModItems.steel_boots, 1, world.rand.nextInt(ModItems.steel_boots.getMaxDamage(ItemStack.EMPTY))));
                    }
                }

                if (!hasMainHand) {
                    if (randomHandNumber == 0)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.pipe_lead, 1, world.rand.nextInt(100)));
                    else if (randomHandNumber == 1)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.reer_graar, 1, world.rand.nextInt(100)));
                    else if (randomHandNumber == 2)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.pipe_rusty, 1, world.rand.nextInt(100)));
                    else if (randomHandNumber == 3)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.crowbar, 1, world.rand.nextInt(100)));
                    else if (randomHandNumber == 4)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.steel_pickaxe, 1, world.rand.nextInt(300)));
                    else if (randomHandNumber == 5)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.bat, 1, world.rand.nextInt(300)));
                    else if (randomHandNumber == 6)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.bat_nail, 1, world.rand.nextInt(300)));
                    else if (randomHandNumber == 7)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.golf_club, 1, world.rand.nextInt(300)));
                    else if (randomHandNumber == 8)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.titanium_sword, 1, world.rand.nextInt(300)));
                    else if (randomHandNumber == 9)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.steel_sword, 1, world.rand.nextInt(300)));
                    else if (randomHandNumber == 10)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.stopsign));
                    else if (randomHandNumber == 11)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.sopsign));
                    else if (randomHandNumber == 12)
                        entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.chernobylsign));
                }

                if (!hasOffHand) {
                    if (rand.nextInt(128) == 0)
                        entity.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(ModItems.geiger_counter, 1));
                }
            }
        }
    }

    @SubscribeEvent
    public void decorateMob(LivingSpawnEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        World world = event.getWorld();

        if(!MobConfig.enableMobGear || entity.isChild() || world.isRemote) return;

        Map<Integer, List<WeightedRandomObject>> slotPools = new HashMap<>();

        float soot = PollutionHandler.getPollution(entity.getEntityWorld(), new BlockPos(MathHelper.floor(event.getX()), MathHelper.floor(event.getY()), MathHelper.floor(event.getZ())), PollutionHandler.PollutionType.SOOT); //uhfgfg

        if(entity instanceof EntityZombie) {
            if(world.rand.nextFloat() < 0.005F && soot > 2) { // full hazmat zombine
                MobUtil.equipFullSet(entity, ModItems.hazmat_helmet, ModItems.hazmat_plate, ModItems.hazmat_legs, ModItems.hazmat_boots);
                return;
            }
            slotPools = MobUtil.slotPoolCommon;

        } else if(entity instanceof EntitySkeleton) {
            slotPools = MobUtil.slotPoolRanged;
            ItemStack bowReplacement = getSkelegun(soot, world.rand);
            slotPools.put(0, createSlotPool(50, bowReplacement != null ? new Object[][]{{bowReplacement, 1}} : new Object[][]{}));
        }

        MobUtil.assignItemsToEntity(entity, slotPools, rand);
    }

    private List<WeightedRandomObject> createSlotPool(int nullWeight, Object[][] items) {
        List<WeightedRandomObject> pool = new ArrayList<>();
        pool.add(new WeightedRandomObject(null, nullWeight));
        for (Object[] item : items) {
            Object obj = item[0];
            int weight = (int) item[1];

            if (obj instanceof Item) {
                pool.add(new WeightedRandomObject(new ItemStack((Item) obj), weight));
            } else if (obj instanceof ItemStack) {		//lol just make it pass ItemStack aswell
                pool.add(new WeightedRandomObject(obj, weight));
            }
        }
        return pool;
    }

    private static ItemStack getSkelegun(float soot, Random rand) {
        if (!MobConfig.enableMobWeapons) return null;
        if (rand.nextDouble() > Math.log(soot) * 0.25) return null;

        ArrayList<WeightedRandomObject> pool = new ArrayList<>();

        if(soot < 0.3){
            pool.add(new WeightedRandomObject(new ItemStack(ModItems.gun_pepperbox), 5));
            pool.add(new WeightedRandomObject(null, 20));
        } else if(soot > 0.3 && soot < 1) {
            pool.addAll(MobUtil.slotPoolGuns.get(0.3));
        } else if (soot < 3) {
            pool.addAll(MobUtil.slotPoolGuns.get(1D));
        } else if (soot < 5) {
            pool.addAll(MobUtil.slotPoolGuns.get(3D));
        } else {
            pool.addAll(MobUtil.slotPoolGuns.get(5D));
        }

        WeightedRandomObject selected = WeightedRandom.getRandomItem(rand, pool);

        return selected.asStack();
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        World world = event.getWorld();
        Entity entity = event.getEntity();
        if(world.isRemote) return;
        if (entity instanceof EntityPlayerMP player) {
            int height = RBMKDials.getColumnHeight(world);
            if (height != (int) RBMKDials.RBMKKeys.KEY_COLUMN_HEIGHT.defValue) {
                PacketThreading.createSendToThreadedPacket(new SurveyPacket(height), player);
            }
        } else if (entity instanceof EntityLiving living) {
            ItemStack held = living.getHeldItem(EnumHand.MAIN_HAND);

            if (!held.isEmpty() && held.getItem() instanceof ItemGunBaseNT) {
                MobUtil.addFireTask(living);
            }
        }
    }

    @SubscribeEvent
    public void onClickSign(PlayerInteractEvent event) {

        BlockPos pos = event.getPos();
        World world = event.getWorld();

        if (!world.isRemote && world.getBlockState(pos).getBlock() == Blocks.STANDING_SIGN) {

            TileEntitySign sign = (TileEntitySign) world.getTileEntity(pos);

            String result = smoosh(sign.signText[0].getUnformattedText(), sign.signText[1].getUnformattedText(), sign.signText[2].getUnformattedText(), sign.signText[3].getUnformattedText());
            //System.out.println(result);

            if (hashes.contains(result)) {
                world.destroyBlock(pos, false);
                EntityItem entityitem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ModItems.bobmazon_hidden));
                entityitem.setPickupDelay(10);
                world.spawnEntity(entityitem);
            }
        }

    }

    private String smoosh(String s1, String s2, String s3, String s4) {

        Random rand = new Random();
        String s = "";

        byte[] b1 = s1.getBytes();
        byte[] b2 = s2.getBytes();
        byte[] b3 = s3.getBytes();
        byte[] b4 = s4.getBytes();

        if (b1.length == 0 || b2.length == 0 || b3.length == 0 || b4.length == 0)
            return "";

        s += s1;
        rand.setSeed(b1[0]);
        s += rand.nextInt(0xffffff);

        s += s2;
        rand.setSeed(rand.nextInt(0xffffff) + b2[0]);
        rand.setSeed(b2[0]);
        s += rand.nextInt(0xffffff);

        s += s3;
        rand.setSeed(rand.nextInt(0xffffff) + b3[0]);
        rand.setSeed(b3[0]);
        s += rand.nextInt(0xffffff);

        s += s4;
        rand.setSeed(rand.nextInt(0xffffff) + b4[0]);
        rand.setSeed(b4[0]);
        s += rand.nextInt(0xffffff);

        //System.out.println(s);

        return getHash(s);
    }

    private String getHash(String inp) {

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] bytes = sha256.digest(inp.getBytes());
            String str = "";

            for (int b : bytes)
                str = str + Integer.toString((b & 0xFF) + 256, 16).substring(1);

            return str;

        } catch (NoSuchAlgorithmException e) {
        }

        return "";
    }

    @SubscribeEvent
    public void chatEvent(ServerChatEvent event) {

        EntityPlayerMP player = event.getPlayer();
        String message = event.getMessage();
        //boolean conditions for the illiterate, edition 1
        //bellow you can see the header of an if-block. inside the brackets, there is a boolean statement.
        //that means nothing other than its value totaling either 'true' or 'false'
        //examples: 'true' would just mean true
        //'1 > 3' would equal false
        //'i < 10' would equal true if 'i' is smaller than 10, if equal or greater, it will result in false

        //let's start from the back:

        //this part means that the message's first character has to equal a '!': ------------------+
        //                                                                                         |
        //this is a logical AND operator: ------------------------------------------------------+  |
        //                                                                                      |  |
        //this is a reference to a field in                                                     |  |
        //ShadyUtil containing a reference UUID: -----------------------------------------+     |  |
        //                                                                                |     |  |
        //this will compare said UUID with                                                |     |  |
        //the string representation of the                                                |     |  |
        //current player's UUID: ----------+                                              |     |  |
        //                                 |                                              |     |  |
        //another AND operator: --------+  |                                              |     |  |
        //                              |  |                                              |     |  |
        //this is a reference to a      |  |                                              |     |  |
        //boolean called                |  |                                              |     |  |
        //'enableDebugMode' which is    |  |                                              |     |  |
        //only set once by the mod's    |  |                                              |     |  |
        //config and is disabled by     |  |                                              |     |  |
        //default. "debug" is not a     |  |                                              |     |  |:
        //substring of the message, nor |  |                                              |     |  |
        //something that can be toggled |  |                                              |     |  |
        //in any other way except for   |  |                                              |     |  |
        //the config file: |            |  |                                              |     |  |
        //                 V            V  V                                              V     V  V
        if (GeneralConfig.enableDebugMode && player.getUniqueID().equals(ShadyUtil.HbMinecraft) && message.startsWith("!")) {

            String[] msg = message.split(" ");

            String m = msg[0].substring(1, msg[0].length()).toLowerCase();

            if ("gv".equals(m)) {

                int id = 0;
                int size = 1;
                int meta = 0;

                if (msg.length > 1 && NumberUtils.isCreatable(msg[1])) {
                    id = (int) (double) NumberUtils.createDouble(msg[1]);
                }

                if (msg.length > 2 && NumberUtils.isCreatable(msg[2])) {
                    size = (int) (double) NumberUtils.createDouble(msg[2]);
                }

                if (msg.length > 3 && NumberUtils.isCreatable(msg[3])) {
                    meta = (int) (double) NumberUtils.createDouble(msg[3]);
                }

                Item item = Item.getItemById(id);

                if (item != null && size > 0 && meta >= 0) {
                    player.inventory.addItemStackToInventory(new ItemStack(item, size, meta));
                }
            }

            player.inventoryContainer.detectAndSendChanges();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void worldTick(WorldTickEvent event) {
        if (event.world == null || event.world.isRemote || event.phase != Phase.START) return;
        int cur = RBMKDials.getColumnHeight(event.world);
        int dim = event.world.provider.getDimension();
        if (RBMK_COL_HEIGHT_MAP.put(dim, cur) != cur) {
            //Drillgon200: Retarded hack because I'm not convinced game rules are client sync'd
            //Yup they are not LMAO
            PacketThreading.createSendToDimensionThreadedPacket(new SurveyPacket(cur), dim);
        }
        BossSpawnHandler.rollTheDice(event.world);
    }

    //mlbv: concurrent workers are safe as long as they don't interfere
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void serverTickFirst(ServerTickEvent e) {
        if (e.phase != Phase.START) return;
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(JetpackHandler::serverTick, THREAD_POOL);
        CompletableFuture<Void> f2 = CompletableFuture.runAsync(RequestNetwork::updateEntries, THREAD_POOL);
        CompletableFuture<Void> f3 = CompletableFuture.runAsync(RTTYSystem::updateBroadcastQueue, THREAD_POOL);
        CompletableFuture<Void> f4 = CompletableFuture.runAsync(TileEntityMachineRadarNT::updateSystem, THREAD_POOL);
        CompletableFuture<Void> f5 = CompletableFuture.runAsync(NeutronHandler::onServerTick, THREAD_POOL);
        CompletableFuture<Void> f6 = UniNodespace.updateNodespaceAsync(THREAD_POOL);
        CompletableFuture<Void> f7 = HazardSystem.onServerTickAsync(THREAD_POOL);
        CompletableFuture.allOf(f1, f2, f3, f4, f5, f6, f7).join();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void serverTickLast(ServerTickEvent e) {
        if (e.phase != Phase.END) return;
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(EntityHitDataHandler::updateSystem, THREAD_POOL);
        CompletableFuture<Void> f2 = RadiationSystemNT.onServerTickLast(e);
        CompletableFuture.allOf(f1, f2).join();
        NetworkHandler.flushServer(); // Flush ALL network packets.
    }

    // Drillgon200: So 1.12.2's going to ignore ISpecialArmor if the damage is
    // unblockable, huh?
    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        EntityLivingBase e = event.getEntityLiving();
        if (event.getEntityLiving() instanceof EntityPlayer player) {
            IHBMData props = HbmCapability.getData(player);
            if(props.getShield() > 0) {
                float reduce = Math.min(props.getShield(), event.getAmount());
                props.setShield(props.getShield() - reduce);
                event.setAmount(event.getAmount() - reduce);
            }
            props.setLastDamage(player.ticksExisted);
            if (ArmorUtil.checkArmor(event.getEntityLiving(), ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)) {
                event.setCanceled(true);
            }
        }

        if(HbmLivingProps.getContagion(event.getEntityLiving()) > 0 && event.getAmount() < 100)
            event.setAmount(event.getAmount() * 2F);

        // mlbv: these below does not exist on 1.7, schedule for removal?
        /// V1 ///
        if (EntityDamageUtil.wasAttackedByV1(event.getSource())) {
            EntityPlayer attacker = (EntityPlayer) event.getSource().getImmediateSource();

            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "vanillaburst");
            data.setInteger("count", (int) Math.min(e.getMaxHealth() / 2F, 250));
            data.setDouble("motion", 0.1D);
            data.setString("mode", "blockdust");
            data.setInteger("block", Block.getIdFromBlock(Blocks.REDSTONE_BLOCK));
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, e.posX, e.posY + e.height * 0.5, e.posZ), new TargetPoint(e.dimension, e.posX, e.posY, e.posZ, 50));

            if (attacker.getDistanceSq(e) < 25) {
                attacker.heal(event.getAmount() * 0.5F);
            }
        }

        /// ARMOR MODS ///
        for (int i = 2; i < 6; i++) {

            ItemStack armor = e.getItemStackFromSlot(EntityEquipmentSlot.values()[i]);

            if (!armor.isEmpty() && ArmorModHandler.hasMods(armor)) {

                for (ItemStack mod : ArmorModHandler.pryMods(armor)) {

                    if (mod != null && mod.getItem() instanceof ItemArmorMod) {
                        ((ItemArmorMod) mod.getItem()).modDamage(event, armor);
                    }
                }
            }
        }

        if(e instanceof EntityPlayer player) {

            /// FSB ARMOR ///
            if(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ArmorFSB fsb)
                fsb.handleHurt(event);

            for(ItemStack stack : player.inventory.armorInventory) {
                if(stack != null && stack.getItem() instanceof IDamageHandler) {
                    ((IDamageHandler)stack.getItem()).handleDamage(event, stack);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event) {
        EntityLivingBase e = event.getEntityLiving();

        if (e instanceof EntityPlayer player) {
            if (ArmorUtil.checkArmor(e, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)) {
                if (event.getSource() != ModDamageSource.digamma) {
                    HbmCapability.plink(player, SoundEvents.ENTITY_ITEM_BREAK, 5.0F, 1.0F + e.getRNG().nextFloat() * 0.5F);
                    event.setCanceled(true);
                }
            } else {
                if (player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ArmorFSB fsb){
                    fsb.handleAttack(event);
                }
                for(ItemStack stack : player.inventory.armorInventory) {
                    if(stack != null && stack.getItem() instanceof IAttackHandler) {
                        ((IAttackHandler)stack.getItem()).handleAttack(event, stack);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerFall(PlayerFlyableFallEvent event) {
        ArmorFSB.handleFall(event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onEntityFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayerMP playerMP)
            ArmorFSB.handleFall(playerMP);
    }

    // only for the ballistic gauntlet! contains dangerous conditional returns!
    @SubscribeEvent
    public void onPlayerPunch(AttackEntityEvent event) {

        EntityPlayer player = event.getEntityPlayer();
        ItemStack chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (!player.world.isRemote && !chestplate.isEmpty() && ArmorModHandler.hasMods(chestplate)) {

            ItemStack held = player.getHeldItemMainhand();
            if (!held.isEmpty() && held.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) return;

            ItemStack[] mods = ArmorModHandler.pryMods(chestplate);
            ItemStack servo = mods[ArmorModHandler.servos];

            if (servo != null && !servo.isEmpty() && servo.getItem() == ModItems.ballistic_gauntlet) {

                BulletConfig firedConfig = null;
                BulletConfig[] gauntletConfigs = new BulletConfig[] {XFactory12ga.g12_bp, XFactory12ga.g12_bp_magnum, XFactory12ga.g12_bp_slug, XFactory12ga.g12, XFactory12ga.g12_slug, XFactory12ga.g12_flechette, XFactory12ga.g12_magnum, XFactory12ga.g12_explosive, XFactory12ga.g12_phosphorus};

                for (BulletConfig config : gauntletConfigs) {
                    if (InventoryUtil.doesPlayerHaveAStack(player, config.ammo, true, true)) {
                        firedConfig = config;
                        break;
                    }
                }

                if (firedConfig != null) {
                    int bullets = firedConfig.projectilesMin;

                    if (firedConfig.projectilesMax > firedConfig.projectilesMin) {
                        bullets += player.getRNG().nextInt(firedConfig.projectilesMax - firedConfig.projectilesMin);
                    }

                    for (int i = 0; i < bullets; i++) {
                        EntityBulletBaseMK4 mk4 = new EntityBulletBaseMK4(player, firedConfig, 15F, 0F, -0.1875, -0.0625, 0.5);
                        player.world.spawnEntity(mk4);
                        if (i == 0 && firedConfig.blackPowder) {
                            BlackPowderCreator.composeEffect(player.world, mk4.posX, mk4.posY, mk4.posZ, mk4.motionX, mk4.motionY, mk4.motionZ, 10, 0.25F, 0.5F, 10, 0.25F);
                        }
                    }

                    player.world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.shotgunShoot, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;

        if (player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ArmorFSB fsb) {
            fsb.handleTick(event);
        }

        if(event.phase == TickEvent.Phase.START) {
            int x = MathHelper.floor(player.posX);
            int y = MathHelper.floor(player.posY - 1);
            int z = MathHelper.floor(player.posZ);
            Block b = player.world.getBlockState(new BlockPos(x, y, z)).getBlock();

            if(b instanceof IStepTickReceiver step && !player.capabilities.isFlying && player.onGround) {
                step.onPlayerStep(player.world, x, y, z, player);
            }
        }

        if (!player.world.isRemote && event.phase == Phase.START) {

            /// GHOST FIX START ///

            if (!Float.isFinite(player.getHealth()) || !Float.isFinite(player.getAbsorptionAmount())) {
                player.sendMessage(new TextComponentString("Your health has been restored!"));
                player.world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.syringeUse, SoundCategory.PLAYERS, 1.0F, 1.0F);
                player.setHealth(player.getMaxHealth());
                player.setAbsorptionAmount(0);
            }

            /// GHOST FIX END ///

            /// BETA HEALTH START ///
            if (Library.hasInventoryItem(player.inventory, ModItems.beta)) {
                if (player.getFoodStats().getFoodLevel() < 10) {
                    player.getFoodStats().setFoodLevel(10);
                }

                if (player.getFoodStats().getFoodLevel() > 10) {
                    player.heal(player.getFoodStats().getFoodLevel() - 10);
                    player.getFoodStats().setFoodLevel(10);
                }
            }
            /// BETA HEALTH END ///

            /// PU RADIATION START ///

            if(player.getUniqueID().equals(ShadyUtil.Pu_238)) {

                List<EntityLivingBase> entities = player.world.getEntitiesWithinAABB(EntityLivingBase.class, player.getEntityBoundingBox().grow(3, 3, 3));

                for(EntityLivingBase e : entities) {

                    if(e != player) {
                        e.addPotionEffect(new PotionEffect(HbmPotion.radiation, 300, 2));
                    }
                }
            }

            /// PU RADIATION END ///

            /// SYNC START ///
            if(!player.world.isRemote && player instanceof EntityPlayerMP playerMP) PacketThreading.createSendToThreadedPacket(new PermaSyncPacket(playerMP), playerMP);
            /// SYNC END ///
        }
        // Alcater addition on June 2023
        if (!player.world.isRemote && event.phase == Phase.START) {
            ItemDigammaDiagnostic.playVoices(player.world, player);
        }

        if (player.world.isRemote && event.phase == Phase.START && !player.isInvisible() && !player.isSneaking()) {

            if (player.getUniqueID().equals(ShadyUtil.Pu_238)) {
                MutableVec3d vec = new MutableVec3d(3 * rand.nextDouble(), 0, 0);
                vec.rotateRollSelf(rand.nextDouble() * Math.PI);
                vec.rotateYawSelf(rand.nextDouble() * Math.PI * 2);
                player.world.spawnParticle(EnumParticleTypes.TOWN_AURA, player.posX + vec.x, player.posY + 1 + vec.y, player.posZ + vec.z, 0.0, 0.0, 0.0);
            }
        }

        /// 1.12.2 EXCLUSIVE AKIMBO GHOST START ///
        if (player.world.isRemote && event.phase == TickEvent.Phase.START) {
            ItemStack main = player.getHeldItemMainhand();
            ItemStack off  = player.getHeldItemOffhand();

            if (player.capabilities != null && player.capabilities.isCreativeMode) {
                if (isGhost(off)) {
                    player.inventory.offHandInventory.set(0, ItemStack.EMPTY);
                }
            } else {
                if (isAkimbo(main)) {
                    if (off.isEmpty() || isGhost(off)) {
                        ItemStack ghost = makeGhostCopy(main);
                        player.inventory.offHandInventory.set(0, ghost);
                    }
                } else {
                    if (isGhost(off)) {
                        player.inventory.offHandInventory.set(0, ItemStack.EMPTY);
                    }
                }
            }
        }

        if (event.phase == Phase.END) {
            JetpackHandler.postPlayerTick(event.player);
            if(!event.player.world.isRemote){
                ItemStack main = player.getHeldItemMainhand();
                ItemStack off  = player.getHeldItemOffhand();

                if (!off.isEmpty() && isAkimbo(off)) {
                    player.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
                    if (!player.inventory.addItemStackToInventory(off)) {
                        player.dropItem(off, false);
                    }
                }

                if (isAkimbo(main) && !off.isEmpty()) {
                    player.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
                    if (!player.inventory.addItemStackToInventory(off)) {
                        player.dropItem(off, false);
                    }
                }
            }
        }
        /// AKIMBO GHOST END ///
    }

    @SubscribeEvent
    public void onPlayerInventoryChanged(InventoryChangedEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player.world.isRemote) {
            return;
        }

        switch (event.getType()) {
            case COMPLEX -> HazardSystem.schedulePlayerUpdate(player);
            case DELTA -> HazardSystem.onInventoryDelta(player, event.getSlotIndex(), event.getOldStack(), event.getNewStack());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.player.world.isRemote) {
            HazardSystem.onPlayerLogout(event.player);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        HbmLivingProps.setRadiation(event.getEntityLiving(), 0);
        if (event.getEntity().world.isRemote)
            return;

        if (event.getEntityLiving() instanceof EntityPlayer) {
            if (ArmorUtil.checkArmor((EntityPlayer) event.getEntityLiving(), ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)) {
                if (event.getSource() != ModDamageSource.digamma) {
                    event.setCanceled(true);
                    event.getEntityLiving().setHealth(event.getEntityLiving().getMaxHealth());
                }
            }
        }
        if (event.isCancelable() && event.isCanceled())
            return;
        if (GeneralConfig.enableCataclysm) {
            EntityBurningFOEQ foeq = new EntityBurningFOEQ(event.getEntity().world);
            foeq.setPositionAndRotation(event.getEntity().posX, 500, event.getEntity().posZ, 0.0F, 0.0F);
            event.getEntity().world.spawnEntity(foeq);
        }
        if (event.getEntity().getUniqueID().equals(ShadyUtil.HbMinecraft)) {
            event.getEntity().dropItem(ModItems.book_of_, 1);
        }

        if (event.getEntity().getUniqueID().equals(ShadyUtil.Alcater)) {
            event.getEntity().entityDropItem(new ItemStack(ModItems.bottle_rad).setStackDisplayName("§aAlcater's §2Neo §aNuka§r"), 0.5F);
        }

        if (event.getEntity() instanceof EntityCreeperTainted && event.getSource() == ModDamageSource.boxcar) {

            for (EntityPlayer player : event.getEntity().getEntityWorld().getEntitiesWithinAABB(EntityPlayer.class, event.getEntity().getEntityBoundingBox().grow(50, 50, 50))) {
                AdvancementManager.grantAchievement(player, AdvancementManager.bobHidden);
            }
        }

        if (!event.getEntityLiving().world.isRemote) {

            if (event.getSource() instanceof EntityDamageSource && ((EntityDamageSource) event.getSource()).getTrueSource() instanceof EntityPlayer
                    && !(((EntityDamageSource) event.getSource()).getTrueSource() instanceof FakePlayer)) {

                if (event.getEntityLiving() instanceof EntitySpider && event.getEntityLiving().getRNG().nextInt(500) == 0) {
                    event.getEntityLiving().dropItem(ModItems.spider_milk, 1);
                }

                if (event.getEntityLiving() instanceof EntityCaveSpider && event.getEntityLiving().getRNG().nextInt(100) == 0) {
                    event.getEntityLiving().dropItem(ModItems.serum, 1);
                }

                if (event.getEntityLiving() instanceof EntityAnimal && event.getEntityLiving().getRNG().nextInt(500) == 0) {
                    event.getEntityLiving().dropItem(ModItems.bandaid, 1);
                }

                if (event.getEntityLiving() instanceof IMob && event.getEntityLiving().getRNG().nextInt(1000) == 0) {
                    event.getEntityLiving().dropItem(ModItems.heart_piece, 1);
                    if(event.getEntityLiving().getRNG().nextInt(250) == 0) event.getEntityLiving().dropItem(ModItems.key_red_cracked, 1);
                    if(event.getEntityLiving().getRNG().nextInt(250) == 0) event.getEntityLiving().dropItem(ModItems.launch_code_piece, 1);
                }

                if (event.getEntityLiving() instanceof EntityCyberCrab && event.getEntityLiving().getRNG().nextInt(500) == 0) {
                    event.getEntityLiving().dropItem(ModItems.wd40, 1);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityDeathFirst(LivingDeathEvent event) {
        if (event.getEntityLiving().getEntityData().getBoolean("killedByMobSlicer")) {
            return; // without that check, if a person with shackles is killed by crucible, he will be dead AND alive at the same time
        }
        for (int i = 2; i < 6; i++) {

            ItemStack stack = event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.values()[i]);

            if (stack != null && stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {

                ItemStack revive = ArmorModHandler.pryMods(stack)[ArmorModHandler.extra];

                if (revive != null) {

                    //Classic revive
                    if (revive.getItem() instanceof ItemModRevive) {
                        revive.setItemDamage(revive.getItemDamage() + 1);

                        if (revive.getItemDamage() >= revive.getMaxDamage()) {
                            ArmorModHandler.removeMod(stack, ArmorModHandler.extra);
                        } else {
                            ArmorModHandler.applyMod(stack, revive);
                        }

                        event.getEntityLiving().setHealth(event.getEntityLiving().getMaxHealth());
                        event.getEntityLiving().addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60, 99));
                        event.setCanceled(true);
                        return;
                    }

                    //Shackles
                    if (revive.getItem() instanceof ItemModShackles && HbmLivingProps.getRadiation(event.getEntityLiving()) < 1000D) {

                        revive.setItemDamage(revive.getItemDamage() + 1);

                        int dmg = revive.getItemDamage();
                        ArmorModHandler.applyMod(stack, revive);

                        event.getEntityLiving().setHealth(event.getEntityLiving().getMaxHealth());
                        HbmLivingProps.incrementRadiation(event.getEntityLiving(), dmg * dmg);
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityDeathLast(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();

        if (EntityDamageUtil.wasAttackedByV1(event.getSource())) {

            NBTTagCompound vdat = new NBTTagCompound();
            vdat.setString("type", "giblets");
            vdat.setInteger("ent", entity.getEntityId());
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(vdat, entity.posX, entity.posY + entity.height * 0.5, entity.posZ), new TargetPoint(entity.dimension, entity.posX, entity.posY + entity.height * 0.5, entity.posZ, 150));

            entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.HOSTILE, 2.0F, 0.95F + entity.world.rand.nextFloat() * 0.2F);

            EntityPlayer attacker = (EntityPlayer) ((EntityDamageSource) event.getSource()).getImmediateSource();

            if (attacker.getDistanceSq(entity) < 100) {
                attacker.heal(entity.getMaxHealth() * 0.25F);
            }
        }

        if (entity instanceof EntityPlayer) {

            EntityPlayer player = (EntityPlayer) entity;

            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {

                ItemStack stack = player.inventory.getStackInSlot(i);

                if (stack.getItem() == ModItems.detonator_deadman) {

                    if (stack.getTagCompound() != null) {

                        int x = stack.getTagCompound().getInteger("x");
                        int y = stack.getTagCompound().getInteger("y");
                        int z = stack.getTagCompound().getInteger("z");

                        if (!player.world.isRemote && player.world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof IBomb) {

                            ((IBomb) player.world.getBlockState(new BlockPos(x, y, z)).getBlock()).explode(player.world, new BlockPos(x, y, z), player);

                            if (GeneralConfig.enableExtendedLogging)
                                MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by dead man's switch from " + player.getDisplayName() + "!");
                        }

                        player.inventory.setInventorySlotContents(i, null);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (event.isCancelable() && event.isCanceled())
            return;
        if(event.getEntityLiving() instanceof EntityCreeper creeper && creeper.getEntityData().getBoolean("hfr_defused")) {
            ItemModDefuser.defuse(creeper, null, false);
        }
        NonNullList<ItemStack> handInventory = event.getEntityLiving().handInventory;
        NonNullList<ItemStack> armorArray =event.getEntityLiving().armorArray;

        if (event.getEntityLiving() instanceof EntityPlayer && event.getEntityLiving().getHeldItemMainhand().getItem() instanceof IEquipReceiver && !ItemStack.areItemsEqual(handInventory.get(0), event.getEntityLiving().getHeldItemMainhand())) {
            ((IEquipReceiver) event.getEntityLiving().getHeldItemMainhand().getItem()).onEquip((EntityPlayer) event.getEntityLiving(), EnumHand.MAIN_HAND);
            ((IEquipReceiver)event.getEntityLiving().getHeldItemMainhand().getItem()).onEquip((EntityPlayer) event.getEntityLiving(), event.getEntityLiving().getHeldItem(EnumHand.MAIN_HAND));
        }
        if (event.getEntityLiving() instanceof EntityPlayer && event.getEntityLiving().getHeldItemOffhand().getItem() instanceof IEquipReceiver && !ItemStack.areItemsEqual(handInventory.get(1), event.getEntityLiving().getHeldItemOffhand())) {
            ((IEquipReceiver) event.getEntityLiving().getHeldItemOffhand().getItem()).onEquip((EntityPlayer) event.getEntityLiving(), EnumHand.OFF_HAND);
        }

        for (int i = 2; i < 6; i++) {

            ItemStack prev = armorArray.get(i - 2);
            ItemStack armor = event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.values()[i]);

            boolean reapply = !ItemStack.areItemStacksEqual(prev, armor);

            if (reapply) {

                if (ArmorModHandler.hasMods(prev)) {

                    for (ItemStack mod : ArmorModHandler.pryMods(prev)) {

                        if (mod != null && mod.getItem() instanceof ItemArmorMod) {

                            Multimap<String, AttributeModifier> map = ((ItemArmorMod) mod.getItem()).getModifiers(EntityEquipmentSlot.values()[i], prev);

                            if (map != null)
                                event.getEntityLiving().getAttributeMap().removeAttributeModifiers(map);
                        }
                    }
                }
            }

            if (ArmorModHandler.hasMods(armor)) {

                for (ItemStack mod : ArmorModHandler.pryMods(armor)) {

                    if (mod != null && mod.getItem() instanceof ItemArmorMod) {
                        ((ItemArmorMod) mod.getItem()).modUpdate(event.getEntityLiving(), armor);

                        if (reapply) {

                            Multimap<String, AttributeModifier> map = ((ItemArmorMod) mod.getItem()).getModifiers(EntityEquipmentSlot.values()[i], armor);

                            if (map != null)
                                event.getEntityLiving().getAttributeMap().applyAttributeModifiers(map);
                        }
                    }
                }
            }
        }

        EntityEffectHandler.onUpdate(event.getEntityLiving());
    }

    @SubscribeEvent
    public void onEntityJump(LivingJumpEvent event) {
        if (event.isCancelable() && event.isCanceled())
            return;
        if (event.getEntityLiving() instanceof EntityPlayer player){
            if (player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ArmorFSB)
                ArmorFSB.handleJump(player);
        }
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent event) { // only fired by players
        // Early validation checks
        if (event.isCancelable() && event.isCanceled()) {
            return;
        }

        EntityPlayer player = event.getPlayer();
        if (!(player instanceof EntityPlayerMP playerMP)) {
            return;
        }

        Block block = event.getState().getBlock();
        World world = event.getWorld();
        BlockPos pos = event.getPos();

        if (block == ModBlocks.stone_gneiss && !AdvancementManager.hasAdvancement(playerMP, AdvancementManager.achStratum)) {
            AdvancementManager.grantAchievement(playerMP, AdvancementManager.achStratum);
            event.setExpToDrop(500);
        }
        if (GeneralConfig.enableCoalGas && (block == Blocks.COAL_ORE || block == Blocks.COAL_BLOCK || block == ModBlocks.ore_lignite)) {// Spawn coal gas in adjacent air blocks
            for (EnumFacing dir : EnumFacing.VALUES) {
                if (world.rand.nextInt(2) == 0) {
                    BlockPos adjacentPos = pos.offset(dir);
                    DelayedTick.nextWorldTickEnd(world, w -> {
                        IBlockState adjacentState = w.getBlockState(adjacentPos);
                        if (adjacentState.getBlock().isAir(adjacentState, w, adjacentPos)) {
                            w.setBlockState(adjacentPos, ModBlocks.gas_coal.getDefaultState(), 3);
                        }
                    });
                }
            }
        }
        if (RadiationConfig.enablePollution && RadiationConfig.enableLeadFromBlocks && !ArmorRegistry.hasProtection(player, EntityEquipmentSlot.HEAD, HazardClass.PARTICLE_FINE)) {
            float metalPollution = PollutionHandler.getPollution(world, pos, PollutionHandler.PollutionType.HEAVYMETAL);
            if (!(metalPollution < 5.0f)) {
                int amplifier;
                if (metalPollution < 10.0f) {
                    amplifier = 0;
                } else if (metalPollution < 25.0f) {
                    amplifier = 1;
                } else {
                    amplifier = 2;
                }
                player.addPotionEffect(new PotionEffect(HbmPotion.lead, 100, amplifier));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP player) {

            if (GeneralConfig.enableMOTD) {
                player.sendMessage(new TextComponentString("Loaded world with Hbm's Nuclear Tech Mod " + Tags.VERSION + " for Minecraft 1.12.2!"));
                if (HTTPHandler.newVersion && GeneralConfig.changelog) {
                    player.sendMessage(new TextComponentTranslation("chat.newver", HTTPHandler.versionNumber));
                    player.sendMessage(new TextComponentString("Click ")
                            .setStyle(new Style().setColor(TextFormatting.YELLOW))
                            .appendSibling(new TextComponentString("[here]")
                                    .setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/ntm-ce/versions")).setUnderlined(Boolean.TRUE).setColor(TextFormatting.RED))
                            ).appendSibling(new TextComponentString(" to download!").setStyle(new Style().setColor(TextFormatting.YELLOW)))
                    );
                }
            }

            if (GeneralConfig.duckButton) {
                if (!player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getBoolean("hasDucked")) {
                    PacketDispatcher.sendTo(new PlayerInformPacket("chat.duck"), player);
                }
            }

            if(GeneralConfig.enableGuideBook) {
                IHBMData props = HbmCapability.getData(player);

                if(!props.hasReceivedBook()) {
                    player.inventory.addItemStackToInventory(new ItemStack(ModItems.book_guide, 1, ItemGuideBook.BookType.STARTER.ordinal()));
                    player.inventoryContainer.detectAndSendChanges();
                    props.setReceivedBook(true);
                }
            }

            if(GeneralConfig.enableServerRecipeSync) {
                File recDir = new File(MainRegistry.configDir.getAbsolutePath() + File.separatorChar + "hbmRecipes");

                MainRegistry.logger.info("Sending recipes to client!");

                boolean hasSent = false;

                for(SerializableRecipe recipe : SerializableRecipe.recipeHandlers) {
                    File recFile = new File(recDir.getAbsolutePath() + File.separatorChar + recipe.getFileName());
                    if(recFile.exists() && recFile.isFile()) {
                        MainRegistry.logger.info("Sending recipe file: " + recFile.getName());
                        ThreadedPacket message = new SerializableRecipePacket(recFile);
                        PacketThreading.createSendToThreadedPacket(message, player);
                        hasSent = true;
                    }
                }

                if(hasSent) {
                    ThreadedPacket message = new SerializableRecipePacket(true);
                    PacketThreading.createSendToThreadedPacket(message, player);
                }
            }

            if (Loader.isModLoaded(Compat.ModIds.GROOVY_SCRIPT)) {
                HbmGroovyPropertyContainer.sendRecipeOverridesToPlayer(player);
            }
        }
    }

    @SubscribeEvent
    public void worldLoad(WorldEvent.Load e) {
        JetpackHandler.worldLoad(e);
    }

    @SubscribeEvent
    public void worldSave(WorldEvent.Save e) {
        JetpackHandler.worldSave(e);
    }

    @SubscribeEvent
    public void onDataSerializerRegister(RegistryEvent.Register<DataSerializerEntry> evt) {
        evt.getRegistry().register(new DataSerializerEntry(MissileStruct.SERIALIZER).setRegistryName(new ResourceLocation(Tags.MODID, "missile_struct")));
    }

    @SubscribeEvent
    public void anvilUpdateEvent(AnvilUpdateEvent event) {

        if (event.getLeft().getItem() instanceof ItemGunBase && event.getRight().getItem() == Items.ENCHANTED_BOOK) {

            event.setOutput(event.getLeft().copy());

            Map<Enchantment, Integer> mapright = EnchantmentHelper.getEnchantments(event.getRight());
            Iterator<Entry<Enchantment, Integer>> itr = mapright.entrySet().iterator();

            while (itr.hasNext()) {
                Entry<Enchantment, Integer> entry = itr.next();
                Enchantment e = entry.getKey();
                int j = entry.getValue();

                EnchantmentUtil.removeEnchantment(event.getOutput(), e);
                EnchantmentUtil.addEnchantment(event.getOutput(), e, j);
            }

            event.setCost(10);
        }
        if (event.getLeft().getItem() == ModItems.ingot_meteorite && event.getRight().getItem() == ModItems.ingot_meteorite &&
                event.getLeft().getCount() == 1 && event.getRight().getCount() == 1) {

            double h1 = ItemHot.getHeat(event.getLeft());
            double h2 = ItemHot.getHeat(event.getRight());

            if (h1 >= 0.5 && h2 >= 0.5) {

                ItemStack out = new ItemStack(ModItems.ingot_meteorite_forged);
                ItemHot.heatUp(out, (h1 + h2) / 2D);
                event.setOutput(out);
                event.setCost(10);
            }
        }

        if (event.getLeft().getItem() == ModItems.ingot_meteorite_forged && event.getRight().getItem() == ModItems.ingot_meteorite_forged &&
                event.getLeft().getCount() == 1 && event.getRight().getCount() == 1) {

            double h1 = ItemHot.getHeat(event.getLeft());
            double h2 = ItemHot.getHeat(event.getRight());

            if (h1 >= 0.5 && h2 >= 0.5) {

                ItemStack out = new ItemStack(ModItems.blade_meteorite);
                ItemHot.heatUp(out, (h1 + h2) / 2D);
                event.setOutput(out);
                event.setCost(30);
            }
        }

        if (event.getLeft().getItem() == ModItems.meteorite_sword_seared && event.getRight().getItem() == ModItems.ingot_meteorite_forged &&
                event.getLeft().getCount() == 1 && event.getRight().getCount() == 1) {

            double h2 = ItemHot.getHeat(event.getRight());

            if (h2 >= 0.5) {

                ItemStack out = new ItemStack(ModItems.meteorite_sword_reforged);
                event.setOutput(out);
                event.setCost(50);
            }
        }

        if (event.getLeft().getItem() == ModItems.ingot_steel_dusted && event.getRight().getItem() == ModItems.ingot_steel_dusted &&
                event.getLeft().getCount() == event.getRight().getCount()) {

            double h1 = ItemHot.getHeat(event.getLeft());
            double h2 = ItemHot.getHeat(event.getRight());

            if (h2 >= 0.5) {

                int i1 = event.getLeft().getItemDamage();
                int i2 = event.getRight().getItemDamage();

                int i3 = Math.min(i1, i2) + 1;

                boolean done = i3 >= 10;

                ItemStack out;
                if (done) {
                    out = new ItemStack(ModItems.ingot_chainsteel, event.getLeft().getCount(), 0);
                } else {
                    out = new ItemStack(ModItems.ingot_steel_dusted, event.getLeft().getCount(), i3);
                }

                ItemHot.heatUp(out, done ? 1D : (h1 + h2) / 2D);
                event.setOutput(out);
                event.setCost(event.getLeft().getCount());
            }
        }
    }

    @SubscribeEvent
    public void onFoodEaten(LivingEntityUseItemEvent.Finish event) {

        ItemStack stack = event.getItem();

        if (stack != null && stack.getItem() instanceof ItemFood) {

            if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("ntmCyanide")) {
                for (int i = 0; i < 10; i++) {
                    event.getEntityLiving().attackEntityFrom(rand.nextBoolean() ? ModDamageSource.euthanizedSelf : ModDamageSource.euthanizedSelf2, 1000);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {

        EntityPlayer player = event.player;

        if (player.getDisplayName().getUnformattedText().equals("Dr_Nostalgia") && !player.world.isRemote) {

            if (!Library.hasInventoryItem(player.inventory, ModItems.hat))
                player.inventory.addItemStackToInventory(new ItemStack(ModItems.hat));

            if (!Library.hasInventoryItem(player.inventory, ModItems.beta))
                player.inventory.addItemStackToInventory(new ItemStack(ModItems.beta));
        }
    }

    // TODO should probably use these.

    @SubscribeEvent
    public void craftingRegister(RegistryEvent.Register<IRecipe> e) {
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("Memory usage before: " + mem);
        CraftingManager.hack = e;
        CraftingManager.init();
        // Load compatibility for OC.
        // mlbv: this would throw an Exception if called at PostInit, at that time hack would be null.
        CompatHandler.init();
        CraftingManager.hack = null;
        mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("Memory usage after: " + mem);
    }

    @SubscribeEvent
    public void onItemRegister(RegistryEvent.Register<Item> evt) {
    }

    @SubscribeEvent
    public void onBlockRegister(RegistryEvent.Register<Block> evt) {
    }

    @SubscribeEvent
    public void onRecipeRegister(RegistryEvent.Register<IRecipe> evt) {
        IRecipe[] recipes = new IRecipe[12];
        IRecipe recipe = null;
        doesArrayContain(recipes, recipe);
    }

    /**
     * see com.hbm.handler.FuelHandler at 1.7
     */
    @SubscribeEvent
    public void onGetFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack fuel = event.getItemStack();
        int single = 200;
        boolean changed = true;
        if (fuel.getItem().equals(ModItems.solid_fuel)) event.setBurnTime(single * 16);
        else if (fuel.getItem().equals(ModItems.solid_fuel_presto)) event.setBurnTime(single * 40);
        else if (fuel.getItem().equals(ModItems.solid_fuel_presto_triplet)) event.setBurnTime(single * 200);
        else if(fuel.getItem().equals(ModItems.solid_fuel_bf))					event.setBurnTime(single * 160);
        else if(fuel.getItem().equals(ModItems.solid_fuel_presto_bf))			event.setBurnTime(single * 400);
        else if(fuel.getItem().equals(ModItems.solid_fuel_presto_triplet_bf))	event.setBurnTime(single * 2000);
        else if (fuel.getItem().equals(ModItems.rocket_fuel)) event.setBurnTime(single * 32);

        else if (fuel.getItem() == ModItems.biomass) event.setBurnTime(single * 2);
        else if (fuel.getItem() == ModItems.biomass_compressed) event.setBurnTime(single * 4);
        else if (fuel.getItem() == ModItems.powder_coal) event.setBurnTime(single * 8);
        else if (fuel.getItem() == ModItems.scrap) event.setBurnTime(single / 4);
        else if (fuel.getItem() == ModItems.dust) event.setBurnTime(single / 8);
        else if (fuel.getItem() == Item.getItemFromBlock(ModBlocks.block_scrap)) event.setBurnTime(single * 2);
        else if (fuel.getItem() == ModItems.powder_fire) event.setBurnTime(6400);
        else if (fuel.getItem() == ModItems.lignite) event.setBurnTime(1200);
        else if (fuel.getItem() == ModItems.powder_lignite) event.setBurnTime(1200);
        else if (fuel.getItem() == ModItems.coke) event.setBurnTime(single * 16);
        else if (fuel.getItem() == Item.getItemFromBlock(ModBlocks.block_coke)) event.setBurnTime(single * 160);
        else if (fuel.getItem() == ModItems.book_guide) event.setBurnTime(single);
        else if (fuel.getItem() == ModItems.coal_infernal) event.setBurnTime(4800);
        else if (fuel.getItem() == ModItems.crystal_coal) event.setBurnTime(6400);
        else if (fuel.getItem() == ModItems.powder_sawdust) event.setBurnTime(single / 2);
        else if (fuel.getItem() == ModItems.briquette) {
            int meta = fuel.getItemDamage();
            switch (meta) {
                case 0 -> event.setBurnTime(single * 10);
                case 1 -> event.setBurnTime(single * 8);
                case 2 -> event.setBurnTime(single * 2);
            }
        } else if (fuel.getItem() == ModItems.powder_ash) {
            int meta = fuel.getItemDamage();
            switch (meta) {
                case 0, 2, 4 -> event.setBurnTime(single / 2);
                case 1, 3 -> event.setBurnTime(single);
            }
        } else changed = false;
        event.setCanceled(changed);
    }

    @SubscribeEvent
    public void onCheckLadder(CheckLadderEvent evt) {
        if (ClimbableRegistry.isEntityOnAny(evt.getWorld(), evt.getEntity())) {
            evt.setResult(Result.ALLOW);
        }
    }

    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof IContainerOpenEventListener listener) {
            listener.onContainerOpened(event.getEntityPlayer());
        }
    }

    @SubscribeEvent
    public void onBiomeRegister(RegistryEvent.Register<Biome> evt) {
        if(WorldConfig.enableCraterBiomes) {
            evt.getRegistry().registerAll(
                    BiomeGenCraterBase.craterBiome.setRegistryName("hbm", "crater"),
                    BiomeGenCraterBase.craterInnerBiome.setRegistryName("hbm", "crater_inner"),
                    BiomeGenCraterBase.craterOuterBiome.setRegistryName("hbm", "crater_outer")
            );
        }
        BiomeGenCraterBase.initDictionary();
    }

    private static final String NBT_AKIMBO = "AkimboGhost";

    private boolean isAkimbo(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return false;
        }
        return switch (stack.getItem().getRegistryName().getPath()){
            case "gun_light_revolver_dani", "gun_aberrator_eott", "gun_maresleg_akimbo", "gun_uzi_akimbo", "gun_minigun_dual" -> true;
            default -> false;
        };
        // TileEntityItemStackRenderer is client only. I'll comment them out for now.
        // TODO: move the isAkimbo() to the GunConfig
//        TileEntityItemStackRenderer renderer = stack.getItem().getTileEntityItemStackRenderer();
//        if (renderer instanceof ItemRenderWeaponBase weaponBase) {
//            return weaponBase.isAkimbo();
//        }
    }

    private static boolean isGhost(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(NBT_AKIMBO);
    }

    private static ItemStack makeGhostCopy(ItemStack source) {
        ItemStack copy = source.copy();
        copy.setCount(1);
        NBTTagCompound tag = copy.getTagCompound();
        if (tag == null) tag = new NBTTagCompound();
        tag.setBoolean(NBT_AKIMBO, true);
        copy.setTagCompound(tag);
        return copy;
    }
}
