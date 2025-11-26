package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.IDynamicModels;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;

import java.util.List;

public class BlockGlyph extends Block implements IDynamicModels {

    public static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 15);

    public BlockGlyph(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH) {
            for (int i = 0; i < 16; ++i) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
        super.onExplosionDestroy(worldIn, pos, explosionIn);
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag advanced) {
        switch (stack.getItemDamage()) {
            case 0:
                list.add("Hourglass");
                break;
            case 1:
                list.add("Eye");
                break;
            case 2:
                list.add("'Pillar'");
                break;
            case 3:
                list.add("IOI");
                break;
            case 4:
                list.add("Delta");
                break;
            case 5:
                list.add("VTPC");
                break;
            case 6:
                list.add("Cool S");
                break;
            case 7:
                list.add("Trefoil");
                break;
            case 8:
                list.add("Pony");
                break;
            case 9:
                list.add("Sparkle");
                break;
            case 10:
                list.add("PiP");
                break;
            case 11:
                list.add("Triangles");
                break;
            case 12:
                list.add("Linux Mint");
                break;
            case 13:
                list.add("13");
                break;
            case 14:
                list.add("Digamma");
                break;
            case 15:
                list.add("Celestial Altar");
                break;
        }
    }

    //Drillgon200: AAAAAAAAAAAAAAAAAAA 1.12.2 always places the block with metadata 0 by default!
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        ItemStack itemstack = placer.getHeldItem(hand);
        if (itemstack.getItem() == Item.getItemFromBlock(this)) {
            return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, itemstack.getMetadata(), placer, hand);
        }
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{TYPE});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, meta);
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {

    }

    @Override
    public void registerModel() {
            for (int i = 0; i < 16; i++)
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), i, new ModelResourceLocation(this.getRegistryName().toString() + i, "inventory"));
    }

    @Override
    public void registerSprite(TextureMap map) {

    }
}
