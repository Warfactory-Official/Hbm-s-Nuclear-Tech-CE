package com.hbm.world.gen.nbt.selector;

import net.minecraft.init.Blocks;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;

import java.util.Random;

public class BrickSelector extends BlockSelector {

    @Override
    public void selectBlocks(Random rand, int x, int y, int z, boolean wall) {
        this.blockstate = Blocks.BRICK_BLOCK.getDefaultState();
    }

}
