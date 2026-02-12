package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachinePumpBase;
import com.hbm.tileentity.machine.TileEntityMachinePumpElectric;
import com.hbm.tileentity.machine.TileEntityMachinePumpSteam;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MachinePump extends BlockDummyable implements ITooltipProvider, ILookOverlay {

    public MachinePump(Material materialIn, String s) {
        super(materialIn, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if(meta >= 12) {
            if(this == ModBlocks.pump_steam) return new TileEntityMachinePumpSteam();
            if(this == ModBlocks.pump_electric) return new TileEntityMachinePumpElectric();
        }
        if(meta >= 6)  {
            if(this == ModBlocks.pump_steam) return new TileEntityProxyCombo(false, false, true);
            if(this == ModBlocks.pump_electric) return new TileEntityProxyCombo(false, true, true);
        }
        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {3, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        this.makeExtra(world, x - dir.offsetX + 1, y, z - dir.offsetZ);
        this.makeExtra(world, x - dir.offsetX - 1, y, z - dir.offsetZ);
        this.makeExtra(world, x - dir.offsetX, y, z - dir.offsetZ + 1);
        this.makeExtra(world, x - dir.offsetX, y, z - dir.offsetZ - 1);
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {

        BlockPos corePos = this.findCore(world, pos);

        if(corePos == null)
            return;

        TileEntity te = world.getTileEntity(corePos);

        if(!(te instanceof TileEntityMachinePumpBase)) return;

        List<String> text = new ArrayList();

        if(te instanceof TileEntityMachinePumpSteam pump) {
            text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + pump.steam.getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", pump.steam.getFill()) + " / " + String.format(Locale.US, "%,d", pump.steam.getMaxFill()) + "mB");
            text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + pump.lps.getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", pump.lps.getFill()) + " / " + String.format(Locale.US, "%,d", pump.lps.getMaxFill()) + "mB");
            text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + pump.water.getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", pump.water.getFill()) + " / " + String.format(Locale.US, "%,d", pump.water.getMaxFill()) + "mB");
        }

        if(te instanceof TileEntityMachinePumpElectric pump) {
            text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + String.format(Locale.US, "%,d", pump.power) + " / " + String.format(Locale.US, "%,d", pump.maxPower) + "HE");
            text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + pump.water.getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", pump.water.getFill()) + " / " + String.format(Locale.US, "%,d", pump.water.getMaxFill()) + "mB");
        }

        if(corePos.getY() > 70) {
            text.add("&[" + ( System.currentTimeMillis() % 1000 < 500 ? 0xff0000 : 0xffff00) + "&]! ! ! ALTITUDE ! ! !");
        }

        if(!((TileEntityMachinePumpBase) te).onGround) {
            text.add("&[" + ( System.currentTimeMillis() % 1000 < 500 ? 0xff0000 : 0xffff00) + "&]! ! ! NO VALID GROUND ! ! !");
        }

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
}
