package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineHephaestus;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MachineHephaestus extends BlockDummyable implements ILookOverlay {

    public MachineHephaestus(Material mat, String s) {
        super(mat, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        if (meta >= 12) return new TileEntityMachineHephaestus();
        if (meta >= 6) return new TileEntityProxyCombo().fluid();
        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[]{11, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x -= dir.offsetX;
        z -= dir.offsetZ;

        this.makeExtra(world, x + 1, y, z);
        this.makeExtra(world, x - 1, y, z);
        this.makeExtra(world, x, y, z + 1);
        this.makeExtra(world, x, y, z - 1);
        this.makeExtra(world, x + 1, y + 11, z);
        this.makeExtra(world, x - 1, y + 11, z);
        this.makeExtra(world, x, y + 11, z + 1);
        this.makeExtra(world, x, y + 11, z - 1);
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player,
                                    @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (!world.isRemote && !player.isSneaking()) {

            if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IItemFluidIdentifier) {
                BlockPos corePos = this.findCore(world, pos);

                if (corePos == null) return false;

                TileEntity te = world.getTileEntity(corePos);

                if (!(te instanceof TileEntityMachineHephaestus heatex)) return false;

                FluidType type = ((IItemFluidIdentifier) player.getHeldItem(EnumHand.MAIN_HAND).getItem()).getType(world, corePos.getX(),
                        corePos.getY(), corePos.getZ(), player.getHeldItem(EnumHand.MAIN_HAND));
                heatex.input.setTankType(type);
                heatex.markDirty();
                player.sendMessage(new TextComponentString("Changed type to ").setStyle(new Style().setColor(TextFormatting.YELLOW)).appendSibling(new TextComponentTranslation(type.getConditionalName())).appendSibling(new TextComponentString("!")));
                return true;
            }
            return false;

        } else {
            return true;
        }
    }

    @Override
    public void printHook(Pre event, World world, BlockPos pos) {
        BlockPos corePos = this.findCore(world, pos);

        if(corePos == null)
            return;

        TileEntity te = world.getTileEntity(corePos);

        if (!(te instanceof TileEntityMachineHephaestus heatex)) return;

        List<String> text = new ArrayList<>();
        text.add(String.format(Locale.US, "%,d", heatex.bufferedHeat) + " TU");

        for (int i = 0; i < heatex.getAllTanks().length; i++) {
            FluidTankNTM tank = heatex.getAllTanks()[i];
            text.add((i == 0 ? (TextFormatting.GREEN + "-> ") : (TextFormatting.RED + "<- ")) + TextFormatting.RESET + tank.getTankType().getLocalizedName() + ": " + tank.getFill() + "/" + tank.getMaxFill() + "mB");
        }

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
}
