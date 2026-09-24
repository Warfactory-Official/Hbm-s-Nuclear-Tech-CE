package com.hbm.items.tool;

import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockCMAnchor;
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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemCMStructure extends ItemBase implements ILookOverlay {

	private static final File file = new File(MainRegistry.configHbmDir, "CMstructureOutput.txt");

	public ItemCMStructure(String s) {
		super(s);
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

	public static void writeToFile(File config, ItemStack stack, World world) {

		NBTTagCompound nbt = stack.getTagCompound();
		if(nbt == null) return;

		int anchorX = nbt.getInteger("anchorX");
		int anchorY = nbt.getInteger("anchorY");
		int anchorZ = nbt.getInteger("anchorZ");
		int x1 = nbt.getInteger("x1");
		int y1 = nbt.getInteger("y1");
		int z1 = nbt.getInteger("z1");
		int x2 = nbt.getInteger("x2");
		int y2 = nbt.getInteger("y2");
		int z2 = nbt.getInteger("z2");

		BlockPos anchor = new BlockPos(anchorX, anchorY, anchorZ);
		IBlockState anchorState = world.getBlockState(anchor);
		EnumFacing dir = anchorState.getBlock() instanceof BlockCMAnchor ? anchorState.getValue(BlockCMAnchor.FACING) : EnumFacing.NORTH;

		int minX = Math.min(x1, x2);
		int maxX = Math.max(x1, x2);
		int minY = Math.min(y1, y2);
		int maxY = Math.max(y1, y2);
		int minZ = Math.min(z1, z2);
		int maxZ = Math.max(z1, z2);

		try {
			JsonWriter writer = new JsonWriter(new FileWriter(config));
			writer.setIndent("  ");
			writer.beginObject();
			writer.name("components").beginArray();

			BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos();

			for(int x = minX; x <= maxX; x++) {
				for(int y = minY; y <= maxY; y++) {
					for(int z = minZ; z <= maxZ; z++) {

						int compY = y - anchorY;
						int compX = 0;
						int compZ = 0;

						if(dir == EnumFacing.SOUTH) {
							compX = anchorX - x;
							compZ = anchorZ - z;
						}
						if(dir == EnumFacing.NORTH) {
							compX = x - anchorX;
							compZ = z - anchorZ;
						}

						if(dir == EnumFacing.WEST) {
							compZ = x - anchorX;
							compX = anchorZ - z;
						}
						if(dir == EnumFacing.EAST) {
							compZ = anchorX - x;
							compX = z - anchorZ;
						}

						if(x == anchorX && y == anchorY && z == anchorZ) continue;

						IBlockState state = world.getBlockState(scan.setPos(x, y, z));
						if(state.getBlock().isAir(state, world, scan)) continue;

						writer.beginObject().setIndent("");
						writer.name("block").value(String.valueOf(state.getBlock().getRegistryName()));
						writer.name("x").value(compX);
						writer.name("y").value(compY);
						writer.name("z").value(compZ);
						writer.name("metas").beginArray().value(state.getBlock().getMetaFromState(state)).endArray();
						writer.endObject().setIndent("  ");
					}
				}
			}
			writer.endArray();
			writer.endObject();
			writer.close();
		} catch(IOException e) {
			MainRegistry.logger.warn("ItemCMStructure encountered an IOException!", e);
		}
	}

	@Override
	public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

		ItemStack stack = player.getHeldItem(hand);

		if(world.getBlockState(pos).getBlock() == ModBlocks.cm_anchor) {
			setAnchor(stack, pos);
			return EnumActionResult.SUCCESS;
		}

		if(getAnchor(stack) == null) {
			return EnumActionResult.PASS;
		}

		NBTTagCompound nbt = stack.getTagCompound();

		if(!nbt.hasKey("x1")) {
			nbt.setInteger("x1", pos.getX());
			nbt.setInteger("y1", pos.getY());
			nbt.setInteger("z1", pos.getZ());
		} else if(!nbt.hasKey("x2")) {
			nbt.setInteger("x2", pos.getX());
			nbt.setInteger("y2", pos.getY());
			nbt.setInteger("z2", pos.getZ());
		} else {
			writeToFile(file, stack, world);
			nbt.removeTag("x1");
			nbt.removeTag("y1");
			nbt.removeTag("z1");
			nbt.removeTag("x2");
			nbt.removeTag("y2");
			nbt.removeTag("z2");
		}

		return EnumActionResult.SUCCESS;
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> list, @NotNull ITooltipFlag flag) {
		list.add(TextFormatting.YELLOW + "Click Custom Machine Structure Positioning Anchor to");
		list.add(TextFormatting.YELLOW + "Confirm the location of the custom machine core block.");
		list.add(TextFormatting.YELLOW + "Output all blocks between Position1 and Position2 with");
		list.add(TextFormatting.YELLOW + "metadata to \"CMstructureOutput.txt\" in hbmConfig.");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
		ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
		List<String> text = new ArrayList<>();

		BlockPos anchor = getAnchor(stack);

		if(anchor == null) {
			text.add(TextFormatting.RED + "No Anchor");
		} else {
			NBTTagCompound nbt = stack.getTagCompound();
			text.add(TextFormatting.GOLD + "Anchor: " + anchor.getX() + " / " + anchor.getY() + " / " + anchor.getZ());

			if(nbt.hasKey("x1")) {
				text.add(TextFormatting.YELLOW + "Position1: " + nbt.getInteger("x1") + " / " + nbt.getInteger("y1") + " / " + nbt.getInteger("z1"));
			}
			if(nbt.hasKey("x2")) {
				text.add(TextFormatting.YELLOW + "Position2: " + nbt.getInteger("x2") + " / " + nbt.getInteger("y2") + " / " + nbt.getInteger("z2"));
			}
		}

		ILookOverlay.printGeneric(event, this.getItemStackDisplayName(stack), 0xffff00, 0x404000, text);
	}
}
