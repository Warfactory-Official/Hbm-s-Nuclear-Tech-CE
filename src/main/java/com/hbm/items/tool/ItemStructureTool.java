package com.hbm.items.tool;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.items.ItemBase;
import com.hbm.main.MainRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ItemStructureTool extends ItemBase implements ILookOverlay {

	File file = new File(MainRegistry.configHbmDir, "structureOutput.txt");
	FileWriter writer;

	public ItemStructureTool(String s) {
		super(s);
	}

	public void writeToFile(String message) {
		if(!GeneralConfig.enableDebugMode)
			return;

		try {
			if(!file.exists()) file.createNewFile();
			if(writer == null) writer = new FileWriter(file, true);

			writer.write(message);
			writer.flush();
		} catch(IOException e) {
			MainRegistry.logger.warn("ItemStructureWand encountered an IOException!", e);
		}
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> list, @NotNull ITooltipFlag flag) {
		BlockPos anchor = getAnchor(stack);

		if(anchor == null)
			list.add(TextFormatting.RED + "No anchor set! Right click an anchor to get started.");

		if(GeneralConfig.enableDebugMode)
			list.add(TextFormatting.GREEN + "Will write to \"structureOutput.txt\" in hbmConfig.");
	}

	public static BlockPos getAnchor(ItemStack stack) {

		NBTTagCompound nbt = stack.getTagCompound();
		if(nbt == null || !nbt.hasKey("anchorX")) {
			return null;
		}

		return new BlockPos(nbt.getInteger("anchorX"), nbt.getInteger("anchorY"), nbt.getInteger("anchorZ"));
	}

	public static void setAnchor(ItemStack stack, BlockPos pos) {

		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}

		NBTTagCompound nbt = stack.getTagCompound();
		nbt.setInteger("anchorX", pos.getX());
		nbt.setInteger("anchorY", pos.getY());
		nbt.setInteger("anchorZ", pos.getZ());
	}

	@Override
	public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

		ItemStack stack = player.getHeldItem(hand);

		if(world.getBlockState(pos).getBlock() == ModBlocks.structure_anchor) {
			setAnchor(stack, pos);
			return EnumActionResult.SUCCESS;
		}

		if(getAnchor(stack) == null) {
			return EnumActionResult.PASS;
		}

		NBTTagCompound nbt = stack.getTagCompound();

		if(!this.dualUse() && world.isRemote) {
			this.doTheThing(stack, world, pos);
		} else {

			if(!nbt.hasKey("x")) {
				nbt.setInteger("x", pos.getX());
				nbt.setInteger("y", pos.getY());
				nbt.setInteger("z", pos.getZ());
			} else {
				if(world.isRemote)
					this.doTheThing(stack, world, pos);
				nbt.removeTag("x");
				nbt.removeTag("y");
				nbt.removeTag("z");
			}
		}

		return EnumActionResult.SUCCESS;
	}

	protected boolean dualUse() {
		return false;
	}

	protected abstract void doTheThing(ItemStack stack, World world, BlockPos pos);

	protected static String describe(IBlockState state) {
		return state.getBlock().getTranslationKey() + ".getStateFromMeta(" + state.getBlock().getMetaFromState(state) + ")";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(Pre event, World world, BlockPos pos) {
		Minecraft mc = Minecraft.getMinecraft();
		ItemStack stack = mc.player.getHeldItemMainhand();
		List<String> text = new ArrayList<>();

		BlockPos anchor = getAnchor(stack);

		if(anchor == null) {
			text.add(TextFormatting.RED + "No Anchor");
		} else {

			int dX = pos.getX() - anchor.getX();
			int dY = pos.getY() - anchor.getY();
			int dZ = pos.getZ() - anchor.getZ();
			text.add(TextFormatting.YELLOW + "Position: " + dX + " / " + dY + " / " + dZ);

			NBTTagCompound nbt = stack.getTagCompound();

			if(this.dualUse() && nbt != null && nbt.hasKey("x")) {
				int sX = Math.abs(pos.getX() - nbt.getInteger("x")) + 1;
				int sY = Math.abs(pos.getY() - nbt.getInteger("y")) + 1;
				int sZ = Math.abs(pos.getZ() - nbt.getInteger("z")) + 1;
				text.add(TextFormatting.GOLD + "Selection: " + sX + " / " + sY + " / " + sZ);
			}
		}

		if(mc.player.isSneaking()) {
			IBlockState state = world.getBlockState(pos);
			text.add("B: " + state.getBlock().getTranslationKey() + ", M: " + state.getBlock().getMetaFromState(state));
		}

		ILookOverlay.printGeneric(event, this.getItemStackDisplayName(stack), 0xffff00, 0x404000, text);
	}
}
