package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.ClientConfig;
import com.hbm.items.IDynamicModels;
import com.hbm.items.IModelRegister;
import com.hbm.render.model.BlockReedsBakedModel;
import com.hbm.tileentity.TileEntityReeds;
import com.hbm.util.UnlistedPropertyInteger;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

public class BlockReeds extends Block implements ICustomBlockItem, IDynamicModels {
    private static final String[] TEXTURES = new String[]{"bottom", "mid", "top"};

    public static final IUnlistedProperty<Integer> DEPTH = new UnlistedPropertyInteger("depth");

    @SideOnly(Side.CLIENT)
    private final TextureAtlasSprite[] sprites = new TextureAtlasSprite[TEXTURES.length];

    public BlockReeds(String regName) {
        super(Material.PLANTS);
        setRegistryName(regName);
        setTranslationKey(regName);

        setSoundType(SoundType.PLANT);

        IDynamicModels.INSTANCES.add(this);
        ModBlocks.ALL_BLOCKS.add(this);

        setDefaultState(blockState.getBaseState());
    }

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
        return new TileEntityReeds();
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{DEPTH});
    }

    @Override
    public @NotNull IBlockState getExtendedState(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
        if (!(state instanceof IExtendedBlockState ex)) return state;

        int depth = 0;

        if (!ClientConfig.RENDER_REEDS.get()) {
            depth = 1;
        } else {
            for (int i = pos.getY() - 1; i > 0; i--) {
                Block depthBlock = world.getBlockState(new BlockPos(pos.getX(), i, pos.getZ())).getBlock();

                depth = pos.getY() - i;
                if (depthBlock != Blocks.WATER && depthBlock != Blocks.FLOWING_WATER) {
                    break;
                }
            }
        }

        return ex.withProperty(DEPTH, depth);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        IBlockState state = worldIn.getBlockState(pos.down());
        Block block = state.getBlock();

        return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos) {
        checkAndDropBlock(world, pos);
    }

    protected void checkAndDropBlock(World world, BlockPos pos) {
        if (!canPlaceBlockAt(world, pos)) {
            world.setBlockToAir(pos);
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(@NotNull IBlockState blockState, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public @NotNull Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        return Items.STICK;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @NotNull BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
                return new ModelResourceLocation(loc, "normal");
            }
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        for (int i = 0; i < TEXTURES.length; i++) {
            sprites[i] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/reeds_" + TEXTURES[i]));
        }
    }

    @Override
    public void registerItem() {
        ItemBlock itemBlock = new BlockReedsItem(this);
        itemBlock.setRegistryName(Objects.requireNonNull(getRegistryName()));
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void bakeModel(ModelBakeEvent event) {
        try {
            ModelResourceLocation loc = new ModelResourceLocation(getRegistryName(), "normal");
            IBakedModel model = new BlockReedsBakedModel(this.sprites);
            event.getModelRegistry().putObject(loc, model);

            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
            IModel retexturedModel = baseModel.retexture(ImmutableMap.of("layer0", sprites[2].getIconName()));
            IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
            ModelResourceLocation itemLoc = new ModelResourceLocation(getRegistryName(), "inventory");
            event.getModelRegistry().putObject(itemLoc, bakedModel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    private static class BlockReedsItem extends ICustomBlockItem.CustomBlockItem implements IModelRegister {
        private BlockReedsItem(Block block) {
            super(block);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void registerModels() {
            ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory"));
        }
    }
}
