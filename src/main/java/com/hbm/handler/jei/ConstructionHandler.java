package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.fusion.MachineFusionTorus;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.util.ItemStackUtil;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConstructionHandler extends JEIUniversalHandler {

    public ConstructionHandler(IGuiHelper helper) {
        super(helper, JEIConfig.CONSTRUCTION, "jei.construction",
                new ItemStack[]{
                        new ItemStack(ModItems.acetylene_torch),
                        new ItemStack(ModItems.blowtorch),
                        new ItemStack(ModItems.boltgun)},
                wrapRecipes3(getRecipesForJEI()));
    }

    public static final HashMap<Object[], Object> bufferedRecipes = new HashMap<>();

    public static HashMap<Object[], Object> getRecipesForJEI() {
        if (!bufferedRecipes.isEmpty()) {
            return bufferedRecipes;
        }

        /* WATZ */
        ItemStack[] watz = new ItemStack[]{
                new ItemStack(ModBlocks.watz_casing, 48),
                Mats.MAT_DURA.make(ModItems.bolt, 64),
                Mats.MAT_DURA.make(ModItems.bolt, 64),
                Mats.MAT_DURA.make(ModItems.bolt, 64),
                new ItemStack(ModBlocks.watz_element, 36),
                new ItemStack(ModBlocks.watz_cooler, 26),
                new ItemStack(ModItems.boltgun)
        };

        bufferedRecipes.put(watz, new ItemStack(ModBlocks.watz));

        /* ITER ACKED*/


        /* PLASMA HEATER */
        ItemStack[] heater = new ItemStack[]{
                new ItemStack(ModBlocks.fusion_heater, 7),
                new ItemStack(ModBlocks.fusion_heater, 64),
                new ItemStack(ModBlocks.fusion_heater, 64)
        };

        bufferedRecipes.put(heater, new ItemStack(ModBlocks.plasma_heater));

        /* COMPACT LAUNCHER */
        ItemStack[] launcher = new ItemStack[]{new ItemStack(ModBlocks.struct_launcher, 8)};

        bufferedRecipes.put(launcher, new ItemStack(ModBlocks.compact_launcher));

        /* LAUNCH TABLE */
        ItemStack[] table = new ItemStack[]{
                new ItemStack(ModBlocks.struct_launcher, 16),
                new ItemStack(ModBlocks.struct_launcher, 64),
                new ItemStack(ModBlocks.struct_scaffold, 11)
        };

        bufferedRecipes.put(table, new ItemStack(ModBlocks.launch_table));

        /* SOYUZ LAUNCHER */
        ItemStack[] soysauce = new ItemStack[]{
                new ItemStack(ModBlocks.struct_launcher, 30),
                ItemStackUtil.addStackSizeLabel(new ItemStack(ModBlocks.struct_launcher, 384)),
                new ItemStack(ModBlocks.struct_scaffold, 63),
                ItemStackUtil.addStackSizeLabel(new ItemStack(ModBlocks.struct_scaffold, 384)),
                new ItemStack(ModBlocks.concrete_smooth, 38),
                ItemStackUtil.addStackSizeLabel(new ItemStack(ModBlocks.concrete_smooth, 320))
        };

        bufferedRecipes.put(soysauce, new ItemStack(ModBlocks.soyuz_launcher));

        /* ICF */
        Object[] icf = new Object[]{
                new ItemStack(ModBlocks.icf_component, 50, 0),
                ItemStackUtil.addStackSizeLabel(new ItemStack(ModBlocks.icf_component, 240, 3)),
                ItemStackUtil.addStackSizeLabel(Mats.MAT_DURA.make(ModItems.bolt, 960)),
                ItemStackUtil.addStackSizeLabel(Mats.MAT_STEEL.make(ModItems.plate_cast, 240)),
                ItemStackUtil.addStackSizeLabel(new ItemStack(ModBlocks.icf_component, 117, 1)),
                new ItemStack[]{
                        ItemStackUtil.addStackSizeLabel(Mats.MAT_BBRONZE.make(ModItems.plate_cast, 117)),
                        ItemStackUtil.addStackSizeLabel(Mats.MAT_ABRONZE.make(ModItems.plate_cast, 117))
                },
                new ItemStack(ModItems.blowtorch),
                new ItemStack(ModItems.boltgun)
        };

        bufferedRecipes.put(icf, new ItemStack(ModBlocks.icf));

        /* FUSION TORUS */
        int wallCount = 0;
        int blanketCount = 0;
        int pipeCount = -1;

        for (int iy = 0; iy < 5; iy++) {
            int l = iy > 2 ? 4 - iy : iy;
            int[][] layer = MachineFusionTorus.layout[l];
            for (int[] ints : layer) {
                for (int iz = 0; iz < layer.length; iz++) {
                    int meta = ints[iz];
                    if (meta == 1) {
                        wallCount++;
                    } else if (meta == 2) {
                        blanketCount++;
                    } else if (meta == 3) {
                        pipeCount++;
                    }
                }
            }
        }

        List<ItemStack> torusItems = new ArrayList<>();
        int plateCount = wallCount;

        while (wallCount > 0) {
            int amount = Math.min(wallCount, 256);
            torusItems.add(new ItemStack(ModBlocks.fusion_component, amount, 0));
            wallCount -= amount;
        }

        while (plateCount > 0) {
            int amount = Math.min(plateCount, 256);
            torusItems.add(Mats.MAT_STEEL.make(ModItems.plate_cast, amount));
            plateCount -= amount;
        }

        while (blanketCount > 0) {
            int amount = Math.min(blanketCount, 256);
            torusItems.add(new ItemStack(ModBlocks.fusion_component, amount, 2));
            blanketCount -= amount;
        }

        while (pipeCount > 0) {
            int amount = Math.min(pipeCount, 256);
            torusItems.add(new ItemStack(ModBlocks.fusion_component, amount, 3));
            pipeCount -= amount;
        }

        torusItems.add(new ItemStack(ModItems.blowtorch));

        for (ItemStack stack : torusItems) {
            ItemStackUtil.addStackSizeLabel(stack);
        }

        ItemStack[] torus = torusItems.toArray(new ItemStack[0]);
        bufferedRecipes.put(torus, new ItemStack(ModBlocks.fusion_torus));

        return bufferedRecipes;
    }
}
