package com.hbm.blocks.machine.rbmk;

import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKOutgasser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RBMKOutgasser extends RBMKBase {

	public RBMKOutgasser(String s, String c){
		super(s, c);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= offset)
			return new TileEntityRBMKOutgasser();
		
		if(hasExtra(meta))
			return new TileEntityProxyCombo(true, false, true);
		
		return null;
	}
	
	@Override
	public boolean onBlockActivated(@NotNull World worldIn, BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ){
		return openInv(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, hand);
	}

}
