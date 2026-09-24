package com.hbm.util;

import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dynamic Trees keeps a tree as a connected network of branch blocks anchored to a rooty block, so carving part of
 * one out with a raw {@code setBlockToAir} leaves the remainder floating and unrooted. Dynamic Trees cannot detect
 * that from its side, so explosions and fallout have to tell it.
 *
 * <p>Calls go through reflection because Dynamic Trees is an optional runtime dependency.</p>
 */
public class CompatDynamicTrees {

	private static final boolean LOADED = Loader.isModLoaded(Compat.ModIds.DYNAMIC_TREES);

	private static Class<?> branchClass;
	private static Class<?> rootyClass;
	private static Method destroyBranchFromNode;
	private static Method destroyTree;
	private static boolean resolved;
	private static boolean available;

	private static boolean resolve() {
		if(resolved) return available;
		resolved = true;

		if(!LOADED) return false;

		try {
			branchClass = Class.forName("com.ferreusveritas.dynamictrees.blocks.BlockBranch");
			destroyBranchFromNode = branchClass.getMethod("destroyBranchFromNode", World.class, BlockPos.class, EnumFacing.class, boolean.class);
			rootyClass = Class.forName("com.ferreusveritas.dynamictrees.blocks.BlockRooty");
			destroyTree = rootyClass.getMethod("destroyTree", World.class, BlockPos.class);
			available = true;
		} catch(ReflectiveOperationException e) {
			MainRegistry.logger.warn("Dynamic Trees is loaded but its tree API could not be resolved, explosions will leave trees floating", e);
		}

		return available;
	}

	public static boolean isBranch(Block block) {
		return resolve() && branchClass.isInstance(block);
	}

	/** True for both branches and the rooty block that anchors them, i.e. anything whose removal orphans a tree. */
	public static boolean isTreePart(Block block) {
		return resolve() && (branchClass.isInstance(block) || rootyClass.isInstance(block));
	}

	/**
	 * Removes the tree reachable from pos, whether pos holds a branch or the rooty block anchoring it. Unlike
	 * Dynamic Trees' own explosion handler this spawns no falling tree entity and drops no logs, matching how NTM
	 * explosions treat every other block.
	 *
	 * @return true if a tree was found and removed
	 */
	public static boolean destroyTreeAt(World world, BlockPos pos) {
		if(!resolve()) return false;

		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		boolean branch = branchClass.isInstance(block);
		if(!branch && !rootyClass.isInstance(block)) return false;

		try {
			if(branch) {
				destroyBranchFromNode.invoke(block, world, pos.toImmutable(), EnumFacing.DOWN, false);
			} else {
				destroyTree.invoke(block, world, pos.toImmutable());
			}
			return true;
		} catch(ReflectiveOperationException | RuntimeException e) {
			MainRegistry.logger.error("Dynamic Trees tree removal failed at {}", pos, e);
			return false;
		}
	}

	/**
	 * Cleans up whatever was left behind after the block at pos was carved out. Only the surviving neighbours still
	 * hold tree parts, so this is what reaches the part of the tree outside the blast.
	 */
	public static void destroyOrphanedNeighbors(World world, BlockPos pos) {
		if(!resolve()) return;

		for(EnumFacing facing : EnumFacing.VALUES) {
			BlockPos neighbor = pos.offset(facing);
			if(!world.isBlockLoaded(neighbor)) continue;
			destroyTreeAt(world, neighbor);
		}
	}

	/**
	 * Every registered branch block, including those added by Dynamic Trees addons. Only valid once blocks are
	 * registered, so call this no earlier than load completion.
	 */
	public static List<Block> getBranchBlocks() {
		if(!resolve()) return Collections.emptyList();

		List<Block> branches = new ArrayList<>();
		for(Block block : Block.REGISTRY) {
			if(branchClass.isInstance(block)) branches.add(block);
		}
		return branches;
	}
}
