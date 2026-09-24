package com.hbm.blocks.generic;

import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockStorageCrateRadResistant extends BlockStorageCrate implements IRadResistantBlock {

	public BlockStorageCrateRadResistant(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		RadiationSystemNT.markSectionForRebuild(worldIn, pos);
		super.onBlockAdded(worldIn, pos, state);
	}

	@Override
	public void breakBlock(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
		RadiationSystemNT.markSectionForRebuild(worldIn, pos);
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, World player, @NotNull List<String> tooltip, @NotNull ITooltipFlag advanced) {
		super.addInformation(stack, player, tooltip, advanced);
		tooltip.add("§2[" + I18nUtil.resolveKey("trait.radshield") + "]");
		float hardness = this.getExplosionResistance(null);
		if(hardness > 50){
			tooltip.add("§6" + I18nUtil.resolveKey("trait.blastres", hardness));
		}
	}
}
