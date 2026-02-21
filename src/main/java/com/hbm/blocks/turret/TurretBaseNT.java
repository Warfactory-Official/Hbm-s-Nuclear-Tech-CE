package com.hbm.blocks.turret;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

public abstract class TurretBaseNT extends BlockDummyable {

	public TurretBaseNT(Material materialIn, String s){
		super(materialIn, s);
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 0, 0, 1, 0, 1, 0 };
	}

	@Override
	public int getOffset() {
		return 0;
	}
	
	@Override
	public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos){
		return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos bpos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] pos = this.findCore(world, bpos.getX(), bpos.getY(), bpos.getZ());

			if(pos == null)
				return false;

			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			return true;
		} else {
			return false;
		}
	}
	
}
