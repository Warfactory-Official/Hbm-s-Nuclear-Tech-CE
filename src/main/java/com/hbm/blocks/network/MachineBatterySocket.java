package com.hbm.blocks.network;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;
import com.hbm.util.BobMathUtil;
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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MachineBatterySocket extends BlockDummyable implements ITooltipProvider, ILookOverlay {

    public MachineBatterySocket(String s) {
        super(Material.IRON, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (meta >= 12) return new TileEntityBatterySocket();
        if (meta >= 6) return new TileEntityProxyCombo().inventory().power().conductor();
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return super.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
    }

    @Override
    public int[] getDimensions() {
        return new int[]{1, 0, 1, 0, 1, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        this.makeExtra(world, x - dir.offsetX, y, z - dir.offsetZ);
        this.makeExtra(world, x + rot.offsetX, y, z + rot.offsetZ);
        this.makeExtra(world, x - dir.offsetX + rot.offsetX, y, z - dir.offsetZ + rot.offsetZ);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        addStandardInfo(tooltip);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        if(getMetaFromState(state) < 6) return 0;
        TileEntity te = this.findCoreTE(world, pos);
        if(!(te instanceof TileEntityBatterySocket socket)) return 0;
        return socket.getComparatorPower();
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
        TileEntity te = this.findCoreTE(world, x, y, z);
        if(!(te instanceof TileEntityBatterySocket socket)) return;
        if(socket.syncStack == null) return;

        List<String> text = new ArrayList();
        text.add(BobMathUtil.getShortNumber(socket.powerFromStack(socket.syncStack)) + " / " + BobMathUtil.getShortNumber(socket.maxPowerFromStack(socket.syncStack)) + "HE");

        double percent = (double) socket.powerFromStack(socket.syncStack) / (double) socket.maxPowerFromStack(socket.syncStack);
        int charge = (int) Math.floor(percent * 10_000D);
        int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int)(0xFF * percent) << 8);

        text.add("&[" + color + "&]" + (charge / 100D) + "%");

        ILookOverlay.printGeneric(event, socket.syncStack.getDisplayName(), 0xffff00, 0x404000, text);
    }
}
