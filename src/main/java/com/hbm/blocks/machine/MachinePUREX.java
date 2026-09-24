package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.integration.ae2.NTMCraftingMachineFactory;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachinePUREX;
import com.hbm.util.Compat;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachinePUREX extends BlockDummyable implements ITooltipProvider {

    public MachinePUREX(Material mat, String s) {
        super(mat, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if(meta >= 12) return Loader.isModLoaded(Compat.ModIds.AE2) ? NTMCraftingMachineFactory.createAE2TileEntity("com.hbm.integration.ae2.tileentity.TileEntityMachinePUREXAE2") : new TileEntityMachinePUREX();
        if(meta >= 6) return Loader.isModLoaded(Compat.ModIds.AE2) ? NTMCraftingMachineFactory.createAE2Proxy(true, true, true) : new TileEntityProxyCombo().inventory().power().fluid();
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
    }

    @Override public int[] getDimensions() { return new int[] {4, 0, 2, 2, 2, 2}; }
    @Override public int getOffset() { return 2; }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x += dir.offsetX * o;
        z += dir.offsetZ * o;

        for(int i = -2; i <= 2; i++) for(int j = -2; j <= 2; j++) {
            if(Math.abs(i) == 2 || Math.abs(j) == 2) this.makeExtra(world, x + i, y, z + j);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, @NotNull ITooltipFlag flag) {
        this.addStandardInfo(tooltip);
    }
}
