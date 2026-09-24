package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.integration.ae2.NTMCraftingMachineFactory;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine;
import com.hbm.util.Compat;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public class MachineAssemblyMachine extends BlockDummyable {

    public MachineAssemblyMachine(Material mat, String s) {
        super(mat, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if(meta >= 12) return Loader.isModLoaded(Compat.ModIds.AE2) ? NTMCraftingMachineFactory.createAE2TileEntity("com.hbm.integration.ae2.tileentity.TileEntityMachineAssemblyMachineAE2") : new TileEntityMachineAssemblyMachine();
        if(meta >= 6) return Loader.isModLoaded(Compat.ModIds.AE2) ? NTMCraftingMachineFactory.createAE2Proxy(true, true, true) : new TileEntityProxyCombo().inventory().power().fluid();
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
    }

    @Override public int[] getDimensions() { return new int[] {2, 0, 1, 1, 1, 1}; }
    @Override public int getOffset() { return 1; }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x -= dir.offsetX;
        z -= dir.offsetZ;

        for(int i = -1; i <= 1; i++) for(int j = -1; j <= 1; j++) {
            if(i != 0 || j != 0) this.makeExtra(world, x + i, y, z + j);
        }
    }
}
