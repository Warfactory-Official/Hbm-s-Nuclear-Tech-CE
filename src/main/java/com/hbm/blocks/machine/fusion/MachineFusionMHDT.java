package com.hbm.blocks.machine.fusion;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.fusion.TileEntityFusionMHDT;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MachineFusionMHDT extends BlockDummyable implements ILookOverlay, ITooltipProvider {

    public MachineFusionMHDT(String s) {
        super(Material.IRON, s);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        if(meta >= 12) return new TileEntityFusionMHDT();
        if(meta >= 6) return new TileEntityProxyCombo().power().fluid();
        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] { 2, 0, 6, 7, 2, 2 };
    }

    @Override
    public int getOffset() {
        return 7;
    }

    @Override
    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
        return super.checkRequirement(world, x, y, z, dir, o) &&
                MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {3, -2, 6, 2, 1, 1}, x, y, z, dir) &&
                MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {3, -2, -6, 7, 1, 1}, x, y, z, dir) &&
                MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {3, -2, -3, 5, 2, 2}, x, y, z, dir) &&
                MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, -3, -3, 5, 1, 1}, x, y, z, dir) &&
                MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * (o + 3), y, z + dir.offsetZ * (o + 3), new int[] {1, 0, 0, 1, 3, 3}, x, y, z, dir);
    }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y, z + dir.offsetZ * o, new int[] {3, -2, 6, 2, 1, 1}, this, dir);
        MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y, z + dir.offsetZ * o, new int[] {3, -2, -6, 7, 1, 1}, this, dir);
        MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y, z + dir.offsetZ * o, new int[] {3, -2, -3, 5, 2, 2}, this, dir);
        MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y, z + dir.offsetZ * o, new int[] {4, -3, -3, 5, 1, 1}, this, dir);
        MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * (o + 3), y, z + dir.offsetZ * (o + 3), new int[] {1, 0, 0, 1, 3, 3}, this, dir);

        x += dir.offsetX * o;
        z += dir.offsetZ * o;

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        this.makeExtra(world, x + dir.offsetX * 4 + rot.offsetX * 3, y, z + dir.offsetZ * 4 + rot.offsetZ * 3);
        this.makeExtra(world, x + dir.offsetX * 4 - rot.offsetX * 3, y, z + dir.offsetZ * 4 - rot.offsetZ * 3);
        this.makeExtra(world, x + dir.offsetX * 7, y + 1, z + dir.offsetZ * 7);
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        BlockPos corePos = this.findCore(world, pos);
        if(corePos == null) return;

        TileEntity te = world.getTileEntity(corePos);

        if(!(te instanceof TileEntityFusionMHDT turbine)) return;

        boolean hasPlasma = turbine.hasMinimumPlasma();
        boolean isCool = turbine.isCool();
        long power = (long) Math.floor(turbine.plasmaEnergy * TileEntityFusionMHDT.PLASMA_EFFICIENCY);
        if(!hasPlasma) power /= 2;

        List<String> text = new ArrayList<>();
        text.add(TextFormatting.GREEN + "-> " + (hasPlasma ? TextFormatting.RESET : TextFormatting.GOLD) + BobMathUtil.getShortNumber(turbine.plasmaEnergy) + "TU/t / " + BobMathUtil.getShortNumber(TileEntityFusionMHDT.MINIMUM_PLASMA) + "TU/t");
        text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + BobMathUtil.getShortNumber(!isCool ? 0 : power) + "HE/t");

        for(int i = 0; i < turbine.getAllTanks().length; i++) {
            FluidTankNTM tank = turbine.getAllTanks()[i];
            text.add((i == 0 ? (TextFormatting.GREEN + "-> ") : (TextFormatting.RED + "<- ")) + TextFormatting.RESET + tank.getTankType().getLocalizedName() + ": " + tank.getFill() + "/" + tank.getMaxFill() + "mB");
        }

        if(turbine.plasmaEnergy > 0 && !hasPlasma) text.add("&[" + (BobMathUtil.getBlink() ? 0xff8000 : 0xffff00) + "&]! LOW POWER !");
        if(!isCool) text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! INSUFFICIENT COOLING ! ! !");

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World player, @NotNull List<String> tooltip, @NotNull ITooltipFlag advanced) {
        addStandardInfo(tooltip);
    }
}
