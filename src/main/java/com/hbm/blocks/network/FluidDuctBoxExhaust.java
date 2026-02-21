package com.hbm.blocks.network;

import com.hbm.Tags;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.render.model.DuctBakedModel;
import com.hbm.tileentity.network.TileEntityPipeExhaust;
import com.hbm.util.I18nUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class FluidDuctBoxExhaust extends FluidDuctBox {

    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconStraight;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconEnd;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconCurveTL;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconCurveTR;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconCurveBL;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[] iconCurveBR;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite[][] iconJunction;

    public FluidDuctBoxExhaust(String s) {
        super(s);
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            iconStraight = new TextureAtlasSprite[1];
            iconEnd = new TextureAtlasSprite[1];
            iconCurveTL = new TextureAtlasSprite[1];
            iconCurveTR = new TextureAtlasSprite[1];
            iconCurveBL = new TextureAtlasSprite[1];
            iconCurveBR = new TextureAtlasSprite[1];
            iconJunction = new TextureAtlasSprite[1][5];
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityPipeExhaust();
    }

    @Override
    protected boolean canConnectTo(IBlockAccess world, int x, int y, int z, EnumFacing dir, TileEntity tile) {
        return canConnectTo(world, x, y, z, dir, (FluidType) null);
    }

    @Override
    public boolean canConnectTo(IBlockAccess world, int x, int y, int z, EnumFacing dir, FluidType ignored) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockPos offset = pos.offset(dir);
        EnumFacing opposite = dir.getOpposite();
        return Library.canConnectFluid(world, offset, ForgeDirection.getOrientation(opposite), Fluids.SMOKE) ||
                Library.canConnectFluid(world, offset, ForgeDirection.getOrientation(opposite), Fluids.SMOKE_LEADED) ||
                Library.canConnectFluid(world, offset, ForgeDirection.getOrientation(opposite), Fluids.SMOKE_POISON);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == CreativeTabs.SEARCH || tab == this.getCreativeTab()) {
            for (int i = 0; i < 5; ++i) {
                items.add(new ItemStack(this, 1, i * 3));
            }
        }
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        List<String> text = new ArrayList<>();
        text.add(Fluids.SMOKE.getLocalizedName());
        text.add(Fluids.SMOKE_LEADED.getLocalizedName());
        text.add(Fluids.SMOKE_POISON.getLocalizedName());
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerSprite(TextureMap map) {
        iconStraight[0] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_exhaust_straight"));
        iconEnd[0] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_exhaust_end"));
        iconCurveTL[0] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_exhaust_curve_tl"));
        iconCurveTR[0] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_exhaust_curve_tr"));
        iconCurveBL[0] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_exhaust_curve_bl"));
        iconCurveBR[0] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_exhaust_curve_br"));
        for (int j = 0; j < 5; j++) {
            iconJunction[0][j] = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/boxduct_exhaust_junction_" + j));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel() {
        for (int i = 0; i < 5; i++) {
            int meta = i * 3;
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), meta, new ModelResourceLocation(this.getRegistryName(), "meta=" + meta));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void bakeModel(ModelBakeEvent event) {
        for (int i = 0; i < 5; i++) {
            int meta = i * 3;
            IBakedModel bakedModel = new DuctBakedModel(meta, true);
            ModelResourceLocation modelLocation = new ModelResourceLocation(this.getRegistryName(), "meta=" + meta);
            event.getModelRegistry().putObject(modelLocation, bakedModel);
        }
    }
}
