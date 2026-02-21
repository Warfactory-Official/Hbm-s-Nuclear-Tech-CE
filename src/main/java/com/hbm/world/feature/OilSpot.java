package com.hbm.world.feature;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockPlantEnumMeta;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import static com.hbm.blocks.PlantEnums.EnumDeadPlantType;
import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType.MUSTARD_WILLOW_0;
import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType.MUSTARD_WILLOW_1;
import static com.hbm.blocks.PlantEnums.EnumTallPlantType.*;
import static com.hbm.blocks.generic.BlockMeta.META;

public class OilSpot {

    private static final MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public static void generateOilSpot(World world, int x, int z, int width, int count, boolean addWillows) {
        MutableBlockPos pos = mutablePos;

        for (int i = 0; i < count; i++) {
            int rX = x + (int) (world.rand.nextGaussian() * width);
            int rZ = z + (int) (world.rand.nextGaussian() * width);
            int rY = world.getHeight(rX, rZ);

            for (int y = rY; y > rY - 4 && y > 0; y--) {

                pos.setPos(rX, y - 1, rZ);
                IBlockState belowState = world.getBlockState(pos);
                Block below = belowState.getBlock();

                pos.setPos(rX, y, rZ);
                IBlockState groundState = world.getBlockState(pos);
                Block ground = groundState.getBlock();

                if (ground instanceof BlockPlantEnumMeta) {
                    int meta = groundState.getValue(META);
                    if (ground == ModBlocks.plant_flower && (meta == MUSTARD_WILLOW_0.ordinal() || meta == MUSTARD_WILLOW_1.ordinal())) {
                        continue;
                    }
                    if (ground == ModBlocks.plant_tall && (meta == MUSTARD_WILLOW_2_LOWER.ordinal() || meta == MUSTARD_WILLOW_3_LOWER.ordinal() || meta == MUSTARD_WILLOW_4_LOWER.ordinal())) {
                        continue;
                    }
                } else if (below.isNormalCube(belowState, world, pos.setPos(rX, y - 1, rZ))) {
                    pos.setPos(rX, y, rZ);

                    if (ground instanceof BlockTallGrass) {
                        if (world.rand.nextInt(10) == 0) {
                            pos.setPos(rX, y + 1, rZ);
                            IBlockState topState = world.getBlockState(pos);
                            Block topBlock = topState.getBlock();
                            pos.setPos(rX, y, rZ);

                            if (topBlock instanceof BlockTallGrass && topState.getValue(BlockTallGrass.TYPE) == BlockTallGrass.EnumType.FERN) {
                                world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.FERN.ordinal()), 2 | 16);
                            } else {
                                world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.GRASS.ordinal()), 2 | 16);
                            }
                        } else {
                            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2 | 16);
                        }
                    } else if (ground instanceof BlockFlower) {
                        world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.FLOWER.ordinal()), 2 | 16);
                    } else if (ground instanceof BlockDoublePlant) {
                        world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.BIG_FLOWER.ordinal()), 2 | 16);
                    } else if (ground instanceof IPlantable) {
                        world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.GENERIC.ordinal()), 2 | 16);
                    }
                }

                if (below == Blocks.GRASS || below == Blocks.DIRT) {
                    pos.setPos(rX, y - 1, rZ);
                    IBlockState dirtState = world.rand.nextInt(10) == 0 ? ModBlocks.dirt_oily.getDefaultState() : ModBlocks.dirt_dead.getDefaultState();
                    world.setBlockState(pos, dirtState, 2 | 16);

                    if (addWillows && world.rand.nextInt(50) == 0) {
                        pos.setPos(rX, y, rZ);
                        if (ModBlocks.plant_flower.canPlaceBlockAt(world, pos)) {
                            world.setBlockState(pos, ModBlocks.plant_flower.getDefaultState()
                                                                           .withProperty(META, MUSTARD_WILLOW_0.ordinal()), 2 | 16);
                        }
                    }

                    break;

                } else if (below == Blocks.SAND || below == ModBlocks.ore_oil_sand) {
                    pos.setPos(rX, y - 1, rZ);
                    IBlockState sandState;

                    if (below == Blocks.SAND && belowState.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND) {
                        sandState = ModBlocks.sand_dirty_red.getDefaultState();
                    } else {
                        sandState = ModBlocks.sand_dirty.getDefaultState();
                    }

                    world.setBlockState(pos, sandState, 2 | 16);
                    break;

                } else if (below == Blocks.STONE) {
                    pos.setPos(rX, y - 1, rZ);
                    world.setBlockState(pos, ModBlocks.stone_cracked.getDefaultState(), 2 | 16);
                    break;

                } else if (belowState.getMaterial() == Material.LEAVES) {
                    pos.setPos(rX, y - 1, rZ);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2 | 16);
                    break;
                }
            }
        }
    }
}
