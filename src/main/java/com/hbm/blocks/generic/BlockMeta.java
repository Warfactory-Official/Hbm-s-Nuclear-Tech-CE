package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.items.ClaimedModelLocationRegistry;
import com.hbm.items.IDynamicModels;
import com.hbm.items.IModelRegister;
import com.hbm.main.MainRegistry;
import com.hbm.main.client.NTMClientRegistry;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class BlockMeta extends BlockBase implements ICustomBlockItem, IDynamicModels, IMetaBlock {

    //Norwood:Yes you could use strings, enums or whatever, but this is much simpler and more efficient, as well as has exactly same scope as 1.7.10
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    protected short META_COUNT;
    protected boolean separateTranslationKeys = true;

    protected BlockBakeFrame[] blockFrames;
    protected boolean showMetaInCreative = true;

    public IBlockState getRandomState(Random rand){
        return this.getDefaultState().withProperty(META, rand.nextInt(META_COUNT));

    }


    public BlockMeta(Material m, String s) {
        super(m, s);
        INSTANCES.add(this);
        META_COUNT = 15;
    }

    public BlockMeta(Material m, String s, BlockBakeFrame... frame) {
        super(m, s);
        INSTANCES.add(this);
        blockFrames = frame;
        META_COUNT = (short) frame.length;
    }

    public BlockMeta(Material mat, SoundType type, String s, short metaCount) {
        super(mat, type, s);
        INSTANCES.add(this);
        META_COUNT = metaCount;
    }


    public BlockMeta(Material m, String s, short metaCount, boolean showMetaInCreative) {
        super(m, s);
        META_COUNT = metaCount;
        this.showMetaInCreative = showMetaInCreative;
        INSTANCES.add(this);
    }

    public BlockMeta(Material m, String s, short metaCount, boolean showMetaInCreative, BlockBakeFrame... frames) {
        super(m, s);
        META_COUNT = metaCount;
        this.showMetaInCreative = showMetaInCreative;
        this.blockFrames = frames;
        INSTANCES.add(this);
    }


    public BlockMeta(Material mat, SoundType type, String s, BlockBakeFrame... blockFrames) {
        this(mat, type, s, (short) blockFrames.length);
        this.blockFrames = blockFrames;
    }

    public BlockMeta(Material mat, SoundType type, String s, String... simpleModelTextures) {
        this(mat, type, s, (short) simpleModelTextures.length);
        this.blockFrames = BlockBakeFrame.simpleModelArray(simpleModelTextures);
    }

    protected boolean useSpecialRenderer() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        Item item = Item.getItemFromBlock(this);
        ModelResourceLocation syntheticLocation = NTMClientRegistry.getSyntheticTeisrModelLocation(item);
        if (syntheticLocation != null) {
            for (int meta = 0; meta < this.META_COUNT; meta++) {
                ModelLoader.setCustomModelResourceLocation(item, meta, syntheticLocation);
            }
            return;
        }
        if (useSpecialRenderer()) {
            for (int meta = 0; meta < this.META_COUNT; meta++) {
                ModelLoader.setCustomModelResourceLocation(item, meta,
                        new ModelResourceLocation(this.getRegistryName(), "inventory"));
            }
            return;
        }
        for (int meta = 0; meta < this.META_COUNT; meta++) {
            ModelLoader.setCustomModelResourceLocation(
                    item,
                    meta,
                    new ModelResourceLocation(this.getRegistryName(), "meta=" + meta)
            );
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        if (useSpecialRenderer()) return;
        if (blockFrames == null || blockFrames.length == 0) {
            MainRegistry.logger.error("No block frames defined for " + getRegistryName());
            throw new RuntimeException("No block frames defined for " + getRegistryName());
        }

        for (BlockBakeFrame frame : blockFrames) {
            frame.registerBlockTextures(map);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        if (useSpecialRenderer()) {
            try {
                IModel blockBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("block/cube_all"));
                ImmutableMap<String, String> blockTextures = ImmutableMap.of("all", "hbm:blocks/block_steel");
                IModel blockRetextured = blockBaseModel.retexture(blockTextures);
                IBakedModel blockBaked = blockRetextured.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
                ModelResourceLocation worldLocation = new ModelResourceLocation(getRegistryName(), "normal");
                event.getModelRegistry().putObject(worldLocation, blockBaked);
                if (!ClaimedModelLocationRegistry.hasSyntheticTeisrBinding(Item.getItemFromBlock(this))) {
                    IModel itemBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("item/generated"));
                    ImmutableMap<String, String> itemTextures = ImmutableMap.of("layer0", "hbm:blocks/" + getRegistryName().getPath());
                    IModel itemRetextured = itemBaseModel.retexture(itemTextures);
                    IBakedModel itemBaked = itemRetextured.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                    ModelResourceLocation inventoryLocation = new ModelResourceLocation(getRegistryName(), "inventory");
                    event.getModelRegistry().putObject(inventoryLocation, itemBaked);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        for (int meta = 0; meta < META_COUNT; meta++) {
            BlockBakeFrame blockFrame = blockFrames[meta % blockFrames.length];
            try {
                IModel baseModel = ModelLoaderRegistry.getModel(blockFrame.getBaseModelLocation());
                ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();

                blockFrame.putTextures(textureMap);
                IModel retexturedModel = baseModel.retexture(textureMap.build());
                IBakedModel bakedModel = retexturedModel.bake(
                        ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
                );

                ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "meta=" + meta);
                event.getModelRegistry().putObject(modelLocation, bakedModel);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    protected boolean getShowMetaInCreative(){
        return showMetaInCreative;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.addAll(this.getDrops(world, pos, state, fortune));
    }

    // mlbv: It's a BAD idea to override this method
    // Ideally we should override all three of getItemDropped, damageDropped, and quantityDropped
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int meta = state.getValue(META);
        return Arrays.asList(new ItemStack(Item.getItemFromBlock(this), 1, meta));
    }

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new MetaBlockItem(this, separateTranslationKeys);
        itemBlock.setRegistryName(this.getRegistryName());
        if (showMetaInCreative) itemBlock.setCreativeTab(this.getCreativeTab());
        ForgeRegistries.ITEMS.register(itemBlock);
    }


    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(META, meta);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(META));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{META});
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, meta);
    }
    // this shit prevents models from registering un-fucking-existent meta variants
    // like cap blocks which have 8 variants but SOMEHOW they register all 15
    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        if (useSpecialRenderer()) return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(loc, "normal");
            }
        };
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                int meta = state.getValue(META);
                if(meta < META_COUNT) return new ModelResourceLocation(loc, "meta=" + meta);
                else return new ModelResourceLocation(loc, "meta=" + 0);
            }
        };
    }


    public static class MetaBlockItem extends ItemBlock implements IModelRegister {
        BlockMeta metaBlock = (BlockMeta) this.block;
        boolean separateTranslationKeys = true;

        public MetaBlockItem(Block block) {
            super(block);
            this.hasSubtypes = true;
            this.canRepair = false;
        }

        public MetaBlockItem(Block block, boolean transationKeys) {
            super(block);
            this.hasSubtypes = true;
            this.canRepair = false;
            this.separateTranslationKeys = transationKeys;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
            if (this.isInCreativeTab(tab)) {
                for (int i = 0; i < metaBlock.META_COUNT; i++) {
                    list.add(new ItemStack(this, 1, i));
                }
            }
        }

        @Override
        public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
        {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getBlock();

            if (!block.isReplaceable(worldIn, pos))
            {
                pos = pos.offset(facing);
            }

            ItemStack itemstack = player.getHeldItem(hand);

            if (!itemstack.isEmpty() && player.canPlayerEdit(pos, facing, itemstack) && worldIn.mayPlace(this.block, pos, false, facing, player))
            {
                int i = itemstack.getItemDamage(); //dude this has to be vanilla bug;
                IBlockState iblockstate1 = this.block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, i, player, hand);

                if (placeBlockAt(itemstack, player, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1))
                {
                    iblockstate1 = worldIn.getBlockState(pos);
                    SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, player);
                    worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    itemstack.shrink(1);
                }

                return EnumActionResult.SUCCESS;
            }
            else
            {
                return EnumActionResult.FAIL;
            }
        }

        @Override
        public String getTranslationKey(ItemStack stack) {
            return separateTranslationKeys ? super.getTranslationKey() + "_" + stack.getItemDamage() : super.getTranslationKey();
        }


        @Override
        public void registerModels() {
            ModelResourceLocation syntheticLocation = NTMClientRegistry.getSyntheticTeisrModelLocation(this);
            if (syntheticLocation != null) {
                for (int meta = 0; meta < metaBlock.META_COUNT; meta++) {
                    ModelLoader.setCustomModelResourceLocation(this, meta, syntheticLocation);
                }
                return;
            }
            for (int meta = 0; meta < metaBlock.META_COUNT; meta++) {
                MainRegistry.logger.info("Registering model for " + this.block.getRegistryName() + " meta=" + meta);
                if (metaBlock.useSpecialRenderer()) {
                    ModelLoader.setCustomModelResourceLocation(this, meta, new ModelResourceLocation(this.getRegistryName(), "inventory"));
                } else {
                    ModelLoader.setCustomModelResourceLocation(this, meta, new ModelResourceLocation(this.getRegistryName(), "meta=" + meta));
                }
            }
        }
    }

}
