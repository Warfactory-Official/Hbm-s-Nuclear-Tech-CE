package com.hbm.blocks.machine;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class MachineDiesel extends BlockMachineBase implements ITooltipProvider {

    public MachineDiesel(Material materialIn, String regName) {
        super(materialIn, 0, regName);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        return new TileEntityMachineDiesel();
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        } else if (!player.isSneaking()) {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean rotatable() {
        return true;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        InventoryHelper.dropInventoryItems(worldIn, pos, worldIn.getTileEntity(pos));
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull Random rand) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityMachineDiesel diesel) {
            if (diesel.hasAcceptableFuel() && diesel.tank.getFill() > 0) {
                EnumFacing facing = EnumFacing.byIndex(tile.getBlockMetadata());
                EnumFacing rot = facing.rotateAround(EnumFacing.Axis.Y);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5 - facing.getXOffset() * 0.6 + rot.getXOffset() * 0.1875, pos.getY() + 0.3125, pos.getZ() + 0.5 - facing.getZOffset() * 0.6 + rot.getZOffset() * 0.1875, 0, 0, 0);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, World worldIn, List<String> list, @NotNull ITooltipFlag flagIn) {
        list.add(I18n.format("trait.fuelefficiency"));
        for (FuelGrade grade : FuelGrade.values()) {
            Double efficiency = TileEntityMachineDiesel.fuelEfficiency.get(grade);

            if (efficiency != null) {
                int eff = (int) (efficiency * 100);
                list.add(ChatFormatting.YELLOW + "-" + grade.getGrade() + ": " + ChatFormatting.RED + eff + "%");
            }
        }

        super.addInformation(stack, worldIn, list, flagIn);
    }
}
