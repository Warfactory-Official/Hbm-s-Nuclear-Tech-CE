package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockNTMGlassPane extends BlockPane implements IRadResistantBlock {
    private final boolean doesDrop;
    private final boolean isRadResistant;

    public BlockNTMGlassPane(Material material, boolean doesDrop, boolean isRadResistant, String regName) {
        super(material, doesDrop);
        setRegistryName(regName);
        setTranslationKey(regName);
        setHarvestLevel("pickaxe", 0);
        this.doesDrop = doesDrop;
        this.isRadResistant = isRadResistant;
        translucent = true;

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public boolean canPaneConnectTo(IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing dir) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        return super.canPaneConnectTo(world, pos, dir) || block instanceof BlockNTMGlass;
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        if (this.isRadResistant) {
            RadiationSystemNT.markSectionForRebuild(worldIn, pos);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public @NotNull BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public void getDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull IBlockState state, int fortune) {
    }

    @Override
    public boolean canSilkHarvest(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player) {
        return doesDrop;
    }

    @Override
    public @NotNull ItemStack getSilkTouchDrop(@NotNull IBlockState state) {
        return new ItemStack(this);
    }

    @Override
    public boolean isRadResistant(World worldIn, BlockPos blockPos) {
        return this.isRadResistant;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World player, @NotNull List<String> tooltip, @NotNull ITooltipFlag advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        float hardness = this.getExplosionResistance(null);
        if (this.isRadResistant) {
            tooltip.add("§2[" + I18nUtil.resolveKey("trait.radshield") + "]");
        }
        if (hardness > 50) {
            tooltip.add("§6" + I18nUtil.resolveKey("trait.blastres", hardness));
        }
    }
}
