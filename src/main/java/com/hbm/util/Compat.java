package com.hbm.util;

import appeng.api.AEApi;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.hbm.util.Compat.ModIds.*;

public class Compat {
    private static final boolean MOD_EIDS = Loader.isModLoaded("jeid") || Loader.isModLoaded("neid");
    private static final boolean MOD_OC = Loader.isModLoaded(ModIds.OPEN_COMPUTERS);

    public static boolean isIDExtensionModLoaded() {
        return MOD_EIDS;
    }

    public static boolean isOpenComputersLoaded() {
        return MOD_OC;
    }

    public static Item tryLoadItem(String domain, String name) {
        return Item.REGISTRY.getObject(new ResourceLocation(domain, name));
    }

    public static Block tryLoadBlock(String domain, String name) {
        return Block.REGISTRY.getObject(new ResourceLocation(domain, name));
    }

    public static TileEntity getTileStandard(World world, int x, int y, int z) {
        if (!world.getChunkProvider().isChunkGeneratedAt(x >> 4, z >> 4)) return null;
        return world.getTileEntity(new BlockPos(x, y, z));
    }

    @Nullable
    @Optional.Method(modid = ModIds.AE2)
    public static IItemList<IAEItemStack> scrapeItemFromME(final ItemStack cell) {
        final IStorageChannel<IAEItemStack> ch = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        final ICellInventoryHandler<IAEItemStack> handler = AEApi.instance().registries().cell().getCellInventory(cell, null, ch);
        if (handler == null) return null;
        final ICellInventory<IAEItemStack> inv = handler.getCellInv();
        final IItemList<IAEItemStack> list = ch.createList();
        inv.getAvailableItems(list);
        return list;
    }

    public static void exitOnIncompatible() {
        final Map<String,String> humanReadable = Map.of(
                HBM_NTM_LUCKY_BLOCKS, "\"HBM NTM Lucky blocks\" by Eag0la",
                POTATOO_STRUCTURE, "\"Potatoo's Custom Structure For HBM's Nuclear Tech Mod\" by Potatoo_Cake",
                HBM_NTM_STRUCTURE, "\"HBM/NTM structure\" by AlimodyKorol"
        );
        for (String mod : ModIds.INCOMPATIBLE_MODS) {
            if (Loader.isModLoaded(mod)) {
                throw new RuntimeException("Mod: " + humanReadable.get(mod) + " is an NTM:EE addon, not compatible with NTM:CE. Please contact the addon developer");
            }
        }
    }

    public static final class ModIds {
        public static final String GROOVY_SCRIPT = "groovyscript";
        public static final String OPEN_COMPUTERS = "opencomputers";
        public static final String CTM = "ctm";
        public static final String AE2 = "appliedenergistics2";
        public static final String MODERN_SPLASH = "modernsplash";
        public static final String HBM_NTM_STRUCTURE = "ntmdopolnenie"; //Yes, this is the modid. Idk what this means
        public static final String POTATOO_STRUCTURE = "potatooscustomstructureforhbm"; //Can we just block all mccreator mods?
        public static final String HBM_NTM_LUCKY_BLOCKS = "luckynuke"; //It's all fucking garbage;
        public static final String[] INCOMPATIBLE_MODS = {HBM_NTM_LUCKY_BLOCKS, POTATOO_STRUCTURE, HBM_NTM_STRUCTURE};


    }
}
