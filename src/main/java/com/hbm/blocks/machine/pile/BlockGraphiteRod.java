package com.hbm.blocks.machine.pile;

import com.hbm.Tags;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.items.ModItems;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGraphiteRod extends BlockGraphiteDrilledBase implements IToolable {
	
	public BlockGraphiteRod(String s){
		super(s);
		this.blockFrames = new BlockBakeFrame[16];
		for (int meta = 0; meta < 16; meta++) {
			boolean isAluminum = (meta & 4) != 0;
			boolean isOut = (meta & 8) != 0;
			String front;
			if (isAluminum) {
				front = isOut ? "block_graphite_rod_out_aluminum" : "block_graphite_rod_in_aluminum";
			} else {
				front = isOut ? "block_graphite_rod_out" : "block_graphite_rod_in";
			}
			this.blockFrames[meta] = new BlockBakeFrame(front, "block_graphite", front);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerSprite(TextureMap map) {
		super.registerSprite(map);
		map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/" + "block_graphite_rod_out_aluminum"));
		map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/" + "block_graphite_rod_in_aluminum"));
		map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/" + "block_graphite_rod_out"));
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking())
			return false;

		int oldMeta = getMetaFromState(state);
		int newMeta = oldMeta ^ 8; // toggle bit #4
		int pureMeta = oldMeta & 3;

		int sideIdx = side.getIndex();
		if (sideIdx == pureMeta * 2 || sideIdx == pureMeta * 2 + 1) {
			if (world.isRemote)
				return true;

			world.setBlockState(pos, state.withProperty(BlockMeta.META, newMeta), 3);

			world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
					SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F,
					pureMeta == (oldMeta & 11) ? 0.75F : 0.65F);

			for (int i = -1; i <= 1; i++) {
				BlockPos iPos = pos.offset(side, i);
				while (world.getBlockState(iPos).getBlock() == this &&
						getMetaFromState(world.getBlockState(iPos)) == oldMeta) {
					world.setBlockState(iPos, world.getBlockState(iPos).withProperty(BlockMeta.META, newMeta), 3);
					iPos = iPos.offset(side, i);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	protected ItemStack getInsertedItem() {
		return new ItemStack(ModItems.pile_rod_boron);
	}
}