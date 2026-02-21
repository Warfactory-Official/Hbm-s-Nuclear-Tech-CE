package com.hbm.items;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.util.EnumUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Locale;

/**
 * Copy of ItemEnumMulti because we don't have multi-inheritance in Java.
 */
public class ItemEnumMultiFood<E extends Enum<E> & ItemEnumMultiFood.FoodSpec> extends ItemFood implements IDynamicModels {

    public static final String ROOT_PATH = "items/";
    protected final E[] theEnum;
    protected final boolean multiName;
    protected final boolean multiTexture;
    protected final String[] textures;

    public ItemEnumMultiFood(String registryName, E[] theEnum, boolean multiName, boolean multiTexture) {
        super(0, 0.0F, false);
        this.setHasSubtypes(true);
        this.setRegistryName(new ResourceLocation(Tags.MODID, registryName));
        this.setTranslationKey(registryName);
        this.theEnum = theEnum;
        this.multiName = multiName;
        this.multiTexture = multiTexture;
        this.textures = Arrays.stream(theEnum).map(Enum::name)
                              .map(name -> getPrefix() + getSeparator() + name.toLowerCase(Locale.US)).toArray(String[]::new);
        ModItems.ALL_ITEMS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    public ItemEnumMultiFood(String registryName, E[] theEnum, boolean multiName, String texture) {
        super(0, 0.0F, false);
        this.setHasSubtypes(true);
        this.setRegistryName(new ResourceLocation(Tags.MODID, registryName));
        this.setTranslationKey(registryName);
        this.theEnum = theEnum;
        this.multiName = multiName;
        this.multiTexture = false;
        this.textures = new String[]{texture};
        ModItems.ALL_ITEMS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    protected String getSeparator() {
        return ".";
    }

    protected String getPrefix() {
        return super.getTranslationKey().substring(5);
    }

    protected E getVariant(ItemStack stack) {
        return EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
    }

    public ItemStack stackFromEnum(int count, E variant) {
        if (variant == null) return ItemStack.EMPTY;
        return new ItemStack(this, count, variant.ordinal());
    }

    public ItemStack stackFromEnum(E variant) {
        return stackFromEnum(1, variant);
    }

    @Override
    public int getHealAmount(ItemStack stack) {
        E v = getVariant(stack);
        return v != null ? v.foodLevel() : 0;
    }

    @Override
    public float getSaturationModifier(ItemStack stack) {
        E v = getVariant(stack);
        return v != null ? v.saturation() : 0.0F;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        E v = getVariant(stack);
        boolean always = v != null && v.alwaysEdible();
        if (playerIn.canEat(always)) {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    public boolean isWolfsFavoriteMeat() {
        for (E v : theEnum) {
            if (v.wolfFood()) return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (int i = 0; i < theEnum.length; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        for (String texture : textures) {
            map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + texture));
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerModel() {
        for (int i = 0; i < theEnum.length; i++) {
            String tex = multiTexture ? textures[i] : textures[0];
            ModelLoader.setCustomModelResourceLocation(this, i,
                    new ModelResourceLocation(new ResourceLocation(Tags.MODID, ROOT_PATH + tex), "inventory"));
        }
    }

    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
            for (int i = 0; i < theEnum.length; i++) {
                String texName = multiTexture ? textures[i] : textures[0];
                ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + texName);
                IModel retextured = baseModel.retexture(ImmutableMap.of("layer0", spriteLoc.toString()));
                IBakedModel baked = retextured.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                ModelResourceLocation bakedLoc = new ModelResourceLocation(spriteLoc, "inventory");
                event.getModelRegistry().putObject(bakedLoc, baked);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if (!multiName) return super.getTranslationKey(stack);
        E v = getVariant(stack);
        if (v == null) return super.getTranslationKey(stack);
        return "item." + getPrefix() + getSeparator() + v.name().toLowerCase(Locale.US);
    }

    @Override
    public ItemEnumMultiFood<E> setCreativeTab(CreativeTabs tab) {
        // noinspection unchecked
        return (ItemEnumMultiFood<E>) super.setCreativeTab(tab);
    }

    public interface FoodSpec {
        int foodLevel();

        float saturation();

        default boolean wolfFood() {
            return false;
        }

        default boolean alwaysEdible() {
            return false;
        }
    }
}
