package com.hbm.blocks.network.energy;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.network.energy.TileEntityPylonBase;
import com.hbm.tileentity.network.energy.TileEntityPylonLarge;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PylonLarge extends BlockDummyable implements ITooltipProvider {

	public PylonLarge(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= 12)
			return new TileEntityPylonLarge();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {13, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public void breakBlock(@NotNull World world, @NotNull BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPylonBase) {
            ((TileEntityPylonBase)te).disconnectAll();
        }
        super.breakBlock(world, pos, state);
    }

    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {
		Collections.addAll(list, I18nUtil.resolveKeyArray("tile.red_pylon_large.desc"));
		super.addInformation(stack, worldIn, list, flagIn);
	}

    @Override
    protected int getMetaForCore(World world, BlockPos pos, EntityPlayer player, int original) {

        int i = MathHelper.floor(player.rotationYaw * 4.0F / 180.0F + 0.5D) & 3;

        ForgeDirection dir = ForgeDirection.NORTH;

        if(i == 0) {
            dir = ForgeDirection.getOrientation(2);
        }
        if(i == 1) {
            dir = ForgeDirection.getOrientation(5);
        }
        if(i == 2) {
            dir = ForgeDirection.getOrientation(3);
        }
        if(i == 3) {
            dir = ForgeDirection.getOrientation(4);
        }

        return dir.ordinal() + offset;
    }

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
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