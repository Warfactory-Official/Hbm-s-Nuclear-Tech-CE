package com.hbm.blocks.generic;

import com.hbm.Tags;
import com.hbm.items.IDynamicModels;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.BarrelBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BaseBarrel extends Block implements IDynamicModels {

    //Xirate: Either protected for use in extending classes or move it out to some BarrelConstants.
    public static final AxisAlignedBB BARREL_BB = new AxisAlignedBB(2 * 0.0625F, 0.0F, 2 * 0.0625F, 14 * 0.0625F, 1.0F, 14 * 0.0625F);

    protected BaseBarrel(Material blockMaterialIn) {
        super(blockMaterialIn);
        IDynamicModels.INSTANCES.add(this);
    }

    protected ResourceLocation getTextureLocation() {
        return new ResourceLocation(Tags.MODID, "blocks/barrel_" + getRegistryName().getPath().replace("_barrel", ""));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        map.registerSprite(getTextureLocation());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        HFRWavefrontObject wavefront = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/barrel.obj"));
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(getTextureLocation().toString());
        event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "normal"), BarrelBakedModel.forBlock(wavefront, sprite));
        event.getModelRegistry().putObject(new ModelResourceLocation(getRegistryName(), "inventory"), BarrelBakedModel.forItem(wavefront, sprite));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(loc, "normal");
            }
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BARREL_BB;
    }
}
