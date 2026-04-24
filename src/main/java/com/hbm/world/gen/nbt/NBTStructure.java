package com.hbm.world.gen.nbt;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockWand;
import com.hbm.blocks.generic.BlockWandTandem.TileEntityWandTandem;
import com.hbm.config.GeneralConfig;
import com.hbm.config.StructureConfig;
import com.hbm.handler.ThreeInts;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileMappings;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.Tuple.Quartet;
import com.hbm.world.gen.nbt.SpawnCondition.WorldCoordinate;
import com.hbm.world.gen.nbt.selector.BiomeBlockSelector;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.*;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Constants.NBT;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

public class NBTStructure {

	/**
	 * Now with structure support!
	 *
	 * the type of structure to generate is saved into the Component,
	 * meaning this can generate all sorts of different structures,
	 * without having to define and register each structure manually
	 */

	private static Map<String, SpawnCondition> namedMap = new HashMap<>();

	protected static Map<Integer, List<SpawnCondition>> weightedMap = new HashMap<>();
	protected static Map<Integer, List<SpawnCondition>> customSpawnMap = new HashMap<>();
    private static Map<Integer, Map<Integer, WeightedSpawnList>> validBiomeCache = new HashMap<>();

	private String name;
    private ResourceLocation resource;

	private boolean isLoaded;

	protected ThreeInts size;
	private List<Pair<Short, String>> itemPalette;
	private BlockState[][][] blockArray;

	private List<List<JigsawConnection>> fromConnections;
	private Map<String, List<JigsawConnection>> toTopConnections;
    private Map<String, List<JigsawConnection>> toBottomConnections;
    private Map<String, List<JigsawConnection>> toHorizontalConnections;
    private Map<Short, String> legacyItemIdToName;
    private static final Map<String, String> substitutions = new HashMap<String, String>() {{
        put(Tags.MODID + ":tile.ore_coal_oil", "minecraft:coal_ore");
        put(Tags.MODID + ":ore_coal_oil", "minecraft:coal_ore");
        put(Tags.MODID + ":fluid_duct_neo", Tags.MODID + ":fluid_duct_mk2");
        put(Tags.MODID + ":rail_narrow", "minecraft:rail");
    }};
    private static final String[] LEGACY_BRICK_SLABS = {
            Tags.MODID + ":reinforced_stone_slab",
            Tags.MODID + ":reinforced_brick_slab",
            Tags.MODID + ":brick_obsidian_slab",
            Tags.MODID + ":brick_light_slab",
            Tags.MODID + ":brick_compound_slab",
            Tags.MODID + ":brick_asbestos_slab",
            Tags.MODID + ":brick_fire_slab"
    };
    private static final String[] LEGACY_BRICK_DOUBLE_SLABS = {
            Tags.MODID + ":reinforced_stone_double_slab",
            Tags.MODID + ":reinforced_brick_double_slab",
            Tags.MODID + ":brick_obsidian_double_slab",
            Tags.MODID + ":brick_light_double_slab",
            Tags.MODID + ":brick_compound_double_slab",
            Tags.MODID + ":brick_asbestos_double_slab",
            Tags.MODID + ":brick_fire_double_slab"
    };
    private static final String[] LEGACY_CONCRETE_BRICK_SLABS = {
            Tags.MODID + ":brick_concrete_slab",
            Tags.MODID + ":brick_concrete_mossy_slab",
            Tags.MODID + ":brick_concrete_cracked_slab",
            Tags.MODID + ":brick_concrete_broken_slab",
            Tags.MODID + ":ducrete_brick_slab"
    };
    private static final String[] LEGACY_CONCRETE_BRICK_DOUBLE_SLABS = {
            Tags.MODID + ":brick_concrete_double_slab",
            Tags.MODID + ":brick_concrete_mossy_double_slab",
            Tags.MODID + ":brick_concrete_cracked_double_slab",
            Tags.MODID + ":brick_concrete_broken_double_slab",
            Tags.MODID + ":ducrete_brick_double_slab"
    };

	public NBTStructure(ResourceLocation resource) {
        this.resource = resource;
		// Can't use regular resource loading, servers don't know how!
		InputStream stream = NBTStructure.class.getResourceAsStream("/assets/" + resource.getNamespace() + "/" + resource.getPath());
		if (stream != null) {
			name = resource.getPath();
			loadStructure(stream);
		} else {
			MainRegistry.logger.error("NBT Structure not found: " + resource.getPath());
		}
	}

	public static void register() {
		MapGenStructureIO.registerStructure(Start.class, "NBTStructures");
		MapGenStructureIO.registerStructureComponent(Component.class, "NBTComponents");
	}

	// Register a new structure for a given dimension
	public static void registerStructure(int dimensionId, SpawnCondition spawn) {
		if(namedMap.containsKey(spawn.name) && namedMap.get(spawn.name) != spawn)
			throw new IllegalStateException("A severe error has occurred in NBTStructure! A SpawnCondition has been registered with the same name as another: " + spawn.name);

		namedMap.put(spawn.name, spawn);

		if(spawn.checkCoordinates != null) {
			List<SpawnCondition> spawnList = customSpawnMap.computeIfAbsent(dimensionId, integer -> new ArrayList<>());
			spawnList.add(spawn);
			return;
		}

		List<SpawnCondition> weightedList = weightedMap.computeIfAbsent(dimensionId, integer -> new ArrayList<>());
		weightedList.add(spawn);
	}

	public static void registerStructure(SpawnCondition spawn, int[] dimensionIds) {
		for(int dimensionId : dimensionIds) {
			registerStructure(dimensionId, spawn);
		}
	}

	// Add a chance for nothing to spawn at a given valid spawn location
	public static void registerNullWeight(int dimensionId, int weight) {
		registerNullWeight(dimensionId, weight, null);
	}

	public static void registerNullWeight(int dimensionId, int weight, Predicate<Biome> predicate) {
		SpawnCondition spawn = new SpawnCondition(weight, predicate);

		List<SpawnCondition> weightedList = weightedMap.computeIfAbsent(dimensionId, k -> new ArrayList<>());
		weightedList.add(spawn);
	}

	// Presents a list of all structures registered (so far)
	public static List<String> listStructures() {
		List<String> names = new ArrayList<>(namedMap.keySet());
		names.sort(String::compareTo);
		return names;
	}

	// Fetches a registered structure by name,
	// If one is not found, will simply return null.
	public static SpawnCondition getStructure(String name) {
		return namedMap.get(name);
	}

