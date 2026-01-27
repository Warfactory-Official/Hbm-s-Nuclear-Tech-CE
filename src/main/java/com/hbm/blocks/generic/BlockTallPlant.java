package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.PlantEnums;
import com.hbm.inventory.OreDictManager;
import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType.HEMP;
import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType.MUSTARD_WILLOW_0;
import static com.hbm.blocks.PlantEnums.EnumTallPlantType;
import static com.hbm.blocks.PlantEnums.EnumTallPlantType.*;

public class BlockTallPlant extends BlockPlantEnumMeta<EnumTallPlantType> implements IGrowable, IPlantable {


    public BlockTallPlant(String registryName) {
        super(registryName, EnumTallPlantType.VALUES);
        this.setTickRandomly(true);

    }

    public enum EnumTallFlower {
        WEED(false),
        CD2(true),
        CD3(true),
        CD4(true);

        public final boolean needsOil;
        EnumTallFlower(boolean needsOil) {
            this.needsOil = needsOil;
        }
    }

    public static void initPlacables() {
        PLANTABLE_BLOCKS.add(ModBlocks.dirt_dead);
        PLANTABLE_BLOCKS.add(ModBlocks.dirt_oily);
        PLANTABLE_BLOCKS.add(Blocks.GRASS);
        PLANTABLE_BLOCKS.add(Blocks.DIRT);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }


    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        EnumTallPlantType type = VALUES[state.getValue(META)];

        if (type.name().endsWith("_LOWER")) {
            EnumTallPlantType upper = valueOf(type.name().replace("_LOWER", "_UPPER"));
            IBlockState upperState = this.getDefaultState().withProperty(META, upper.ordinal());
            worldIn.setBlockState(pos.up(), upperState, 2);
        }
    }

    @Override
    public @NotNull IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer, EnumHand hand) {
        EnumTallPlantType type = VALUES[meta];
        if (!type.name().endsWith("_LOWER")) {
            type = valueOf(type.name().replace("_UPPER", "_LOWER"));
        }
        return this.getDefaultState().withProperty(META, type.ordinal());
    }

