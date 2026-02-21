package com.hbm.core;

import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.CompatExternal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

@SuppressWarnings("unused")
public final class PlayerInteractionManagerHook {

    private PlayerInteractionManagerHook() {
    }

    public static EnumActionResult onSpectatorRightClickBlock(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand, BlockPos pos,
                                                              EnumFacing facing, float hitX, float hitY, float hitZ, TileEntity tileentity) {
        //mlbv: in case somehow a spectator is able to move an item: add a check in com.hbm.handler.GuiHandler.getServerGuiElement and wrap the returned Container
        //with a delegation container that makes methods like slotClick no-op
        //currently it works as intended; if it ever breaks, add that check and it should be fine.
        if (tileentity instanceof IGUIProvider provider) {
            FMLNetworkHandler.openGui(player, provider.getModInstanceForGui(), 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return EnumActionResult.SUCCESS;
        }
        if (worldIn.getBlockState(pos).getBlock() instanceof IGUIProvider provider) {
            FMLNetworkHandler.openGui(player, provider.getModInstanceForGui(), 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return EnumActionResult.SUCCESS;
        }
        TileEntity core = CompatExternal.getCoreFromPos(worldIn, pos);
        if (core instanceof IGUIProvider provider) {
            FMLNetworkHandler.openGui(player, provider.getModInstanceForGui(), 0, worldIn, core.getPos().getX(), core.getPos().getY(), core.getPos().getZ());
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    // only used on hybrid servers. This is a BAD solution, feel free to modify if you come up with a better idea.
    public static EnumActionResult onRightClickBlockPost(PlayerInteractionManager manager, EntityPlayer player, World world, ItemStack stack,
                                                         EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                                         EnumActionResult original) {
        if (manager.getGameType() != GameType.SPECTATOR) return original;
        if (original == EnumActionResult.FAIL) return original;
        TileEntity te = world.getTileEntity(pos);
        return onSpectatorRightClickBlock(player, world, stack, hand, pos, facing, hitX, hitY, hitZ, te);
    }
}
