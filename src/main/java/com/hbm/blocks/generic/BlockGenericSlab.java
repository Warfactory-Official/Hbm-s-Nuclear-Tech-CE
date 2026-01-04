package com.hbm.blocks.generic;

import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.List;
import java.util.Random;

public class BlockGenericSlab extends BlockSlab implements ICustomBlockItem {

    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

    private final boolean isDouble;
    private final BlockSlab singleBlock;

    public BlockGenericSlab(Material materialIn, Block singleBlock, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.isDouble = true;
        this.singleBlock = (BlockSlab) singleBlock;
        this.fullBlock = true;
        this.useNeighborBrightness = false;
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, Variant.DEFAULT));
        ModBlocks.ALL_BLOCKS.add(this);
    }

    public BlockGenericSlab(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.isDouble = false;
        this.singleBlock = null;
        this.fullBlock = false;
        this.useNeighborBrightness = true;
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, Variant.DEFAULT).withProperty(HALF, EnumBlockHalf.BOTTOM));
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        float hardness = this.getExplosionResistance(null);
        if (hardness > 50) {
            tooltip.add("§6" + I18nUtil.resolveKey("trait.blastres", hardness));
        }
    }

    @Override
    public String getTranslationKey(int meta) {
        return this.getTranslationKey();
    }


    @Override
    public boolean isDouble() {
        return isDouble;
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;

        if (!this.isDouble && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
            i |= 8;
        }

        return i;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, Variant.DEFAULT);

        if (!this.isDouble) {
            iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        }

        return iblockstate;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(isDouble ? singleBlock : this);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(isDouble ? singleBlock : this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return this.isDouble ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
    }

    @Override
    public void registerItem() {
        if (!isDouble) return;
        ItemSlab itemSlab = new ItemSlab(singleBlock, singleBlock, this);
        String path = getRegistryName().getPath().replace("_double_slab", "_slab");
        itemSlab.setRegistryName(new ResourceLocation(getRegistryName().getNamespace(), path));
        ForgeRegistries.ITEMS.register(itemSlab);
    }

    public enum Variant implements IStringSerializable {
        DEFAULT;

        public String getName() {
            return "default";
        }
    }
}
