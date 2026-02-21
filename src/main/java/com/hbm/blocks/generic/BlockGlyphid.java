package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Random;

public class BlockGlyphid extends Block implements IBlockMulti {
    public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);

    public BlockGlyphid(Material m, String s) {
        super(m);
        this.setRegistryName(s);
        this.setTranslationKey(s);
        this.setCreativeTab(MainRegistry.blockTab);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, Type.BASE));

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public int getSubCount() {
        return 3;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE).ordinal();
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, Type.VALUES[meta]);
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE);
    }

    public enum Type implements IStringSerializable {
        BASE, INFESTED, RAD;

        public static final Type[] VALUES = values();

        @Override
        public @NotNull String getName() {
            return name().toLowerCase(Locale.US);
        }
    }
}