//    public int quantityDropped(int meta, int fortune, Random random) {
//        return 1;
//    }

    @Override
    public String enumToTranslationKey(EnumTallPlantType value) {
        return this.getTranslationKey() + "." + value.name().toLowerCase(Locale.US).substring(0, value.name().length() - 6);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote) return;
        EnumTallPlantType type = VALUES[state.getValue(META)];
        if (type.name().endsWith("_UPPER"))
            return;
        Block onTop = worldIn.getBlockState(pos.down()).getBlock();

        if (!type.needsOil) {
            if (onTop == ModBlocks.dirt_dead || onTop == ModBlocks.dirt_oily) {
                worldIn.setBlockState(pos, stateFromEnum(ModBlocks.plant_dead, PlantEnums.EnumDeadPlantType.BIG_FLOWER), 3);
                return;
            }
        }
        if (canGrow(worldIn, pos, state, false) && canUseBonemeal(worldIn, rand, pos, state) && rand.nextInt(3) == 0)
            grow(worldIn, rand, pos, state);

    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        EnumTallPlantType type = VALUES[state.getValue(META)];

        if (type.name().endsWith("_UPPER")) {
            BlockPos below = pos.down();
            IBlockState belowState = world.getBlockState(below);

            if (belowState.getBlock() != this || !belowState.getValue(META).equals(valueOf(type.name().replace("_UPPER", "_LOWER")).ordinal())) {
                world.setBlockToAir(pos); // Break orphaned upper half
            }
        }

        if (type.name().endsWith("_LOWER")) {
            BlockPos above = pos.up();
            BlockPos below = pos.down();
            IBlockState aboveState = world.getBlockState(above);
            IBlockState belowState = world.getBlockState(below);

            if (aboveState.getBlock() != this || !aboveState.getValue(META).equals(valueOf(type.name().replace("_LOWER", "_UPPER")).ordinal())) {
                world.setBlockState(pos, ModBlocks.plant_flower.getDefaultState().withProperty(META, type == HEMP_LOWER ? HEMP.ordinal() : MUSTARD_WILLOW_0.ordinal())); // Break orphaned lower half
            }
            checkAndDropBlock(world, pos, state);


        }
    }


    protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (EnumTallPlantType type : VALUES) {
            if (type.name().endsWith("_LOWER")) {
                list.add(new ItemStack(this, 1, type.ordinal()));
            }
        }
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        EnumTallPlantType type = VALUES[state.getValue(META)];
        switch (type) {
            case MUSTARD_WILLOW_2_LOWER:
                if (!isWatered(worldIn, pos)) return false;
                break;
            case MUSTARD_WILLOW_3_LOWER:
                if (!isWatered(worldIn, pos) || (type.needsOil && !isOiled(worldIn, pos))) return false;
                break;
            case MUSTARD_WILLOW_2_UPPER:
                if (!isWatered(worldIn, pos.down())) return false;
                break;
            case MUSTARD_WILLOW_3_UPPER:
                if (!isWatered(worldIn, pos.down()) || (type.needsOil && !isOiled(worldIn, pos.down()))) return false;
                break;
            default:
                return false;
        }
        return true;


    }


    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        EnumTallPlantType type = VALUES[state.getValue(META)];
        if (type == MUSTARD_WILLOW_3_LOWER)
            return true;

        return rand.nextFloat() < 0.33F;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        if (!canGrow(worldIn, pos, state, false)) return;
        var type = (PlantEnums.EnumTallPlantType) this.getEnumFromState(state);

        switch (type) {
            case MUSTARD_WILLOW_2_LOWER:
                worldIn.setBlockState(pos, ModBlocks.plant_tall.getDefaultState().withProperty(META, MUSTARD_WILLOW_3_LOWER.ordinal()), 2);

                worldIn.setBlockState(pos.up(), ModBlocks.plant_tall.getDefaultState().withProperty(META, MUSTARD_WILLOW_3_UPPER.ordinal()), 2);
                break;
            case MUSTARD_WILLOW_3_LOWER:
                worldIn.setBlockState(pos, ModBlocks.plant_tall.getDefaultState().withProperty(META, MUSTARD_WILLOW_4_LOWER.ordinal()), 2);

                worldIn.setBlockState(pos.up(), ModBlocks.plant_tall.getDefaultState().withProperty(META, MUSTARD_WILLOW_4_UPPER.ordinal()), 2);

                worldIn.setBlockState(pos.down(), Blocks.DIRT.getDefaultState(), 3);
                break;
        }
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess blockAccess, BlockPos pos, IBlockState state, int fortune) {
        World world = (World) blockAccess;
        List<ItemStack> drops = NonNullList.create();
        EnumTallPlantType type = (EnumTallPlantType) getEnumFromState(state);
        switch (type) {
            case HEMP_LOWER:
                drops.add(new ItemStack(EnumMetaBlockItem.getItemFromBlock(ModBlocks.plant_flower), 2, HEMP.ordinal()));
                break;
            case HEMP_UPPER:
                drops.add(new ItemStack(EnumMetaBlockItem.getItemFromBlock(ModBlocks.plant_flower), 1, HEMP.ordinal()));
                break;
            case MUSTARD_WILLOW_4_UPPER:
                drops.add(OreDictManager.DictFrame.fromOne(ModItems.plant_item, com.hbm.items.ItemEnums.EnumPlantType.MUSTARDWILLOW, 3 + world.rand.nextInt(4)));
                break;
            default:
                drops.add(new ItemStack(EnumMetaBlockItem.getItemFromBlock(ModBlocks.plant_flower), 1, MUSTARD_WILLOW_0.ordinal()));
                break;
        }
        return drops;
    }

}
