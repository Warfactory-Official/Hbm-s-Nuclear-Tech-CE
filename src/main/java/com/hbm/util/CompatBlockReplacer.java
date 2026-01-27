package com.hbm.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * stolen from Leafia's Cursed Addon by Leafia herself
 *
 * @author Leafia
 */
public class CompatBlockReplacer {
    /**
     * The blocks to be replaced.
     * <p>Meta will be automatically copied. (you cannot set it manually)
     * <p>Keep in mind that if SpecialReplacer for the same block exists, that is prioritized instead.
     */
    public static final Map<String, Block> replacementMap = new HashMap<>();
    /**
     * Replacement function for more customizable replacement.
     * <p>Used in leafia's cursed addon, for making meta < 2 waste dirts turn into normal dirt but meta >= 2 turn into addon waste variant.
     */
    public static final Map<String, BiFunction<String, IBlockState, IBlockState>> specialReplacer = new HashMap<>();

    static {
        replacementMap.put("hbm:waste_ice", Blocks.ICE);
        replacementMap.put("hbm:waste_snow", Blocks.SNOW_LAYER);
        replacementMap.put("hbm:waste_snow_block", Blocks.SNOW);
        replacementMap.put("hbm:waste_dirt", Blocks.DIRT);
        replacementMap.put("hbm:waste_gravel", Blocks.GRAVEL);
        replacementMap.put("hbm:waste_sand", Blocks.SAND);
        replacementMap.put("hbm:waste_sandstone", Blocks.SANDSTONE);
        replacementMap.put("hbm:waste_sand_red", Blocks.SAND);
        replacementMap.put("hbm:waste_red_sandstone", Blocks.RED_SANDSTONE);
        replacementMap.put("hbm:waste_terracotta", Blocks.HARDENED_CLAY);
    }

    public static @NotNull IBlockState replaceBlock(IBlockState missingBlock) {
        if (missingBlock == null) return Blocks.AIR.getDefaultState();
        try {
            ResourceLocation reg = missingBlock.getBlock().getRegistryName();
            if (reg == null) return missingBlock;
            String name = reg.toString();
            if (specialReplacer.containsKey(name)) {
                BiFunction<String, IBlockState, IBlockState> processor = specialReplacer.get(name);
                return processor.apply(name, missingBlock);
            } else {
                Block newBlock = replacementMap.get(name);
                if (newBlock == null) return missingBlock;
                return newBlock.getStateFromMeta(missingBlock.getBlock().getMetaFromState(missingBlock));
            }
        } catch (RuntimeException ex) {
            return missingBlock;
        }
    }
}
