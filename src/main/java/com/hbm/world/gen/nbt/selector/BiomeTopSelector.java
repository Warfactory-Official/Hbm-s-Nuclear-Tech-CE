package com.hbm.world.gen.nbt.selector;

import java.util.Random;

public class BiomeTopSelector extends BiomeBlockSelector {

    @Override
    public void selectBlocks(Random rand, int x, int y, int z, boolean wall) {
        this.blockstate = nextBiome.topBlock;
    }

}
