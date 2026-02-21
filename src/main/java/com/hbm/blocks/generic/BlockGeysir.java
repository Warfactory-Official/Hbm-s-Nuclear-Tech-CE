package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.deco.TileEntityGeysir;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockGeysir extends BlockContainer {

    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockGeysir(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(null);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        return new TileEntityGeysir();
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    public static void setState(IBlockState state, World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        worldIn.setBlockState(pos, state, 2);

        if (tileentity != null) {
            tileentity.validate();
            worldIn.setTileEntity(pos, tileentity);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull Random rand) {
        boolean active = stateIn.getValue(ACTIVE);

        if (this == ModBlocks.geysir_vapor && active) {
            float f = pos.getX() + 0.5F;
            float f1 = pos.getY() + 1.0F;
            float f2 = pos.getZ() + 0.5F;

            worldIn.spawnParticle(EnumParticleTypes.CLOUD, f, f1, f2, 0.0D, 0.1D, 0.0D);
        }
        if (this == ModBlocks.geysir_nether) {
            worldIn.spawnParticle(EnumParticleTypes.FLAME, pos.getX() + 0.5F, pos.getY() + 1.0625F, pos.getZ() + 0.5F, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTIVE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        boolean active = state.getValue(ACTIVE);
        return active ? 1 : 0;
    }

    @Override
    public @NotNull Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(ACTIVE, meta > 0);
    }

}
