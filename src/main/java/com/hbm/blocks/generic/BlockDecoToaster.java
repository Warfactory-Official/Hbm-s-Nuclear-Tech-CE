package com.hbm.blocks.generic;

import com.hbm.Tags;
import com.hbm.blocks.BlockEnums;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.BlockDecoBakedModel;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class BlockDecoToaster extends BlockDecoModel<BlockEnums.DecoToasterEnum> {

    public BlockDecoToaster(Material mat, SoundType type, String registryName) {
        super(mat, type, registryName, BlockEnums.DecoToasterEnum.VALUES, false, true,
                new ResourceLocation(Tags.MODID, "models/blocks/toaster.obj"));
        this.setBlockBoundsTo(0.25F, 0.0F, 0.375F, 0.75F, 0.325F, 0.625F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/toaster_iron"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/toaster_steel"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/toaster_wood"));
    }

    @Override
    public @NotNull IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                                     float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer, @NotNull EnumHand hand) {
        int i = MathHelper.floor(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        return this.getDefaultState().withProperty(META, (meta << 2) | i);
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        int meta = state.getValue(META);
        int rot = meta & 3;
        if(rot % 2 == 0) return new AxisAlignedBB(0.25F, 0.0F, 0.375F, 0.75F, 0.325F, 0.625F);
        else return new AxisAlignedBB(0.375F, 0.0F, 0.25F, 0.625F, 0.325F, 0.75F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        HFRWavefrontObject wavefront = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/toaster.obj"));
        TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();
        String[] variants = new String[]{"toaster_iron", "toaster_steel", "toaster_wood"};

        for (int m = 0; m < variants.length; m++) {
            TextureAtlasSprite sprite = atlas.getAtlasSprite(new ResourceLocation(Tags.MODID, "blocks/" + variants[m]).toString());
            IBakedModel bakedWorld = new BlockDecoBakedModel(wavefront, sprite, true, 1.0F, 0.0F, -0.5F, 0.0F, 0, true);

            for (int orient = 0; orient < 4; orient++) {
                int meta = (m << 2) | orient;
                ModelResourceLocation mrlWorld = new ModelResourceLocation(getRegistryName(), "meta=" + meta);
                event.getModelRegistry().putObject(mrlWorld, bakedWorld);
            }

            IBakedModel bakedItem = new BlockDecoBakedModel(wavefront, sprite, false, 1.0F, 0.0F, -0.25F, 0.0F, -1, true);
            ModelResourceLocation mrlItem = new ModelResourceLocation(new ResourceLocation(Tags.MODID, getRegistryName().getPath() + "_item_" + m), "inventory");
            event.getModelRegistry().putObject(mrlItem, bakedItem);
        }
    }

    @Override
    public @NotNull ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(META) >> 2);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return Collections.singletonList(new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(META) >> 2));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        Item item = Item.getItemFromBlock(this);
        int count = 3; // IRON, STEEL, WOOD
        for (int m = 0; m < count; m++) {
            ModelResourceLocation inv = new ModelResourceLocation(new ResourceLocation(Tags.MODID, getRegistryName().getPath() + "_item_" + m), "inventory");
            ModelLoader.setCustomModelResourceLocation(item, m, inv);
        }
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
    public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaDecoModelLow(meta, coordBaseMode);
    }
}