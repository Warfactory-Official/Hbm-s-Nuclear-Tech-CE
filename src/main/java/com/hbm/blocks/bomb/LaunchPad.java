package com.hbm.blocks.bomb;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.bomb.TileEntityLaunchPad;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class LaunchPad extends BlockDummyable implements IBomb {

	public LaunchPad(Material materialIn, String s) {
		super(materialIn, s);
		this.bounding.add(new AxisAlignedBB(-1.5D, 0D, -1.5D, -0.5D, 1D, -0.5D));
		this.bounding.add(new AxisAlignedBB(0.5D, 0D, -1.5D, 1.5D, 1D, -0.5D));
		this.bounding.add(new AxisAlignedBB(-1.5D, 0D, 0.5D, -0.5D, 1D, 1.5D));
		this.bounding.add(new AxisAlignedBB(0.5D, 0D, 0.5D, 1.5D, 1D, 1.5D));
		this.bounding.add(new AxisAlignedBB(-0.5D, 0.5D, -1.5D, 0.5D, 1D, 1.5D));
		this.bounding.add(new AxisAlignedBB(-1.5D, 0.5D, -0.5D, 1.5D, 1D, 0.5D));
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= 12) return new TileEntityLaunchPad();
		if(meta >= 6) return new TileEntityProxyCombo().inventory().power().fluid();
		return null;
	}

	@Override
	public boolean onBlockActivated(@NotNull World world, BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {

		if(!world.isRemote) {

			int[] corePos = findCore(world, pos.getX(), pos.getY(), pos.getZ());
			if(corePos != null){
				TileEntity core = world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));
				if(core instanceof TileEntityLaunchPad entity){
					return entity.launchFromDesignator();
				}
			}
		}

		return BombReturnCode.UNDEFINED;
	}

	@Override
	public void neighborChanged(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos) {
		if (!world.isRemote) {
			int[] corePos = findCore(world, pos.getX(), pos.getY(), pos.getZ());
			if (corePos != null) {
				TileEntity core = world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));
				if (core instanceof TileEntityLaunchPad) {
					TileEntityLaunchPad launchpad = (TileEntityLaunchPad) core;
					launchpad.updateRedstonePower(pos.getX(), pos.getY(), pos.getZ());
				}
			}
		}
		super.neighborChanged(state, world, pos, blockIn, fromPos);
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		this.makeExtra(world, x + 1, y, z + 1);
		this.makeExtra(world, x + 1, y, z - 1);
		this.makeExtra(world, x - 1, y, z + 1);
		this.makeExtra(world, x - 1, y, z - 1);
	}
}