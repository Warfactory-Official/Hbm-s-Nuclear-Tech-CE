package com.hbm.blocks.network;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityRadioAUTOCAL;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RadioAUTOCAL extends BlockDummyable {
    public RadioAUTOCAL(String s) {
        super(Material.IRON, s);
    }

    @Override
    public @Nullable TileEntity createNewTileEntity(World worldIn, int meta) {
        if (meta >= 12) return new TileEntityRadioAUTOCAL();
        return null;
    }

    @Override public int[] getDimensions() { return new int[] {1, 0, 0, 0, 0 ,0}; }
    @Override public int getOffset() { return 0; }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (playerIn.isSneaking()) return false;
        if (worldIn.isRemote) {
            BlockPos core = this.findCore(worldIn, pos);
            if (core == null) return false;
            FMLNetworkHandler.openGui(playerIn, MainRegistry.instance, 0, worldIn, core.getX(), core.getY(), core.getZ());
        }
        return true;
    }
}
