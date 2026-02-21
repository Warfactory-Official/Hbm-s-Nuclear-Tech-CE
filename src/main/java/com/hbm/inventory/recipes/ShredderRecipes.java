package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ShredderRecipes extends SerializableRecipe {

    public static HashMap<ComparableStack, ItemStack> shredderRecipes = new HashMap<>();
    public static HashMap<Object, Object> jeiShredderRecipes;

    public static ItemStack getDustByName(String name) {

        return getOredictByName("dust" + name);
    }

    public static ItemStack getTinyDustByName(String name) {

        return getOredictByName("dustTiny" + name);
    }

    public static ItemStack getOredictByName(String name) {

        List<ItemStack> matches = OreDictionary.getOres(name);
        if (matches != null && !matches.isEmpty())
            return matches.getFirst().copy();

        return new ItemStack(ModItems.scrap);
    }

    public static void setRecipe(Item in, ItemStack out) {
        setRecipe(new ComparableStack(in), out);
    }

    public static void setRecipe(Block in, ItemStack out) {
        setRecipe(new ComparableStack(in), out);
    }

    public static void setRecipe(ItemStack in, ItemStack out) {
        setRecipe(new ComparableStack(in), out);
    }

    public static void setRecipe(ComparableStack in, ItemStack out) {
        if (!shredderRecipes.containsKey(in)) {
            shredderRecipes.put(in, out);
        }
    }

    public static void setRecipe(String in, String out) {
        if (OreDictionary.doesOreNameExist(in) && OreDictionary.doesOreNameExist(out))
            setRecipe(getOredictByName(in), getOredictByName(out));
    }

    public static void removeRecipe(ItemStack in) {

        shredderRecipes.remove(new ComparableStack(in));
    }

    // idk this shit doesn't work for now
    public static Map<Object, Object> getShredderRecipes() {
        if (jeiShredderRecipes == null || jeiShredderRecipes.size() != shredderRecipes.size()) {
            HashMap<Object, Object> map = new HashMap<>();

            for (Map.Entry<ComparableStack, ItemStack> entry : shredderRecipes.entrySet()) {
                ComparableStack comp = entry.getKey();
                if (comp == null) continue;

                ItemStack in = comp.toStack();
                if (in.isEmpty()) continue;

                ItemStack out = entry.getValue();
                if (out == null || out.isEmpty()) continue;

                map.put(comp.makeSingular(), out.copy());
            }

            jeiShredderRecipes = map;
        }

        return jeiShredderRecipes;
    }

    public static ItemStack getShredderResult(ItemStack stack) {
        if(stack.isEmpty())
            return new ItemStack(ModItems.scrap);

        ComparableStack comp = new ComparableStack(stack).makeSingular();
        ItemStack sta = shredderRecipes.get(comp);

        if(sta == null) {
            comp.meta = OreDictionary.WILDCARD_VALUE;
            sta = shredderRecipes.get(comp);
        }

        return sta == null ? new ItemStack(ModItems.scrap) : sta;

    }

    @Override
    public void registerPost() {

        String[] names = OreDictionary.getOreNames();

        for (String name : names) {

            //if the dict contains invalid names, skip
            if (name == null || name.isEmpty())
                continue;

            List<ItemStack> matches = OreDictionary.getOres(name);

            //if the name isn't assigned to an ore, also skip
            if (matches == null || matches.isEmpty())
                continue;

            if (name.length() > 5 && name.startsWith("ingot")) {
                ItemStack dust = getDustByName(name.substring(5));

                if (dust.getItem() != ModItems.scrap) {

                    for (ItemStack stack : matches) {
                        setRecipe(new ComparableStack(stack), dust);
                    }
                }
            } else if (name.length() > 5 && name.startsWith("plate")) {
                ItemStack dust = getDustByName(name.substring(5));

                if (dust.getItem() != ModItems.scrap) {

                    for (ItemStack stack : matches) {
                        setRecipe(new ComparableStack(stack), dust);
                    }
                }
            } else if (name.length() > 6 && name.startsWith("nugget")) {
                ItemStack dust = getTinyDustByName(name.substring(6));

                if (dust.getItem() != ModItems.scrap) {

                    for (ItemStack stack : matches) {
                        setRecipe(new ComparableStack(stack), dust);
                    }
                }
            } else if (name.length() > 3 && name.startsWith("ore")) {
                ItemStack dust = getDustByName(name.substring(3));

                if (dust.getItem() != ModItems.scrap) {

                    dust.setCount(2);

                    for (ItemStack stack : matches) {
                        setRecipe(new ComparableStack(stack), dust);
                    }
                }
            } else if (name.length() > 5 && name.startsWith("block")) {
                ItemStack dust = getDustByName(name.substring(5));

                if (dust.getItem() != ModItems.scrap) {

                    dust.setCount(9);

                    for (ItemStack stack : matches) {
                        setRecipe(new ComparableStack(stack), dust);
                    }
                }
            } else if (name.length() > 3 && name.startsWith("gem")) {
                ItemStack dust = getDustByName(name.substring(3));

                if (dust.getItem() != ModItems.scrap) {

                    for (ItemStack stack : matches) {
                        setRecipe(new ComparableStack(stack), dust);
                    }
                }
            } else if (name.startsWith("dust")) {

                for (ItemStack stack : matches) {
                    if (stack != null && !stack.isEmpty() && Item.REGISTRY.getNameForObject(stack.getItem()) != null)
                        setRecipe(new ComparableStack(stack), new ItemStack(ModItems.dust));
                }
            }
        }
    }

    @Override
    public void registerDefaults() {

        /* Primary recipes */
        ShredderRecipes.setRecipe(ModItems.scrap, new ItemStack(ModItems.dust));
        ShredderRecipes.setRecipe(ModItems.dust, new ItemStack(ModItems.dust));
        ShredderRecipes.setRecipe(ModItems.dust_tiny, new ItemStack(ModItems.dust_tiny));
        ShredderRecipes.setRecipe(Blocks.GLOWSTONE, new ItemStack(Items.GLOWSTONE_DUST, 4));
        ShredderRecipes.setRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 0), new ItemStack(ModItems.powder_quartz, 4));
        ShredderRecipes.setRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 1), new ItemStack(ModItems.powder_quartz, 4));
        ShredderRecipes.setRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 2), new ItemStack(ModItems.powder_quartz, 4));
        ShredderRecipes.setRecipe(Blocks.QUARTZ_STAIRS, new ItemStack(ModItems.powder_quartz, 3));
        ShredderRecipes.setRecipe(new ItemStack(Blocks.STONE_SLAB, 1, 7), new ItemStack(ModItems.powder_quartz, 2));
        ShredderRecipes.setRecipe(Items.QUARTZ, new ItemStack(ModItems.powder_quartz));
        ShredderRecipes.setRecipe(Blocks.QUARTZ_ORE, new ItemStack(ModItems.powder_quartz, 2));
        ShredderRecipes.setRecipe(ModBlocks.ore_nether_fire, new ItemStack(ModItems.powder_fire, 6));
        ShredderRecipes.setRecipe(Blocks.PACKED_ICE, new ItemStack(ModItems.powder_ice, 1));
        ShredderRecipes.setRecipe(ModBlocks.brick_light, new ItemStack(Items.CLAY_BALL, 4));
        ShredderRecipes.setRecipe(ModBlocks.concrete, new ItemStack(Blocks.GRAVEL, 1));
        ShredderRecipes.setRecipe(ModBlocks.concrete_smooth, new ItemStack(Blocks.GRAVEL, 1));
        ShredderRecipes.setRecipe(ModBlocks.brick_concrete, new ItemStack(Blocks.GRAVEL, 1));
        ShredderRecipes.setRecipe(ModBlocks.brick_concrete_mossy, new ItemStack(Blocks.GRAVEL, 1));
        ShredderRecipes.setRecipe(ModBlocks.brick_concrete_cracked, new ItemStack(Blocks.GRAVEL, 1));
        ShredderRecipes.setRecipe(ModBlocks.brick_concrete_broken, new ItemStack(Blocks.GRAVEL, 1));
        ShredderRecipes.setRecipe(ModBlocks.brick_obsidian, new ItemStack(ModBlocks.gravel_obsidian, 1));
        ShredderRecipes.setRecipe(Blocks.OBSIDIAN, new ItemStack(ModBlocks.gravel_obsidian, 1));
        ShredderRecipes.setRecipe(Blocks.STONE, new ItemStack(Blocks.GRAVEL, 1));
        ShredderRecipes.setRecipe(ModBlocks.ore_oil_empty, new ItemStack(Blocks.GRAVEL, 1));
        ShredderRecipes.setRecipe(Blocks.COBBLESTONE, new ItemStack(Blocks.GRAVEL, 1));
        ShredderRecipes.setRecipe(Blocks.STONEBRICK, new ItemStack(Blocks.GRAVEL, 1));
        ShredderRecipes.setRecipe(Blocks.GRAVEL, new ItemStack(Blocks.SAND, 1));
        ShredderRecipes.setRecipe(Blocks.BRICK_BLOCK, new ItemStack(Items.CLAY_BALL, 4));
        ShredderRecipes.setRecipe(Blocks.BRICK_STAIRS, new ItemStack(Items.CLAY_BALL, 3));
        ShredderRecipes.setRecipe(Items.FLOWER_POT, new ItemStack(Items.CLAY_BALL, 3));
        ShredderRecipes.setRecipe(Items.BRICK, new ItemStack(Items.CLAY_BALL, 1));
        ShredderRecipes.setRecipe(Blocks.SANDSTONE, new ItemStack(Blocks.SAND, 4));
        ShredderRecipes.setRecipe(Blocks.SANDSTONE_STAIRS, new ItemStack(Blocks.SAND, 6));
        ShredderRecipes.setRecipe(Blocks.CLAY, new ItemStack(Items.CLAY_BALL, 4));
        ShredderRecipes.setRecipe(Blocks.HARDENED_CLAY, new ItemStack(Items.CLAY_BALL, 4));
        ShredderRecipes.setRecipe(Blocks.TNT, new ItemStack(Items.GUNPOWDER, 5));
        ShredderRecipes.setRecipe(OreDictManager.DictFrame.fromOne(ModBlocks.stone_resource, BlockEnums.EnumStoneType.LIMESTONE), new ItemStack(ModItems.powder_limestone, 4));
        ShredderRecipes.setRecipe(ModBlocks.stone_gneiss, new ItemStack(ModItems.powder_lithium_tiny, 1));
        ShredderRecipes.setRecipe(ModItems.powder_lapis, new ItemStack(ModItems.powder_cobalt_tiny, 1));
        ShredderRecipes.setRecipe(ModItems.fragment_neodymium, new ItemStack(ModItems.powder_neodymium_tiny, 1));
        ShredderRecipes.setRecipe(ModItems.fragment_cobalt, new ItemStack(ModItems.powder_cobalt_tiny, 1));
        ShredderRecipes.setRecipe(ModItems.fragment_niobium, new ItemStack(ModItems.powder_niobium_tiny, 1));
        ShredderRecipes.setRecipe(ModItems.fragment_cerium, new ItemStack(ModItems.powder_cerium_tiny, 1));
        ShredderRecipes.setRecipe(ModItems.fragment_lanthanium, new ItemStack(ModItems.powder_lanthanium_tiny, 1));
        ShredderRecipes.setRecipe(ModItems.fragment_actinium, new ItemStack(ModItems.powder_actinium_tiny, 1));
        ShredderRecipes.setRecipe(ModItems.fragment_boron, new ItemStack(ModItems.powder_boron_tiny, 1));
        ShredderRecipes.setRecipe(ModItems.fragment_meteorite, new ItemStack(ModItems.powder_meteorite_tiny, 1));
        ShredderRecipes.setRecipe(ModBlocks.block_meteor, new ItemStack(ModItems.powder_meteorite, 10));
        ShredderRecipes.setRecipe(Items.ENCHANTED_BOOK, new ItemStack(ModItems.powder_magic, 1));
        ShredderRecipes.setRecipe(ModBlocks.meteor_polished, new ItemStack(ModItems.powder_meteorite, 1));
        ShredderRecipes.setRecipe(ModBlocks.meteor_brick, new ItemStack(ModItems.powder_meteorite, 1));
        ShredderRecipes.setRecipe(ModBlocks.meteor_brick_mossy, new ItemStack(ModItems.powder_meteorite, 1));
        ShredderRecipes.setRecipe(ModBlocks.meteor_brick_cracked, new ItemStack(ModItems.powder_meteorite, 1));
        ShredderRecipes.setRecipe(ModBlocks.meteor_brick_chiseled, new ItemStack(ModItems.powder_meteorite, 1));
        ShredderRecipes.setRecipe(ModBlocks.meteor_pillar, new ItemStack(ModItems.powder_meteorite, 1));
        ShredderRecipes.setRecipe(ModBlocks.ore_rare, new ItemStack(ModItems.powder_desh_mix, 1));
        ShredderRecipes.setRecipe(Blocks.DIAMOND_ORE, new ItemStack(ModBlocks.gravel_diamond, 2));
        ShredderRecipes.setRecipe(ModBlocks.ore_sellafield_diamond, new ItemStack(ModBlocks.gravel_diamond, 2));
        ShredderRecipes.setRecipe(ModBlocks.boxcar, new ItemStack(ModItems.powder_steel, 32));
        ShredderRecipes.setRecipe(ModItems.ingot_schrabidate, new ItemStack(ModItems.powder_schrabidate, 1));
        ShredderRecipes.setRecipe(ModBlocks.block_schrabidate, new ItemStack(ModItems.powder_schrabidate, 9));
        ShredderRecipes.setRecipe(ModItems.coal_infernal, new ItemStack(ModItems.powder_coal, 2));
        ShredderRecipes.setRecipe(Items.FERMENTED_SPIDER_EYE, new ItemStack(ModItems.powder_poison, 3));
        ShredderRecipes.setRecipe(Items.POISONOUS_POTATO, new ItemStack(ModItems.powder_poison, 1));
        ShredderRecipes.setRecipe(ModBlocks.ore_tektite_osmiridium, new ItemStack(ModItems.powder_tektite, 1));
        ShredderRecipes.setRecipe(Blocks.DIRT, new ItemStack(ModItems.dust, 1));
        ShredderRecipes.setRecipe(Items.REEDS, new ItemStack(Items.SUGAR, 3));
        ShredderRecipes.setRecipe(Items.APPLE, new ItemStack(Items.SUGAR, 1));
        ShredderRecipes.setRecipe(Items.CARROT, new ItemStack(Items.SUGAR, 1));
        ShredderRecipes.setRecipe(ModItems.can_empty, new ItemStack(ModItems.powder_aluminium, 2));
        ShredderRecipes.setRecipe(OreDictManager.DictFrame.fromOne(ModItems.chunk_ore, ItemEnums.EnumChunkType.RARE), new ItemStack(ModItems.powder_desh_mix));
        ShredderRecipes.setRecipe(Blocks.SAND, new ItemStack(ModItems.dust, 2));
        ShredderRecipes.setRecipe(ModBlocks.block_slag, new ItemStack(ModItems.powder_cement, 4));
        ShredderRecipes.setRecipe(ModBlocks.ore_aluminium, OreDictManager.DictFrame.fromOne(ModItems.chunk_ore, ItemEnums.EnumChunkType.CRYOLITE, 2));
        ShredderRecipes.setRecipe(ModBlocks.block_bakelite, new ItemStack(ModItems.powder_bakelite, 9));
        ShredderRecipes.setRecipe(ModItems.ingot_bakelite, new ItemStack(ModItems.powder_bakelite));

        List<ItemStack> logs = OreDictionary.getOres("logWood");
        List<ItemStack> planks = OreDictionary.getOres("plankWood");
        List<ItemStack> saplings = OreDictionary.getOres("treeSapling");

        for (ItemStack log : logs)
            ShredderRecipes.setRecipe(log, new ItemStack(ModItems.powder_sawdust, 4));
        for (ItemStack plank : planks)
            ShredderRecipes.setRecipe(plank, new ItemStack(ModItems.powder_sawdust, 1));
        for (ItemStack sapling : saplings)
            ShredderRecipes.setRecipe(sapling, new ItemStack(Items.STICK, 1));

        for (int i = 0; i < 5; i++)
            ShredderRecipes.setRecipe(new ItemStack(Items.SKULL, 1, i), new ItemStack(ModItems.biomass, 4));

        /* Crystal processing */
        ShredderRecipes.setRecipe(ModItems.ingot_schraranium, new ItemStack(ModItems.nugget_schrabidium, 2));
        ShredderRecipes.setRecipe(ModItems.crystal_coal, new ItemStack(ModItems.powder_coal, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_iron, new ItemStack(ModItems.powder_iron, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_gold, new ItemStack(ModItems.powder_gold, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_redstone, new ItemStack(Items.REDSTONE, 8));
        ShredderRecipes.setRecipe(ModItems.crystal_lapis, new ItemStack(ModItems.powder_lapis, 8));
        ShredderRecipes.setRecipe(ModItems.crystal_diamond, new ItemStack(ModItems.powder_diamond, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_uranium, new ItemStack(ModItems.powder_uranium, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_plutonium, new ItemStack(ModItems.powder_plutonium, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_thorium, new ItemStack(ModItems.powder_thorium, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_titanium, new ItemStack(ModItems.powder_titanium, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_sulfur, new ItemStack(ModItems.sulfur, 8));
        ShredderRecipes.setRecipe(ModItems.crystal_niter, new ItemStack(ModItems.niter, 8));
        ShredderRecipes.setRecipe(ModItems.crystal_copper, new ItemStack(ModItems.powder_copper, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_tungsten, new ItemStack(ModItems.powder_tungsten, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_aluminium, new ItemStack(ModItems.powder_aluminium, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_fluorite, new ItemStack(ModItems.fluorite, 8));
        ShredderRecipes.setRecipe(ModItems.crystal_beryllium, new ItemStack(ModItems.powder_beryllium, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_lead, new ItemStack(ModItems.powder_lead, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_schraranium, new ItemStack(ModItems.nugget_schrabidium, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_schrabidium, new ItemStack(ModItems.powder_schrabidium, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_rare, new ItemStack(ModItems.powder_desh_mix, 2));
        ShredderRecipes.setRecipe(ModItems.crystal_phosphorus, new ItemStack(ModItems.powder_fire, 8));
        ShredderRecipes.setRecipe(ModItems.crystal_trixite, new ItemStack(ModItems.powder_plutonium, 6));
        ShredderRecipes.setRecipe(ModItems.crystal_lithium, new ItemStack(ModItems.powder_lithium, 3));
        ShredderRecipes.setRecipe(ModItems.crystal_starmetal, new ItemStack(ModItems.powder_dura_steel, 6));
        ShredderRecipes.setRecipe(ModItems.crystal_cobalt, new ItemStack(ModItems.powder_cobalt, 3));

        /* Misc recycling */
        ShredderRecipes.setRecipe(ModBlocks.steel_poles, new ItemStack(ModItems.powder_steel_tiny, 2));
        ShredderRecipes.setRecipe(ModBlocks.steel_roof, new ItemStack(ModItems.powder_steel_tiny, 9));
        ShredderRecipes.setRecipe(ModBlocks.steel_wall, new ItemStack(ModItems.powder_steel_tiny, 9));
        ShredderRecipes.setRecipe(ModBlocks.steel_corner, new ItemStack(ModItems.powder_steel_tiny, 18));
        ShredderRecipes.setRecipe(ModBlocks.steel_beam, new ItemStack(ModItems.powder_steel_tiny, 3));
        ShredderRecipes.setRecipe(new ItemStack(ModBlocks.steel_scaffold, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ModItems.powder_steel_tiny, 4));
        ShredderRecipes.setRecipe(ModItems.coil_copper, new ItemStack(ModItems.powder_red_copper, 1));
        ShredderRecipes.setRecipe(ModItems.coil_copper_torus, new ItemStack(ModItems.powder_red_copper, 2));
        ShredderRecipes.setRecipe(ModItems.coil_advanced_alloy, new ItemStack(ModItems.powder_advanced_alloy, 1));
        ShredderRecipes.setRecipe(ModItems.coil_advanced_torus, new ItemStack(ModItems.powder_advanced_alloy, 2));
        ShredderRecipes.setRecipe(ModItems.coil_gold, new ItemStack(ModItems.powder_gold, 1));
        ShredderRecipes.setRecipe(ModItems.coil_gold_torus, new ItemStack(ModItems.powder_gold, 2));
        ShredderRecipes.setRecipe(ModItems.coil_tungsten, new ItemStack(ModItems.powder_tungsten, 1));
        ShredderRecipes.setRecipe(ModItems.coil_magnetized_tungsten, new ItemStack(ModItems.powder_magnetized_tungsten, 1));
        ShredderRecipes.setRecipe(ModBlocks.crate_iron, new ItemStack(ModItems.powder_iron, 8));
        ShredderRecipes.setRecipe(ModBlocks.crate_steel, new ItemStack(ModItems.powder_steel, 8));
        ShredderRecipes.setRecipe(ModBlocks.crate_tungsten, new ItemStack(ModItems.powder_tungsten, 36));
        ShredderRecipes.setRecipe(Blocks.ANVIL, new ItemStack(ModItems.powder_iron, 31));
        ShredderRecipes.setRecipe(ModBlocks.chain, new ItemStack(ModItems.powder_steel_tiny, 1));
        ShredderRecipes.setRecipe(ModBlocks.steel_grate, new ItemStack(ModItems.powder_steel_tiny, 3));
        ShredderRecipes.setRecipe(ModItems.pipes_steel, new ItemStack(ModItems.powder_steel, 27));
        ShredderRecipes.setRecipe(new ItemStack(ModItems.bedrock_ore, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Blocks.GRAVEL));

        /* Sellafite scrapping */
        ShredderRecipes.setRecipe(ModBlocks.sellafield_slaked, new ItemStack(Blocks.GRAVEL));
        ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 0), new ItemStack(ModItems.scrap_nuclear, 1));
        ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 1), new ItemStack(ModItems.scrap_nuclear, 2));
        ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 2), new ItemStack(ModItems.scrap_nuclear, 3));
        ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 3), new ItemStack(ModItems.scrap_nuclear, 5));
        ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 4), new ItemStack(ModItems.scrap_nuclear, 7));
        ShredderRecipes.setRecipe(new ItemStack(ModBlocks.sellafield, 1, 5), new ItemStack(ModItems.scrap_nuclear, 15));

        /* Fracking debris scrapping */
        ShredderRecipes.setRecipe(ModBlocks.dirt_dead, new ItemStack(ModItems.scrap_oil, 1));
        ShredderRecipes.setRecipe(ModBlocks.dirt_oily, new ItemStack(ModItems.scrap_oil, 1));
        ShredderRecipes.setRecipe(ModBlocks.sand_dirty, new ItemStack(ModItems.scrap_oil, 1));
        ShredderRecipes.setRecipe(ModBlocks.sand_dirty_red, new ItemStack(ModItems.scrap_oil, 1));
        ShredderRecipes.setRecipe(ModBlocks.stone_cracked, new ItemStack(ModItems.scrap_oil, 1));
        ShredderRecipes.setRecipe(ModBlocks.stone_porous, new ItemStack(ModItems.scrap_oil, 1));

        /* Deco pipe recycling */
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rusted, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_green, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_green_rusted, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_red, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_marked, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim_rusted, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim_green, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim_green_rusted, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim_red, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_rim_marked, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad_rusted, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad_green, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad_green_rusted, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad_red, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_quad_marked, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed_rusted, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed_green, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed_green_rusted, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed_red, new ItemStack(ModItems.powder_steel, 1));
        ShredderRecipes.setRecipe(ModBlocks.deco_pipe_framed_marked, new ItemStack(ModItems.powder_steel, 1));

        /* Wool and clay scrapping */
        for (int i = 0; i < 16; i++) {
            ShredderRecipes.setRecipe(new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, i), new ItemStack(Items.CLAY_BALL, 4));
            ShredderRecipes.setRecipe(new ItemStack(Blocks.WOOL, 1, i), new ItemStack(Items.STRING, 4));
        }

        /* Shredding bobbleheads */
        for(int i = 0; i < BobbleType.VALUES.length; i++) {
            ShredderRecipes.setRecipe(new ItemStack(ModBlocks.bobblehead, 1, i), new ItemStack(ModItems.scrap_plastic, 1));
        }

        /* Debris shredding */
        ShredderRecipes.setRecipe(ModItems.debris_concrete, new ItemStack(ModItems.scrap_nuclear, 2));
        ShredderRecipes.setRecipe(ModItems.debris_shrapnel, new ItemStack(ModItems.powder_steel_tiny, 5));
        ShredderRecipes.setRecipe(ModItems.debris_exchanger, new ItemStack(ModItems.powder_steel, 3));
        ShredderRecipes.setRecipe(ModItems.debris_element, new ItemStack(ModItems.scrap_nuclear, 4));
        ShredderRecipes.setRecipe(ModItems.debris_metal, new ItemStack(ModItems.powder_steel_tiny, 3));
        ShredderRecipes.setRecipe(ModItems.debris_graphite, new ItemStack(ModItems.powder_coal, 1));

        /* GC COMPAT */
		/*Block gcMoonBlock = Compat.tryLoadBlock(Compat.MOD_GCC, "moonBlock");
		if(gcMoonBlock != null && gcMoonBlock != Blocks.AIR) {
			ShredderRecipes.setRecipe(new ItemStack(gcMoonBlock, 1, 3), new ItemStack(ModBlocks.moon_turf)); //Moon dirt
			ShredderRecipes.setRecipe(new ItemStack(gcMoonBlock, 1, 5), new ItemStack(ModBlocks.moon_turf)); //Moon topsoil
		}

		/* AR COMPAT */
		/*Block arMoonTurf = Compat.tryLoadBlock(Compat.MOD_AR, "turf");
		if(arMoonTurf != null && arMoonTurf != Blocks.AIR) ShredderRecipes.setRecipe(arMoonTurf, new ItemStack(ModBlocks.moon_turf)); //i assume it's moon turf
		Block arMoonTurfDark = Compat.tryLoadBlock(Compat.MOD_AR, "turfDark");
		if(arMoonTurfDark != null && arMoonTurfDark != Blocks.AIR) ShredderRecipes.setRecipe(arMoonTurfDark, new ItemStack(ModBlocks.moon_turf));*/ //probably moon dirt? would have helped if i had ever played AR for more than 5 seconds
    }

    @Override
    public String getFileName() {
        return "hbmShredder.json";
    }

    @Override
    public Object getRecipeObject() {
        return shredderRecipes;
    }

    @Override
    public void readRecipe(JsonElement recipe) {
        JsonObject obj = (JsonObject) recipe;
        ItemStack stack = readItemStack(obj.get("input").getAsJsonArray());
        ComparableStack comp = new ComparableStack(stack).makeSingular();
        ItemStack out = readItemStack(obj.get("output").getAsJsonArray());
        shredderRecipes.put(comp, out);
    }

    @Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        Entry<ComparableStack, ItemStack> entry = (Entry<ComparableStack, ItemStack>) recipe;

        writer.name("input");
        writeItemStack(entry.getKey().toStack(), writer);
        writer.name("output");
        writeItemStack(entry.getValue(), writer);
    }

    @Override
    public void deleteRecipes() {
        shredderRecipes.clear();
        jeiShredderRecipes = null;
    }

    @Override
    public String getComment() {
        return "Ingot/block/ore -> dust recipes are generated in post and can therefore not be changed with the config. Non-auto recipes do not use ore dict.";
    }
}
