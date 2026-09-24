package com.hbm.blocks.network;

import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIScreenRadioTorchController;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.network.TileEntityRadioTorchController;
import com.hbm.util.Compat;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RadioTorchController extends RadioTorchBase implements IGUIProvider {

    public RadioTorchController(String regName) {
        super();
        setRegistryName(regName);
        setTranslationKey(regName);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntityRadioTorchController();
    }

    @Override
    public boolean canPlaceBlockOnSide(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        if (!super.canPlaceBlockOnSide(worldIn, pos, side)) return false;
        BlockPos checkPos = pos.offset(side.getOpposite());
        IBlockState checkState = worldIn.getBlockState(checkPos);

        return canBlockStay(worldIn, side, checkState.getBlock(), checkPos, checkState);
    }

    public boolean canBlockStay(World world, EnumFacing dir, Block b, BlockPos checkPos, IBlockState checkState) {
        TileEntity te = Compat.getTileStandard(world, checkPos.getX(), checkPos.getY(), checkPos.getZ());
        return te instanceof IRORValueProvider;
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEntityRadioTorchController radio) {
            List<String> text = new ArrayList<>();

            if (radio.channel != null && !radio.channel.isEmpty()) {
                text.add(ChatFormatting.AQUA + "Freq: " + radio.channel);
            }

            ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
        }
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te instanceof TileEntityRadioTorchController)
            return new GUIScreenRadioTorchController((TileEntityRadioTorchController) te);
        return null;
    }
}
