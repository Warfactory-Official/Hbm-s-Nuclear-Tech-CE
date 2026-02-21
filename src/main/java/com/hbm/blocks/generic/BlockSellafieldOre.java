package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IDynamicModels;
import com.hbm.items.IModelRegister;
import com.hbm.items.ModItems;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.render.icon.TextureAtlasSpriteMultipass;
import com.hbm.render.model.VariantBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockSellafieldOre extends BlockSellafieldSlaked implements ICustomBlockItem, IDynamicModels {

    public BlockEnums.OreType oreType;
    private final Random rand = new Random();

    public BlockSellafieldOre(String s, BlockEnums.OreType oreType) {
        super(Material.ROCK, SoundType.STONE, s);
        this.oreType = oreType;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (this == ModBlocks.ore_sellafield_diamond) return Items.DIAMOND;
        if (this == ModBlocks.ore_sellafield_emerald) return Items.EMERALD;
        if (this == ModBlocks.ore_sellafield_radgem) return ModItems.gem_rad;
        return Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random rand) {
        if (fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(getDefaultState(), rand, fortune)) {
            int j = rand.nextInt(fortune + 2) - 1;
            if (j < 0) j = 0;
            return this.quantityDropped(rand) * (j + 1);
        } else {
            return this.quantityDropped(rand);
        }
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        if (this.getItemDropped(state, rand, fortune) != Item.getItemFromBlock(this)) {
            int experience = 0;

            if (this == ModBlocks.ore_sellafield_diamond) experience = rand.nextInt(5) + 3;
            if (this == ModBlocks.ore_sellafield_emerald) experience = rand.nextInt(5) + 3;
            if (this == ModBlocks.ore_sellafield_radgem) experience = rand.nextInt(5) + 3;

            return experience;
        }
        return 0;
    }

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new BlockSellafieldOreItem(this);
        itemBlock.setRegistryName(this.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ((IModelRegister) Item.getItemFromBlock(this)).registerModels();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        for (int i = 0; i < sellafieldTextures.length; i++) {
            ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, BlockBakeFrame.ROOT_PATH + this.getRegistryName().getPath() + "_" + i);
            TextureAtlasSpriteMultipass layeredSprite = new TextureAtlasSpriteMultipass(spriteLoc.toString(), "blocks/" + sellafieldTextures[i].textureArray[0], "blocks/" + "ore_overlay_" + oreType.getName());
            map.setTextureEntry(layeredSprite);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        var models = new IBakedModel[META_COUNT];
        for (int i = 0; i < sellafieldTextures.length; i++) {
            BlockBakeFrame blockFrame = sellafieldTextures[i % sellafieldTextures.length];
            try {
                IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation(blockFrame.getBaseModel()));
                ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();

                ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, BlockBakeFrame.ROOT_PATH + this.getRegistryName().getPath() + "_" + i);
                textureMap.put("all", spriteLoc.toString());
                IModel retexturedModel = baseModel.retexture(textureMap.build());
                IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());

                models[i] = bakedModel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName(), "normal"), new VariantBakedModel(models, models[0], VARIANT));
        event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName(), "inventory"), models[0]);
    }

    private static class BlockSellafieldOreItem extends CustomBlockItem implements IModelRegister {
        private BlockSellafieldOreItem(Block block) {
            super(block);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void registerModels() {
            for (int meta = 0; meta < 16; meta++) {
                ModelLoader.setCustomModelResourceLocation(this, meta, new ModelResourceLocation(this.getRegistryName(), "inventory"));
            }
        }
    }
}
