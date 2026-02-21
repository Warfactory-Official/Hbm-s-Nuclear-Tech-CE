package com.hbm.blocks.machine.rbmk;

import com.hbm.api.fluid.IFluidConnectorBlock;
import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.lib.ForgeDirection;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RBMKLoader extends BlockBase implements IFluidConnectorBlock, ITooltipProvider {

    public RBMKLoader(Material material, String s) {
        super(material, s);
    }

    @Override // this method is purely visual, actual logic at TileEntityRBMKBoiler
    public boolean canConnect(FluidType type, IBlockAccess world, int x, int y, int z, ForgeDirection dir) {
        if (dir == ForgeDirection.UP) return type.hasTrait(FT_Heatable.class);
        return type.hasTrait(FT_Coolable.class);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        this.addStandardInfo(tooltip);
    }
}
