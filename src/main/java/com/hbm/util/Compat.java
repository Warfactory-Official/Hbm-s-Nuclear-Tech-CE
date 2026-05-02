package com.hbm.util;

import appeng.api.AEApi;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import com.hbm.handler.HazmatRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.hbm.util.Compat.ModIds.*;

public class Compat {
    private static final boolean MOD_EIDS = Loader.isModLoaded("jeid") || Loader.isModLoaded("neid");
    private static final boolean MOD_OC = Loader.isModLoaded(ModIds.OPEN_COMPUTERS);
    public static final boolean REDSTONE_FLUX_LOADED = Loader.isModLoaded("redstoneflux");
    public static final boolean BAUBLES_LOADED = Loader.isModLoaded("baubles");//there are a lot of forks but they all use the same modid as the original

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

    public static void registerCompatHazmat() {

        double helmet = 0.2D;
        double chest = 0.4D;
        double legs = 0.3D;
        double boots = 0.1D;

        double p90 = 1.0D; // 90%
        double p99 = 2D; // 99%

        tryRegisterHazmat("gregtech", "gt.armor.hazmat.radiation.head",		p90 * helmet);
        tryRegisterHazmat("gregtech", "gt.armor.hazmat.radiation.chest",	p90 * chest);
        tryRegisterHazmat("gregtech", "gt.armor.hazmat.radiation.legs",		p90 * legs);
        tryRegisterHazmat("gregtech", "gt.armor.hazmat.radiation.boots",	p90 * boots);

        tryRegisterHazmat("gregtech", "gt.armor.hazmat.universal.head",		p99 * helmet);
        tryRegisterHazmat("gregtech", "gt.armor.hazmat.universal.chest",	p99 * chest);
        tryRegisterHazmat("gregtech", "gt.armor.hazmat.universal.legs",		p99 * legs);
        tryRegisterHazmat("gregtech", "gt.armor.hazmat.universal.boots",	p99 * boots);

        tryRegisterHazmat("futureminecraf", "netherite_helmet", 		p90 * helmet);
        tryRegisterHazmat("futureminecraf", "netherite_chestplate",	p90 * chest);
        tryRegisterHazmat("futureminecraf", "netherite_leggings",		p90 * legs);
        tryRegisterHazmat("futureminecraf", "netherite_boots",			p90 * boots);
    }

    private static void tryRegisterHazmat(String mod, String name, double resistance) {
        Item item = Compat.tryLoadItem(mod, name);
        if(item != null) {
            HazmatRegistry.registerHazmat(item, resistance);
        }
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
