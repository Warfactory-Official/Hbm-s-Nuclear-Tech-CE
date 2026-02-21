package com.hbm.api.block;

import com.hbm.inventory.RecipesCommon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IToolable {
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool);
	default boolean onScrew(World world, EntityPlayer player, BlockPos pos, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool){
		return onScrew(world, player, pos.getX(), pos.getY(), pos.getZ(), side, fX, fY, fZ, hand, tool);
	};

	enum ToolType {
		SCREWDRIVER,
		HAND_DRILL,
		DEFUSER,
		WRENCH,
		TORCH,
		BOLT;

        public static final ToolType[] VALUES = values();

		public List<ItemStack> stacksForDisplay = new ArrayList();
		private static HashMap<RecipesCommon.ComparableStack, ToolType> map = new HashMap();

		public void register(ItemStack stack) {
			stacksForDisplay.add(stack);
		}

		public static ToolType getType(ItemStack stack) {

			if(!map.isEmpty()) {
				return map.get(new RecipesCommon.ComparableStack(stack));
			}

			for(ToolType type : ToolType.VALUES) {
				for(ItemStack tool : type.stacksForDisplay) {
					map.put(new RecipesCommon.ComparableStack(tool), type);
				}
			}

			return map.get(new RecipesCommon.ComparableStack(stack));
		}
	}
}