	// Saves a selected area into an NBT structure (+ some of our non-standard stuff to support 1.7.10)
	public static void saveArea(String filename, World world, int x1, int y1, int z1, int x2, int y2, int z2, Set<Pair<Block, Integer>> exclude) {
		NBTTagCompound structure = new NBTTagCompound();
		NBTTagList nbtBlocks = new NBTTagList();
		NBTTagList nbtPalette = new NBTTagList();
		NBTTagList nbtItemPalette = new NBTTagList();

		// Quick access hash slinging slashers
		Map<Pair<Block, Integer>, Integer> palette = new HashMap<>();
		Map<Short, Integer> itemPalette = new HashMap<>();

		structure.setInteger("version", 1);

		int ox = Math.min(x1, x2);
		int oy = Math.min(y1, y2);
		int oz = Math.min(z1, z2);

		for(int x = ox; x <= Math.max(x1, x2); x++) {
			for(int y = oy; y <= Math.max(y1, y2); y++) {
				for(int z = oz; z <= Math.max(z1, z2); z++) {
					BlockPos pos = new BlockPos(x, y, z);
					IBlockState state = world.getBlockState(pos);
					Block blk = state.getBlock();
					int metaVal = blk.getMetaFromState(state);

					Pair<Block, Integer> block = new Pair<>(blk, metaVal);

					if(exclude.contains(block)) continue;

					if(block.key instanceof BlockWand) {
						block.key = ((BlockWand) block.key).exportAs;
					}

					int paletteId = palette.size();
					if(palette.containsKey(block)) {
						paletteId = palette.get(block);
					} else {
						palette.put(block, paletteId);

						NBTTagCompound nbtBlock = new NBTTagCompound();
						nbtBlock.setString("Name", block.key.getRegistryName().toString());

						NBTTagCompound nbtProp = new NBTTagCompound();
						nbtProp.setString("meta", block.value.toString());

						nbtBlock.setTag("Properties", nbtProp);

						nbtPalette.appendTag(nbtBlock);
					}

					NBTTagCompound nbtBlock = new NBTTagCompound();
					nbtBlock.setInteger("state", paletteId);

					NBTTagList nbtPos = new NBTTagList();
					nbtPos.appendTag(new NBTTagInt(x - ox));
					nbtPos.appendTag(new NBTTagInt(y - oy));
					nbtPos.appendTag(new NBTTagInt(z - oz));

					nbtBlock.setTag("pos", nbtPos);

					TileEntity te = world.getTileEntity(pos);
					if(te != null) {
						NBTTagCompound nbt = new NBTTagCompound();
						te.writeToNBT(nbt);

						nbt.removeTag("x");
						nbt.removeTag("y");
						nbt.removeTag("z");

						nbtBlock.setTag("nbt", nbt);

						String itemKey = null;
						if(nbt.hasKey("items")) itemKey = "items";
						if(nbt.hasKey("Items")) itemKey = "Items";

						if(nbt.hasKey(itemKey)) {
							NBTTagList items = nbt.getTagList(itemKey, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
							for(int i = 0; i < items.tagCount(); i++) {
								NBTTagCompound item = items.getCompoundTagAt(i);

								Item foundItem = null;

								if(item.hasKey("id", net.minecraftforge.common.util.Constants.NBT.TAG_STRING)) {
									String idStr = item.getString("id");
									foundItem = Item.getByNameOrId(idStr);
								} else if(item.hasKey("id", net.minecraftforge.common.util.Constants.NBT.TAG_INT)) {
									int idInt = item.getInteger("id");
									foundItem = Item.getItemById(idInt);
								} else if(item.hasKey("id", net.minecraftforge.common.util.Constants.NBT.TAG_SHORT)) {
									short idShort = item.getShort("id");
									foundItem = Item.getItemById(idShort);
								}

								if(foundItem != null && foundItem.getRegistryName() != null) {
									short idShort = (short) Item.getIdFromItem(foundItem);
									String name = foundItem.getRegistryName().toString();

									if(!itemPalette.containsKey(idShort)) {
										int itemPaletteId = itemPalette.size();
										itemPalette.put(idShort, itemPaletteId);

										NBTTagCompound nbtItem = new NBTTagCompound();
										nbtItem.setShort("ID", idShort);
										nbtItem.setString("Name", name);

										nbtItemPalette.appendTag(nbtItem);
									}
								}
							}
						}
					}

					nbtBlocks.appendTag(nbtBlock);
				}
			}
		}

		structure.setTag("blocks", nbtBlocks);
		structure.setTag("palette", nbtPalette);
		structure.setTag("itemPalette", nbtItemPalette);

		NBTTagList nbtSize = new NBTTagList();
		nbtSize.appendTag(new NBTTagInt(Math.abs(x1 - x2) + 1));
		nbtSize.appendTag(new NBTTagInt(Math.abs(y1 - y2) + 1));
		nbtSize.appendTag(new NBTTagInt(Math.abs(z1 - z2) + 1));
		structure.setTag("size", nbtSize);

		structure.setTag("entities", new NBTTagList());

		try {
			File structureDirectory = new File(Minecraft.getMinecraft().gameDir, "structures");
			structureDirectory.mkdir();

			File structureFile = new File(structureDirectory, filename);

			CompressedStreamTools.writeCompressed(structure, new FileOutputStream(structureFile));
		} catch (Exception ex) {
			MainRegistry.logger.warn("Failed to save NBT structure", ex);
		}
	}

	private void loadStructure(InputStream inputStream) {
		try (inputStream) {
			NBTTagCompound data = CompressedStreamTools.readCompressed(inputStream);

			// GET SIZE (for offsetting to center)
			size = parsePos(data.getTagList("size", NBT.TAG_INT));

			// PARSE BLOCK PALETTE
			NBTTagList paletteList = data.getTagList("palette", NBT.TAG_COMPOUND);
			BlockDefinition[] palette = new BlockDefinition[paletteList.tagCount()];

			for (int i = 0; i < paletteList.tagCount(); i++) {
				NBTTagCompound p = paletteList.getCompoundTagAt(i);

				String blockName = p.getString("Name");
				NBTTagCompound prop = p.getCompoundTag("Properties");

				int meta = 0;
				try {
					meta = Integer.parseInt(prop.getString("meta"));
				} catch (NumberFormatException ex) {
					MainRegistry.logger.info("Failed to parse: " + prop.getString("meta"));
				}

				palette[i] = new BlockDefinition(blockName, meta);

				if (StructureConfig.debugStructures && palette[i].block == Blocks.AIR) {
					palette[i] = new BlockDefinition(ModBlocks.wand_air, meta);
				}
			}

			// PARSE ITEM PALETTE (custom shite)
			if (data.hasKey("itemPalette")) {
				NBTTagList itemPaletteList = data.getTagList("itemPalette", NBT.TAG_COMPOUND);
				itemPalette = new ArrayList<>(itemPaletteList.tagCount());

				for (int i = 0; i < itemPaletteList.tagCount(); i++) {
					NBTTagCompound p = itemPaletteList.getCompoundTagAt(i);

					short id = p.getShort("ID");
					String name = p.getString("Name");

					itemPalette.add(new Pair<>(id, name));
				}
			} else {
				itemPalette = null;
			}

            Map<Short, String> legacyItemIdMap = getLegacyItemIdToNameMap();

			// LOAD IN BLOCKS
			NBTTagList blockData = data.getTagList("blocks", NBT.TAG_COMPOUND);
			blockArray = new BlockState[size.x][size.y][size.z];

			List<JigsawConnection> connections = new ArrayList<>();

			for (int i = 0; i < blockData.tagCount(); i++) {
				NBTTagCompound block = blockData.getCompoundTagAt(i);
				int state = block.getInteger("state");
				ThreeInts pos = parsePos(block.getTagList("pos", NBT.TAG_INT));

				BlockState blockState = new BlockState(palette[state]);

				if (block.hasKey("nbt")) {
					NBTTagCompound nbt = block.getCompoundTag("nbt").copy();
                    nbtFixerUpper(nbt, legacyItemIdMap);
					blockState.nbt = nbt;

					// Load in connection points for jigsaws
					if (blockState.definition.block == ModBlocks.wand_jigsaw) {
						if (toTopConnections == null) toTopConnections = new HashMap<>();
						if (toBottomConnections == null) toBottomConnections = new HashMap<>();
						if (toHorizontalConnections == null) toHorizontalConnections = new HashMap<>();

						int selectionPriority = nbt.getInteger("selection");
						int placementPriority = nbt.getInteger("placement");
						EnumFacing direction = EnumFacing.byIndex(nbt.getInteger("direction"));
						String poolName = nbt.getString("pool");
						String ourName = nbt.getString("name");
						String targetName = nbt.getString("target");
						String replaceBlock = nbt.getString("block");
						int replaceMeta = nbt.getInteger("meta");
						boolean isRollable = nbt.getBoolean("roll");

						JigsawConnection connection = new JigsawConnection(pos, direction, poolName, targetName, isRollable, selectionPriority, placementPriority);

						connections.add(connection);

						Map<String, List<JigsawConnection>> toConnections;
						if (direction == EnumFacing.UP) {
							toConnections = toTopConnections;
						} else if (direction == EnumFacing.DOWN) {
							toConnections = toBottomConnections;
						} else {
							toConnections = toHorizontalConnections;
						}

						List<JigsawConnection> namedConnections = toConnections.computeIfAbsent(ourName, name -> new ArrayList<>());
						namedConnections.add(connection);

						if (!StructureConfig.debugStructures) {
							blockState = new BlockState(new BlockDefinition(replaceBlock, replaceMeta));
						}
					}
				}

				blockArray[pos.x][pos.y][pos.z] = blockState;
			}

			// MAP OUT CONNECTIONS + PRIORITIES
			if (connections.size() > 0) {
				fromConnections = new ArrayList<>();

				connections.sort((a, b) -> b.selectionPriority - a.selectionPriority); // sort by descending priority, highest first

				// Sort out our from connections, splitting into individual lists for each priority level
				List<JigsawConnection> innerList = null;
				int currentPriority = 0;
				for (JigsawConnection connection : connections) {
					if (innerList == null || currentPriority != connection.selectionPriority) {
						innerList = new ArrayList<>();
						fromConnections.add(innerList);
						currentPriority = connection.selectionPriority;
					}

					innerList.add(connection);
				}
			}

			isLoaded = true;

		} catch (Exception e) {
			MainRegistry.logger.error("Exception reading NBT Structure format", e);
		}
		// hush
	}

	private TileEntity buildTileEntity(World world, NBTTagCompound nbt, int coordBaseMode, String structureName) {
        nbt = nbt.copy(); // wtf Bob?..

        TileEntity te = TileMappings.create(world, nbt);
        if (te instanceof INBTTileEntityTransformable transformable) {
            transformable.transformTE(world, coordBaseMode);
        }
        if (te instanceof TileEntityWandTandem tandem) {
            tandem.arm(getStructure(structureName));
        }
        return te;
    }

	public void build(World world, int x, int y, int z) {
		build(world, x, y, z, 0);
	}

	public void build(World world, int x, int y, int z, int coordBaseMode) {
		if(!isLoaded) {
			MainRegistry.logger.info("NBTStructure is invalid");
			return;
		}

		boolean swizzle = coordBaseMode == 1 || coordBaseMode == 3;
		x -= (swizzle ? size.z : size.x) / 2;
		z -= (swizzle ? size.x : size.z) / 2;

		int maxX = size.x;
		int maxZ = size.z;

		for(int bx = 0; bx < maxX; bx++) {
			for(int bz = 0; bz < maxZ; bz++) {
				int rx = rotateX(bx, bz, coordBaseMode) + x;
				int rz = rotateZ(bx, bz, coordBaseMode) + z;

				for(int by = 0; by < size.y; by++) {
					BlockState state = blockArray[bx][by][bz];
					if(state == null) continue;

					int ry = by + y;

					Block block = transformBlock(state.definition, null, world.rand);
					int meta = transformMeta(state.definition, null, coordBaseMode);

					BlockPos pos = new BlockPos(rx, ry, rz);
					IBlockState place = block.getStateFromMeta(meta);
					world.setBlockState(pos, place, 2);

					if(state.nbt != null) {
						TileEntity te = buildTileEntity(world, state.nbt, coordBaseMode, null);
						world.setTileEntity(pos, te);
					}
				}
			}
		}
	}

	// Used to construct tandems
	public void build(World world, JigsawPiece piece, int x, int y, int z, int coordBaseMode, String structureName) {
		StructureBoundingBox bb = switch (coordBaseMode) {
			case 1, 3 ->
					new StructureBoundingBox(x, y, z, x + piece.structure.size.z - 1, y + piece.structure.size.y - 1, z + piece.structure.size.x - 1);
			default ->
					new StructureBoundingBox(x, y, z, x + piece.structure.size.x - 1, y + piece.structure.size.y - 1, z + piece.structure.size.z - 1);
		};

		build(world, piece, bb, bb, coordBaseMode, structureName);
	}

	protected boolean build(World world, JigsawPiece piece, StructureBoundingBox totalBounds, StructureBoundingBox generatingBounds, int coordBaseMode, String structureName) {
		if(!isLoaded) {
			MainRegistry.logger.info("NBTStructure is invalid");
			return false;
		}

		final int sx = blockArray.length;
		final int sy = blockArray[0].length;
		final int sz = blockArray[0][0].length;
		final int maxIdxX = sx - 1;
		final int maxIdxZ = sz - 1;

        int sizeX = totalBounds.maxX - totalBounds.minX;
        int sizeZ = totalBounds.maxZ - totalBounds.minZ;

        int absMinX = Math.max(generatingBounds.minX - totalBounds.minX, 0);
        int absMinZ = Math.max(generatingBounds.minZ - totalBounds.minZ, 0);
        int absMaxX = Math.min(generatingBounds.maxX - totalBounds.minX, sizeX);
        int absMaxZ = Math.min(generatingBounds.maxZ - totalBounds.minZ, sizeZ);
        if (absMinX > sizeX || absMinZ > sizeZ || absMaxX < 0 || absMaxZ < 0) return true;

		int ux1 = unrotateX(absMinX, absMinZ, coordBaseMode);
		int uz1 = unrotateZ(absMinX, absMinZ, coordBaseMode);
		int ux2 = unrotateX(absMinX, absMaxZ, coordBaseMode);
		int uz2 = unrotateZ(absMinX, absMaxZ, coordBaseMode);
		int ux3 = unrotateX(absMaxX, absMinZ, coordBaseMode);
		int uz3 = unrotateZ(absMaxX, absMinZ, coordBaseMode);
		int ux4 = unrotateX(absMaxX, absMaxZ, coordBaseMode);
		int uz4 = unrotateZ(absMaxX, absMaxZ, coordBaseMode);

		int minX = Math.max(0, Math.min(Math.min(ux1, ux2), Math.min(ux3, ux4)));
		int maxX = Math.min(maxIdxX, Math.max(Math.max(ux1, ux2), Math.max(ux3, ux4)));
		int minZ = Math.max(0, Math.min(Math.min(uz1, uz2), Math.min(uz3, uz4)));
		int maxZ = Math.min(maxIdxZ, Math.max(Math.max(uz1, uz2), Math.max(uz3, uz4)));

        if (piece.blockTable != null || piece.platform != null) {
            int biomeX = generatingBounds.minX + (generatingBounds.maxX - generatingBounds.minX + 1) / 2;
            int biomeZ = generatingBounds.minZ + (generatingBounds.maxZ - generatingBounds.minZ + 1) / 2;
            Biome biome = world.getBiome(new BlockPos(biomeX, 0, biomeZ));

            if (piece.blockTable != null) {
                for (StructureComponent.BlockSelector selector : piece.blockTable.values()) {
                    if (selector instanceof BiomeBlockSelector biomeSelector) {
                        biomeSelector.nextBiome = biome;
                    }
                }
            }

            if (piece.platform instanceof BiomeBlockSelector biomeSelector) {
                biomeSelector.nextBiome = biome;
            }
        }

		for(int bx = minX; bx <= maxX; bx++) {
			for(int bz = minZ; bz <= maxZ; bz++) {

				if (bx < 0 || bx >= sx || bz < 0 || bz >= sz) continue;

				int rx = rotateX(bx, bz, coordBaseMode) + totalBounds.minX;
				int rz = rotateZ(bx, bz, coordBaseMode) + totalBounds.minZ;
				int oy = piece.conformToTerrain ? world.getTopSolidOrLiquidBlock(new BlockPos(rx, 0, rz)).getY() + piece.heightOffset : totalBounds.minY;
                boolean hasBase = false;

				for(int by = 0; by < sy; by++) {
					BlockState state = blockArray[bx][by][bz];
					if(state == null) continue;

					int ry = by + oy;

					Block block = transformBlock(state.definition, piece.blockTable, world.rand);
					int meta = transformMeta(state.definition, piece.blockTable, coordBaseMode);
                    if (ry < 1) continue;

					BlockPos pos = new BlockPos(rx, ry, rz);
					IBlockState place = block.getStateFromMeta(meta);
					world.setBlockState(pos, place, 2);

					if(state.nbt != null) {
						TileEntity te = buildTileEntity(world, state.nbt, coordBaseMode, structureName);
						world.setTileEntity(pos, te);
					}

                    if (by == 0 && piece.platform != null && !place.getMaterial().isReplaceable()) {
                        hasBase = true;
                    }
				}

                if (hasBase && piece.platform != null && !piece.conformToTerrain) {
                    for (int y = oy - 1; y > 0; y--) {
                        BlockPos fillPos = new BlockPos(rx, y, rz);
                        if (!world.getBlockState(fillPos).getMaterial().isReplaceable()) break;
                        piece.platform.selectBlocks(world.rand, 0, 0, 0, false);
                        world.setBlockState(fillPos, piece.platform.getBlockState(), 2);
                    }
				}
			}
		}

		return true;
	}

	public List<JigsawConnection> getConnectionPool(EnumFacing dir, String target) {
		if(dir == EnumFacing.DOWN) {
			return toTopConnections.get(target);
		} else if(dir == EnumFacing.UP) {
			return toBottomConnections.get(target);
		}

		return toHorizontalConnections.get(target);
	}

	// What a fucken mess, why even implement the IntArray NBT if ye aint gonna use it Moe Yang?
	private ThreeInts parsePos(NBTTagList pos) {
		int x = pos.getIntAt(0);
		int y = pos.getIntAt(1);
		int z = pos.getIntAt(2);

		return new ThreeInts(x, y, z);
	}

	// NON-STANDARD, items are serialized with IDs, which will differ from world to world!
	// So our fixed exporter adds an itemPalette, please don't hunt me down for fucking with the spec
    //mlbv: we no longer need it as 1.12.2 use String id for items
	private void relinkItems(HashMap<Short, Short> palette, NBTTagCompound nbt) {
		NBTTagList items = null;
		if(nbt.hasKey("items"))
			items = nbt.getTagList("items", NBT.TAG_COMPOUND);
		if(nbt.hasKey("Items"))
			items = nbt.getTagList("Items", NBT.TAG_COMPOUND);

		if(items == null) return;

		for(int i = 0; i < items.tagCount(); i++) {
			NBTTagCompound item = items.getCompoundTagAt(i);
			item.setShort("id", palette.get(item.getShort("id")));
		}
	}

    private Map<Short, String> getLegacyItemIdToNameMap() {
        if (itemPalette == null || itemPalette.isEmpty())
            return null;

        if (legacyItemIdToName == null) {
            legacyItemIdToName = new HashMap<>(itemPalette.size());
            for (Pair<Short, String> entry : itemPalette) {
                legacyItemIdToName.put(entry.key, entry.value);
            }
        }

        return legacyItemIdToName;
    }

    private void nbtFixerUpper(NBTTagCompound teNbt, Map<Short, String> idPalette) {
        if (teNbt == null) return;

        fixLegacyBlockNames(teNbt);
        fixLegacyItemStackIds(teNbt, idPalette);

        // Keep legacy lists intact: structure parity depends on many TEs still deserializing "items" or
        // "Items" directly. If a modern inventory wrapper is absent, synthesize it additively instead.
        if (teNbt.hasKey("inventory", Constants.NBT.TAG_COMPOUND)) return;
        String listKey = null;
        if (teNbt.hasKey("items", Constants.NBT.TAG_LIST)) listKey = "items";
        else if (teNbt.hasKey("Items", Constants.NBT.TAG_LIST)) listKey = "Items";
        if (listKey == null) return;
        NBTTagList oldList = teNbt.getTagList(listKey, Constants.NBT.TAG_COMPOUND);
        if (oldList.tagCount() == 0) return;
        int maxSlot = -1;
        for (int i = 0; i < oldList.tagCount(); i++) {
            NBTTagCompound oldStack = oldList.getCompoundTagAt(i);
            int slot = getSlot(oldStack);
            if (slot >= 0 && slot > maxSlot) {
                maxSlot = slot;
            }
        }
        if (maxSlot < 0) return;
        NBTTagList newItems = new NBTTagList();
        for (int i = 0; i < oldList.tagCount(); i++) {
            NBTTagCompound oldStack = oldList.getCompoundTagAt(i);
            int slot = getSlot(oldStack);
            if (slot < 0) continue;
            String idString = null;
            if (oldStack.hasKey("id", Constants.NBT.TAG_STRING)) {
                idString = oldStack.getString("id");
            } else if (oldStack.hasKey("id", Constants.NBT.TAG_SHORT)) {
                short legacyId = oldStack.getShort("id");
                idString = mapId(legacyId, idPalette);
            } else if (oldStack.hasKey("id", Constants.NBT.TAG_INT)) {
                int legacyId = oldStack.getInteger("id");
                if (legacyId >= Short.MIN_VALUE && legacyId <= Short.MAX_VALUE) {
                    idString = mapId((short) legacyId, idPalette);
                } else {
                    Item item = Item.getItemById(legacyId);
                    if (item.getRegistryName() != null) {
                        idString = item.getRegistryName().toString();
                    }
                }
            }
            if (idString == null || idString.isEmpty()) continue;
            NBTTagCompound newStack = new NBTTagCompound();
            newStack.setInteger("Slot", slot);
            newStack.setString("id", idString);
            if (oldStack.hasKey("Count", Constants.NBT.TAG_BYTE)) {
                newStack.setByte("Count", oldStack.getByte("Count"));
            } else {
                newStack.setByte("Count", (byte) 1);
            }
            if (oldStack.hasKey("Damage", Constants.NBT.TAG_SHORT)) {
                newStack.setShort("Damage", oldStack.getShort("Damage"));
            } else {
                newStack.setShort("Damage", (short) 0);
            }
            if (oldStack.hasKey("tag", Constants.NBT.TAG_COMPOUND)) {
                newStack.setTag("tag", oldStack.getCompoundTag("tag"));
            }
            if (oldStack.hasKey("ForgeCaps", Constants.NBT.TAG_COMPOUND)) {
                newStack.setTag("ForgeCaps", oldStack.getCompoundTag("ForgeCaps"));
            }

            newItems.appendTag(newStack);
        }

        if (newItems.tagCount() == 0) return;
        NBTTagCompound invTag = new NBTTagCompound();
        invTag.setTag("Items", newItems);
        //invTag.setInteger("Size", maxSlot + 1);
        // Th3_Sl1ze: problem is, if any ticking update tries to access greater slot than maxSlot + 1 (e.g. fluid tank trying to empty a fluid container),
        // you'll get into a fucking miracle of corrupted saves
        teNbt.setTag("inventory", invTag);
        MainRegistry.logger.debug("[NBTStructure] Added inventory wrapper for TE with {} items in structure {}", newItems.tagCount(), resource);
    }

    private void fixLegacyBlockNames(NBTBase tag) {
        if (tag instanceof NBTTagCompound compound) {
            if (compound.hasKey("block", Constants.NBT.TAG_STRING)) {
                int meta = compound.hasKey("meta", Constants.NBT.TAG_ANY_NUMERIC) ? compound.getInteger("meta") : 0;
                LegacyBlockDefinition remappedDefinition = remapLegacyBlockDefinition(compound.getString("block"), meta);
                if (remappedDefinition != null) {
                    compound.setString("block", remappedDefinition.name);
                    compound.setInteger("meta", remappedDefinition.meta);
                } else {
                    String remapped = remapLegacyBlockName(compound.getString("block"));
                    if (remapped != null) {
                        compound.setString("block", remapped);
                    }
                }
            }

            for (String key : compound.getKeySet()) {
                fixLegacyBlockNames(compound.getTag(key));
            }
            return;
        }

        if (tag instanceof NBTTagList list) {
            for (int i = 0; i < list.tagCount(); i++) {
                fixLegacyBlockNames(list.get(i));
            }
        }
    }

    private void fixLegacyItemStackIds(NBTBase tag, Map<Short, String> idPalette) {
        if (tag instanceof NBTTagCompound compound) {
            if (looksLikeLegacyItemStack(compound)) {
                String remapped = remapLegacyItemId(compound, idPalette);
                if (remapped != null && !remapped.isEmpty()) {
                    compound.setString("id", remapped);
                }
            }

            for (String key : compound.getKeySet()) {
                fixLegacyItemStackIds(compound.getTag(key), idPalette);
            }
            return;
        }

        if (tag instanceof NBTTagList list) {
            for (int i = 0; i < list.tagCount(); i++) {
                fixLegacyItemStackIds(list.get(i), idPalette);
            }
        }
    }

    private boolean looksLikeLegacyItemStack(NBTTagCompound tag) {
        if (!tag.hasKey("id")) return false;

        return tag.hasKey("Count", Constants.NBT.TAG_BYTE)
                || tag.hasKey("Damage", Constants.NBT.TAG_SHORT)
                || tag.hasKey("tag", Constants.NBT.TAG_COMPOUND)
                || tag.hasKey("ForgeCaps", Constants.NBT.TAG_COMPOUND)
                || tag.hasKey("Slot", Constants.NBT.TAG_BYTE)
                || tag.hasKey("Slot", Constants.NBT.TAG_INT)
                || tag.hasKey("slot", Constants.NBT.TAG_BYTE)
                || tag.hasKey("slot", Constants.NBT.TAG_INT);
    }

    private @Nullable String remapLegacyItemId(NBTTagCompound itemTag, Map<Short, String> idPalette) {
        if (itemTag.hasKey("id", Constants.NBT.TAG_STRING)) {
            String idString = itemTag.getString("id");
            if (idString.isEmpty()) return null;

            Item item = Item.getByNameOrId(idString);
            if (item != null && item.getRegistryName() != null) {
                return item.getRegistryName().toString();
            }

            return idString.contains(":") ? idString : null;
        }

        if (itemTag.hasKey("id", Constants.NBT.TAG_SHORT)) {
            return mapId(itemTag.getShort("id"), idPalette);
        }

        if (itemTag.hasKey("id", Constants.NBT.TAG_INT)) {
            return mapId(itemTag.getInteger("id"), idPalette);
        }

        return null;
    }

    private static int getSlot(NBTTagCompound tag) {
        int id = tag.getTagId("slot");
        if (id != 0) {
            if (id == NBT.TAG_BYTE) {
                return tag.getByte("slot") & 0xFF;
            } else if (id == NBT.TAG_INT) {
                return tag.getInteger("slot");
            }
        } else {
            id = tag.getTagId("Slot");
            if (id != 0) {
                if (id == NBT.TAG_BYTE) {
                    return tag.getByte("Slot") & 0xFF;
                } else if (id == NBT.TAG_INT) {
                    return tag.getInteger("Slot");
                }
            }
        }
        return -1;
    }

    private @Nullable String mapId(int legacyId, Map<Short, String> idPalette) {
        if (legacyId >= Short.MIN_VALUE && legacyId <= Short.MAX_VALUE && idPalette != null) {
            String name = idPalette.get((short) legacyId);
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }

        Item item = Item.getItemById(legacyId);
        //noinspection ConstantValue
        if (item != null && item.getRegistryName() != null) {
            return item.getRegistryName().toString();
        }

        MainRegistry.logger.debug("[NBTStructure] Could not find id {} in structure {}'s item palette", legacyId, resource);
        return null;
    }

    private static Block resolveBlockName(String name) {
        Block block = Block.getBlockFromName(name);
        if (block != null) {
            return block;
        }

        String remapped = remapLegacyBlockName(name);
        if (remapped != null) {
            block = Block.getBlockFromName(remapped);
            if (block != null) {
                return block;
            }
        }

        return Blocks.AIR;
    }

    private static @Nullable LegacyBlockDefinition remapLegacyBlockDefinition(String name, int meta) {
        String normalized = remapLegacyBlockName(name);
        if (normalized == null) {
            normalized = name;
        }

        return switch (normalized) {
            case Tags.MODID + ":brick_slab" -> remapLegacySlab(meta, LEGACY_BRICK_SLABS, true);
            case Tags.MODID + ":brick_double_slab" -> remapLegacySlab(meta, LEGACY_BRICK_DOUBLE_SLABS, false);
            case Tags.MODID + ":concrete_brick_slab" -> remapLegacySlab(meta, LEGACY_CONCRETE_BRICK_SLABS, true);
            case Tags.MODID + ":concrete_brick_double_slab" -> remapLegacySlab(meta, LEGACY_CONCRETE_BRICK_DOUBLE_SLABS, false);
            default -> null;
        };
    }

    private static @Nullable LegacyBlockDefinition remapLegacySlab(int meta, String[] variants, boolean preserveHalf) {
        int variant = meta & 7;
        if (variant < 0 || variant >= variants.length) {
            return null;
        }

        return new LegacyBlockDefinition(variants[variant], preserveHalf ? meta & 8 : 0);
    }

    private static @Nullable String remapLegacyBlockName(String name) {
        String substituted = substitutions.get(name);
        if (substituted != null) {
            return substituted;
        }

        String prefix = Tags.MODID + ":tile.";
        if (name.startsWith(prefix)) {
            String stripped = Tags.MODID + ":" + name.substring(prefix.length());
            return substitutions.getOrDefault(stripped, stripped);
        }

        return null;
    }

    private static class LegacyBlockDefinition {

        final String name;
        final int meta;

        private LegacyBlockDefinition(String name, int meta) {
            this.name = name;
            this.meta = meta;
        }

    }

	private Block transformBlock(BlockDefinition definition, Map<Block, StructureComponent.BlockSelector> blockTable, Random rand) {
		if(blockTable != null && blockTable.containsKey(definition.block)) {
			final StructureComponent.BlockSelector selector = blockTable.get(definition.block);
			selector.selectBlocks(rand, 0, 0, 0, false); // fuck the vanilla shit idc
			return selector.getBlockState().getBlock();
		}

		if(definition.block instanceof INBTBlockTransformable) return ((INBTBlockTransformable) definition.block).transformBlock(definition.block);

		return definition.block;
	}

	private int transformMeta(BlockDefinition definition, Map<Block, StructureComponent.BlockSelector> blockTable, int coordBaseMode) {
		if(blockTable != null && blockTable.containsKey(definition.block)) {
			IBlockState sel = blockTable.get(definition.block).getBlockState();
			return sel.getBlock().getMetaFromState(sel);
		}

		// Our shit
		if(definition.block instanceof INBTBlockTransformable) return ((INBTBlockTransformable) definition.block).transformMeta(definition.meta, coordBaseMode);

		if(coordBaseMode == 0) return definition.meta;

		// Vanilla shit
		if(definition.block instanceof BlockStairs) return INBTBlockTransformable.transformMetaStairs(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockRotatedPillar) return INBTBlockTransformable.transformMetaPillar(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockDirectional) return INBTBlockTransformable.transformMetaDirectional(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockTorch) return INBTBlockTransformable.transformMetaTorch(definition.meta, coordBaseMode);
        if(definition.block instanceof BlockButton) return INBTBlockTransformable.transformMetaTorch(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockDoor) return INBTBlockTransformable.transformMetaDoor(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockLever) return INBTBlockTransformable.transformMetaLever(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockSign) return INBTBlockTransformable.transformMetaDeco(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockLadder) return INBTBlockTransformable.transformMetaDeco(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockTripWireHook) return INBTBlockTransformable.transformMetaDirectional(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockVine) return INBTBlockTransformable.transformMetaVine(definition.meta, coordBaseMode);
		if(definition.block instanceof BlockTrapDoor) return INBTBlockTransformable.transformMetaTrapdoor(definition.meta, coordBaseMode);
		return definition.meta;
	}

	public int rotateX(int x, int z, int coordBaseMode) {
		return switch (coordBaseMode) {
			case 1 -> size.z - 1 - z;
			case 2 -> size.x - 1 - x;
			case 3 -> z;
			default -> x;
		};
	}

	public int rotateZ(int x, int z, int coordBaseMode) {
		return switch (coordBaseMode) {
			case 1 -> x;
			case 2 -> size.z - 1 - z;
			case 3 -> size.x - 1 - x;
			default -> z;
		};
	}

	private int unrotateX(int x, int z, int coordBaseMode) {
		return switch (coordBaseMode) {
			case 3 -> size.x - 1 - z;
			case 2 -> size.x - 1 - x;
			case 1 -> z;
			default -> x;
		};
	}

	private int unrotateZ(int x, int z, int coordBaseMode) {
		return switch (coordBaseMode) {
			case 3 -> x;
			case 2 -> size.z - 1 - z;
			case 1 -> size.z - 1 - x;
			default -> z;
		};
	}

	private static class BlockState {

		final BlockDefinition definition;
		NBTTagCompound nbt;

		BlockState(BlockDefinition definition) {
			this.definition = definition;
		}

	}

	private static class BlockDefinition {

		final Block block;
		final int meta;

		BlockDefinition(String name, int meta) {
            LegacyBlockDefinition remapped = remapLegacyBlockDefinition(name, meta);
            if (remapped != null) {
                this.block = resolveBlockName(remapped.name);
                this.meta = remapped.meta;
                return;
            }

			this.block = resolveBlockName(name);
			this.meta = meta;
		}

		BlockDefinition(Block block, int meta) {
			this.block = block;
			this.meta = meta;
		}

	}

	// Each jigsaw block in a structure will instance one of these
	public static class JigsawConnection {

		public final ThreeInts pos;
		public final EnumFacing dir;

		// what pool should we look through to find a connection
		private final String poolName;

		// when we successfully find a pool, what connections in that jigsaw piece can we target
		private final String targetName;

		private final boolean isRollable;

		private final int selectionPriority;
		private final int placementPriority;

		private JigsawConnection(ThreeInts pos, EnumFacing dir, String poolName, String targetName, boolean isRollable, int selectionPriority, int placementPriority) {
			this.pos = pos;
			this.dir = dir;
			this.poolName = poolName;
			this.targetName = targetName;
			this.isRollable = isRollable;
			this.selectionPriority = selectionPriority;
			this.placementPriority = placementPriority;
		}

	}

	public static class Component extends StructureComponent {

		JigsawPiece piece;

		int minHeight = 1;
		int maxHeight = 128;

		boolean heightUpdated = false;

		int priority;

		// this is fucking hacky but we need a way to update ALL component bounds once a Y-level is determined
		private Start parent;

		private JigsawConnection connectedFrom;

		// keep our own rotation index (0..3) for custom rotation math
		private int coordBaseMode;

		public Component() {}

		public Component(SpawnCondition spawn, JigsawPiece piece, Random rand, int x, int z) {
			this(spawn, piece, rand, x, 0, z, rand.nextInt(4));
		}

        public Component(SpawnCondition spawn, JigsawPiece piece, Random rand, int x, int y, int z, int coordBaseMode) {
            super(0);
            this.coordBaseMode = coordBaseMode;
            this.piece = piece;
            this.minHeight = spawn.minHeight;
            this.maxHeight = spawn.maxHeight;
            this.boundingBox = switch (coordBaseMode) {
                case 1, 3 ->
                        new StructureBoundingBox(x, y, z, x + piece.structure.size.z - 1, y + piece.structure.size.y - 1, z + piece.structure.size.x - 1);
                default ->
                        new StructureBoundingBox(x, y, z, x + piece.structure.size.x - 1, y + piece.structure.size.y - 1, z + piece.structure.size.z - 1);
            };
        }

		public Component connectedFrom(JigsawConnection connection) {
			this.connectedFrom = connection;
			return this;
		}

		// Save to NBT
		@Override
		protected void writeStructureToNBT(NBTTagCompound nbt) {
			nbt.setString("piece", piece != null ? piece.name : "NULL");
			nbt.setInteger("min", minHeight);
			nbt.setInteger("max", maxHeight);
			nbt.setBoolean("hasHeight", heightUpdated);
			nbt.setInteger("rot", coordBaseMode);
		}

		// Load from NBT
		@Override
		protected void readStructureFromNBT(NBTTagCompound nbt, TemplateManager templateManager) {
			piece = JigsawPiece.jigsawMap.get(nbt.getString("piece"));
			minHeight = nbt.getInteger("min");
			maxHeight = nbt.getInteger("max");
			heightUpdated = nbt.getBoolean("hasHeight");
			coordBaseMode = nbt.hasKey("rot") ? nbt.getInteger("rot") : 0;
		}

		@Override
		public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
			if (piece == null) return false;

			// now we're in the world, update minY/maxY
			if (!piece.conformToTerrain && !heightUpdated) {
                int averageHeight = getAverageHeight(world, box) + piece.heightOffset;
                boolean isFlatWorld = world.getWorldInfo().getTerrainType() == net.minecraft.world.WorldType.FLAT;
				int y = isFlatWorld ? averageHeight : MathHelper.clamp(averageHeight, minHeight, maxHeight);

				if (!piece.alignToTerrain) {
					parent.offsetYHeight(y);
				} else {
					offsetYHeight(y);
				}
			}

			return piece.structure.build(world, piece, boundingBox, box, coordBaseMode, parent.name);
		}

		public void offsetYHeight(int y) {
			boundingBox.minY += y;
			boundingBox.maxY += y;

			heightUpdated = true;
		}

		// Overrides to fix Mojang's fucked rotations which FLIP instead of rotating in two instances
		// vaer being in the mines doing this the hard way for years was absolutely not for naught
		@Override
		protected int getXWithOffset(int x, int z) {
			return boundingBox.minX + piece.structure.rotateX(x, z, coordBaseMode);
		}

		@Override
		protected int getYWithOffset(int y) {
			return boundingBox.minY + y;
		}

		@Override
		protected int getZWithOffset(int x, int z) {
			return boundingBox.minZ + piece.structure.rotateZ(x, z, coordBaseMode);
		}

		private EnumFacing rotateDir(EnumFacing dir) {
			if (dir == EnumFacing.UP || dir == EnumFacing.DOWN) return dir;
			return switch (coordBaseMode) {
				default -> dir;
				case 1 -> dir.rotateY();
				case 2 -> dir.getOpposite();
				case 3 -> dir.rotateYCCW();
			};
		}

		private int getAverageHeight(World world, StructureBoundingBox box) {
			int total = 0;
			int iterations = 0;

			for (int z = box.minZ; z <= box.maxZ; z++) {
				for (int x = box.minX; x <= box.maxX; x++) {
					total += world.getHeight(new BlockPos(x, 0, z)).getY();
					iterations++;
				}
			}

			if (iterations == 0)
				return 64;

			return total / iterations;
		}

		private int getNextCoordBase(JigsawConnection fromConnection, JigsawConnection toConnection, Random rand) {
			if (fromConnection.dir == EnumFacing.DOWN || fromConnection.dir == EnumFacing.UP) {
				if (fromConnection.isRollable) return rand.nextInt(4);
				return coordBaseMode;
			}

			return directionOffsetToCoordBase(fromConnection.dir.getOpposite(), toConnection.dir);
		}

		private int directionOffsetToCoordBase(EnumFacing from, EnumFacing to) {
			EnumFacing cur = from;
			for (int i = 0; i < 4; i++) {
				if (cur == to) return (i + coordBaseMode) % 4;
				cur = cur.rotateYCCW();
			}
			return coordBaseMode;
		}

		protected boolean hasIntersectionIgnoringSelf(List<StructureComponent> components, StructureBoundingBox box) {
			for (StructureComponent component : components) {
				if (component == this) continue;

				if (component.getBoundingBox().intersectsWith(box)) return true;
			}

			return false;
		}

		protected boolean isInsideIgnoringSelf(List<StructureComponent> components, int x, int y, int z) {
			for (StructureComponent component : components) {
				if (component == this) continue;

				if (component.getBoundingBox().isVecInside(new BlockPos(x, y, z))) return true;
			}

			return false;
		}

	}

