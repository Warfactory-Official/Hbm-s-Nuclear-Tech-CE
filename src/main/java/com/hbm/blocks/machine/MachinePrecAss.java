package com.hbm.blocks.machine;

import com.hbm.integration.ae2.NTMCraftingMachineFactory;
import com.hbm.tileentity.machine.TileEntityMachinePrecAss;
import com.hbm.util.Compat;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public class MachinePrecAss extends MachineAssemblyMachine {

    public MachinePrecAss(Material mat, String s) {
        super(mat, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if(meta >= 12) return Loader.isModLoaded(Compat.ModIds.AE2) ? NTMCraftingMachineFactory.createAE2TileEntity("com.hbm.integration.ae2.tileentity.TileEntityMachinePrecAssAE2") : new TileEntityMachinePrecAss();
        return super.createNewTileEntity(world, meta);
    }
}
