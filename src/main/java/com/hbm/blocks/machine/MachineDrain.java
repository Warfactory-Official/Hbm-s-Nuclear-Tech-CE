package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineDrain;
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
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.ArrayList;
import java.util.List;

public class MachineDrain extends BlockDummyable implements ILookOverlay {

    public MachineDrain(Material mat, String s) {
        super(mat, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if(meta >= 12) return new TileEntityMachineDrain();
        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {0, 0, 2, 0, 0, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if(!world.isRemote && !player.isSneaking()) {

            if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof IItemFluidIdentifier) {
                int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
                if(posC == null) return false;

                TileEntity te = world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));
                if(!(te instanceof TileEntityMachineDrain)) return false;

                TileEntityMachineDrain drain = (TileEntityMachineDrain) te;

                FluidType type = ((IItemFluidIdentifier) player.getHeldItem(hand).getItem()).getType(world, posC[0], posC[1], posC[2], player.getHeldItem(hand));
                drain.tank.setTankType(type);
                drain.markDirty();
                player.sendMessage(new TextComponentString("Changed type to ")
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))
                        .appendSibling(new TextComponentTranslation(type.getConditionalName()))
                        .appendSibling(new TextComponentString("!")));

                return true;
            }
            return false;

        } else {
            return true;
        }
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        BlockPos corePos = this.findCore(world, pos);

        if(corePos == null)
            return;

        TileEntity te = world.getTileEntity(corePos);
        if(!(te instanceof TileEntityMachineDrain drain)) return;

        List<String> text = new ArrayList();
        text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + drain.tank.getTankType().getLocalizedName() + ": " + drain.tank.getFill() + "/" + drain.tank.getMaxFill() + "mB");
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
}
