package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.gui.GUIScreenRadioTorchLogic;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.network.TileEntityRadioTorchLogic;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RadioTorchLogic extends RadioTorchRWBase implements IGUIProvider {

    public RadioTorchLogic(String regName) {
        super();

        this.setTranslationKey(regName);
        this.setRegistryName(regName);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        TileEntityRadioTorchLogic tile = new TileEntityRadioTorchLogic();
        tile.lastUpdate = world.getTotalWorldTime();
        return tile;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(@NotNull IBlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(@NotNull IBlockState blockState, IBlockAccess blockAccess, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        TileEntity tile = blockAccess.getTileEntity(pos);

        if (tile instanceof TileEntityRadioTorchLogic) {
            return ((TileEntityRadioTorchLogic) tile).lastState;
        }

        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEntityRadioTorchLogic radio) {
            List<String> text = new ArrayList<>();
            if (radio.channel != null && !radio.channel.isEmpty())
                text.add(ChatFormatting.AQUA + "Freq: " + radio.channel);
            text.add(ChatFormatting.RED + "Signal: " + radio.lastState);
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

        if (te instanceof TileEntityRadioTorchLogic)
            return new GUIScreenRadioTorchLogic((TileEntityRadioTorchLogic) te);

        return null;
    }
}
