package com.hbm.blocks.machine;

import com.hbm.blocks.*;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.IMetaItemTesr;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityStirling;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MachineStirling extends BlockDummyable implements ILookOverlay, ITooltipProvider, IBlockMulti, IMetaItemTesr, ICustomBlockItem {

    public MachineStirling(String name) {
        super(Material.IRON, name);
        IMetaItemTesr.INSTANCES.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        if (meta >= 12)
            return new TileEntityStirling();

        if (meta >= extra)
            return new TileEntityProxyCombo(false, true, false);

        return null;
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
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x = x + dir.offsetX * o;
        z = z + dir.offsetZ * o;

        this.makeExtra(world, x + 1, y, z);
        this.makeExtra(world, x - 1, y, z);
        this.makeExtra(world, x, y, z + 1);
        this.makeExtra(world, x, y, z - 1);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (world.isRemote) {
            return true;

        } else if (!player.isSneaking()) {
            int[] pos = this.findCore(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());

            if (pos == null)
                return false;

            TileEntityStirling stirling = (TileEntityStirling) world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
            int meta = stirling.getGeatMeta();

            if (!stirling.hasCog && player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() == ModItems.gear_large && player.getHeldItem(EnumHand.MAIN_HAND).getItemDamage() == meta) {
                player.getHeldItem(EnumHand.MAIN_HAND).shrink(1);
                stirling.hasCog = true;
                stirling.markDirty();
                world.playSound(null, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.PLAYERS, 1.5F, 0.75F);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onBlockPlacedBy(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase player, @NotNull ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, player, itemStack);

        if (itemStack.getItemDamage() == 1) {
            EnumFacing dir = player.getHorizontalFacing().getOpposite();
            dir = getDirModified(dir);

            BlockPos offsetPos = pos.offset(dir, -getOffset());

            TileEntity te = world.getTileEntity(offsetPos);
            if (te instanceof TileEntityStirling) {
                ((TileEntityStirling) te).hasCog = false;
            }
        }
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (player.capabilities.isCreativeMode && world.getTileEntity(pos) instanceof TileEntityStirling stirling){
            stirling.setDestroyedByCreativePlayer();
        }
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityStirling stirling && stirling.shouldDrop()) {
            ItemStack itemstack = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, stirling.hasCog ? 0 : 1);
            Block.spawnAsEntity(worldIn, pos, itemstack);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World worldIn, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, findCoreTE(worldIn, pos) instanceof TileEntityStirling stirling && !stirling.hasCog ? 1 : 0);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    }

    @Override
    public void printHook(Pre event, World world, BlockPos pos) {

        BlockPos corePos = this.findCore(world, pos);

        if(corePos == null)
            return;

        TileEntity te = world.getTileEntity(corePos);

        if (!(te instanceof TileEntityStirling stirling))
            return;

        List<String> text = new ArrayList<>();
        text.add(stirling.heat + "TU/t");
        text.add((stirling.hasCog ? stirling.powerBuffer : 0) + "HE/t");

        if (this != ModBlocks.machine_stirling_creative) {
            int maxHeat = stirling.maxHeat();
            double percent = (double) stirling.heat / (double) maxHeat;
            int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int) (0xFF * percent) << 8);

            if (percent > 1D)
                color = 0xff0000;

            text.add("&[" + color + "&]" + ((stirling.heat * 1000 / maxHeat) / 10D) + "%");

            if (stirling.heat > maxHeat) {
                text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! OVERSPEED ! ! !");
            }

            if (!stirling.hasCog) {
                text.add("&[" + 0xff0000 + "&]Gear missing!");
            }
        }

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn){
        this.addStandardInfo(tooltip);
    }

    @Override
    public int getSubCount() {
        return 1;
    }

    @Override
    public int getSubitemCount() {
        return 2;
    }

    @Override
    public String getResourceLocationAsString() {
        return getRegistryName().toString();
    }

}
