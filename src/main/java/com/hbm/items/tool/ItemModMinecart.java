package com.hbm.items.tool;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.entity.cart.EntityMinecartCrate;
import com.hbm.entity.cart.EntityMinecartDestroyer;
import com.hbm.entity.cart.EntityMinecartOre;
import com.hbm.entity.cart.EntityMinecartPowder;
import com.hbm.entity.cart.EntityMinecartSemtex;
import com.hbm.items.IClaimedModelLocation;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ItemBase;
import com.hbm.items.ModItems;
import com.hbm.util.EnumUtil;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ItemModMinecart extends ItemBase implements IDynamicModels, IClaimedModelLocation {

    public static final String CART_BASE_NBT = "cartBase";

    public enum EnumCartBase {
        VANILLA,
        WOOD,
        STEEL,
        PAINTED
    }

    public enum EnumMinecart {
        EMPTY       (EnumCartBase.WOOD, EnumCartBase.STEEL, EnumCartBase.PAINTED),
        CRATE       (EnumCartBase.VANILLA),
        DESTROYER   (EnumCartBase.STEEL, EnumCartBase.PAINTED),
        POWDER      (EnumCartBase.WOOD, EnumCartBase.STEEL, EnumCartBase.PAINTED),
        SEMTEX      (EnumCartBase.WOOD, EnumCartBase.STEEL, EnumCartBase.PAINTED);

        public static final EnumMinecart[] VALUES = values();

        public int types;

        EnumMinecart(EnumCartBase... types) {
            this.types = 0;
            for(EnumCartBase type : types) {
                this.types |= (1 << type.ordinal());
            }
        }

        public boolean supportsBase(int type) {
            return (this.types & (1 << type)) > 0;
        }

        public boolean supportsBase(EnumCartBase type) {
            return supportsBase(type.ordinal());
        }
    }

    public ItemModMinecart(String s) {
        super(s);
        this.setMaxStackSize(4);
        this.setHasSubtypes(true);
        this.setCreativeTab(CreativeTabs.TRANSPORTATION);
        IDynamicModels.INSTANCES.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            for(EnumMinecart cart : EnumMinecart.VALUES) {
                EnumMap<EnumCartBase, IBakedModel> variants = new EnumMap<>(EnumCartBase.class);

                for(EnumCartBase base : EnumCartBase.values()) {
                    variants.put(base, layered(base, cart).bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter()));
                }

                event.getModelRegistry().putObject(modelLocation(cart), new CartBakedModel(variants));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    private static class CartBakedModel implements IBakedModel {

        private final EnumMap<EnumCartBase, IBakedModel> variants;
        private final IBakedModel fallback;
        private final ItemOverrideList overrides;

        private CartBakedModel(EnumMap<EnumCartBase, IBakedModel> variants) {
            this.variants = variants;
            this.fallback = variants.get(EnumCartBase.VANILLA);
            this.overrides = new ItemOverrideList(ImmutableList.of()) {
                @Override
                public @NotNull IBakedModel handleItemState(@NotNull IBakedModel original, @NotNull ItemStack stack,
                                                            World world, EntityLivingBase entity) {
                    IBakedModel model = CartBakedModel.this.variants.get(getBaseType(stack));
                    return model != null ? model : CartBakedModel.this.fallback;
                }
            };
        }

        @Override public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) { return fallback.getQuads(state, side, rand); }
        @Override public boolean isAmbientOcclusion() { return fallback.isAmbientOcclusion(); }
        @Override public boolean isGui3d() { return fallback.isGui3d(); }
        @Override public boolean isBuiltInRenderer() { return fallback.isBuiltInRenderer(); }
        @Override public @NotNull TextureAtlasSprite getParticleTexture() { return fallback.getParticleTexture(); }
        @Override public @NotNull ItemCameraTransforms getItemCameraTransforms() { return fallback.getItemCameraTransforms(); }
        @Override public @NotNull ItemOverrideList getOverrides() { return overrides; }
        @Override public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type) { return fallback.handlePerspective(type); }
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull ItemStack stack) {
        EnumMinecart cart = EnumUtil.grabEnumSafely(EnumMinecart.class, stack.getItemDamage());
        return super.getTranslationKey() + "." + cart.name().toLowerCase(Locale.US);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items) {
        if(!this.isInCreativeTab(tab)) return;

        for(EnumMinecart cart : EnumMinecart.VALUES) {
            for(EnumCartBase base : EnumCartBase.values()) {
                if(cart.supportsBase(base)) {
                    items.add(createCartItem(base, cart));
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        for(EnumMinecart cart : EnumMinecart.VALUES) {
            map.registerSprite(new ResourceLocation(Tags.MODID, "items/cart_overlay." + cart.name().toLowerCase(Locale.US)));
        }
        for(EnumCartBase base : EnumCartBase.values()) {
            map.registerSprite(new ResourceLocation(Tags.MODID, "items/cart." + base.name().toLowerCase(Locale.US)));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        for(EnumMinecart cart : EnumMinecart.VALUES) {
            ModelLoader.setCustomModelResourceLocation(this, cart.ordinal(), modelLocation(cart));
        }
    }

    @SideOnly(Side.CLIENT)
    private static ModelResourceLocation modelLocation(EnumMinecart cart) {
        return new ModelResourceLocation(new ResourceLocation(Tags.MODID, "items/cart_overlay." + cart.name().toLowerCase(Locale.US)), "inventory");
    }

    private static String baseTexture(EnumCartBase base) {
        return Tags.MODID + ":items/cart." + base.name().toLowerCase(Locale.US);
    }

    private static String overlayTexture(EnumMinecart cart) {
        return Tags.MODID + ":items/cart_overlay." + cart.name().toLowerCase(Locale.US);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean ownsModelLocation(ModelResourceLocation location) {
        for(EnumMinecart cart : EnumMinecart.VALUES) {
            if(location.equals(modelLocation(cart))) return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IModel loadModel(ModelResourceLocation location) {
        for(EnumMinecart cart : EnumMinecart.VALUES) {
            if(location.equals(modelLocation(cart))) {
                try {
                    return layered(EnumCartBase.VANILLA, cart);
                } catch(Exception e) {
                    return IClaimedModelLocation.super.loadModel(location);
                }
            }
        }
        return IClaimedModelLocation.super.loadModel(location);
    }

    @SideOnly(Side.CLIENT)
    private static IModel layered(EnumCartBase base, EnumMinecart cart) throws Exception {
        IModel generated = ModelLoaderRegistry.getModel(new ResourceLocation("item/generated"));
        return generated.retexture(ImmutableMap.of("layer0", baseTexture(base), "layer1", overlayTexture(cart)));
    }

    public static EnumCartBase getBaseType(ItemStack stack) {
        if(!stack.hasTagCompound()) return EnumCartBase.VANILLA;

        int meta = stack.getTagCompound().getInteger(CART_BASE_NBT);
        return EnumUtil.grabEnumSafely(EnumCartBase.class, meta);
    }

    public static ItemStack createCartItem(EnumCartBase base, EnumMinecart cart) {
        ItemStack stack = new ItemStack(ModItems.cart, 1, cart.ordinal());
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(CART_BASE_NBT, base.ordinal());
        stack.setTagCompound(nbt);
        return stack;
    }

    @Override
    public @NotNull EnumActionResult onItemUse(EntityPlayer player, World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float fx, float fy, float fz) {

        ItemStack stack = player.getHeldItem(hand);

        IBlockState state = world.getBlockState(pos);

        if(BlockRailBase.isRailBlock(state)) {
            if(!world.isRemote) {

                BlockRailBase.EnumRailDirection shape = state.getBlock() instanceof BlockRailBase
                        ? state.getValue(((BlockRailBase) state.getBlock()).getShapeProperty())
                        : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
                double ascent = shape.isAscending() ? 0.5D : 0.0D;

                EntityMinecart cart = createMinecart(world, pos.getX() + 0.5D, pos.getY() + 0.0625D + ascent, pos.getZ() + 0.5D, stack);

                if(stack.hasDisplayName()) {
                    cart.setCustomNameTag(stack.getDisplayName());
                }

                world.spawnEntity(cart);
            }

            stack.shrink(1);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    public static EntityMinecart createMinecart(World world, double x, double y, double z, ItemStack stack) {
        EnumMinecart type = EnumUtil.grabEnumSafely(EnumMinecart.class, stack.getItemDamage());
        EnumCartBase base = getBaseType(stack);
        switch(type) {
            case CRATE: return new EntityMinecartCrate(world, x, y, z, base, stack);
            case DESTROYER: return new EntityMinecartDestroyer(world, x, y, z, base);
            case POWDER: return new EntityMinecartPowder(world, x, y, z, base);
            case SEMTEX: return new EntityMinecartSemtex(world, x, y, z, base);
            default: return new EntityMinecartOre(world, x, y, z, base);
        }
    }
}
