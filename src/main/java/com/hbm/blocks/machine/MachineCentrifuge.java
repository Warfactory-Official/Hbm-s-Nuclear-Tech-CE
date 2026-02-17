package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineCentrifuge;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

public class MachineCentrifuge extends BlockDummyable {

    public MachineCentrifuge(Material materialIn, String s) {
        super(materialIn, s);
        this.bounding.add(new AxisAlignedBB(-0.5D, 0D, -0.5D, 0.5D, 1D, 0.5D));
        this.bounding.add(new AxisAlignedBB(-0.375D, 1D, -0.375D, 0.375D, 4D, 0.375D));
        FULL_BLOCK_AABB.setMaxY(0.999D); //item bounce prevention
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {

        if (meta >= 12)
            return new TileEntityMachineCentrifuge();
        if (meta >= 6)
            return new TileEntityProxyCombo(false, true, true);

        return null;
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        } else if (!player.isSneaking()) {
            int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

            if (posC == null)
                return false;

            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, posC[0], posC[1], posC[2]);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return state.getBlock().getMetaFromState(state) >= 12;
    }

    @Override
    public int[] getDimensions() {
        return new int[]{3, 0, 0, 0, 0, 0,};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);
    }
}
