package com.hbm.world;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsLegacy;
import com.hbm.lib.Library;
import com.hbm.world.phased.AbstractPhasedStructure;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static com.hbm.blocks.generic.BlockMeta.META;

public class Barrel extends AbstractPhasedStructure {
	public static final Barrel INSTANCE = new Barrel();
	private Barrel() {}

    @Override
    protected boolean isValidSpawnBlock(Block block) {
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.STONE || block == Blocks.SAND || block == Blocks.SANDSTONE;
    }

	@Override
	public boolean checkSpawningConditions(@NotNull World world, long pos) {
		return locationIsValidSpawn(world, pos) &&
				locationIsValidSpawn(world, Library.shiftBlockPos(pos, 4, 0, 0)) &&
				locationIsValidSpawn(world, Library.shiftBlockPos(pos, 4, 0, 6)) &&
				locationIsValidSpawn(world, Library.shiftBlockPos(pos, 0, 0, 6));
	}

    @Override
    protected boolean isCacheable() {
        return false;
    }

	@Override
	public @NotNull LongArrayList getHeightPoints(long origin) {
		int ox = Library.getBlockPosX(origin);
		int oy = Library.getBlockPosY(origin);
		int oz = Library.getBlockPosZ(origin);
		LongArrayList points = new LongArrayList(4);
		points.add(origin);
		points.add(Library.blockPosToLong(ox + 4, oy, oz));
		points.add(Library.blockPosToLong(ox + 4, oy, oz + 6));
		points.add(Library.blockPosToLong(ox, oy, oz + 6));
		return points;
	}

