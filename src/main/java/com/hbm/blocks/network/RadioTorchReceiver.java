package com.hbm.blocks.network;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.network.TileEntityRadioTorchReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RadioTorchReceiver extends RadioTorchRWBase {
    public RadioTorchReceiver(String regName) {
        super();
        this.setTranslationKey(regName);
        this.setRegistryName(regName);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        TileEntityRadioTorchReceiver tile = new TileEntityRadioTorchReceiver();
        tile.lastUpdate = worldIn.getTotalWorldTime();
        return tile;
    }

    @Override
    public boolean canProvidePower(@NotNull IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(@NotNull IBlockState state, IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityRadioTorchReceiver) {
            return ((TileEntityRadioTorchReceiver) tile).lastState;
        }

        return 0;
    }
}
