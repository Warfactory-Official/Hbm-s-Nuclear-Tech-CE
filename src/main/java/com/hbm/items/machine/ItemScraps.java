package com.hbm.items.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemAutogen;
import com.hbm.util.I18nUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class ItemScraps extends ItemAutogen {

    public ItemScraps(String s) {
        super(null, s);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (this.isInCreativeTab(tab)) {
            for (NTMMaterial mat : Mats.orderedList) {
                if (mat.smeltable == NTMMaterial.SmeltingBehavior.SMELTABLE || mat.smeltable == NTMMaterial.SmeltingBehavior.ADDITIVE) {
                    list.add(new ItemStack(this, 1, mat.id));
                }
            }
        }
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        super.registerSprite(map);
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/scraps_liquid"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "items/scraps_additive"));
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
            for (NTMMaterial mat : Mats.orderedList) {
                if (mat.smeltable == NTMMaterial.SmeltingBehavior.SMELTABLE
                        || mat.smeltable == NTMMaterial.SmeltingBehavior.ADDITIVE) {
                    String pathIn = getTexturePath(mat);
                    ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, pathIn);
                    IModel retexturedModel = baseModel.retexture(ImmutableMap.of("layer0", spriteLoc.toString()));
                    IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                    ModelResourceLocation bakedModelLocation =
                            new ModelResourceLocation(new ResourceLocation(Tags.MODID, pathIn), "inventory");
                    event.getModelRegistry().putObject(bakedModelLocation, bakedModel);
                }
            }

            for (String extra : new String[] { "items/scraps_liquid", "items/scraps_additive" }) {
                ResourceLocation spriteLoc = new ResourceLocation(Tags.MODID, extra);
                IModel retexturedModel = baseModel.retexture(ImmutableMap.of("layer0", spriteLoc.toString()));
                IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                ModelResourceLocation bakedModelLocation =
                        new ModelResourceLocation(new ResourceLocation(Tags.MODID, extra), "inventory");
                event.getModelRegistry().putObject(bakedModelLocation, bakedModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        List<ResourceLocation> variants = new ArrayList<>();
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(new ResourceLocation(Tags.MODID, "items/scraps-stone"), "inventory"));
        for (NTMMaterial mat : Mats.orderedList) {
            if (mat.smeltable == NTMMaterial.SmeltingBehavior.SMELTABLE
                    || mat.smeltable == NTMMaterial.SmeltingBehavior.ADDITIVE) {
                variants.add(new ResourceLocation(Tags.MODID, getTexturePath(mat))); // items/<reg>-<matname>
            }
        }

        variants.add(new ResourceLocation(Tags.MODID, "items/scraps_liquid"));
        variants.add(new ResourceLocation(Tags.MODID, "items/scraps_additive"));
        ModelBakery.registerItemVariants(this, variants.toArray(new ResourceLocation[0]));

        final ModelResourceLocation LIQUID_MRL =
                new ModelResourceLocation(new ResourceLocation(Tags.MODID, "items/scraps_liquid"), "inventory");
        final ModelResourceLocation ADDITIVE_MRL =
                new ModelResourceLocation(new ResourceLocation(Tags.MODID, "items/scraps_additive"), "inventory");

        ModelLoader.setCustomMeshDefinition(this, stack -> {
            if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("liquid")) {
                NTMMaterial mat = Mats.matById.get(stack.getMetadata());
                if (mat != null) {
                    return mat.smeltable == NTMMaterial.SmeltingBehavior.ADDITIVE ? ADDITIVE_MRL : LIQUID_MRL;
                }
            }
            NTMMaterial mat = Mats.matById.get(stack.getMetadata());
            if (mat != null) {
                String path = ItemScraps.this.getTexturePath(mat);
                return new ModelResourceLocation(new ResourceLocation(Tags.MODID, path), "inventory");
            }
            return LIQUID_MRL;
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {

        if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("liquid")) {
            Mats.MaterialStack contents = getMats(stack);
            if(contents != null) {
                return I18nUtil.resolveKey(contents.material.getTranslationKey());
            }
        }

        return ("" + I18n.format(this.getUnlocalizedNameInefficiently(stack) + ".name")).trim();
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        Mats.MaterialStack contents = getMats(stack);

        if(contents != null) {

            if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("liquid")) {
                list.add(Mats.formatAmount(contents.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)));
                if(contents.material.smeltable == contents.material.smeltable.ADDITIVE) list.add(TextFormatting.DARK_RED + "Additive, not castable!");
            } else {
                list.add(I18nUtil.resolveKey(contents.material.getTranslationKey()) + ", " + Mats.formatAmount(contents.amount, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)));
            }
        }
    }

    public static Mats.MaterialStack getMats(ItemStack stack) {

        if(stack.getItem() != ModItems.scraps) return null;

        NTMMaterial mat = Mats.matById.get(stack.getItemDamage());
        if(mat == null) return null;

        int amount = MaterialShapes.INGOT.q(1);

        if(stack.hasTagCompound()) {
            amount = stack.getTagCompound().getInteger("amount");
        }

        return new Mats.MaterialStack(mat, amount);
    }

    public static ItemStack create(Mats.MaterialStack stack) {
        return create(stack, false);
    }

    public static ItemStack create(Mats.MaterialStack stack, boolean liquid) {
        if(stack.material == null)
            return new ItemStack(ModItems.nothing); //why do i bother adding checks for fucking everything when they don't work
        ItemStack scrap = new ItemStack(ModItems.scraps, 1, stack.material.id);
        scrap.setTagCompound(new NBTTagCompound());
        scrap.getTagCompound().setInteger("amount", stack.amount);
        if(liquid) scrap.getTagCompound().setBoolean("liquid", true);
        return scrap;
    }
}
