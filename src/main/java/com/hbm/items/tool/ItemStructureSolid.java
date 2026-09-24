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

public class ItemStructureSolid extends ItemStructureTool {

	public ItemStructureSolid(String s) {
		super(s);
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> list, @NotNull ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);
		list.add(TextFormatting.YELLOW + "Click to print a <fillWithMetadataBlocks> or <fillWithBlocks>");
		list.add(TextFormatting.YELLOW + "line with wildcard block and metadata.");
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

		int savedX = nbt.getInteger("x");
		int savedY = nbt.getInteger("y");
		int savedZ = nbt.getInteger("z");

		int minX = Math.min(savedX, pos.getX()) - anchor.getX();
		int minY = Math.min(savedY, pos.getY()) - anchor.getY();
		int minZ = Math.min(savedZ, pos.getZ()) - anchor.getZ();
		int maxX = Math.max(savedX, pos.getX()) - anchor.getX();
		int maxY = Math.max(savedY, pos.getY()) - anchor.getY();
		int maxZ = Math.max(savedZ, pos.getZ()) - anchor.getZ();

		IBlockState state = world.getBlockState(pos);
		int meta = state.getBlock().getMetaFromState(state);

		String line;
		if(meta > 0)
			line = "fillWithMetadataBlocks(world, box, " + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + ", " + state.getBlock().getTranslationKey() + ", " + meta + ");\n";
		else
			line = "fillWithBlocks(world, box, " + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + ", " + state.getBlock().getTranslationKey() + ");\n";

		System.out.print(line);
		writeToFile(line);
	}
}