	public static class Start extends StructureStart {

		public String name;

		public Start() {}

		public Start(World world, Random rand, SpawnCondition spawn, int chunkX, int chunkZ) {
			super(chunkX, chunkZ);

			name = spawn.name;

			int x = chunkX << 4;
			int z = chunkZ << 4;

			JigsawPiece startPiece = spawn.structure != null ? spawn.structure : spawn.pools.get(spawn.startPool).get(rand);

			Component startComponent = new Component(spawn, startPiece, rand, x, z);
			startComponent.parent = this;

			this.components.add(startComponent);

			List<Component> queuedComponents = new ArrayList<>();
			if (spawn.structure == null) queuedComponents.add(startComponent);
            Set<JigsawPiece> requiredPieces = findRequiredPieces(spawn);

			// Iterate through and build out all the components we intend to spawn
			while (!queuedComponents.isEmpty()) {
				queuedComponents.sort((a, b) -> b.priority - a.priority); // sort by placement priority descending
				int matchPriority = queuedComponents.get(0).priority;
				int max = 1;
				while (max < queuedComponents.size()) {
					if (queuedComponents.get(max).priority != matchPriority) break;
					max++;
				}

				final int i = rand.nextInt(max);
				Component fromComponent = queuedComponents.remove(i);

				if (fromComponent.piece.structure.fromConnections == null) continue;

				int distance = getDistanceTo(fromComponent.getBoundingBox());
                boolean fallbacksOnly = (requiredPieces.isEmpty() && (this.components.size() >= spawn.sizeLimit || distance >= spawn.rangeLimit))
                        || this.components.size() > 1024;

				for (List<JigsawConnection> unshuffledList : fromComponent.piece.structure.fromConnections) {
					List<JigsawConnection> connectionList = new ArrayList<>(unshuffledList);
					Collections.shuffle(connectionList, rand);

					for (JigsawConnection fromConnection : connectionList) {
						if (fromComponent.connectedFrom == fromConnection) continue; // if we already connected to this piece, don't process

						if (fallbacksOnly) {
							String fallback = spawn.pools.get(fromConnection.poolName).fallback;

							if (fallback != null) {
								Component fallbackComponent = buildNextComponent(rand, spawn, spawn.pools.get(fallback), fromComponent, fromConnection);
								addComponent(fallbackComponent, fromConnection.placementPriority);
							}

							continue;
						}

						JigsawPool nextPool = spawn.getPool(fromConnection.poolName);
						if (nextPool == null) {
							MainRegistry.logger.warn("[Jigsaw] Jigsaw block points to invalid pool: " + fromConnection.poolName);
							continue;
						}

						Component nextComponent = null;

						// Iterate randomly through the pool, attempting each piece until one fits
						while (nextPool.totalWeight > 0) {
							nextComponent = buildNextComponent(rand, spawn, nextPool, fromComponent, fromConnection);
							if (nextComponent != null && !fromComponent.hasIntersectionIgnoringSelf(this.components, nextComponent.getBoundingBox())) break;
							nextComponent = null;
						}

						if (nextComponent != null) {
							addComponent(nextComponent, fromConnection.placementPriority);
							queuedComponents.add(nextComponent);
                            requiredPieces.remove(nextComponent.piece);
						} else {
							// If we failed to fit anything in, grab something from the fallback pool, ignoring bounds check
							// unless we are perfectly abutting another piece, so grid layouts can work!
							if (nextPool.fallback != null) {
								BlockPos checkPos = getConnectionTargetPosition(fromComponent, fromConnection);

								if (!fromComponent.isInsideIgnoringSelf(this.components, checkPos.getX(), checkPos.getY(), checkPos.getZ())) {
									nextComponent = buildNextComponent(rand, spawn, spawn.pools.get(nextPool.fallback), fromComponent, fromConnection);
									addComponent(nextComponent, fromConnection.placementPriority); // don't add to queued list, we don't want to try continue from fallback
								}
							}
						}
					}
				}
			}

			if (GeneralConfig.enableDebugMode) {
				MainRegistry.logger.info("[Debug] Spawning NBT structure " + name + " with " + this.components.size() + " piece(s) at: " + chunkX * 16 + ", " + chunkZ * 16);
				String componentList = "[Debug] Components: ";
				for (StructureComponent component : this.components) {
					componentList += ((Component) component).piece.structure.name + " ";
				}
				MainRegistry.logger.info(componentList);
			}

			updateBoundingBox();
		}

