package com.hbm.items.tool;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.gui.GUITurretTargetFilter;
import com.hbm.items.ItemBakedBase;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class ItemTurretTargetFilter extends ItemBakedBase implements IGUIProvider {
    private TileEntityTurretBaseNT turret;

    public ItemTurretTargetFilter(String name) {
        super(name);

        setMaxStackSize(1);
    }

    @ParametersAreNonnullByDefault
    @Override
    public @NotNull EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!player.isSneaking()) {
            return EnumActionResult.PASS;
        }

        Block b = world.getBlockState(pos).getBlock();

        if (b instanceof BlockDummyable) {
            int[] corePos = ((BlockDummyable) b).findCore(world, pos.getX(), pos.getY(), pos.getZ());

            if (corePos == null) return EnumActionResult.FAIL;

            TileEntity tile = world.getTileEntity(new BlockPos(corePos[0], corePos[1], corePos[2]));

            if (tile instanceof TileEntityTurretBaseNT turretBaseNT) {
                this.turret = turretBaseNT;
                player.openGui(MainRegistry.instance, 0, world, 0, -1, 0);
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUITurretTargetFilter(turret);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World player, List<String> tooltip, @NotNull ITooltipFlag advanced) {
        tooltip.add(TextFormatting.GRAY + "Shift-click on turret to open filter list");
    }
}
