package com.hbm.blocks.network;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.network.TileEntityRadioTorchSender;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RadioTorchSender extends RadioTorchRWBase {
    public RadioTorchSender(String regName) {
        super();
        this.setTranslationKey(regName);
        this.setRegistryName(regName);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        return new TileEntityRadioTorchSender();
    }

    @Override
    public boolean getWeakChanges(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        return true;
    }
}
