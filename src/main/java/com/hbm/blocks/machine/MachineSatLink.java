package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.items.ISatChip;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineSatLink;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MachineSatLink extends BlockDummyable implements ILookOverlay {

	public MachineSatLink(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= 12) return new TileEntityMachineSatLink();
		if(meta >= 6) return new TileEntityProxyCombo();
		return null;
	}

	@Override public int[] getDimensions() { return new int[] {6, 0, 1, 0, 1, 0}; }
	@Override public int getOffset() { return 0; }

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		this.makeExtra(world, x - dir.offsetX, y, z - dir.offsetZ);
		this.makeExtra(world, x + rot.offsetX, y, z + rot.offsetZ);
		this.makeExtra(world, x - dir.offsetX + rot.offsetX, y, z - dir.offsetZ + rot.offsetZ);
	}

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(!world.isRemote && !player.isSneaking()) {

			ItemStack held = player.getHeldItem(hand);

			if(!held.isEmpty() && held.getItem() instanceof ISatChip) {

				int[] corePos = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
				if(corePos == null) return false;

				TileEntity te = world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));
				if(!(te instanceof TileEntityMachineSatLink)) return false;

				TileEntityMachineSatLink link = (TileEntityMachineSatLink) te;

				link.freq = ISatChip.getFreqS(held);
				player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Set frequency to " + link.freq));
				world.playSound(null, pos, HBMSoundHandler.techBleep, SoundCategory.BLOCKS, 1F, 1F);

				return true;
			}
			return false;

		} else {
			return true;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {

		int[] corePos = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
		if(corePos == null) return;

		TileEntity te = world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));
		if(!(te instanceof TileEntityMachineSatLink)) return;

		TileEntityMachineSatLink link = (TileEntityMachineSatLink) te;

		List<String> text = new ArrayList<>();
		text.add(I18nUtil.resolveKey("tile.machine_satlink.freq") + ": " + link.freq);
		text.add(I18nUtil.resolveKey("tile.machine_satlink.connected") + ": " + (link.connected
				? (TextFormatting.GREEN + I18nUtil.resolveKey("tile.machine_satlink.yes"))
				: (TextFormatting.RED + I18nUtil.resolveKey("tile.machine_satlink.no"))));

		for(ITextComponent comp : link.info) {
			if(comp != null) text.add(comp.getFormattedText());
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
}
