package com.hbm.blocks.machine;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.items.tool.ItemTooling;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityHeaterOilburner;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HeaterOilburner extends BlockDummyable implements ITooltipProvider, ILookOverlay, IToolable {
    public HeaterOilburner(Material mat, String s) {
        super(mat, s);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        if (meta >= 12)
            return new TileEntityHeaterOilburner();

        if (hasExtra(meta) && meta - extra > 1)
            return new TileEntityProxyCombo(false, false, true);

        if (hasExtra(meta))
            return new TileEntityProxyCombo(false, false, false, true);

        return null;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(playerIn.getHeldItem(hand).getItem() instanceof ItemTooling){
            ItemTooling tool = (ItemTooling)playerIn.getHeldItem(hand).getItem();
            if(tool.getType() == ToolType.SCREWDRIVER || tool.getType() == ToolType.HAND_DRILL)
                return false;
        }
        return this.standardOpenBehavior(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, 0);
    }

    @Override
    public int[] getDimensions() {
        return new int[]{1, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x = x + dir.offsetX * o;
        z = z + dir.offsetZ * o;

        this.makeExtra(world, x + 1, y, z);
        this.makeExtra(world, x - 1, y, z);
        this.makeExtra(world, x, y, z + 1);
        this.makeExtra(world, x, y, z - 1);
        this.makeExtra(world, x, y + 1, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        this.addStandardInfo(tooltip);
    }

    @Override
    public void printHook(Pre event, World world, BlockPos pos) {
        BlockPos corePos = this.findCore(world, pos);
        if(corePos == null) {
            return;
        }

        TileEntity te = world.getTileEntity(corePos);

        if (!(te instanceof TileEntityHeaterOilburner))
            return;

        TileEntityHeaterOilburner heater = (TileEntityHeaterOilburner) te;

        List<String> text = new ArrayList();
        text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + heater.setting + " mB/t");
        FluidType type = heater.tank.getTankType();
        if(type.hasTrait(FT_Flammable.class)) {
            int heat = (int)(type.getTrait(FT_Flammable.class).getHeatEnergy() * heater.setting / 1000);
            text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + String.format(Locale.US, "%,d", heat) + " TU/t");
        }

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER && tool != ToolType.HAND_DRILL)
            return false;

        if (world.isRemote) return true;

        int[] pos = this.findCore(world, x, y, z);

        if (pos == null) return false;

        TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));

        if (!(te instanceof TileEntityHeaterOilburner)) return false;

        TileEntityHeaterOilburner tile = (TileEntityHeaterOilburner) te;
        if(tool == ToolType.SCREWDRIVER)
            tile.toggleSettingUp();
        else
            tile.toggleSettingDown();
        tile.markDirty();

        return true;
    }
}
