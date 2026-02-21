package com.hbm.interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRadResistantBlock {

	//Anything implementing this must override onBlockAdded and breakBlock and call RadiationSystemNT.markChunkForRebuild or it won't work

    /**
     * @implNote must not carry side effects
     */
	default boolean isRadResistant(World worldIn, BlockPos blockPos){
		return true;
	}
}
