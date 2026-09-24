package com.hbm.blocks.fluid;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockSellafieldSlaked;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import java.util.Random;

public class RadBlock extends VolcanicBlock {

	public RadBlock(Fluid fluid, Material material, String s) {
		super(fluid, material, s);
	}

	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity){
		if(entity instanceof EntityLivingBase)
			ContaminationUtil.contaminate((EntityLivingBase) entity, HazardType.RADIATION, ContaminationType.CREATIVE, 5F);
	}

	@Override
	public Block getBasaltForCheck() {
		return ModBlocks.sellafield_slaked;
	}

	@Override
	public void onSolidify(World world, BlockPos pos, int lavaCount, int basaltCount, Random rand) {
		int r = rand.nextInt(400);

		Block above = world.getBlockState(pos.up(10)).getBlock();
		boolean canMakeGem = lavaCount + basaltCount == 6 && lavaCount < 3 && (above == ModBlocks.sellafield_slaked || above == ModBlocks.rad_lava_block);
		int shade = 5 + rand.nextInt(3);

		Block target;
		if(r < 2) target = ModBlocks.ore_sellafield_diamond;
		else if(r == 2) target = ModBlocks.ore_sellafield_emerald;
		else if(r < 20 && canMakeGem) target = ModBlocks.ore_sellafield_radgem;
		else target = ModBlocks.sellafield_slaked;

		world.setBlockState(pos, target.getDefaultState().withProperty(BlockSellafieldSlaked.SHADE, shade), 3);
	}

	@Override
	public IBlockState getReaction(World world, int x, int y, int z) {
		IBlockState state = world.getBlockState(new BlockPos(x, y, z));
		Block b = state.getBlock();
		if(state.getMaterial() == Material.WATER) return Blocks.STONE.getDefaultState();
		if(b instanceof BlockLog) return ModBlocks.waste_log.getDefaultState();
		if(b == Blocks.PLANKS) return ModBlocks.waste_planks.getDefaultState();
		if(b instanceof BlockLeaves) return Blocks.FIRE.getDefaultState();
		if(b == Blocks.DIAMOND_ORE) return ModBlocks.ore_sellafield_radgem.getDefaultState();
		if(b == ModBlocks.ore_uranium || b == ModBlocks.ore_gneiss_uranium) {
			return world.rand.nextInt(5) == 0
					? ModBlocks.ore_sellafield_schrabidium.getDefaultState()
					: ModBlocks.ore_sellafield_uranium_scorched.getDefaultState();
		}
		return null;
	}
}
