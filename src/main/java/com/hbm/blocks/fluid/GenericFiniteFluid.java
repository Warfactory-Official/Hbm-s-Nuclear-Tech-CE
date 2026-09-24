package com.hbm.blocks.fluid;

import com.hbm.blocks.ModBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import org.jetbrains.annotations.NotNull;

public class GenericFiniteFluid extends BlockFluidFinite {

	public GenericFiniteFluid(Fluid fluid, Material material, String s) {
		super(fluid, material);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(null);
		displacements.put(this, false);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public @NotNull IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
													 EntityLivingBase placer, EnumHand hand) {
		return this.getDefaultState().withProperty(LEVEL, Math.max(0, this.quantaPerBlock - 1));
	}
}