		private void addComponent(Component component, int placementPriority) {
			if (component == null) return;
			this.components.add(component);

			component.parent = this;
			component.priority = placementPriority;
		}

		private BlockPos getConnectionTargetPosition(Component component, JigsawConnection connection) {
			// The direction this component is extending towards in ABSOLUTE direction
			EnumFacing extendDir = component.rotateDir(connection.dir);

			// Set the starting point for the next structure to the location of the connector block
			int x = component.getXWithOffset(connection.pos.x, connection.pos.z) + extendDir.getXOffset();
			int y = component.getYWithOffset(connection.pos.y) + extendDir.getYOffset();
			int z = component.getZWithOffset(connection.pos.x, connection.pos.z) + extendDir.getZOffset();

			return new BlockPos(x, y, z);
		}

        private Set<JigsawPiece> findRequiredPieces(SpawnCondition spawn) {
            Set<JigsawPiece> requiredPieces = new HashSet<>();

            if (spawn.pools == null) return requiredPieces;

            for (JigsawPool pool : spawn.pools.values()) {
                for (Pair<JigsawPiece, Integer> weight : pool.pieces) {
                    if (weight.getKey().required) {
                        requiredPieces.add(weight.getKey());
                    }
                }
            }

            return requiredPieces;
        }

		private Component buildNextComponent(Random rand, SpawnCondition spawn, JigsawPool pool, Component fromComponent, JigsawConnection fromConnection) {
			JigsawPiece nextPiece = pool.get(rand);
			if (nextPiece == null) {
				MainRegistry.logger.warn("[Jigsaw] Pool returned null piece: " + fromConnection.poolName);
				return null;
			}

            if (nextPiece.instanceLimit > 0) {
                int instances = 0;
                for (StructureComponent component : this.components) {
                    if (component instanceof Component && ((Component) component).piece == nextPiece) {
                        instances++;
                        if (instances >= nextPiece.instanceLimit) return null;
                    }
                }
            }

			List<JigsawConnection> connectionPool = nextPiece.structure.getConnectionPool(fromConnection.dir, fromConnection.targetName);
			if (connectionPool == null || connectionPool.isEmpty()) {
				MainRegistry.logger.warn("[Jigsaw] No valid connections for: " + fromConnection.targetName + " - in piece: " + nextPiece.name);
				return null;
			}

			JigsawConnection toConnection = connectionPool.get(rand.nextInt(connectionPool.size()));

			// Rotate our incoming piece to plug it in
			int nextCoordBase = fromComponent.getNextCoordBase(fromConnection, toConnection, rand);

			BlockPos pos = getConnectionTargetPosition(fromComponent, fromConnection);

			// offset the starting point to the connecting point
			int ox = nextPiece.structure.rotateX(toConnection.pos.x, toConnection.pos.z, nextCoordBase);
			int oy = toConnection.pos.y;
			int oz = nextPiece.structure.rotateZ(toConnection.pos.x, toConnection.pos.z, nextCoordBase);

			return new Component(spawn, nextPiece, rand, pos.getX() - ox, pos.getY() - oy, pos.getZ() - oz, nextCoordBase).connectedFrom(toConnection);
		}

