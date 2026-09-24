package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.integration.ae2.NTMCraftingMachineFactory;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineRockMill;
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

public class MachineRockMill extends BlockDummyable {

    public MachineRockMill(Material mat, String s) {
        super(mat, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if(meta >= 12) return Loader.isModLoaded(Compat.ModIds.AE2) ? NTMCraftingMachineFactory.createAE2TileEntity("com.hbm.integration.ae2.tileentity.TileEntityMachineRockMillAE2") : new TileEntityMachineRockMill();
        if(meta >= 6) return Loader.isModLoaded(Compat.ModIds.AE2) ? NTMCraftingMachineFactory.createAE2Proxy(true, true, true) : new TileEntityProxyCombo().inventory().power().fluid();
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return this.standardOpenBehavior(world, pos, player, 0);
    }

    @Override public int[] getDimensions() { return new int[] {2, 0, 2, 2, 2, 2}; }
    @Override public int getOffset() { return 2; }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x += dir.offsetX * o;
        z += dir.offsetZ * o;

        this.makeExtra(world, x + 2, y, z + 1);
        this.makeExtra(world, x - 2, y, z + 1);
        this.makeExtra(world, x + 2, y, z - 1);
        this.makeExtra(world, x - 2, y, z - 1);
        this.makeExtra(world, x + 1, y, z + 2);
        this.makeExtra(world, x + 1, y, z - 2);
        this.makeExtra(world, x - 1, y, z + 2);
        this.makeExtra(world, x - 1, y, z - 2);
    }
}
