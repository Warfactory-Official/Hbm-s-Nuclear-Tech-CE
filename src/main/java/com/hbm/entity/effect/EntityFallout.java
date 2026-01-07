package com.hbm.entity.effect;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.blocks.generic.WasteLog;
import com.hbm.config.VersatileConfig;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.main.MainRegistry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * The base class for all fallout entities, only aim for behavioral upstream parity.
 *
 * @author mlbv
 */
public abstract class EntityFallout extends Entity implements IChunkLoader {

    protected static final DataParameter<Integer> SCALE = EntityDataManager.createKey(EntityFallout.class, DataSerializers.VARINT);

    protected double s0;
    protected double s1;
    protected double s2;
    protected double s3;
    protected double s4;
    protected double s5;
    protected double s6;

    protected Ticket loaderTicket;
    protected List<ChunkPos> loadedChunks = new ArrayList<>();

    protected boolean done;

    public EntityFallout(World world) {
        super(world);
        this.ignoreFrustumCheck = false;
        this.isImmuneToFire = true;
    }

    @Override
    protected void entityInit() {
        init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, Type.ENTITY));
        this.dataManager.register(SCALE, 0);
    }

    @Override
    public void init(Ticket ticket) {
        if (!world.isRemote) {
            if (ticket != null) {
                if (loaderTicket == null) {
                    loaderTicket = ticket;
                    loaderTicket.bindEntity(this);
                    loaderTicket.getModData();
                }
                ForgeChunkManager.forceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
            }
        }
    }

    @Override
    public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
        if (!world.isRemote && loaderTicket != null) {
            for (ChunkPos chunk : loadedChunks) {
                ForgeChunkManager.unforceChunk(loaderTicket, chunk);
            }

            loadedChunks.clear();
            loadedChunks.add(new ChunkPos(newChunkX, newChunkZ));
            loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ + 1));
            loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ - 1));
            loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ - 1));
            loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ + 1));
            loadedChunks.add(new ChunkPos(newChunkX + 1, newChunkZ));
            loadedChunks.add(new ChunkPos(newChunkX, newChunkZ + 1));
            loadedChunks.add(new ChunkPos(newChunkX - 1, newChunkZ));
            loadedChunks.add(new ChunkPos(newChunkX, newChunkZ - 1));

            for (ChunkPos chunk : loadedChunks) {
                ForgeChunkManager.forceChunk(loaderTicket, chunk);
            }
        }
    }

    protected void unloadAllChunks() {
        if (loaderTicket != null) {
            for (ChunkPos chunk : loadedChunks) {
                ForgeChunkManager.unforceChunk(loaderTicket, chunk);
            }
        }
    }

    public int getScale() {
        int scale = this.dataManager.get(SCALE);
        return scale == 0 ? 1 : scale;
    }

    protected void calculateS(int i) {
        s0 = 0.8 * i;
        s1 = 0.65 * i;
        s2 = 0.5 * i;
        s3 = 0.4 * i;
        s4 = 0.3 * i;
        s5 = 0.2 * i;
        s6 = 0.1 * i;
    }

    protected double getFallingRadius() {
        return 0;
    }

    protected boolean getSpawnFire() {
        return false;
    }

    protected void placeBlockFromDist(double dist, Block b, BlockPos pos) {
        double ranDist = dist * (1D + world.rand.nextDouble() * 0.2D);
        int meta;
        if (ranDist > s1) meta = 0;
        else if (ranDist > s2) meta = 1;
        else if (ranDist > s3) meta = 2;
        else if (ranDist > s4) meta = 3;
        else if (ranDist > s5) meta = 4;
        else if (ranDist > s6) meta = 5;
        else meta = 6;
        world.setBlockState(pos, b.getStateFromMeta(meta));
    }

    @SuppressWarnings("deprecation")
    protected boolean processBlock(World world, BlockPos.MutableBlockPos pos, double dist, boolean isSurface, int stoneDepth, int maxStoneDepth,
                                   boolean reachedStone, boolean lastReachedStone, int contactHeight) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (world.isAirBlock(pos)) return true;
        if (block instanceof BlockBush) {
            BlockPos lowerPos = pos;
            if (block instanceof BlockDoublePlant) {
                BlockPos upperPos;
                if (world.getBlockState(pos).getValue(BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.LOWER) {
                    upperPos = pos.up();
                } else {
                    lowerPos = pos.down();
                    upperPos = pos;
                }
                world.setBlockState(upperPos, Blocks.AIR.getDefaultState(), 2);
            }
            Block blockBelow = world.getBlockState(lowerPos.down()).getBlock();
            if (blockBelow == Blocks.FARMLAND) {
                placeBlockFromDist(dist, ModBlocks.waste_grass_tall, lowerPos);
            } else if (blockBelow instanceof BlockGrass) {
                placeBlockFromDist(dist, ModBlocks.waste_earth, lowerPos.down());
                placeBlockFromDist(dist, ModBlocks.waste_grass_tall, lowerPos);
            } else if (blockBelow == Blocks.MYCELIUM) {
                placeBlockFromDist(dist, ModBlocks.waste_mycelium, lowerPos.down());
                world.setBlockState(lowerPos, ModBlocks.mush.getDefaultState());
            }
            return true;
        }
        String registryName = Objects.toString(block.getRegistryName(), "");
        switch (registryName) {
            case "minecraft:stone":
            case "minecraft:cobblestone":
                double ranDist = dist * (1D + world.rand.nextDouble() * (isSurface ? 0.2D : 0.1D));

                IBlockState targetBlock;
                if (ranDist > s1 || (isSurface && stoneDepth == maxStoneDepth)) {
                    targetBlock = ModBlocks.sellafield_slaked.getDefaultState();
                } else if (ranDist > s2 || (isSurface && stoneDepth == maxStoneDepth - 1)) {
                    targetBlock = ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 0);
                } else if (ranDist > s3 || (isSurface && stoneDepth == maxStoneDepth - 2)) {
                    targetBlock = ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 1);
                } else if (ranDist > s4 || (isSurface && stoneDepth == maxStoneDepth - 3)) {
                    targetBlock = ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 2);
                } else if (ranDist > s5 || (isSurface && stoneDepth == maxStoneDepth - 4)) {
                    targetBlock = ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 3);
                } else if (ranDist > s6 || (isSurface && stoneDepth == maxStoneDepth - 5)) {
                    targetBlock = ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 4);
                } else {
                    targetBlock = ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 5);
                }


                world.setBlockState(pos, targetBlock);
                return false;
            case "minecraft:bedrock":
            case "hbm:ore_bedrock_oil":
            case "hbm:ore_bedrock_block":
                world.setBlockState(pos, ModBlocks.sellafield_bedrock.getDefaultState());
                return false;
            case "minecraft:grass":
                placeBlockFromDist(dist, ModBlocks.waste_earth, pos);
                return false;
            case "minecraft:dirt":
                BlockDirt.DirtType dirtVariant = blockState.getValue(BlockDirt.VARIANT);
                switch (dirtVariant) {
                    case PODZOL -> placeBlockFromDist(dist, ModBlocks.waste_mycelium, pos);
                }
                return false;

            case "minecraft:mycelium":
                placeBlockFromDist(dist, ModBlocks.waste_mycelium, pos);
                return false;
            case "minecraft:sand":
                BlockSand.EnumType sandVariant = blockState.getValue(BlockSand.VARIANT);
                Block trinitite = sandVariant == BlockSand.EnumType.SAND ? ModBlocks.waste_trinitite : ModBlocks.waste_trinitite_red;
                if (rand.nextInt(60) == 0) {
                    placeBlockFromDist(dist, trinitite, pos);
                }
                return false;
            case "minecraft:clay":
                world.setBlockState(pos, Blocks.HARDENED_CLAY.getDefaultState());
                return false;
            case "minecraft:mossy_cobblestone":
                world.setBlockState(pos, Blocks.COAL_ORE.getDefaultState());
                return false;
            case "minecraft:coal_ore":
                double coalLevel = isSurface ? s5 : s6;
                if (dist < coalLevel) {
                    int ra = rand.nextInt(150);
                    if (ra < 7) {
                        world.setBlockState(pos, Blocks.DIAMOND_ORE.getDefaultState());
                    } else if (ra < 10) {
                        world.setBlockState(pos, Blocks.EMERALD_ORE.getDefaultState());
                    }
                }
                return isSurface;
            case "minecraft:brown_mushroom_block":
            case "minecraft:red_mushroom_block":
                if (dist < s0) {
                    BlockHugeMushroom.EnumType mushVariant = blockState.getValue(BlockHugeMushroom.VARIANT);
                    if (mushVariant == BlockHugeMushroom.EnumType.STEM) {
                        world.setBlockState(pos, ModBlocks.mush_block_stem.getDefaultState());
                    } else {
                        world.setBlockState(pos, ModBlocks.mush_block.getDefaultState());
                    }
                }
                return isSurface;
            case "minecraft:brown_mushroom":
            case "minecraft:red_mushroom":
                if (isSurface && dist < s0) {
                    world.setBlockState(pos, ModBlocks.mush.getDefaultState());
                }
                return true;
            case "minecraft:vine":
                world.setBlockToAir(pos);
                return true;
            case "hbm:brick_concrete":
                int chance = isSurface ? 80 : 60;
                if (rand.nextInt(chance) == 0) {
                    world.setBlockState(pos, ModBlocks.brick_concrete_broken.getDefaultState());
                }
                return false;
