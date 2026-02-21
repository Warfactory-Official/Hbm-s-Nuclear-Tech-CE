package com.hbm.world.gen;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.StructureConfig;
import com.hbm.main.StructureManager;
import com.hbm.world.gen.component.BunkerComponents;
import com.hbm.world.gen.component.Component;
import com.hbm.world.gen.nbt.JigsawPiece;
import com.hbm.world.gen.nbt.JigsawPool;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.gen.nbt.SpawnCondition;
import net.minecraft.block.Block;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class NTMWorldGenerator implements IWorldGenerator {

	public NTMWorldGenerator() {
		final List<Biome> invalidBiomes = Arrays.asList(Biomes.OCEAN, Biomes.RIVER, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER, Biomes.DEEP_OCEAN);
		final List<Biome> oceanBiomes = Arrays.asList(Biomes.OCEAN, Biomes.DEEP_OCEAN);
		final List<Biome> beachBiomes = Arrays.asList(Biomes.BEACH, Biomes.STONE_BEACH, Biomes.COLD_BEACH);
		final List<Biome> lighthouseBiomes = Arrays.asList(Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.BEACH, Biomes.STONE_BEACH, Biomes.COLD_BEACH);
		final List<Biome> flatbiomes = Arrays.asList(Biomes.PLAINS, Biomes.ICE_PLAINS, Biomes.DESERT);

		/// SPIRE ///
		NBTStructure.registerStructure(0, new SpawnCondition("spire") {{
			canSpawn = biome -> biome.getHeightVariation() <= 0.05F && !invalidBiomes.contains(biome);
			structure = new JigsawPiece("spire", StructureManager.spire, -1);
			spawnWeight = StructureConfig.spireSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("features") {{
			canSpawn = biome -> !invalidBiomes.contains(biome);
			start = d -> new MapGenNTMFeatures.Start(d.getW(), d.getX(), d.getY(), d.getZ());
			spawnWeight = StructureConfig.featuresSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("bunker") {{
			canSpawn = biome -> !invalidBiomes.contains(biome);
			start = d -> new BunkerComponents.BunkerStart(d.getW(), d.getX(), d.getY(), d.getZ());
			spawnWeight = StructureConfig.bunkerSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("vertibird") {{
			canSpawn = biome -> !biome.canRain() && biome.getDefaultTemperature() >= 2F;
			structure = new JigsawPiece("vertibird", StructureManager.vertibird, -3);
			spawnWeight = StructureConfig.vertibirdSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("crashed_vertibird") {{
			canSpawn = biome -> !biome.canRain() && biome.getDefaultTemperature() >= 2F;
			structure = new JigsawPiece("crashed_vertibird", StructureManager.crashed_vertibird, -10);
			spawnWeight = StructureConfig.vertibirdCrashedSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("aircraft_carrier") {{
			canSpawn = oceanBiomes::contains;
			structure = new JigsawPiece("aircraft_carrier", StructureManager.aircraft_carrier, -6);
			maxHeight = 42;
            spawnWeight = StructureConfig.enableOceanStructures ? StructureConfig.aircraftCarrierSpawnWeight : 0;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("oil_rig") {{
			canSpawn = biome -> biome == Biomes.DEEP_OCEAN;
			structure = new JigsawPiece("oil_rig", StructureManager.oil_rig, -20);
			maxHeight = 12;
			minHeight = 11;
            spawnWeight = StructureConfig.enableOceanStructures ? StructureConfig.oilRigSpawnWeight : 0;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("lighthouse") {{
			canSpawn = lighthouseBiomes::contains;
			structure = new JigsawPiece("lighthouse", StructureManager.lighthouse, -40);
			maxHeight = 29;
			minHeight = 28;
            spawnWeight = StructureConfig.enableOceanStructures ? StructureConfig.lighthouseSpawnWeight : 0;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("beached_patrol") {{
			canSpawn = beachBiomes::contains;
			structure = new JigsawPiece("beached_patrol", StructureManager.beached_patrol, -5);
			minHeight = 58;
			maxHeight = 67;
			spawnWeight = StructureConfig.beachedPatrolSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("dish") {{
			canSpawn = biome -> biome == Biomes.PLAINS;
			structure = new JigsawPiece("dish", StructureManager.dish, -10);
			minHeight = 53;
			maxHeight = 65;
			spawnWeight = StructureConfig.dishSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("forestchem") {{
			canSpawn = biome -> biome == Biomes.FOREST;
			structure = new JigsawPiece("forest_chem", StructureManager.forest_chem, -9);
			spawnWeight = StructureConfig.forestChemSpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("plane1") {{
			canSpawn = biome -> biome == Biomes.FOREST || biome == Biomes.PLAINS;
			structure = new JigsawPiece("crashed_plane_1", StructureManager.plane1, -5);
			spawnWeight = StructureConfig.plane1SpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("plane2") {{
			canSpawn = biome -> biome == Biomes.FOREST || biome == Biomes.PLAINS;
			structure = new JigsawPiece("crashed_plane_2", StructureManager.plane2, -8);
			spawnWeight = StructureConfig.plane2SpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("desert_shack_1") {{
			canSpawn = biome -> biome == Biomes.DESERT;
			structure = new JigsawPiece("desert_shack_1", StructureManager.desert_shack_1, -7);
			spawnWeight = StructureConfig.desertShack1SpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("desert_shack_2") {{
			canSpawn = biome -> biome == Biomes.DESERT;
			structure = new JigsawPiece("desert_shack_2", StructureManager.desert_shack_2, -7);
			spawnWeight = StructureConfig.desertShack2SpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("desert_shack_3") {{
			canSpawn = biome -> biome == Biomes.DESERT;
			structure = new JigsawPiece("desert_shack_3", StructureManager.desert_shack_3, -5);
			spawnWeight = StructureConfig.desertShack3SpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("labolatory") {{
			canSpawn = biome -> biome == Biomes.PLAINS;
			structure = new JigsawPiece("laboratory", StructureManager.laboratory, -10);
			minHeight = 53;
			maxHeight = 65;
			spawnWeight = StructureConfig.laboratorySpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("forest_post") {{
			canSpawn = biome -> biome == Biomes.FOREST;
			structure = new JigsawPiece("forest_post", StructureManager.forest_post, -9);
			spawnWeight = StructureConfig.forestPostSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("factory") {{
			canSpawn = flatbiomes::contains;
			structure = new JigsawPiece("factory", StructureManager.factory, -10);
			spawnWeight = StructureConfig.factorySpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("crane") {{
			canSpawn = flatbiomes::contains;
			structure = new JigsawPiece("crane", StructureManager.crane, -9);
			spawnWeight = StructureConfig.craneSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("broadcaster_tower") {{
			canSpawn = flatbiomes::contains;
			structure = new JigsawPiece("broadcaster_tower", StructureManager.broadcasting_tower, -9);
			spawnWeight = StructureConfig.broadcastingTowerSpawnWeight;
		}});

		NBTStructure.registerStructure(0, new SpawnCondition("ruin1") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canRain();
			structure = new JigsawPiece("NTMRuinsA", StructureManager.ntmruinsA, -1) {{ conformToTerrain = true; }};
			spawnWeight = StructureConfig.ruinsASpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruin2") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canRain();
			structure = new JigsawPiece("NTMRuinsB", StructureManager.ntmruinsB, -1) {{ conformToTerrain = true; }};
			spawnWeight = StructureConfig.ruinsBSpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruin3") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canRain();
			structure = new JigsawPiece("NTMRuinsC", StructureManager.ntmruinsC, -1) {{ conformToTerrain = true; }};
			spawnWeight = StructureConfig.ruinsCSpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruin4") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canRain();
			structure = new JigsawPiece("NTMRuinsD", StructureManager.ntmruinsD, -1) {{ conformToTerrain = true; }};
			spawnWeight = StructureConfig.ruinsDSpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruin5") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canRain();
			structure = new JigsawPiece("NTMRuinsE", StructureManager.ntmruinsE, -1) {{ conformToTerrain = true; }};
			spawnWeight = StructureConfig.ruinsESpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruin6") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canRain();
			structure = new JigsawPiece("NTMRuinsF", StructureManager.ntmruinsF, -1) {{ conformToTerrain = true; }};
			spawnWeight = StructureConfig.ruinsFSpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruin7") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canRain();
			structure = new JigsawPiece("NTMRuinsG", StructureManager.ntmruinsG, -1) {{ conformToTerrain = true; }};
			spawnWeight = StructureConfig.ruinsGSpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruin8") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canRain();
			structure = new JigsawPiece("NTMRuinsH", StructureManager.ntmruinsH, -1) {{ conformToTerrain = true; }};
			spawnWeight = StructureConfig.ruinsHSpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruin9") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canRain();
			structure = new JigsawPiece("NTMRuinsI", StructureManager.ntmruinsI, -1) {{ conformToTerrain = true; }};
			spawnWeight = StructureConfig.ruinsISpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("ruin10") {{
			canSpawn = biome -> !invalidBiomes.contains(biome) && biome.canRain();
			structure = new JigsawPiece("NTMRuinsJ", StructureManager.ntmruinsJ, -1) {{ conformToTerrain = true; }};
			spawnWeight = StructureConfig.ruinsJSpawnWeight;
		}});
		NBTStructure.registerStructure(0, new SpawnCondition("radio") {{
			canSpawn = flatbiomes::contains;
			structure = new JigsawPiece("radio_house", StructureManager.radio_house, -6);
			spawnWeight = StructureConfig.radioSpawnWeight;
		}});

		NBTStructure.registerNullWeight(0, StructureConfig.plainsNullWeight, biome -> biome == Biomes.PLAINS);
		NBTStructure.registerNullWeight(0, StructureConfig.oceanNullWeight, oceanBiomes::contains);

		Map<Block, StructureComponent.BlockSelector> bricks = new HashMap<>() {{
            put(ModBlocks.meteor_brick, new Component.MeteorBricks());
        }};
		Map<Block, StructureComponent.BlockSelector> crates = new HashMap<>() {{
            put(ModBlocks.meteor_brick, new Component.MeteorBricks());
            put(ModBlocks.crate, new Component.SupplyCrates());
            put(ModBlocks.meteor_spawner, new Component.CrabSpawners());
        }};
		Map<Block, StructureComponent.BlockSelector> ooze = new HashMap<>() {{
            put(ModBlocks.meteor_brick, new Component.MeteorBricks());
            put(ModBlocks.concrete_colored, new Component.GreenOoze());
        }};

		NBTStructure.registerStructure(0, new SpawnCondition("meteor_dungeon") {{
			minHeight = 32;
			maxHeight = 32;
			sizeLimit = 128;
			canSpawn = biome -> biome.getBaseHeight() >= 0.0F;
			startPool = "start";
            spawnWeight = StructureConfig.meteorDungeonSpawnWeight;
			pools = new HashMap<>() {{
                put("start", new JigsawPool() {{
                    add(new JigsawPiece("meteor_core", StructureManager.meteor_core) {{
                        blockTable = bricks;
                    }}, 1);
                }});
                put("spike", new JigsawPool() {{
                    add(new JigsawPiece("meteor_spike", StructureManager.meteor_spike) {{
                        heightOffset = -3;
                        conformToTerrain = true;
                    }}, 1);
                }});
                put("default", new JigsawPool() {{
                    add(new JigsawPiece("meteor_corner", StructureManager.meteor_corner) {{
                        blockTable = bricks;
                    }}, 2);
                    add(new JigsawPiece("meteor_t", StructureManager.meteor_t) {{
                        blockTable = bricks;
                    }}, 3);
                    add(new JigsawPiece("meteor_stairs", StructureManager.meteor_stairs) {{
                        blockTable = bricks;
                    }}, 1);
                    add(new JigsawPiece("meteor_room_base_thru", StructureManager.meteor_room_base_thru) {{
                        blockTable = bricks;
                    }}, 3);
                    add(new JigsawPiece("meteor_room_base_end", StructureManager.meteor_room_base_end) {{
                        blockTable = bricks;
                    }}, 4);
                    fallback = "fallback";
                }});
                put("10room", new JigsawPool() {{
                    add(new JigsawPiece("meteor_room_basic", StructureManager.meteor_room_basic) {{
                        blockTable = bricks;
                    }}, 1);
                    add(new JigsawPiece("meteor_room_balcony", StructureManager.meteor_room_balcony) {{
                        blockTable = bricks;
                    }}, 1);
                    add(new JigsawPiece("meteor_room_dragon", StructureManager.meteor_room_dragon) {{
                        blockTable = bricks;
                    }}, 1);
                    add(new JigsawPiece("meteor_room_ladder", StructureManager.meteor_room_ladder) {{
                        blockTable = bricks;
                    }}, 1);
                    add(new JigsawPiece("meteor_room_ooze", StructureManager.meteor_room_ooze) {{
                        blockTable = ooze;
                    }}, 1);
                    add(new JigsawPiece("meteor_room_split", StructureManager.meteor_room_split) {{
                        blockTable = bricks;
                    }}, 1);
                    add(new JigsawPiece("meteor_room_stairs", StructureManager.meteor_room_stairs) {{
                        blockTable = bricks;
                    }}, 1);
                    add(new JigsawPiece("meteor_room_triple", StructureManager.meteor_room_triple) {{
                        blockTable = bricks;
                    }}, 1);
                    fallback = "roomback";
                }});
                put("3x3loot", new JigsawPool() {{
                    add(new JigsawPiece("meteor_3_bale", StructureManager.meteor_3_bale), 1);
                    add(new JigsawPiece("meteor_3_blank", StructureManager.meteor_3_blank), 1);
                    add(new JigsawPiece("meteor_3_block", StructureManager.meteor_3_block), 1);
                    add(new JigsawPiece("meteor_3_crab", StructureManager.meteor_3_crab), 1);
                    add(new JigsawPiece("meteor_3_crab_tesla", StructureManager.meteor_3_crab_tesla), 1);
                    add(new JigsawPiece("meteor_3_crate", StructureManager.meteor_3_crate), 1);
                    add(new JigsawPiece("meteor_3_dirt", StructureManager.meteor_3_dirt), 1);
                    add(new JigsawPiece("meteor_3_lead", StructureManager.meteor_3_lead), 1);
                    add(new JigsawPiece("meteor_3_ooze", StructureManager.meteor_3_ooze), 1);
                    add(new JigsawPiece("meteor_3_pillar", StructureManager.meteor_3_pillar), 1);
                    add(new JigsawPiece("meteor_3_star", StructureManager.meteor_3_star), 1);
                    add(new JigsawPiece("meteor_3_tesla", StructureManager.meteor_3_tesla), 1);
                    add(new JigsawPiece("meteor_3_book", StructureManager.meteor_3_book), 1);
                    add(new JigsawPiece("meteor_3_mku", StructureManager.meteor_3_mku), 1);
                    add(new JigsawPiece("meteor_3_statue", StructureManager.meteor_3_statue), 1);
                    add(new JigsawPiece("meteor_3_glow", StructureManager.meteor_3_glow), 1);
                    fallback = "3x3loot";
                }});
                put("headloot", new JigsawPool() {{
                    add(new JigsawPiece("meteor_dragon_chest", StructureManager.meteor_dragon_chest) {{
                        blockTable = crates;
                    }}, 1);
                    add(new JigsawPiece("meteor_dragon_tesla", StructureManager.meteor_dragon_tesla) {{
                        blockTable = crates;
                    }}, 1);
                    add(new JigsawPiece("meteor_dragon_trap", StructureManager.meteor_dragon_trap) {{
                        blockTable = crates;
                    }}, 1);
                    add(new JigsawPiece("meteor_dragon_crate_crab", StructureManager.meteor_dragon_crate_crab) {{
                        blockTable = crates;
                    }}, 1);
                    fallback = "headback";
                }});
                put("fallback", new JigsawPool() {{
                    add(new JigsawPiece("meteor_fallback", StructureManager.meteor_fallback) {{
                        blockTable = bricks;
                    }}, 1);
                }});
                put("roomback", new JigsawPool() {{
                    add(new JigsawPiece("meteor_room_fallback", StructureManager.meteor_room_fallback) {{
                        blockTable = bricks;
                    }}, 1);
                }});
                put("headback", new JigsawPool() {{
                    add(new JigsawPiece("meteor_loot_fallback", StructureManager.meteor_dragon_fallback) {{
                        blockTable = crates;
                    }}, 1);
                }});
            }};
		}});
	}

	private NBTStructure.GenStructure nbtGen = new NBTStructure.GenStructure();

	private final Random rand = new Random();

	@SubscribeEvent
	public void onLoad(WorldEvent.Load event) {
		nbtGen = (NBTStructure.GenStructure) TerrainGen.getModdedMapGen(new NBTStructure.GenStructure(), EventType.CUSTOM);

		hasPopulationEvent = false;
	}

	private void setRandomSeed(World world, int chunkX, int chunkZ) {
		rand.setSeed(world.getSeed() + world.provider.getDimension());
		final long i = rand.nextLong() / 2L * 2L + 1L;
		final long j = rand.nextLong() / 2L * 2L + 1L;
		rand.setSeed((long) chunkX * i + (long) chunkZ * j ^ world.getSeed());
	}

	private boolean hasPopulationEvent = false;

	@SubscribeEvent
	public void generateStructures(PopulateChunkEvent.Pre event) {
		hasPopulationEvent = true;

		if (StructureConfig.enableStructures == 0) return;
		if (StructureConfig.enableStructures == 2 && !event.getWorld().getWorldInfo().isMapFeaturesEnabled()) return;

		setRandomSeed(event.getWorld(), event.getChunkX(), event.getChunkZ());

		nbtGen.generateStructures(event.getWorld(), rand, event.getWorld().getChunkProvider(), event.getChunkX(), event.getChunkZ());
	}

	@Override
	public void generate(Random unusedRandom, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (hasPopulationEvent) return;

		if (StructureConfig.enableStructures == 0) return;
		if (StructureConfig.enableStructures == 2 && !world.getWorldInfo().isMapFeaturesEnabled()) return;

		setRandomSeed(world, chunkX, chunkZ);

		nbtGen.generateStructures(world, rand, chunkProvider, chunkX, chunkZ);
	}

	public SpawnCondition getStructureAt(World world, int chunkX, int chunkZ) {
		if (StructureConfig.enableStructures == 0) return null;
		if (StructureConfig.enableStructures == 2 && !world.getWorldInfo().isMapFeaturesEnabled()) return null;

		setRandomSeed(world, chunkX, chunkZ);

		return nbtGen.getStructureAt(world, chunkX, chunkZ);
	}

}
