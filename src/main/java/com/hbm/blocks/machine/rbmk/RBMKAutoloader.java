package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKAutoloader;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RBMKAutoloader extends BlockDummyable {

    public RBMKAutoloader(String name) {
        super(Material.IRON, name);

        this.bounding.add(new AxisAlignedBB(-0.125, 0, -0.125, 0.125, 4, 0.125));
        this.bounding.add(new AxisAlignedBB(-0.5, 4, -0.5, 0.5, 9, 0.5));
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        if(meta >= 12) return new TileEntityRBMKAutoloader();
        return new TileEntityProxyCombo().inventory();
    }

    @Override
    public int[] getDimensions() {
        return new int[] {8, 0, 0, 0, 0, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public boolean onBlockActivated(@NotNull World worldIn, BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        return this.standardOpenBehavior(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, 0);
    }
}
