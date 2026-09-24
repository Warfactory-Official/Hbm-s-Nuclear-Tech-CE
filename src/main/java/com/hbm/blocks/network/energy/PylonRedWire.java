package com.hbm.blocks.network.energy;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.network.energy.TileEntityPylon;
import com.hbm.tileentity.network.energy.TileEntityPylonBase;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PylonRedWire extends BlockDummyable implements ITooltipProvider {

	public PylonRedWire(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta == 0 || meta >= 12) return new TileEntityPylon();
		return null;
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {
		Collections.addAll(list, I18nUtil.resolveKeyArray(this.getTranslationKey() + ".desc"));
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {4, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public void breakBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityPylonBase pylon) pylon.disconnectAll();
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(player.isSneaking()) return false;
		BlockPos core = this.findCore(world, pos);
		if(core == null) return false;
		TileEntity te = world.getTileEntity(core);
		return te instanceof TileEntityPylonBase pylon && pylon.setColor(player.getHeldItem(hand));
	}
}
