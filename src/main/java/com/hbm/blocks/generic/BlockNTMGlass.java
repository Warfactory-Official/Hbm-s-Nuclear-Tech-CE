package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockNTMGlass extends BlockBreakable implements IRadResistantBlock {

    BlockRenderLayer layer;
    private final boolean doesDrop;
    private final boolean isRadResistant;

    public BlockNTMGlass(Material materialIn, BlockRenderLayer layer, String s) {
        this(materialIn, layer, false, s);
    }

    public BlockNTMGlass(Material materialIn, BlockRenderLayer layer, boolean doesDrop, String s) {
        this(materialIn, layer, doesDrop, false, s);
    }

    public BlockNTMGlass(Material materialIn, BlockRenderLayer layer, boolean doesDrop, boolean isRadResistant, String s) {
        super(materialIn, false);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        setHarvestLevel("pickaxe", 0);
        this.layer = layer;
        this.doesDrop = doesDrop;
        this.isRadResistant = isRadResistant;
        lightOpacity = 0;
        translucent = true;
        ModBlocks.ALL_BLOCKS.add(this);
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
    public void onBlockAdded(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        if (this.isRadResistant) {
            RadiationSystemNT.markSectionForRebuild(worldIn, pos);
        }
        super.onBlockAdded(worldIn, pos, state);
    }

    @Override
    public boolean isBlockNormalCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        if (this.isRadResistant) {
            RadiationSystemNT.markSectionForRebuild(worldIn, pos);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public @NotNull BlockRenderLayer getRenderLayer() {
        return layer;
    }

    @Override
    public boolean isRadResistant(World worldIn, BlockPos blockPos) {
        return this.isRadResistant;
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
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
