package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import java.util.Random;

import static com.hbm.blocks.ModBlocks.*;
import static com.hbm.blocks.PlantEnums.EnumDeadPlantType;
import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType;
import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType.*;
import static com.hbm.blocks.PlantEnums.EnumTallPlantType.*;

public class BlockFlowerPlant extends BlockPlantEnumMeta<EnumFlowerPlantType> implements IGrowable, IPlantable {

    public BlockFlowerPlant(String registryName) {
        super(registryName, EnumFlowerPlantType.VALUES);
        this.setTickRandomly(true);
    }


    public static void initPlacables() {
        PLANTABLE_BLOCKS.add(ModBlocks.dirt_dead);
        PLANTABLE_BLOCKS.add(ModBlocks.dirt_oily);
        PLANTABLE_BLOCKS.add(Blocks.GRASS);
        PLANTABLE_BLOCKS.add(Blocks.DIRT);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote) return;
        EnumFlowerPlantType type = EnumFlowerPlantType.values()[state.getValue(META)];

        if (!(type == HEMP || type == MUSTARD_WILLOW_0 || type == MUSTARD_WILLOW_1)) return;

        if (canGrow(worldIn, pos, state, false) && canUseBonemeal(worldIn, rand, pos, state) && rand.nextInt(3) == 0)
            grow(worldIn, rand, pos, state);
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        EnumFlowerPlantType type = EnumFlowerPlantType.values()[state.getValue(META)];
        if (type == MUSTARD_WILLOW_0 || type == MUSTARD_WILLOW_1) {
            if (!isWatered(worldIn, pos))
                return false;
        }
        if (type == HEMP || type == MUSTARD_WILLOW_1)
            return worldIn.isAirBlock(pos.up());

        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        var type = (EnumFlowerPlantType) this.getEnumFromState(state);
        return switch (type) {
            case HEMP, MUSTARD_WILLOW_0, MUSTARD_WILLOW_1 -> rand.nextFloat() < 0.33F;
            default -> true;
        };
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        var type = (EnumFlowerPlantType) this.getEnumFromState(state);
        switch (type) {
            case HEMP:
                Block ground = worldIn.getBlockState(pos.down()).getBlock();
                if (ground == dirt_dead || ground == dirt_oily) {
                    worldIn.setBlockState(pos, plant_dead.getDefaultState().withProperty(META, EnumDeadPlantType.GENERIC.ordinal()), 3);
                    break;
                }
                worldIn.setBlockState(pos, plant_tall.getDefaultState()
                        .withProperty(META, HEMP_LOWER.ordinal()), 2);

                worldIn.setBlockState(pos.up(), plant_tall.getDefaultState()
                        .withProperty(META, HEMP_UPPER.ordinal()), 2);
                break;
            case MUSTARD_WILLOW_0:
                if (isWatered(worldIn, pos))
                    worldIn.setBlockState(pos, plant_flower.getDefaultState().withProperty(META, MUSTARD_WILLOW_1.ordinal()), 3);
                break;
            case MUSTARD_WILLOW_1:
                if (isWatered(worldIn, pos)) {
                    worldIn.setBlockState(pos, plant_tall.getDefaultState().withProperty(META, MUSTARD_WILLOW_2_LOWER.ordinal()), 3);
                    worldIn.setBlockState(pos.up(), plant_tall.getDefaultState().withProperty(META, MUSTARD_WILLOW_2_UPPER.ordinal()), 3);
                }
                break;
            default:
                worldIn.spawnEntity(new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Item.getItemFromBlock(this), 1, type.ordinal())));
                break;
        }
    }
}
