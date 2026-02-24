package com.hbm.items;

import com.hbm.Tags;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class BrokenItem extends ItemBakedBase {

    public BrokenItem(String s, String texturePath) {
        super(s, texturePath);
    }

    public BrokenItem(String s) {
        super(s);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull String getItemStackDisplayName(ItemStack stack) {
        if(stack.getTagCompound() == null) return super.getItemStackDisplayName(stack);

        String id = stack.getTagCompound().getString("itemID");
        int meta = stack.getTagCompound().getInteger("itemMeta");

        Item item = Item.getByNameOrId(id);
        if(item == null || item == net.minecraft.init.Items.AIR) return super.getItemStackDisplayName(stack);

        ItemStack sta = new ItemStack(item, 1, meta);
        return I18n.translateToLocalFormatted(this.getTranslationKey(stack) + ".prefix", sta.getDisplayName());
    }

    public static ItemStack make(ItemStack stack) {
        return make(stack.getItem(), stack.getCount(), stack.getItemDamage());
    }

    public static ItemStack make(Item item) {
        return make(item, 1, 0);
    }

    public static ItemStack make(Item item, int meta) {
        return make(item, 1, meta);
    }

    public static ItemStack make(Item item, int stacksize, int meta) {
        ItemStack stack = new ItemStack(ModItems.broken_item, stacksize);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("itemID", item.getRegistryName().toString());
        nbt.setInteger("itemMeta", meta);
        stack.setTagCompound(nbt);
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        super.bakeModel(event);
        try {
            ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + this.texturePath);
            ModelResourceLocation bakedModelLocation = new ModelResourceLocation(spriteLoc, "inventory");

            IBakedModel original = event.getModelRegistry().getObject(bakedModelLocation);
            if(original != null) {
                event.getModelRegistry().putObject(bakedModelLocation, new BrokenItemModel(original));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    private static class BrokenItemModel implements IBakedModel {
        private final IBakedModel baseModel;
        private final BrokenItemOverrideList overrides;

        public BrokenItemModel(IBakedModel baseModel) {
            this.baseModel = baseModel;
            this.overrides = new BrokenItemOverrideList(this);
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return baseModel.getQuads(state, side, rand);
        }

        @Override
        public boolean isAmbientOcclusion() { return baseModel.isAmbientOcclusion(); }

        @Override
        public boolean isGui3d() { return baseModel.isGui3d(); }

        @Override
        public boolean isBuiltInRenderer() { return baseModel.isBuiltInRenderer(); }

        @Override
        public @NotNull TextureAtlasSprite getParticleTexture() { return baseModel.getParticleTexture(); }

        @Override
        public @NotNull ItemCameraTransforms getItemCameraTransforms() { return baseModel.getItemCameraTransforms(); }

        @Override
        public @NotNull ItemOverrideList getOverrides() { return overrides; }

        @Override
        public @NotNull Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.@NotNull TransformType cameraTransformType) {
            return baseModel.handlePerspective(cameraTransformType);
        }
    }

    @SideOnly(Side.CLIENT)
    private static class BrokenItemOverrideList extends ItemOverrideList {
        private final BrokenItemModel parent;

        public BrokenItemOverrideList(BrokenItemModel parent) {
            super(java.util.Collections.emptyList());
            this.parent = parent;
        }

        @Override
        public @NotNull IBakedModel handleItemState(@NotNull IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            if(stack.getTagCompound() == null) return parent.baseModel;

            String id = stack.getTagCompound().getString("itemID");
            int meta = stack.getTagCompound().getInteger("itemMeta");

            Item item = Item.getByNameOrId(id);
            if(item == null || item == Items.AIR) return parent.baseModel;

            ItemStack innerStack = new ItemStack(item, 1, meta);
            IBakedModel innerModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(innerStack, world, entity);

            return new CompositeModel(innerModel, parent.baseModel);
        }
    }

    @SideOnly(Side.CLIENT)
    private static class CompositeModel implements IBakedModel {
        private final IBakedModel innerModel;
        private final IBakedModel overlayModel;

        public CompositeModel(IBakedModel innerModel, IBakedModel overlayModel) {
            this.innerModel = innerModel;
            this.overlayModel = overlayModel;
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            List<BakedQuad> quads = new ArrayList<>();
            quads.addAll(innerModel.getQuads(state, side, rand));
            quads.addAll(overlayModel.getQuads(state, side, rand));
            return quads;
        }

        @Override
        public boolean isAmbientOcclusion() { return overlayModel.isAmbientOcclusion(); }

        @Override
        public boolean isGui3d() { return overlayModel.isGui3d(); }

        @Override
        public boolean isBuiltInRenderer() { return overlayModel.isBuiltInRenderer(); }

        @Override
        public @NotNull TextureAtlasSprite getParticleTexture() { return overlayModel.getParticleTexture(); }

        @Override
        public @NotNull ItemCameraTransforms getItemCameraTransforms() { return innerModel.getItemCameraTransforms(); }

        @Override
        public @NotNull ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }

        @Override
        public @NotNull Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.@NotNull TransformType cameraTransformType) {
            Pair<? extends IBakedModel, Matrix4f> pair = innerModel.handlePerspective(cameraTransformType);
            return Pair.of(this, pair.getRight());
        }
    }
}