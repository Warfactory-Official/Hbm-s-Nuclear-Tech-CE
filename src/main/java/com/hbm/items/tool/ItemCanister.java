package com.hbm.items.tool;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemCanister extends Item implements IDynamicModels {

    public static final ModelResourceLocation fluidCanisterModel = new ModelResourceLocation(Tags.MODID + ":canister_empty", "inventory");
    public static final String overlay = "canister_overlay";
    public static final String base = "canister_empty";
    public static final ResourceLocation canisterFullLoc = new ResourceLocation(Tags.MODID, ROOT_PATH + "canister_full");
    public int cap;


    public ItemCanister(String s, int cap) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.controlTab);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.cap = cap;
        ModItems.ALL_ITEMS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    public static ItemStack getStackFromFluid(FluidType f) {
        return new ItemStack(ModItems.canister_full, 1, f.getID());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        String s = ("" + I18n.format("item.canister_full.name")).trim();
        String s1 = ("" + I18n.format(Fluids.fromID(stack.getItemDamage()).getConditionalName())).trim();

        if (!s1.equals(Fluids.NONE.getConditionalName())) {
            s = s + " " + s1;
        } else return I18n.format("item.canister_empty.name");

        return s;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH) {
            FluidType[] order = Fluids.getInNiceOrder();
            for (int i = 1; i < order.length; ++i) {
                FluidType type = order[i];

                if (type.getContainer(Fluids.CD_Canister.class) != null) {
                    if (type != Fluids.NONE) items.add(new ItemStack(this, 1, type.getID()));
                }
            }
        }
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
            ResourceLocation baseTexture = new ResourceLocation(Tags.MODID, ROOT_PATH + base);
            ResourceLocation overlayTexture = new ResourceLocation(Tags.MODID, ROOT_PATH + overlay);
            IModel retexturedModel = baseModel.retexture(
                    ImmutableMap.of(
                            "layer0", baseTexture.toString(),
                            "layer1", overlayTexture.toString()
                    )

            );
            IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
            ModelResourceLocation bakedModelLocation = new ModelResourceLocation(canisterFullLoc, "inventory");
            event.getModelRegistry().putObject(bakedModelLocation, bakedModel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void registerModel() {
        for (short i = 0; i < cap; i++)
            ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(canisterFullLoc, "inventory"));
    }

    @Override
    public void registerSprite(TextureMap map) {

        map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + base));
        map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + overlay));
        map.registerSprite(canisterFullLoc);
    }

    @SideOnly(Side.CLIENT)
    public IItemColor getItemColorHandler() {
        return (stack, tintIndex) ->
        {
            if (tintIndex == 0) {
                return 16777215;
            } else {

                Fluids.CD_Canister canister = Fluids.fromID(stack.getItemDamage()).getContainer(Fluids.CD_Canister.class);
                int j = canister == null ? -1 : canister.color;

                if (j < 0) {
                    j = 16777215;
                }

                return j;
            }


        };
    }
}
