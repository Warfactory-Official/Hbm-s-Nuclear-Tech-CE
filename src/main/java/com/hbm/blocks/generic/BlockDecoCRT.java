package com.hbm.blocks.generic;

import com.hbm.Tags;
import com.hbm.blocks.BlockEnums;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.BlockDecoBakedModel;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDecoCRT extends BlockDecoModel<BlockEnums.DecoCRTEnum> {

    public BlockDecoCRT(Material mat, SoundType type, String registryName) {
        super(mat, type, registryName, BlockEnums.DecoCRTEnum.VALUES, false, true,
                new ResourceLocation(Tags.MODID, "models/blocks/crt.obj"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crt_clean"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crt_broken"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crt_blinking"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/crt_bsod"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        HFRWavefrontObject wavefront = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/crt.obj"));
        TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();

        String[] variants = new String[]{"crt_clean", "crt_broken", "crt_blinking", "crt_bsod"};
        for (int m = 0; m < variants.length; m++) {
            TextureAtlasSprite sprite = atlas.getAtlasSprite(new ResourceLocation(Tags.MODID, "blocks/" + variants[m]).toString());
            IBakedModel bakedWorld = BlockDecoBakedModel.forBlock(wavefront, sprite, -0.5F, -1);
            ModelResourceLocation mrlWorld = new ModelResourceLocation(getRegistryName(), "meta=" + m);
            event.getModelRegistry().putObject(mrlWorld, bakedWorld);

            IBakedModel bakedItem = new BlockDecoBakedModel(wavefront, sprite, false, 1.0F, 0.0F, -0.5F, 0.0F, 2);
            ModelResourceLocation mrlItem = new ModelResourceLocation(new ResourceLocation(Tags.MODID, getRegistryName().getPath() + "_item_" + m), "inventory");
            event.getModelRegistry().putObject(mrlItem, bakedItem);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                int meta = state.getValue(META) & 3;
                return new ModelResourceLocation(loc, "meta=" + meta);
            }
        };
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(META) & 3);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        Item item = Item.getItemFromBlock(this);
        int count = 4; // CLEAN, BROKEN, BLINKING, BSOD
        for (int m = 0; m < count; m++) {
            ModelResourceLocation inv = new ModelResourceLocation(new ResourceLocation(Tags.MODID, getRegistryName().getPath() + "_item_" + m), "inventory");
            ModelLoader.setCustomModelResourceLocation(item, m, inv);
        }
    }
}


