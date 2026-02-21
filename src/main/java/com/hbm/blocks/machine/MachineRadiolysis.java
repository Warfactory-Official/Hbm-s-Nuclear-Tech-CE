package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.BossSpawnHandler;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineRadiolysis;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

public class MachineRadiolysis extends BlockDummyable {

    public MachineRadiolysis(Material mat, String s) {
        super(mat, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        if(meta >= 12)
            return new TileEntityMachineRadiolysis();
        if(meta >= 6)
            return new TileEntityProxyCombo(true, true, true);

        return null;
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(world.isRemote) {
            return true;
        } else if(!player.isSneaking()) {
            BossSpawnHandler.markFBI(player);

            int[] corePos = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

            if(corePos == null)
                return false;

            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, corePos[0], corePos[1], corePos[2]);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int[] getDimensions() {
        return new int[] {2, 0, 1, 1, 1, 1,};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        this.makeExtra(world, x + dir.offsetX * o + 1, y, z + dir.offsetZ * o);
        this.makeExtra(world, x + dir.offsetX * o - 1, y, z + dir.offsetZ * o);
        this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o + 1);
        this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o - 1);
    }
}
