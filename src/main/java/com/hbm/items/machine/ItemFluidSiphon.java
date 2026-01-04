package com.hbm.items.machine;

import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FluidTraitSimple;
import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemPipette;
import com.hbm.util.CompatExternal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemFluidSiphon extends ItemBakedBase {

    public ItemFluidSiphon(String s) {
        super(s);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            TileEntity teClient = CompatExternal.getCoreFromPos(world, pos);
            return (teClient instanceof IFluidStandardReceiverMK2) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
        }
        TileEntity te = CompatExternal.getCoreFromPos(world, pos);
        if (!(te instanceof IFluidStandardReceiverMK2)) return EnumActionResult.PASS;
        FluidTankNTM[] tanks = ((IFluidStandardReceiverMK2) te).getReceivingTanks();
        if (tanks == null) return EnumActionResult.PASS;
        for (FluidTankNTM tank : tanks) {
            if (tank == null) continue;
            int fill = tank.getFill();
            if (fill <= 0) continue;
            FluidType tankType = tank.getTankType();
            if (tankType == Fluids.NONE) continue;
            if (tankType.hasTrait(FluidTraitSimple.FT_Unsiphonable.class)) continue;
            boolean drainedThisTank = false;
            ItemStack availablePipette = ItemStack.EMPTY;
            for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
                ItemStack inv = player.inventory.mainInventory.get(slot);
                if (inv.isEmpty()) continue;
                if (availablePipette.isEmpty() && inv.getItem() instanceof ItemPipette pipette) {
                    if (pipette != ModItems.pipette_laboratory && !pipette.willFizzle(tankType) && pipette.acceptsFluid(tankType, inv)) {
                        availablePipette = inv;
                    }
                }
                FluidContainerRegistry.FluidContainer recipe = FluidContainerRegistry.getFillRecipe(inv, tankType);
                if (recipe == null) continue;
                int perContainer = recipe.content();
                if (perContainer <= 0) continue;
                int maxByFluid = fill / perContainer;
                if (maxByFluid == 0) continue;
                int toFillTotal = Math.min(inv.getCount(), maxByFluid);
                if (toFillTotal <= 0) continue;
                inv.shrink(toFillTotal);
                if (inv.getCount() <= 0) {
                    player.inventory.mainInventory.set(slot, ItemStack.EMPTY);
                }

                ItemStack outTemplate = recipe.fullContainer().copy();
                int maxOutStack = Math.max(1, outTemplate.getMaxStackSize());

                int remaining = toFillTotal;
                while (remaining > 0) {
                    int batch = Math.min(remaining, maxOutStack);
                    ItemStack out = outTemplate.copy();
                    out.setCount(batch);
                    if (!player.inventory.addItemStackToInventory(out)) {
                        player.dropItem(out, false);
                    }
                    remaining -= batch;
                }

                fill -= toFillTotal * perContainer;
                drainedThisTank = true;

                if (fill <= 0) break;
            }

            if (!availablePipette.isEmpty() && fill > 0 && fill < 1000) {
                ItemPipette pipette = (ItemPipette) availablePipette.getItem();
                int newFill = pipette.tryFill(tankType, fill, availablePipette);
                if (newFill != fill) {
                    fill = newFill;
                    drainedThisTank = true;
                }
            }

            if (drainedThisTank) {
                tank.setFill(fill);
                te.markDirty();
                player.inventory.markDirty();
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }
}
