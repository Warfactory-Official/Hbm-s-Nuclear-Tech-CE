package com.hbm.items.tool;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemStructureSingle extends ItemStructureTool {

	public ItemStructureSingle(String s) {
		super(s);
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> list, @NotNull ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);
		list.add(TextFormatting.YELLOW + "Click to print exactly one <setBlockState>");
		list.add(TextFormatting.YELLOW + "line with the targeted block and metadata");
	}

	@Override
	protected void doTheThing(ItemStack stack, World world, BlockPos pos) {

		BlockPos anchor = getAnchor(stack);
		if(anchor == null) return;

		int ix = pos.getX() - anchor.getX();
		int iy = pos.getY() - anchor.getY();
		int iz = pos.getZ() - anchor.getZ();

		IBlockState state = world.getBlockState(pos);

		String message = "setBlockState(world, " + describe(state) + ", " + ix + ", " + iy + ", " + iz + ", box);\n";
		System.out.print(message);
		writeToFile(message);
	}
}
