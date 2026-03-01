package com.hbm.inventory;

import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidCapabilityHandler;
import com.hbm.config.GeneralConfig;
import com.hbm.forgefluid.SpecialContainerFillLists;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FluidContainerRegistry {
    public static final Set<FluidContainer> allContainers = new ObjectOpenHashSet<>();
    private static final Reference2ObjectMap<Item, Int2ObjectOpenHashMap<FluidContainer>> fullContainerMapByItem = new Reference2ObjectOpenHashMap<>();
    private static final Reference2ObjectMap<Item, Int2ObjectOpenHashMap<Candidates>> emptyContainerMapByItem = new Reference2ObjectOpenHashMap<>();

    public static void register() {
        // mlbv: I commented manual conversions out as they are now being handled by my capability wrapper

        // Vanilla buckets & bottles
        /*
        registerContainer(new FluidContainer(new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET), Fluids.WATER, 1000));
        registerContainer(new FluidContainer(new ItemStack(Items.POTIONITEM), new ItemStack(Items.GLASS_BOTTLE), Fluids.WATER, 250));
        registerContainer(new FluidContainer(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.BUCKET), Fluids.LAVA, 1000));

        // Buckets for our fluids
        registerContainer(new FluidContainer(new ItemStack(ModFluids.mud_fluid.getBlock()), new ItemStack(Items.BUCKET), Fluids.WATZ, 1000));
        registerContainer(new FluidContainer(new ItemStack(ModFluids.schrabidic_fluid.getBlock()), new ItemStack(Items.BUCKET), Fluids.SCHRABIDIC, 1000));
        registerContainer(new FluidContainer(new ItemStack(ModFluids.sulfuric_acid_fluid.getBlock()), new ItemStack(Items.BUCKET), Fluids.SULFURIC_ACID, 1000));
        */

        // Barrels → tanks
        registerContainer(new FluidContainer(new ItemStack(ModBlocks.red_barrel), new ItemStack(ModItems.tank_steel), Fluids.DIESEL, 10000));
        registerContainer(new FluidContainer(new ItemStack(ModBlocks.pink_barrel), new ItemStack(ModItems.tank_steel), Fluids.KEROSENE, 10000));
        registerContainer(new FluidContainer(new ItemStack(ModBlocks.lox_barrel), new ItemStack(ModItems.tank_steel), Fluids.OXYGEN, 10000));

        // Ores & special items
        registerContainer(new FluidContainer(new ItemStack(ModBlocks.ore_oil), null, Fluids.OIL, 250));
        registerContainer(new FluidContainer(new ItemStack(ModBlocks.ore_gneiss_gas), null, Fluids.PETROLEUM, GeneralConfig.enable528 ? 50 : 250));
        registerContainer(new FluidContainer(new ItemStack(ModItems.bottle_mercury), new ItemStack(Items.GLASS_BOTTLE), Fluids.MERCURY, 1000));
        registerContainer(new FluidContainer(new ItemStack(ModItems.ingot_mercury), null, Fluids.MERCURY, 125));
        registerContainer(new FluidContainer(new ItemStack(ModItems.rod_zirnox_tritium), new ItemStack(ModItems.rod_zirnox_empty), Fluids.TRITIUM, 2000));

        // Particles
        registerContainer(new FluidContainer(new ItemStack(ModItems.particle_hydrogen), new ItemStack(ModItems.particle_empty), Fluids.HYDROGEN, 1000));
        registerContainer(new FluidContainer(new ItemStack(ModItems.particle_amat), new ItemStack(ModItems.particle_empty), Fluids.AMAT, 1000));
        registerContainer(new FluidContainer(new ItemStack(ModItems.particle_aschrab), new ItemStack(ModItems.particle_empty), Fluids.ASCHRAB, 1000));

        // IVs
        registerContainer(new FluidContainer(new ItemStack(ModItems.iv_blood), new ItemStack(ModItems.iv_empty), Fluids.BLOOD, 100));
        registerContainer(new FluidContainer(new ItemStack(ModItems.iv_xp), new ItemStack(ModItems.iv_xp_empty), Fluids.XPJUICE, 100));


        // Dynamic containers
        FluidType[] fluids = Fluids.getAll();
        for (int i = 1; i < fluids.length; i++) {
            FluidType type = fluids[i];
            int id = type.getID();

            if (type.getContainer(Fluids.CD_Canister.class) != null) {
                registerContainer(new FluidContainer(new ItemStack(ModItems.canister_full, 1, id), new ItemStack(ModItems.canister_empty), type, 1000));
            }
            if (type.getContainer(Fluids.CD_Gastank.class) != null) {
                registerContainer(new FluidContainer(new ItemStack(ModItems.gas_full, 1, id), new ItemStack(ModItems.gas_empty), type, 1000));
            }

            if (!type.hasNoContainer()) {
                if (type.isDispersable()) {
                    registerContainer(new FluidContainer(new ItemStack(ModItems.disperser_canister, 1, i), new ItemStack(ModItems.disperser_canister_empty), Fluids.fromID(i), 2000));
                    registerContainer(new FluidContainer(new ItemStack(ModItems.glyphid_gland, 1, i), new ItemStack(ModItems.glyphid_gland_empty), Fluids.fromID(i), 4000));
                }
                registerContainer(new FluidContainer(new ItemStack(ModItems.fluid_tank_lead_full, 1, id), new ItemStack(ModItems.fluid_tank_lead_empty), type, 1000));
                if (!type.needsLeadContainer()) {
                    registerContainer(new FluidContainer(new ItemStack(ModItems.fluid_tank_full, 1, id), new ItemStack(ModItems.fluid_tank_empty), type, 1000));
                    registerContainer(new FluidContainer(new ItemStack(ModItems.fluid_barrel_full, 1, id), new ItemStack(ModItems.fluid_barrel_empty), type, 16000));
                }
            }
        }

        // Cells
        for (FluidType type : SpecialContainerFillLists.EnumCell.getFluids()) {
            if (type != null) {
                FluidContainer cell = new FluidContainer(new ItemStack(ModItems.cell, 1, type.getID()), new ItemStack(ModItems.cell, 1, 0), type, 1000);
                registerContainer(cell);
            }
        }

        NTMFluidCapabilityHandler.initialize();
    }

    public static void registerContainer(FluidContainer con) {
        allContainers.add(con);
        OreDictionary.registerOre(con.type().getDict(con.content()), con.fullContainer());

        // full
        final Item fullItem = con.fullContainer().getItem();
        final int fullMeta = con.fullContainer().getMetadata();
        Int2ObjectOpenHashMap<FluidContainer> metaMap = fullContainerMapByItem.computeIfAbsent(fullItem, k -> new Int2ObjectOpenHashMap<>(4));
        metaMap.put(fullMeta, con);

        // empty -> candidates
        ItemStack empty = con.emptyContainer();
        if (empty != null && !empty.isEmpty()) {
            final Item emptyItem = empty.getItem();
            final int emptyMeta = empty.getMetadata();

            Int2ObjectOpenHashMap<Candidates> emptyMetaMap = emptyContainerMapByItem.computeIfAbsent(emptyItem, k -> new Int2ObjectOpenHashMap<>(4));
            Candidates bucket = emptyMetaMap.get(emptyMeta);
            if (bucket == null) {
                bucket = new Candidates();
                emptyMetaMap.put(emptyMeta, bucket);
            }
            bucket.byType.put(con.type(), con);
            bucket.asList.add(con);
            if (con.content() > bucket.maxCapacity) bucket.maxCapacity = con.content();
        }
    }

    /**
     * @return amount of a specific fluid in the given full container stack.
     */
    @Contract(pure = true)
    public static int getFluidContent(ItemStack stack, FluidType type) {
        if (stack == null || stack.isEmpty() || type == null) return 0;

        if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (handler != null) {
                FluidStack fs = FluidUtil.getFluidContained(stack);
                if (fs != null) {
                    FluidType t = NTMFluidCapabilityHandler.getFluidType(fs.getFluid());
                    if (t == type) {
                        return fs.amount;
                    }
                }
            }
        }

        FluidContainer recipe = getFluidContainer(stack);
        return (recipe != null && recipe.type() == type) ? recipe.content() : 0;
    }

    @Contract(pure = true)
    public static int getFluidContent(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;

        FluidStack fs = FluidUtil.getFluidContained(stack);
        if (fs != null) {
            return fs.amount;
        }

        FluidContainer recipe = getFluidContainer(stack);
        return recipe != null ? recipe.content() : 0;
    }

    /**
     * Gets the FluidType contained in a full container stack.
     */
    @NotNull
    @Contract(pure = true, value = "_->!null")
    public static FluidType getFluidType(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Fluids.NONE;
        FluidContainer recipe = getFluidContainer(stack);
        return recipe != null ? recipe.type() : Fluids.NONE;
    }

    /**
     * Gets the full container item for a given empty container and fluid type. Count insensitive.
     * @return a copy of the full container item for the given empty container and fluid type, or null if none is found.
     */
    @Nullable
    @Contract(pure = true, value = "null,_ -> null; _,null -> null")
    public static ItemStack getFullContainer(ItemStack stack, FluidType type) {
        if (stack == null || stack.isEmpty() || type == null) return null;
        FluidContainer recipe = getFillRecipe(stack, type);
        return recipe != null ? recipe.fullContainer().copy() : null;
    }

    /**
     * Gets the empty container item for a given full container stack.
     * @return a copy of the empty container item for the given full container stack, or null if none is found.
     */
    @Contract(pure = true)
    public static ItemStack getEmptyContainer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        FluidContainer recipe = getFluidContainer(stack);
        if (recipe != null && recipe.emptyContainer() != null) return recipe.emptyContainer().copy();
        return ItemStack.EMPTY;
    }

    /**
     * @return the FluidContainer of the given full container stack, or null if none is found.
     */
    @Nullable
    @Contract(pure = true)
    public static FluidContainer getFluidContainer(@NotNull ItemStack fullStack) {
        if (fullStack.isEmpty()) return null;
        Int2ObjectOpenHashMap<FluidContainer> metaMap = fullContainerMapByItem.get(fullStack.getItem());
        if (metaMap == null) return null;
        final int meta = fullStack.getMetadata();
        FluidContainer fc = metaMap.get(meta);
        if (fc == null && meta != OreDictionary.WILDCARD_VALUE) fc = metaMap.get(OreDictionary.WILDCARD_VALUE);
        return fc;
    }

    /**
     * @return the FluidContainer of the given empty container and FluidType, or null if none is found.
     */
    @Nullable
    @Contract(pure = true, value = "_,null -> null")
    public static FluidContainer getFillRecipe(@NotNull ItemStack emptyStack, @Nullable FluidType type) {
        if (emptyStack.isEmpty() || type == null) return null;
        Int2ObjectOpenHashMap<Candidates> metaMap = emptyContainerMapByItem.get(emptyStack.getItem());
        if (metaMap == null) return null;
        final int meta = emptyStack.getMetadata();
        Candidates bucket = metaMap.get(meta);
        if (bucket == null && meta != OreDictionary.WILDCARD_VALUE) bucket = metaMap.get(OreDictionary.WILDCARD_VALUE);
        if (bucket == null) return null;
        return bucket.byType.get(type);
    }

    @Nullable
    @Contract(pure = true)
    public static FluidContainer getFillRecipe(@NotNull ItemStack emptyStack, @NotNull Fluid fluid) {
        return getFillRecipe(emptyStack, NTMFluidCapabilityHandler.getFluidType(fluid));
    }

    /**
     * Gets all possible fill recipes for a given empty item stack.
     *
     * @return A list of possible FluidContainer recipes, or an empty list if none are found.
     * @apiNote the returned List must not be modified.
     */
    @NotNull
    @Contract(pure = true, value = "_->!null")
    public static List<FluidContainer> getFillRecipes(@NotNull ItemStack emptyStack) {
        if (emptyStack.isEmpty()) return Collections.emptyList();
        Int2ObjectOpenHashMap<Candidates> metaMap = emptyContainerMapByItem.get(emptyStack.getItem());
        if (metaMap == null) return Collections.emptyList();
        final int meta = emptyStack.getMetadata();
        Candidates bucket = metaMap.get(meta);
        if (bucket == null && meta != OreDictionary.WILDCARD_VALUE) bucket = metaMap.get(OreDictionary.WILDCARD_VALUE);
        return bucket != null ? bucket.asList : Collections.emptyList();
    }

    @Contract(pure = true)
    public static int getMaxFillCapacity(@NotNull ItemStack emptyStack) {
        if (emptyStack.isEmpty()) return 0;
        Int2ObjectOpenHashMap<Candidates> metaMap = emptyContainerMapByItem.get(emptyStack.getItem());
        if (metaMap == null) return 0;
        final int meta = emptyStack.getMetadata();
        Candidates bucket = metaMap.get(meta);
        if (bucket == null && meta != OreDictionary.WILDCARD_VALUE) bucket = metaMap.get(OreDictionary.WILDCARD_VALUE);
        return bucket == null ? 0 : bucket.maxCapacity;
    }

    public record FluidContainer(@NotNull ItemStack fullContainer, @Nullable ItemStack emptyContainer,
                                 @NotNull FluidType type, int content) {
    }

    private static final class Candidates {
        final Reference2ObjectOpenHashMap<FluidType, FluidContainer> byType = new Reference2ObjectOpenHashMap<>(4);
        final ObjectArrayList<FluidContainer> asList = new ObjectArrayList<>(4);
        int maxCapacity;
    }
}
