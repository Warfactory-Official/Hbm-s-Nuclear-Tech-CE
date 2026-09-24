package com.hbm.items.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockPWR;
import com.hbm.blocks.machine.BlockPWR.TileEntityBlockPWR;
import com.hbm.blocks.machine.MachinePWRController;
import com.hbm.inventory.gui.GUIScreenSlicePrinter;
import com.hbm.items.ItemBase;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.TileEntityPWRController;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

public class ItemPWRPrinter extends ItemBase implements IGUIProvider {

	private static int x1, y1, z1;
	private static int x2, y2, z2;
	private static IBlockState[] stateSync;
	private static EnumFacing dir;

	private final HashSet<BlockPos> fill = new HashSet<>();
	private static final HashSet<Block> whitelist = new HashSet<Block>() {{
		add(ModBlocks.pwr_block);
		add(ModBlocks.pwr_controller);
	}};

	public ItemPWRPrinter(String s) {
		super(s);
	}

	public static void serialize(World world, ByteBuf buf) {
		buf.writeInt(x1);
		buf.writeInt(y1);
		buf.writeInt(z1);
		buf.writeInt(x2);
		buf.writeInt(y2);
		buf.writeInt(z2);
		buf.writeInt(dir.getIndex());

		for(IBlockState state : stateSync) {
			buf.writeInt(state == null ? 0 : Block.getStateId(state));
		}

		stateSync = null;
	}

	@SideOnly(Side.CLIENT)
	public static void deserialize(World world, ByteBuf buf) {
		x1 = buf.readInt();
		y1 = buf.readInt();
		z1 = buf.readInt();
		x2 = buf.readInt();
		y2 = buf.readInt();
		z2 = buf.readInt();
		dir = EnumFacing.byIndex(buf.readInt());

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for(int x = x1; x <= x2; x++) {
			for(int y = y1; y <= y2; y++) {
				for(int z = z1; z <= z2; z++) {
					IBlockState state = Block.getStateById(buf.readInt());

					TileEntity tile = world.getTileEntity(pos.setPos(x, y, z));
					if(!(tile instanceof TileEntityBlockPWR)) continue;
					((TileEntityBlockPWR) tile).originalBlockState = state;
				}
			}
		}

		EntityPlayer player = Minecraft.getMinecraft().player;
		if(player != null && player.getHeldItemMainhand().getItem() instanceof ItemPWRPrinter) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, 0, 0, 0);
		}
	}

	@Override
	public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tile = world.getTileEntity(pos);
		if(!(tile instanceof TileEntityPWRController)) return EnumActionResult.PASS;
		if(world.isRemote) return EnumActionResult.SUCCESS;

		syncAndScreenshot(world, (TileEntityPWRController) tile);

		return EnumActionResult.SUCCESS;
	}

	public void syncAndScreenshot(World world, TileEntityPWRController pwr) {
		findBounds(world, pwr);

		int sizeX = x2 - x1 + 1;
		int sizeY = y2 - y1 + 1;
		int sizeZ = z2 - z1 + 1;

		stateSync = new IBlockState[sizeX * sizeY * sizeZ];
		int i = 0;

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for(int x = x1; x <= x2; x++) {
			for(int y = y1; y <= y2; y++) {
				for(int z = z1; z <= z2; z++) {
					TileEntity tile = world.getTileEntity(pos.setPos(x, y, z));
					if(tile instanceof TileEntityBlockPWR) {
						stateSync[i] = ((TileEntityBlockPWR) tile).originalBlockState;
					}
					i++;
				}
			}
		}

		pwr.isPrinting = true;
	}

	public void findBounds(World world, TileEntityPWRController pwr) {
		BlockPos core = pwr.getPos();
		dir = world.getBlockState(core).getValue(MachinePWRController.FACING).getOpposite();

		fill.clear();
		fill.add(core);
		x1 = x2 = core.getX();
		y1 = y2 = core.getY();
		z1 = z2 = core.getZ();
		floodFill(world, core.getX() + dir.getXOffset(), core.getY(), core.getZ() + dir.getZOffset());
	}

	public void floodFill(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		if(fill.contains(pos)) return;

		if(world.getBlockState(pos).getBlock() instanceof BlockPWR) {
			fill.add(pos);

			x1 = Math.min(x1, x);
			y1 = Math.min(y1, y);
			z1 = Math.min(z1, z);
			x2 = Math.max(x2, x);
			y2 = Math.max(y2, y);
			z2 = Math.max(z2, z);

			floodFill(world, x + 1, y, z);
			floodFill(world, x - 1, y, z);
			floodFill(world, x, y + 1, z);
			floodFill(world, x, y - 1, z);
			floodFill(world, x, y, z + 1);
			floodFill(world, x, y, z - 1);
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIScreenSlicePrinter(x1, y1, z1, x2, y2, z2, dir, whitelist);
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> list, @NotNull ITooltipFlag flag) {
		list.add("Use on a constructed PWR controller to generate construction diagrams");
	}
}
