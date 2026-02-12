package com.hbm.blocks.network;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IDynamicModels;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.network.TileEntityFluidValve;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class FluidSwitch extends FluidDuctBase implements IDynamicModels, ILookOverlay {

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 1);

    public FluidSwitch(Material mat, String name) {
        super(mat);
        setRegistryName(Tags.MODID, name);
        setTranslationKey(name);
        this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));
        useNeighborBrightness = true;
        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, META);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, meta % 2);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityFluidValve();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/fluid_switch_on"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/fluid_switch_off"));
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (worldIn.isRemote) return;

        boolean on = worldIn.isBlockPowered(pos);
        int meta = state.getValue(META);

        boolean update = false;

        if (on && meta == 0) {
            worldIn.setBlockState(pos, state.withProperty(META, 1), 2);
            worldIn.playSound(null, pos, HBMSoundHandler.reactorStart, SoundCategory.BLOCKS, 1.0F, 1.0F);
            update = true;
        } else if (!on && meta == 1) {
            worldIn.setBlockState(pos, state.withProperty(META, 0), 2);
            worldIn.playSound(null, pos, HBMSoundHandler.reactorStart, SoundCategory.BLOCKS, 1.0F, 0.85F);
            update = true;
        }

        if(update) {
            TileEntityFluidValve te = (TileEntityFluidValve) worldIn.getTileEntity(pos);
            te.updateState();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube_all"));
            ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();

            textureMap.put("all", new ResourceLocation(Tags.MODID, "blocks/fluid_switch_off").toString());
            textureMap.put("particle", new ResourceLocation(Tags.MODID, "blocks/fluid_switch_off").toString());

            IModel retexturedModel = baseModel.retexture(textureMap.build());
            IBakedModel bakedModelOff = retexturedModel.bake(
                    ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
            );

            textureMap = ImmutableMap.builder();
            textureMap.put("all", new ResourceLocation(Tags.MODID, "blocks/fluid_switch_on").toString());
            textureMap.put("particle", new ResourceLocation(Tags.MODID, "blocks/fluid_switch_on").toString());

            retexturedModel = baseModel.retexture(textureMap.build());
            IBakedModel bakedModelOn = retexturedModel.bake(
                    ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
            );

            ModelResourceLocation modelLocationOff = new ModelResourceLocation(this.getRegistryName(), "meta=0");
            event.getModelRegistry().putObject(modelLocationOff, bakedModelOff);

            ModelResourceLocation modelLocationOn = new ModelResourceLocation(this.getRegistryName(), "meta=1");
            event.getModelRegistry().putObject(modelLocationOn, bakedModelOn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "meta=0"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 1, new ModelResourceLocation(this.getRegistryName(), "meta=1"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(loc, "meta=" + state.getValue(META));
            }
        };
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);

        if(!(te instanceof TileEntityFluidValve duct))
            return;

        List<String> text = new ArrayList<>();
        text.add("&[" + duct.getType().getColor() + "&]" + duct.getType().getLocalizedName());
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
}

