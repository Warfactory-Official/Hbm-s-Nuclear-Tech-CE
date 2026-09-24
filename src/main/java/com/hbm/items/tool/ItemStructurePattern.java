package com.hbm.items.tool;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemStructurePattern extends ItemStructureTool {

	public ItemStructurePattern(String s) {
		super(s);
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> list, @NotNull ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);
		list.add(TextFormatting.YELLOW + "Click to print all <setBlockState>");
		list.add(TextFormatting.YELLOW + "lines for the current selection with blocks and metadata.");
	}

	@Override
	protected boolean dualUse() {
		return true;
	}

	@Override
	protected void doTheThing(ItemStack stack, World world, BlockPos pos) {

		BlockPos anchor = getAnchor(stack);
		if(anchor == null) return;

		NBTTagCompound nbt = stack.getTagCompound();
		if(nbt == null) return;

		StringBuilder message = new StringBuilder();
		int savedX = nbt.getInteger("x");
		int savedY = nbt.getInteger("y");
		int savedZ = nbt.getInteger("z");

		int minX = Math.min(savedX, pos.getX()) - anchor.getX();
		int minY = Math.min(savedY, pos.getY()) - anchor.getY();
		int minZ = Math.min(savedZ, pos.getZ()) - anchor.getZ();
		int maxX = Math.max(savedX, pos.getX()) - anchor.getX();
		int maxY = Math.max(savedY, pos.getY()) - anchor.getY();
		int maxZ = Math.max(savedZ, pos.getZ()) - anchor.getZ();

		BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos();

		for(int ix = minX; ix <= maxX; ix++) {
			for(int iy = minY; iy <= maxY; iy++) {
				for(int iz = minZ; iz <= maxZ; iz++) {

					scan.setPos(ix + anchor.getX(), iy + anchor.getY(), iz + anchor.getZ());
					IBlockState state = world.getBlockState(scan);
					if(state.getBlock().isAir(state, world, scan)) continue;

					message.append("setBlockState(world, ").append(describe(state)).append(", ").append(ix).append(", ").append(iy).append(", ").append(iz).append(", box);\n");
				}
			}
		}

		System.out.print(message);
		writeToFile(message.toString());
	}
}