	@Override
	public void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
		generate_r0(builder, rand, 0, 0, 0);
	}
	
	Block Block1 = ModBlocks.reinforced_brick;
	Block Block2 = ModBlocks.sellafield_slaked;
	Block Block3 = ModBlocks.brick_concrete;
	IBlockState Block4 = ModBlocks.sellafield.getDefaultState().withProperty(META, 3);
	IBlockState Block5 = ModBlocks.sellafield.getDefaultState().withProperty(META, 4);
	IBlockState Block6 = ModBlocks.sellafield.getDefaultState().withProperty(META, 5);
	IBlockState Block7 = ModBlocks.sellafield.getDefaultState().withProperty(META, 2);
	IBlockState Block8 = ModBlocks.sellafield.getDefaultState().withProperty(META, 1);
	IBlockState Block9 = ModBlocks.sellafield.getDefaultState().withProperty(META, 0);
	Block Block10 = ModBlocks.deco_lead;
	Block Block11 = ModBlocks.reinforced_glass;
	Block Block12 = ModBlocks.toxic_block;

	public boolean generate_r0(LegacyBuilder world, Random rand, int x, int y, int z) {
		MutableBlockPos pos = this.mutablePos;

		world.setBlockState(pos.setPos(x + 1, y + -1, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + -1, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + -1, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + -1, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + -1, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + -1, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + -1, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + -1, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + -1, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + -1, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + -1, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + -1, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + -1, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + -1, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + -1, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + -1, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + -1, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + -1, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + -1, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + -1, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + -1, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + -1, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + -1, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + -1, z + 5), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + -1, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + -1, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + -1, z + 6), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 0, z + 0), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 0, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 0, z + 0), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 0, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 0, z + 1), Block4, 3);
		world.setBlockState(pos.setPos(x + 2, y + 0, z + 1), Block5, 3);
		world.setBlockState(pos.setPos(x + 3, y + 0, z + 1), Block4, 3);
		world.setBlockState(pos.setPos(x + 4, y + 0, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 0, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 0, z + 2), Block5, 3);
		world.setBlockState(pos.setPos(x + 2, y + 0, z + 2), Block6, 3);
		
		//Drillgon200: This tile entity is never actually used. Possibly a bug?
		/*if(world.getTileEntity(x + 2, y + 0, z + 2) instanceof TileEntitySellafield) {
			((TileEntitySellafield)world.getTileEntity(x + 2, y + 0, z + 2)).radius = 2.5;
		}*/
		
		world.setBlockState(pos.setPos(x + 3, y + 0, z + 2), Block5, 3);
		world.setBlockState(pos.setPos(x + 4, y + 0, z + 2), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 0, z + 3), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 0, z + 3), Block5, 3);
		world.setBlockState(pos.setPos(x + 2, y + 0, z + 3), Block4, 3);
		world.setBlockState(pos.setPos(x + 3, y + 0, z + 3), Block5, 3);
		world.setBlockState(pos.setPos(x + 4, y + 0, z + 3), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 0, z + 4), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 0, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 0, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 1, z + 0), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 1, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 1, z + 0), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 1, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 1, z + 1), Block7, 3);
		world.setBlockState(pos.setPos(x + 2, y + 1, z + 1), Block4, 3);
		world.setBlockState(pos.setPos(x + 3, y + 1, z + 1), Block4, 3);
		world.setBlockState(pos.setPos(x + 4, y + 1, z + 1), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 1, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 1, z + 2), Block4, 3);
		
		/*world.setBlock(x + 2, y + 1, z + 2, Blocks.chest, 3, 3);

		if(world.getBlock(x + 2, y + 1, z + 2) == Blocks.chest)
		{
			WeightedRandomChestContent.generateChestContents(rand, HbmChestContents.getLoot(3), (TileEntityChest)world.getTileEntity(x + 2, y + 1, z + 2), 16);
		}*/

		world.setBlockState(pos.setPos(x + 2, y + 1, z + 2), ModBlocks.crate_steel.getDefaultState(),
				((worldIn, random, blockPos, chest) ->
						WeightedRandomChestContentFrom1710.generateChestContents(random, ItemPool.getPool(ItemPoolsLegacy.POOL_EXPENSIVE), chest, 16)));
		world.setBlockState(pos.setPos(x + 3, y + 1, z + 2), Block4, 3);
		world.setBlockState(pos.setPos(x + 4, y + 1, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 1, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 1, z + 3), Block4, 3);
		world.setBlockState(pos.setPos(x + 2, y + 1, z + 3), Block7, 3);
		world.setBlockState(pos.setPos(x + 3, y + 1, z + 3), Block4, 3);
		world.setBlockState(pos.setPos(x + 4, y + 1, z + 3), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 1, z + 4), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 1, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 1, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 2, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 0), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 2, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 2, z + 1), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 2, z + 1), Block8, 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 1), Block7, 3);
		world.setBlockState(pos.setPos(x + 3, y + 2, z + 1), Block7, 3);
		world.setBlockState(pos.setPos(x + 4, y + 2, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 2, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 2, z + 2), Block7, 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 2), Block5, 3);
		world.setBlockState(pos.setPos(x + 3, y + 2, z + 2), Block7, 3);
		world.setBlockState(pos.setPos(x + 4, y + 2, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 2, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 2, z + 3), Block7, 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 3), Block8, 3);
		world.setBlockState(pos.setPos(x + 3, y + 2, z + 3), Block7, 3);
		world.setBlockState(pos.setPos(x + 4, y + 2, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 2, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 2, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 3, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 3, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 3, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 3, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 3, z + 1), Block8, 3);
		world.setBlockState(pos.setPos(x + 2, y + 3, z + 1), Block8, 3);
		world.setBlockState(pos.setPos(x + 3, y + 3, z + 1), Block8, 3);
		world.setBlockState(pos.setPos(x + 4, y + 3, z + 1), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 3, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 3, z + 2), Block8, 3);
		world.setBlockState(pos.setPos(x + 2, y + 3, z + 2), Block4, 3);
		world.setBlockState(pos.setPos(x + 3, y + 3, z + 2), Block8, 3);
		world.setBlockState(pos.setPos(x + 4, y + 3, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 3, z + 3), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 3, z + 3), Block8, 3);
		world.setBlockState(pos.setPos(x + 2, y + 3, z + 3), Block9, 3);
		world.setBlockState(pos.setPos(x + 3, y + 3, z + 3), Block8, 3);
		world.setBlockState(pos.setPos(x + 4, y + 3, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 3, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 3, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 3, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 4, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 4, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 4, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 4, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 4, z + 1), Block9, 3);
		world.setBlockState(pos.setPos(x + 2, y + 4, z + 1), Block8, 3);
		world.setBlockState(pos.setPos(x + 3, y + 4, z + 1), Block9, 3);
		world.setBlockState(pos.setPos(x + 4, y + 4, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 4, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 4, z + 2), Block9, 3);
		world.setBlockState(pos.setPos(x + 2, y + 4, z + 2), Block7, 3);
		world.setBlockState(pos.setPos(x + 3, y + 4, z + 2), Block8, 3);
		world.setBlockState(pos.setPos(x + 4, y + 4, z + 2), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 4, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 4, z + 3), Block8, 3);
		world.setBlockState(pos.setPos(x + 2, y + 4, z + 3), Block9, 3);
		world.setBlockState(pos.setPos(x + 3, y + 4, z + 3), Block9, 3);
		world.setBlockState(pos.setPos(x + 4, y + 4, z + 3), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 4, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 4, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 4, z + 4), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 5, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 5, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 1), Block9, 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 1), Block9, 3);
		world.setBlockState(pos.setPos(x + 3, y + 5, z + 1), Block9, 3);
		world.setBlockState(pos.setPos(x + 4, y + 5, z + 1), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 5, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 2), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 2), Block8, 3);
		world.setBlockState(pos.setPos(x + 3, y + 5, z + 2), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 5, z + 2), Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 5, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 3), Block9, 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 3), Block9, 3);
		world.setBlockState(pos.setPos(x + 3, y + 5, z + 3), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 5, z + 3), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 5, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 5, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 0), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 0), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6, z + 0), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 6, z + 1), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 1), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 1), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6, z + 1), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6, z + 1), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 6, z + 2), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 2), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 2), Block9, 3);
		world.setBlockState(pos.setPos(x + 3, y + 6, z + 2), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6, z + 2), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 6, z + 3), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 3), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 3), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6, z + 3), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 6, z + 3), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 6, z + 4), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 4), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 6, z + 4), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 7, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 1), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 1), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7, z + 1), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7, z + 1), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 7, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 2), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 2), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7, z + 2), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7, z + 2), Block2.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 7, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 3), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 3), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7, z + 3), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 7, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 7, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 7, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 0), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 0), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8, z + 0), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 8, z + 1), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 1), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 1), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8, z + 1), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8, z + 1), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 8, z + 2), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 2), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 2), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8, z + 2), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8, z + 2), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 8, z + 3), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 3), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 3), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8, z + 3), Block12.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 8, z + 3), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 8, z + 4), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 4), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 8, z + 4), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 9, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 9, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 9, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 9, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 9, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 9, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 9, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 9, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 9, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 9, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 9, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 9, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 10, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 10, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 10, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 10, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 10, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 10, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 10, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 10, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 10, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 10, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		//world.setBlock(x + 2, y + 10, z + 4, Blocks.iron_door, 2, 3);
		world.setBlockState(pos.setPos(x + 3, y + 10, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 11, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 11, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 11, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 11, z + 1), Block11.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 11, z + 2), Block11.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 11, z + 2), Block11.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 11, z + 3), Block11.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 11, z + 3), Block11.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 11, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		//world.setBlock(x + 2, y + 11, z + 4, Blocks.iron_door, 8, 3);
        world.placeDoorWithoutCheck(pos.setPos(x + 2, y + 10, z + 4), EnumFacing.WEST, Blocks.IRON_DOOR, false);
		world.setBlockState(pos.setPos(x + 3, y + 11, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 12, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 12, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 12, z + 0), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 12, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 12, z + 1), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 12, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 12, z + 2), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 12, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 12, z + 3), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 12, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 12, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 12, z + 4), Library.getRandomConcrete(rand).getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 13, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 13, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 13, z + 0), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 13, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 13, z + 1), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 13, z + 1), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 13, z + 1), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 13, z + 1), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 13, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 13, z + 2), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 13, z + 2), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 4, y + 13, z + 2), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 0, y + 13, z + 3), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 13, z + 3), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 13, z + 3), Block10.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 1, y + 13, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 2, y + 13, z + 4), Block1.getDefaultState(), 3);
		world.setBlockState(pos.setPos(x + 3, y + 13, z + 4), Block1.getDefaultState(), 3);

		generate_r02_last(world, rand, x, y, z, pos);
		return true;

	}

	public boolean generate_r02_last(LegacyBuilder world, Random rand, int x, int y, int z, MutableBlockPos pos) {

		world.setBlockState(pos.setPos(x + 2, y + 0, z + 5), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 2, y + 1, z + 5), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 2, y + 2, z + 5), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 2, y + 3, z + 5), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 2, y + 4, z + 5), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 2, y + 5, z + 5), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 2, y + 6, z + 5), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 2, y + 7, z + 5), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 2, y + 8, z + 5), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), 3);
		world.setBlockState(pos.setPos(x + 2, y + 9, z + 5), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH), 3);
		return true;

	}

}
