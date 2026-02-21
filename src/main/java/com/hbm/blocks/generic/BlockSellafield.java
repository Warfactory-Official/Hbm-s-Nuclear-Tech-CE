package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.items.IDynamicModels;
import com.hbm.potion.HbmPotion;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.render.extended_blockstate.PropertyRandomVariant;
import com.hbm.render.icon.RGBMutatorInterpolatedComponentRemap;
import com.hbm.render.icon.TextureAtlasSpriteMutatable;
import com.hbm.render.model.VariantBakedModel;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
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

import java.util.Random;

import static com.hbm.blocks.generic.BlockSellafieldSlaked.getVariantForPos;
import static com.hbm.blocks.generic.BlockSellafieldSlaked.sellafieldTextures;
import static com.hbm.render.block.BlockBakeFrame.ROOT_PATH;

/**
 * See parent class for more detailed info. Since I could not cram all the data into single item due to 4bit block data
 * restriction, check out getSellafiteFromLvl and getLvlfromSellafite, they should make it much closer in behavior to
 * what 1.7 can get away with.
 *
 * @author MrNorwood
 */
public class BlockSellafield extends BlockMeta implements IDynamicModels {

    public static final IUnlistedProperty<Integer> VARIANT = new PropertyRandomVariant(sellafieldTextures.length);
    private static final short LEVELS = 6;
    private static final float rad = 0.5f;
    private static final int[][] colors = new int[][]{
            {0x4C7939, 0x41463F},
            {0x418223, 0x3E443B},
            {0x338C0E, 0x3B5431},
            {0x1C9E00, 0x394733},
            {0x02B200, 0x37492F},
            {0x00D300, 0x324C26}
    };


    public BlockSellafield(Material mat, SoundType type, String s) {
        super(mat, type, s, LEVELS);
        this.showMetaInCreative = true;
        this.needsRandomTick = true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{META}, new IUnlistedProperty[]{VARIANT});

    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extended = (IExtendedBlockState) state;
        int variantValue = getVariantForPos(pos);
        return extended.withProperty(VARIANT, variantValue);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        int level = worldIn.getBlockState(pos).getValue(META);
        if (entityIn instanceof EntityLivingBase livingBase) {
            livingBase.addPotionEffect(new PotionEffect(HbmPotion.radiation, 30 * 20, level < 5 ? level : level * 2));
            if (level >= 3)
                entityIn.setFire(level);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) return;
        IBlockState currentState = world.getBlockState(pos);
        int level = currentState.getValue(META);
        float netRad = rad * (level + 1);
        ChunkRadiationManager.proxy.incrementRad(world, pos, netRad);

        if (rand.nextInt(level == 0 ? 25 : 15) == 0) {
            if (level > 0)
                world.setBlockState(pos, ModBlocks.sellafield.getDefaultState().withProperty(META, level - 1), 2);
            else
                world.setBlockState(pos, ModBlocks.sellafield_slaked.getDefaultState(), 3);
        }
    }


    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        for (int level = 0; level < LEVELS; level++) {
            int[] tint = colors[level];

            for (BlockBakeFrame texture : sellafieldTextures) {
                ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + texture.textureArray[0] + "-" + level);
                TextureAtlasSpriteMutatable mutatedTexture = new TextureAtlasSpriteMutatable(spriteLoc.toString(), new RGBMutatorInterpolatedComponentRemap(0x858384, 0x434343, tint[0], tint[1]));
                map.setTextureEntry(mutatedTexture);
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        for (int level = 0; level < LEVELS; level++) {
            var models = new IBakedModel[4];
            for (int variant = 0; variant < 4; variant++) {
                IModel baseModel = ModelLoaderRegistry.getModelOrMissing(new ResourceLocation(sellafieldTextures[0].getBaseModel()));
                ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();
                textureMap.put("all", new ResourceLocation(Tags.MODID, ROOT_PATH) + sellafieldTextures[variant].textureArray[0] + "-" + level);

                IModel retexturedModel = baseModel.retexture(textureMap.build());
                IBakedModel bakedModel = retexturedModel.bake(
                        ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
                );

                models[variant] = bakedModel;
            }
            event.getModelRegistry().putObject(new ModelResourceLocation(this.getRegistryName(), "meta=" + level), new VariantBakedModel(models, models[0], VARIANT));
        }

    }

}