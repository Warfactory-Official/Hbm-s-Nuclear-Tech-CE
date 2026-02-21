package com.hbm.lib;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockResourceStone;
import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.blocks.machine.PinkCloudBroadcaster;
import com.hbm.blocks.machine.SoyuzCapsule;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.config.MobConfig;
import com.hbm.config.WorldConfig;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsSingle;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.TomSaveData;
import com.hbm.tileentity.bomb.TileEntityLandmine;
import com.hbm.tileentity.deco.TileEntityLanternBehemoth;
import com.hbm.tileentity.machine.TileEntitySafe;
import com.hbm.tileentity.machine.TileEntitySoyuzCapsule;
import com.hbm.util.LootGenerator;
import com.hbm.world.*;
import com.hbm.world.dungeon.AncientTombStructure;
import com.hbm.world.dungeon.ArcticVault;
import com.hbm.world.dungeon.LibraryDungeon;
import com.hbm.world.feature.*;
import com.hbm.world.generator.DungeonToolbox;
import com.hbm.world.generator.JungleDungeonStructure;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class HbmWorldGen implements IWorldGenerator {

    private int parseInt(Object e) {
        if (e == null) return 0;
        return (int) e;
    }

    @Override
    public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        try {
            int dim = world.provider.getDimension();
            int chunkMinX = chunkX * 16;
            int chunkMinZ = chunkZ * 16;

            generateOres(world, rand, chunkMinX, chunkMinZ);
            if (dim == 0) {
                generatePlants(world, rand, chunkMinX, chunkMinZ);
            }
            if (world.getWorldInfo().isMapFeaturesEnabled()) {
                generateStructures(world, rand, chunkMinX, chunkMinZ);
            }
            if (dim == 0) {
                generateBlueprintChest(world, rand, chunkMinX, chunkMinZ, 5000, 5000);
            }

        } catch (Throwable t) {
            MainRegistry.logger.error("NTM Worldgen Error", t);
        }
    }

    //TEMPORARY
    public void generatePlants(World world, Random rand, int chunkMinX, int chunkMinZ) {

        int x = chunkMinX + rand.nextInt(16);
        int z = chunkMinZ + rand.nextInt(16);

        if (!TomSaveData.forWorld(world).impact) {
            if (rand.nextInt(16) == 0) {
                NTMFlowers.INSTANCE_FOXGLOVE.generate(world, rand, new BlockPos(x, 0, z));
            }

            if (rand.nextInt(8) == 0) {
                NTMFlowers.INSTANCE_NIGHTSHADE.generate(world, rand, new BlockPos(x, 0, z));
            }

            if (rand.nextInt(8) == 0) {
                NTMFlowers.INSTANCE_TOBACCO.generate(world, rand, new BlockPos(x, 0, z));
            }

            if (rand.nextInt(64) == 0) {
                NTMFlowers.INSTANCE_HEMP.generate(world, rand, new BlockPos(x, 0, z));
            }
//			if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER) && rand.nextInt(4) == 0) {
//				DungeonToolbox.generateFlowers(world, rand, i, j, ModBlocks.reeds, 0);
//			}
//
//			if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH) && rand.nextInt(8) == 0) {
//				DungeonToolbox.generateFlowers(world, rand, i, j, ModBlocks.reeds, 0);
//			}

        }
    }

    public void generateOres(World world, Random rand, int chunkMinX, int chunkMinZ) {
        int dimID = world.provider.getDimension();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

//        int dimOilcoalSpawn = parseInt(CompatibilityConfig.oilcoalSpawn.get(dimID));
//        if (dimOilcoalSpawn > 0 && rand.nextInt(dimOilcoalSpawn) == 0) {
//            DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, 1, 64, 32, 32, ModBlocks.ore_coal_oil);
//        }

        int dimGasbubbleSpawn = parseInt(CompatibilityConfig.gasbubbleSpawn.get(dimID));
        if (dimGasbubbleSpawn > 0 && rand.nextInt(dimGasbubbleSpawn) == 0 && GeneralConfig.enableFlammableGas) {
            // mlbv: upstream use meta 1 here, does it mean anything?
            DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, 1, 32, 30, 10, ModBlocks.gas_flammable);
        }

        int dimExplosivebubbleSpawn = parseInt(CompatibilityConfig.explosivebubbleSpawn.get(dimID));
        if (dimExplosivebubbleSpawn > 0 && rand.nextInt(dimExplosivebubbleSpawn) == 0 && GeneralConfig.enableExplosiveGas) {
            //mlbv: upstream use meta 1
            DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, 1, 32, 30, 10, ModBlocks.gas_explosive);
        }

        int dimAlexandriteSpawn = parseInt(CompatibilityConfig.alexandriteSpawn.get(dimID));
        if (dimAlexandriteSpawn > 0 && rand.nextInt(dimAlexandriteSpawn) == 0) {
            DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, 1, 3, 10, 5, ModBlocks.ore_alexandrite);
        }
        if (dimID == 0) {
            DepthDeposit.generateConditionOverworld(world, chunkMinX, 0, 3, chunkMinZ, 5, 0.6D, ModBlocks.cluster_depth_iron, rand, 24);
            DepthDeposit.generateConditionOverworld(world, chunkMinX, 0, 3, chunkMinZ, 5, 0.6D, ModBlocks.cluster_depth_titanium, rand, 32);
            DepthDeposit.generateConditionOverworld(world, chunkMinX, 0, 3, chunkMinZ, 5, 0.6D, ModBlocks.cluster_depth_tungsten, rand, 32);
            DepthDeposit.generateConditionOverworld(world, chunkMinX, 0, 3, chunkMinZ, 5, 0.8D, ModBlocks.ore_depth_cinnabar, rand, 16);
            DepthDeposit.generateConditionOverworld(world, chunkMinX, 0, 3, chunkMinZ, 5, 0.8D, ModBlocks.ore_depth_zirconium, rand, 16);
            DepthDeposit.generateConditionOverworld(world, chunkMinX, 0, 3, chunkMinZ, 5, 0.8D, ModBlocks.ore_depth_borax, rand, 16);
        }
        if (dimID == -1) {
            DepthDeposit.generateConditionNether(world, chunkMinX, 0, 3, chunkMinZ, 7, 0.6D, ModBlocks.ore_depth_nether_neodymium, rand, 16);
            DepthDeposit.generateConditionNether(world, chunkMinX, 125, 3, chunkMinZ, 7, 0.6D, ModBlocks.ore_depth_nether_neodymium, rand, 16);
            DepthDeposit.generateConditionNether(world, chunkMinX, 0, 3, chunkMinZ, 7, 0.6D, ModBlocks.ore_depth_nether_nitan, rand, 16);
            DepthDeposit.generateConditionNether(world, chunkMinX, 125, 3, chunkMinZ, 7, 0.6D, ModBlocks.ore_depth_nether_nitan, rand, 16);

            for (int k = 0; k < 30; k++) {
                int x = chunkMinX + rand.nextInt(16);
                int z = chunkMinZ + rand.nextInt(16);
                int d = 16 + rand.nextInt(96);

                for (int y = d - 5; y <= d; y++) {
                    pos.setPos(x, y + 1, z);
                    IBlockState state = world.getBlockState(pos);
                    if (state.getBlock().isAir(state, world, pos)) {
                        pos.setPos(x, y, z);
                        if (world.getBlockState(pos).getBlock() == Blocks.NETHERRACK) {
                            world.setBlockState(pos, ModBlocks.ore_nether_smoldering.getDefaultState(), 2 | 16);
                        }
                    }
                }
            }
        }

        //Gneiss
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, dimID == 0 ? 25 : 0, 6, 30, 10, ModBlocks.ore_gneiss_iron, ModBlocks.stone_gneiss);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, dimID == 0 ? 10 : 0, 6, 30, 10, ModBlocks.ore_gneiss_gold, ModBlocks.stone_gneiss);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.uraniumSpawn.get(dimID)) * 3, 6, 30, 10, ModBlocks.ore_gneiss_uranium, ModBlocks.stone_gneiss);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.copperSpawn.get(dimID)) * 3, 6, 30, 10, ModBlocks.ore_gneiss_copper, ModBlocks.stone_gneiss);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.asbestosSpawn.get(dimID)) * 3, 6, 30, 10, ModBlocks.ore_gneiss_asbestos, ModBlocks.stone_gneiss);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.lithiumSpawn.get(dimID)), 6, 30, 10, ModBlocks.ore_gneiss_lithium, ModBlocks.stone_gneiss);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.rareSpawn.get(dimID)), 6, 30, 10, ModBlocks.ore_gneiss_asbestos, ModBlocks.stone_gneiss);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.gassshaleSpawn.get(dimID)) * 3, 10, 30, 10, ModBlocks.ore_gneiss_gas, ModBlocks.stone_gneiss);

        //Normal ores
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.uraniumSpawn.get(dimID)), 5, 5, 20, ModBlocks.ore_uranium);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.thoriumSpawn.get(dimID)), 5, 5, 25, ModBlocks.ore_thorium);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.titaniumSpawn.get(dimID)), 6, 5, 30, ModBlocks.ore_titanium);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.sulfurSpawn.get(dimID)), 8, 5, 30, ModBlocks.ore_sulfur);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.aluminiumSpawn.get(dimID)), 6, 5, 40, ModBlocks.ore_aluminium);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.copperSpawn.get(dimID)), 6, 5, 45, ModBlocks.ore_copper);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.fluoriteSpawn.get(dimID)), 4, 5, 45, ModBlocks.ore_fluorite);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.niterSpawn.get(dimID)), 6, 5, 30, ModBlocks.ore_niter);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.tungstenSpawn.get(dimID)), 8, 5, 30, ModBlocks.ore_tungsten);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.leadSpawn.get(dimID)), 9, 5, 30, ModBlocks.ore_lead);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.berylliumSpawn.get(dimID)), 4, 5, 30, ModBlocks.ore_beryllium);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.rareSpawn.get(dimID)), 5, 5, 20, ModBlocks.ore_rare);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.ligniteSpawn.get(dimID)), 24, 35, 25, ModBlocks.ore_lignite);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.asbestosSpawn.get(dimID)), 4, 16, 16, ModBlocks.ore_asbestos);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.cinnabarSpawn.get(dimID)), 4, 8, 16, ModBlocks.ore_cinnabar);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.cobaltSpawn.get(dimID)), 4, 4, 8, ModBlocks.ore_cobalt);

        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.ironClusterSpawn.get(dimID)), 6, 15, 45, ModBlocks.cluster_iron);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.titaniumClusterSpawn.get(dimID)), 6, 15, 30, ModBlocks.cluster_titanium);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.aluminiumClusterSpawn.get(dimID)), 6, 15, 35, ModBlocks.cluster_aluminium);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.copperClusterSpawn.get(dimID)), 6, 15, 20, ModBlocks.cluster_copper);

        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, WorldConfig.limestoneSpawn, 6, 15, 20, ModBlocks.stone_resource.getDefaultState().withProperty(BlockResourceStone.META, BlockEnums.EnumStoneType.LIMESTONE.ordinal()));

        if (WorldConfig.newBedrockOres) {
            if (rand.nextInt(10) == 0) {
                int randPosX = chunkMinX + rand.nextInt(2) + 8;
                int randPosZ = chunkMinZ + rand.nextInt(2) + 8;

//                BedrockOre.generate(world, randPosX, randPosZ, new ItemStack(ModItems.bedrock_ore_base), null, 0xD78A16, 1);
                BedrockOre.OVERWORLD.generate(world, world.rand, new BlockPos(randPosX, 0, randPosZ));
            }
        }

        //Special ores

        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.australiumSpawn.get(dimID)), 3, 14, 18, ModBlocks.ore_australium);

        //Nether ores
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.netherUraniumSpawn.get(dimID)), 6, 0, 127, ModBlocks.ore_nether_uranium, Blocks.NETHERRACK);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.netherTungstenSpawn.get(dimID)), 10, 0, 127, ModBlocks.ore_nether_tungsten, Blocks.NETHERRACK);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.netherSulfurSpawn.get(dimID)), 12, 0, 127, ModBlocks.ore_nether_sulfur, Blocks.NETHERRACK);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.netherPhosphorusSpawn.get(dimID)), 6, 0, 127, ModBlocks.ore_nether_fire, Blocks.NETHERRACK);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.netherCoalSpawn.get(dimID)), 32, 16, 96, ModBlocks.ore_nether_coal, Blocks.NETHERRACK);
        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.netherCobaltSpawn.get(dimID)), 6, 100, 26, ModBlocks.ore_nether_cobalt, Blocks.NETHERRACK);
        if (GeneralConfig.enablePlutoniumOre) {
            DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.netherPlutoniumSpawn.get(dimID)), 4, 0, 127, ModBlocks.ore_nether_plutonium, Blocks.NETHERRACK);
        }
        if (world.provider.getDimension() == -1 && rand.nextInt(10) == 0) {
            int randPosX = chunkMinX + rand.nextInt(2) + 8;
            int randPosZ = chunkMinZ + rand.nextInt(2) + 8;
            BedrockOre.getWeightedNetherOre(world, chunkMinX >> 4, chunkMinZ >> 4).generate(world, rand, pos.setPos(randPosX, 0, randPosZ));
        }

        DungeonToolbox.generateOre(world, rand, chunkMinX, chunkMinZ, parseInt(CompatibilityConfig.endTixiteSpawn.get(dimID)), 6, 0, 127, ModBlocks.ore_tikite, Blocks.END_STONE);

        if (dimID == 0) {
            Random colRand = new Random(world.getSeed() + 5);
            int colX = (int) (colRand.nextGaussian() * 1500);
            int colZ = (int) (colRand.nextGaussian() * 1500);
            int colRange = 750;

            if ((GeneralConfig.enable528BedrockSpawn || GeneralConfig.enable528BedrockDeposit) && rand.nextInt(GeneralConfig.bedrockRate) == 0) {
                int x = chunkMinX + rand.nextInt(16) + 8;
                int z = chunkMinZ + rand.nextInt(16) + 8;

                if (GeneralConfig.enable528BedrockSpawn || (GeneralConfig.enable528BedrockDeposit && x <= colX + colRange && x >= colX - colRange && z <= colZ + colRange && z >= colZ - colRange)) {
                    BedrockOre.COLTAN.generate(world, rand, new BlockPos(x, 0, z));
                }
            }

            if (GeneralConfig.enable528ColtanDeposit) {
                for (int k = 0; k < 2; k++) {
                    for (int r = 1; r <= 5; r++) {
                        int randPosX = chunkMinX + rand.nextInt(16);
                        int randPosY = rand.nextInt(25) + 15;
                        int randPosZ = chunkMinZ + rand.nextInt(16);

                        int range = colRange / r;

                        if (randPosX <= colX + range && randPosX >= colX - range && randPosZ <= colZ + range && randPosZ >= colZ - range) {
                            new WorldGenMinableNonCascade(ModBlocks.ore_coltan.getDefaultState(), 4).generate(world, rand, new BlockPos(randPosX, randPosY, randPosZ));
                        }
                    }
                }
            }

            for (int k = 0; k < rand.nextInt(4); k++) {
                int randPosX = chunkMinX + rand.nextInt(16);
                int randPosY = rand.nextInt(15) + 15;
                int randPosZ = chunkMinZ + rand.nextInt(16);

                if (randPosX <= -350 && randPosX >= -450 && randPosZ <= -350 && randPosZ >= -450)
                    new WorldGenMinableNonCascade(ModBlocks.ore_australium.getDefaultState(), 50).generate(world, rand, new BlockPos(randPosX, randPosY, randPosZ));
            }
        }

        generateBedrockOil(world, rand, chunkMinX, chunkMinZ, dimID);
    }

    /**
     * Fake noise generator "unruh" ("unrest", the motion of a clockwork), using a bunch of layered, scaaled and offset
     * sine functions to simulate a simple noise generator that runs somewhat efficiently
     *
     * @param seed  the random function seed used for this operation
     * @param x     the exact x-coord of the height you want
     * @param z     the exact z-coord of the height you want
     * @param scale how much the x/z coords should be amplified
     * @param depth the resolution of the operation, higher numbers call more sine functions
     * @return the height value
     */
    private double generateUnruh(long seed, int x, int z, double scale, int depth) {

        scale = 1 / scale;

        double result = 1;

        Random rand = new Random(seed);

        for (int i = 0; i < depth; i++) {

            double offsetX = rand.nextDouble() * Math.PI * 2;
            double offsetZ = rand.nextDouble() * Math.PI * 2;

            result += Math.sin(x / Math.pow(2, depth) * scale + offsetX) * Math.sin(z / Math.pow(2, depth) * scale + offsetZ);
        }

        return result / depth;
    }

    private void generateAStructure(World world, Random rand, int chunkMinX, int chunkMinZ, WorldGenerator structure, int chance) {
        if (chance > 0 && rand.nextInt(chance) == 0) {
            int x = chunkMinX + rand.nextInt(16);
            int z = chunkMinZ + rand.nextInt(16);

            if (structure instanceof AbstractPhasedStructure phased) {
                phased.generate(world, rand, new BlockPos(x, 0, z));
            } else {
                int y = world.getHeight(x, z);
                if (y > 0 && y < world.getHeight()) {
                    structure.generate(world, rand, new BlockPos(x, y, z));
                }
            }
        }
    }

    private void generateBedrockOil(World world, Random rand, int chunkMinX, int chunkMinZ, int dimID) {
        int dimBedrockOilFreq = parseInt(CompatibilityConfig.bedrockOilSpawn.get(dimID));
        if (dimBedrockOilFreq > 0 && rand.nextInt(dimBedrockOilFreq) == 0) {
            int randPosX = chunkMinX + rand.nextInt(16);
            int randPosZ = chunkMinZ + rand.nextInt(16);
            BedrockOilDeposit.generate(world, randPosX, randPosZ);
        }
    }

    private void generateSellafieldPool(World world, Random rand, int chunkMinX, int chunkMinZ, int dimID) {
        int dimRadFreq = parseInt(CompatibilityConfig.radfreq.get(dimID));
        if (dimRadFreq > 0 && rand.nextInt(dimRadFreq) == 0) {
            int x = chunkMinX + rand.nextInt(16);
            int z = chunkMinZ + rand.nextInt(16);

            double r = rand.nextInt(15) + 10;

            if (rand.nextInt(50) == 0) r = 50;

            new Sellafield(r, r * 0.35D).generate(world, rand, new BlockPos(x, 0, z));

            if (GeneralConfig.enableDebugMode) MainRegistry.logger.info("[Debug] Successfully spawned raditation hotspot at x={} z={}", x, z);
        }
    }

    private void generateStructures(World world, Random rand, int chunkMinX, int chunkMinZ) {
        int dimID = world.provider.getDimension();
        int centerX = chunkMinX + 8;
        int centerZ = chunkMinZ + 8;

        Biome biome = world.getBiome(new BlockPos(centerX, 0, centerZ));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        if (GeneralConfig.enableDungeons) {

            if (MobConfig.enableHives && rand.nextInt(MobConfig.hiveSpawn) == 0 && world.provider.getDimension() == 0) {
                int x = chunkMinX + rand.nextInt(16);
                int z = chunkMinZ + rand.nextInt(16);
                int y = world.getHeight(x, z);

                for (int k = 3; k >= -1; k--) {
                    pos.setPos(x, y - 1 + k, z);
                    if (world.getBlockState(pos).isNormalCube()) {
                        GlyphidHive.generate(world, x, y + k, z, rand, rand.nextInt(10) == 0, true);
                        break;
                    }
                }
            }

            if (biome.getDefaultTemperature() >= 0.8F && biome.getRainfall() > 0.7F) {
                generateAStructure(world, rand, chunkMinX, chunkMinZ, Radio01.INSTANCE, parseInt(CompatibilityConfig.radioStructure.get(dimID)));
            }
            if (biome.getDefaultTemperature() <= 0.5F) {
                generateAStructure(world, rand, chunkMinX, chunkMinZ, Antenna.INSTANCE, parseInt(CompatibilityConfig.antennaStructure.get(dimID)));
            }
            if (!biome.canRain() && biome.getDefaultTemperature() >= 2F) {
                generateAStructure(world, rand, chunkMinX, chunkMinZ, DesertAtom001.INSTANCE, parseInt(CompatibilityConfig.atomStructure.get(dimID)));
            }
            if (biome.getDefaultTemperature() < 2F || biome.getDefaultTemperature() > 1.0F) {
                generateAStructure(world, rand, chunkMinX, chunkMinZ, Relay.INSTANCE, parseInt(CompatibilityConfig.relayStructure.get(dimID)));
            }
            if (biome.getDefaultTemperature() > 1.8F) {
                generateAStructure(world, rand, chunkMinX, chunkMinZ, Barrel.INSTANCE, parseInt(CompatibilityConfig.barrelStructure.get(dimID)));
            }
            if (biome.getDefaultTemperature() < 1F || biome.getDefaultTemperature() > 1.8F) {
                generateAStructure(world, rand, chunkMinX, chunkMinZ, Satellite.INSTANCE, parseInt(CompatibilityConfig.satelliteStructure.get(dimID)));
            }

            generateAStructure(world, rand, chunkMinX, chunkMinZ, Spaceship.INSTANCE, parseInt(CompatibilityConfig.spaceshipStructure.get(dimID)));
            generateAStructure(world, rand, chunkMinX, chunkMinZ, Bunker.INSTANCE, parseInt(CompatibilityConfig.bunkerStructure.get(dimID)));
            generateAStructure(world, rand, chunkMinX, chunkMinZ, new Dud(), parseInt(CompatibilityConfig.dudStructure.get(dimID)));

            if (biome.getTempCategory() == Biome.TempCategory.WARM && biome.getTempCategory() != Biome.TempCategory.OCEAN) {
                generateSellafieldPool(world, rand, chunkMinX, chunkMinZ, dimID);
            }

            if (GeneralConfig.enableMines) {
                int dimMineFreq = parseInt(CompatibilityConfig.minefreq.get(dimID));
                if (dimMineFreq > 0 && rand.nextInt(dimMineFreq) == 0) {
                    int x = chunkMinX + rand.nextInt(16);
                    int z = chunkMinZ + rand.nextInt(16);
                    int y = world.getHeight(x, z);

                    BlockPos below = pos.setPos(x, y - 1, z);
                    if (world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP)) {
                        BlockPos minePos = pos.setPos(x, y, z);
                        world.setBlockState(minePos, ModBlocks.mine_ap.getDefaultState(), 2 | 16);
                        TileEntityLandmine landmine = (TileEntityLandmine) world.getTileEntity(minePos);
                        if (landmine != null) landmine.waitingForPlayer = true;

                        if (GeneralConfig.enableDebugMode) {
                            MainRegistry.logger.info("[Debug] Successfully spawned landmine at x={} y={} z={}", x, y, z);
                        }
                    }
                }
            }

            if (rand.nextInt(2000) == 0 && world.provider.getDimension() == 0) {
                int x = chunkMinX + rand.nextInt(16);
                int z = chunkMinZ + rand.nextInt(16);
                int y = world.getHeight(x, z);

                BlockPos below = pos.setPos(x, y - 1, z);
                IBlockState belowState = world.getBlockState(below);
                BlockPos basePos = pos.setPos(x, y, z);
                IBlockState baseState = world.getBlockState(basePos);

                if (belowState.getBlock().canPlaceTorchOnTop(belowState, world, pos.setPos(x, y - 1, z)) && baseState.getBlock()
                                                                                                                     .isReplaceable(world, pos.setPos(x, y, z))) {
                    pos.setPos(x, y, z);
                    world.setBlockState(basePos, ModBlocks.lantern_behemoth.getDefaultState().withProperty(BlockDummyable.META, 12), 2 | 16);
                    MultiblockHandlerXR.fillSpace(world, x, y, z, new int[]{4, 0, 0, 0, 0, 0}, ModBlocks.lantern_behemoth, ForgeDirection.NORTH);

                    TileEntityLanternBehemoth lantern = (TileEntityLanternBehemoth) world.getTileEntity(basePos);
                    if (lantern != null) lantern.isBroken = true;

                    if (rand.nextInt(2) == 0) {
                        LootGenerator.setBlock(world, x, y, z - 2);
                        LootGenerator.lootBooklet(world, x, y, z - 2);
                    }

                    if (GeneralConfig.enableDebugMode) {
                        MainRegistry.logger.info("[Debug] Successfully spawned lantern at {} {} {}", x, y, z);
                    }
                }
            }

            if (GeneralConfig.enable528 && GeneralConfig.enable528BosniaSimulator && world.provider.getDimension() == 0 && rand.nextInt(16) == 0) {
                int x = chunkMinX + rand.nextInt(16);
                int z = chunkMinZ + rand.nextInt(16);
                int y = world.getHeight(x, z);

                BlockPos below = pos.setPos(x, y - 1, z);
                if (world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP)) {
                    BlockPos minePos = pos.setPos(x, y, z);
                    world.setBlockState(minePos, ModBlocks.mine_he.getDefaultState(), 2 | 16);
                    TileEntityLandmine landmine = (TileEntityLandmine) world.getTileEntity(minePos);
                    if (landmine != null) landmine.waitingForPlayer = true;
                }
            }

            int dimBroadcaster = parseInt(CompatibilityConfig.broadcaster.get(dimID));
            if (dimBroadcaster > 0 && rand.nextInt(dimBroadcaster) == 0) {
                int x = chunkMinX + rand.nextInt(16);
                int z = chunkMinZ + rand.nextInt(16);
                int y = world.getHeight(x, z);

                BlockPos below = pos.setPos(x, y - 1, z);
                if (world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP)) {
                    BlockPos bcPos = pos.setPos(x, y, z);
                    world.setBlockState(bcPos, ModBlocks.broadcaster_pc.getDefaultState()
                                                                       .withProperty(PinkCloudBroadcaster.FACING, EnumFacing.byIndex(rand.nextInt(4) + 2)), 2 | 16);

                    if (GeneralConfig.enableDebugMode)
                        MainRegistry.logger.info("[Debug] Successfully spawned corrupted broadcaster at x={} y={} z={}", x, y, z);
                }
            }

            int dimDungeonStructure = parseInt(CompatibilityConfig.dungeonStructure.get(dimID));
            if (dimDungeonStructure > 0 && rand.nextInt(dimDungeonStructure) == 0) {
                int x = chunkMinX + rand.nextInt(16);
                int y = rand.nextInt(256);
                int z = chunkMinZ + rand.nextInt(16);
                LibraryDungeon.INSTANCE.generate(world, rand, pos.setPos(x, y, z));
            }

            if (biome.getRainfall() > 2F) {
                int dimGeyserWater = parseInt(CompatibilityConfig.geyserWater.get(dimID));
                if (dimGeyserWater > 0 && rand.nextInt(dimGeyserWater) == 0) {
                    int x = chunkMinX + rand.nextInt(16);
                    int z = chunkMinZ + rand.nextInt(16);
                    int y = world.getHeight(x, z);

                    pos.setPos(x, y - 1, z);
                    if (world.getBlockState(pos).getBlock() == Blocks.GRASS) Geyser.INSTANCE.generate(world, rand, pos.setPos(x, y, z));
                }
            }

            if (biome.getDefaultTemperature() > 1.8F && biome.getRainfall() < 1F) {
                int dimGeyserChlorine = parseInt(CompatibilityConfig.geyserChlorine.get(dimID));
                if (dimGeyserChlorine > 0 && rand.nextInt(dimGeyserChlorine) == 0) {
                    int x = chunkMinX + rand.nextInt(16);
                    int z = chunkMinZ + rand.nextInt(16);
                    int y = world.getHeight(x, z);

                    pos.setPos(x, y - 1, z);
                    if (world.getBlockState(pos).getBlock() == Blocks.SAND) {
                        GeyserLarge.INSTANCE.generate(world, rand, pos.setPos(x, y, z));
                    }
                }
            }

            // Geyser vapor – stone under air
            int dimGeyserVapor = parseInt(CompatibilityConfig.geyserVapor.get(dimID));
            if (dimGeyserVapor > 0 && rand.nextInt(dimGeyserVapor) == 0) {
                int x = chunkMinX + rand.nextInt(16);
                int z = chunkMinZ + rand.nextInt(16);
                int y = world.getHeight(x, z);

                BlockPos below = pos.setPos(x, y - 1, z);
                if (world.getBlockState(below).getBlock() == Blocks.STONE) {
                    world.setBlockState(below, ModBlocks.geysir_vapor.getDefaultState(), 2 | 16);
                }
            }

            int dimGeyserNether = parseInt(CompatibilityConfig.geyserNether.get(dimID));
            if (dimGeyserNether > 0 && rand.nextInt(dimGeyserNether) == 0) {
                int x = chunkMinX + rand.nextInt(16);
                int z = chunkMinZ + rand.nextInt(16);
                int d = 16 + rand.nextInt(96);

                for (int y = d - 5; y <= d; y++) {
                    pos.setPos(x, y + 1, z);
                    if (world.getBlockState(pos).getBlock() == Blocks.AIR) {
                        pos.setPos(x, y, z);
                        if (world.getBlockState(pos).getBlock() == Blocks.NETHERRACK) {
                            world.setBlockState(pos, ModBlocks.geysir_nether.getDefaultState(), 2 | 16);
                        }
                    }
                }
            }

            if (biome.getDefaultTemperature() <= 1F) {
                int dimCapsuleStructure = parseInt(CompatibilityConfig.capsuleStructure.get(dimID));
                if (dimCapsuleStructure > 0 && rand.nextInt(dimCapsuleStructure) == 0) {
                    int x = chunkMinX + rand.nextInt(16);
                    int z = chunkMinZ + rand.nextInt(16);
                    int y = world.getHeight(x, z) - 4;

                    BlockPos above = pos.setPos(x, y + 1, z);
                    if (world.getBlockState(above).isSideSolid(world, above, EnumFacing.UP)) {

                        BlockPos capsulePos = pos.setPos(x, y, z);
                        world.setBlockState(capsulePos, ModBlocks.soyuz_capsule.getDefaultState().withProperty(SoyuzCapsule.RUSTY, true), 2 | 16);

                        TileEntitySoyuzCapsule cap = (TileEntitySoyuzCapsule) world.getTileEntity(capsulePos);

                        if (cap != null) {
                            cap.inventory.setStackInSlot(rand.nextInt(cap.inventory.getSlots()), new ItemStack(ModItems.record_glass));
                        }

                        if (GeneralConfig.enableDebugMode) MainRegistry.logger.info("[Debug] Successfully spawned capsule at x={} z={}", x, z);
                    }
                }
            }

            if (rand.nextInt(1000) == 0) {
                int x = chunkMinX + rand.nextInt(16);
                int z = chunkMinZ + rand.nextInt(16);

                boolean done = false;
                for (int y = 0; y < 256; y++) {
                    pos.setPos(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    if (state.getBlock() == Blocks.LOG && state.getValue(BlockOldLog.VARIANT) == BlockPlanks.EnumType.OAK) {
                        world.setBlockState(pos, ModBlocks.pink_log.getDefaultState(), 2 | 16);
                        done = true;
                    }
                }
                if (GeneralConfig.enableDebugMode && done) MainRegistry.logger.info("[Debug] Successfully spawned pink tree at x={} z={}", x, z);
            }

            if (GeneralConfig.enableVaults) {
                int dimVaultFreq = parseInt(CompatibilityConfig.vaultfreq.get(dimID));
                if (dimVaultFreq > 0 && rand.nextInt(dimVaultFreq) == 0) {
                    int x = chunkMinX + rand.nextInt(16);
                    int z = chunkMinZ + rand.nextInt(16);
                    int y = world.getHeight(x, z);

                    BlockPos below = pos.setPos(x, y - 1, z);
                    if (world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP)) {
                        BlockPos safePos = pos.setPos(x, y, z);
                        boolean set = world.setBlockState(safePos, ModBlocks.safe.getDefaultState()
                                                                                 .withProperty(BlockStorageCrate.FACING, EnumFacing.byIndex(rand.nextInt(4) + 2)), 2 | 16);

                        if (set) {
                            TileEntitySafe safe = (TileEntitySafe) world.getTileEntity(safePos);
                            if (safe != null) {
                                int roll = rand.nextInt(10);
                                switch (roll) {
                                    case 0, 1, 2, 3 -> {
                                        WeightedRandomChestContentFrom1710.generateChestContents(rand, ItemPool.getPool(ItemPoolsSingle.POOL_VAULT_RUSTY), safe, rand.nextInt(4) + 3);
                                        safe.setPins(rand.nextInt(999) + 1);
                                        safe.setMod(1.0);
                                        safe.lock();
                                    }
                                    case 4, 5, 6 -> {
                                        WeightedRandomChestContentFrom1710.generateChestContents(rand, ItemPool.getPool(ItemPoolsSingle.POOL_VAULT_STANDARD), safe, rand.nextInt(3) + 2);
                                        safe.setPins(rand.nextInt(999) + 1);
                                        safe.setMod(0.1);
                                        safe.lock();
                                    }
                                    case 7, 8 -> {
                                        WeightedRandomChestContentFrom1710.generateChestContents(rand, ItemPool.getPool(ItemPoolsSingle.POOL_VAULT_REINFORCED), safe, rand.nextInt(3) + 1);
                                        safe.setPins(rand.nextInt(999) + 1);
                                        safe.setMod(0.02);
                                        safe.lock();
                                    }
                                    case 9 -> {
                                        WeightedRandomChestContentFrom1710.generateChestContents(rand, ItemPool.getPool(ItemPoolsSingle.POOL_VAULT_UNBREAKABLE), safe, rand.nextInt(2) + 1);
                                        safe.setPins(rand.nextInt(999) + 1);
                                        safe.setMod(0.0);
                                        safe.lock();
                                    }
                                }

                                if (GeneralConfig.enableDebugMode)
                                    MainRegistry.logger.info("[Debug] Successfully spawned safe at x={} y={} z={}", x, y + 1, z);
                            }
                        }
                    }
                }
            }

            if (biome.isHighHumidity() && biome.getDefaultTemperature() < 1.2 && biome.getDefaultTemperature() > 0.8) {
                int dimJungleStructure = parseInt(CompatibilityConfig.jungleStructure.get(dimID));
                if (dimJungleStructure > 0 && rand.nextInt(dimJungleStructure) == 0) {
                    int x = chunkMinX + rand.nextInt(16);
                    int z = chunkMinZ + rand.nextInt(16);

                    //mlbv: what is this supposed to mean? three dungeons stacked?
                    JungleDungeonStructure.INSTANCE.generate(world, world.rand, pos.setPos(x, 20, z));
                    JungleDungeonStructure.INSTANCE.generate(world, world.rand, pos.setPos(x, 24, z));
                    JungleDungeonStructure.INSTANCE.generate(world, world.rand, pos.setPos(x, 28, z));

                    if (GeneralConfig.enableDebugMode)
                        MainRegistry.logger.info("[Debug] Successfully spawned jungle dungeon at x={} y=10 z={}", x, z);

                    int yTop = world.getHeight(x, z);
                    int columnY = yTop;
                    for (int y1 = yTop + 1; y1 > 1; y1--) {
                        pos.setPos(x, y1, z);
                        IBlockState s = world.getBlockState(pos);
                        if (!s.getBlock().isReplaceable(world, pos) && s.isOpaqueCube()) {
                            columnY = y1 + 1;
                            break;
                        }
                    }

                    for (int f = 0; f < 3; f++) {
                        pos.setPos(x, columnY + f, z);
                        world.setBlockState(pos, ModBlocks.deco_titanium.getDefaultState(), 2 | 16);
                    }
                    pos.setPos(x, columnY + 3, z);
                    world.setBlockState(pos, Blocks.REDSTONE_BLOCK.getDefaultState(), 2 | 16);
                }
            }

            if (biome.getTempCategory() == Biome.TempCategory.COLD) {
                int dimArcticStructure = parseInt(CompatibilityConfig.arcticStructure.get(dimID));
                if (dimArcticStructure > 0 && rand.nextInt(dimArcticStructure) == 0) {
                    int x = chunkMinX + rand.nextInt(16);
                    int z = chunkMinZ + rand.nextInt(16);
                    int y = 16 + rand.nextInt(32);
                    ArcticVault.INSTANCE.generate(world, rand, pos.setPos(x, y, z));
                }
            }

            if (biome.getDefaultTemperature() >= 1.8F) {
                int dimPyramidStructure = parseInt(CompatibilityConfig.pyramidStructure.get(dimID));
                if (dimPyramidStructure > 0 && rand.nextInt(dimPyramidStructure) == 0) {
                    int x = chunkMinX + rand.nextInt(16);
                    int z = chunkMinZ + rand.nextInt(16);
                    int y = world.getHeight(x, z);

                    AncientTombStructure.INSTANCE.generate(world, rand, pos.setPos(x, y, z));
                }
            }

            if (!biome.canRain() && biome.getDefaultTemperature() >= 1.8F) {
                if (rand.nextInt(600) == 0) {
                    int x = chunkMinX + rand.nextInt(16);
                    int z = chunkMinZ + rand.nextInt(16);
                    int y = world.getHeight(x, z);

                    new OilSandBubble(15 + rand.nextInt(31)).generate(world, rand, pos.setPos(x, y, z));
                }
            }
        }

        int dimOilSpawn = parseInt(CompatibilityConfig.oilBubbleSpawn.get(dimID));
        if (dimOilSpawn > 0 && rand.nextInt(dimOilSpawn) == 0) {
            int randPosX = chunkMinX + rand.nextInt(16);
            int randPosY = rand.nextInt(25);
            int randPosZ = chunkMinZ + rand.nextInt(16);

            new OilBubble(10 + rand.nextInt(7)).generate(world, rand, pos.setPos(randPosX, randPosY, randPosZ));
        }

        if (GeneralConfig.enableNITAN) {

            if (chunkMinX <= 10000 && chunkMinX + 16 >= 10000 && chunkMinZ <= 10000 && chunkMinZ + 16 >= 10000) {
                BlockPos p = pos.setPos(10000, 250, 10000);
                generateNitanChest(world, rand, p);
            }
            if (chunkMinX <= 0 && chunkMinX + 16 >= 0 && chunkMinZ <= 10000 && chunkMinZ + 16 >= 10000) {
                BlockPos p = pos.setPos(0, 250, 10000);
                generateNitanChest(world, rand, p);
            }
            if (chunkMinX <= -10000 && chunkMinX + 16 >= -10000 && chunkMinZ <= 10000 && chunkMinZ + 16 >= 10000) {
                BlockPos p = pos.setPos(-10000, 250, 10000);
                generateNitanChest(world, rand, p);
            }
            if (chunkMinX <= 10000 && chunkMinX + 16 >= 10000 && chunkMinZ <= 0 && chunkMinZ + 16 >= 0) {
                BlockPos p = pos.setPos(10000, 250, 0);
                generateNitanChest(world, rand, p);
            }
            if (chunkMinX <= -10000 && chunkMinX + 16 >= -10000 && chunkMinZ <= 0 && chunkMinZ + 16 >= 0) {
                BlockPos p = pos.setPos(-10000, 250, 0);
                generateNitanChest(world, rand, p);
            }
            if (chunkMinX <= 10000 && chunkMinX + 16 >= 10000 && chunkMinZ <= -10000 && chunkMinZ + 16 >= -10000) {
                BlockPos p = pos.setPos(10000, 250, -10000);
                generateNitanChest(world, rand, p);
            }
            if (chunkMinX <= 0 && chunkMinX + 16 >= 0 && chunkMinZ <= -10000 && chunkMinZ + 16 >= -10000) {
                BlockPos p = pos.setPos(0, 250, -10000);
                generateNitanChest(world, rand, p);
            }
            if (chunkMinX <= -10000 && chunkMinX + 16 >= -10000 && chunkMinZ <= -10000 && chunkMinZ + 16 >= -10000) {
                BlockPos p = pos.setPos(-10000, 250, -10000);
                generateNitanChest(world, rand, p);
            }
        }

        // 1.7.10: genBlueprintChest(world, rand, i, j, 5000, 5000);
        // keep behavior: overworld-only, large periodic grid
        if (dimID == 0) {
            generateBlueprintChest(world, rand, chunkMinX, chunkMinZ, 5000, 5000);
        }

        // mlbv: this previously always outside the owning chunk (i + rand + 8 with i shifted)
        if (rand.nextInt(4) == 0) {
            int x = chunkMinX + rand.nextInt(16);
            int y = 6 + rand.nextInt(13);
            int z = chunkMinZ + rand.nextInt(16);

            BlockPos keyPos = pos.setPos(x, y, z);
            IBlockState state = world.getBlockState(keyPos);

            if (state.getBlock().isReplaceableOreGen(state, world, keyPos, WorldUtil.STONE_PREDICATE)) {
                world.setBlockState(keyPos, ModBlocks.stone_keyhole.getDefaultState(), 2 | 16);
            }
        }
    }

    private void generateBlueprintChest(World world, Random rand, int chunkMinX, int chunkMinZ, int boundsX, int boundsZ) {
        if (Math.abs(chunkMinX) < 100 && Math.abs(chunkMinZ) < 100) return;
        if (rand.nextBoolean()) return;

        int cX = Math.abs(chunkMinX) % boundsX;
        int cZ = Math.abs(chunkMinZ) % boundsZ;

        if (cX <= 0 && cX + 16 >= 0 && cZ <= 0 && cZ + 16 >= 0) {
            int x = chunkMinX + 8;
            int z = chunkMinZ + 8;
            int surfaceY = world.getHeight(x, z);
            int y = surfaceY - rand.nextInt(2);

            BlockPos chestPos = new BlockPos(x, y, z);

            world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2 | 16);

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int a = x - 1; a <= x + 1; a++) {
                for (int b = y - 1; b <= y + 1; b++) {
                    for (int c = z - 1; c <= z + 1; c++) {
                        if (a == x && b == y && c == z) continue;
                        pos.setPos(a, b, c);
                        world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState(), 2 | 16);
                    }
                }
            }

            TileEntity tile = world.getTileEntity(chestPos);
            if (tile != null) {
                WeightedRandomChestContentFrom1710.generateChestContents(rand, ItemPool.getPool(ItemPoolsSingle.POOL_BLUEPRINTS), tile, 50);
            }
        }
    }

    private static void generateNitanChest(World world, Random rand, BlockPos p) {
        IBlockState state = world.getBlockState(p);
        if (state.getBlock().isAir(state, world, p)) {
            world.setBlockState(p, Blocks.CHEST.getDefaultState(), 2 | 16);
            TileEntity te = world.getTileEntity(p);
            if (te != null && world.getBlockState(p).getBlock() == Blocks.CHEST) {
                WeightedRandomChestContentFrom1710.generateChestContents(rand, ItemPool.getPool(ItemPoolsSingle.POOL_POWDER), te, 29);
            }
        }
    }
}

