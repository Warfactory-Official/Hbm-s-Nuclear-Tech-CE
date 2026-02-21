package com.hbm.blocks.bomb;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.ModContext;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.bomb.TileEntityLaunchPadBase;
import com.hbm.tileentity.bomb.TileEntityLaunchPadLarge;
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

public class LaunchPadLarge extends BlockDummyable implements IBomb {

	public LaunchPadLarge(String s, Material mat) {
		super(mat, s);
		this.bounding.add(new AxisAlignedBB(-4.5D, 0D, -4.5D, 4.5D, 1D, -0.5D));
		this.bounding.add(new AxisAlignedBB(-4.5D, 0D, 0.5D, 4.5D, 1D, 4.5D));
		this.bounding.add(new AxisAlignedBB(-4.5D, 0.875D, -0.5D, 4.5D, 1D, 0.5D));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityLaunchPadLarge();
		if(meta >= 6) return new TileEntityProxyCombo(true, true, true);
		return new TileEntityProxyCombo(true, false, false);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 4, 4, 4, 4};
	}

	@Override
	public int getOffset() {
		return 4;
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {
		
		if(!world.isRemote) {
			
			int[] corePos = findCore(world, pos.getX(), pos.getY(), pos.getZ());
			BlockPos cPos = new BlockPos(corePos[0], corePos[1], corePos[2]);
			TileEntity core = world.getTileEntity(cPos);
			if(core instanceof TileEntityLaunchPadLarge launchPadLarge){
				ModContext.DETONATOR_CONTEXT.set(detonator);
				BombReturnCode result;
				try {
					result = launchPadLarge.launchFromDesignator();
				} finally {
					ModContext.DETONATOR_CONTEXT.remove();
				}
				return result;
			}
		}
		
		return BombReturnCode.UNDEFINED;
	}
	
	@Override
	public void neighborChanged(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos){
		
		if(!world.isRemote){

			int[] corePos = findCore(world, pos.getX(), pos.getY(), pos.getZ());
			if(corePos != null) {
				BlockPos cPos = new BlockPos(corePos[0], corePos[1], corePos[2]);
				TileEntity core = world.getTileEntity(cPos);
				if (core instanceof TileEntityLaunchPadLarge launchpad) {
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

		this.makeExtra(world, x + 4, y, z + 2);
		this.makeExtra(world, x + 4, y, z - 2);
		this.makeExtra(world, x - 4, y, z + 2);
		this.makeExtra(world, x - 4, y, z - 2);
		this.makeExtra(world, x + 2, y, z + 4);
		this.makeExtra(world, x - 2, y, z + 4);
		this.makeExtra(world, x + 2, y, z - 4);
		this.makeExtra(world, x - 2, y, z - 4);
	}
}
