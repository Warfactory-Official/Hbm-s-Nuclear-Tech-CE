package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.BlockBase;
import com.hbm.items.IDynamicModels;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.render.extended_blockstate.PropertyRandomVariant;
import com.hbm.render.model.VariantBakedModel;
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
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

/**
 * This is a 1:1 Sellafield block ported from 1.7.10, but also allows for retrieval of any variant of the block in game, as it's not based on renderer
 * Only limitation is that I was not able to cram all the meta blocks into single block due to 4 bit restriction (it would have been 5 bits to do so),
 * hence a compromise of splitting all of them between different blocks, but allowing each block to have states with textures independent off of the
 * coordinates.
 *
 * @author MrNorwood
 */
public class BlockSellafieldSlaked extends BlockBase implements IDynamicModels {
    public static final BlockBakeFrame[] sellafieldTextures = new BlockBakeFrame[]{new BlockBakeFrame("sellafield_slaked"), new BlockBakeFrame("sellafield_slaked_1"), new BlockBakeFrame("sellafield_slaked_2"),
            new BlockBakeFrame("sellafield_slaked_3")};
    public static final int TEXTURE_VARIANTS = sellafieldTextures.length;
    public static final int META_COUNT = TEXTURE_VARIANTS;
    public static final PropertyInteger SHADE = PropertyInteger.create("shade", 0, 15);
    public static final IUnlistedProperty<Integer> VARIANT = new PropertyRandomVariant(sellafieldTextures.length);

    public BlockSellafieldSlaked(Material mat, SoundType type, String s) {
        super(mat, type, s);
        INSTANCES.add(this);
        this.setDefaultState(this.blockState.getBaseState().withProperty(SHADE, 0));
    }

    public static int getVariantForPos(BlockPos pos) {
        long l = (pos.getX() * 3129871L) ^ (long) pos.getY() * 116129781L ^ (long) pos.getZ();
        l = l * l * 42317861L + l * 11L;
        int i = (int) (l >> 16 & 3L);
        return Math.abs(i) % TEXTURE_VARIANTS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        Item item = Item.getItemFromBlock(this);
        for (int meta = 0; meta < META_COUNT; meta++) {
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(this.getRegistryName(), "inventory"));
        }
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extended = (IExtendedBlockState) state;
        int variantValue = getVariantForPos(pos);
        return extended.withProperty(VARIANT, variantValue);
    }


    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        var models = new IBakedModel[META_COUNT];
        for (int meta = 0; meta < META_COUNT; meta++) {
            BlockBakeFrame blockFrame = sellafieldTextures[meta % sellafieldTextures.length];
            try {
                IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation(blockFrame.getBaseModel()));
                ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();

                blockFrame.putTextures(textureMap);
                IModel retexturedModel = baseModel.retexture(textureMap.build());
                IBakedModel bakedModel = retexturedModel.bake(
                        ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
                );

                models[meta] = bakedModel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName(), "normal"), new VariantBakedModel(models, models[0], VARIANT));
        event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName(), "inventory"), models[0]);
    }


    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        int meta = stack.getMetadata();
        world.setBlockState(pos, state.withProperty(SHADE, meta), 3);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(
                this,
                new IProperty[]{SHADE},
                new IUnlistedProperty[]{VARIANT}
        );
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(SHADE);
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(SHADE, meta);
    }

    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        for (BlockBakeFrame frame : sellafieldTextures) {
            frame.registerBlockTextures(map);
        }
    }

    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
               return new ModelResourceLocation(state.getBlock().getRegistryName(), "normal");
            }
        };

    }

    @Override
    @SideOnly(Side.CLIENT)
    public IBlockColor getBlockColorHandler() {
        return (state, _, _, _) -> {
            int meta = state.getValue(SHADE);
            return Color.HSBtoRGB(0F, 0F, 1F - meta / 15F);
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IItemColor getItemColorHandler() {
        return (stack, _) -> {
            int meta = stack.getMetadata() & 15;
            return Color.HSBtoRGB(0F, 0F, 1F - meta / 15F);
        };
    }
}
