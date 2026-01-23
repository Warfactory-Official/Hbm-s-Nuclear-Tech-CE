package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityHeaterHeatex;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class HeaterHeatex extends BlockDummyable implements ITooltipProvider, ILookOverlay {

    public HeaterHeatex(Material mat, String s) {
        super(Material.IRON, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        if (meta >= 12) return new TileEntityHeaterHeatex();
        if (hasExtra(meta)) {
            return new TileEntityProxyCombo(false, false, true);
        }

        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[]{0, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        if(player.isSneaking()) {
            TileEntity core = findCoreTE(world, pos);
            if(!(core instanceof TileEntityHeaterHeatex boiler)) return false;
            if (player.getHeldItem(hand).isEmpty() || !(player.getHeldItem(hand).getItem() instanceof IItemFluidIdentifier identifier))
                return false;
            FluidType type = identifier.getType(world, pos.getX(), pos.getY(), pos.getZ(), player.getHeldItem(hand));
            boiler.tanksNew[0].setTankType(type);
            boiler.markDirty();
            player.sendMessage(new TextComponentString("Changed type to ").setStyle(new Style().setColor(TextFormatting.YELLOW)).appendSibling(new TextComponentTranslation(type.getConditionalName())).appendSibling(new TextComponentString("!")));
        } else {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        this.addStandardInfo(list);
        super.addInformation(stack, worldIn, list, flagIn);
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x += dir.offsetX * o;
        z += dir.offsetZ * o;

        this.makeExtra(world, x + 1, y, z + 1);
        this.makeExtra(world, x + 1, y, z - 1);
        this.makeExtra(world, x - 1, y, z + 1);
        this.makeExtra(world, x - 1, y, z - 1);
    }

    @Override
    public void printHook(Pre event, World world, int x, int y, int z) {
        TileEntity te = this.findCoreTE(world, x, y, z);
        if (!(te instanceof TileEntityHeaterHeatex heater))
            return;

        List<String> text = new ArrayList<>();
        text.add(String.format("%,d", heater.heatEnergy) + " TU");
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
}
