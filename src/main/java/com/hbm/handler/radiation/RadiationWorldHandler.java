package com.hbm.handler.radiation;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

class RadiationWorldHandler {
    private static final IBlockState AIR_DEFAULT_STATE = Blocks.AIR.getDefaultState();

    static void handleWorldDestruction(WorldServer world) {
        if (!RadiationConfig.worldRadEffects || !GeneralConfig.enableRads) return;
        RadiationSystemNT.handleWorldDestruction(world);
    }

    static void decayBlock(World world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        if (block.getRegistryName() == null) return;
        if (block instanceof BlockDoublePlant) {
            BlockDoublePlant.EnumBlockHalf half;
            try {
                half = state.getValue(BlockDoublePlant.HALF);
            } catch (Exception _) { return; }
            BlockPos lowerPos = (half == BlockDoublePlant.EnumBlockHalf.LOWER) ? pos : pos.down();
            BlockPos upperPos = (half == BlockDoublePlant.EnumBlockHalf.LOWER) ? pos.up() : pos;
            world.setBlockState(upperPos, AIR_DEFAULT_STATE, 2);
            world.setBlockState(lowerPos, AIR_DEFAULT_STATE, 2);
            return;
        }
        if (block == Blocks.GRASS) {
            world.setBlockState(pos, ModBlocks.waste_earth.getDefaultState(), 2);
            return;
        }
        if (block == Blocks.TALLGRASS) {
            world.setBlockState(pos, AIR_DEFAULT_STATE, 2);
            return;
        }
        if (state.getMaterial() == Material.LEAVES && block != ModBlocks.waste_leaves) {
            if (world.rand.nextInt(7) <= 5) {
                world.setBlockState(pos, ModBlocks.waste_leaves.getDefaultState(), 2);
            } else {
                world.setBlockState(pos, AIR_DEFAULT_STATE, 2);
            }
        }
    }

    @Deprecated
    static void decayBlock(World world, BlockPos pos, IBlockState state, boolean isLegacy) {
        Block block = state.getBlock();
        if (block.getRegistryName() == null) return;

        if (block instanceof BlockDoublePlant) {
            BlockPos lowerPos;
            BlockPos upperPos;
            if (state.getValue(BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.LOWER) {
                lowerPos = pos;
                upperPos = pos.up();
            } else {
                lowerPos = pos.down();
                upperPos = pos;
            }
            world.setBlockState(upperPos, Blocks.AIR.getDefaultState(), 2);
            world.setBlockState(lowerPos, ModBlocks.waste_grass_tall.getDefaultState(), 2);
            return;
        }

        ResourceLocation registryName = block.getRegistryName();
        String namespace = registryName.getNamespace();
        String path = registryName.getPath();

        if ("hbm".equals(namespace) && "waste_leaves".equals(path)) {
            if (world.rand.nextInt(8) == 0) {
                world.setBlockToAir(pos);
            }
            return;
        }
        if (!"minecraft".equals(namespace)) return;
        IBlockState newState = switch (path) {
            case "grass" -> ModBlocks.waste_earth.getDefaultState();

            case "mycelium" -> ModBlocks.waste_mycelium.getDefaultState();
            case "sand" -> {
                BlockSand.EnumType meta = state.getValue(BlockSand.VARIANT);
                if (isLegacy && world.rand.nextInt(60) == 0) {
                    yield meta == BlockSand.EnumType.SAND ? ModBlocks.waste_trinitite.getDefaultState() :
                            ModBlocks.waste_trinitite_red.getDefaultState();
                } else {
                    yield meta == BlockSand.EnumType.SAND ? ModBlocks.waste_trinitite.getDefaultState() : ModBlocks.waste_trinitite_red.getDefaultState();
                }
            }
            default -> {
                if (block instanceof BlockLeaves) {
                    yield ModBlocks.waste_leaves.getDefaultState();
                } else if (block instanceof BlockBush) {
                    yield ModBlocks.waste_grass_tall.getDefaultState();
                }
                yield null;
            }
        };

        if (newState != null) world.setBlockState(pos, newState, 2);
    }
}