package com.hbm.blocks.network;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityRadioTorchReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
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
        return new TileEntityRadioTorchReceiver();
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        } else if (!player.isSneaking()) {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        } else {
            return false;
        }
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

    @Override
    public int getStrongPower(IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        if (side == state.getValue(FACING).getOpposite()) return getWeakPower(state, world, pos, side);
        return 0;
    }
}
