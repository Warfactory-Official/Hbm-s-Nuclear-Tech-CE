package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDiFurnace;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@Deprecated
public class MachineDiFurnaceExtension extends BlockContainer {
    public MachineDiFurnaceExtension(Material mat, String s) {
        super(mat);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.machineTab);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityProxyCombo(true, false, false);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else if (!playerIn.isSneaking()) {
            TileEntity te = worldIn.getTileEntity(pos.down());

            if (te instanceof TileEntityDiFurnace) {
                playerIn.openGui(MainRegistry.instance, ModBlocks.guiID_test_difurnace, worldIn, pos.getX(), pos.getY() - 1, pos.getZ());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
