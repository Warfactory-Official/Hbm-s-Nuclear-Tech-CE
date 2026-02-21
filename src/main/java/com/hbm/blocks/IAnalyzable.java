package com.hbm.blocks;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
// TODO port other fluid network blocks
public interface IAnalyzable {
    public List<String> getDebugInfo(World world, BlockPos pos);
}