		private int getDistanceTo(StructureBoundingBox box) {
			int x = getCenterX(box);
			int z = getCenterZ(box);

			return Math.max(Math.abs(x - (this.getChunkPosX() << 4)), Math.abs(z - (this.getChunkPosZ() << 4)));
		}

		public int getCenterX(StructureBoundingBox box) { return box.minX + (box.maxX - box.minX + 1) / 2; }
		public int getCenterZ(StructureBoundingBox box)
		{
			return box.minZ + (box.maxZ - box.minZ + 1) / 2;
		}

		// post loading, update parent reference for loaded components
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			name = nbt.getString("name");

			for (StructureComponent o : this.components) {
				((Component) o).parent = this;
			}
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setString("name", name);
		}

		public void offsetYHeight(int y) {
			for (StructureComponent o : this.components) {
				Component component = (Component) o;
				if (component.heightUpdated || component.piece.conformToTerrain || component.piece.alignToTerrain) continue;
				component.offsetYHeight(y);
			}
		}

	}

	public static class GenStructure extends MapGenStructure {

		private SpawnCondition nextSpawn;

		public void generateStructures(World world, Random rand, IChunkProvider chunkProvider, int chunkX, int chunkZ) {
			ChunkPrimer primer = new ChunkPrimer();

			this.generate(world, chunkX, chunkZ, primer);
			this.generateStructure(world, rand, new ChunkPos(chunkX, chunkZ));
		}

		@Override
		public String getStructureName() {
			return "NBTStructures";
		}

		@Nullable
		@Override
		public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
			return null;
		}

		@Override
		protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
			nextSpawn = getSpawnAtCoords(chunkX, chunkZ);
			return nextSpawn != null;
		}

		public SpawnCondition getStructureAt(World world, int chunkX, int chunkZ) {
			// make sure the random is in the correct state
			this.world = world;
			this.rand.setSeed(world.getSeed());
			long l = this.rand.nextLong();
			long i1 = this.rand.nextLong();

			long l1 = (long) chunkX * l;
			long i2 = (long) chunkZ * i1;
			this.rand.setSeed(l1 ^ i2 ^ world.getSeed());

			// random nextInt call just before `canSpawnStructureAtCoords`, no, I don't know why Mojang added that
			this.rand.nextInt();

			return getSpawnAtCoords(chunkX, chunkZ);
		}

		private SpawnCondition getSpawnAtCoords(int chunkX, int chunkZ) {
			// attempt to spawn with custom chunk coordinate rules
			if (customSpawnMap.containsKey(world.provider.getDimension())) {
				WorldCoordinate coords = new WorldCoordinate(world, new ChunkPos(chunkX, chunkZ), rand);

				List<SpawnCondition> spawnList = customSpawnMap.get(world.provider.getDimension());
				for (SpawnCondition spawn : spawnList) {
					if ((spawn.pools != null || spawn.structure != null) && spawn.checkCoordinates.test(coords)) {
						return spawn;
					}
				}
			}

			if (!weightedMap.containsKey(world.provider.getDimension()))
				return null;

			int x = chunkX;
			int z = chunkZ;

			if (x < 0) x -= StructureConfig.structureMaxChunks - 1;
			if (z < 0) z -= StructureConfig.structureMaxChunks - 1;

			x /= StructureConfig.structureMaxChunks;
			z /= StructureConfig.structureMaxChunks;
			rand.setSeed((long) x * 341873128712L + (long) z * 132897987541L + this.world.getWorldInfo().getSeed() + 996996996L - world.provider.getDimension());
			x *= StructureConfig.structureMaxChunks;
			z *= StructureConfig.structureMaxChunks;
			x += rand.nextInt(StructureConfig.structureMaxChunks - StructureConfig.structureMinChunks);
			z += rand.nextInt(StructureConfig.structureMaxChunks - StructureConfig.structureMinChunks);

			if (chunkX == x && chunkZ == z) {
				Biome biome = this.world.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));

				SpawnCondition spawn = findSpawn(biome);

				if (spawn != null && (spawn.pools != null || spawn.start != null || spawn.structure != null))
					return spawn;
			}

			return null;
		}

		@Override
		protected StructureStart getStructureStart(int chunkX, int chunkZ) {
			if (nextSpawn.start != null)
				return nextSpawn.start.apply(new Quartet<>(this.world, this.rand, chunkX, chunkZ));
			return new Start(this.world, this.rand, nextSpawn, chunkX, chunkZ);
		}

		private SpawnCondition findSpawn(Biome biome) {
            int biomeId = Biome.getIdForBiome(biome);
            Map<Integer, WeightedSpawnList> dimensionCache = validBiomeCache.computeIfAbsent(world.provider.getDimension(), integer -> new HashMap<>());

            WeightedSpawnList filteredList;
            if (!dimensionCache.containsKey(biomeId)) {
                List<SpawnCondition> spawnList = weightedMap.get(world.provider.getDimension());

                filteredList = new WeightedSpawnList();
                for (SpawnCondition spawn : spawnList) {
                    if (spawn.isValid(biome)) {
                        filteredList.add(spawn);
                        filteredList.totalWeight += spawn.spawnWeight;
                    }
                }

                dimensionCache.put(biomeId, filteredList);
            } else {
                filteredList = dimensionCache.get(biomeId);
            }

            if (filteredList.totalWeight == 0) return null;

            int weight = rand.nextInt(filteredList.totalWeight);
            for (SpawnCondition spawn : filteredList) {
                weight -= spawn.spawnWeight;
                if (weight < 0) {
                    return spawn;
                }
            }

            return null;
		}
	}

    private static class WeightedSpawnList extends ArrayList<SpawnCondition> {

        public int totalWeight = 0;

    }

}
