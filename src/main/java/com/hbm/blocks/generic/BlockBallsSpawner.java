package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.mob.botprime.EntityBOTPrimeHead;
import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBallsSpawner extends Block {

	public BlockBallsSpawner(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == ModItems.mech_key) {
			player.getHeldItem(hand).shrink(1);

			if(!world.isRemote) {

				EntityBOTPrimeHead bot = new EntityBOTPrimeHead(world);
				bot.setPositionAndRotation(pos.getX() + 0.5, 300, pos.getZ() + 0.5, 0, 0);
				bot.motionY = -1.0;
				bot.onInitialSpawn(world.getDifficultyForLocation(pos), null);
				world.spawnEntity(bot);

				world.setBlockState(pos, ModBlocks.brick_jungle_cracked.getDefaultState());
			}
		}

		return false;
	}

}
