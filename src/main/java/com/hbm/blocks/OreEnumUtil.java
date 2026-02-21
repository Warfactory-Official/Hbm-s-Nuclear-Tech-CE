package com.hbm.blocks;

import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsSingle;
import com.hbm.items.ItemEnums;
import com.hbm.lib.TriFunction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.hbm.items.ModItems.*;


/**
 * Simple function driven enum that allows for easy and flexible ore block drops.
 * Simply pass Enum to your class and use both quantity and drop function to retrieve relevant data
 * @author MrNorwood
 */
public class OreEnumUtil {

    public static int base2Rand3Fortune(IBlockState state, int fortune, Random rand) { return 2 + rand.nextInt(3) + fortune; }
    public static int base2Rand2Fortune(IBlockState state, int fortune, Random rand) { return 2 + rand.nextInt(2) + fortune; }
    public static int base1Rand2Fortune(IBlockState state, int fortune, Random rand) { return 1 + rand.nextInt(2) + fortune; }
    public static int base1Rand3(IBlockState state, int fortune, Random rand) { return 1 + rand.nextInt(3); }
    public static int const1(IBlockState state, int fortune, Random rand) { return 1; }
    public static int vanillaFortune(IBlockState state, int fortune, Random rand) { return 1 + applyFortune(rand, fortune); }
    public static int cobaltAmount(IBlockState state, int fortune, Random rand) { return 4 + rand.nextInt(6); }
    public static int cobaltNetherAmount(IBlockState state, int fortune, Random rand) { return 5 + rand.nextInt(8); }
    public static int applyFortune(Random rand, int fortune) { return fortune <= 0 ? 0 : rand.nextInt(fortune); }

    // --- Drop Functions ---

    public static ItemStack getMeteorTreasure(IBlockState state, Random rand) {
        WeightedRandomChestContentFrom1710[] pool = ItemPool.getPool(ItemPoolsSingle.POOL_METEORITE_TREASURE);
        return ItemPool.getStack(pool, rand);
    }


    public static ItemStack phosphorusNetherDrop(IBlockState state, Random rand) {
        return rand.nextInt(10) == 0 ? new ItemStack(ingot_phosphorus) : new ItemStack(powder_fire);
    }

    public static ItemStack blockMeteorDrop(IBlockState state, Random rand) {
        return rand.nextInt(10) == 0 ? new ItemStack(plate_dalekanium) : new ItemStack(Item.getItemFromBlock(ModBlocks.block_meteor));
    }

    // --- OreEnum ---

    public enum OreEnum implements IOreType {

        COAL(() -> new ItemStack(Items.COAL), OreEnumUtil::vanillaFortune),
        DIAMOND(() -> new ItemStack(Items.DIAMOND), OreEnumUtil::vanillaFortune),
        EMERALD(() -> new ItemStack(Items.EMERALD), OreEnumUtil::vanillaFortune),

        ASBESTOS(() -> new ItemStack(ingot_asbestos), OreEnumUtil::vanillaFortune),
        SULFUR(() -> new ItemStack(sulfur), OreEnumUtil::base2Rand3Fortune),
        NITER(() -> new ItemStack(niter), OreEnumUtil::base1Rand2Fortune),
        FLUORITE(() -> new ItemStack(fluorite), OreEnumUtil::base2Rand3Fortune),
        METEORITE_FRAG(() -> new ItemStack(fragment_meteorite), OreEnumUtil::base1Rand3),
        METEORITE_TREASURE(OreEnumUtil::getMeteorTreasure, OreEnumUtil::base1Rand3),
        COBALT(() -> new ItemStack(fragment_cobalt), OreEnumUtil::cobaltAmount),
        COBALT_NETHER(() -> new ItemStack(fragment_cobalt), OreEnumUtil::cobaltNetherAmount),
        PHOSPHORUS_NETHER(OreEnumUtil::phosphorusNetherDrop, OreEnumUtil::vanillaFortune),
        LIGNITE(() -> new ItemStack(lignite), OreEnumUtil::vanillaFortune),
        RARE_EARTHS(() -> new ItemStack(chunk_ore, 1, ItemEnums.EnumChunkType.RARE.ordinal()), OreEnumUtil::vanillaFortune),
        BLOCK_METEOR(OreEnumUtil::blockMeteorDrop, OreEnumUtil::vanillaFortune),
        CINNABAR(() -> new ItemStack(cinnabar), OreEnumUtil::base1Rand2Fortune),
        ALEXANDRITE(() -> new ItemStack(gem_alexandrite), OreEnumUtil::base1Rand2Fortune),
        COLTAN(() -> new ItemStack(fragment_coltan), OreEnumUtil::vanillaFortune),
        RAD_GEM(() -> new ItemStack(gem_rad), OreEnumUtil::vanillaFortune),
        WASTE_TRINITE(() -> new ItemStack(trinitite), OreEnumUtil::vanillaFortune),
        ZIRCON(() -> new ItemStack(nugget_zirconium), OreEnumUtil::base2Rand2Fortune),
        NEODYMIUM(() -> new ItemStack(fragment_neodymium), OreEnumUtil::base2Rand2Fortune),
        NITAN(() -> new ItemStack(powder_nitan_mix), OreEnumUtil::const1),

        CLUSTER_IRON(() -> new ItemStack(crystal_iron), OreEnumUtil::vanillaFortune),
        CLUSTER_TITANIUM(() -> new ItemStack(crystal_titanium), OreEnumUtil::vanillaFortune),
        CLUSTER_ALUMINIUM(() -> new ItemStack(crystal_aluminium), OreEnumUtil::vanillaFortune),
        CLUSTER_COPPER(() -> new ItemStack(crystal_copper), OreEnumUtil::vanillaFortune),
        CLUSTER_TUNGSTEN(() -> new ItemStack(crystal_tungsten), OreEnumUtil::vanillaFortune),
        ;

        public final BiFunction<IBlockState, Random, ItemStack> dropFunction;
        public final TriFunction<IBlockState, Integer, Random, Integer> quantityFunction;

        OreEnum(BiFunction<IBlockState, Random, ItemStack> dropFunction, TriFunction<IBlockState, Integer, Random, Integer> quantityFunction) {
            this.dropFunction = dropFunction;
            this.quantityFunction = quantityFunction;
        }

        OreEnum(Supplier<ItemStack> drop, TriFunction<IBlockState, Integer, Random, Integer> quantity) {
            this((state, rand) -> new ItemStack(drop.get().getItem(), 1, drop.get().getMetadata()), quantity);
        }

        @Override
        public BiFunction<IBlockState, Random, ItemStack> getDropFunction() { return this.dropFunction; }

        @Override
        public TriFunction<IBlockState, Integer, Random, Integer> getQuantityFunction() { return this.quantityFunction; }
    }


}
