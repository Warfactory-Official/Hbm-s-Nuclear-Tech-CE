package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.items.IDynamicModels;
import com.hbm.items.IModelRegister;
import com.hbm.main.MainRegistry;
import com.hbm.render.icon.TextureAtlasSpriteMultipass;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;
import java.util.Random;

import static com.hbm.blocks.BlockEnums.OreType;
import static com.hbm.blocks.OreEnumUtil.OreEnum;

//MrNorwood: Welp, and I made it backwards. No biggie, this is still incredibly useful
//Overengieered it award
public class BlockOreMeta extends BlockMeta implements IDynamicModels, ICustomBlockItem {

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    public final short META_COUNT;
    public final boolean showMetaInCreative;
    public final String baseTextureName;
    public final OreType[] overlays;


    public BlockOreMeta(Material material, String name, String baseTexture, OreType... overlays) {
        super(material, name);
        this.baseTextureName = baseTexture;
        this.overlays = overlays;
        META_COUNT = (short) overlays.length;
        INSTANCES.add(this);
        showMetaInCreative = true;
    }

    @SideOnly(Side.CLIENT)
    public void registerModel() {
            for (int meta = 0; meta <= this.META_COUNT; meta++) {
                ModelLoader.setCustomModelResourceLocation(
                        Item.getItemFromBlock(this),
                        meta,
                        new ModelResourceLocation(this.getRegistryName(), "meta=" + meta)
                );
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, META);
    }

    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        for (OreType overlay : this.overlays) {
            ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, "blocks/" + this.getRegistryName().getPath() + "-" + "ore_overlay_" + overlay.getName());
            TextureAtlasSpriteMultipass layeredSprite = new TextureAtlasSpriteMultipass(spriteLoc.toString(), "blocks/" + baseTextureName, "blocks/" + "ore_overlay_" + overlay);
            map.setTextureEntry(layeredSprite);
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, meta);
    }

//    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
//        Random rand = ((World) world).rand;
//        int meta = state.getValue(META);
//        OreType typeEnum = this.overlays[meta];
//        if(typeEnum.getDrop() == null)
//            return Collections.singletonList(new ItemStack(Item.getItemFromBlock(this), 1, meta));
//
//        return Collections.singletonList(new ItemStack(typeEnum.getDrop().getItem(), typeEnum.getDropCount(rand.nextInt(fortune+1)), typeEnum.drop.getMetadata()));
//    }

    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {

        OreEnum oreEnum = overlays[state.getValue(META)].oreEnum;
        Random rand = world instanceof World ? ((World) world).rand : RANDOM;


        int count = (oreEnum == null) ? quantityDropped(state, fortune, rand) : oreEnum.quantityFunction.apply(state, fortune, rand);

        for (int i = 0; i < count; i++) {
            ItemStack droppedItem;

            if (oreEnum == null) {
                droppedItem = new ItemStack(this.getItemDropped(state, rand, fortune), 1, this.damageDropped(state));
            } else {
                droppedItem = oreEnum.dropFunction.apply(state, rand);
            }

            if (droppedItem.getItem() != Items.AIR) {
                drops.add(droppedItem);
            }
        }
    }

    public void registerItem() {
        ItemBlock itemBlock = new BlockOreMeta.MetaBlockOreItem(this);
        itemBlock.setRegistryName(Objects.requireNonNull(this.getRegistryName()));
        if (showMetaInCreative) itemBlock.setCreativeTab(this.getCreativeTab());
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        int meta = stack.getMetadata();
        world.setBlockState(pos, this.getStateFromMeta(meta), 3);
    }

    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube_all"));

            for (int meta = 0; meta <= META_COUNT - 1; meta++) {
                ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();
                String overlay = overlays[meta % overlays.length].getName();
                ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, "blocks/" + this.getRegistryName().getPath() + "-" + "ore_overlay_" + overlay);

                // Base texture
                textureMap.put("all", spriteLoc.toString());


                IModel retexturedModel = baseModel.retexture(textureMap.build());
                IBakedModel bakedModel = retexturedModel.bake(
                        ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
                );

                ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "meta=" + meta);
                event.getModelRegistry().putObject(modelLocation, bakedModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MetaBlockOreItem extends ItemBlock implements IModelRegister {
        BlockOreMeta metaBlock = (BlockOreMeta) this.block;

        public MetaBlockOreItem(Block block) {
            super(block);
            this.hasSubtypes = true;
            this.canRepair = false;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
            if (this.isInCreativeTab(tab)) {
                for (int i = 0; i <= metaBlock.META_COUNT - 1; i++) {
                    list.add(new ItemStack(this, 1, i));
                }
            }
        }

        @Override
        public String getTranslationKey(ItemStack stack) {
            return super.getTranslationKey() + "_" + stack.getItemDamage();
        }


        @Override
        public void registerModels() {
            for (int meta = 0; meta <= metaBlock.META_COUNT - 1; meta++) {
                MainRegistry.logger.info("Registering model for " + this.block.getRegistryName() + " meta=" + meta);
                ModelLoader.setCustomModelResourceLocation(this, meta,
                        new ModelResourceLocation(this.getRegistryName(), "meta=" + meta));
            }
        }
    }


}
