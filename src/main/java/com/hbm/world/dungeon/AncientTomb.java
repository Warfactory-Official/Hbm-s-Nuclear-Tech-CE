package com.hbm.world.dungeon;

import com.hbm.blocks.ModBlocks;
import com.hbm.util.Vec3NT;
import com.hbm.world.generator.DungeonToolbox;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AncientTomb {
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public void buildChamber(AbstractPhasedStructure.LegacyBuilder world, Random rand, int x, int y, int z) {
        List<IBlockState> concrete = Arrays.asList(ModBlocks.brick_concrete.getDefaultState(),
                ModBlocks.brick_concrete_broken.getDefaultState(),
                ModBlocks.brick_concrete_cracked.getDefaultState());

        int size = 5;
        int cladding = size - 1;
        int core = size - 2;

        int dimOuter = size * 2 + 1;
        int dimInner = cladding * 2 + 1;
        int dimCore = core * 2 + 1;
        DungeonToolbox.generateBox(world, x - size, y - size, z - size, dimOuter, dimOuter, dimOuter, concrete);
        DungeonToolbox.generateBox(world, x - cladding, y - cladding, z - cladding, dimInner, dimInner, dimInner, ModBlocks.brick_obsidian.getDefaultState());
        DungeonToolbox.generateBox(world, x - core, y - core, z - core, dimCore, dimCore, dimCore, ModBlocks.ancient_scrap.getDefaultState());
        DungeonToolbox.generateBox(world, x + 6, y - 2, z - 1, 2, 1, 3, concrete);
        DungeonToolbox.generateBox(world, x + 4, y - 1, z - 1, 5, 3, 3, ModBlocks.gas_radon_tomb.getDefaultState());
        DungeonToolbox.generateBox(world, x + 6, y + 2, z - 1, 2, 1, 3, concrete);
    }

    public void buildSurfaceFeatures(World world, Random rand, int x, int z) {
        List<IBlockState> concrete = Arrays.asList(ModBlocks.brick_concrete.getDefaultState(),
                ModBlocks.brick_concrete_broken.getDefaultState(),
                ModBlocks.brick_concrete_cracked.getDefaultState());

        int chamberY = 20;
        int surfaceY = Math.max(world.getHeight(x, z), 35) - 5;
        int pySize = 15;

        /// PRINT PYRAMID ///
        for (int iy = pySize; iy > 0; iy--) {

            int range = (pySize - iy);

            for (int ix = -range; ix <= range; ix++) {
                for (int iz = -range; iz <= range; iz++) {
                    mutablePos.setPos(x + ix, surfaceY + iy, z + iz);
                    if ((ix <= -range + 1 || ix >= range - 1) && (iz <= -range + 1 || iz >= range - 1)) {
                        world.setBlockState(mutablePos, ModBlocks.reinforced_stone.getDefaultState(), 2 | 16);
                        continue;
                    }

                    if (iy == 1) {
                        world.setBlockState(mutablePos, ModBlocks.concrete_smooth.getDefaultState(), 2 | 16);
                        continue;
                    }

                    if ((ix <= -range + 1 || ix >= range - 1) || (iz <= -range + 1 || iz >= range - 1)) {
                        world.setBlockState(mutablePos, ModBlocks.concrete_smooth.getDefaultState(), 2 | 16);
                        continue;
                    }
                    world.setBlockToAir(mutablePos);
                }
            }
        }

        DungeonToolbox.generateBox(world, x - 2, surfaceY + 2, z - 2, 5, 4, 5, concrete);
        world.setBlockState(mutablePos.setPos(x + 2, surfaceY + 3, z), ModBlocks.brick_concrete_marked.getDefaultState(), 2 | 16);
        world.setBlockState(mutablePos.setPos(x - 2, surfaceY + 3, z), ModBlocks.brick_concrete_marked.getDefaultState(), 2 | 16);
        world.setBlockState(mutablePos.setPos(x, surfaceY + 3, z + 2), ModBlocks.brick_concrete_marked.getDefaultState(), 2 | 16);
        world.setBlockState(mutablePos.setPos(x, surfaceY + 3, z - 2), ModBlocks.brick_concrete_marked.getDefaultState(), 2 | 16);

        DungeonToolbox.generateBox(world, x + 5, surfaceY + 2, z + 5, 1, 7, 1, ModBlocks.concrete_pillar.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y));
        DungeonToolbox.generateBox(world, x + 5, surfaceY + 2, z - 5, 1, 7, 1, ModBlocks.concrete_pillar.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y));
        DungeonToolbox.generateBox(world, x - 5, surfaceY + 2, z - 5, 1, 7, 1, ModBlocks.concrete_pillar.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y));
        DungeonToolbox.generateBox(world, x - 5, surfaceY + 2, z + 5, 1, 7, 1, ModBlocks.concrete_pillar.getDefaultState().withProperty(BlockRotatedPillar.AXIS, EnumFacing.Axis.Y));

        /// GENERATE TUNNEL ///
        Vec3NT sVec = new Vec3NT(10, 0, 0);
        float sRot = (float) Math.toRadians(360F / 32F);
        for (int i = chamberY - 1; i < surfaceY + 2; i++) {
            int ix = (int) Math.floor(sVec.x);
            int iz = (int) Math.floor(sVec.z);
            int h = i < surfaceY ? 3 : 2;
            if (i > 40)
                DungeonToolbox.generateBox(world, x + ix - 1, i, z + iz - 1, 3, h, 3, Blocks.AIR.getDefaultState());
            else
                DungeonToolbox.generateBox(world, x + ix - 1, i, z + iz - 1, 3, h, 3, ModBlocks.gas_radon_tomb.getDefaultState());

            for (int dx = x + ix - 2; dx < x + ix + 3; dx++) {
                for (int dy = i - 1; dy < i + 4; dy++) {
                    for (int dz = z + iz - 2; dz < z + iz + 3; dz++) {



                        Block b = world.getBlockState(mutablePos.setPos(dx, dy, dz)).getBlock();

                        if (b != Blocks.AIR && b != ModBlocks.gas_radon_tomb && b != ModBlocks.concrete && b != ModBlocks.concrete_smooth && b != ModBlocks.brick_concrete && b != ModBlocks.brick_concrete_cracked && b != ModBlocks.brick_concrete_broken) {
                            world.setBlockState(mutablePos.setPos(dx, dy, dz), DungeonToolbox.getRandom(concrete, rand), 2 | 16);
                        }
                    }
                }
            }

            sVec.rotateYaw(sRot);
        }

        for (int dx = x + 4; dx < x + 8; dx++) {
            for (int dy = chamberY - 1; dy < chamberY + 4; dy++) {
                for (int dz = z - 2; dz < z + 3; dz++) {

                    Block b = world.getBlockState(mutablePos.setPos(dx, dy, dz)).getBlock();
                    if (b != Blocks.AIR && b != ModBlocks.gas_radon_tomb && b != ModBlocks.concrete && b != ModBlocks.concrete_smooth && b != ModBlocks.brick_concrete && b != ModBlocks.brick_concrete_cracked && b != ModBlocks.brick_concrete_broken) {
                        world.setBlockState(mutablePos.setPos(dx, dy, dz), DungeonToolbox.getRandom(concrete, rand), 2 | 16);
                    }
                }
            }
        }

        int spikeCount = 36 + rand.nextInt(15);
        Vec3NT vec = new Vec3NT(20, 0, 0);
        float rot = (float) Math.toRadians(360F / spikeCount);
        for (int i = 0; i < spikeCount; i++) {
            vec.rotateYaw(rot);
            double variance = 1D + rand.nextDouble() * 0.4D;
            int ix = (int) (x + vec.x * variance);
            int iz = (int) (z + vec.z * variance);
            int iy = world.getHeight(ix, iz) - 3;
            for (int j = iy; j < iy + 7; j++) {
                world.setBlockState(mutablePos.setPos(ix, j, iz), ModBlocks.deco_steel.getDefaultState(), 2 | 16);
            }
        }
    }
}
