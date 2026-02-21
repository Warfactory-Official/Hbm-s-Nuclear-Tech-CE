package com.hbm.blocks.network.energy;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyConductor;
import com.hbm.tileentity.network.energy.TileEntityPylonBase;
import com.hbm.tileentity.network.energy.TileEntitySubstation;
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

import java.util.List;

public class Substation extends BlockDummyable implements ITooltipProvider {

	public Substation(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		
		if(meta >= 12)
			return new TileEntitySubstation();
		
		if(meta >= 6)
			return new TileEntityProxyConductor();
		
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {4, 0, 1, 1, 2, 2};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void breakBlock(@NotNull World world, @NotNull BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPylonBase) {
            ((TileEntityPylonBase)te).disconnectAll();
        }
        super.breakBlock(world, pos, state);
    }

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		this.makeExtra(world, x + dir.offsetX * o + 1, y, z + dir.offsetZ * o + 1);
		this.makeExtra(world, x + dir.offsetX * o + 1, y, z + dir.offsetZ * o - 1);
		this.makeExtra(world, x + dir.offsetX * o - 1, y, z + dir.offsetZ * o + 1);
		this.makeExtra(world, x + dir.offsetX * o - 1, y, z + dir.offsetZ * o - 1);
	}

	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        this.addStandardInfo(list);
        super.addInformation(stack, worldIn, list, flagIn);
    }

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
			if(posC == null) return false;
			TileEntityPylonBase te = (TileEntityPylonBase) world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));
			return te.setColor(player.getHeldItem(hand));
		} else {
			return false;
		}
	}
}