//            case "hbm:sellafield_4":
//                if (isSurface) {
//                    world.setBlockState(pos,
//                            ModBlocks.sellafield_core.getDefaultState().withProperty(BlockSellafieldSlaked.NATURAL, true).withProperty(BlockSellafieldSlaked.SHADE, rand.nextInt(4)));
//                    return true;
//                }
//                break;
//            case "hbm:sellafield_3":
//                if (isSurface) {
//                    world.setBlockState(pos,
//                            ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 4).getDefaultState().withProperty(BlockSellafieldSlaked.NATURAL, true).withProperty(BlockSellafieldSlaked.SHADE, rand.nextInt(4)));
//                    return true;
//                }
//                break;
//            case "hbm:sellafield_2":
//                if (isSurface) {
//                    world.setBlockState(pos,
//                            ModBlocks.sellafield.getDefaultState();
//                    return true;
//                }
//                break;
//            case "hbm:sellafield_1":
//                if (isSurface) {
//                    world.setBlockState(pos,
//                            ModBlocks.sellafield.getDefaultState();
//                    return true;
//                }
//                break;
//            case "hbm:sellafield_0":
//                if (isSurface) {
//                    world.setBlockState(pos,
//                            ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 1).getDefaultState().withProperty(BlockSellafieldSlaked.NATURAL, true).withProperty(BlockSellafieldSlaked.SHADE, rand.nextInt(4)));
//                    return true;
//                }
//                break;
//            case "hbm:sellafield_slaked":
//                if (isSurface) {
//                    world.setBlockState(pos,
//                            ModBlocks.sellafield.getDefaultState().withProperty(BlockMeta.META, 0).getDefaultState().withProperty(BlockSellafieldSlaked.NATURAL, true).withProperty(BlockSellafieldSlaked.SHADE, rand.nextInt(4)));
//                    return true;
//                }
//                break;
        }
        if (isSurface) {
            if (pos.getY() == contactHeight - 1 && block != ModBlocks.fallout && Math.abs(rand.nextGaussian() * (dist * dist) / (s0 * s0)) < 0.05 && rand.nextDouble() < 0.05 && ModBlocks.fallout.canPlaceBlockAt(world, pos.up())) {
                placeBlockFromDist(dist, ModBlocks.fallout, pos.up());
            }
            if (getSpawnFire() && dist < s2 && block.isFlammable(world, pos, EnumFacing.UP) && world.isAirBlock(pos.up())) {
                world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
            }
        }
        if (isSurface && reachedStone && !lastReachedStone && dist < s1 && block instanceof BlockOre) {
            world.setBlockState(pos, ModBlocks.toxic_block.getDefaultState());
            return true;
        }
        ItemStack stack = new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE);
        if (!stack.isEmpty()) {
            for (int id : OreDictionary.getOreIDs(stack)) {
                String oreName = OreDictionary.getOreName(id);
                if ("treeLeaves".equals(oreName)) {
                    boolean condition = dist > s1;
                    if (isSurface) {
                        condition = condition || (dist > getFallingRadius() && (world.rand.nextFloat() < (-5F * (getFallingRadius() / dist) + 5F)));
                    }
                    if (condition) {
                        world.setBlockState(pos, ModBlocks.waste_leaves.getDefaultState());
                    } else {
                        world.setBlockToAir(pos);
                    }
                    return true;
                }
                if ("logWood".equals(oreName)) {
                    if (dist < s0) {
                        world.setBlockState(pos, ((WasteLog) ModBlocks.waste_log).getSameRotationState(blockState));
                    }
                    return isSurface;
                }
                if ("oreUranium".equals(oreName) && !registryName.equals("hbm:ore_nether_uranium_scorched")) {
                    Block scorched = ModBlocks.ore_uranium_scorched;
                    Block schrab = ModBlocks.ore_schrabidium;
                    double level = s6;
                    int schrabChance = VersatileConfig.getSchrabOreChance();
                    if (registryName.equals("hbm:ore_nether_uranium")) {
                        scorched = ModBlocks.ore_nether_uranium_scorched;
                        schrab = ModBlocks.ore_nether_schrabidium;
                        level = s5;
                    } else if (registryName.equals("hbm:ore_gneiss_uranium")) {
                        scorched = ModBlocks.ore_gneiss_uranium_scorched;
                        schrab = ModBlocks.ore_gneiss_schrabidium;
                        level = s4;
                        schrabChance /= 2;
                    }
                    if (dist <= level) {
                        if (rand.nextInt(1 + schrabChance) == 0) {
                            world.setBlockState(pos, schrab.getDefaultState());
                        } else {
                            world.setBlockState(pos, scorched.getDefaultState());
                        }
                    }
                    return false;
                }
            }
        }
        Material mat = blockState.getMaterial();
        if (mat == Material.WOOD && block != ModBlocks.waste_log && block != ModBlocks.waste_planks) {
            if (dist < s0) {
                world.setBlockState(pos, ModBlocks.waste_planks.getDefaultState());
            }
            return isSurface;
        }
        if (mat == Material.ROCK || mat == Material.IRON) {
            return isSurface;
        }
        return !isSurface || !(block.getExplosionResistance(null) > 300);
    }

    protected void traverseRay(Vec3d start, Vec3d end, Predicate<BlockPos> visitor) {
        Vec3d d = end.subtract(start);
        if (d.lengthSquared() == 0) return;
        int x = (int) Math.floor(start.x);
        int y = (int) Math.floor(start.y);
        int z = (int) Math.floor(start.z);
        int stepX = (int) Math.signum(d.x);
        int stepY = (int) Math.signum(d.y);
        int stepZ = (int) Math.signum(d.z);
        double deltaX = d.x == 0 ? Double.MAX_VALUE : Math.abs(1.0 / d.x);
        double deltaY = d.y == 0 ? Double.MAX_VALUE : Math.abs(1.0 / d.y);
        double deltaZ = d.z == 0 ? Double.MAX_VALUE : Math.abs(1.0 / d.z);
        double tMaxX = d.x == 0 ? Double.MAX_VALUE : (stepX > 0 ? (x + 1 - start.x) / d.x : (x - start.x) / d.x);
        double tMaxY = d.y == 0 ? Double.MAX_VALUE : (stepY > 0 ? (y + 1 - start.y) / d.y : (y - start.y) / d.y);
        double tMaxZ = d.z == 0 ? Double.MAX_VALUE : (stepZ > 0 ? (z + 1 - start.z) / d.z : (z - start.z) / d.z);
        double t = 0.0;
        double tEnd = 1.0;
        while (true) {
            if (!visitor.test(new BlockPos(x, y, z))) break;
            if (tMaxX <= tMaxY && tMaxX <= tMaxZ) {
                t = tMaxX;
                x += stepX;
                tMaxX += deltaX;
            } else if (tMaxY <= tMaxZ) {
                t = tMaxY;
                y += stepY;
                tMaxY += deltaY;
            } else {
                t = tMaxZ;
                z += stepZ;
                tMaxZ += deltaZ;
            }
            if (t > tEnd) break;
        }
    }
}