package com.hbm.items.tool;

import com.hbm.items.machine.ItemSatellite;
import com.hbm.lib.Library;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteSavedData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemSatDesignator extends ItemSatellite {

    public ItemSatDesignator(String regName) {
        super(regName);
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, @NotNull EntityPlayer player, @NotNull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!world.isRemote) {
            Satellite sat = SatelliteSavedData.getData(world).getSatFromFreq(this.getFreq(stack));

            if (sat != null) {
                RayTraceResult pos = Library.rayTrace(player, 300, 1);
                BlockPos rayBlockPos = pos.getBlockPos();

                EnumFacing facing = pos.sideHit;
                int x = rayBlockPos.getX() + facing.getXOffset();
                int y = rayBlockPos.getY() + facing.getYOffset();
                int z = rayBlockPos.getZ() + facing.getZOffset();

                if (sat.satIface == Satellite.Interfaces.SAT_COORD) {
                    sat.onCoordAction(world, (EntityPlayerMP) player, x, y, z);
                } else if (sat.satIface == Satellite.Interfaces.SAT_PANEL) {
                    sat.onClick(world, (EntityPlayerMP) player, x, z);
                }
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

}
