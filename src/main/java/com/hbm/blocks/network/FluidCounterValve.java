package com.hbm.blocks.network;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IDynamicModels;
import com.hbm.tileentity.network.TileEntityFluidCounterValve;
import com.hbm.util.I18nUtil;
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
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
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

public class FluidCounterValve extends FluidDuctBase implements IDynamicModels, ILookOverlay, ITooltipProvider {

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 1);

    public FluidCounterValve(Material mat, String name) {
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
        return new TileEntityFluidCounterValve();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/fluid_counter_valve_on"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/fluid_counter_valve_off"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube_all"));
            ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();

            textureMap.put("all", new ResourceLocation(Tags.MODID, "blocks/fluid_counter_valve_off").toString());
            textureMap.put("particle", new ResourceLocation(Tags.MODID, "blocks/fluid_counter_valve_off").toString());

            IModel retexturedModel = baseModel.retexture(textureMap.build());
            IBakedModel bakedModelOff = retexturedModel.bake(
                    ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter()
            );

            textureMap = ImmutableMap.builder();
            textureMap.put("all", new ResourceLocation(Tags.MODID, "blocks/fluid_counter_valve_on").toString());
            textureMap.put("particle", new ResourceLocation(Tags.MODID, "blocks/fluid_counter_valve_on").toString());

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

        if(!(te instanceof TileEntityFluidCounterValve))
            return;

        TileEntityFluidCounterValve duct = (TileEntityFluidCounterValve) te;

        List<String> text = new ArrayList<>();
        text.add("&[" + duct.getType().getColor() + "&]" + duct.getType().getLocalizedName());
        text.add("Counter: " + duct.getCounter());
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
        this.addStandardInfo(list);
    }
}

