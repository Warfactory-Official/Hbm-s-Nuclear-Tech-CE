package com.hbm.blocks.machine.fusion;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.machine.fusion.TileEntityFusionCoupler;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MachineFusionCoupler extends BlockDummyable implements ITooltipProvider {

    public MachineFusionCoupler(String s) {
        super(Material.IRON, s);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        if(meta >= 12) return new TileEntityFusionCoupler();
        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] { 3, 0, 1, 1, 1, 1 };
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World player, @NotNull List<String> tooltip, @NotNull ITooltipFlag advanced) {
        addStandardInfo(tooltip);
    }
}
