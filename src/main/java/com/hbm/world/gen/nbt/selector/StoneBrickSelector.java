package com.hbm.world.gen.nbt.selector;

import net.minecraft.init.Blocks;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;

import java.util.Random;

public class StoneBrickSelector extends BlockSelector {

    @Override
    public void selectBlocks(Random rand, int x, int y, int z, boolean wall) {
        float f = rand.nextFloat();

        if (f < 0.2F) {
            this.blockstate = Blocks.STONEBRICK.getStateFromMeta(2);
        } else if (f < 0.5F) {
            this.blockstate = Blocks.STONEBRICK.getStateFromMeta(1);
        } else {
            this.blockstate = Blocks.STONEBRICK.getDefaultState();
        }
    }

}
