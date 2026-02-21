package com.hbm.blocks.network.energy;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.network.energy.TileEntityPylonBase;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class PylonBase extends BlockContainer implements ITooltipProvider
{
    protected PylonBase(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        ModBlocks.ALL_BLOCKS.add(this);
    }
    
    public void breakBlock(World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPylonBase pylonBase) {
            pylonBase.disconnectAll();
        }
        super.breakBlock(world, pos, state);
    }
    
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }
    
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }
    
    public boolean isBlockNormalCube(@NotNull IBlockState state) {
        return false;
    }
    
    public boolean isNormalCube(@NotNull IBlockState state) {
        return false;
    }
    
    public boolean isNormalCube(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
        return false;
    }
    
    public boolean shouldSideBeRendered(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        return false;
    }
    
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {
        Collections.addAll(list, I18nUtil.resolveKeyArray(this.getTranslationKey() + ".desc"));
        super.addInformation(stack, worldIn, list, flagIn);
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(world.isRemote) {
            return true;
        } else if(!player.isSneaking()) {
            TileEntityPylonBase te = (TileEntityPylonBase) world.getTileEntity(pos);
            return te != null && te.setColor(player.getHeldItem(hand));
        } else {
            return false;
        }
    }
}