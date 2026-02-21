package com.hbm.items;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.util.EnumUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ItemEnumMulti<E extends Enum<E>> extends ItemBase implements IDynamicModels {

    public static final String ROOT_PATH = "items/";
    protected String[] textures;
    //hell yes, now we're thinking with enums!
    //assume the enum constants are constant
    protected E[] theEnum;
    protected boolean multiName;
    protected boolean multiTexture;

    public ItemEnumMulti(String registryName, E[] theEnum, boolean multiName, boolean multiTexture) {
        super(registryName);
        this.setHasSubtypes(true);
        this.theEnum = theEnum;
        this.multiName = multiName;
        this.multiTexture = multiTexture;
        INSTANCES.add(this);
        this.textures = Arrays.stream(theEnum)
                .map(Enum::name)
                .map(name -> registryName + getSeparationChar() + name.toLowerCase(Locale.US))
                .toArray(String[]::new);
    }

    public ItemEnumMulti(String registryName, E[] theEnum, boolean multiName, String texture) {
        super(registryName);
        this.setHasSubtypes(true);
        this.theEnum = theEnum;
        this.multiName = multiName;
        this.multiTexture = false;
        INSTANCES.add(this);
        this.textures = new String[]{texture};
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
            ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(new ResourceLocation(Tags.MODID, ROOT_PATH + (multiTexture ? textures[i] : textures[0])), "inventory"));
        }
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
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
            for (int i = 0; i < theEnum.length; i++) {
                String textureName = multiTexture ? textures[i] : textures[0];
                ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + textureName);

                IModel retexturedModel = baseModel.retexture(ImmutableMap.of("layer0", spriteLoc.toString()));
                IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                ModelResourceLocation bakedModelLocation = new ModelResourceLocation(spriteLoc, "inventory");
                event.getModelRegistry().putObject(bakedModelLocation, bakedModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Returns null when the wrong enum is passed. Only really used for recipes anyway so it's good.
     */
    public ItemStack stackFromEnum(int count, E num) {
        return new ItemStack(this, count, num.ordinal());
    }

    public ItemStack stackFromEnum(E num) {
        return stackFromEnum(1, num);
    }

    public boolean isMultiTexture() {
        return multiTexture;
    }

    @Override
    public Item setTranslationKey(String unlocalizedName) {
        super.setTranslationKey(unlocalizedName);
        return this;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if (multiName) {
            E num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
            return super.getTranslationKey() + getSeparationChar() + num.name().toLowerCase(Locale.US);
        } else {
            return super.getTranslationKey(stack);
        }
    }

    @Override
    public ItemEnumMulti<E> setCreativeTab(CreativeTabs tab) {
        //noinspection unchecked
        return (ItemEnumMulti<E>) super.setCreativeTab(tab);
    }

    protected String getSeparationChar() {
        return ".";
    }
    // srsly guys, do you think I'd create separate classes just for the sake of description?
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if(this == ModItems.item_expensive) tooltip.add(TextFormatting.RED + "Expensive mode item");
    }
}
